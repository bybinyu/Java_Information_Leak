# Security Vulnerability Fixes

## Fix 1: Swagger UI Unauthorized Access

### Changes Required

Add Spring Security to protect Swagger UI and API docs endpoints.

#### Step 1: Add Spring Security dependency to pom.xml

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
```

#### Step 2: Create SecurityConfig.java

```java
package com.company.kb.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                // Protect Swagger UI and API docs
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").authenticated()
                // Protect H2 console
                .requestMatchers("/h2-console/**").authenticated()
                // Allow everything else (for now)
                .anyRequest().permitAll()
            )
            .httpBasic(withDefaults())
            // Required for H2 console frames
            .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()));
        return http.build();
    }
}
```

#### Step 3: Add credentials in application.yml

```yaml
spring:
  security:
    user:
      name: admin
      password: admin123
      roles: ADMIN
```

#### Alternative Fix (No Spring Security) — Profile-Based Toggle

**application.yml (default, production):**
```yaml
springdoc:
  api-docs:
    enabled: false
  swagger-ui:
    enabled: false
```

**application-dev.yml:**
```yaml
springdoc:
  api-docs:
    enabled: true
  swagger-ui:
    enabled: true
```

Start with:
```bash
# Dev — Swagger enabled
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Production — Swagger disabled
mvn spring-boot:run
```

---

## Fix 2: .git Directory Exposure

### Root Cause

The `.git` directory is exposed via a static resource handler in `WebResourceConfig.java`:
```java
registry.addResourceHandler("/.git/**")
        .addResourceLocations("file:./.git_backup/");
```

### Option A: Remove the Resource Handler

Delete or comment out the `.git` handler from `WebResourceConfig.java`:

```java
@Configuration
public class WebResourceConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:./uploads/")
                .setCachePeriod(0);

        // REMOVED
        // registry.addResourceHandler("/.git/**")...
    }
}
```

### Option B: Add Access Filter

```java
@Component
public class GitPathFilter implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
                         FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        if (httpRequest.getRequestURI().startsWith("/.git")) {
            ((HttpServletResponse) response).setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        chain.doFilter(request, response);
    }
}
```

### Option C: Reverse Proxy Level (Production)

**Nginx:**
```nginx
location ~ /\.git {
    deny all;
    return 404;
}
```

### Option D: Clean Deployment

```bash
# Remove .git from production
rm -rf .git
# Or use git archive for deployment
git archive --format=tar HEAD | tar xf - -C /deploy/path/
```

---

## Fix 3: Directory Traversal Vulnerability

### Vulnerable Code (AttachmentService.java)

```java
public Resource downloadFile(String path) throws IOException {
    Path filePath = uploadDir.resolve(path);
    if (!Files.exists(filePath) || !Files.isReadable(filePath)) {
        throw new RuntimeException("File not found: " + path);
    }
    return new UrlResource(filePath.toUri());
}
```

### Fixed Code

```java
package com.company.kb.service;

import com.company.kb.model.Attachment;
import com.company.kb.repository.AttachmentRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

@Service
public class AttachmentService {

    private final Path uploadDir = Paths.get("uploads").normalize().toAbsolutePath();
    private final AttachmentRepository attachmentRepository;

    public AttachmentService(AttachmentRepository attachmentRepository) {
        this.attachmentRepository = attachmentRepository;
    }

    @PostConstruct
    public void init() throws IOException {
        Files.createDirectories(uploadDir);
    }

    @Transactional
    public Attachment uploadFile(MultipartFile file, Long articleId, String uploadedBy) throws IOException {
        String originalName = file.getOriginalFilename();
        Path target = uploadDir.resolve(originalName).normalize();
        if (!target.startsWith(uploadDir)) {
            throw new RuntimeException("Invalid file name");
        }
        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
        Attachment attachment = new Attachment(originalName, file.getSize(), file.getContentType(),
                target.toAbsolutePath().toString(), articleId, uploadedBy);
        return attachmentRepository.save(attachment);
    }

    /**
     * Fixed download method with path traversal protection.
     */
    public Resource downloadFile(String path) throws IOException {
        Path filePath = uploadDir.resolve(path).normalize();
        // Step 2: Verify the resolved path is still within the upload directory
        if (!filePath.startsWith(uploadDir)) {
            throw new RuntimeException("Access denied: invalid file path");
        }
        if (!Files.exists(filePath) || !Files.isReadable(filePath)) {
            throw new RuntimeException("File not found: " + path);
        }
        return new UrlResource(filePath.toUri());
    }

    public List<Attachment> listByArticle(Long articleId) {
        return attachmentRepository.findByArticleId(articleId);
    }

    public List<Attachment> listAll() {
        return attachmentRepository.findAll();
    }
}
```

---

## Fix 4: Actuator env Information Leak

### Root Cause

Actuator endpoints are fully exposed without authentication, and `show-values=always` reveals all property values including secrets.

### Fix 1: Restrict Exposure

In `application.yml`, limit exposed endpoints to only necessary ones (e.g., `health`):

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info    # Only health and info
  endpoint:
    env:
      enabled: false            # Disable env endpoint
    configprops:
      enabled: false            # Disable configprops
```

### Fix 2: Mask Sensitive Values

Spring Boot automatically masks `password`, `secret`, `key` values in Actuator output. To ensure all sensitive properties are masked:

```yaml
management:
  endpoint:
    env:
      show-values: never        # Never show actual values
    configprops:
      show-values: never
```

Or use `when-authorized` to require specific roles:

```yaml
management:
  endpoint:
    env:
      show-values: when-authorized
```

### Fix 3: Add Authentication (Recommended)

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info
  endpoint:
    env:
      enabled: false

spring:
  security:
    user:
      name: admin
      password: admin123
      roles: ACTUATOR
```

Then in `SecurityConfig.java`:

```java
@Bean
public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/actuator/**").hasRole("ACTUATOR")
            .anyRequest().permitAll()
        )
        .httpBasic(withDefaults());
    return http.build();
}
```

### Verification

```bash
# Before fix — anyone can access
curl http://localhost:8080/actuator/env
# Returns all environment properties with values

# After fix
curl http://localhost:8080/actuator/env
# Returns 401 Unauthorized (with security) or
# Only {"activeProfiles":[], "propertySources":[]} with empty values (when masked)
```