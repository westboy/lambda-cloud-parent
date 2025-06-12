package com.lambda.cloud.core.base;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * BaseDTO
 *
 * @author Jin
 */
public abstract class BaseDTO<D, E> {

	/**
	 * convertToEntity
	 *
	 * @return
	 */
	public abstract E convertToEntity();

	/**
	 * convertFor
	 *
	 * @param e
	 * @return
	 */
	public abstract D convertFor(E e);

	protected Page<E> getPage(long current, long size) {
		return new Page<>(current, size);
	}
}
