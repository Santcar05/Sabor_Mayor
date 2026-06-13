package com.sabormayor.content.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PostRequest(
        @NotBlank @Size(max = 200) String title,
        @Size(max = 300) String excerpt,
        @NotBlank String body,
        String coverImage,
        @Size(max = 200) String metaTitle,
        @Size(max = 300) String metaDescription,
        boolean published) {
}
