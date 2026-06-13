package com.sabormayor.auth.mapper;

import org.mapstruct.Mapper;

import com.sabormayor.auth.domain.UserAccount;
import com.sabormayor.auth.web.dto.UserResponse;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserResponse toResponse(UserAccount user);
}
