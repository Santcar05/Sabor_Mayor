package com.sabormayor.user.web;

import java.util.List;
import java.util.UUID;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sabormayor.user.application.ProfileService;
import com.sabormayor.user.mapper.UserProfileMapper;
import com.sabormayor.user.web.dto.StaffResponse;
import com.sabormayor.user.web.dto.UpdateStaffRequest;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/staff")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
@Tag(name = "Staff", description = "Staff directory management (admin)")
public class StaffController {

    private final ProfileService profileService;
    private final UserProfileMapper mapper;

    @GetMapping
    public List<StaffResponse> listStaff() {
        return mapper.toStaffResponses(profileService.listStaff());
    }

    @PutMapping("/{staffId}")
    public StaffResponse updateStaff(@PathVariable UUID staffId, @Valid @RequestBody UpdateStaffRequest request) {
        return mapper.toResponse(profileService.updateStaff(staffId, request));
    }
}
