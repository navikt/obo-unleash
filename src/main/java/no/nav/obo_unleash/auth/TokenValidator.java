package no.nav.obo_unleash.auth;

import com.nimbusds.openid.connect.sdk.claims.IDTokenClaimsSet;

import java.util.Optional;

public interface TokenValidator {
    Optional<IDTokenClaimsSet> validate(String token);

}
