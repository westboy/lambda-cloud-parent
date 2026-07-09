package com.lambda.cloud.ocpp.message.v16.remotestarttransaction;

/**
 * OCPP 1.6 RemoteStartTransaction 请求(CSMS -> CP)。
 *
 * @param connectorId 连接器编号
 * @param idTag       用户身份标识
 */
public record RemoteStartTransactionRequest(int connectorId, String idTag) {}
