package roart.aggregate;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.config.MyMyConfig;
import roart.indicator.Indicator;
import roart.model.ResultItemTableRow;
import roart.model.StockItem;

public abstract class Aggregator {

    protected static Logger log = LoggerFactory.getLogger(Indicator.class);

    protected String title;
    protected MyMyConfig conf;
    protected int category;
    
    public Aggregator(MyMyConfig conf, String string, int category) {
        this.title = string;
        this.conf = conf;
        this.category = category;
    }

    abstract public boolean isEnabled();

    public Object[] getResultItemTitle() {
        Object[] titleArray = new Object[1];
        titleArray[0] = "Agg"+title;
        return titleArray;
    }

    abstract public Object[] getResultItem(StockItem stock);

    public Object calculate(double[] array) {
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

    public abstract void addResultItem(ResultItemTableRow row, StockItem stock);

    public abstract void addResultItemTitle(ResultItemTableRow headrow);

    public Map<String, Object> getResultMap() {
        // TODO Auto-generated method stub
        return null;
    } 

    public String getTitle() {
        return title;
    }
    
    public abstract String getName();
    //public abstract void addResultItemTitle(ResultItemTableRow headrow);
}
