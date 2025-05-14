package com.lambda.cloud.mvc;

import com.google.common.collect.Maps;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.Map;

/**
 * @author Jin
 */
public class DeferredResultManager<T> {

    private final Map<String, DeferredResult<T>> deferredResults = Maps.newHashMap();

    public DeferredResult<T> get(String key) {
        return deferredResults.get(key);
    }

    public void put(String key, DeferredResult<T> deferredResult) {
        deferredResults.put(key, deferredResult);
    }

    public void remove(String key) {
        deferredResults.remove(key);
    }

    public void timeout(String key) {
        remove(key);
    }

    public void setResult(String key, T result) {
        DeferredResult<T> deferredResult = get(key);
        if (deferredResult != null) {
            deferredResult.setResult(result);
            deferredResults.remove(key);
        }
    }

    public int size() {
        return deferredResults.size();
    }

}
