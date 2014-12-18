package com.im.examples.model.types;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/** Complex type for structures.
 * Has an original representation (e.g. smiles or molfile) and can also contain 
 * alternative representations (such as Molecule instances, or standardized representations).
 * These alternative representations are not serialized or persisted allowing the 
 * Structure instance to be sent over remote connections.  
 *
 * @author timbo
 */
public class Structure implements Serializable {

    private final String original;

    private final transient Map representations = new HashMap();

    public Structure(String original) {
        this.original = original;
    }
    
    public String getOriginal() {
        return original;
    }
    
    public Object getRepresentation(Object key) {
       return representations.get(key);
    }
    
    public <T> T getRepresentation(Object key, Class<T> type) {
       return (T)representations.get(key);
    }
    
    public boolean hasRepresentation(Object key) {
       return representations.containsKey(key);
    }
    
    public Object putRepresentation(Object key, Object value) {
       return representations.put(key, value);
    }
}
