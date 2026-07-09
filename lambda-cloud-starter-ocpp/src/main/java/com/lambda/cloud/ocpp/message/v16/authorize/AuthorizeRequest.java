package com.lambda.cloud.ocpp.message.v16.authorize;

/**
 * OCPP 1.6 Authorize 请求(CP -> CSMS)。
 *
 * @param idTag 用户身份标识(RFID 卡号 / 中央 token)
 */
public record AuthorizeRequest(String idTag) {}
