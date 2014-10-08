package com.im.chemaxon.examples.jchemsearch

import org.apache.camel.CamelContext
import org.apache.camel.ProducerTemplate
import org.apache.camel.builder.RouteBuilder
import org.apache.camel.impl.DefaultCamelContext
import com.im.chemaxon.camel.db.DefaultJChemSearcher


CamelContext camelContext = new DefaultCamelContext()
camelContext.addRoutes(new RouteBuilder() {
        def void configure() {

            from('direct:drugbankSearch')
            .log('Processing drugbank search for ${body}')
            .to('http4:localhost:8080/chemsearch/drugbank')
            .log('Found results ${body}')

        }
    })

camelContext.start()

ProducerTemplate t = camelContext.createProducerTemplate()
//t.sendBody('direct:drugbankSearch', 'Cc1ccncc1C')
//t.requestBodyAndHeader('direct:drugbankSearch', 'CCOC1=CC=C(C=C1)C1=C(C#N)C(N)=NC2=C1C(N)=C(S2)C(N)=O', 
//DefaultJChemSearcher.HEADER_SEARCH_OPTIONS, 'sep=, maxResults:10')
//t.sendBody('direct:drugbankSearch', 'NS(=O)(=O)C1=NC2=C(S1)C=C(O)C=C2')
//t.sendBody('direct:drugbankSearch', '[H][C@]1(CC(F)(F)CN1C(=O)CCN1C(=O)C2=C(C=CC=C2)C1=O)C(=O)N1CCCC1')
t.requestBodyAndHeader('direct:drugbankSearch', 'N[C@@H](CC1=CN=CN1)C(O)=O',
DefaultJChemSearcher.HEADER_SEARCH_OPTIONS, 'sep=, t:i,dissimilarityThreshold:0.1')
t.requestBodyAndHeader('direct:drugbankSearch', 'N[C@@H](CC1=CN=CN1)C(O)=O',
DefaultJChemSearcher.HEADER_SEARCH_OPTIONS, 'sep=, t:i,dissimilarityThreshold:0.9')
//t.requestBodyAndHeader('direct:drugbankSearch', 'CCOC1=CC=C(C=C1)C1=C(C#N)C(N)=NC2=C1C(N)=C(S2)C(N)=O', 
//DefaultJChemSearcher.HEADER_SEARCH_OPTIONS, 'sep=, maxResults:10')
sleep(100)
camelContext.stop()