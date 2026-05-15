package com.hjc.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Update material attachment metadata request")
public class UpdateMaterialAttachmentRequest {

    private Long materialId;

    @Size(max = 255, message = "originalName length must be less than or equal to 255")
    private String originalName;

    @Size(max = 255, message = "storedName length must be less than or equal to 255")
    private String storedName;

    @Size(max = 500, message = "filePath length must be less than or equal to 500")
    private String filePath;

    @Size(max = 500, message = "fileUrl length must be less than or equal to 500")
    private String fileUrl;

    @Size(max = 50, message = "fileType length must be less than or equal to 50")
    private String fileType;

    @Min(value = 0, message = "fileSize must be greater than or equal to 0")
    private Long fileSize;
}
