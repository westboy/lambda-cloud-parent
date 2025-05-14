package com.lambda.cloud.sms.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * SendResult
 *
 * @author Jin
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SmsSendResult {

    /**
     * 请求是否成功
     */
    private boolean success = false;

    /**
     * 我方请求记录id
     */
    private String id;

    /**
     * 运营商记录业务id
     */
    private String bizId;

    /**
     * 响应描述
     */
    private String message;
}
