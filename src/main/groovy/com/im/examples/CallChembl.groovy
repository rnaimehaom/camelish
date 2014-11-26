package com.im.examples

import com.im.chemaxon.io.MoleculeIOUtils

import org.apache.camel.CamelContext
import org.apache.camel.Exchange
import org.apache.camel.ProducerTemplate
import org.apache.camel.builder.RouteBuilder
import org.apache.camel.impl.DefaultCamelContext
import org.apache.camel.component.http4.*
import org.apache.camel.util.jsse.*



CamelContext camelContext = new DefaultCamelContext()

camelContext.addRoutes(new RouteBuilder() {
        def void configure() {
            
            from("file:data/filedrop/CallChembl?move=out")
            .log('Processing ${file:name}')
            .to('direct:uncompressFile')
            .split().method(MoleculeIOUtils.class, "mrecordIterator")
            .log('Processing record ${property.CamelSplitIndex}')
            .end()
            .log('Finished processing')

            
            from('direct:uncompressFile')
            .choice().
                when(header("CamelFileName").endsWith(".gz")).unmarshal().gzip().
                when(header("CamelFileName").endsWith(".zip")).unmarshal().zip()
            .endChoice()
            
            from('direct:simple')
            .to('https4:www.ebi.ac.uk/chemblws/compounds/CHEMBL1.json')
            .log('got ${body}')
            
            from('direct:httppost')
            .setHeader(Exchange.HTTP_METHOD, constant(HttpMethods.POST))
            .setHeader(Exchange.CONTENT_TYPE, constant('application/json'))
            .to('https4:www.ebi.ac.uk/chemblws/compounds/substructure.json')
            .log('got ${body}')
            
            from('direct:restlet')
            .to('restlet:http://www.ebi.ac.uk/chemblws/compounds/substructure.json?restletMethod=POST')
            .log("received ${body()}")
                
                
        }
    })

camelContext.start()

//println "Ready to process. Drop files into data/filedrop/CallChembl to process"
//println "Will run for 1 mins or use Ctrl-C to finish"
//
//sleep(60000)
//db.close()

// END of main script

ProducerTemplate t = camelContext.createProducerTemplate()
//t.sendBody('direct:simple', null)
t.sendBody('direct:httppost', '{"smiles": "Cc1cc(ccc1C(=O)c2ccccc2Cl)N3N=CC(=O)NC3=O"}')


println "finished"

camelContext.stop()