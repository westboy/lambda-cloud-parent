package com.lambda.cloud.t645.message;

/**
 * T645 协议数据体接口。
 *
 * <p>所有 T645 协议的业务报文载荷（请求/应答）均需实现此接口，
 * 通过 {@link #getDi()} 返回数据标识 DI，用于报文分发与路由。</p>
 *
 * <p>DI（Data Identifier）是 DL/T 645-2007 协议中标识具体数据项的 4 字节编码，
 * 例如 {@code 00010000} 表示当前组合有功总电能。</p>
 */
public interface T645DataBody {
    /**
     * 获取数据标识 DI。
     *
     * @return DI 十六进制字符串（大写），无标准 DI 的私有报文返回 {@code "NONE"} 或 {@code null}
     */
    String getDi();
}
