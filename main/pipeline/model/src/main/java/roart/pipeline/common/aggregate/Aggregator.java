package roart.pipeline.common.aggregate;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.common.ml.NeuralNetCommand;
import roart.common.pipeline.data.*;
import roart.iclij.config.IclijConfig;
import roart.common.inmemory.model.Inmemory;
import roart.common.model.StockDTO;
import roart.common.pipeline.PipelineConstants;
import roart.common.pipeline.model.PipelineResultData;
import roart.common.util.MathUtil;
import roart.result.model.ResultItemTableRow;

public abstract class Aggregator extends PipelineResultData {

    protected static Logger log = LoggerFactory.getLogger(Aggregator.class);

    protected String title;
    protected IclijConfig conf;
    protected int category;
    
    protected Map<String, Object[]> objectMap;
    protected Map<String, Object[]> objectFixedMap;
    protected Map<String, Double[]> calculatedMap;
    protected Map<String, Object> accuracyMap;
    protected Map<String, Object> lossMap;
    protected Map<String, Object[]> otherResultMap;
    protected Map<String, Object[]> resultMap;
    protected SerialMap resultSMap = new SerialMap();
    @Deprecated // ?
    protected List<Object[]> otherMeta;
    //protected List<Object[]> resultMetaArray;
    private SerialList resultMetas = new SerialList();

    protected Inmemory inmemory;

    public Aggregator(IclijConfig conf, String string, int category, Inmemory inmemory) {
        this.title = string;
        this.conf = conf;
        this.category = category;
        this.inmemory = inmemory;
    }

    public SerialList getResultMetas() {
        return resultMetas;
    }

    public void setResultMetas(SerialList resultMetas) {
        this.resultMetas = resultMetas;
    }

    public abstract boolean isEnabled();

    public Object[] getResultItemTitle() {
        Object[] titleArray = new Object[1];
        titleArray[0] = "Agg"+title;
        return titleArray;
    }

    public abstract Object[] getResultItem(StockDTO stock);

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

    public abstract void addResultItem(ResultItemTableRow row, StockDTO stock);

    public abstract void addResultItemTitle(ResultItemTableRow headrow);

    public Map<String, Object> getResultMap() {
        return null;
    } 

    public String getTitle() {
        return title;
    }
    
    public IclijConfig getConf() {
        return conf;
    }

    public abstract String getName();

    @Override
    public SerialPipeline putData() {
        SerialPipeline list = getData();
        //map.setName(getName());
        list.add(new PipelineData(getName(), PipelineConstants.CATEGORY, null, new SerialInteger(category), false));
        list.add(new PipelineData(getName(), PipelineConstants.CATEGORYTITLE, null, new SerialString(title), false));
        // mix of number and string
        list.add(new PipelineData(getName(), PipelineConstants.RESULT, null, new SerialMapPlain(resultMap), true));
        // TODO unused
        //list.add(new PipelineData(getName(), PipelineConstants.OTHERRESULT, otherResultMap);
        list.add(new PipelineData(getName(), PipelineConstants.RESULTMETA, null, resultMetas, true));
        // TODO remove
        //list.add(new PipelineData(getName(), PipelineConstants.RESULTMETAARRAY, resultMetaArray);
        list.add(new PipelineData(getName(), PipelineConstants.ACCURACY, null, new SerialMapPlain(accuracyMap), true));
        // TODO unused
        //list.add(new PipelineData(getName(), PipelineConstants.LOSS, lossMap);
        // TODO unused?
        //list.add(new PipelineData(getName(), PipelineConstants.OBJECT, objectMap);
        // TODO unused
        //list.add(new PipelineData(getName(), PipelineConstants.OBJECTFIXED, objectFixedMap);
        //map.smap().put(PipelineConstants.RESULT, resultSMap);
        return list;
    }
    
    protected Object[] round(Object[] objs, int places) {
        return MathUtil.round3(objs, places);
    }

    public abstract void calculateMe(IclijConfig conf,
                                     SerialPipeline datareaders, NeuralNetCommand neuralnetcommand) throws Exception;
    public abstract void cleanMLDaos();
}
