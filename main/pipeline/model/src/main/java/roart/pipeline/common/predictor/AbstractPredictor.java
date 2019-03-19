package roart.pipeline.common.predictor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.common.config.MyMyConfig;
import roart.common.pipeline.PipelineConstants;
import roart.common.pipeline.model.PipelineResultData;
import roart.model.StockItem;
import roart.result.model.ResultItemTableRow;

public abstract class AbstractPredictor extends PipelineResultData {

    protected static Logger log = LoggerFactory.getLogger(AbstractPredictor.class);

    protected String title;
    protected MyMyConfig conf;
    protected int category;
    protected Map<String, Object[]> resultMap;
    protected Map<String, Object> probabilityMap;
   
    public AbstractPredictor(MyMyConfig conf, String string, int category) {
        this.title = string;
        this.conf = conf;
        this.category = category;
    }

    public abstract boolean isEnabled();

    public String getTitle() {
        return title;
    }
    
    public int getCategory() {
        return category;
    }
    
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

    @Override
    public Map<String, Object> getLocalResultMap() {
        Map<String, Object> map = new HashMap<>();
        map.put(PipelineConstants.CATEGORY, category);
        map.put(PipelineConstants.CATEGORYTITLE, title);
        map.put(PipelineConstants.RESULT, resultMap);
        map.put(PipelineConstants.PROBABILITY, probabilityMap);
        return map;
    }

    public abstract String predictorName();

    public abstract void calculate() throws Exception;

    public abstract boolean hasValue();
    
}

