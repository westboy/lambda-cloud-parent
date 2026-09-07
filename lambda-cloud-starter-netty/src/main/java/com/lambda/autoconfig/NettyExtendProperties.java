package com.lambda.autoconfig;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@SuppressFBWarnings("EI_EXPOSE_REP")
@Setter
@Getter
@ConfigurationProperties(prefix = "spring.netty")
public class NettyExtendProperties {

    NettyServerConfig server = new NettyServerConfig();

    ProtocolConfig protocol = new ProtocolConfig();

    @Setter
    @Getter
    public static class NettyServerConfig {
        /**
         * 端口
         */
        private int tcpPort;
        /**
         * boss线程数
         */
        private int bossThreadCount;
        /**
         * worker线程数
         */
        private int workerThreadCount;
        /**
         * 是否开启tcp长连接
         */
        private boolean soKeepalive = true;
        /**
         * 是否开启tcp缓存
         */
        private Map<String, Object> optionMap = new HashMap<>();
        /**
         * 是否自动启动
         */
        private boolean autoStartUp = true;

        /**
         * 解码数据的最大长度
         */
        private int maxFrameLength = 255;

        /**
         * 线程池大小
         */
        private int dispatchPermits = 100;

        /**
         * 空闲状态配置
         */
        private IdleConfig idleConfig = new IdleConfig();
    }

    @Data
    public static class ProtocolConfig {
        /**
         * CRC 字节序兼容开关
         * <p>
         * 开启后，CRC 校验失败时会按 CRC 字段长度对计算值做字节交换后再比对一次，
         * 用于兼容按小端序存储 CRC 的设备；命中时输出 WARN 日志。
         * 注意：仅作用于校验方向，下行报文的 CRC 字段字节序仍由字段的
         * littleEndian 属性控制。
         * </p>
         */
        private boolean crcByteSwap = false;
    }

    @Data
    public static class IdleConfig {
        /**
         * 读取客户端超时
         */
        private int readerIdleTimeSeconds = 0;

        /**
         * 发送给客户端的数据超时未接收
         */
        private int writerIdleTimeSeconds = 0;

        /**
         * 发送和读取都超时
         */
        private int allIdleTimeSeconds = 180;

        /**
         * 时间单位
         */
        private TimeUnit unit = TimeUnit.SECONDS;
    }
}
