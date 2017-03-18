package roart.model;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

//@JsonIgnoreProperties(ignoreUnknown = true)
@JsonIgnoreProperties({ "arr" })
public class ResultItemTableRow {
    public List<Object> cols = new ArrayList();
    
    public void add(Object obj) {
    	cols.add(obj);
    }
    
    public Object get(int i) {
    	return cols.get(i);
    }
    
    //@JsonProperty("grr")
    public List<Object> get() {
    	return cols;
    }
    
    public Object[] getarr() {
    	return cols.toArray();
    }

}
