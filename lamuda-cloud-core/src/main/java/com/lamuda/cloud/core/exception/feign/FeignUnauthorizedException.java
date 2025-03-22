package com.lamuda.cloud.core.exception.feign;

import com.lamuda.cloud.core.exception.model.ErrorModel;

/**
 *
 * @author Jin
 */
public class FeignUnauthorizedException extends AbstractFeignException {

	public FeignUnauthorizedException(ErrorModel model) {
		super(model);
	}

	@Override
	public int getStatus() {
		return 401;
	}

}
