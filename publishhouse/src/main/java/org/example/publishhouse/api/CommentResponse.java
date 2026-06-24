package org.example.publishhouse.api;

import java.time.LocalDateTime;

public record CommentResponse(
        long id,
        long articleId,
        String text,
        int score,
        LocalDateTime createdAt
) {
}

