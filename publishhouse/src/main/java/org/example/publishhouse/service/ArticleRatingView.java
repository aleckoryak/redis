package org.example.publishhouse.service;

public class ArticleRatingView {

    private final long id;
    private final String title;
    private final String text;
    private final double averageRating;
    private final long commentsCount;

    public ArticleRatingView(long id, String title, String text, double averageRating, long commentsCount) {
        this.id = id;
        this.title = title;
        this.text = text;
        this.averageRating = averageRating;
        this.commentsCount = commentsCount;
    }

    public long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getText() {
        return text;
    }

    public double getAverageRating() {
        return averageRating;
    }

    public long getCommentsCount() {
        return commentsCount;
    }
}

