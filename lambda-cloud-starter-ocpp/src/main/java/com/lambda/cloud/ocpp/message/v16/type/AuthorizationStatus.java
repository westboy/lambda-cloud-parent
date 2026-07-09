package com.lambda.cloud.ocpp.message.v16.type;

/**
 * OCPP 1.6 AuthorizationStatus。
 * <p>枚举名与线缆字符串一致(Jackson 默认按 name 序列化)。</p>
 */
public enum AuthorizationStatus {
    Accepted,
    Blocked,
    Expired,
    Invalid,
    ConcurrentTx
}
