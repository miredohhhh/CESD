package com.hjc.backend.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class MenuPermissionVO {

    private Long id;

    private String permissionName;

    private String permissionCode;

    private String routePath;

    private String componentPath;

    private String icon;

    private Integer sortOrder;

    private List<MenuPermissionVO> children = new ArrayList<>();
}
