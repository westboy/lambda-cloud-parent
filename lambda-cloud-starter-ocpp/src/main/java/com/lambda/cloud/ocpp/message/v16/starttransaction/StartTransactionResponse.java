package com.lambda.cloud.ocpp.message.v16.starttransaction;

import com.lambda.cloud.ocpp.message.v16.type.IdTagInfo;

/**
 * OCPP 1.6 StartTransaction 应答(CSMS -> CP)。
 *
 * @param idTagInfo    鉴权结果
 * @param transactionId 事务 ID(CSMS 生成,对应 ChargeMind transactionId)
 */
public record StartTransactionResponse(IdTagInfo idTagInfo, int transactionId) {}
