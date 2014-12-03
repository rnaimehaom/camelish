package com.im.chemaxon.processor

import spock.lang.Specification
import chemaxon.formats.MolImporter
import chemaxon.formats.MolExporter
import chemaxon.struc.Molecule
import com.im.camel.testsupport.CamelSpecificationBase
import org.apache.camel.builder.RouteBuilder

/**
 * Created by timbo on 14/04/2014.
 */
class ChemTermsProcessorSpecification extends CamelSpecificationBase {


    def resultEndpoint

    def 'ChemTerms processor for List'() {

        given:
        resultEndpoint = camelContext.getEndpoint('mock:result')
        resultEndpoint.expectedMessageCount(1)
        

        when:
        def mols = []
        mols << MolImporter.importMol('C')
        mols << MolImporter.importMol('CC')        
        mols << MolImporter.importMol('CCC')
        template.sendBody('direct:chemTermsCalculator', mols)

        then:
        resultEndpoint.assertIsSatisfied()
        List result = resultEndpoint.receivedExchanges.in.body[0]
        result.size() == 3
        result[0].getPropertyObject('atom_count') == 5
        result[1].getPropertyObject('atom_count') == 8
        result[2].getPropertyObject('atom_count') == 11
        result[0].getPropertyObject('bond_count') == 4
        result[1].getPropertyObject('bond_count') == 7
        result[2].getPropertyObject('bond_count') == 10
    }
    
     def 'ChemTerms processor for Molecule'() {

        given:
        resultEndpoint = camelContext.getEndpoint('mock:result')
        resultEndpoint.expectedMessageCount(2)
        

        when:
        def mol0 = MolImporter.importMol('C')
        def mol1 = MolImporter.importMol('CC')  
        template.sendBody('direct:chemTermsCalculator', mol0)
        template.sendBody('direct:chemTermsCalculator', mol1)
        

        then:
        resultEndpoint.assertIsSatisfied()
        Molecule result0 = resultEndpoint.receivedExchanges.in.body[0]
        Molecule result1 = resultEndpoint.receivedExchanges.in.body[1]
        result0.getPropertyObject('atom_count') == 5
        result1.getPropertyObject('atom_count') == 8
        
    }

    @Override
    RouteBuilder createRouteBuilder() {
        return new RouteBuilder() {
            public void configure() {
                from("direct:chemTermsCalculator")
                .process(new ChemTermsProcessor()
                    .add('atomCount', 'atom_count')
                    .add('bondCount', 'bond_count'))
                .to('mock:result')
            }
        }
    }
}
