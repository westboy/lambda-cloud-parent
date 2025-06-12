package com.lambda.cloud.core.convert;

/**
 * Converter
 *
 * @author Jin
 */
public interface Converter<A, B> {

	/**
	 * doForward
	 *
	 * @param a A
	 * @return B
	 */
	B doForward(A a);

	/**
	 * doBackward
	 *
	 * @param b B
	 * @return A
	 */
	A doBackward(B b);
}
