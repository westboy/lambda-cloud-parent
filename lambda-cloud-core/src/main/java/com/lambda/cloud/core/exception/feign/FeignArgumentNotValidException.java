package com.lambda.cloud.core.exception.feign;

import com.google.common.collect.ImmutableList;
import com.lambda.cloud.core.exception.model.ArgumentError;
import com.lambda.cloud.core.exception.model.ErrorModel;
import java.util.List;
import lombok.Getter;

/**
 * @author Jin
 */
@Getter
public class FeignArgumentNotValidException extends AbstractFeignException {

    final List<ArgumentError> errors;

    public FeignArgumentNotValidException(ErrorModel model) {
        super(model);
        this.errors = model.getErrors() != null ? ImmutableList.copyOf(model.getErrors()) : ImmutableList.of();
    }

    @Override
    public int getStatus() {
        return 400;
    }
}
