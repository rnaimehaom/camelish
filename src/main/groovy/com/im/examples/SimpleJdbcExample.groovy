package com.im.examples

import groovy.sql.*
import java.sql.*
import javax.sql.*
import org.postgresql.ds.PGSimpleDataSource

PGSimpleDataSource ds = new PGSimpleDataSource()
ds.serverName = 'localhost'
ds.portNumber = 49153
ds.databaseName = 'chemcentral'
ds.user = 'chembl'
ds.password = 'chembl'


Connection con = ds.connection
con.autoCommit = false

Statement st = con.createStatement();

// Turn use of the cursor on.
st.setFetchSize(50);
//st.setMaxRows(100)
println "executing"
ResultSet rs = st.executeQuery("SELECT * FROM chembl_19.compound_structures");
println "executed"
while (rs.next())
{
   print(".");
}
rs.close();
con.close()




