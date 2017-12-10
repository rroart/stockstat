package roart.model;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

// not working in either places
//@JsonIgnoreProperties(ignoreUnknown=true)
@JsonIgnoreProperties({ "arr" })
public class ResultItemTable extends ResultItem {
	//@JsonProperty("rows")
	public List<ResultItemTableRow> rows = new ArrayList();
    
    public void add(ResultItemTableRow row) {
    	rows.add(row);
    }
    
    public int size() {
    	return rows.size();
    }
    
    public ResultItemTableRow get(int i) {
    	return rows.get(i);
    }
}
