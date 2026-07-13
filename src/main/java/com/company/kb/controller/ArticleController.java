package com.company.kb.controller;

import com.company.kb.model.Article;
import com.company.kb.service.ArticleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/articles")
@Tag(name = "Articles", description = "Knowledge article management")
public class ArticleController {

    private final ArticleService articleService;

    public ArticleController(ArticleService articleService) {
        this.articleService = articleService;
    }

    @GetMapping
    @Operation(summary = "List all articles", description = "Returns all knowledge articles")
    public List<Article> listAll() {
        return articleService.listAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get article by ID")
    public ResponseEntity<Article> getById(@PathVariable Long id) {
        return articleService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/search")
    @Operation(summary = "Search articles by keyword")
    public List<Article> search(@RequestParam String q) {
        return articleService.search(q);
    }

    @GetMapping("/count")
    @Operation(summary = "Total article count")
    public long count() {
        return articleService.count();
    }
}