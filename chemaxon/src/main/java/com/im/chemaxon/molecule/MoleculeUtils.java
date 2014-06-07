package com.im.chemaxon.molecule;

import chemaxon.calculations.clean.Cleaner;
import chemaxon.calculations.hydrogenize.Hydrogenize;
import chemaxon.formats.MolExporter;
import chemaxon.struc.MolAtom;
import chemaxon.struc.Molecule;
import chemaxon.struc.MoleculeGraph;
import java.io.IOException;

/**
 *
 * @author timbo
 */
public class MoleculeUtils {

    public String cleanOpts = null;
    public int removeExplicityHFlags = MolAtom.LONELY_H | MolAtom.WEDGED_H;
    public String exportFormat = "sdf";

    static public int heavyAtomCount(MoleculeGraph mol) {
        int count = 0;
        for (MolAtom atom : mol.getAtomArray()) {
            if (atom.getAtno() > 1) {
                count++;
            }
        }
        return count;
    }

    public MoleculeGraph clean(MoleculeGraph mol, int dim, String opts) {
        Cleaner.clean(mol, dim, opts);
        return mol;
    }

    public MoleculeGraph clean2d(MoleculeGraph mol) {
        clean(mol, 2, cleanOpts);
        return mol;
    }

    public MoleculeGraph clean3d(MoleculeGraph mol) {
        clean(mol, 3, cleanOpts);
        return mol;
    }

    public MoleculeGraph removeExplicitH(MoleculeGraph mol) {
        Hydrogenize.convertExplicitHToImplicit(mol, removeExplicityHFlags);
        return mol;
    }

    public String exportAsString(Molecule mol) throws IOException {
        return MolExporter.exportToFormat(mol, exportFormat);
    }
    
//    public static Processor createChemTermsProcessor(String chemTerms, Class cls) {
//        return new Processor() {
//            @Override
//            public Integer process(Molecule mol) {
//                return 0;
//            }
//        };
//    }
//    
//    public interface Processor<T> {
//        T process(Molecule mol);
//    }
   
}
