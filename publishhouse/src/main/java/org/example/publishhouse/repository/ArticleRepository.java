package org.example.publishhouse.repository;

import org.example.publishhouse.domain.Article;
import org.example.publishhouse.service.ArticleRatingView;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ArticleRepository extends JpaRepository<Article, Long> {

    @Query("""
            select new org.example.publishhouse.service.ArticleRatingView(
                a.id,
                a.title,
                a.text,
                coalesce(avg(c.score), 0.0),
                count(c.id)
            )
            from Article a
            left join a.comments c
            group by a.id, a.title, a.text
            order by a.id
            """)
    List<ArticleRatingView> findAllWithRatings();

    @Query("""
            select new org.example.publishhouse.service.ArticleRatingView(
                a.id,
                a.title,
                a.text,
                coalesce(avg(c.score), 0.0),
                count(c.id)
            )
            from Article a
            left join a.comments c
            where a.id = :articleId
            group by a.id, a.title, a.text
            """)
    Optional<ArticleRatingView> findByArticleIdWithRating(@Param("articleId") Long articleId);

    @Query("""
            select new org.example.publishhouse.service.ArticleRatingView(
                a.id,
                a.title,
                a.text,
                coalesce(avg(c.score), 0.0),
                count(c.id)
            )
            from Article a
            left join a.comments c
            group by a.id, a.title, a.text
            order by coalesce(avg(c.score), 0.0) desc
            """)
    List<ArticleRatingView> findTrending(Pageable pageable);
}


