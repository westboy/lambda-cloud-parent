package com.lambda.cloud.ocpp.message.v16.type;

/**
 * OCPP 1.6 ChargePointStatus(连接器状态)。
 */
public enum ChargePointStatus {
    Available,
    Preparing,
    Charging,
    SuspendedEVSE,
    SuspendedEV,
    Finishing,
    Reserved,
    Unavailable,
    Faulted
}
