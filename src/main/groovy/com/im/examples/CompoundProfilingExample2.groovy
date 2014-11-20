package com.im.examples

import chemaxon.struc.Molecule
import com.im.chemaxon.processor.ChemTermsProcessor
import chemaxon.formats.MolImporter
import groovy.sql.Sql
import org.apache.camel.*
import org.apache.camel.impl.*
import org.apache.camel.builder.*

ConfigObject database = new ConfigSlurper().parse(new File('loaders/database.properties').toURL())
ConfigObject drugbank = new ConfigSlurper().parse(new File('loaders/drugbank.properties').toURL())

Sql db = Sql.newInstance(database.url, 'vendordbs', 'vendordbs')

Processor dbQuery = new Processor() {
    // this is just to have something that works. A real impl would look completely different 
    @Override
    public void process(Exchange exchange) throws Exception {
        // TODO - stream the data, probably using a blocking queue?
        List results = new ArrayList<Molecule>()
        String sql = exchange.in.getBody(String.class)
        db.eachRow(sql) { row ->
            Molecule mol = MolImporter.importMol(row.cd_structure)
            mol.setPropertyObject('cd_id', row.cd_id)
            results << mol
        }
        exchange.out.body = results
    }
}

// think of these are the permanantly running routes that can be called.
CamelContext camelContext = new DefaultCamelContext()
camelContext.addRoutes(new RouteBuilder() {
        def void configure() {
            // route 1 - reads from the DB based on a query contained in the body and generates a stream of Molecules
            from("direct:drugBankMoleculeStream")
            .log('Processing ${body}')
            .process(dbQuery)
        
            // route 2 - calculates the atom count and adds it as a property of the Molecule
            from("direct:chemTermsCalculator")
            .log('Processing chem terms')
            .process(new ChemTermsProcessor().add('atomCount', 'atom_count'))
            
            // route 3 - pass to python to generate the frequencies
            from("direct:python")
            .to("language:python:file:src/main/python/FreqCounter.py?transform=false")
        }
    })
camelContext.start()

/* think of this as the glue that links together the routes based on the user interaction.
In this case each route is called on a request/response basis
*/
ProducerTemplate t = camelContext.createProducerTemplate()
def mols1 = t.requestBody('direct:drugBankMoleculeStream', \
    "select cd_id, cd_structure from ${drugbank.table} where cd_molweight > 300 and cd_molweight < 600 limit 100")
def mols2 = t.requestBody('direct:chemTermsCalculator', mols1)
def result = t.requestBody('direct:python', mols2)
println "Result = $result"


camelContext.stop()
db.close()
