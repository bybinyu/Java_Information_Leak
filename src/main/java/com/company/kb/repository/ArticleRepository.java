package com.company.kb.repository;

import com.company.kb.model.Article;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ArticleRepository extends JpaRepository<Article, Long> {
    List<Article> findByTitleContainingIgnoreCase(String keyword);
    List<Article> findByTagsContainingIgnoreCase(String tag);
    List<Article> findByAuthorId(Long authorId);
}
