package com.demo.infoleak.model;

import java.time.LocalDateTime;

public class FileItem {
    private String name;
    private Long size;
    private String absolutePath; // 暴露了服务器文件系统路径
    private LocalDateTime lastModified;
    private String downloadUrl;

    public FileItem() {}

    public FileItem(String name, Long size, String absolutePath, LocalDateTime lastModified, String downloadUrl) {
        this.name = name;
        this.size = size;
        this.absolutePath = absolutePath;
        this.lastModified = lastModified;
        this.downloadUrl = downloadUrl;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Long getSize() { return size; }
    public void setSize(Long size) { this.size = size; }
    public String getAbsolutePath() { return absolutePath; }
    public void setAbsolutePath(String absolutePath) { this.absolutePath = absolutePath; }
    public LocalDateTime getLastModified() { return lastModified; }
    public void setLastModified(LocalDateTime lastModified) { this.lastModified = lastModified; }
    public String getDownloadUrl() { return downloadUrl; }
    public void setDownloadUrl(String downloadUrl) { this.downloadUrl = downloadUrl; }
}
