package com.demo.infoleak.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 允许直接访问上传目录中的文件
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:./uploads/")
                .setCachePeriod(0);

        // ===== 漏洞：暴露 .git 目录 =====
        // 攻击者可通过 /.git/HEAD、/.git/config 等路径获取全部源码和提交历史
        // 注：.git_backup 包含模拟 git 历史数据，用于演示
        // 实际环境中攻击者会访问真实的 .git 目录
        registry.addResourceHandler("/.git/**")
                .addResourceLocations("file:./.git_backup/")
                .setCachePeriod(0);
    }
}
