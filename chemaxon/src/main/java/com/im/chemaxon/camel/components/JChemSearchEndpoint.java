/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.im.chemaxon.camel.components;

import chemaxon.jchem.db.JChemSearch;
import chemaxon.sss.SearchConstants;
import chemaxon.sss.search.JChemSearchOptions;
import chemaxon.util.ConnectionHandler;
import java.util.logging.Logger;
import javax.sql.DataSource;
import org.apache.camel.Consumer;
import org.apache.camel.Processor;
import org.apache.camel.Producer;
import org.apache.camel.spi.UriEndpoint;

/**
 *
 * @author timbo
 */
@UriEndpoint(scheme = "jchemsearch")
public class JChemSearchEndpoint extends AbstractJChemTableEndpoint {

    private static Logger LOG = Logger.getLogger(JChemSearchEndpoint.class.getName());

    protected JChemSearch searcher;

    /** JChem Search options string e.g. 't:d' 
     * 
     */
    private String searchOptions;


    public JChemSearchEndpoint(String uri, JChemSearchComponent component) {
        super(uri, component);
    }

    @Override
    public Producer createProducer() throws Exception {
        return new JChemSearchProducer(this);
    }

    @Override
    public Consumer createConsumer(Processor processor) throws Exception {
        throw new UnsupportedOperationException("from: not supported - You can't read messages from this endpoint");
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    /**
     * @return the searchOptions
     */
    public String getSearchOptions() {
        return searchOptions;
    }

    /**
     * @param searchOptions the searchOptions to set
     */
    public void setSearchOptions(String searchOptions) {
        this.searchOptions = searchOptions;
    }

   
    
    @Override
    protected void doStart() throws Exception {
        super.doStart();
        
        ds = getComponent().getCamelContext().getRegistry().lookupByNameAndType(getDataSourceRef(), DataSource.class);
        
        conh = createConnectionHandler();
        
        LOG.info("Creating JChemSearch");
        JChemSearchOptions opts = new JChemSearchOptions(SearchConstants.SUBSTRUCTURE);
        opts.setOptions(searchOptions);
        searcher = new JChemSearch();
        searcher.setStructureTable(getStructureTableName());
        searcher.setSearchOptions(opts);
        searcher.setConnectionHandler(conh);
        
    }

    @Override
    protected void doStop() throws Exception {
        
        searcher = null;
        
        super.doStop();        
    }
    
}
