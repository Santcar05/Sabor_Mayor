package com.sabormayor.user.web;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sabormayor.user.application.ProfileService;
import com.sabormayor.user.mapper.UserProfileMapper;
import com.sabormayor.user.web.dto.AddressRequest;
import com.sabormayor.user.web.dto.AddressResponse;
import com.sabormayor.user.web.dto.PaymentMethodRequest;
import com.sabormayor.user.web.dto.PaymentMethodResponse;
import com.sabormayor.user.web.dto.ProfileResponse;
import com.sabormayor.user.web.dto.UpdateProfileRequest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "Customer profile, addresses and payment method references")
public class UserController {

    private final ProfileService profileService;
    private final UserProfileMapper mapper;

    @GetMapping("/me")
    @Operation(summary = "Current customer's profile")
    public ProfileResponse myProfile(@AuthenticationPrincipal Jwt jwt) {
        return mapper.toResponse(profileService.getProfile(UUID.fromString(jwt.getSubject())));
    }

    @PutMapping("/me")
    @Operation(summary = "Update profile, dietary preferences and allergies")
    public ProfileResponse updateProfile(@AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody UpdateProfileRequest request) {
        return mapper.toResponse(profileService.updateProfile(
                UUID.fromString(jwt.getSubject()), jwt.getClaimAsString("email"), request));
    }

    @GetMapping("/me/addresses")
    public List<AddressResponse> myAddresses(@AuthenticationPrincipal Jwt jwt) {
        return mapper.toAddressResponses(profileService.getAddresses(UUID.fromString(jwt.getSubject())));
    }

    @PostMapping("/me/addresses")
    public ResponseEntity<AddressResponse> addAddress(@AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody AddressRequest request) {
        AddressResponse response = mapper.toResponse(
                profileService.addAddress(UUID.fromString(jwt.getSubject()), request));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/me/addresses/{addressId}")
    public AddressResponse updateAddress(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID addressId,
            @Valid @RequestBody AddressRequest request) {
        return mapper.toResponse(
                profileService.updateAddress(UUID.fromString(jwt.getSubject()), addressId, request));
    }

    @DeleteMapping("/me/addresses/{addressId}")
    public ResponseEntity<Void> deleteAddress(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID addressId) {
        profileService.deleteAddress(UUID.fromString(jwt.getSubject()), addressId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me/payment-methods")
    public List<PaymentMethodResponse> myPaymentMethods(@AuthenticationPrincipal Jwt jwt) {
        return mapper.toPaymentMethodResponses(profileService.getPaymentMethods(UUID.fromString(jwt.getSubject())));
    }

    @PostMapping("/me/payment-methods")
    public ResponseEntity<PaymentMethodResponse> addPaymentMethod(@AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody PaymentMethodRequest request) {
        PaymentMethodResponse response = mapper.toResponse(
                profileService.addPaymentMethod(UUID.fromString(jwt.getSubject()), request));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/me/payment-methods/{paymentMethodId}")
    public ResponseEntity<Void> deletePaymentMethod(@AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID paymentMethodId) {
        profileService.deletePaymentMethod(UUID.fromString(jwt.getSubject()), paymentMethodId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    @Operation(summary = "List customers (admin)")
    public Page<ProfileResponse> listCustomers(Pageable pageable) {
        return profileService.listCustomers(pageable).map(mapper::toResponse);
    }

    @GetMapping("/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN','MESERO')")
    @Operation(summary = "Customer detail, e.g. allergies for waiters (staff)")
    public ProfileResponse getCustomer(@PathVariable UUID userId) {
        return mapper.toResponse(profileService.getProfile(userId));
    }
}
