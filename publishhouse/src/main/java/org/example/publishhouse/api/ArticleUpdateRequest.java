package org.example.publishhouse.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ArticleUpdateRequest(
        @NotBlank @Size(max = 255) String title,
        @NotBlank @Size(max = 4000) String text
) {
}

