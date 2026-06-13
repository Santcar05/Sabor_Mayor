package com.sabormayor.user.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sabormayor.common.events.UserDeletedEvent;
import com.sabormayor.common.events.UserRegisteredEvent;
import com.sabormayor.common.security.Roles;
import com.sabormayor.user.domain.CustomerProfile;
import com.sabormayor.user.domain.StaffMember;
import com.sabormayor.user.infrastructure.AddressRepository;
import com.sabormayor.user.infrastructure.CustomerProfileRepository;
import com.sabormayor.user.infrastructure.PaymentMethodRefRepository;
import com.sabormayor.user.infrastructure.StaffMemberRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserSyncService {

    private final CustomerProfileRepository customerProfileRepository;
    private final StaffMemberRepository staffMemberRepository;
    private final AddressRepository addressRepository;
    private final PaymentMethodRefRepository paymentMethodRefRepository;

    @Transactional
    public void onUserRegistered(UserRegisteredEvent event) {
        if (Roles.CLIENTE.equals(event.role())) {
            if (!customerProfileRepository.existsById(event.userId())) {
                customerProfileRepository.save(CustomerProfile.builder()
                        .id(event.userId())
                        .email(event.email())
                        .fullName(event.fullName())
                        .build());
            }
        } else {
            if (!staffMemberRepository.existsById(event.userId())) {
                staffMemberRepository.save(StaffMember.builder()
                        .id(event.userId())
                        .email(event.email())
                        .fullName(event.fullName())
                        .role(event.role())
                        .active(true)
                        .build());
            }
        }
    }

    /** Habeas Data: remove every piece of personal data we hold for the user. */
    @Transactional
    public void onUserDeleted(UserDeletedEvent event) {
        addressRepository.deleteByCustomerId(event.userId());
        paymentMethodRefRepository.deleteByCustomerId(event.userId());
        customerProfileRepository.deleteById(event.userId());
    }
}
