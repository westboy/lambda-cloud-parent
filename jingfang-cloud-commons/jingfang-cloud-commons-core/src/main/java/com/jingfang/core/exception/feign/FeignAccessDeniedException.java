package com.jingfang.core.exception.feign;

import com.jingfang.core.exception.model.ErrorModel;

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
