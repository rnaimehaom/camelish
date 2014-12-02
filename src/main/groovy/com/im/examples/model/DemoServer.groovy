package com.im.examples.model

import javax.sql.DataSource
import chemaxon.formats.MolImporter
import chemaxon.util.*
import chemaxon.sss.screen.Similarity

import com.im.loaders.Utils
import groovy.transform.Canonical
import groovy.sql.Sql
import groovy.json.JsonSlurper
import groovy.json.JsonOutput

class DemoServer {
    
    private ConfigObject database, chemcentral
    private DataSource dataSource
    private Sql db
    private Map datasets = [:]


    DemoServer() {
        database = Utils.createConfig('loaders/database.properties')
        chemcentral = Utils.createConfig('loaders/chemcentral.properties')
        dataSource = Utils.createDataSource(database, chemcentral.user, chemcentral.password)
        db = new Sql(dataSource)
    }
    
    
    static void main(String[] args) {
        def server = new DemoServer()
        try {
            //    
            //    println "Generating similarity scores"
            //    t0 = System.currentTimeMillis()
            //    def queryFp = json[0].fp
            //    List hits = server.filterBySimilarity("data", queryFp, 0.05d)
            //    println "  Found ${hits.size()} hits "//${hits*.id.join(' ')}"
            //    t1 = System.currentTimeMillis()
            //    println "  Took ${t1-t0}ms"
    
            execute("Cleaning...") {
                server.clean()
                println "  clean complete"
            }
    
            // This simulates generating an intial dataset based on a query
            int listId
            execute("Generating list for query") {
                listId = server.queryForStructuresAsList('tim', 'test 1', null)
                println "  generated results for list $listId"
            }
    
            // this simulates restoring a persisted dataset and making it available for use
            String datasetName = 'tmpdata'
            execute("Restoring dataset $listId as $datasetName") {
                server.restoreDataSet(datasetName, listId)
                println "  dataset restored"
            }
    
            // create molecules and fingerprints. Are these persisted or generated dynamically?
            execute("Structurising") {
                //server.structurise(datasetName)
            }
    
            // run a simple filter
            FilterResult filter
            execute("Simple filtering") {
                filter = server.filterByFirstN(datasetName, 500)
                println "  filter $filter.uuid contains ${filter.count} items"
            }
   
            // run a structure filter
    
            // retrieve data for display
            execute("Results of filter as JSON") {
                String json = server.fetchData(datasetName, filter.uuid)
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
    
    void restoreDataSet(String name, int listId) {
        List result = fetchStructuresForList(listId)
        datasets[name] = new Model(result)
    }
    
    void structurise(String name) {
        def data = datasets[name].data
        if (data) {
            MolHandler mh = new MolHandler()
            data.each {
                def mol = createMol(it.structure)
                mh.molecule = mol
                def fp = createFp(mh)
                it.fp = fp
            }
        }
    }
    
    List filterBySimilarity(String name, def queryFp, double threshold) {
        List hits = []
        datasets[name].model.each {
            def targetFp = it.fp
            double similarity = Similarity.getTanimoto(queryFp, targetFp)
            if (similarity < threshold) {
                hits << it
            }
        }
        return hits
    }
    
    String fetchData(String datasetName, String filterId) {
        def model = datasets[datasetName]
        def results
        if (filterId) {
            results = model.filters[filterId]
        } else {
            results = model.data
        }
        return beansToJson(results)
    }
    
    FilterResult filterByFirstN(String datasetName, int n) {
        List hits = []
        def model = datasets[datasetName]
        def data = model.data
        (0..(n-1)).each {
            hits << data[it]
        }
        return generateFilter(hits, model)
    }
    
    private FilterResult generateFilter(List hits, Model model) {
        String uuid = UUID.randomUUID().toString()
        model.filters[uuid] = hits
        return new FilterResult(uuid, hits.size())
    }
    
        private String beansToJson(def obj) {
            return JsonOutput.toJson(obj)
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
    
    List fetchStructuresForList(int listId) {
        println "  retrieving data for list $listId"
            
        List result = []
        db.eachRow("""\
                    SELECT cd_id, cd_structure
                      FROM chemcentral_01.structures s
                      JOIN users.hit_list_data d ON d.id_item = s.cd_id
                      JOIN users.hit_lists l ON l.id = d.hit_list_id
                      WHERE l.id = ?""", [listId]) { row ->
            
            result << buildRowData(row)
        }
        return result
    }

    
    int queryForStructuresAsList(String username, String listname, def query /*ignored for now */) {
        int listId = generateHitList(username, listname)
        
        println "  executing query against structures"
        db.executeInsert("""\
                    INSERT INTO users.hit_list_data (hit_list_id, id_item)
                    (SELECT ?, cd_id
                      FROM chemcentral_01.structures
                      WHERE frag_count = 1 LIMIT 1000)""", [listId]) 
        println "  query complete"
        return listId 
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

    class Model {
        def data
        Map <String, Set> filters = [:]
    
        Model(def data) {
            this.data = data
        }
    }

    class FilterResult {
        String uuid
        int count
        FilterResult(String uuid, int count) {
            this.uuid = uuid
            this.count = count
        }
    }
}
