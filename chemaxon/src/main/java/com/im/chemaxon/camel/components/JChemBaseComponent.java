/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.im.chemaxon.camel.components;

import java.util.Map;
import java.util.logging.Logger;
import org.apache.camel.Endpoint;
import org.apache.camel.impl.DefaultComponent;

/**
 *
 * @author timbo
 */
public class JChemBaseComponent extends DefaultComponent {

    private static final Logger LOG = Logger.getLogger(JChemBaseComponent.class.getName());
    
    private JChemBaseEndpoint endpoint;

    @Override
    protected Endpoint createEndpoint(String uri, String remaining, Map<String, Object> parameters)
            throws Exception {
        endpoint = new JChemBaseEndpoint(uri, this);
        return endpoint;
    }

    @Override
    protected void setProperties(Object bean, Map<String, Object> parameters) throws Exception {
        super.setProperties(bean, parameters);
        
        if (endpoint.getMode() == null) {
            throw new IllegalStateException("Mode parameter must be specified");
        }
        
    }
    
    

}
