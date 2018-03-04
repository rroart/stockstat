package roart.predictor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.config.MyMyConfig;
import roart.model.ResultItemTableRow;
import roart.model.StockItem;
import roart.pipeline.PipelineConstants;

public abstract class Predictor {

    protected static Logger log = LoggerFactory.getLogger(Predictor.class);

    protected String title;
    protected MyMyConfig conf;
    protected int category;
    protected Map<String, Object[]> resultMap;
   
    public Predictor(MyMyConfig conf, String string, int category) {
        this.title = string;
        this.conf = conf;
        this.category = category;
    }

    public abstract boolean isEnabled();

    public Object[] getResultItemTitle() {
    	Object[] titleArray = new Object[1];
    	titleArray[0] = title;
        return titleArray;
    }

    public abstract Object[] getResultItem(StockItem stock);

    public Object calculate(Double[] array) {
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

    public Map<String, Object> getResultMap() {
        return null;
    }

    public Map<String, Object> getLocalResultMap() {
        Map<String, Object> map = new HashMap<>();
        map.put(PipelineConstants.CATEGORY, category);
        map.put(PipelineConstants.CATEGORYTITLE, title);
        map.put(PipelineConstants.RESULT, resultMap);
        return map;
    }

    public abstract String predictorName();
    
}

