package com.lambda.security;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 登录接口返回实体
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginResponse {

    /**
     * 登录凭证
     */
    @JsonProperty("accessToken")
    private String accessToken;

    /**
     * token 有效期（秒）
     */
    @JsonProperty("expiresIn")
    private Long expiresIn;

    /**
     * 用户唯一标识（手机号、用户名或邮箱）
     */
    @JsonProperty("subject")
    private Object subject;

    /**
     * 登录设备类型，例如 default / web / mobile
     */
    @JsonProperty("deviceType")
    private String deviceType;
}
