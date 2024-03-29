package roart.common.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.common.constants.Constants;
import roart.common.pipeline.PipelineConstants;
import roart.common.pipeline.data.MapOneDim;
import roart.common.pipeline.data.OneDim;
import roart.common.pipeline.data.OneDimD;
import roart.common.pipeline.data.OneDimd;
import roart.common.pipeline.data.PipelineData;
import roart.common.pipeline.data.TwoDimD;
import roart.common.pipeline.data.TwoDimd;

public class PipelineUtils {
    private static Logger log = LoggerFactory.getLogger(PipelineUtils.class);
    
    public static Map<String, PipelineData> getPipelineMap(PipelineData[] datareaders) {
        Map<String, PipelineData> pipelineMap = new HashMap<>();
        for (PipelineData datareader : datareaders) {
            pipelineMap.put(datareader.getName(), datareader);
        }
        return pipelineMap;
    }

    public static Map<String, PipelineData> getPipelineMapStartsWith(PipelineData[] datareaders, String startsWith) {
        Map<String, PipelineData> pipelineMap = new HashMap<>();
        for (PipelineData datareader : datareaders) {
            if (datareader.getName().startsWith(startsWith)) {
                pipelineMap.put(datareader.getName(), datareader);
            }
        }
        return pipelineMap;
    }

    public static PipelineData getPipeline(PipelineData[] datareaders, String name) {
        for (PipelineData datareader : datareaders) {
            if (name.equals(datareader.getName())) {
                return datareader;
            }
        }
        return null;
    }

    public static Map<String, OneDimd> convertd(Map<String, double[]> map) {
        Map<String, OneDimd> newMap = new HashMap<>();
        if (map == null) {
            return newMap;
        }
        for (Entry<String, double[]> entry : map.entrySet()) {
            newMap.put(entry.getKey(), new OneDimd(entry.getValue()));
        }
        return newMap;
    }

    public static Map<String, OneDimD> convertD(Map<String, Double[]> map) {
        Map<String, OneDimD> newMap = new HashMap<>();
        if (map == null) {
            return newMap;
        }
        for (Entry<String, Double[]> entry : map.entrySet()) {
            newMap.put(entry.getKey(), new OneDimD(entry.getValue()));
        }
        return newMap;
    }

    public static Map<String, TwoDimd> convertdd(Map<String, double[][]> map) {
        Map<String, TwoDimd> newMap = new HashMap<>();
        if (map == null) {
            return newMap;
        }
        for (Entry<String, double[][]> entry : map.entrySet()) {
            newMap.put(entry.getKey(), new TwoDimd(entry.getValue()));
        }
        return newMap;
    }

    public static Map<String, TwoDimD> convertDD(Map<String, Double[][]> map) {
        Map<String, TwoDimD> newMap = new HashMap<>();
        if (map == null) {
            return newMap;
        }
        for (Entry<String, Double[][]> entry : map.entrySet()) {
            newMap.put(entry.getKey(), new TwoDimD(entry.getValue()));
        }
        return newMap;
    }
    
    public static Map<String, double[]> convertOneDimd(Map<String, OneDimd> map) {
        Map<String, double[]> newMap = new HashMap<>();
        if (map == null) {
            return newMap;
        }
        for (Entry<String, OneDimd> entry : map.entrySet()) {
            newMap.put(entry.getKey(), entry.getValue().getArray());
        }
        return newMap;
    }

    public static Map<String, Double[]> convertOneDimD(Map<String, OneDimD> map) {
        Map<String, Double[]> newMap = new HashMap<>();
        if (map == null) {
            return newMap;
        }
        for (Entry<String, OneDimD> entry : map.entrySet()) {
            newMap.put(entry.getKey(), entry.getValue().getArray());
        }
        return newMap;
    }

    public static Map<String, double[][]> convertTwoDimd(Map<String, TwoDimd> map) {
        Map<String, double[][]> newMap = new HashMap<>();
        if (map == null) {
            return newMap;
        }
        for (Entry<String, TwoDimd> entry : map.entrySet()) {
            newMap.put(entry.getKey(), entry.getValue().getArray());
        }
        return newMap;
    }

    public static Map<String, Double[][]> convertTwoDimD(Map<String, TwoDimD> map) {
        Map<String, Double[][]> newMap = new HashMap<>();
        if (map == null) {
            return newMap;
        }
        for (Entry<String, TwoDimD> entry : map.entrySet()) {
            newMap.put(entry.getKey(), entry.getValue().getArray());
        }
        return newMap;
    }
    
    private static final List<String> strings = List.of(PipelineConstants.MLINDICATOR, PipelineConstants.MLATR, PipelineConstants.MLCCI, PipelineConstants.MLMACD, PipelineConstants.MLMULTI, PipelineConstants.MLRSI, PipelineConstants.MLSTOCH);

    private static final List<String> othermap = List.of(PipelineConstants.MARKETOBJECT);
    
    private static final List<String> other = List.of(PipelineConstants.OBJECT);
    
    private static final List<String> onedim = List.of(PipelineConstants.RESULT);

    private static final List<String> twodimD = List.of(PipelineConstants.LIST, PipelineConstants.FILLLIST, PipelineConstants.BASE100LIST, PipelineConstants.BASE100FILLLIST);
    
    private static final List<String> twodimd = List.of(PipelineConstants.TRUNCLIST, PipelineConstants.TRUNCFILLLIST, PipelineConstants.TRUNCBASE100LIST, PipelineConstants.TRUNCBASE100FILLLIST);
    // , PipelineConstants.MARKETOBJECT
    
    public static void fixPipeline(PipelineData[] pipelineData, Class marketStockClass, Class stockDataClass) {
        for (PipelineData data : pipelineData) {
            for (Entry<String, Object> entry : data.getMap().entrySet()) {
                if (PipelineConstants.VOLUME.equals(entry.getKey())) {
                    //continue;
                }
                if (PipelineConstants.MARKETSTOCKS.equals(entry.getKey())) {
                    Object value = transformListObject((List) entry.getValue(), marketStockClass);
                    data.getMap().put(entry.getKey(), value);
                    continue;
                }
                if (PipelineConstants.DATAREADER.equals(entry.getKey())) {
                    Map<String, PipelineData[]> value = transformListObject((Map) entry.getValue(), PipelineData[].class);
                    for (Entry<String, PipelineData[]> anEntry : value.entrySet()) {
                        fixPipeline(anEntry.getValue(), marketStockClass, stockDataClass);
                    }
                    data.getMap().put(entry.getKey(), value);
                    continue;   
                }
                Object value = entry.getValue();
                if (value instanceof Map map2) {
                    Map<String, Object> map = map2;
                    Map newMap = new HashMap<>();
                    for (Entry mapEntry : map.entrySet()) {
                        try {
                        Object newData = null;
                        if (twodimD.contains(entry.getKey())) {
                            newData = JsonUtil.convert(mapEntry.getValue(), TwoDimD.class);
                        }
                        if (twodimd.contains(entry.getKey())) {
                            newData = JsonUtil.convert(mapEntry.getValue(), TwoDimd.class);
                        }
                        if (onedim.contains(entry.getKey())) {
                            if (!strings.contains(data.getName())) {
                            newData = transformList(mapEntry.getValue());
                            } else {
                                newData = transformList2(mapEntry.getValue());
                            }
                        }
                        if (other.contains(entry.getKey())) {
                            newData = transformListObject(mapEntry.getValue());
                        }
                        if (othermap.contains(entry.getKey())) {
                            newData = transformMap(mapEntry.getValue());
                        }
                        if (newData != null) {
                            newMap.put(mapEntry.getKey(), newData);
                        }
                        } catch (Exception e) {
                            log.error(Constants.EXCEPTION, e);
                            log.info("key {}", mapEntry.getKey());
                            log.info("key {}", mapEntry.getValue().getClass().getName());
                            log.info("key {}", mapEntry.getValue());
                            log.info("key {}", mapEntry.getValue());

                        }
                    }
                    map.putAll(newMap);
                }
            }
        }
    }



    private static Object transformMap(Object value) {
        if (value instanceof Map map2) {
            Map<String, Object> map = map2;
            Map newMap = new HashMap<>();
            for (Entry<String, Object> mapEntry : map.entrySet()) {
                newMap.put(mapEntry.getKey(), transformListObject(mapEntry.getValue()));
            }
            return newMap;
        } else {
            return value;
        }
    }

    private static <T> List<T> transformListObject(List list, Class<T> clazz) {
        List<T> newList = new ArrayList<>();
        for (Object object : list) {
            newList.add(JsonUtil.convert(object, clazz));
        }
        return newList;
    }

    private static <V> Map<String, V> transformListObject(Map<String, Object> map, Class<V> clazz) {
        Map<String, V> newMap = new HashMap<>();
        for (Entry<String, Object> entry : map.entrySet()) {
            newMap.put(entry.getKey(), JsonUtil.convert(entry.getValue(), clazz));
        }
        return newMap;
    }

    private static Object transform(Object data) {
        if (data instanceof List list) {
            return list.stream().map(e -> transform(e)).toArray();
        }
        return data;
    }

    private static Object transformListList(Object data) {
        if (data instanceof List list) {
            for (Object object : list) {
                if (!(object instanceof List)) {
                    return data;
                }
            }
            return ArraysUtil.convert((List<List<Double>>) data);
        }
        return data;
    }

    private static Object transformList(Object data) {
        if (data instanceof List list) {
            List l = (List) data;
            for (Object o : l) {
                //Log.info("ob" + o + " " + o.getClass().getName());
            }
            return ArraysUtil.convert1((List<Double>) data);
        }
        return data;
    }

    private static Object transformList2(Object data) {
        if (data instanceof List list) {
            List l = (List) data;
            for (Object o : l) {
                //Log.info("ob" + o + " " + o.getClass().getName());
            }
            return l.toArray(new Object[0]);
        }
        return data;
    }

    private static Object transformListObject(Object data) {
        if (data instanceof List list) {
            List l = (List) data;
            for (Object o : l) {
                //Log.info("ob" + o + " " + o.getClass().getName());
            }
            return ArraysUtil.convert2((List) data);
        }
        return data;
    }

    public static MapOneDim getMapOneDim(Object object) {
        Map<String, Object> map = (Map<String, Object>) object;
        Map<String, OneDim> newMap = new HashMap<>();
        for (Entry<String, Object> entry : map.entrySet()) {
            Object value = entry.getValue();
            Object[] array;
            if (value instanceof Object[] arr) {
                array = arr;
            } else {
                array = ((List) value).toArray(new Object[0]);
            }
            newMap.put(entry.getKey(), new OneDim(array));
        }
        return new MapOneDim(newMap );
    }

    public static void printmap(Object o, int i) {
        if (o == null) {
            return;
        }
        //System.out.println("" + i + " " + o.hashCode());
        Map<String, Object> m = (Map<String, Object>) o;
        for (Entry<String, Object> e : m.entrySet()) {
            Object value = e.getValue();
            if (value instanceof Map) {
                log.debug("{} {} {}", i, e.getKey(), value.hashCode());
                printmap((Map<String, Object>) value, i + 1);
            } else {
                if (value == null) {
                    log.debug("Kv {} {} {}", i, e.getKey(), null);
                    //System.out.println(" v " + null);
                }
            }
        }
    }

    public static void printmap(PipelineData[] data) {
        long total = 0;
        for (PipelineData datum : data) {
            Set<String> keys = datum.keySet();
            log.info("Data {} {}", datum.getName(), keys);
            if (log.isDebugEnabled()) {
                for (String key : keys) {
                    String str = JsonUtil.convert(datum.get(key));
                    long size = str != null ? str.length() : 0;
                    total += size;
                    log.debug("Size {} {}", key, size);
                }
            }
        }
        log.info("Total Size {}", total);
    }

}
