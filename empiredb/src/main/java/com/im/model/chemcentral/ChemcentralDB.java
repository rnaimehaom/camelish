package com.im.model.chemcentral;

import org.apache.empire.data.DataMode;
import org.apache.empire.data.DataType;
import org.apache.empire.db.DBDatabase;
import org.apache.empire.db.DBTable;
import org.apache.empire.db.DBTableColumn;

/**
 *
 * @author timbo
 */
public class ChemcentralDB extends DBDatabase {

    private final static long serialVersionUID = 1L;

    // Declare all Tables and Views here
    public final ChemcentralDB.Sources SOURCES = new ChemcentralDB.Sources(this);
    public final ChemcentralDB.Categories CATEGORIES = new ChemcentralDB.Categories(this);
    public final ChemcentralDB.PropertyDefinitions PROPERTY_DEFINTIONS = new ChemcentralDB.PropertyDefinitions(this);
    public final ChemcentralDB.StructureProps STRUCTURE_PROPS = new ChemcentralDB.StructureProps(this);
    public final ChemcentralDB.Structures STRUCTURES = new ChemcentralDB.Structures(this);

    public ChemcentralDB() {
        // Define Foreign-Key Relations
        addRelation(SOURCES.CATEGORY_ID.referenceOn(CATEGORIES.ID));
        addRelation(SOURCES.ID.referenceOn(CATEGORIES.ID));
        addRelation(PROPERTY_DEFINTIONS.SOURCE_ID.referenceOn(SOURCES.ID));
        addRelation(STRUCTURE_PROPS.SOURCE_ID.referenceOn(SOURCES.ID));
        addRelation(STRUCTURE_PROPS.PROPERTY_ID.referenceOn(PROPERTY_DEFINTIONS.PROPERTY_ID));
        addRelation(STRUCTURE_PROPS.STRUCTURE_ID.referenceOn(STRUCTURES.CD_ID));
    }

    public static class Structures extends DBTable {

        public final DBTableColumn CD_ID;

        public Structures(DBDatabase db) {
            super("STRUCTURES", db);
            CD_ID = addColumn("CD_ID", DataType.AUTOINC, 0, DataMode.AutoGenerated);
            
            setPrimaryKey(CD_ID);

        }
    }

    public static class Sources extends DBTable {

        public final DBTableColumn ID;
        public final DBTableColumn CATEGORY_ID;
        public final DBTableColumn SOURCE_NAME;
        public final DBTableColumn SOURCE_DESCRIPTION;
        public final DBTableColumn TYPE;
        public final DBTableColumn OWNER;
        public final DBTableColumn MAINTAINER;
        public final DBTableColumn ACTIVE;
        public final DBTableColumn UPDATE_TIMESTAMP;

        public Sources(DBDatabase db) {
            super("SOURCES", db);
            // PROPERTY_ID
            ID = addColumn("ID", DataType.AUTOINC, 0, DataMode.AutoGenerated);
            CATEGORY_ID = addColumn("CATEGORY_ID", DataType.INTEGER, 0, DataMode.NotNull);
            SOURCE_NAME = addColumn("SOURCE_NAME", DataType.TEXT, 16, DataMode.NotNull);
            SOURCE_DESCRIPTION = addColumn("SOURCE_DESCRIPTION", DataType.TEXT, 500, DataMode.Nullable);
            TYPE = addColumn("TYPE", DataType.CHAR, 1, DataMode.NotNull);
            OWNER = addColumn("OWNER", DataType.TEXT, 50, DataMode.NotNull);
            MAINTAINER = addColumn("MAINTAINER", DataType.TEXT, 50, DataMode.NotNull);
            ACTIVE = addColumn("ACTIVE", DataType.BOOL, 1, DataMode.NotNull);
            UPDATE_TIMESTAMP = addColumn("UPDATE_TIMESTAMP", DataType.DATETIME, 0, DataMode.AutoGenerated);

            setPrimaryKey(ID);

            // TODO: unique constraing on SOURCE_NAME
        }

    }

    public static class Categories extends DBTable {

        public final DBTableColumn ID;
        public final DBTableColumn CATEGORY_NAME;
        public final DBTableColumn UPDATE_TIMESTAMP;

        public Categories(DBDatabase db) {
            super("CATEGORIES", db);
            // PROPERTY_ID
            ID = addColumn("ID", DataType.AUTOINC, 0, DataMode.AutoGenerated);
            CATEGORY_NAME = addColumn("CATEGORY_NAME", DataType.TEXT, 16, DataMode.NotNull);
            UPDATE_TIMESTAMP = addColumn("UPDATE_TIMESTAMP", DataType.DATETIME, 0, DataMode.AutoGenerated);

            setPrimaryKey(ID);

            // TODO: unique constraing on CATEGORY_NAME
        }

    }

    public static class PropertyDefinitions extends DBTable {

        public final DBTableColumn PROPERTY_ID;
        public final DBTableColumn SOURCE_ID;
        public final DBTableColumn PROPERTY_DESCRIPTION;
        public final DBTableColumn ORIGINAL_ID;
        public final DBTableColumn EST_SIZE;
        public final DBTableColumn DEFINITION;
        public final DBTableColumn EXAMPLE;
        public final DBTableColumn UPDATE_TIMESTAMP;

        public PropertyDefinitions(DBDatabase db) {
            super("PROPERTY_DEFINITIONS", db);
            // PROPERTY_ID
            PROPERTY_ID = addColumn("PROPERTY_ID", DataType.AUTOINC, 0, DataMode.AutoGenerated);
            SOURCE_ID = addColumn("CATEGORY_NAME", DataType.INTEGER, 0, DataMode.NotNull);
            PROPERTY_DESCRIPTION = addColumn("PROPERTY_DESCRIPTION", DataType.TEXT, 0, DataMode.NotNull);
            ORIGINAL_ID = addColumn("ORIGINAL_ID", DataType.TEXT, 32, DataMode.NotNull);
            EST_SIZE = addColumn("EST_SIZE", DataType.INTEGER, 0, DataMode.Nullable);
            DEFINITION = addColumn("DEFINITION", DataType.TEXT, 0, DataMode.NotNull);
            EXAMPLE = addColumn("EXAMPLE", DataType.TEXT, 0, DataMode.NotNull);

            UPDATE_TIMESTAMP = addColumn("UPDATE_TIMESTAMP", DataType.DATETIME, 0, DataMode.AutoGenerated);

            setPrimaryKey(PROPERTY_ID);

            // TODO: unique constraing on CATEGORY_NAME
        }

    }

    public static class StructureProps extends DBTable {

        public final DBTableColumn ID;
        public final DBTableColumn SOURCE_ID;
        public final DBTableColumn STRUCTURE_ID;
        public final DBTableColumn BATCH_ID;
        public final DBTableColumn PROPERTY_ID;
        public final DBTableColumn UPDATE_TIMESTAMP;
        public final DBTableColumn PROPERTY_DATA;

        public StructureProps(DBDatabase db) {
            super("STRUCTURE_PROPS", db);
            // PROPERTY_ID
            ID = addColumn("ID", DataType.AUTOINC, 0, DataMode.AutoGenerated);
            SOURCE_ID = addColumn("SOURCE_ID", DataType.INTEGER, 0, DataMode.NotNull);
            STRUCTURE_ID = addColumn("STRUCTURE_ID", DataType.INTEGER, 0, DataMode.NotNull);
            BATCH_ID = addColumn("BATCH_ID", DataType.INTEGER, 0, DataMode.Nullable);
            PROPERTY_ID = addColumn("PROPERTY_ID", DataType.INTEGER, 0, DataMode.NotNull);
            PROPERTY_DATA = addColumn("PROPERTY_DATA", DataType.TEXT, 0, DataMode.NotNull);

            UPDATE_TIMESTAMP = addColumn("UPDATE_TIMESTAMP", DataType.DATETIME, 0, DataMode.AutoGenerated);

            setPrimaryKey(ID);

            // TODO: unique constraing on CATEGORY_NAME
        }

    }

}
