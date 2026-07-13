package com.demo.infoleak.service;

import com.demo.infoleak.model.FileItem;
import jakarta.annotation.PostConstruct;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@Service
public class FileService {

    private final Path uploadDir = Paths.get("uploads");

    @PostConstruct
    public void init() throws IOException {
        Files.createDirectories(uploadDir);
    }

    /**
     * 列出指定目录下的文件
     * ===== 漏洞：目录遍历 =====
     * dir 参数未做合法性校验，传入 "../../../etc" 可遍历服务任意目录
     */
    public List<FileItem> listFiles(String dir) throws IOException {
        Path targetDir = uploadDir.resolve(dir != null ? dir : "").normalize();
        List<FileItem> items = new ArrayList<>();
        try (Stream<Path> paths = Files.list(targetDir)) {
            paths.forEach(path -> {
                try {
                    FileItem item = new FileItem();
                    item.setName(path.getFileName().toString());
                    item.setSize(Files.size(path));
                    item.setAbsolutePath(path.toAbsolutePath().toString());
                    item.setLastModified(LocalDateTime.ofInstant(
                            Files.getLastModifiedTime(path).toInstant(), ZoneId.systemDefault()));
                    item.setDownloadUrl("/api/files/download?path=" + (dir != null ? dir + "/" : "") + path.getFileName().toString());
                    items.add(item);
                } catch (IOException ignored) {}
            });
        }
        return items;
    }

    /**
     * 下载指定文件
     * ===== 漏洞：目录遍历 =====
     * path 参数直接拼接，传入 "../../../etc/passwd" 可读取任意文件
     */
    public Resource downloadFile(String path) throws IOException {
        Path filePath = uploadDir.resolve(path);
        if (!Files.exists(filePath) || !Files.isReadable(filePath)) {
            throw new RuntimeException("文件不存在或不可读: " + path);
        }
        return new UrlResource(filePath.toUri());
    }

    /**
     * 上传文件
     */
    public void uploadFile(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new RuntimeException("上传文件为空");
        }
        Path target = uploadDir.resolve(file.getOriginalFilename());
        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
    }
}
