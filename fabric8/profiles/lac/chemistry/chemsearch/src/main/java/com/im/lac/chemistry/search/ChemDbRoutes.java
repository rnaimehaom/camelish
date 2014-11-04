/**
 *  Copyright 2005-2014 Red Hat, Inc.
 *
 *  Red Hat licenses this file to you under the Apache License, version
 *  2.0 (the "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 *  implied.  See the License for the specific language governing
 *  permissions and limitations under the License.
 */
package com.im.lac.chemistry.search;

import com.im.chemaxon.camel.db.DefaultJChemSearcher;

import chemaxon.struc.Molecule;

import javax.ejb.Startup;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.cdi.ContextName;
import org.apache.camel.cdi.Uri;

/**
 * Configures all our Camel routes, components, endpoints and beans
 */
@ContextName("DrugbankSearch")
@Startup
@ApplicationScoped
public class ChemDbRoutes extends RouteBuilder {

    @Inject
    @Uri("file://target/out")
    private Endpoint resultEndpoint;

    @Inject  
    private DefaultJChemSearcher dbSearcher;
    
    
    public ChemDbRoutes() {
        
    }

    @Override
    public void configure() throws Exception {
        
        from("jetty:http://0.0.0.0:8080/chemsearch/drugbank")
            // Jetty uses stream so body can only be read once.
            // So to avoid problems grab it as a String immediately
            .convertBodyTo(String.class) 
            .log("Processing search for ${body}")
            .setHeader(Exchange.CONTENT_TYPE, constant("text/plain")) 
            .process(dbSearcher);
    }

    public Endpoint getResultEndpoint() {
        return resultEndpoint;
    }
}
