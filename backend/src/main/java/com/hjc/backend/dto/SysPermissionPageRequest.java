package com.hjc.backend.dto;

import com.hjc.backend.common.PageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "Permission page request")
public class SysPermissionPageRequest extends PageRequest {

    private Long pageNo;

    private String keyword;

    private String permissionType;

    private Integer status;

    private Long parentId;
}
