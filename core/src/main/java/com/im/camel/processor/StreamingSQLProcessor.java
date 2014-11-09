/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.im.camel.processor;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.sql.DataSource;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.component.jdbc.ResultSetIterator;
import org.apache.camel.spi.Synchronization;

/**Processor that allows a ResultSet to be streamed and avoid holding it entirely 
 * in memory.
 * This Processor is a workaround to a problem with the Camel JBBC component that
 * does not allow the ResultSet to be streamed using the fetchSize property (at least
 * with PostgreSQL).
 * This class uses the same processing semantics as the Camel JBBC component.
 *
 * @author timbo
 */
public class StreamingSQLProcessor implements Processor {
    
    private final DataSource dataSource;
    private final int fetchSize;
    private boolean isJDBC4Semantics = true;
    
    /**
     *
     * @param dataSource The dataSource to use
     * @param fetchSize The fetch size to use. Results in Statement.setFetchSize() being called with that value.
     * @param isJDBC4Semantics
     */
    public StreamingSQLProcessor(DataSource dataSource, int fetchSize, boolean isJDBC4Semantics) {
        this.dataSource = dataSource;
        this.fetchSize = fetchSize;
        this.isJDBC4Semantics = isJDBC4Semantics;
    }

    @Override
    public void process(Exchange exchange) throws SQLException {
        String sql = exchange.getIn().getBody(String.class);
        Connection con = dataSource.getConnection();
        con.setAutoCommit(false);
        Statement st = con.createStatement();
        st.setFetchSize(fetchSize);
        ResultSet rs = st.executeQuery(sql);
        final ResultSetIterator iterator = new ResultSetIterator(rs, isJDBC4Semantics);
        exchange.addOnCompletion(new Synchronization() {
            @Override
            public void onComplete(Exchange exch) {
                iterator.close();
                iterator.closeConnection();
            }

            @Override
            public void onFailure(Exchange exch) {
                iterator.close();
                iterator.closeConnection();
            }
        });
        exchange.getIn().setBody(iterator);
    }

}
