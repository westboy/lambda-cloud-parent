package com.jingfang.cloud.logger.service;

import com.jingfang.cloud.logger.model.OperationBody;

/**
 * @author jpjoo
 */
public interface OperationService {
    /**
     * 保存操作日志
     *
     * @param operationBody operationBody
     */
    void save(OperationBody operationBody);
}
