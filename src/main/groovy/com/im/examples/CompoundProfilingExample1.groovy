package com.im.examples

import chemaxon.struc.Molecule
import com.im.chemaxon.molecule.ChemTermsProcessor
import chemaxon.formats.MolImporter
import groovy.sql.Sql
import org.apache.camel.*
import org.apache.camel.impl.*
import org.apache.camel.builder.*

Sql db = Sql.newInstance('jdbc:mysql://localhost/vendordbs', 'vendordbs', 'vendordbs')

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

Processor frequncyCounter = new Processor() {
    @Override
    public void process(Exchange exchange) throws Exception {
        Iterable<Molecule> iter = exchange.in.getBody(Iterable.class)
        String property = exchange.in.getHeader('FrequncyCounter.property_name')
        def freqs = new TreeMap()
        if (property != null) {
            iter.each { Molecule m ->
                def value = m.getPropertyObject(property)
                if (freqs.containsKey(value)) {
                    freqs[value]++
                } else {
                    freqs[value] = 1
                }
            }
        }
        exchange.in.body = freqs
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
        
            // route 3 - builds the frequencies for the property identified by the header named FrequncyCounter.property_name
            from("direct:frequencyCounter")
            .log('Building frequencies')
            .process(frequncyCounter)
        }
    })
camelContext.start()

/* think of this as the glue that links together the routes based on the user interaction.
In this case each route is called on a request/response basis
 **/
ProducerTemplate t = camelContext.createProducerTemplate()
def mols1 = t.requestBody('direct:drugBankMoleculeStream', 'select * from DRUGBANK_FEB_2014 where cd_molweight > 300 and cd_molweight < 600 limit 100')
def mols2 = t.requestBody('direct:chemTermsCalculator', mols1)
def freqs = t.requestBodyAndHeader('direct:frequencyCounter', mols2, 'FrequncyCounter.property_name', 'atom_count')
println "Freqs: $freqs"

camelContext.stop()
db.close()
