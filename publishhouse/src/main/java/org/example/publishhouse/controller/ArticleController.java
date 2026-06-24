package org.example.publishhouse.controller;

import jakarta.validation.Valid;
import org.example.publishhouse.api.ArticleCreateRequest;
import org.example.publishhouse.api.ArticleResponse;
import org.example.publishhouse.api.ArticleUpdateRequest;
import org.example.publishhouse.service.PublishhouseService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class ArticleController {

    private final PublishhouseService publishhouseService;

    public ArticleController(PublishhouseService publishhouseService) {
        this.publishhouseService = publishhouseService;
    }

    @PostMapping("/articles")
    @ResponseStatus(HttpStatus.CREATED)
    public ArticleResponse createArticle(@Valid @RequestBody ArticleCreateRequest request) {
        return publishhouseService.createArticle(request);
    }

    @PutMapping("/articles/{id}")
    public ArticleResponse updateArticle(@PathVariable("id") Long id, @Valid @RequestBody ArticleUpdateRequest request) {
        return publishhouseService.updateArticle(id, request);
    }

    @DeleteMapping("/articles/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteArticle(@PathVariable("id") Long id) {
        publishhouseService.deleteArticle(id);
    }

    @GetMapping("/articles")
    public List<ArticleResponse> getArticles() {
        return publishhouseService.getArticles();
    }

    @GetMapping("/trending")
    public ArticleResponse getTrending() {
        return publishhouseService.getTrending();
    }

    @GetMapping("/articles/{id}")
    public ArticleResponse getArticleById(@PathVariable("id") Long id) {
        return publishhouseService.getArticleById(id);
    }
}
