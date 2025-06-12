package com.lambda.cloud.web;

/**
 * @author Jin
 */
public final class RequestTimeHolder {

	private static final ThreadLocal<Long> CONTEXT_HOLDER = new ThreadLocal<>();

	public void clearContext() {
		CONTEXT_HOLDER.remove();
	}

	public static Long getTime() {
		Long start = CONTEXT_HOLDER.get();
		if (start == null) {
			start = System.currentTimeMillis();
			CONTEXT_HOLDER.set(start);
		}
		return start;
	}

	public static void setTime(long start) {
		CONTEXT_HOLDER.set(start);
	}

	public static void clear() {
		CONTEXT_HOLDER.remove();
	}
}
