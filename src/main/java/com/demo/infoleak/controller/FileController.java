package com.demo.infoleak.controller;

import com.demo.infoleak.model.FileItem;
import com.demo.infoleak.service.FileService;
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
@RequestMapping("/api/files")
@Tag(name = "文件管理", description = "文件上传、列表、下载（包含目录遍历漏洞）")
public class FileController {

    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @GetMapping("/list")
    @Operation(summary = "文件列表",
            description = "列出目录中的文件。参数 dir 不做合法性校验，可遍历任意目录。例如: dir=../../../etc")
    public List<FileItem> listFiles(
            @RequestParam(required = false, defaultValue = "") String dir) {
        try {
            return fileService.listFiles(dir);
        } catch (Exception e) {
            throw new RuntimeException("列目录失败: " + e.getMessage());
        }
    }

    @GetMapping("/download")
    @Operation(summary = "下载文件",
            description = "根据路径下载文件。路径不受限制，可下载系统任意文件。例如: path=../../../etc/passwd")
    public ResponseEntity<Resource> downloadFile(
            @RequestParam String path) {
        try {
            Resource resource = fileService.downloadFile(path);
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);
        } catch (Exception e) {
            throw new RuntimeException("文件下载失败: " + e.getMessage());
        }
    }

    @PostMapping("/upload")
    @Operation(summary = "上传文件")
    public String uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            fileService.uploadFile(file);
            return "文件上传成功: " + file.getOriginalFilename();
        } catch (Exception e) {
            return "文件上传失败: " + e.getMessage();
        }
    }

    @GetMapping("/preview")
    @Operation(summary = "预览文件（文本）",
            description = "直接以文本形式返回文件内容。同样存在目录遍历漏洞。")
    public String previewFile(@RequestParam String path) {
        try {
            Resource resource = fileService.downloadFile(path);
            return new String(resource.getInputStream().readAllBytes());
        } catch (Exception e) {
            return "读取失败: " + e.getMessage();
        }
    }
}
