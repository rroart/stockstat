package roart.pipeline.common.predictor;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.iclij.config.IclijConfig;
import roart.common.inmemory.model.Inmemory;
import roart.common.ml.NeuralNetCommand;
import roart.common.model.StockDTO;
import roart.common.pipeline.PipelineConstants;
import roart.common.pipeline.data.PipelineData;
import roart.common.pipeline.data.SerialPipeline;
import roart.common.pipeline.data.SerialInteger;
import roart.common.pipeline.data.SerialList;
import roart.common.pipeline.data.SerialMap;
import roart.common.pipeline.data.SerialMapPlain;
import roart.common.pipeline.data.SerialString;
import roart.common.pipeline.model.PipelineResultData;
import roart.result.model.ResultItemTableRow;

public abstract class AbstractPredictor extends PipelineResultData {

    protected static Logger log = LoggerFactory.getLogger(AbstractPredictor.class);

    protected String title;
    protected IclijConfig conf;
    protected int category;
    protected Map<String, Object[]> resultMap;
    protected Map<String, Object> accuracyMap;
    protected Map<String, Object> lossMap;
    //protected List<Object[]> resultMetaArray;
    protected SerialMap resultSMap = new SerialMap();
    private SerialList resultMetas = new SerialList();

    protected NeuralNetCommand neuralnetcommand;

    protected Inmemory inmemory;
   
    public AbstractPredictor(IclijConfig conf, String string, int category, NeuralNetCommand neuralnetcommand, Inmemory inmemory) {
        this.title = string;
        this.conf = conf;
        this.category = category;
        this.neuralnetcommand = neuralnetcommand;
        this.inmemory = inmemory;
    }

    public abstract boolean isEnabled();

    public SerialList getResultMetas() {
        return resultMetas;
    }

    public void setResultMetas(SerialList resultMetas) {
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

    public abstract Object[] getResultItem(StockDTO stock);

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
    public SerialPipeline putData() {
        SerialPipeline list = new SerialPipeline();
        //map.setName(getName());
        list.add(new PipelineData(getName(), PipelineConstants.CATEGORY, null, new SerialInteger(category), false));
        list.add(new PipelineData(getName(), PipelineConstants.CATEGORYTITLE, null, new SerialString(title), false));
        list.add(new PipelineData(getName(), PipelineConstants.RESULT, null, new SerialMapPlain(resultMap), true));
        list.add(new PipelineData(getName(), PipelineConstants.RESULTMETA, null, resultMetas, false));
        // TODO remove
        //list.add(new PipelineData(getName(), PipelineConstants.RESULTMETAARRAY, resultMetaArray);
        list.add(new PipelineData(getName(), PipelineConstants.ACCURACY, null, new SerialMapPlain(accuracyMap), false));
        // TODO unused
        //list.add(new PipelineData(getName(), PipelineConstants.LOSS, lossMap);
        //map.smap().put(PipelineConstants.RESULT, resultSMap);
        return list;
    }

    public abstract String predictorName();

    public abstract void calculate() throws Exception;

    public abstract boolean hasValue();

    public abstract String getName();
    
}

