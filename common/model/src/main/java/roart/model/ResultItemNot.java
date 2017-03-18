package roart.model;

import java.util.List;
import java.io.Serializable;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResultItemNot {

    private static final Logger log = LoggerFactory.getLogger(ResultItemNot.class);

    private List<Object> items = new ArrayList<Object>();
    public ResultItemNot() {
    }
    public ResultItemNot(String s) {
	add(s);
    }
    public void add(Object s) {
	items.add(s);
    }
    public List<Object> get() {
	return items;
    }
    public Object[] getarr() {
	Object[] strarr = new Object[items.size()];
	for(int i = 0; i < items.size(); i++) {
	    strarr[i] = items.get(i);
	}
	return strarr;
    }
}
