package com.lambda.security.web.verify.service.sms.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * SmsSendResult
 *
 * @author Jin
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SmsVerifyCodeResponse implements Serializable {
    private String id;
    private Integer resendSeconds;
    private Integer validMinutes;
}
