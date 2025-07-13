package com.lambda.cloud.sms.sender;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.IdUtil;
import com.github.qcloudsms.SmsSingleSender;
import com.github.qcloudsms.SmsSingleSenderResult;
import com.github.qcloudsms.httpclient.HTTPException;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lambda.autoconfig.SmsProperties;
import com.lambda.cloud.sms.SmsISP;
import com.lambda.cloud.sms.SmsMessageSender;
import com.lambda.cloud.sms.model.SmsSendResult;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.InitializingBean;

/**
 * 腾讯短信业务
 *
 * @author Jin
 */
@Slf4j
@SuppressFBWarnings(value = "EI_EXPOSE_REP")
@RequiredArgsConstructor
public class TencentSmsMessageSender implements SmsMessageSender, InitializingBean {

    public static final String[] ARGS = {};
    private static final int OK = 0;

    private final Gson gson = new Gson();

    private final SmsProperties properties;

    private SmsSingleSender smsSingleSender;

    @Override
    public SmsSendResult sendVerifyCode(String phone, String code, int expire) {
        String[] params = {code, String.valueOf(expire)};
        return toSendTencentMessage(phone, properties.getTencent().getCodeTemplateId(), params);
    }

    @Override
    public SmsSendResult sendMessage(String phone, String templateId, String params) {
        String[] args = ARGS;
        if (StringUtils.isNotBlank(params)) {
            List<String> list = gson.fromJson(params, new ListTypeToken().getType());
            args = ArrayUtil.toArray(list, String.class);
        }
        return toSendTencentMessage(phone, Integer.parseInt(templateId), args);
    }

    @Override
    public SmsISP isp() {
        return SmsISP.TENCENT;
    }

    @Override
    public void afterPropertiesSet() {
        log.info("初始化腾讯短信服务：{}", properties.getTencent());
        smsSingleSender = new SmsSingleSender(
                properties.getTencent().getAppId(), properties.getTencent().getAppKey());
    }

    private SmsSendResult toSendTencentMessage(String phone, int templateId, String[] params) {
        SmsSendResult response = null;
        try {
            response = new SmsSendResult();
            response.setId(IdUtil.fastSimpleUUID());
            SmsSingleSenderResult result = smsSingleSender.sendWithParam(
                    properties.getTencent().getNationCode(),
                    phone,
                    templateId,
                    params,
                    properties.getTencent().getSmsSign(),
                    "",
                    "");
            response.setBizId(result.sid);
            response.setMessage(result.errMsg);
            if (result.result == OK) {
                response.setSuccess(true);
                return response;
            }
            log.error("短信发送失败,第三方提示：{}", result.errMsg);
            return response;
        } catch (HTTPException | IOException e) {
            log.error("短信发送失败：{}", e.getMessage());
            return response;
        }
    }

    private static class ListTypeToken extends TypeToken<List<String>> {}
}
