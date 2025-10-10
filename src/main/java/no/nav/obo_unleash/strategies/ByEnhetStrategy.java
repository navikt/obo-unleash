package no.nav.obo_unleash.strategies;

import io.getunleash.UnleashContext;
import io.getunleash.strategy.Strategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.nav.common.auth.context.AuthContextHolder;
import no.nav.common.client.msgraph.AdGroupData;
import no.nav.common.client.msgraph.AdGroupFilter;
import no.nav.common.client.msgraph.MsGraphClient;
import no.nav.common.token_client.client.AzureAdOnBehalfOfTokenClient;
import no.nav.obo_unleash.config.EnvironmentProperties;
import no.nav.obo_unleash.utils.MsGraphUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.*;

import static java.util.stream.Collectors.toList;

@Component
@RequiredArgsConstructor
@Slf4j
public class ByEnhetStrategy implements Strategy {

    static final String PARAM = "valgtEnhet";
    private final MsGraphClient msGraphClient;
    private final EnvironmentProperties environmentProperties;
    private final AzureAdOnBehalfOfTokenClient azureAdOnBehalfOfTokenClient;
    private final AuthContextHolder authContextHolder;


    @NotNull
    @Override
    public String getName() {
        return "byEnhet";
    }

    @Override
    public boolean isEnabled(@NotNull Map<String, String> parameters, UnleashContext unleashContext) {
        return unleashContext.getUserId()
                .flatMap(currentUserId -> Optional.ofNullable(parameters.get(PARAM))
                        .map(enheterString -> Set.of(enheterString.split(",\\s?")))
                        .map(enabledeEnheter -> !Collections.disjoint(enabledeEnheter, brukersEnheter())))
                .orElse(false);
    }

    private List<String> brukersEnheter() {
        List<String> enheterFraEntra = hentEnheterFraEntraId();

        try {
            if (!enheterFraEntra.isEmpty()) {
                log.info(
                        "Første enhet fra Entra: {} Antall enheter fra Entra: {}",
                        enheterFraEntra.getFirst(), enheterFraEntra.size()
                );
            } else {
                log.info("Ingen enheter funnet fra Entra");
            }
        } catch (Exception e) {
            log.error("ByEnhet: Klarte ikke hente enheter fra EntraId. Feilmelding: {}", e.getMessage());
        }
        return enheterFraEntra;
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