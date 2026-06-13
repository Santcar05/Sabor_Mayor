package com.sabormayor.payment.mapper;

import java.util.List;

import org.mapstruct.Mapper;

import com.sabormayor.payment.domain.Payment;
import com.sabormayor.payment.web.dto.PaymentResponse;

@Mapper(componentModel = "spring")
public interface PaymentMapper {

    PaymentResponse toResponse(Payment payment);

    List<PaymentResponse> toResponses(List<Payment> payments);
}
