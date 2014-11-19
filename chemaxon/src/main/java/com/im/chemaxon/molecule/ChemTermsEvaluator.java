package com.im.chemaxon.molecule;

import chemaxon.jep.ChemJEP;
import chemaxon.jep.Evaluator;
import chemaxon.jep.context.MolContext;
import chemaxon.nfunk.jep.ParseException;
import chemaxon.struc.Molecule;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author timbo
 */
public class ChemTermsEvaluator {

    ChemJEP chemJEP;
    String propName;

    public ChemTermsEvaluator(String chemTermsFunction, String name) throws ParseException {
        // create ChemJEP, compile the Chemical Terms expression
        chemJEP = new Evaluator().compile(chemTermsFunction, MolContext.class);
        this.propName = name;
    }

    public void evaluate(Iterable<Molecule> mols) {

        MolContext context = new MolContext();

        for (Molecule mol : mols) {
            context.setMolecule(mol);
            try {
                Object result = chemJEP.evaluate(context);
                mol.setPropertyObject(propName, result);
            } catch (ParseException ex) {
                Logger.getLogger(ChemTermsEvaluator.class.getName()).log(
                        Level.WARNING, "Failed to evaluate chem terms expression", ex);
            }

        }
    }
}
