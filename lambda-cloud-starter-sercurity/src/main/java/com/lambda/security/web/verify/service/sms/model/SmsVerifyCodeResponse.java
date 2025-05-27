package com.lambda.security.web.verify.service.sms.model;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
