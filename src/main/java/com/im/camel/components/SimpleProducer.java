/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.im.camel.components;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.impl.DefaultProducer;

/**
 *
 * @author timbo
 */
public class SimpleProducer extends DefaultProducer {
    
    SimpleEndpoint endpoint;
    
    public SimpleProducer(SimpleEndpoint endpoint) {
        super(endpoint);
        this.endpoint = endpoint;
    }

    @Override
    public void process(Exchange exchange) throws Exception {
        System.out.println("Producer for " + endpoint.getEndpointUri() + " is processing " + exchange);
        SimpleConsumer cons = endpoint.getConsumer();
        System.out.println("Consumer: " + cons);
        Processor p = cons.getProcessor();
        System.out.println("Processor: " + p);
        p.process(exchange);
    }
    
}
