package roart.pipeline.common.aggregate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.common.config.MyMyConfig;
import roart.common.pipeline.PipelineConstants;
import roart.common.pipeline.model.PipelineResultData;
import roart.model.StockItem;
import roart.pipeline.Pipeline;
import roart.result.model.ResultItemTableRow;
import roart.result.model.ResultMeta;

public abstract class Aggregator extends PipelineResultData {

    protected static Logger log = LoggerFactory.getLogger(Aggregator.class);

    protected String title;
    protected MyMyConfig conf;
    protected int category;
    
    protected Map<String, Object[]> objectMap;
    protected Map<String, Object[]> objectFixedMap;
    protected Map<String, Double[]> calculatedMap;
    protected Map<String, Object> accuracyMap;
    protected Map<String, Object> lossMap;
    protected Map<String, Object[]> otherResultMap;
    protected Map<String, Object[]> resultMap;
    protected List<Object[]> otherMeta;
    protected List<Object[]> resultMetaArray;
    private List<ResultMeta> resultMetas;

    public Aggregator(MyMyConfig conf, String string, int category) {
        this.title = string;
        this.conf = conf;
        this.category = category;
        resultMetas = new ArrayList<>();
    }

    public List<ResultMeta> getResultMetas() {
        return resultMetas;
    }

    public void setResultMetas(List<ResultMeta> resultMetas) {
        this.resultMetas = resultMetas;
    }

    public abstract boolean isEnabled();

    public Object[] getResultItemTitle() {
        Object[] titleArray = new Object[1];
        titleArray[0] = "Agg"+title;
        return titleArray;
    }

    public abstract Object[] getResultItem(StockItem stock);

    public Object calculate(double[][] array) {
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
        return null;
    } 

    public String getTitle() {
        return title;
    }
    
    public abstract String getName();

    public Map<String, Object> getLocalResultMap() {
        Map<String, Object> map = new HashMap<>();
        map.put(PipelineConstants.CATEGORY, category);
        map.put(PipelineConstants.CATEGORYTITLE, title);
        map.put(PipelineConstants.RESULT, resultMap);
        map.put(PipelineConstants.OTHERRESULT, otherResultMap);
        map.put(PipelineConstants.RESULTMETA, resultMetas);
        map.put(PipelineConstants.RESULTMETAARRAY, resultMetaArray);
        map.put(PipelineConstants.PROBABILITY, accuracyMap);
        map.put(PipelineConstants.OBJECT, objectMap);
        map.put(PipelineConstants.OBJECTFIXED, objectFixedMap);
        return map;
    }
    
}
