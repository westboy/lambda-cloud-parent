package com.lamuda.cloud.logger.service;

import com.lamuda.cloud.logger.model.OperationBody;
import lombok.extern.slf4j.Slf4j;

import static com.lamuda.cloud.core.exception.model.ErrorModel.GSON;


/**
 * @author jpjoo
 */
@Slf4j
public class DefaultOperationServiceImpl implements OperationService {

    /**
     * 保存日志
     *
     * @param operationBody operationBody
     */
    @Override
    public void save(OperationBody operationBody) {
        log.info("save OPERATION LOGGER: {}", GSON.toJson(operationBody));
    }
}
