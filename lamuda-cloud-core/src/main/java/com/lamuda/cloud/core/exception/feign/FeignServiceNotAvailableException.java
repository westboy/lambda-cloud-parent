package com.lamuda.cloud.core.exception.feign;

import com.lamuda.cloud.core.exception.model.ErrorModel;

/**
 *
 * @author Jin
 */
@SuppressWarnings("serial")
public class FeignServiceNotAvailableException extends AbstractFeignException {

	public FeignServiceNotAvailableException(ErrorModel model) {
		super(model);
	}

	@Override
	public int getStatus() {
		return 503;
	}

}
