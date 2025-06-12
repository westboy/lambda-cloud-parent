package com.lambda.cloud.core.exception.feign;

import com.lambda.cloud.core.exception.model.ErrorModel;

/**
 *
 * @author Jin
 */
public class FeignInternalServerErrorException extends AbstractFeignException {

	public FeignInternalServerErrorException(ErrorModel model) {
		super(model);
	}

	@Override
	public int getStatus() {
		return 500;
	}
}
