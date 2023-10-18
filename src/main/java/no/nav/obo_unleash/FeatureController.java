package no.nav.obo_unleash;

import io.getunleash.DefaultUnleash;
import io.getunleash.UnleashContext;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import no.nav.obo_unleash.auth.TokenValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static no.nav.common.auth.Constants.AAD_NAV_IDENT_CLAIM;
import static no.nav.common.auth.utils.CookieUtils.getCookie;

@Slf4j
@RestController
@RequestMapping("/api/feature")
public class FeatureController {

    private static final String UNLEASH_SESSION_ID_COOKIE_NAME = "UNLEASH_SESSION_ID";
    private final DefaultUnleash unleashClient;
    private final TokenValidator tokenValidator;

    @Autowired
    public FeatureController(DefaultUnleash unleashClient, TokenValidator tokenValidator) {
        this.unleashClient = unleashClient;
        this.tokenValidator = tokenValidator;
    }

    @GetMapping
    public Map<String, Boolean> getFeatures(
            @RequestParam("feature") List<String> features,
            HttpServletRequest request,
            HttpServletResponse response
    ) {

        Optional<String> maybeNavident = Optional.ofNullable(request.getHeader("Authorization"))
                .map(authHeader -> {
                    String[] parts = authHeader.split(" ");
                    if(parts.length > 1) {
                        return parts[1];
                    }
                    return null;
                })
                .flatMap(tokenValidator::validate)
                .map(claims -> claims.getStringClaim(AAD_NAV_IDENT_CLAIM));

        String sessionId = getCookie(UNLEASH_SESSION_ID_COOKIE_NAME, request)
                .map(Cookie::getValue)
                .orElseGet(() -> generateSessionId(response));

        UnleashContext unleashContext = UnleashContext.builder()
                .userId(maybeNavident.orElse(null))
                .sessionId(sessionId)
                .remoteAddress(request.getRemoteAddr())
                .build();

        return features.stream().collect(Collectors.toMap(e -> e, e -> unleashClient.isEnabled(e, unleashContext)));
    }

    private String generateSessionId(HttpServletResponse httpServletRequest) {
        UUID uuid = UUID.randomUUID();
        String sessionId = Long.toHexString(uuid.getMostSignificantBits()) + Long.toHexString(uuid.getLeastSignificantBits());
        Cookie cookie = new Cookie(UNLEASH_SESSION_ID_COOKIE_NAME, sessionId);
        cookie.setPath("/");
        cookie.setMaxAge(-1);
        httpServletRequest.addCookie(cookie);
        return sessionId;
    }
}
