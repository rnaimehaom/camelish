/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.im.chemaxon.camel.components;

import org.apache.camel.Endpoint;
import org.apache.camel.Processor;
import org.apache.camel.impl.DefaultConsumer;

/**
 *
 * @author timbo
 */
public class JChemSearchConsumer extends DefaultConsumer {

    public JChemSearchConsumer(Endpoint endpoint, Processor processor) {
        super(endpoint, processor);
    }
    
}
