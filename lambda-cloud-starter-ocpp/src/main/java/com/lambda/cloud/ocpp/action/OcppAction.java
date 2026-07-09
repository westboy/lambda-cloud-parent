package com.lambda.cloud.ocpp.action;

/**
 * OCPP 1.6 Action 字符串常量。CSMS 与 CP 双向调用统一以 action 名区分请求类型。
 */
public final class OcppAction {

    private OcppAction() {}

    // CP -> CSMS(设备发起)
    public static final String BOOT_NOTIFICATION = "BootNotification";
    public static final String AUTHORIZE = "Authorize";
    public static final String START_TRANSACTION = "StartTransaction";
    public static final String STOP_TRANSACTION = "StopTransaction";
    public static final String STATUS_NOTIFICATION = "StatusNotification";
    public static final String METER_VALUES = "MeterValues";
    public static final String HEARTBEAT = "Heartbeat";
    public static final String DATA_TRANSFER = "DataTransfer";
    public static final String FIRMWARE_STATUS_NOTIFICATION = "FirmwareStatusNotification";

    // CSMS -> CP(平台发起)
    public static final String REMOTE_START_TRANSACTION = "RemoteStartTransaction";
    public static final String REMOTE_STOP_TRANSACTION = "RemoteStopTransaction";
    public static final String RESET = "Reset";
    public static final String SET_CHARGING_PROFILE = "SetChargingProfile";
    public static final String GET_CONFIGURATION = "GetConfiguration";
    public static final String TRIGGER_MESSAGE = "TriggerMessage";
}
