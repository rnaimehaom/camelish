/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.im.bioassay.camel.components;

import java.util.Map;
import java.util.logging.Logger;
import org.apache.camel.Endpoint;
import org.apache.camel.impl.DefaultComponent;

/**
 *
 * @author timbo
 */
public class CurvefitComponent extends DefaultComponent {
    
    private static Logger LOG = Logger.getLogger(CurvefitComponent.class.getName());
 
    
    @Override
    protected Endpoint createEndpoint(String uri, String remaining, Map<String, Object> parameters) throws Exception {
        Endpoint endpoint = new CurvefitEndpoint(uri, this);
        setProperties(endpoint, parameters);
        return endpoint;
    }
    
    
    
}
