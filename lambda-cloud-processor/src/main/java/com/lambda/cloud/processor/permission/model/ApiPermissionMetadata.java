package com.lambda.cloud.processor.permission.model;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

/**
 * API 权限元数据
 *
 * <p>表示单个 API 接口的权限信息，包括路径、HTTP 方法、权限标识、角色等。
 *
 * @author Jin
 */
@Data
@SuppressFBWarnings("EI_EXPOSE_REP")
public class ApiPermissionMetadata {

    /** 接口路径（支持路径变量，如 /api/users/{id}） */
    private String path;

    /** HTTP 方法（GET/POST/PUT/DELETE/PATCH） */
    private String method;

    /** 权限标识列表 */
    private List<String> permissions = new ArrayList<>();

    /** 权限逻辑（AND/OR），默认 AND */
    private String permissionLogic = "AND";

    /** 角色标识列表 */
    private List<String> roles = new ArrayList<>();

    /** 是否需要认证，默认 true */
    private boolean requiresAuth = true;

    /** 接口描述 */
    private String description;

    /** 所属分组/模块 */
    private String group;

    /** Controller 类名 */
    private String controller;

    /** 方法名 */
    private String methodName;

    /** 是否已废弃 */
    private boolean deprecated = false;

    /** 标签列表 */
    private List<String> tags = new ArrayList<>();

    /** 所属模块名称（用于多模块聚合） */
    private String module;
}
