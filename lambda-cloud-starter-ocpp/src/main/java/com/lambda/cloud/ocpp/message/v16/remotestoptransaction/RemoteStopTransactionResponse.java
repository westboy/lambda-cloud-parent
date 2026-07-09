package com.lambda.cloud.ocpp.message.v16.remotestoptransaction;

/**
 * OCPP 1.6 RemoteStopTransaction 应答(CP -> CSMS)。
 *
 * @param status "Accepted" / "Rejected"
 */
public record RemoteStopTransactionResponse(String status) {}
