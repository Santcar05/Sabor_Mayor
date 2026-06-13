package com.sabormayor.auth.application;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sabormayor.auth.domain.AuditLogEntry;
import com.sabormayor.auth.domain.AuthProvider;
import com.sabormayor.auth.domain.RefreshToken;
import com.sabormayor.auth.domain.Role;
import com.sabormayor.auth.domain.UserAccount;
import com.sabormayor.auth.infrastructure.AuditLogRepository;
import com.sabormayor.auth.infrastructure.TokenDenylist;
import com.sabormayor.auth.infrastructure.UserAccountRepository;
import com.sabormayor.auth.infrastructure.UserEventPublisher;
import com.sabormayor.auth.web.dto.CreateStaffRequest;
import com.sabormayor.auth.web.dto.LoginRequest;
import com.sabormayor.auth.web.dto.RegisterRequest;
import com.sabormayor.common.error.ApiException;
import com.sabormayor.common.error.ConflictException;
import com.sabormayor.common.error.ResourceNotFoundException;
import com.sabormayor.common.events.UserDeletedEvent;
import com.sabormayor.common.events.UserRegisteredEvent;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserAccountRepository userRepository;
    private final AuditLogRepository auditLogRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;
    private final TokenDenylist tokenDenylist;
    private final UserEventPublisher eventPublisher;
    private final OAuthTokenVerifier oAuthTokenVerifier;

    @Transactional
    public TokenPair register(RegisterRequest request) {
        if (userRepository.existsByEmailIgnoreCase(request.email())) {
            throw new ConflictException("Email is already registered");
        }
        UserAccount user = userRepository.save(UserAccount.builder()
                .email(request.email().toLowerCase())
                .passwordHash(passwordEncoder.encode(request.password()))
                .fullName(request.fullName())
                .role(Role.CLIENTE)
                .provider(AuthProvider.LOCAL)
                .enabled(true)
                .build());
        eventPublisher.publishUserRegistered(new UserRegisteredEvent(
                user.getId(), user.getEmail(), user.getFullName(), user.getRole().name(), Instant.now()));
        return tokenService.issue(user);
    }

    @Transactional
    public TokenPair login(LoginRequest request) {
        UserAccount user = userRepository.findByEmailIgnoreCase(request.email())
                .filter(UserAccount::isEnabled)
                .filter(u -> u.getPasswordHash() != null
                        && passwordEncoder.matches(request.password(), u.getPasswordHash()))
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));
        return tokenService.issue(user);
    }

    @Transactional
    public TokenPair refresh(String rawRefreshToken) {
        RefreshToken presented = tokenService.validateAndRevoke(rawRefreshToken);
        UserAccount user = userRepository.findById(presented.getUserId())
                .filter(UserAccount::isEnabled)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Account disabled or removed"));
        return tokenService.issue(user);
    }

    @Transactional
    public void logout(UUID userId, String accessTokenJti, Instant accessTokenExpiry) {
        tokenService.revokeAllForUser(userId);
        if (accessTokenJti != null && accessTokenExpiry != null) {
            tokenDenylist.revoke(accessTokenJti, Duration.between(Instant.now(), accessTokenExpiry));
        }
    }

    @Transactional(readOnly = true)
    public UserAccount getUser(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> ResourceNotFoundException.of("User", userId));
    }

    @Transactional
    public TokenPair oauthLogin(String provider, String idToken) {
        AuthProvider authProvider = parseProvider(provider);
        OAuthTokenVerifier.VerifiedOAuthUser verified = oAuthTokenVerifier.verify(provider, idToken);
        UserAccount user = userRepository.findByProviderAndProviderId(authProvider, verified.providerId())
                .orElseGet(() -> userRepository.findByEmailIgnoreCase(verified.email())
                        .map(existing -> {
                            existing.setProvider(authProvider);
                            existing.setProviderId(verified.providerId());
                            return existing;
                        })
                        .orElseGet(() -> {
                            UserAccount created = userRepository.save(UserAccount.builder()
                                    .email(verified.email().toLowerCase())
                                    .fullName(verified.fullName())
                                    .role(Role.CLIENTE)
                                    .provider(authProvider)
                                    .providerId(verified.providerId())
                                    .enabled(true)
                                    .build());
                            eventPublisher.publishUserRegistered(new UserRegisteredEvent(
                                    created.getId(), created.getEmail(), created.getFullName(),
                                    created.getRole().name(), Instant.now()));
                            return created;
                        }));
        if (!user.isEnabled()) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Account disabled");
        }
        return tokenService.issue(user);
    }

    @Transactional
    public UserAccount createStaff(CreateStaffRequest request, UUID actorId, boolean actorIsSuperAdmin) {
        if (userRepository.existsByEmailIgnoreCase(request.email())) {
            throw new ConflictException("Email is already registered");
        }
        Role role = request.role();
        if (role == Role.SUPER_ADMIN || (role == Role.ADMIN && !actorIsSuperAdmin)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Not allowed to create users with role " + role);
        }
        UserAccount staff = userRepository.save(UserAccount.builder()
                .email(request.email().toLowerCase())
                .passwordHash(passwordEncoder.encode(request.password()))
                .fullName(request.fullName())
                .role(role)
                .provider(AuthProvider.LOCAL)
                .enabled(true)
                .build());
        audit(actorId, "STAFF_CREATED", "Created %s account for %s".formatted(role, staff.getEmail()));
        eventPublisher.publishUserRegistered(new UserRegisteredEvent(
                staff.getId(), staff.getEmail(), staff.getFullName(), staff.getRole().name(), Instant.now()));
        return staff;
    }

    /** Habeas Data (Colombia): anonymizes the account and notifies other services. */
    @Transactional
    public void deleteAccount(UUID userId) {
        UserAccount user = userRepository.findById(userId)
                .orElseThrow(() -> ResourceNotFoundException.of("User", userId));
        String originalEmail = user.getEmail();
        user.setEnabled(false);
        user.setEmail("deleted-" + userId + "@anonymized.local");
        user.setFullName("Deleted user");
        user.setPasswordHash(null);
        user.setProviderId(null);
        tokenService.revokeAllForUser(userId);
        audit(userId, "ACCOUNT_DELETED", "Account deletion requested by the user (Habeas Data)");
        eventPublisher.publishUserDeleted(new UserDeletedEvent(userId, originalEmail, Instant.now()));
    }

    private void audit(UUID actorId, String action, String detail) {
        auditLogRepository.save(AuditLogEntry.builder().actorId(actorId).action(action).detail(detail).build());
    }

    private AuthProvider parseProvider(String provider) {
        try {
            return AuthProvider.valueOf(provider.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Unknown OAuth provider: " + provider);
        }
    }
}
