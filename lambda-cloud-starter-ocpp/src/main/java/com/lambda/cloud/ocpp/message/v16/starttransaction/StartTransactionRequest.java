package com.lambda.cloud.ocpp.message.v16.starttransaction;

/**
 * OCPP 1.6 StartTransaction 请求(CP -> CSMS)。
 *
 * @param connectorId  连接器编号(1..N,对应 ChargeMind connectorNo)
 * @param idTag        用户身份标识
 * @param meterStart   起始电表读数(Wh,可选)
 * @param timestamp    起始时间(ISO 8601 字符串)
 * @param reservationId 预约 ID(可选)
 */
public record StartTransactionRequest(
        int connectorId, String idTag, Integer meterStart, String timestamp, Integer reservationId) {}
