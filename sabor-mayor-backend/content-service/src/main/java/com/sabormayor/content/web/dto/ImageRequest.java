package com.sabormayor.content.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ImageRequest(
        @NotBlank String url,
        @Size(max = 200) String caption,
        int displayOrder) {
}
