package no.nav.obo_unleash;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import no.nav.common.client.axsys.AxsysClient;
import no.nav.common.client.msgraph.AdGroupData;
import no.nav.common.client.msgraph.MsGraphClient;
import no.nav.common.token_client.client.AzureAdMachineToMachineTokenClient;
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
    private AzureAdMachineToMachineTokenClient tokenClient;

    @Mock
    private EnvironmentProperties environmentProperties;

    @Mock
    private NaisEnv naisEnv;

    @Mock
    private AxsysClient axsysClient;

    private ByEnhetAndEnvironmentStrategy strategy;
    private List<AdGroupData> testAdGroupData;

    @BeforeEach
    void setup() throws IOException {
        strategy = new ByEnhetAndEnvironmentStrategy(axsysClient, naisEnv, msGraphClient,
                environmentProperties, tokenClient);

        // Load test data from JSON file
        String jsonContent = new String(Files.readAllBytes(Paths.get("src/test/resources/adGroupData.json")));
        ObjectMapper objectMapper = new ObjectMapper();
        testAdGroupData = objectMapper.readValue(jsonContent, new TypeReference<List<AdGroupData>>() {});
    }

    @Test
    void hentEnheterFraEntraId_shouldFilterAndExtractCorrectEnhetIds() {
        // Given
        String navIdent = "Z999999";
        String scope = "api://dev.scope";
        String token = "dummy-token";

        when(environmentProperties.getMicrosoftGraphScope()).thenReturn(scope);
        when(tokenClient.createMachineToMachineToken(scope)).thenReturn(token);
        when(msGraphClient.hentAdGroupsForUser(token, navIdent)).thenReturn(testAdGroupData);

        // When
        List<String> result = strategy.hentEnheterFraEntraId(navIdent);

        // Then
        assertEquals(14, result.size());
        assertTrue(result.contains("1234"));
        assertTrue(result.contains("4321"));
        assertTrue(result.contains("0100"));
        assertTrue(result.contains("0200"));
        assertTrue(result.contains("0300"));

        // Verify none of the non-enhet groups were included
        assertFalse(result.contains("Team_Alpha"));
        assertFalse(result.contains("Team_Beta"));
        assertFalse(result.contains("IT-Support"));
    }
}