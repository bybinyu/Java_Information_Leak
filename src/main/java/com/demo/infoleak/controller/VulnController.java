package com.demo.infoleak.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@RestController
@RequestMapping("/vuln")
@Tag(name = "漏洞演示专用", description = "用于演示敏感信息泄露的额外漏洞端点")
public class VulnController {

    @GetMapping("/file-read")
    @Operation(summary = "文件读取（字符串拼接）",
            description = "通过字符串拼接路径读取文件。参数 file 不做任何校验。例如: file=../../../etc/passwd")
    public String readFile(@RequestParam String file) {
        String basePath = "uploads/";
        try {
            // 漏洞：字符串直接拼接，不做任何安全校验
            return Files.readString(Paths.get(basePath + file));
        } catch (IOException e) {
            return "读取失败: " + e.getMessage();
        }
    }

    @GetMapping("/env")
    @Operation(summary = "泄露环境变量",
            description = "返回部分系统环境变量和系统属性，帮助攻击者搜集信息")
    public String leakEnv() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== 系统属性 ===\n");
        sb.append("user.name: ").append(System.getProperty("user.name")).append("\n");
        sb.append("user.dir: ").append(System.getProperty("user.dir")).append("\n");
        sb.append("os.name: ").append(System.getProperty("os.name")).append("\n");
        sb.append("java.version: ").append(System.getProperty("java.version")).append("\n");
        sb.append("java.class.path: ").append(System.getProperty("java.class.path")).append("\n\n");
        sb.append("=== 环境变量 ===\n");
        sb.append("PATH: ").append(System.getenv("PATH")).append("\n");
        sb.append("JAVA_HOME: ").append(System.getenv("JAVA_HOME")).append("\n");
        sb.append("USERPROFILE: ").append(System.getenv("USERPROFILE")).append("\n");
        return sb.toString();
    }
}
