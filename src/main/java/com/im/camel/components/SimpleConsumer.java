/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.im.camel.components;

import org.apache.camel.Endpoint;
import org.apache.camel.Processor;
import org.apache.camel.impl.DefaultConsumer;

/**
 *
 * @author timbo
 */
public class SimpleConsumer extends DefaultConsumer {

    public SimpleConsumer(Endpoint endpoint, Processor processor) {
        super(endpoint, processor);
    }

    @Override
    public SimpleEndpoint getEndpoint() {
        return (SimpleEndpoint) super.getEndpoint();
    }

    @Override
    protected void doStart() throws Exception {
        // add consumer to endpoint
        boolean existing = this == getEndpoint().getConsumer();
        if (!existing && getEndpoint().hasConsumer(this)) {
            throw new IllegalArgumentException("Cannot add a 2nd consumer to the same endpoint. Endpoint " + getEndpoint() + " only allows one consumer.");
        }
        if (!existing) {
            getEndpoint().addConsumer(this);
        }
    }

    @Override
    protected void doStop() throws Exception {
        getEndpoint().removeConsumer(this);
    }

    @Override
    protected void doSuspend() throws Exception {
        getEndpoint().removeConsumer(this);
    }

    @Override
    protected void doResume() throws Exception {
        // resume by using the start logic
        doStart();
    }

}
