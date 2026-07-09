package com.lambda.cloud.ocpp.message;

/**
 * OCPP-J 错误码(OCPP 1.6 §RPC framework)。
 * <p>线缆格式为驼峰字符串(如 {@code NotImplementedError});{@link #wire()} 返回该字符串。</p>
 */
public enum OcppErrorCode {
    NOT_IMPLEMENTED("NotImplementedError"),
    NOT_SUPPORTED("NotSupportedError"),
    INTERNAL_ERROR("InternalError"),
    PROTOCOL_ERROR("ProtocolError"),
    SECURITY_ERROR("SecurityError"),
    FORMATION_VIOLATION("FormationViolation"),
    PROPERTY_CONSTRAINT_VIOLATION("PropertyConstraintViolation"),
    OCCURRENCE_CONSTRAINT_VIOLATION("OccurrenceConstraintViolation"),
    FORMAT_ERROR("FormatError"),
    GENERIC_ERROR("GenericError");

    private final String wire;

    OcppErrorCode(String wire) {
        this.wire = wire;
    }

    public String wire() {
        return wire;
    }

    public static OcppErrorCode fromWire(String wire) {
        for (OcppErrorCode e : values()) {
            if (e.wire.equals(wire)) {
                return e;
            }
        }
        return GENERIC_ERROR;
    }
}
