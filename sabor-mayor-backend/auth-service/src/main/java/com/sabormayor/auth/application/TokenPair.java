package com.sabormayor.auth.application;

public record TokenPair(String accessToken, String refreshToken, long expiresInSeconds) {
}
