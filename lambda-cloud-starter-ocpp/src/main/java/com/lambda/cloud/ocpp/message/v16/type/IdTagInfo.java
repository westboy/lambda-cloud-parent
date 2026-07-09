package com.lambda.cloud.ocpp.message.v16.type;

/**
 * OCPP 1.6 IdTagInfo:鉴权结果信息。
 *
 * @param status       鉴权状态
 * @param expiryDate   过期时间(ISO 8601 字符串,可选)
 * @param parentIdTag  父标签(可选,用于分组/子标签)
 */
public record IdTagInfo(AuthorizationStatus status, String expiryDate, String parentIdTag) {}
