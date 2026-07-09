package com.lambda.cloud.ocpp.message.v16.heartbeat;

/**
 * OCPP 1.6 Heartbeat 应答(CSMS -> CP)。
 *
 * @param currentTime CSMS 当前时间(ISO 8601 字符串)
 */
public record HeartbeatResponse(String currentTime) {}
