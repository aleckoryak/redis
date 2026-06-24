package org.example.publishhouse.controller;

import jakarta.validation.Valid;
import org.example.publishhouse.api.CommentCreateRequest;
import org.example.publishhouse.api.CommentResponse;
import org.example.publishhouse.service.PublishhouseService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class CommentController {

    private final PublishhouseService publishhouseService;

    public CommentController(PublishhouseService publishhouseService) {
        this.publishhouseService = publishhouseService;
    }

    @PostMapping("/articles/{id}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    public CommentResponse createComment(@PathVariable("id") Long id, @Valid @RequestBody CommentCreateRequest request) {
        return publishhouseService.createComment(id, request);
    }

    @GetMapping("/articles/{id}/comments")
    public List<CommentResponse> getComments(@PathVariable("id") Long id) {
        return publishhouseService.getCommentsByArticleId(id);
    }
}


