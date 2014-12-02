package com.im.model.chemcentral;

import org.apache.empire.samples.db.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.List;

import org.apache.empire.commons.StringUtils;
import org.apache.empire.data.bean.BeanResult;
import org.apache.empire.db.DBColumnExpr;
import org.apache.empire.db.DBCommand;
import org.apache.empire.db.DBDatabaseDriver;
import org.apache.empire.db.DBReader;
import org.apache.empire.db.DBRecord;
import org.apache.empire.db.DBSQLScript;
import org.apache.empire.db.derby.DBDatabaseDriverDerby;
import org.apache.empire.db.h2.DBDatabaseDriverH2;
import org.apache.empire.db.hsql.DBDatabaseDriverHSql;
import org.apache.empire.db.postgresql.DBDatabaseDriverPostgreSQL;
import org.apache.empire.xml.XMLWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

public class ChemcentralApp {

    // Logger
    private static final Logger log = LoggerFactory.getLogger(ChemcentralApp.class);

    private static final ChemcentralDB db = new ChemcentralDB();

    private static ChemcentralConfig config = new ChemcentralConfig();

    private enum QueryType {

        Reader,
        BeanList,
        XmlDocument
    }

    /**
     * <PRE>
     * This is the entry point of the Empire-DB Sample Application
     * Please check the config.xml configuration file for Database and Connection settings.
     * </PRE>
     *
     * @param args arguments
     */
    public static void main(String[] args) {
        try {
            // Init Configuration
            config.init((args.length > 0 ? args[0] : "config-chemcentral.xml"));

            System.out.println("Running DB Sample...");

            // STEP 1: Get a JDBC Connection
            System.out.println("*** Step 1: getJDBCConnection() ***");

            Connection conn = getJDBCConnection();

            // STEP 2: Choose a driver
            System.out.println("*** Step 2: getDatabaseProvider() ***");
            DBDatabaseDriver driver = getDatabaseDriver(config.getDatabaseProvider(), conn);

            // STEP 3: Open Database (and create if not existing)
            System.out.println("*** Step 3: openDatabase() ***");
            try {
                // Open the database
                db.open(driver, conn);
                // Check whether database exists
                databaseExists(conn);
                System.out.println("*** Database already exists. Skipping Step4 ***");

            } catch (Exception e) {
                // STEP 4: Create Database
                System.out.println("*** Step 4: createDDL() ***");
                // postgre does not support DDL in transaction
                if (db.getDriver() instanceof DBDatabaseDriverPostgreSQL) {
                    conn.setAutoCommit(true);
                }
                createDatabase(driver, conn);
                if (db.getDriver() instanceof DBDatabaseDriverPostgreSQL) {
                    conn.setAutoCommit(false);
                }
                // Open again
                if (db.isOpen() == false) {
                    db.open(driver, conn);
                }
            }

            // STEP 5: Clear Database (Delete all records)
            System.out.println("*** Step 5: clearDatabase() ***");
            clearDatabase(conn);
//
            // STEP 6: Seed data
            System.out.println("*** Step 6: insertDepartment() & insertEmployee() ***");
            int idDefaultCategory = insertCategory(conn, "default");

            // commit
            db.commit(conn);

            // STEP 8: Option 1: Query Records and print tab-separated
            System.out.println("*** Step 8 Option 1: queryRecords() / Tab-Output ***");
            queryRecords(conn, QueryType.Reader); // Tab-Output

            // STEP 8: Option 2: Query Records as a list of java beans
            System.out.println("*** Step 8 Option 2: queryRecords() / Bean-List-Output ***");
            queryRecords(conn, QueryType.BeanList); // Bean-List-Output

            // STEP 8: Option 3: Query Records as XML
            System.out.println("*** Step 8 Option 3: queryRecords() / XML-Output ***");
            queryRecords(conn, QueryType.XmlDocument); // XML-Output
//
//			// STEP 9: Use Bean Result to query beans
//			queryBeans(conn);
            // Done
            System.out.println("DB Sample finished successfully.");

        } catch (Exception e) {
            // Error
            System.out.println(e.toString());
            e.printStackTrace();
        }

    }

    /**
     * <PRE>
     * Opens and returns a JDBC-Connection.
     * JDBC url, user and password for the connection are obtained from the SampleConfig bean
     * Please use the config.xml file to change connection params.
     * </PRE>
     */
    private static Connection getJDBCConnection() {
        // Establish a new database connection
        Connection conn = null;
        log.info("Connecting to Database'" + config.getJdbcURL() + "' / User=" + config.getJdbcUser());
        try {
            // Connect to the database
            Class.forName(config.getJdbcClass()).newInstance();
            conn = DriverManager.getConnection(config.getJdbcURL(), config.getJdbcUser(), config.getJdbcPwd());
            log.info("Connected successfully");
            // set the AutoCommit to false for this connection. 
            // commit must be called explicitly! 
            conn.setAutoCommit(false);
            log.info("AutoCommit is " + conn.getAutoCommit());

        } catch (Exception e) {
            log.error("Failed to connect directly to '" + config.getJdbcURL() + "' / User=" + config.getJdbcUser());
            log.error(e.toString());
            throw new RuntimeException(e);
        }
        return conn;
    }

    /**
     * Creates an Empire-db DatabaseDriver for the given provider and applies
     * driver specific configuration
     */
    private static DBDatabaseDriver getDatabaseDriver(String provider, Connection conn) {
        try {   // Get Driver Class Name
            String driverClassName = config.getEmpireDBDriverClass();
            if (StringUtils.isEmpty(driverClassName)) {
                throw new RuntimeException("Configuration error: Element 'empireDBDriverClass' not found in node 'properties-" + provider + "'");
            }

            // Create driver
            DBDatabaseDriver driver = (DBDatabaseDriver) Class.forName(driverClassName).newInstance();

            // Configure driver
            config.readProperties(driver, "properties-" + provider, "empireDBDriverProperites");

            // Special cases
            if (driver instanceof DBDatabaseDriverPostgreSQL) {   // Create the reverse function that is needed by this sample
                ((DBDatabaseDriverPostgreSQL) driver).createReverseFunction(conn);
            }

            // done
            return driver;

        } catch (Exception e) {   // catch any checked exception and forward it
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    /**
     * <PRE>
     * Checks whether the database exists or not by executing
     *     select count(*) from CATEGORIES
     * If the Departments table does not exist the querySingleInt() function return -1 for failure.
     * Please note that in this case an error will appear in the log which can be ignored.
     * </PRE>
     */
    private static boolean databaseExists(Connection conn) {
        // Check whether DB exists
        DBCommand cmd = db.createCommand();
        cmd.select(db.CATEGORIES.count());
        // Check using "select count(*) from CATEGORIES"
        System.out.println("Checking whether table CATEGORIES exists (SQLException will be logged if not - please ignore) ...");
        return (db.querySingleInt(cmd, -1, conn) >= 0);
    }

    /**
     * <PRE>
     * Creates a DDL Script for entire SampleDB Database and executes it line by line.
     * Please make sure you uses the correct DatabaseDriver for your target DBMS.
     * </PRE>
     */
    private static void createDatabase(DBDatabaseDriver driver, Connection conn) {
        // create DDL for Database Definition
        DBSQLScript script = new DBSQLScript();
        db.getCreateDDLScript(driver, script);
        // Show DDL Statement
        System.out.println(script.toString());
        // Execute Script
        script.run(driver, conn, false);
        // Commit
        db.commit(conn);
    }

    /**
     * <PRE>
     * Empties all Tables.
     * </PRE>
     */
    private static void clearDatabase(Connection conn) {
        DBCommand cmd = db.createCommand();
        db.executeDelete(db.STRUCTURE_PROPS, cmd, conn);
        db.executeDelete(db.PROPERTY_DEFINTIONS, cmd, conn);
        db.executeDelete(db.STRUCTURES, cmd, conn);
        db.executeDelete(db.SOURCES, cmd, conn);
        db.executeDelete(db.CATEGORIES, cmd, conn);
    }

    /**
     * <PRE>
     * Insert a Department into the Departments table.
     * </PRE>
     */
    private static int insertCategory(Connection conn, String categoryName) {
        // Insert a Department
        DBRecord rec = new DBRecord();
        rec.create(db.CATEGORIES);
        rec.setValue(db.CATEGORIES.CATEGORY_NAME, categoryName);
        rec.update(conn);
        // Return Department ID
        return rec.getInt(db.CATEGORIES.ID);
    }

    /**
     * <PRE>
     * Performs an SQL-Query and prints the result to System.out
     *
     *
     * For processing the rows there are three options available:
     *
     *   QueryType.Reader:
     *     Iterates through all rows and prints field values as tabbed text.
     *
     *   QueryType.BeanList:
     *     Obtains the query result as a list of JavaBean objects of type SampleBean.
     *     It then iterates through the list of beans and uses bean.toString() for printing.
     *
     *   QueryType.XmlDocument:
     *     Obtains the query result as an XML-Document and prints the document.
     *     Please note, that the XML not only contains the data but also the field metadata.
     * </PRE>
     */
    private static void queryRecords(Connection conn, QueryType queryType) {

        // Define the query
        DBCommand cmd = db.createCommand();
        // Define shortcuts for tables used - not necessary but convenient
        ChemcentralDB.Categories CAT = db.CATEGORIES;

        // DBColumnExpr genderExpr = cmd.select(EMP.GENDER.decode(EMP.GENDER.getOptions()).as(EMP.GENDER.getName()));
        // Select required columns
        cmd.select(CAT.ID, CAT.CATEGORY_NAME);

        // Query Records and print output
        DBReader reader = new DBReader();
        try {
            // Open Reader
            System.out.println("Running Query:");
            System.out.println(cmd.getSelect());
            reader.open(cmd, conn);
            // Print output
            System.out.println("---------------------------------");
            switch (queryType) {
                case Reader:
                    // Text-Output by iterating through all records.
                    while (reader.moveNext()) {
                        System.out.println(reader.getString(CAT.ID)
                                + "\t" + reader.getString(CAT.CATEGORY_NAME));
                    }
                    break;
                case BeanList:
                    // Text-Output using a list of Java Beans supplied by the DBReader
                    List<CategoryBean> beanList = reader.getBeanList(CategoryBean.class);
                    System.out.println(String.valueOf(beanList.size()) + " CategoryBeans returned from Query.");
                    for (CategoryBean b : beanList) {
                        System.out.println(b.toString());
                    }
                    break;
                case XmlDocument:
                    // XML Output
                    Document doc = reader.getXmlDocument();
                    // Print XML Document to System.out
                    XMLWriter.debug(doc);
                    break;
            }

        } finally {
            // always close Reader
            reader.close();
        }
    }
//

    private static void queryBeans(Connection conn) {
        // Query all males
//        BeanResult<SampleBean> result = new BeanResult<SampleBean>(SampleBean.class, db.EMPLOYEES);
//        result.getCommand().where(db.EMPLOYEES.GENDER.is(Gender.M));
//        result.fetch(conn);
//
//        System.out.println("Number of male employees is: " + result.size());
//
//        // And now, the females
//        result.getCommand().where(db.EMPLOYEES.GENDER.is(Gender.F));
//        result.fetch(conn);
//
//        System.out.println("Number of female employees is: " + result.size());
    }

}
