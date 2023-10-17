package no.nav.obo_unleash;

import com.nimbusds.openid.connect.sdk.claims.IDTokenClaimsSet;
import no.nav.common.featuretoggle.UnleashClient;
import no.nav.obo_unleash.auth.TokenValidator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.util.Optional;

import static org.mockito.Mockito.mock;

@Configuration
@Import({FeatureController.class })
public class TestApplicationConfig {

    @Bean
    public UnleashClient unleashClient() {
        return mock(UnleashClient.class);
    }

    @Bean
    public TokenValidator tokenValidator() {
        return new TokenValidator() {
            @Override
            public Optional<IDTokenClaimsSet> validate(String token) {
                return Optional.empty();
            }
        };
    }
}
