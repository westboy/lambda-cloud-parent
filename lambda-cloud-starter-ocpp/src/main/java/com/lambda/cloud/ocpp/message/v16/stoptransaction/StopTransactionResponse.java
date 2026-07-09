package com.lambda.cloud.ocpp.message.v16.stoptransaction;

import com.lambda.cloud.ocpp.message.v16.type.IdTagInfo;

/**
 * OCPP 1.6 StopTransaction 应答(CSMS -> CP)。
 */
public record StopTransactionResponse(IdTagInfo idTagInfo) {}
