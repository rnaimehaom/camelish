package com.im.examples.model.model2.converters

import chemaxon.formats.MolFormatException
import chemaxon.struc.Molecule
import com.im.chemaxon.io.MoleculeIOUtils
import org.apache.camel.Converter
import org.apache.camel.Exchange

import com.im.examples.model.model2.RowSet

/**
 *
 * @author timbo
 */
@Converter
class RowToMoleculeTypeConverter  {
	
    @Converter
    public static Molecule convert(RowSet.Row row, Exchange exchange) throws MolFormatException {
        println "================ Converting Row ================"
        def msg = exchange.in
        // TODO - allow the prop names to be read from a header
        Molecule mol = msg.body.data['__MOLECULE']
        if (mol != null) { 
            return mol
        } else {
            String molstr = msg.body.data['MOLECULE_AS_STRING']
            if (molstr != null) {
                return MoleculeIOUtils.convertToMolecule(molstr)
            } else {
                return null
            }
        }
    }
}

