package com.lambda.cloud.sms.produce;

import cn.hutool.core.util.IdUtil;
import com.aliyuncs.CommonRequest;
import com.aliyuncs.CommonResponse;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.profile.DefaultProfile;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lambda.autoconfig.SmsProperties;
import com.lambda.cloud.sms.SmsISP;
import com.lambda.cloud.sms.SmsMessageSender;
import com.lambda.cloud.sms.model.SmsSendResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.InitializingBean;

import java.lang.reflect.Type;
import java.util.Map;

/**
 * 阿里短信业务
 *
 * @author Jin
 */
@Slf4j
@RequiredArgsConstructor
public class AliYunSmsMessageSender implements SmsMessageSender, InitializingBean {

    private static final String OK = "OK";
    private static final String RESULT_CODE_KEY = "Code";
    private static final String RESULT_BIZ_ID_KEY = "BizId";
    private static final String RESULT_MESSAGE_KEY = "Message";

    private final Gson gson = new Gson();

    private final SmsProperties properties;

    private IAcsClient client;


    @Override
    public SmsSendResult sendVerifyCode(String phone, String code, int expire) {
        Map<String, Object> params = Maps.newHashMapWithExpectedSize(2);
        params.put("code", code);
        params.put("expire", String.valueOf(expire));
        return toSendAliYunMessage(phone, properties.getAliyun().getCodeTemplateId(), gson.toJson(params));
    }

    @Override
    public SmsSendResult sendMessage(String phone, String templateId, String params) {
        return toSendAliYunMessage(phone, templateId, params);
    }

    @Override
    public SmsISP isp() {
        return SmsISP.ALIYUN;
    }

    @Override
    public void afterPropertiesSet() {
        log.info("初始化阿里云短信服务：{}", properties.getAliyun());
        DefaultProfile profile = DefaultProfile.getProfile(properties.getAliyun().getRegionId(),
                properties.getAliyun().getAccessKeyId(), properties.getAliyun().getAccessKeySecret());
        client = new DefaultAcsClient(profile);
    }

    private SmsSendResult toSendAliYunMessage(String phone, String templateId, String params) {
        String id = IdUtil.fastSimpleUUID();
        CommonRequest request = new CommonRequest();
        request.setSysMethod(MethodType.POST);
        request.setSysDomain(properties.getAliyun().getDomain());
        request.setSysVersion(properties.getAliyun().getVersion());
        request.setSysAction(properties.getAliyun().getAction().toString());
        request.putQueryParameter("RegionId", properties.getAliyun().getRegionId());
        request.putQueryParameter("PhoneNumbers", phone);
        request.putQueryParameter("SignName", properties.getAliyun().getSignName());
        request.putQueryParameter("TemplateCode", templateId);
        request.putQueryParameter("OutId", id);
        if (StringUtils.isNotBlank(params)) {
            request.putQueryParameter("TemplateParam", params);
        }
        SmsSendResult res = new SmsSendResult();
        res.setId(id);
        try {
            CommonResponse response = client.getCommonResponse(request);
            Type type = new MapTypeToken().getType();
            Map<String, String> result = gson.fromJson(response.getData(), type);
            res.setBizId(result.get(RESULT_BIZ_ID_KEY));
            res.setMessage(result.get(RESULT_MESSAGE_KEY));
            if (OK.equals(result.get(RESULT_CODE_KEY))) {
                res.setSuccess(true);
                return res;
            }
            return res;
        } catch (ClientException e) {
            log.error("短信发送失败：{}", e.getMessage());
            return res;
        }
    }

    private static class MapTypeToken extends TypeToken<Map<String, String>> {
    }
}
