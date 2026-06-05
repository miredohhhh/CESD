package com.hjc.backend.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Data
@Component
@ConfigurationProperties(prefix = "cesd.upload")
public class UploadProperties {

    /**
     * Local root directory for uploaded material attachments.
     */
    private String rootDir = "./uploads";

    /**
     * Max size for a single uploaded file, in bytes.
     */
    private long maxFileSize = 10 * 1024 * 1024L;

    /**
     * Allowed lowercase file extensions without the dot.
     */
    private List<String> allowedExtensions = new ArrayList<>(List.of("jpg", "jpeg", "png", "pdf", "doc", "docx"));

    /**
     * URL prefix used when writing attachment metadata.
     */
    private String accessUrlPrefix = "/api/material-attachments";
}
