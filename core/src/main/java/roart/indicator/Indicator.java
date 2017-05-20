package roart.indicator;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.config.MyConfig;
import roart.model.ResultItemTable;
import roart.model.ResultItemTableRow;
import roart.model.StockItem;

public abstract class Indicator {

    protected static Logger log = LoggerFactory.getLogger(Indicator.class);

    protected String title;
    protected MyConfig conf;
    protected int category;
    
    public Indicator(MyConfig conf, String string, int category) {
        this.title = string;
        this.conf = conf;
        this.category = category;
    }

    abstract public boolean isEnabled();

    public Object[] getResultItemTitle() {
    	Object[] titleArray = new Object[1];
    	titleArray[0] = title;
        return titleArray;
    }

    abstract public Object[] getResultItem(StockItem stock);

    public Object calculate(Object as) {
        return null;
    }

    public List<Integer> getTypeList() {
        return null;
    }

    public Map<Integer, String> getMapTypes() {
        return null;
    }

    public Map<Integer, List<ResultItemTableRow>> otherTables() {
        return null;
    }

}

