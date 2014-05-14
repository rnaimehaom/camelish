package com.im.chemaxon.camel.db;

import chemaxon.util.ConnectionHandler;
import org.apache.camel.support.ServiceSupport;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.logging.Logger;

/**
 * Created by timbo on 26/04/2014.
 */
public class ConnectionHandlerService extends ServiceSupport {


    private static final Logger log = Logger.getLogger(ConnectionHandlerService.class.getName());


    private Connection connection;
    private DataSource dataSource;
    private ConnectionHandler conh;

    @Override
    protected void doStart() throws Exception {
        if (conh == null) {

            if (connection == null) {
                if (dataSource == null) {
                    throw new IllegalStateException("ConnectionHandlerService cannot be started. " +
                            "Must provide ConnectionHandler, DataSource or Connection");
                }
                connection = dataSource.getConnection();
            }
            conh = new ConnectionHandler();
            conh.setConnection(connection);
        }
    }

    @Override
    protected void doStop() throws Exception {
        //conh.close();
        conh.setConnection(null);

    }

    public Connection getConnection() {
        return connection;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public ConnectionHandler getConnectionHandler() {
        return conh;
    }

    public void setConnectionHandler(ConnectionHandler conh) {
        this.conh = conh;
    }
}
