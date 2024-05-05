package com.jingfang.core.exception.feign;

import com.jingfang.core.exception.model.ArgumentError;
import com.jingfang.core.exception.model.ErrorModel;

import java.util.List;

/**
 * @author Jin
 */
@SuppressWarnings("serial")
public class FeignArgumentNotValidException extends AbstractFeignException {

    final transient List<ArgumentError> errors;

    public FeignArgumentNotValidException(ErrorModel model) {
        super(model);
        this.errors = model.getErrors();
    }

    public List<ArgumentError> getErrors() {
        return this.errors;
    }

    @Override
    public int getStatus() {
        return 400;
    }

}
