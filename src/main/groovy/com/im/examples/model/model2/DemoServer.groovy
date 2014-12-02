package com.im.examples.model.model2

import javax.sql.DataSource
import chemaxon.formats.MolImporter
import chemaxon.util.*
import chemaxon.sss.screen.Similarity

import com.im.loaders.Utils
import groovy.transform.Canonical
import groovy.sql.Sql
import groovy.json.JsonSlurper

class DemoServer {
    
    private ConfigObject database, chemcentral
    private DataSource dataSource
    private Sql db
    private Map<String, DataSet> datasets = [:]
    private JsonSlurper jsonSlurper = new JsonSlurper()


    DemoServer() {
        database = Utils.createConfig('loaders/database.properties')
        chemcentral = Utils.createConfig('loaders/chemcentral.properties')
        dataSource = Utils.createDataSource(database, chemcentral.user, chemcentral.password)
        db = new Sql(dataSource)
    }
    
    
    /** this simulates the client
     */
    static void main(String[] args) {
        def server = new DemoServer()
        try {
   
            execute("Cleaning...") {
                server.clean()
                println "  clean complete"
            }
    
            // This simulates generating an intial dataset based on a query
            SubsetInfo listInfo
            execute("Generating list for query") {
                listInfo = server.queryForStructuresAsList('tim', 'test 1', null)
                println "  generated results for list $listInfo.id"
            }
            
    
            // this simulates restoring a persisted dataset and making it available for use
            String datasetName = 'tmpdata'
            execute("Restoring dataset $listInfo.id as $datasetName") {
                server.restoreDataSet(datasetName, listInfo.id)
                println "  dataset restored"
            }
    
            //            // create molecules and fingerprints. Are these persisted or generated dynamically?
            //            execute("Structurising") {
            //                //server.structurise(datasetName)
            //            }
    
            // run a simple filter
            SubsetInfo filterInfo
            execute("Simple filtering") {
                /* This is stop-gap measure to provide filtering just by passing a closure 
                 * that does the filtering.
                 * TODO - Need to establish how this would best be implemented.
                 */
                filterInfo = server.filter(datasetName, 'tim') { dataset -> 
                    // simple case of taking the first 100
                    def hits = []
                    def keys = dataset.members.keySet()
                    (0..99).each {
                        hits << keys[it]
                    }
                    return hits
                }
                println "  filter $filterInfo.id generated with $filterInfo.size results"
            }
            
            execute("Merging properties") {
                server.mergeProperties(datasetName, 1, 'drugbank') { json ->
                    def props = [:]
                    props['DrugBankID'] = json.drugbank_id
                    props['Brands'] = json.brands
                    props['Generic name'] = json.generic_name
                    
                    return props
                }
                println "  merge complete"
            }
            
            execute("Getting data") {
                def data = server.fetchData(datasetName, filterInfo.id)
                println "  fetched ${data.size()} rows"
            }
            
            execute("Cleaning...") {
                server.clean()
                println "  clean complete"
            }
        } finally {
            server.close()
        }
    }

    static def execute(String desc, Closure closure) {
        println desc
        long t0 = System.currentTimeMillis()
        closure()
        long t1 = System.currentTimeMillis()
        println "  took ${t1-t0}ms"
    }  
    
    /* ---------------- Sever methods --------------------- */
    
    void close() {
        db?.close()
    }
    
    void restoreDataSet(Object id, int listId) {
        DataSet dataset = fetchStructuresForList(listId)
        dataset.listId = listId
        datasets[id] = dataset
    }
    
    //    void structurise(String name) {
    //        def data = datasets[name].data
    //        if (data) {
    //            MolHandler mh = new MolHandler()
    //            data.each {
    //                def mol = createMol(it.structure)
    //                mh.molecule = mol
    //                def fp = createFp(mh)
    //                it.fp = fp
    //            }
    //        }
    //    }
    //    
    //    List filterBySimilarity(String name, def queryFp, double threshold) {
    //        List hits = []
    //        datasets[name].model.each {
    //            def targetFp = it.fp
    //            double similarity = Similarity.getTanimoto(queryFp, targetFp)
    //            if (similarity < threshold) {
    //                hits << it
    //            }
    //        }
    //        return hits
    //    }
    
    
    /** Fetch the actual data. This would need to be as JSON or similar.
     * TODO - need to be able to access subsets of data - only those that are needed
     * for display
     */
    List<RowSet.Row> fetchData(String datasetName, Object filterId) {
        def dataset = datasets[datasetName]
        List<RowSet.Row> results = dataset.getRows(filterId)
        return results
    }
    
    void mergeProperties(String datasetName, int propertyDefId, String propName, Closure closure) {
        def dataset = datasets[datasetName]
        RowSet childRowset = dataset.createChildRowSet(propName)
        def ex = childRowset.createDataExtractor(propertyDefId, closure)
        
        db.eachRow("""\
            |SELECT p.id, p.structure_id, p.property_data
            |  FROM chemcentral_01.structure_props p
            |  JOIN chemcentral_01.structures s ON p.structure_id = s.cd_id
            |  JOIN users.hit_list_data d ON d.id_item = s.cd_id
            |  JOIN users.hit_lists l ON l.id = d.hit_list_id
            |  WHERE l.id = ? AND p.property_id = ?""".stripMargin(), 
            [dataset.listId, propertyDefId]) { row ->
            
            def json = jsonSlurper.parseText(row['property_data'] as String)
            //            Map data = closure(json)
            int structureId = row['structure_id']
            int propertyId = row['id']
            //            def parentRow = dataset.members[structureId]
            //            if (parentRow) {
            //                childRowset.createRow(propertyId, data, parentRow)
            //            }

            ex.extract(structureId, propertyId, json)
            
        }
        //println "  $propName data: ${childRowset.members.values()}"
    }
    
    
    SubsetInfo filter(String datasetName, String owner, Closure closure) {
        DataSet dataset = datasets[datasetName]
        RowSet.Filter filter = dataset.createFilter(owner, closure)
        return new SubsetInfo(filter.uuid, filter.created, filter.owner, filter.ids.size())
    }
    
    private int generateHitList(String username, String listname) {
        println "  generating hit list named $listname"
        int id = db.executeInsert("INSERT INTO users.hit_lists (username, list_name, list_source) \n\
            VALUES (?,?,?)", [username, listname, 'https://foo.com/structures'])[0][0]
        println "  hitlist $id created"
        return id
    }
    
    void clean() {
        //println "  deleting all lists"
        db.executeUpdate("DELETE FROM users.hit_lists")
        //println "  lists deleted"
    }
    
    DataSet fetchStructuresForList(int listId) {
        println "  retrieving data for list $listId"
            
        DataSet dataset = new DataSet("Data $listId")
        db.eachRow("""\
                    SELECT cd_id, cd_structure
                      FROM chemcentral_01.structures s
                      JOIN users.hit_list_data d ON d.id_item = s.cd_id
                      JOIN users.hit_lists l ON l.id = d.hit_list_id
                      WHERE l.id = ?""", [listId]) { row ->
            
            dataset.createRow(row['cd_id'], buildRowData(row))
        }
        return dataset
    }

    
    SubsetInfo queryForStructuresAsList(String username, String listname, def query /*ignored for now */) {
        int listId = generateHitList(username, listname)
        
        println "  executing query against structures"
        db.executeInsert("""\
                    INSERT INTO users.hit_list_data (hit_list_id, id_item)
                    (SELECT ?, cd_id
                      FROM chemcentral_01.structures
                      WHERE frag_count = 1 AND cd_id < 2000)""", [listId]) 
        println "  query complete"
        // TODO: handle list size 
        return new SubsetInfo(listId, new Date(), 'username', 0)
    }
    
    private Map buildRowData(def row) {
        def o  = [:]
        o.id = row['cd_id']
        o.structure = new String(row['cd_structure'])
        return o
    }
    
    private byte[] createFp(def mh) {
        byte[] fp = mh.generateFingerprintInBytes(16, 2, 6)
        return fp
    }

    private def createMol(def thing) {
        def m = MolImporter.importMol(thing)
        m
    }  

    @Canonical
    class SubsetInfo {
        Object id
        Date created
        String owner
        int size
    }
}
