package com.jingfang.core.exception.feign;

import com.jingfang.core.exception.model.ErrorModel;

/**
 *
 * @author Jin
 */
@SuppressWarnings("serial")
public class FeignUnauthorizedException extends AbstractFeignException {

	public FeignUnauthorizedException(ErrorModel model) {
		super(model);
	}

	@Override
	public int getStatus() {
		return 401;
	}

}
