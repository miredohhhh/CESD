package com.hjc.backend.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class RolePermissionVO {

    private Long roleId;

    private List<Long> permissionIds = new ArrayList<>();

    private List<String> permissionCodes = new ArrayList<>();
}
