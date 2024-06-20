package com.jingfang.cloud.logger.service;

import com.jingfang.cloud.logger.advices.LogContext;

/**
 * @author jpjoo
 */
public interface OperationService {
    /**
     * 保存操作日志
     *
     * @param context
     */
    void save(LogContext context);
}
