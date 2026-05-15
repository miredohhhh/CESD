package com.hjc.backend.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class CurrentUserPermissionVO {

    private Long userId;

    private String roleCode;

    private List<String> permissionCodes = new ArrayList<>();

    private List<MenuPermissionVO> menus = new ArrayList<>();

    private List<String> buttons = new ArrayList<>();
}
