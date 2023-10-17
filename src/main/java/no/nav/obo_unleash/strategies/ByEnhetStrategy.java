package no.nav.obo_unleash.strategies;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.finn.unleash.UnleashContext;
import no.finn.unleash.strategy.Strategy;
import no.nav.common.client.axsys.AxsysClient;
import no.nav.common.types.identer.NavIdent;
import no.nav.obo_unleash.utils.NAVidentUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.*;

import static java.util.stream.Collectors.toList;

@Component
@RequiredArgsConstructor
@Slf4j
public class ByEnhetStrategy implements Strategy {

    static final String PARAM = "valgtEnhet";
    static final String TEMA_OPPFOLGING = "OPP";
    private final AxsysClient axsysClient;

    @NotNull
    @Override
    public String getName() {
        return "byEnhet";
    }

    @Override
    public boolean isEnabled(@NotNull Map<String, String> parameters) {
        return false;
    }

    @Override
    public boolean isEnabled(@NotNull Map<String, String> parameters, UnleashContext unleashContext) {
        return unleashContext.getUserId()
                .flatMap(currentUserId -> Optional.ofNullable(parameters.get(PARAM))
                        .map(enheterString -> Set.of(enheterString.split(",\\s?")))
                        .map(enabledeEnheter -> !Collections.disjoint(enabledeEnheter, brukersEnheter(currentUserId))))
                .orElse(false);
    }

    private List<String> brukersEnheter(String navIdent) {
        if (!NAVidentUtils.erNavIdent(navIdent)) {
            log.warn("Fikk ident som ikke er en NAVident. Om man ser mye av denne feilen bør man utforske hvorfor.");
            return Collections.emptyList();
        }

        return hentEnheter(navIdent);
    }

    private List<String> hentEnheter(String navIdent) {
        return axsysClient.hentTilganger(new NavIdent(navIdent)).stream()
                .filter(enhet -> enhet.getTemaer().contains(TEMA_OPPFOLGING))
                .map(enhet -> enhet.getEnhetId().get()).collect(toList());
    }
}
