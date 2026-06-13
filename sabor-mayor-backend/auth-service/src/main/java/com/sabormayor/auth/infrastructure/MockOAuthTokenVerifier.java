package com.sabormayor.auth.infrastructure;

import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.sabormayor.auth.application.OAuthTokenVerifier;
import com.sabormayor.common.error.ApiException;

/**
 * Dev adapter: accepts tokens with the shape
 * {@code mock:<email>:<full name>} so the whole OAuth2 flow can be exercised
 * without real Google/Apple credentials.
 */
@Component
@Profile("!prod")
public class MockOAuthTokenVerifier implements OAuthTokenVerifier {

    @Override
    public VerifiedOAuthUser verify(String provider, String idToken) {
        if (idToken == null || !idToken.startsWith("mock:")) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid OAuth token (dev expects mock:<email>:<name>)");
        }
        String[] parts = idToken.split(":", 3);
        if (parts.length < 3 || parts[1].isBlank()) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid mock OAuth token format");
        }
        return new VerifiedOAuthUser(provider + "-" + parts[1], parts[1], parts[2]);
    }
}
