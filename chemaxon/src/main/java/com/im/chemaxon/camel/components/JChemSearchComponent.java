/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.im.chemaxon.camel.components;

import chemaxon.sss.SearchConstants;
import chemaxon.sss.search.JChemSearchOptions;
import java.util.Map;
import java.util.logging.Logger;
import org.apache.camel.Endpoint;
import org.apache.camel.impl.DefaultComponent;

/**
 *
 * @author timbo
 */
public class JChemSearchComponent extends DefaultComponent {

    private static final Logger LOG = Logger.getLogger(JChemSearchComponent.class.getName());

    @Override
    protected Endpoint createEndpoint(String uri, String remaining, Map<String, Object> parameters)
            throws Exception {

        return new JChemSearchEndpoint(uri, this);
    }

//    @Override
//    protected void setProperties(Object bean, Map<String, Object> parameters) throws Exception {
//        super.setProperties(bean, parameters);
//
//        String optionsString = getAndRemoveParameter(parameters, "searchOptions", String.class);
//        LOG.info("Search options: " + optionsString);
//        JChemSearchOptions opts = new JChemSearchOptions(SearchConstants.SUBSTRUCTURE);
//        opts.setOptions(optionsString);
//
//    }
    

}
