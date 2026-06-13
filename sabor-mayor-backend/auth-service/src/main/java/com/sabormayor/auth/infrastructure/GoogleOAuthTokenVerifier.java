package com.sabormayor.auth.infrastructure;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.sabormayor.auth.application.OAuthTokenVerifier;
import com.sabormayor.common.error.ApiException;

/**
 * Production adapter. Google: validates the ID token against the tokeninfo
 * endpoint and checks the audience. Apple requires uploading the Sign in with
 * Apple keys; until configured it is rejected explicitly.
 */
@Component
@Profile("prod")
public class GoogleOAuthTokenVerifier implements OAuthTokenVerifier {

    private final RestClient restClient = RestClient.create();

    @Value("${app.oauth2.google.client-id:}")
    private String googleClientId;

    @Override
    @SuppressWarnings("unchecked")
    public VerifiedOAuthUser verify(String provider, String idToken) {
        if (!"google".equalsIgnoreCase(provider)) {
            throw new ApiException(HttpStatus.NOT_IMPLEMENTED,
                    "OAuth provider '%s' is not configured yet".formatted(provider));
        }
        Map<String, Object> body;
        try {
            body = restClient.get()
                    .uri("https://oauth2.googleapis.com/tokeninfo?id_token={token}", idToken)
                    .retrieve()
                    .body(Map.class);
        } catch (Exception ex) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Google token verification failed");
        }
        if (body == null || !googleClientId.equals(body.get("aud"))) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Google token audience mismatch");
        }
        return new VerifiedOAuthUser(
                String.valueOf(body.get("sub")),
                String.valueOf(body.get("email")),
                String.valueOf(body.getOrDefault("name", body.get("email"))));
    }
}
