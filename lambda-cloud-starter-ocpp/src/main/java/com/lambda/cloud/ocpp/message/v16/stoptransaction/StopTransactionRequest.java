package com.lambda.cloud.ocpp.message.v16.stoptransaction;

/**
 * OCPP 1.6 StopTransaction 请求(CP -> CSMS)。
 *
 * @param transactionId 事务 ID
 * @param idTag         用户身份标识(可选)
 * @param meterStop     结束电表读数(Wh,可选)
 * @param timestamp     结束时间(ISO 8601 字符串)
 * @param reason        停止原因(OCPP Reason 枚举字符串,可选)
 */
public record StopTransactionRequest(
        int transactionId, String idTag, Integer meterStop, String timestamp, String reason) {}
