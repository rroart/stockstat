package roart.indicator;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.iclij.config.IclijConfig;
import roart.common.constants.Constants;
import roart.common.inmemory.model.Inmemory;
import roart.common.model.StockDTO;
import roart.common.pipeline.PipelineConstants;
import roart.common.pipeline.data.PipelineData;
import roart.common.pipeline.data.SerialPipeline;
import roart.common.pipeline.data.SerialMap;
import roart.common.pipeline.data.SerialMapD;
import roart.common.pipeline.data.SerialMapTA;
import roart.common.pipeline.data.SerialTA;
import roart.common.pipeline.util.PipelineUtils;
import roart.common.util.MathUtil;
import roart.pipeline.common.Calculatable;
import roart.result.model.ResultItemTableRow;

public abstract class AbstractIndicator extends Calculatable {

    protected static Logger log = LoggerFactory.getLogger(AbstractIndicator.class);

    protected String title;
    protected IclijConfig conf;
    protected String key;
    public int fieldSize = 0;
    protected Object[] emptyField;

    protected SerialPipeline datareader;
    /*
    protected Map<String, Double[][]> listMap;
    protected Map<String, Double[][]> fillListMap;

    protected Map<String, double[][]> truncListMap;
    protected Map<String, double[][]> truncFillListMap;
    
    protected Map<String, Double[][]> base100ListMap;
    protected Map<String, Double[][]> base100FillListMap;
    
    protected Map<String, double[][]> truncBase100ListMap;
    protected Map<String, double[][]> truncBase100FillListMap;
    */
    // save and return this map
    // need getters for this and not? buy/sell
    protected Map<String, SerialTA> objectMap;
    protected Map<String, Object[]> objectFixedMap;
    protected Map<String, Double[]> calculatedMap;
    protected Map<String, Object[]> resultMap;

    protected Map<String, SerialMap> marketObjectMap;
    protected Map<String, Map<String, Object[]>> marketResultMap;
    protected Map<String, Map<String, Double[]>> marketCalculatedMap;

    protected SerialMap resultSMap = new SerialMap();

    protected Inmemory inmemory;

    public AbstractIndicator(IclijConfig conf, String string, int category, Inmemory inmemory) {
        this.title = string;
        this.conf = conf;
        this.category = category;
        this.inmemory = inmemory;
    }

    public abstract boolean isEnabled();
    protected abstract Double[] getCalculated(Map<String, SerialTA> objectMap, String id);
    protected abstract void getFieldResult(Double[] momentum, Object[] fields);
    public abstract int getInputArrays();

    public abstract String getName();

    public Object[] getResultItemTitle() {
        Object[] titleArray = new Object[1];
        titleArray[0] = title;
        return titleArray;
    }

    @Override
    public SerialTA calculate(double[][] array) {
        return null;
    }

    @Override
    public SerialTA calculate(Double[][] array) {
        double[][] newArray = new double[array.length][];
        for (int i = 0; i < array.length; i ++) {
            newArray[i] = ArrayUtils.toPrimitive(array[i]);
        }
        return calculate(newArray);
    }

    @Override
    public SerialTA calculate(scala.collection.Seq[] objArray) {
        double[][] newArray = new double[objArray.length][];
        for (int i = 0; i < objArray.length; i++) {
            List list = scala.collection.JavaConverters.seqAsJavaList(objArray[0]);
            Double[] array = new Double[list.size()];
            array = (Double[]) list.toArray(array);
            newArray[i] = ArrayUtils.toPrimitive(array);
        }
        return calculate(newArray);
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

    public Object[] getDayResult(SerialTA objsIndicator, int offset) {
        return null;
    }

    @Override
    public SerialPipeline putData() {
        SerialPipeline list = getData();
        //map.setName(indicatorName());
        // the mixed and complex results of indicator
        // an array with numbers or arrays
        list.add(new PipelineData(indicatorName(), PipelineConstants.OBJECT, null, new SerialMapTA(objectMap != null ? objectMap : new HashMap<>()), true));
        // TODO unused
        //list.add(new PipelineData(indicatorName(), PipelineConstants.OBJECTFIXED, null, objectFixedMap));
        //list.add(new PipelineData()(PipelineConstants.LIST, listMap);
        //list.add(new PipelineData()(PipelineConstants.TRUNCLIST, truncListMap);
        
        // for web
        list.add(new PipelineData(indicatorName(), PipelineConstants.RESULT, null, new SerialMapD(calculatedMap), true));
        
        // market as key, for extras
        // raw calculations
        list.add(new PipelineData(indicatorName(), PipelineConstants.MARKETOBJECT, null, new SerialMap(marketObjectMap), true));
        // prep for web?
        // TODO unused?
        //list.add(new PipelineData()(PipelineConstants.MARKETCALCULATED, marketCalculatedMap);
        // result for web table
        // TODO unused?
        //map.put(PipelineConstants.MARKETRESULT, marketResultMap);
        //map.smap().put(PipelineConstants.RESULT, resultSMap);
        return list;
    }

    public int getResultSize() {
        return 0;
    }

    public String indicatorName() {
        return null;
    }

    public boolean wantForExtras() {
        return false;        
    }


    public boolean anythingHereA(Map<String, double[][]> listMap) {
        if (listMap == null) {
            return false;
        }
        for (double[][] array : listMap.values()) {
            if (array[0].length > 0) {
                return true;
            }
        }
        return false;
    }

    public boolean anythingHere(Map<String, Double[][]> listMap2) {
        if (listMap2 == null) {
            return false;
        }
        for (Double[][] array : listMap2.values()) {
            for (int i = 0; i < array[0].length; i++) {
                if (array[0][i] != null) {
                    return true;
                }
            }
        }
        return false;
    }

    protected boolean anythingHere3(Map<String, Double[][]> listMap2) {
        if (listMap2 == null) {
            return false;
        }
        for (Double[][] array : listMap2.values()) {
            if (array.length != Constants.OHLC) {
                return false;
            }
            out:
            for (int i = 0; i < array[0].length; i++) {
                for (int j = 0; j < array.length - 1; j++) {
                    if (array[j].length == 0 || array[j][i] == null) {
                        continue out;
                    }
                }
                return true;
            }
        }
        return false;
    }

    protected abstract int getAnythingHereRange();
    
    protected Map<String, Double[]> getCalculatedMap(Map<String, SerialTA> myObjectMap, Map<String, double[][]> truncListMap) {
        Map<String, Double[]> result = new HashMap<>();
        for (String id : truncListMap.keySet()) {
            Double[] calculated = getCalculated(myObjectMap, id);
            if (calculated != null) {
                result.put(id, calculated);
                // and continue?
            } else {
                log.debug("nothing for id {}", id);
            }
        }
        return result;
    }

    protected Map<String, Object[]> getResultMap(IclijConfig conf, Map<String, SerialTA> myObjectMap, Map<String, Double[]> momMap) {
        Map<String, Object[]> result = new HashMap<>();
        Map<String, Double[][]> listMap = null;
        if (datareader != null) {
            listMap = getListMap();
        }
        if (listMap == null) {
            return result;
        }
        for (String id : listMap.keySet()) {
            Double[] momentum = momMap.get(id);
            Object[] fields = new Object[fieldSize];
            result.put(id, fields);
            if (momentum == null) {
                log.debug("zero mom for id {}", id);
            }
            getFieldResult(momentum, fields);
        }
        return result;
    }

    public Object[] getResultItem(StockDTO stock) {
        String market = conf.getConfigData().getMarket();
        String id = stock.getId();
        Pair<String, String> pair = new ImmutablePair<>(market, id);
        Set<Pair<String, String>> ids = new HashSet<>();
        ids.add(pair);
        if (resultMap == null) {
            return emptyField;
        }
        Object[] result = resultMap.get(id);
        if (result == null) {
            result = emptyField;
        }
        result = MathUtil.round(result, 3);
        return result;
    }

    // TODO?
    protected Map<String, Double[][]> getListMap() {
        return PipelineUtils.sconvertMapDD(PipelineUtils.getPipelineValue(datareader, key, PipelineConstants.LIST, inmemory));
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
    
}

