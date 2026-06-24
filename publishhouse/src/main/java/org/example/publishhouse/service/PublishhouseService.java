package org.example.publishhouse.service;

import org.example.publishhouse.api.ArticleCreateRequest;
import org.example.publishhouse.api.ArticleResponse;
import org.example.publishhouse.api.ArticleUpdateRequest;
import org.example.publishhouse.api.CommentCreateRequest;
import org.example.publishhouse.api.CommentResponse;
import org.example.publishhouse.domain.Article;
import org.example.publishhouse.domain.Comment;
import org.example.publishhouse.repository.ArticleRepository;
import org.example.publishhouse.repository.CommentRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class PublishhouseService {

    private final ArticleRepository articleRepository;
    private final CommentRepository commentRepository;

    public PublishhouseService(ArticleRepository articleRepository, CommentRepository commentRepository) {
        this.articleRepository = articleRepository;
        this.commentRepository = commentRepository;
    }

    @Transactional
    public ArticleResponse createArticle(ArticleCreateRequest request) {
        Article article = new Article(request.title(), request.text());
        Article saved = articleRepository.save(article);
        return new ArticleResponse(saved.getId(), saved.getTitle(), saved.getText(), 0.0, 0L);
    }

    @Transactional
    public ArticleResponse updateArticle(Long articleId, ArticleUpdateRequest request) {
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Article not found: " + articleId));

        article.updateContent(request.title(), request.text());
        articleRepository.save(article);
        ArticleRatingView view = articleRepository.findByArticleIdWithRating(articleId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Article not found: " + articleId));
        return toArticleResponse(view);
    }

    @Transactional
    public void deleteArticle(Long articleId) {
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Article not found: " + articleId));

        articleRepository.delete(article);
    }

    @Transactional(readOnly = true)
    public List<ArticleResponse> getArticles() {
        return articleRepository.findAllWithRatings().stream()
                .map(this::toArticleResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ArticleResponse getTrending() {
        return articleRepository.findTrending(PageRequest.of(0, 1)).stream()
                .findFirst()
                .map(this::toArticleResponse)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No articles found"));
    }

    @Transactional
    public CommentResponse createComment(Long articleId, CommentCreateRequest request) {
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Article not found: " + articleId));

        Comment comment = new Comment(article, request.text(), request.score());
        Comment saved = commentRepository.save(comment);
        return new CommentResponse(saved.getId(), article.getId(), saved.getText(), saved.getScore(), saved.getCreatedAt());
    }

    @Transactional(readOnly = true)
    public List<CommentResponse> getCommentsByArticleId(Long articleId) {
        articleRepository.findById(articleId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Article not found: " + articleId));

        return commentRepository.findByArticle_IdOrderByIdAsc(articleId).stream()
                .map(comment -> new CommentResponse(
                        comment.getId(),
                        comment.getArticle().getId(),
                        comment.getText(),
                        comment.getScore(),
                        comment.getCreatedAt()
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public ArticleResponse getArticleById(Long id) {
        return articleRepository.findByArticleIdWithRating(id)
                .map(this::toArticleResponse)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Article not found: " + id));
    }

    private ArticleResponse toArticleResponse(ArticleRatingView view) {
        return new ArticleResponse(
                view.getId(),
                view.getTitle(),
                view.getText(),
                view.getAverageRating(),
                view.getCommentsCount()
        );
    }
}
