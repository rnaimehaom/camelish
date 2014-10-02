/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.im.camel.components;

import java.util.HashMap;
import java.util.Map;
import org.apache.camel.Consumer;
import org.apache.camel.Processor;
import org.apache.camel.Producer;
import org.apache.camel.impl.DefaultEndpoint;
import org.apache.camel.spi.UriEndpoint;
import org.apache.camel.util.ObjectHelper;

/**
 *
 * @author timbo
 */
@UriEndpoint(scheme = "simple")
public class SimpleEndpoint extends DefaultEndpoint {

    private volatile Map<String, SimpleConsumer> consumers = new HashMap<String, SimpleConsumer>();

    public SimpleEndpoint() {
    }

    public SimpleEndpoint(String uri, SimpleComponent c) {
        super(uri, c);
        System.out.println("Creating Simple endpoint for " + uri);
    }

    @Override
    public Producer createProducer() throws Exception {
        System.out.println("createProducer for " + getEndpointUri() + " | " + getEndpointKey());
        return new SimpleProducer(this);
    }


    @Override
    public boolean isSingleton() {
        return true;
    }

    private String foo;

    /**
     * Get the value of foo
     *
     * @return the value of foo
     */
    public String getFoo() {
        return foo;
    }

    /**
     * Set the value of foo
     *
     * @param foo new value of foo
     */
    public void setFoo(String foo) {
        this.foo = foo;
    }

    @Override
    public Consumer createConsumer(Processor processor) throws Exception {
        System.out.println("createConsumer for " + getEndpointUri());
        Consumer answer = new SimpleConsumer(this, processor);
        configureConsumer(answer);
        return answer;
    }

    public void addConsumer(SimpleConsumer consumer) {
        String key = consumer.getEndpoint().getKey();
        consumers.put(key, consumer);
        System.out.println("Added consumer " + key + " " + consumer);
    }

    public void removeConsumer(SimpleConsumer consumer) {
        String key = consumer.getEndpoint().getKey();
        consumers.remove(key);
    }

    public boolean hasConsumer(SimpleConsumer consumer) {
        String key = consumer.getEndpoint().getKey();
        return consumers.containsKey(key);
    }

    public SimpleConsumer getConsumer() {
        System.out.println("Number of consumers for " + getEndpointUri() + " is " + consumers.size());
        String key = getKey();
        return consumers.get(key);
    }

    protected String getKey() {
        String uri = getEndpointUri();
        if (uri.indexOf('?') != -1) {
            return ObjectHelper.before(uri, "?");
        } else {
            return uri;
        }
    }

}
