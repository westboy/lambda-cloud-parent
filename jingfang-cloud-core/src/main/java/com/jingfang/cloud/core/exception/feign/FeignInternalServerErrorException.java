package com.jingfang.cloud.core.exception.feign;

import com.jingfang.cloud.core.exception.model.ErrorModel;

/** 
 *
 * @author Jin
 */
@SuppressWarnings("serial")
public class FeignInternalServerErrorException extends AbstractFeignException {

	public FeignInternalServerErrorException(ErrorModel model) {
		super(model);
	}

	@Override
	public int getStatus() {
		return 500;
	}

}
