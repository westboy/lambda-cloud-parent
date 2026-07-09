package com.lambda.cloud.ocpp.message.v16.statusnotification;

import com.lambda.cloud.ocpp.message.v16.type.ChargePointStatus;

/**
 * OCPP 1.6 StatusNotification 请求(CP -> CSMS)。
 *
 * @param connectorId     连接器编号
 * @param status          连接器状态
 * @param errorCode       错误码(OCPP ChargePointErrorCode 字符串,可选)
 * @param info            附加信息(可选)
 * @param timestamp       时间(ISO 8601 字符串,可选)
 * @param vendorId        厂商标识(可选)
 * @param vendorErrorCode 厂商错误码(可选)
 */
public record StatusNotificationRequest(
        int connectorId,
        ChargePointStatus status,
        String errorCode,
        String info,
        String timestamp,
        String vendorId,
        String vendorErrorCode) {}
