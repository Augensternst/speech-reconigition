package com.example.backend.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {
    @Bean
    public OpenAPI openAPI(@Value("${springdoc.version}") String appVersion) {
        return new OpenAPI()
                .info(new Info().title("SR Project Backend API")
                        .version(appVersion)
                        .description("语音识别前段调用的后端API")
                        .termsOfService("http://swagger.io/terms/")
                        .contact(new Contact().email("aaa.com").name("aaa").url("https://aaa.com")));
    }
}
