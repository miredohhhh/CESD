package com.hjc.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_permission")
@Schema(description = "System permission")
public class SysPermission {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("permission_name")
    private String permissionName;

    @TableField("permission_code")
    private String permissionCode;

    @TableField("permission_type")
    private String permissionType;

    @TableField("parent_id")
    private Long parentId;

    @TableField("route_path")
    private String routePath;

    @TableField("component_path")
    private String componentPath;

    @TableField("api_path")
    private String apiPath;

    @TableField("http_method")
    private String httpMethod;

    private String icon;

    @TableField("sort_order")
    private Integer sortOrder;

    private Integer status;

    private String remark;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_time")
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
