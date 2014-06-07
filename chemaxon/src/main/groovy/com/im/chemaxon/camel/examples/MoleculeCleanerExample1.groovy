package com.im.chemaxon.camel.examples

import com.im.chemaxon.molecule.MoleculeUtils
import chemaxon.formats.MolExporter
import chemaxon.struc.Molecule
import com.im.chemaxon.io.MoleculeIOUtils
import org.apache.camel.*
import org.apache.camel.impl.*
import org.apache.camel.processor.aggregate.AggregationStrategy

import org.apache.camel.builder.*

MoleculeUtils molUtils = new MoleculeUtils()

MolExporter exporter = new MolExporter('/Users/timbo/tmp/examples/moleculeCleaner/out/file.sdf', 'sdf')
Processor processor = new Processor() {
    void process(Exchange exchange) {
        Molecule mol = exchange.in.getBody(Molecule.class)
        exporter.write(mol)
    }
}

class MolExporterAggregater implements AggregationStrategy {
    
    @Override
    public Exchange aggregate(Exchange oldExchange, Exchange newExchange) {
        Molecule mol = newExchange.in.getBody(Molecule.class)
        MolExporter molExporter
        if (oldExchange == null) {
            String specifiedPath = newExchange.in.getHeader('ChemAxonMolExporterFileName')            
            String specifiedFormat = newExchange.in.getHeader('ChemAxonMolExporterFileFormat')
            if (!specifiedPath) {
                String path = newExchange.in.getHeader('CamelFileParent')
                String file = newExchange.in.getHeader('CamelFileNameOnly')
                specifiedPath = path + File.separator + 'out' + File.separator + file
            }
            if (!specifiedFormat) {
                specifiedFormat = mol.getInputFormat()
            }
            if (!specifiedFormat) {
                specifiedFormat = 'sdf'
            }
            
            
            molExporter = new MolExporter(specifiedPath, specifiedFormat)
        } else {
            molExporter = oldExchange.in.getBody(MolExporter.class)
        }
        molExporter.write(mol)
        newExchange.in.setBody(molExporter)
        return newExchange
    }
}


CamelContext camelContext = new DefaultCamelContext()
camelContext.addRoutes(new RouteBuilder() {
        def void configure() {
            from("file:///Users/timbo/tmp/examples/moleculeCleaner?move=orig")
            .log('Processing ${file:name}')
            .split().method(MoleculeIOUtils.class, 'moleculeIterator').streaming()
            .aggregationStrategy(new MolExporterAggregater())
            .log('Processing record')
            .bean(molUtils, 'removeExplicitH')
            .bean(molUtils, 'clean2d')
            //.process(processor)
            .end()
            .log('done ${body}')
            //.to("file:///Users/timbo/tmp/examples/moleculeCleaner/out")
                
        }
    })
camelContext.start()
sleep(10000)
camelContext.stop()

exporter.flush()
exporter.close()