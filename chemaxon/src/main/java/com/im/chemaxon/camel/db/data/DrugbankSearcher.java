package com.im.chemaxon.camel.db.data;

import com.im.chemaxon.camel.db.DefaultJChemSearcher;

public class DrugbankSearcher extends DefaultJChemSearcher {

    public DrugbankSearcher() {
        setSearchOptions("t:d");
        setStructureTable("DRUGBANK_FEB_2014");
        //dbSearcher.connection = ds.getConnection()
        setOutputMode(DefaultJChemSearcher.OutputMode.STREAM);
        setOutputFormat("cxsmiles");

    }
}
