package roart.pipeline.common.predictor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.iclij.config.IclijConfig;
import roart.common.ml.NeuralNetCommand;
import roart.common.model.StockItem;
import roart.common.pipeline.PipelineConstants;
import roart.common.pipeline.data.PipelineData;
import roart.common.pipeline.model.PipelineResultData;
import roart.result.model.ResultItemTableRow;
import roart.result.model.ResultMeta;

public abstract class AbstractPredictor extends PipelineResultData {

    protected static Logger log = LoggerFactory.getLogger(AbstractPredictor.class);

    protected String title;
    protected IclijConfig conf;
    protected int category;
    protected Map<String, Object[]> resultMap;
    protected Map<String, Object> accuracyMap;
    protected Map<String, Object> lossMap;
    protected List<Object[]> resultMetaArray;
    private List<ResultMeta> resultMetas;

    protected NeuralNetCommand neuralnetcommand;
   
    public AbstractPredictor(IclijConfig conf, String string, int category, NeuralNetCommand neuralnetcommand) {
        this.title = string;
        this.conf = conf;
        this.category = category;
        this.neuralnetcommand = neuralnetcommand;
        resultMetas = new ArrayList<>();
    }

    public abstract boolean isEnabled();

    public List<ResultMeta> getResultMetas() {
        return resultMetas;
    }

    public void setResultMetas(List<ResultMeta> resultMetas) {
        this.resultMetas = resultMetas;
    }

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
    public PipelineData putData() {
        PipelineData map = getData();
        map.setName(getName());
        map.put(PipelineConstants.CATEGORY, category);
        map.put(PipelineConstants.CATEGORYTITLE, title);
        map.put(PipelineConstants.RESULT, resultMap);
        map.put(PipelineConstants.RESULTMETA, resultMetas);
        map.put(PipelineConstants.RESULTMETAARRAY, resultMetaArray);
        map.put(PipelineConstants.ACCURACY, accuracyMap);
        map.put(PipelineConstants.LOSS, lossMap);
        return map;
    }

    public abstract String predictorName();

    public abstract void calculate() throws Exception;

    public abstract boolean hasValue();

    public abstract String getName();
    
}

