package com.lambda.cloud.ocpp.message.v16.bootnotification;

/**
 * OCPP 1.6 BootNotification 请求(CP -> CSMS)。
 * <p>chargePointIdentity 由 WebSocket URL path 携带,不在请求体内。</p>
 */
public record BootNotificationRequest(
        String chargePointVendor,
        String chargePointModel,
        String chargePointSerialNumber,
        String firmwareVersion,
        String chargeBoxSerialNumber,
        String meterType,
        String meterSerialNumber) {}
