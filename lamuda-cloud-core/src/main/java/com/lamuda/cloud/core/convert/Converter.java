package com.lamuda.cloud.core.convert;


/**
 * Converter
 *
 * @author Jin
 */
public interface Converter<A, B> {

    /**
     * doForward
     *
     * @param a
     * @return
     */
    B doForward(A a);

    /**
     * doBackward
     *
     * @param b
     * @return
     */
    A doBackward(B b);
}
