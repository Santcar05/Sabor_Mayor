package com.sabormayor.reservation.mapper;

import java.util.List;

import org.mapstruct.Mapper;

import com.sabormayor.reservation.domain.Reservation;
import com.sabormayor.reservation.web.dto.ReservationResponse;

@Mapper(componentModel = "spring")
public interface ReservationMapper {

    ReservationResponse toResponse(Reservation reservation);

    List<ReservationResponse> toResponses(List<Reservation> reservations);
}
