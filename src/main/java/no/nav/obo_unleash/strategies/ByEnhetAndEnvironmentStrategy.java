package no.nav.obo_unleash.strategies;

import io.getunleash.UnleashContext;
import io.getunleash.strategy.Strategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.nav.common.auth.context.AuthContextHolder;
import no.nav.common.client.axsys.AxsysClient;
import no.nav.common.client.msgraph.AdGroupData;
import no.nav.common.client.msgraph.AdGroupFilter;
import no.nav.common.client.msgraph.MsGraphClient;
import no.nav.common.token_client.client.AzureAdOnBehalfOfTokenClient;
import no.nav.common.types.identer.NavIdent;
import no.nav.obo_unleash.config.EnvironmentProperties;
import no.nav.obo_unleash.env.NaisEnv;
import no.nav.obo_unleash.utils.NAVidentUtils;
import no.nav.obo_unleash.utils.MsGraphUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.*;

import static java.util.stream.Collectors.toList;


@Component
@RequiredArgsConstructor
@Slf4j
public class ByEnhetAndEnvironmentStrategy implements Strategy {

    static final String PARAM = "valgtEnhet";
    static final String MILJO_PARAM = "tilgjengeligIProd";
    static final String TEMA_OPPFOLGING = "OPP";
    private final AxsysClient axsysClient;

    private final NaisEnv naisEnv;
    private final MsGraphClient msGraphClient;
    private final EnvironmentProperties environmentProperties;
    private final AzureAdOnBehalfOfTokenClient azureAdOnBehalfOfTokenClient;
    private final AuthContextHolder authContextHolder;

    @NotNull
    @Override
    public String getName() {
        return "byEnhetAndEnvironment";
    }

    @Override
    public boolean isEnabled(@NotNull Map<String, String> parameters, UnleashContext unleashContext) {
        boolean enhetValgt = unleashContext.getUserId()
                .flatMap(currentUserId -> Optional.ofNullable(parameters.get(PARAM))
                        .map(enheterString -> Set.of(enheterString.split(",\\s?")))
                        .map(enabledeEnheter -> !Collections.disjoint(enabledeEnheter, brukersEnheter(currentUserId))))
                .orElse(false);

        if (!enhetValgt) return false;

        return (naisEnv.isLocal() || naisEnv.isDevGCP()) || Objects.equals(parameters.get(MILJO_PARAM), "true");
    }

    private List<String> brukersEnheter(String navIdent) {
        if (!NAVidentUtils.erNavIdent(navIdent)) {
            log.warn("Fikk ident som ikke er en NAVident. Om man ser mye av denne feilen bør man utforske hvorfor.");
            return Collections.emptyList();
        }
        List<String> enheterFraAxsys;
        List<String> enheterFraEntra;

        enheterFraAxsys = hentEnheter(navIdent);
        try {
            enheterFraEntra = hentEnheterFraEntraId();
            if (!enheterFraEntra.isEmpty()) {
                log.info(
                        "Første enhet fra Entra: {} Antall enheter fra Entra: {} Antall enheter fra Axsys: {}",
                        enheterFraEntra.getFirst(), enheterFraEntra.size(), enheterFraAxsys.size()
                );
            } else {
                log.info("Ingen enheter funnet fra Entra");
            }
        } catch (Exception e) {
            log.error("Feil ved henting av enheter fra Entra: {}", e.getMessage(), e);
        }

        return enheterFraAxsys;
    }

    private List<String> hentEnheter(String navIdent) {
        return axsysClient.hentTilganger(new NavIdent(navIdent)).stream()
                .filter(enhet -> enhet.getTemaer().contains(TEMA_OPPFOLGING))
                .map(enhet -> enhet.getEnhetId().get()).collect(toList());
    }

    public List<String> hentEnheterFraEntraId() {
        List<AdGroupData> adGroups = msGraphClient.hentAdGroupsForUser(
                azureAdOnBehalfOfTokenClient.exchangeOnBehalfOfToken(
                        environmentProperties.getMicrosoftGraphScope(),
                        authContextHolder.requireIdTokenString()
                ),
                AdGroupFilter.ENHET
        );

        return adGroups.stream()
                .map(AdGroupData::displayName)
                .filter(Objects::nonNull)
                .map(MsGraphUtils::tilEnhetId)
                .collect(toList());
    }

}