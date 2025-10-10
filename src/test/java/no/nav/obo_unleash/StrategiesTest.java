package no.nav.obo_unleash;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import no.nav.common.auth.context.AuthContextHolderThreadLocal;
import no.nav.common.client.msgraph.AdGroupData;
import no.nav.common.client.msgraph.AdGroupFilter;
import no.nav.common.client.msgraph.MsGraphClient;
import no.nav.common.token_client.client.AzureAdOnBehalfOfTokenClient;
import no.nav.obo_unleash.config.EnvironmentProperties;
import no.nav.obo_unleash.env.NaisEnv;
import no.nav.obo_unleash.strategies.ByEnhetAndEnvironmentStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StrategiesTest {

    @Mock
    private MsGraphClient msGraphClient;

    @Mock
    private AzureAdOnBehalfOfTokenClient tokenClient;

    @Mock
    private AuthContextHolderThreadLocal authContextHolder;

    @Mock
    private EnvironmentProperties environmentProperties;

    @Mock
    private NaisEnv naisEnv;

    private ByEnhetAndEnvironmentStrategy strategy;
    private List<AdGroupData> testAdGroupData;

    @BeforeEach
    void setup() throws IOException {
        strategy = new ByEnhetAndEnvironmentStrategy(naisEnv, msGraphClient,
                environmentProperties, tokenClient, authContextHolder);

        String jsonContent = new String(Files.readAllBytes(Paths.get("src/test/resources/adGroupData.json")));
        ObjectMapper objectMapper = new ObjectMapper();
        testAdGroupData = objectMapper.readValue(jsonContent, new TypeReference<>() {
        });
    }

    @Test
    void hentEnheterFraEntraId_shouldFilterAndExtractCorrectEnhetIds() {

        String navIdent = "Z999999";
        String scope = "api://dev.scope";
        String token = "dummy-token";

        when(environmentProperties.getMicrosoftGraphScope()).thenReturn(scope);
        when(tokenClient.exchangeOnBehalfOfToken(scope, navIdent)).thenReturn(token);
        when(msGraphClient.hentAdGroupsForUser(token, AdGroupFilter.ENHET)).thenReturn(testAdGroupData);
        when(authContextHolder.requireIdTokenString()).thenReturn(navIdent);

        List<String> result = strategy.hentEnheterFraEntraId();

        assertEquals(14, result.size());
        assertTrue(result.contains("1234"));
        assertTrue(result.contains("4321"));
        assertTrue(result.contains("0100"));
        assertTrue(result.contains("0200"));
        assertTrue(result.contains("0300"));
    }
}