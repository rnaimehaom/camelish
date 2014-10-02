/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.im.chemaxon.camel.components;

import chemaxon.jchem.db.UpdateHandler;
import java.util.logging.Logger;
import org.apache.camel.Exchange;
import org.apache.camel.impl.DefaultProducer;

/**
 *
 * @author timbo
 */
public class JChemBaseProducer extends DefaultProducer {
    
    private static final Logger LOG = Logger.getLogger(JChemBaseProducer.class.getName());
    
    private final JChemBaseEndpoint endpoint;
    
    
    public JChemBaseProducer(JChemBaseEndpoint endpoint) {
        super(endpoint);
        this.endpoint = endpoint;
    }

    @Override
    public void process(Exchange exchange) throws Exception {
        LOG.info("processing ...");
        
        String mol = exchange.getIn().getBody(String.class);
        
        endpoint.prepareConnectionHandler();
        
        UpdateHandler uh = endpoint.inserter;
        uh.setStructure(mol);
        int id = uh.execute(true);
        LOG.info("Inserted structure with ID " + id);
        
    }
    
}
