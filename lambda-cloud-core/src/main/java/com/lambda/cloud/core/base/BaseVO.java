package com.lambda.cloud.core.base;

import com.lambda.cloud.core.convert.Converter;

/**
 * BaseVo
 *
 * @author Jin
 */
public abstract class BaseVO<V, E> implements Converter<V, E> {

	/**
	 * doForward
	 * @param  v V
	 * @return E
	 */
	@Override
	public E doForward(V v) {
		return null;
	}

	/**
	 * doBackward
	 * @param e E
	 * @return V
	 */
	@Override
	public V doBackward(E e) {
		return null;
	}
}
