package com.sabormayor.auth.application;

/**
 * Verifies an external identity-provider token (Google/Apple ID token) and
 * returns the verified identity. Implementations: a mock for dev profiles and
 * real verifiers for production.
 */
public interface OAuthTokenVerifier {

    VerifiedOAuthUser verify(String provider, String idToken);

    record VerifiedOAuthUser(String providerId, String email, String fullName) {
    }
}
