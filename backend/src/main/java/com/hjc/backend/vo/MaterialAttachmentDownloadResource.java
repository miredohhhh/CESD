package com.hjc.backend.vo;

import org.springframework.core.io.Resource;

public record MaterialAttachmentDownloadResource(
        Resource resource,
        String originalName,
        String contentType,
        long contentLength
) {
}
