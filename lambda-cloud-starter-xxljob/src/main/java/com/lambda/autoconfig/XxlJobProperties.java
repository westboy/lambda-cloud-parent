package com.lambda.autoconfig;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * XXL-Job 执行器配置（lambda.cloud.xxl-job）。
 * <p>仅承载<b>执行器连接参数</b>；调度参数（cron、路由策略、失败重试、超时）一律在
 * XXL-Job 调度中心配置，禁止经属性硬编码（工程契约 §23.3.2）。</p>
 *
 * @author Jin
 */
@Setter
@Getter
@Validated
@ConfigurationProperties(prefix = "lambda.cloud.xxl-job")
public class XxlJobProperties {

    /** 是否启用 XXL-Job 执行器（默认关闭，部署环境连上调度中心后开启）。 */
    private boolean enabled = false;

    /** 调度中心地址，多个逗号分隔，如 http://127.0.0.1:8080/xxl-job-admin。 */
    @NotBlank(message = "启用 XXL-Job 时 adminAddresses 不能为空")
    private String adminAddresses;

    /** 调度中心与执行器通信令牌（须与调度中心一致，可空）。 */
    private String accessToken;

    /** 执行器 AppName（执行器注册分组标识，调度中心按此路由）。 */
    @NotBlank(message = "启用 XXL-Job 时 appname 不能为空")
    private String appname;

    /** 执行器注册地址（可空：空则自动发现，内网调度通常留空）。 */
    private String address;

    /** 执行器 IP（可空：空则自动获取本机 IP）。 */
    private String ip;

    /** 执行器端口（默认 9999，同机多执行器须错开）。 */
    private int port = 9999;

    /** 执行器日志文件路径（可空：默认 /data/applogs/xxl-job/jobhandler）。 */
    private String logPath;

    /** 执行器日志保留天数（默认 30，-1 永久保留）。 */
    private int logRetentionDays = 30;
}
