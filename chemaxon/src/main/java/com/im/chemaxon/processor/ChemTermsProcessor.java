package com.im.chemaxon.processor;

import chemaxon.nfunk.jep.ParseException;
import chemaxon.struc.Molecule;
import com.im.camel.processor.ResultExtractor;
import com.im.chemaxon.molecule.ChemTermsEvaluator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.TypeConverter;
import java.util.logging.Logger;

/**
 *
 * @author timbo
 */
public class ChemTermsProcessor implements Processor, ResultExtractor<Molecule> {

    private static Logger LOG = Logger.getLogger(ChemTermsProcessor.class.getName());

    private TypeConverter typeConverter;
    private final List<ChemTermsEvaluator> evaluators = new ArrayList<>();

    public ChemTermsProcessor add(String ctExpression, String name) throws ParseException {

        evaluators.add(new ChemTermsEvaluator(ctExpression, name));
        return this;
    }

    public TypeConverter getTypeConverter() {
        return typeConverter;
    }

    @Override
    public void process(Exchange exchange) throws Exception {
        Molecule mol = exchange.getIn().getBody(Molecule.class);
        if (mol != null) {
            for (ChemTermsEvaluator evaluator : evaluators) {
                evaluator.evaluateMolecule(mol);
            }
            exchange.getIn().setBody(mol);
        } else {
            Iterable<Molecule> mols = exchange.getIn().getBody(Iterable.class);
            if (mols != null) {
                for (ChemTermsEvaluator evaluator : evaluators) {
                    evaluator.evaluateMolecules(mols);
                }
                exchange.getIn().setBody(mols);
            } else {
                LOG.warning("No valid Molecule content could be found. Calculations skipped");
            }

        }
    }

    /**
     * Get the calculated results for the Molecule. Allow for a molecule to be
     * passed back
     *
     * @param mol
     * @return
     */
    public Map<String, Object> extractResults(Molecule mol) {
        Map<String, Object> results = new HashMap<>();
        for (ChemTermsEvaluator evaluator : evaluators) {
            results.put(evaluator.getPropName(), evaluator.getResult(mol));
        }
        return results;
    }
}
