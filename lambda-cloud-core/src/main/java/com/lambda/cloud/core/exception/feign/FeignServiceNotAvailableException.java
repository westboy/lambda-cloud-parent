package com.lambda.cloud.core.exception.feign;

import com.lambda.cloud.core.exception.model.ErrorModel;

/**
 *
 * @author Jin
 */
public class FeignServiceNotAvailableException extends AbstractFeignException {

    public FeignServiceNotAvailableException(ErrorModel model) {
        super(model);
    }

    @Override
    public int getStatus() {
        return 503;
    }
}
