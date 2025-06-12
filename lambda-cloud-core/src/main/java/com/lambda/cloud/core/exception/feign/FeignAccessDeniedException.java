package com.lambda.cloud.core.exception.feign;

import com.lambda.cloud.core.exception.model.ErrorModel;

/**
 *
 * @author Jin
 */
public class FeignAccessDeniedException extends AbstractFeignException {

	public FeignAccessDeniedException(ErrorModel model) {
		super(model);
	}

	@Override
	public int getStatus() {
		return 403;
	}
}
