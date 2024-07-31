package com.jingfang.cloud.core.exception.feign;

import com.jingfang.cloud.core.exception.model.ErrorModel;

/** 
 *
 * @author Jin
 */
@SuppressWarnings("serial")
public class FeignAccessDeniedException extends AbstractFeignException {

	public FeignAccessDeniedException(ErrorModel model) {
		super(model);
	}

	@Override
	public int getStatus() {
		return 403;
	}

}
