package com.im.chemaxon.molecule

import spock.lang.Specification
import chemaxon.formats.MolImporter

/**
 * Created by timbo on 14/04/2014.
 */
class MoleculeUtilsSpecification extends Specification {


    def resultEndpoint

    def 'heavy atom counter'() {
        
        expect:
        MoleculeUtils.heavyAtomCount(MolImporter.importMol(smiles)) == counts

        where:
        smiles << ['c1ccccc1', 'c1ccncc1', 'CCCC', 'C[H]']
        counts << [6, 6, 4, 1]
    }

  
}
