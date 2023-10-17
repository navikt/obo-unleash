package no.nav.obo_unleash;

import no.nav.common.auth.oidc.discovery.OidcDiscoveryConfiguration;
import no.nav.common.client.axsys.AxsysClient;
import no.nav.common.client.axsys.AxsysV2ClientImpl;
import no.nav.common.client.axsys.CachedAxsysClient;
import no.nav.common.featuretoggle.UnleashClient;
import no.nav.common.featuretoggle.UnleashClientImpl;
import no.nav.common.rest.filter.SetStandardHttpHeadersFilter;
import no.nav.common.token_client.builder.AzureAdTokenClientBuilder;
import no.nav.common.token_client.client.AzureAdMachineToMachineTokenClient;
import no.nav.obo_unleash.auth.TokenValidator;
import no.nav.obo_unleash.auth.TokenValidatorImpl;
import no.nav.obo_unleash.auth.discovery.OidcDiscoveryConfigurationClient;
import no.nav.obo_unleash.config.EnvironmentProperties;
import no.nav.obo_unleash.env.NaisEnv;
import no.nav.obo_unleash.strategies.ByEnhetAndEnvironmentStrategy;
import no.nav.obo_unleash.strategies.ByEnhetStrategy;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@EnableConfigurationProperties({EnvironmentProperties.class})
public class ApplicationConfig {

    private final static String APPLICATION_NAME = "obo-unleash";

    @Bean
    public FilterRegistrationBean<SetStandardHttpHeadersFilter> setStandardHttpHeadersFilter() {
        FilterRegistrationBean<SetStandardHttpHeadersFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new SetStandardHttpHeadersFilter());
        registration.setOrder(1);
        registration.addUrlPatterns("/*");
        return registration;
    }

    @Bean
    public UnleashClient unleashClient(EnvironmentProperties environmentProperties, ByEnhetStrategy byEnhetStrategy, ByEnhetAndEnvironmentStrategy byEnhetAndEnvironmentStrategy) {
        return new UnleashClientImpl(environmentProperties.getUnleashUrl(), APPLICATION_NAME, List.of(byEnhetStrategy, byEnhetAndEnvironmentStrategy));
    }

    @Bean
    public TokenValidator tokenValidator(EnvironmentProperties environmentProperties) {
        OidcDiscoveryConfigurationClient oidcClient = new OidcDiscoveryConfigurationClient();
        OidcDiscoveryConfiguration oidcDiscoveryConfiguration = oidcClient.fetchDiscoveryConfiguration(environmentProperties.getAzureAdDiscoveryUrl());
        return new TokenValidatorImpl(environmentProperties.getAzureAdClientId(), oidcDiscoveryConfiguration);
    }

    @Bean
    public AxsysClient axsysClient(EnvironmentProperties environmentProperties, AzureAdMachineToMachineTokenClient azureAdMachineToMachineTokenClient) {
        AxsysClient axsysClient = new AxsysV2ClientImpl(environmentProperties.getAxsysUrl(), () -> azureAdMachineToMachineTokenClient.createMachineToMachineToken(environmentProperties.getAxsysScope()));
        return new CachedAxsysClient(axsysClient);
    }

    @Bean
    public AzureAdMachineToMachineTokenClient azureAdMachineToMachineTokenClient() {
        return AzureAdTokenClientBuilder.builder()
                .withNaisDefaults()
                .buildMachineToMachineTokenClient();
    }

    @Bean
    public NaisEnv naisEnv() {
        return new NaisEnv();
    }
}
