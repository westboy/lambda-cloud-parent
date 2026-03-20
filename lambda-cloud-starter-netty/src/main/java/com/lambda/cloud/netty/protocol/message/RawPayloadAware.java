package com.lambda.cloud.netty.protocol.message;

/**
 * 原始报文感知接口
 * <p>
 * 协议实体类实现该接口后，底层的 ProtocolEngine 会在解析完成时将完整的网络原始字节流注入该对象。
 * 极大地便利数字签名延迟验证、原始报文透传代理、未解明文死信记录等场景。
 * </p>
 *
 * @author Jin
 */
public interface RawPayloadAware {

    /**
     * 获取原始协议包数据
     *
     * @return 完整的网络原始字节数组
     */
    byte[] getRawPayload();

    /**
     * 引擎回调注入原始协议包数据
     *
     * @param rawPayload 网络原始字节数组
     */
    void setRawPayload(byte[] rawPayload);
}
