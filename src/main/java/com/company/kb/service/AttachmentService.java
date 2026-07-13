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

    private final Path uploadDir = Paths.get("uploads");
    private final AttachmentRepository attachmentRepository;

    public AttachmentService(AttachmentRepository attachmentRepository) {
        this.attachmentRepository = attachmentRepository;
    }

    @PostConstruct
    public void init() throws IOException {
        Files.createDirectories(uploadDir);
        createSampleFiles();
    }

    private void createSampleFiles() throws IOException {
        Path doc1 = uploadDir.resolve("api-specification-v2.txt");
        if (!Files.exists(doc1)) {
            Files.writeString(doc1, "API Specification v2.0\n\nEndpoints:\n- GET /api/users - List users\n- GET /api/articles - List articles\n- GET /api/attachments/download - Download files\n- POST /api/attachments/upload - Upload files\n\nAuthentication: Planned (not yet implemented)");
        }
        Path doc2 = uploadDir.resolve("server-config-backup.txt");
        if (!Files.exists(doc2)) {
            Files.writeString(doc2, "Server Configuration Backup\nDate: 2024-12-20\n\nHost: 192.168.1.100\nOS: Ubuntu 22.04 LTS\nDeploy User: deploy / D3ploy!Pass\nApp Path: /opt/app/knowledge-base\nLog Path: /var/log/knowledge-base\n\nStartup Command:\ncd /opt/app/knowledge-base && git pull && ./restart.sh");
        }
    }

    @Transactional
    public Attachment uploadFile(MultipartFile file, Long articleId, String uploadedBy) throws IOException {
        String originalName = file.getOriginalFilename();
        Path target = uploadDir.resolve(originalName);
        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

        Attachment attachment = new Attachment(originalName, file.getSize(), file.getContentType(),
                target.toAbsolutePath().toString(), articleId, uploadedBy);
        return attachmentRepository.save(attachment);
    }

    public Resource downloadFile(String path) throws IOException {
        Path filePath = uploadDir.resolve(path).normalize();

        // ===== 校验解析后的路径仍在 uploadDir 内 =====
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