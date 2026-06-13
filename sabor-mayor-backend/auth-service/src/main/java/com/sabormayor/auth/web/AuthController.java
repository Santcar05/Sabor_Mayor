package com.sabormayor.auth.web;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sabormayor.auth.application.AuthService;
import com.sabormayor.auth.mapper.UserMapper;
import com.sabormayor.auth.web.dto.CreateStaffRequest;
import com.sabormayor.auth.web.dto.LoginRequest;
import com.sabormayor.auth.web.dto.OAuthLoginRequest;
import com.sabormayor.auth.web.dto.RefreshRequest;
import com.sabormayor.auth.web.dto.RegisterRequest;
import com.sabormayor.auth.web.dto.TokenResponse;
import com.sabormayor.auth.web.dto.UserResponse;
import com.sabormayor.common.security.Roles;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "Registration, login, token lifecycle and staff management")
public class AuthController {

    private final AuthService authService;
    private final UserMapper userMapper;

    @PostMapping("/register")
    @Operation(summary = "Register a new customer account")
    public ResponseEntity<TokenResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(TokenResponse.from(authService.register(request)));
    }

    @PostMapping("/login")
    @Operation(summary = "Login with email/password")
    public TokenResponse login(@Valid @RequestBody LoginRequest request) {
        return TokenResponse.from(authService.login(request));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Rotate the refresh token and obtain a new token pair")
    public TokenResponse refresh(@Valid @RequestBody RefreshRequest request) {
        return TokenResponse.from(authService.refresh(request.refreshToken()));
    }

    @PostMapping("/oauth2/{provider}")
    @Operation(summary = "Login/registration with an external identity provider (google, apple)")
    public TokenResponse oauthLogin(@PathVariable String provider, @Valid @RequestBody OAuthLoginRequest request) {
        return TokenResponse.from(authService.oauthLogin(provider, request.idToken()));
    }

    @PostMapping("/logout")
    @Operation(summary = "Revoke every refresh token of the user and denylist the current access token")
    public ResponseEntity<Void> logout(@AuthenticationPrincipal Jwt jwt) {
        authService.logout(UUID.fromString(jwt.getSubject()), jwt.getId(), jwt.getExpiresAt());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    @Operation(summary = "Current authenticated user")
    public UserResponse me(@AuthenticationPrincipal Jwt jwt) {
        return userMapper.toResponse(authService.getUser(UUID.fromString(jwt.getSubject())));
    }

    @DeleteMapping("/me")
    @Operation(summary = "Habeas Data: delete/anonymize the account and propagate deletion")
    public ResponseEntity<Void> deleteAccount(@AuthenticationPrincipal Jwt jwt) {
        authService.deleteAccount(UUID.fromString(jwt.getSubject()));
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/staff")
    @PreAuthorize("hasAnyRole('" + Roles.ADMIN + "','" + Roles.SUPER_ADMIN + "')")
    @Operation(summary = "Create a staff account (MESERO/COCINERO; ADMIN only by SUPER_ADMIN)")
    public ResponseEntity<UserResponse> createStaff(@Valid @RequestBody CreateStaffRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        boolean isSuperAdmin = Roles.SUPER_ADMIN.equals(jwt.getClaimAsString("role"));
        UserResponse response = userMapper.toResponse(
                authService.createStaff(request, UUID.fromString(jwt.getSubject()), isSuperAdmin));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
