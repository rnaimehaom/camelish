package com.im.camel.processor;

import java.util.Map;

/**
 *
 * @author timbo
 */
public interface ResultExtractor<T> {
    
    public Map<String, Object> extractResults(T mol);
    
}
