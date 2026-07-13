package com.company.kb.service;

import com.company.kb.model.Article;
import com.company.kb.model.User;
import com.company.kb.repository.ArticleRepository;
import com.company.kb.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ArticleService {

    private final ArticleRepository articleRepository;
    private final UserRepository userRepository;

    public ArticleService(ArticleRepository articleRepository, UserRepository userRepository) {
        this.articleRepository = articleRepository;
        this.userRepository = userRepository;
    }

    @PostConstruct
    public void init() {
        if (articleRepository.count() == 0) {
            User admin = userRepository.findByUsername("admin").orElse(null);
            if (admin != null) {
                articleRepository.save(new Article(
                    "Getting Started with Knowledge Base",
                    "Welcome to the internal knowledge base.\n\nThis platform allows team members to create, share, and discover knowledge across the organization.\n\n## Features\n- Create and edit knowledge articles\n- Upload file attachments\n- Full-text search\n- Team collaboration",
                    "onboarding,guide,internal", admin));
                articleRepository.save(new Article(
                    "Database Connection Reference",
                    "## Production Database\n- Host: db.internal.company.com\n- Port: 3306\n- Database: production_db\n- Read-only user: readonly_user\n\n## Connection String\n```\njdbc:mysql://db.internal.company.com:3306/production_db\n```\n\nContact the DBA team for write access credentials.",
                    "database,devops,technical", admin));
                articleRepository.save(new Article(
                    "Deployment Pipeline Guide",
                    "## Deployment Workflow\n1. Merge feature branch to master\n2. CI pipeline builds and runs tests\n3. Deploy to staging for validation\n4. Manual approval for production\n\n## Environments\n- Staging: http://staging.internal.company.com:8080\n- Production: http://kb.company.com\n\n## Server Access\nSSH to bastion: deploy@10.0.0.1:2222\nKey: /home/deploy/.ssh/id_rsa",
                    "devops,deployment,infrastructure", admin));
            }
        }
    }

    public List<Article> listAll() {
        return articleRepository.findAll();
    }

    public Optional<Article> findById(Long id) {
        return articleRepository.findById(id);
    }

    public List<Article> search(String keyword) {
        return articleRepository.findByTitleContainingIgnoreCase(keyword);
    }

    public long count() {
        return articleRepository.count();
    }

    @Transactional
    public Article createArticle(String title, String content, String tags, Long authorId) {
        User author = userRepository.findById(authorId).orElse(null);
        Article article = new Article(title, content, tags, author);
        return articleRepository.save(article);
    }
}