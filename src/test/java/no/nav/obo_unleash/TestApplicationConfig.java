package no.nav.obo_unleash;

import io.getunleash.DefaultUnleash;
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
    public DefaultUnleash unleashClient() {
        return mock(DefaultUnleash.class);
    }

    @Bean
    public TokenValidator tokenValidator() {
        return token -> Optional.empty();
    }
}
