package org.example.publishhouse.repository;

import org.example.publishhouse.domain.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findByArticle_IdOrderByIdAsc(Long articleId);
}

