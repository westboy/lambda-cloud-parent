package com.lamuda.cloud.core.exception;

/**
 * @author Jin
 */
public class NotSupportedException extends RuntimeException {

    public NotSupportedException() {
        super("Not currently supported!!!");
    }

    public NotSupportedException(String msg) {
        super(msg);
    }
}
