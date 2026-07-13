package com.demo.infoleak.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("文件管理系统 API 文档")
                        .version("1.0.0")
                        .description("企业内部文件管理系统 REST API。用于文档的上传、检索、下载与管理。\n\n注意：此文档仅限内部开发人员使用，请勿外传。")
                        .contact(new Contact()
                                .name("研发中心")
                                .email("dev@company-internal.com")
                                .url("http://192.168.1.100:8080/internal")));
    }
}
