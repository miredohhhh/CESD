package com.hjc.backend.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Schema(description = "Permission view object")
public class SysPermissionVO {

    private Long id;

    private String permissionName;

    private String permissionCode;

    private String permissionType;

    private Long parentId;

    private String routePath;

    private String componentPath;

    private String apiPath;

    private String httpMethod;

    private String icon;

    private Integer sortOrder;

    private Integer status;

    private String remark;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private List<SysPermissionVO> children = new ArrayList<>();
}
