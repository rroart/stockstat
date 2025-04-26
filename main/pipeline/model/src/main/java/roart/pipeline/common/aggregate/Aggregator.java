package roart.pipeline.common.aggregate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.iclij.config.IclijConfig;
import roart.common.inmemory.model.Inmemory;
import roart.common.model.StockDTO;
import roart.common.pipeline.PipelineConstants;
import roart.common.pipeline.data.PipelineData;
import roart.common.pipeline.data.SerialList;
import roart.common.pipeline.data.SerialMap;
import roart.common.pipeline.data.SerialMapPlain;
import roart.common.pipeline.model.PipelineResultData;
import roart.common.util.MathUtil;
import roart.pipeline.Pipeline;
import roart.result.model.ResultItemTableRow;
import roart.result.model.ResultMeta;

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
    
    public abstract String getName();

    public PipelineData putData() {
        PipelineData map = getData();
        map.setName(getName());
        map.put(PipelineConstants.CATEGORY, category);
        map.put(PipelineConstants.CATEGORYTITLE, title);
        // mix of number and string
        map.put(PipelineConstants.RESULT, new SerialMapPlain(resultMap));
        // TODO unused
        //map.put(PipelineConstants.OTHERRESULT, otherResultMap);
        map.put(PipelineConstants.RESULTMETA, resultMetas);
        // TODO remove
        //map.put(PipelineConstants.RESULTMETAARRAY, resultMetaArray);
        map.put(PipelineConstants.ACCURACY, new SerialMapPlain(accuracyMap));
        // TODO unused
        //map.put(PipelineConstants.LOSS, lossMap);
        // TODO unused?
        //map.put(PipelineConstants.OBJECT, objectMap);
        // TODO unused
        //map.put(PipelineConstants.OBJECTFIXED, objectFixedMap);
        //map.smap().put(PipelineConstants.RESULT, resultSMap);
        return map;
    }
    
    protected Object[] round(Object[] objs, int places) {
        return MathUtil.round3(objs, places);
    }

}
