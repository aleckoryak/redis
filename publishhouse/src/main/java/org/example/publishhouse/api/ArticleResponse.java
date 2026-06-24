package org.example.publishhouse.api;

public record ArticleResponse(
        long id,
        String title,
        String text,
        double averageRating,
        long commentsCount
) {
}

