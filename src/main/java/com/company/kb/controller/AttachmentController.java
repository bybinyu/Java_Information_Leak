package com.company.kb.controller;

import com.company.kb.model.Attachment;
import com.company.kb.service.AttachmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/attachments")
@Tag(name = "Attachments", description = "File attachment management")
public class AttachmentController {

    private final AttachmentService attachmentService;

    public AttachmentController(AttachmentService attachmentService) {
        this.attachmentService = attachmentService;
    }

    @GetMapping
    @Operation(summary = "List all attachments")
    public List<Attachment> listAll() {
        return attachmentService.listAll();
    }

    @GetMapping("/download")
    @Operation(summary = "Download a file", description = "Download a file by its path relative to the upload directory")
    public ResponseEntity<Resource> downloadFile(@RequestParam String path) {
        try {
            Resource resource = attachmentService.downloadFile(path);
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);
        } catch (Exception e) {
            throw new RuntimeException("Download failed: " + e.getMessage());
        }
    }

    @GetMapping("/by-article")
    @Operation(summary = "List attachments for an article")
    public List<Attachment> listByArticle(@RequestParam Long articleId) {
        return attachmentService.listByArticle(articleId);
    }

    @PostMapping("/upload")
    @Operation(summary = "Upload a file attachment")
    public String uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(required = false) Long articleId,
            @RequestParam(required = false, defaultValue = "anonymous") String uploader) {
        try {
            Attachment saved = attachmentService.uploadFile(file, articleId, uploader);
            return "Uploaded: " + saved.getFileName()
                    + " (" + saved.getFileSize() + " bytes, id=" + saved.getId() + ")";
        } catch (Exception e) {
            return "Upload failed: " + e.getMessage();
        }
    }
}