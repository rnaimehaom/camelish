/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.im.chemaxon.camel.components;

import chemaxon.jchem.db.JChemSearch;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.impl.DefaultProducer;

/**
 *
 * @author timbo
 */
public class JChemSearchProducer extends DefaultProducer {

    private static final Logger LOG = Logger.getLogger(JChemSearchProducer.class.getName());

    private final JChemSearchEndpoint endpoint;

    public JChemSearchProducer(JChemSearchEndpoint endpoint) {
        super(endpoint);
        this.endpoint = endpoint;
    }

    @Override
    public void process(Exchange exchange) throws Exception {
        String query = exchange.getIn().getBody(String.class);

        endpoint.prepareConnectionHandler();

        JChemSearch searcher = endpoint.searcher;

        // set params
        searcher.setQueryStructure(query);
        // and press the GO button
        LOG.finer("Running JChemSearch");
        searcher.run();
        int[] hits = searcher.getResults();
        LOG.log(Level.INFO, "JChemSearch found {0} hits", hits.length);
        exchange.getIn().setBody(hits);
    }

}
