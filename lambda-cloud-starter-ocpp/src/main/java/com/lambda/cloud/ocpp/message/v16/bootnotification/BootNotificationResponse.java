package com.lambda.cloud.ocpp.message.v16.bootnotification;

import com.lambda.cloud.ocpp.message.v16.type.RegistrationStatus;

/**
 * OCPP 1.6 BootNotification 应答(CSMS -> CP)。
 *
 * @param currentTime CSMS 当前时间(ISO 8601 字符串)
 * @param interval    心跳间隔(秒)
 * @param status      注册状态
 */
public record BootNotificationResponse(String currentTime, int interval, RegistrationStatus status) {}
