# obo-unleash

Tjeneste for å gjøre kall mot Unleash Next med tilpassede strategier.

_Forket fra [poao-unleash](https://github.com/navikt/poao-unleash)._


## Strategier
Tjenesten støtter følgende strategier

### byCluster
byCluster-strategien kan brukes for å skru på funksjonalitet for et gitt cluster. Feks. `dev-gcp`

### userWithId
userWithId-strategien kan brukes for å gi en eller flere brukeridenter tilgang til funksjonalitet.

### byEnhetAndEnvironment
byEnhetAndEnvironment-strategien brukes for å skru på funksjonalitet for en gitt enhet med `fagområdet/tema = oppfølging (OPP)`. Feks vil verdien 0106 skru på funksjonalitet for alle i Nav-enheten Fredrikstad som har fagområdet `OPP` (oppfølging), men med mulighet for å skru av i produksjon. På den måten kan man teste enheten i dev før man skrur på i produksjon.

### byEnhet
byEnhet-strategien brukes for å skru på funksjonalitet for en gitt enhet med `fagområdet/tema = oppfølging (OPP)`. Feks vil verdien 0106 skru på funksjonalitet for alle i Nav-enheten Fredrikstad som har fagområdet `OPP` (oppfølging).

## Arkitektur

```mermaid
sequenceDiagram
	participant FE as Frontend-applikasjon
	participant OBO as obo-unleash (denne tjenesten)
	participant IDP as Azure AD / OIDC Discovery
	participant UNL as Unleash Server (Next)

	Note over FE,OBO: Frontend henter feature-flagg fra denne tjenesten
	FE->>OBO: GET /api/feature?feature=A&feature=B\nAuthorization: Bearer <token>

	Note over OBO,IDP: Tjenesten validerer token og henter navIdent
	OBO->>IDP: Valider token (OIDC discovery/JWKS)
	IDP-->>OBO: Claims (navIdent)

	Note over OBO,FE: Tjenesten sikrer en stabil sessionId for Unleash-context
	OBO-->>FE: Set-Cookie: UNLEASH_SESSION_ID=... (hvis mangler)

	Note over OBO,UNL: Tjenesten fungerer som en “proxy/fasade” mot Unleash
	OBO->>UNL: Hent/refresh toggles (server-side SDK)\nUNLEASH_SERVER_API_TOKEN
	UNL-->>OBO: Toggle-definisjoner + strategier

	OBO->>OBO: Evaluer toggles lokalt med context\nuserId=navIdent, sessionId, remoteAddress
	OBO-->>FE: { "A": true, "B": false }
```

## Kode generert av GitHub Copilot

Dette repoet bruker GitHub Copilot til å generere kode.
