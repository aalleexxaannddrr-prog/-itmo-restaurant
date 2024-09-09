package com.example.demo.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
@Configuration
@Getter
public class PathConfig {
    @Value("${storage.service.folder.path}")
    private String StorageServiceFolderPath;
    @Value("${storage.type.service.folder.path}")
    private String StorageTypeServiceFolderPath;
    @Value("${swagger.url.path}")
    private String swaggerUrlPath;
}
