package roart.indicator;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.iclij.config.IclijConfig;
import roart.common.constants.Constants;
import roart.common.model.StockItem;
import roart.common.pipeline.PipelineConstants;
import roart.common.pipeline.data.PipelineData;
import roart.common.pipeline.data.SerialMap;
import roart.common.pipeline.data.SerialMapD;
import roart.common.pipeline.data.SerialMapPlain;
import roart.common.pipeline.data.SerialMapTA;
import roart.common.pipeline.data.SerialTA;
import roart.common.pipeline.data.TwoDimD;
import roart.common.util.MathUtil;
import roart.common.util.PipelineUtils;
import roart.model.data.MarketData;
import roart.model.data.PeriodData;
import roart.pipeline.Pipeline;
import roart.pipeline.common.Calculatable;
import roart.result.model.ResultItemTableRow;

public abstract class AbstractIndicator extends Calculatable {

    protected static Logger log = LoggerFactory.getLogger(AbstractIndicator.class);

    protected String title;
    protected IclijConfig conf;
    protected String key;
    public int fieldSize = 0;
    protected Object[] emptyField;

    protected PipelineData datareader = new PipelineData();
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

    protected Map<String, Map<String, SerialTA>> marketObjectMap;
    protected Map<String, Map<String, Object[]>> marketResultMap;
    protected Map<String, Map<String, Double[]>> marketCalculatedMap;

    protected SerialMap resultSMap = new SerialMap();

    public AbstractIndicator(IclijConfig conf, String string, int category) {
        this.title = string;
        this.conf = conf;
        this.category = category;
    }

    public abstract boolean isEnabled();
    protected abstract Double[] getCalculated(Map<String, SerialTA> objectMap, String id);
    protected abstract void getFieldResult(Double[] momentum, Object[] fields);

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

    public PipelineData putData() {
        PipelineData map = getData();
        map.setName(indicatorName());
        // the mixed and complex results of indicator
        // an array with numbers or arrays
        map.put(PipelineConstants.OBJECT, new SerialMapTA(objectMap != null ? objectMap : new HashMap<>()));
        // TODO unused
        map.put(PipelineConstants.OBJECTFIXED, objectFixedMap);
        //map.put(PipelineConstants.LIST, listMap);
        //map.put(PipelineConstants.TRUNCLIST, truncListMap);
        
        // for web
        map.put(PipelineConstants.RESULT, new SerialMapD(calculatedMap));
        
        // market as key, for extras
        // raw calculations
        map.put(PipelineConstants.MARKETOBJECT, new SerialMapPlain(marketObjectMap));
        // prep for web?
        // TODO unused?
        //map.put(PipelineConstants.MARKETCALCULATED, marketCalculatedMap);
        // result for web table
        // TODO unused?
        //map.put(PipelineConstants.MARKETRESULT, marketResultMap);
        //map.smap().put(PipelineConstants.RESULT, resultSMap);
        return map;
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

    public Object[] getResultItem(StockItem stock) {
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

    protected Map<String, Double[][]> getListMap() {
        return PipelineUtils.sconvertMapDD(datareader.get(PipelineConstants.LIST));
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
    
}

