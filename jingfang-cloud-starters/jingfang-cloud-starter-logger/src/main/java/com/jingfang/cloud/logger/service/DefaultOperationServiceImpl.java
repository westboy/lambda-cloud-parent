package com.jingfang.cloud.logger.service;

import cn.hutool.json.JSONUtil;
import com.jingfang.cloud.logger.advices.LogContext;
import lombok.extern.slf4j.Slf4j;


/**
 * @author jpjoo
 */
@Slf4j
public class DefaultOperationServiceImpl implements OperationService {

    /**
     * 保存日志
     *
     * @param entry
     */
    @Override
    public void save(LogContext entry) {
        log.info("SEND : {}", JSONUtil.toJsonPrettyStr(entry));
    }

}
