/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.im.chemaxon.molecule;

import chemaxon.nfunk.jep.ParseException;
import chemaxon.struc.Molecule;
import java.util.ArrayList;
import java.util.List;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;

/**
 *
 * @author timbo
 */
public class ChemTermsProcessor implements Processor {
    
    private final List<ChemTermsEvaluator> evaluators = new ArrayList<ChemTermsEvaluator>();
    
    public ChemTermsProcessor add(String ctExpression, String name) throws ParseException {
        
        evaluators.add(new ChemTermsEvaluator(ctExpression, name));
        return this;
    }

    @Override
    public void process(Exchange exchange) throws Exception {
        Iterable<Molecule> mols = exchange.getIn().getBody(Iterable.class);
        for (ChemTermsEvaluator evaluator : evaluators) {
            evaluator.evaluate(mols);
        }
    }
    
}
