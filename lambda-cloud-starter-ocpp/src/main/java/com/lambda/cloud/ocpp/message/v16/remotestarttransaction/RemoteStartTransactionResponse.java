package com.lambda.cloud.ocpp.message.v16.remotestarttransaction;

/**
 * OCPP 1.6 RemoteStartTransaction 应答(CP -> CSMS)。
 *
 * @param status "Accepted" / "Rejected"
 */
public record RemoteStartTransactionResponse(String status) {}
