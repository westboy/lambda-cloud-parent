package com.lambda.cloud.ocpp.message.v16.remotestoptransaction;

/**
 * OCPP 1.6 RemoteStopTransaction 请求(CSMS -> CP)。
 *
 * @param transactionId 事务 ID
 */
public record RemoteStopTransactionRequest(int transactionId) {}
