package roart.indicator;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.config.MyMyConfig;
import roart.model.ResultItemTable;
import roart.model.ResultItemTableRow;
import roart.model.StockItem;

public abstract class Indicator {

    protected static Logger log = LoggerFactory.getLogger(Indicator.class);

    protected String title;
    protected MyMyConfig conf;
    protected int category;
    
    public Indicator(MyMyConfig conf, String string, int category) {
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

    public Object calculate(double[] array) {
        return null;
    }

    public Object calculate(Double[] array) {
        return calculate(ArrayUtils.toPrimitive(array));
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

    public Map<String, Object> getResultMap() {
         return null;
    }

    public Object[] getDayResult(Object[] objs, int offset) {
        return null;
    }

    public Map<String, Object> getLocalResultMap() {
        return null;
    }

    public int getResultSize() {
        return 0;
    }

    public String indicatorName() {
        return null;
    }

    public int getCategory() {
        return category;
    }
}

