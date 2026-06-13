package com.sabormayor.user.application;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sabormayor.common.error.ResourceNotFoundException;
import com.sabormayor.user.domain.Address;
import com.sabormayor.user.domain.CustomerProfile;
import com.sabormayor.user.domain.PaymentMethodRef;
import com.sabormayor.user.domain.StaffMember;
import com.sabormayor.user.infrastructure.AddressRepository;
import com.sabormayor.user.infrastructure.CustomerProfileRepository;
import com.sabormayor.user.infrastructure.PaymentMethodRefRepository;
import com.sabormayor.user.infrastructure.StaffMemberRepository;
import com.sabormayor.user.web.dto.AddressRequest;
import com.sabormayor.user.web.dto.PaymentMethodRequest;
import com.sabormayor.user.web.dto.UpdateProfileRequest;
import com.sabormayor.user.web.dto.UpdateStaffRequest;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private static final int FREQUENT_GUEST_VISITS = 5;

    private final CustomerProfileRepository profileRepository;
    private final AddressRepository addressRepository;
    private final PaymentMethodRefRepository paymentMethodRepository;
    private final StaffMemberRepository staffRepository;

    @Transactional(readOnly = true)
    public CustomerProfile getProfile(UUID userId) {
        return profileRepository.findById(userId)
                .orElseThrow(() -> ResourceNotFoundException.of("Profile", userId));
    }

    /** Upsert keeps the API usable even if the Kafka projection lagged behind. */
    @Transactional
    public CustomerProfile updateProfile(UUID userId, String email, UpdateProfileRequest request) {
        CustomerProfile profile = profileRepository.findById(userId)
                .orElseGet(() -> CustomerProfile.builder()
                        .id(userId)
                        .email(email != null ? email : "unknown-" + userId + "@sabormayor.local")
                        .fullName(request.fullName())
                        .build());
        profile.setFullName(request.fullName());
        profile.setPhone(request.phone());
        if (request.dietaryPreferences() != null) {
            profile.getDietaryPreferences().clear();
            profile.getDietaryPreferences().addAll(request.dietaryPreferences());
        }
        if (request.allergies() != null) {
            profile.getAllergies().clear();
            profile.getAllergies().addAll(request.allergies());
        }
        return profileRepository.save(profile);
    }

    @Transactional
    public void registerVisit(UUID userId) {
        profileRepository.findById(userId).ifPresent(profile -> {
            profile.setVisits(profile.getVisits() + 1);
            if (profile.getVisits() >= FREQUENT_GUEST_VISITS) {
                profile.setFrequentGuest(true);
            }
        });
    }

    @Transactional(readOnly = true)
    public List<Address> getAddresses(UUID userId) {
        return addressRepository.findByCustomerId(userId);
    }

    @Transactional
    public Address addAddress(UUID userId, AddressRequest request) {
        if (request.defaultAddress()) {
            addressRepository.findByCustomerId(userId).forEach(a -> a.setDefaultAddress(false));
        }
        return addressRepository.save(Address.builder()
                .customerId(userId)
                .label(request.label())
                .street(request.street())
                .city(request.city())
                .notes(request.notes())
                .defaultAddress(request.defaultAddress())
                .build());
    }

    @Transactional
    public Address updateAddress(UUID userId, UUID addressId, AddressRequest request) {
        Address address = addressRepository.findByIdAndCustomerId(addressId, userId)
                .orElseThrow(() -> ResourceNotFoundException.of("Address", addressId));
        if (request.defaultAddress() && !address.isDefaultAddress()) {
            addressRepository.findByCustomerId(userId).forEach(a -> a.setDefaultAddress(false));
        }
        address.setLabel(request.label());
        address.setStreet(request.street());
        address.setCity(request.city());
        address.setNotes(request.notes());
        address.setDefaultAddress(request.defaultAddress());
        return address;
    }

    @Transactional
    public void deleteAddress(UUID userId, UUID addressId) {
        Address address = addressRepository.findByIdAndCustomerId(addressId, userId)
                .orElseThrow(() -> ResourceNotFoundException.of("Address", addressId));
        addressRepository.delete(address);
    }

    @Transactional(readOnly = true)
    public List<PaymentMethodRef> getPaymentMethods(UUID userId) {
        return paymentMethodRepository.findByCustomerId(userId);
    }

    @Transactional
    public PaymentMethodRef addPaymentMethod(UUID userId, PaymentMethodRequest request) {
        return paymentMethodRepository.save(PaymentMethodRef.builder()
                .customerId(userId)
                .gatewayToken(request.gatewayToken())
                .brand(request.brand())
                .last4(request.last4())
                .build());
    }

    @Transactional
    public void deletePaymentMethod(UUID userId, UUID paymentMethodId) {
        PaymentMethodRef ref = paymentMethodRepository.findByIdAndCustomerId(paymentMethodId, userId)
                .orElseThrow(() -> ResourceNotFoundException.of("Payment method", paymentMethodId));
        paymentMethodRepository.delete(ref);
    }

    @Transactional(readOnly = true)
    public Page<CustomerProfile> listCustomers(Pageable pageable) {
        return profileRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public List<StaffMember> listStaff() {
        return staffRepository.findAll();
    }

    @Transactional
    public StaffMember updateStaff(UUID staffId, UpdateStaffRequest request) {
        StaffMember staff = staffRepository.findById(staffId)
                .orElseThrow(() -> ResourceNotFoundException.of("Staff member", staffId));
        staff.setPosition(request.position());
        staff.setActive(request.active());
        return staff;
    }
}
