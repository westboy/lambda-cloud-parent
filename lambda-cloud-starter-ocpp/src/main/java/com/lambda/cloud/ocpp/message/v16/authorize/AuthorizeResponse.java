package com.lambda.cloud.ocpp.message.v16.authorize;

import com.lambda.cloud.ocpp.message.v16.type.IdTagInfo;

/**
 * OCPP 1.6 Authorize 应答(CSMS -> CP)。
 */
public record AuthorizeResponse(IdTagInfo idTagInfo) {}
