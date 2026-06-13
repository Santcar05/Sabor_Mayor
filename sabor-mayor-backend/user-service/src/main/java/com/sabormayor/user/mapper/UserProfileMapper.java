package com.sabormayor.user.mapper;

import java.util.List;

import org.mapstruct.Mapper;

import com.sabormayor.user.domain.Address;
import com.sabormayor.user.domain.CustomerProfile;
import com.sabormayor.user.domain.PaymentMethodRef;
import com.sabormayor.user.domain.StaffMember;
import com.sabormayor.user.web.dto.AddressResponse;
import com.sabormayor.user.web.dto.PaymentMethodResponse;
import com.sabormayor.user.web.dto.ProfileResponse;
import com.sabormayor.user.web.dto.StaffResponse;

@Mapper(componentModel = "spring")
public interface UserProfileMapper {

    ProfileResponse toResponse(CustomerProfile profile);

    AddressResponse toResponse(Address address);

    List<AddressResponse> toAddressResponses(List<Address> addresses);

    PaymentMethodResponse toResponse(PaymentMethodRef paymentMethod);

    List<PaymentMethodResponse> toPaymentMethodResponses(List<PaymentMethodRef> paymentMethods);

    StaffResponse toResponse(StaffMember staff);

    List<StaffResponse> toStaffResponses(List<StaffMember> staff);
}
