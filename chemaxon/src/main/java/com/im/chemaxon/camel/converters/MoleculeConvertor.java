package com.im.chemaxon.camel.converters;

import chemaxon.formats.MolFormatException;
import chemaxon.struc.Molecule;
import com.im.chemaxon.io.MoleculeIOUtils;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.SQLException;
import java.util.Map;
import org.apache.camel.Converter;
import org.apache.camel.Exchange;

/**
 * Created by timbo on 21/04/2014.
 */
@Converter
public class MoleculeConvertor {

    @Converter
    public static Molecule convert(String s, Exchange exchange) throws MolFormatException {
        return MoleculeIOUtils.convertToMolecule(s);
    }

    @Converter
    public static Molecule convert(byte[] bytes, Exchange exchange) throws MolFormatException {
        return MoleculeIOUtils.convertToMolecule(bytes);
    }

    @Converter
    public static Molecule convert(Blob blob, Exchange exchange) throws MolFormatException, SQLException {
        return MoleculeIOUtils.convertToMolecule(blob);
    }

    @Converter
    public static Molecule convert(Clob clob, Exchange exchange) throws MolFormatException, SQLException {
        return MoleculeIOUtils.convertToMolecule(clob);
    }

    @Converter
    public static Molecule convert(Map map, String key, Exchange exchange) throws MolFormatException, SQLException {
        return MoleculeIOUtils.convertToMolecule(map, key);
    }
}
