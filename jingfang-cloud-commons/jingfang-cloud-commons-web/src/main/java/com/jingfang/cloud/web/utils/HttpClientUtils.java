package com.jingfang.cloud.web.utils;

import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * 本类负责调用接口
 */
@Slf4j
public class HttpClientUtils {

    /**
     * 禁止实例化
     */
    private HttpClientUtils() {

    }

    /**
     * post请求
     */
    public static String postTemplate(String url, Object data) {
        return restTemplate(url, data, "post");
    }

    /**
     * get请求
     */
    public static String getTemplate(String url) {
        return restTemplate(url, null, "get");
    }

    /**
     * restTemplate请求
     */
    private static String restTemplate(String url, Object data, String type) {
        log.info("【调用第三方接口地址:" + url);
        log.info("【请求开始，超时时间:" + 60000);
        String resultStr = "";
        try {
            //HttpRequestFactory初始化请求参数
            SimpleClientHttpRequestFactory httpRequestFactory = new SimpleClientHttpRequestFactory();
            httpRequestFactory.setReadTimeout(60000);
            httpRequestFactory.setConnectTimeout(60000);
            //创建请求对象
            RestTemplate restTemplate = new RestTemplate(httpRequestFactory);
            Object resStr;
            if ("post".equals(type)) {
                log.info("【请求报文:" + JSONUtil.toJsonStr(data));
                //设置请求报文头参数
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.set("Accept-Charset", "utf-8");
                HttpEntity<Object> request = new HttpEntity<>(data, headers);
                resultStr = restTemplate.postForObject(url, request, String.class);
            } else {
                resultStr = restTemplate.getForObject(url, String.class);
            }
        } catch (Exception e) {
            log.error("【第三方接口连接失败", e);
        } finally {
            log.info("【发送请求结束，返回报文：" + resultStr);
            log.info("【调用第三方接口结束");
        }
        return resultStr;
    }

}