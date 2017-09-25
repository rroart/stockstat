package roart.aggregate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.config.MyMyConfig;
import roart.indicator.Indicator;
import roart.model.ResultItemTableRow;
import roart.model.StockItem;
import roart.pipeline.PipelineConstants;

public abstract class Aggregator {

    protected static Logger log = LoggerFactory.getLogger(Indicator.class);

    protected String title;
    protected MyMyConfig conf;
    protected int category;
    
    protected Map<String, Object[]> objectMap;
    protected Map<String, Object[]> objectFixedMap;
    //Map<String, Double> resultMap;
    protected Map<String, Double[]> calculatedMap;
    protected Map<String, Object> probabilityMap;
    protected Map<String, Object[]> resultMap;

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

    public Map<String, Object> getLocalResultMap() {
        Map<String, Object> map = new HashMap<>();
        map.put(PipelineConstants.RESULT, calculatedMap);
        map.put(PipelineConstants.PROBABILITY, probabilityMap);
        map.put(PipelineConstants.OBJECT, objectMap);
        map.put(PipelineConstants.OBJECTFIXED, objectFixedMap);
        return map;
    }
    
}
