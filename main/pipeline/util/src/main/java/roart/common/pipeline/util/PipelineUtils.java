package roart.common.pipeline.util;

import java.util.*;
import java.util.Map.Entry;

import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.common.pipeline.data.*;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import org.apache.curator.framework.CuratorFramework;

import roart.common.constants.Constants;
import roart.common.pipeline.PipelineConstants;
import roart.common.util.JsonUtil;
import roart.common.inmemory.model.Inmemory;
import roart.common.inmemory.model.InmemoryMessage;

public class PipelineUtils {
    private static Logger log = LoggerFactory.getLogger(PipelineUtils.class);
    
    private static final ObjectMapper mapper = JsonMapper.builder().build();

    /*
    public static Map<String, PipelineData> getPipelineMap(SerialPipeline datareaders) {
        Map<String, PipelineData> pipelineMap = new HashMap<>();
        for (PipelineData datareader : datareaders) {
            pipelineMap.put(datareader.getName(), datareader);
        }
        return pipelineMap;
    }
    */

    public static Set<String> getPipelineMapFirstKeys(SerialPipeline datareaders) {
        Set<String> pipelineKeys = new HashSet<>();
        for (PipelineData datareader : datareaders) {
            pipelineKeys.add(datareader.getKey().getFirst());
        }
        return pipelineKeys;
    }

    public static Set<SerialPipelineKey> getPipelineMapKeys(SerialPipeline datareaders) {
        Set<SerialPipelineKey> pipelineKeys = new HashSet<>();
        for (PipelineData datareader : datareaders) {
            pipelineKeys.add(datareader.getKey());
        }
        return pipelineKeys;
    }

/*
    @Deprecated
    public static Map<String, PipelineData> getPipelineMapStartsWith(SerialPipeline datareaders, String startsWith) {
        Map<String, PipelineData> pipelineMap = new HashMap<>();
        for (PipelineData datareader : datareaders) {
            if (datareader.getName().startsWith(startsWith)) {
                pipelineMap.put(datareader.getName(), datareader);
            }
        }
        return pipelineMap;
    }

    public static PipelineData getPipeline(SerialPipeline datareaders, String name) {
        for (PipelineData datareader : datareaders) {
            if (name == null) {
                int jj = 0;
            }
            if (name.equals(datareader.getName())) {
                return datareader;
            }
        }
        return null;
    }

    public static PipelineData getPipeline(SerialPipeline datareaders, String name, Inmemory inmemory) {
        if (name == null) {
            int jj = 0;
        }
        for (int i = 0; i < datareaders.length; i++) {
            PipelineData datareader = datareaders[i];
            if (name.equals(datareader.getName())) {
                if (datareader.isLoaded()) {
                    return datareader;
                } else {
                    InmemoryMessage msg = JsonUtil.convertnostrip(datareader.getMessage(), InmemoryMessage.class);                    
                    String str = inmemory.read(msg);
                    if (str != null) {
                    log.info("Pipeline reading {}", str.length());
                    } else {
                        log.error("No pipeline reading {}", msg.getId());
                    }
                    datareaders[i] = JsonUtil.convertnostrip(str, PipelineData.class, mapper);
                    datareaders[i].setLoaded(true);
                    log.info("Pipeline read {} {}", datareaders[i].getId(), datareaders[i].getName());
                    return datareaders[i];
                }
            }
        }
        return null;
    }

     */

    public static SerialPipeline getPipelines(SerialPipeline pipelines, String key, Inmemory inmemory) {
        return getPipelines(pipelines, new SerialPipelineKey(new String[] { key, null, null, null, null }), inmemory);
    }

    public static SerialPipeline getPipelines(SerialPipeline pipelines, String key, String secondKey, Inmemory inmemory) {
        return getPipelines(pipelines, new SerialPipelineKey(new String[] { key, secondKey, null, null, null }), inmemory);
    }

    public static SerialPipeline getPipelines(SerialPipeline pipelines, String key, String secondKey, String thirdKey, Inmemory inmemory) {
        return getPipelines(pipelines, new SerialPipelineKey(new String[] { key, secondKey, thirdKey, null, null }), inmemory);
    }

    public static SerialPipeline getPipelines(SerialPipeline pipelines, String key, String secondKey, String thirdKey, String fourthKey, Inmemory inmemory) {
        return getPipelines(pipelines, new SerialPipelineKey(new String[] { key, secondKey, thirdKey, fourthKey, null }), inmemory);
    }

    public static SerialPipeline getPipelines(SerialPipeline pipelines, String key, String secondKey, String thirdKey, String fourthKey, String fifthKey, Inmemory inmemory) {
        return getPipelines(pipelines, new SerialPipelineKey(new String[] { key, secondKey, thirdKey, fourthKey, fifthKey }), inmemory);
    }

    public static SerialPipeline getPipelines(SerialPipeline pipelines, SerialPipelineKey key, Inmemory inmemory) {
        SerialPipeline newPipelines = new SerialPipeline();
        for (PipelineData pipeline : pipelines) {
            if (key.matches(pipeline.getKey())) {
                newPipelines.add(pipeline);
            }
        }
        return newPipelines;
    }

    public static SerialPipeline getPipelinesRest(SerialPipeline pipelines, String key, Inmemory inmemory) {
        SerialPipeline newPipelines = getPipelines(pipelines, new SerialPipelineKey(new String[] { key, null, null, null, null }), inmemory);
        SerialPipeline newNewPipelinex = new SerialPipeline();
        for (PipelineData pipeline : newPipelines) {
            PipelineData newPipeline = pipeline.clone();
            newPipeline.restKey();
            newNewPipelinex.add(newPipeline);
        }
        return newNewPipelinex;
    }

    public static PipelineData getPipeline(SerialPipeline pipelines, String key, String secondKey, String thirdKey, Inmemory inmemory) {
        return getPipeline(pipelines, new SerialPipelineKey(new String[] { key, secondKey, thirdKey, null, null}), inmemory);
    }

    public static PipelineData getPipeline(SerialPipeline pipelines, String key, String secondKey, Inmemory inmemory) {
        return getPipeline(pipelines, new SerialPipelineKey(new String[] { key, secondKey, null, null, null}), inmemory);
    }

    public static PipelineData getPipeline(SerialPipeline pipelines, String key, Inmemory inmemory) {
        return getPipeline(pipelines, new SerialPipelineKey(new String[] { key, null, null, null, null}), inmemory);
    }

    public static PipelineData getPipeline(SerialPipeline pipelines, SerialPipelineKey key, Inmemory inmemory) {
        SerialPipeline list = getPipelines(pipelines, key, inmemory);
        if (list.length() == 1) {
            return list.iterator().next();
        } else {
            if (list.isEmpty()) {
                log.error("No key {}", key.toString());
            } else {
                log.error("Duplicate key {}", key.toString());
                try {
                    String s = null;
                    s.length();
                } catch (Exception e) {
                    log.error("Exception", e);
                }
            }
        }
        return null;
    }

    public static SerialObject getPipelineValue(SerialPipeline pipelines, String key, Inmemory inmemory) {
        return getPipelineValue(pipelines, new SerialPipelineKey(new String[] { key, null, null, null, null}), inmemory);
    }

    public static SerialObject getPipelineValue(SerialPipeline pipelines, String key, String secondKey, Inmemory inmemory) {
        return getPipelineValue(pipelines, new SerialPipelineKey(new String[] { key, secondKey, null, null, null}), inmemory);
    }

    public static SerialObject getPipelineValue(SerialPipeline pipelines, String key, String secondKey, String thirdKey, Inmemory inmemory) {
        return getPipelineValue(pipelines, new SerialPipelineKey(new String[] { key, secondKey, thirdKey, null, null}), inmemory);
    }

    public static SerialObject getPipelineValue(SerialPipeline pipelines, String key, String secondKey, String thirdKey, String fourthKey, Inmemory inmemory) {
        return getPipelineValue(pipelines, new SerialPipelineKey(new String[] { key, secondKey, thirdKey, fourthKey, null}), inmemory);
    }

    /*
    public static SerialObject getPipelineValueOld(SerialPipeline pipelines, String name, String key, String secondKey, Inmemory inmemory) {
        for (PipelineData pipeline : pipelines) {
            if (pipeline.getName().equals(name)) {
                if (key == null || key.equals(pipeline.getKey())) {
                   if (secondKey == null || secondKey.equals(pipeline.getSecondKey())) {
                       return pipeline.getValue();
                   }
                }
            }
        }
        return null;
    }

     */

    public static SerialObject getPipelineValueOld(SerialPipeline pipelines, SerialPipelineKey key, Inmemory inmemory) {
        SerialPipeline list = getPipelines(pipelines, key, inmemory);
        if (list.length() == 1) {
            PipelineData pipeline = list.iterator().next();
            getPipelineValue(pipeline, inmemory);
            return pipeline.getValue();
        } else {
            if (list.isEmpty()) {
                log.error("No key {}", key.toString());
            } else {
                log.error("Duplicate key {}", key.toString());
            }
        }
        return null;
    }

    public static SerialObject getPipelineValue(SerialPipeline pipelines, SerialPipelineKey key, Inmemory inmemory) {
        log.info("Pipe len" + pipelines.length());
        PipelineData pipeline = getPipeline(pipelines, key);
        if (pipeline != null) {
            getPipelineValue(pipeline, inmemory);
            return pipeline.getValue();
        } else {
            log.error("No key {}", key.toString());
            log.error("Keys {}", getPipelineMapKeys(pipelines));
            try {
                String s = null;
                s.length();
            } catch (Exception e) {
                log.error(Constants.EXCEPTION, e);
            }
            return null;
        }
    }

    public static PipelineData getPipeline(SerialPipeline pipelines, SerialPipelineKey key) {
        SerialPipeline list = new SerialPipeline();
        for (PipelineData pipeline : pipelines) {
            if (key.equals(pipeline.getKey())) {
                list.add(pipeline);
            }
        }
        if (list.length() == 1) {
            PipelineData pipeline = list.iterator().next();
            return pipeline;
        } else {
            if (list.isEmpty()) {
                log.error("No key {}", key.toString());
                log.error("Keys {}", getPipelineMapKeys(pipelines));
                try {
                    String s = null;
                    s.length();
                } catch (Exception e) {
                    log.error(Constants.EXCEPTION, e);
                }
            } else {
                log.error("Duplicate key {}", key.toString());
            }
        }
        return null;
    }

    public static void getPipelineValue(PipelineData datareader, Inmemory inmemory) {
        if (!datareader.isLoaded()) {
            InmemoryMessage msg = JsonUtil.convertnostrip(datareader.getMessage(), InmemoryMessage.class);
            String str = inmemory.read(msg);
            if (str != null) {
                log.info("Pipeline reading {}", str.length());
            } else {
                log.error("No pipeline reading {}", msg.getId());
            }
            datareader.setValue(JsonUtil.convertnostrip(str, SerialObject.class, mapper));
            datareader.setLoaded(true);
            log.info("Pipeline read {} {} {}", datareader.getId(), datareader, str.length());
        }
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
    
    public static SerialMap sconvertdd(Map<String, double[][]> map) {
        SerialMap newMap = new SerialMap();
        if (map == null) {
            return newMap;
        }
        for (Entry<String, double[][]> entry : map.entrySet()) {
            newMap.put(entry.getKey(), new TwoDimd(entry.getValue()));
        }
        return newMap;
    }

    public static SerialMap sconvertDD(Map<String, Double[][]> map) {
        SerialMap newMap = new SerialMap();
        if (map == null) {
            return newMap;
        }
        for (Entry<String, Double[][]> entry : map.entrySet()) {
            newMap.put(entry.getKey(), new TwoDimD(entry.getValue()));
        }
        return newMap;
    }
    
    public static SerialMapPlain sconvertplaindd(Map<String, double[][]> map) {
        return new SerialMapPlain(map);
    }

    public static SerialMapPlain sconvertplainDD(Map<String, Double[][]> map) {
        return new SerialMapPlain(map);
    }
    
    public static Map<String, Double[][]> sconvertTwoDimD(Object object) {
        SerialMap serialmap = (SerialMap) object;
        Map map = serialmap.getMap();
        return convertTwoDimD(map);
    }

    public static Map<String, Double[][]> sconvertTwoDimd(Object object) {
        SerialMap serialmap = (SerialMap) object;
        Map map = serialmap.getMap();
        return convertTwoDimd(map);
    }

    public static Map<String, Double[][]> sconvertMapDD(Object object) {
        SerialMapDD serialmap = (SerialMapDD) object;
        return serialmap.getMap();
    }

    public static Map<String, double[][]> sconvertMapdd(Object object) {
        SerialMapdd serialmap = (SerialMapdd) object;
        return serialmap.getMap();
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

    /*
    public static void fixPipeline(SerialPipeline pipelineData, Class marketStockClass, Class stockDataClass) {
        // TODO remaining volumemap, namemap, meta, datelist, title
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
                    Map<String, SerialPipeline> value = transformListObject((Map) entry.getValue(), SerialPipeline.class);
                    for (Entry<String, SerialPipeline> anEntry : value.entrySet()) {
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

     */

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

    /*
    public static void printmap(SerialPipeline data) {
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

    public static void printkeys(SerialPipeline data) {
        log.info("Printkeys");
        for (PipelineData datum : data) {
            //Set<String> keys = datum.keySet();
            //log.info("Data {} {}", datum.getName(), keys);
            log.info("Data {} Y: {} N: {}", datum.getName(), datum.getUsedKeys(), datum.getUnusedKeys());
        }
    }

     */

    public static void printkeys(SerialPipeline data) {
        printmap(data);
    }

    public static void printmap(SerialPipeline data) {
        log.info("Printkeys");
        for (PipelineData datum : data) {
            //Set<String> keys = datum.keySet();
            //log.info("Data {} {}", datum.getName(), keys);
            log.info("Data {}", ArrayUtils.toString(datum.getKey()));
        }
    }

    // unused?
    public static Map<String, Object[][]> getVolumeMap(PipelineData data) {
        return null;
    }
    
    public static List<String> getDatelist(SerialPipeline data, String name, Inmemory inmemory) {
        SerialObject object = getPipelineValue(data, name, PipelineConstants.DATELIST, inmemory);
        SerialListPlain list = (SerialListPlain) object;
        if (list != null) {
            return list.getList();
        }
        return null;
    }
    
    public static Map getNamemap(SerialPipeline data, String name, Inmemory inmemory) {
        SerialObject object = getPipelineValue(data, name, PipelineConstants.NAME, inmemory);
        SerialMapPlain list = (SerialMapPlain) object;
        if (list != null) {
            return list.getMap();
        }
        return null;
    }

    public static Integer getWantedcat(SerialPipeline data, Inmemory inmemory) {
        SerialObject object = getPipelineValue(data, PipelineConstants.META, PipelineConstants.WANTEDCAT, inmemory);
        SerialInteger list = (SerialInteger) object;
        if (list != null) {
            return list.getInteger();
        }
        return null;
    }

    public static String getMetaCat(SerialPipeline data, Inmemory inmemory) {
        SerialObject object = getPipelineValue(data, PipelineConstants.META, PipelineConstants.CATEGORY, inmemory);
        SerialString list = (SerialString) object;
        if (list != null) {
            return list.getString();
        }
        return null;
    }

    public static SerialMeta getMeta(SerialPipeline data, String name, Inmemory inmemory) {
        SerialObject object = getPipelineValue(data, name, PipelineConstants.META, inmemory);
        return (SerialMeta) object;
    }

    public static Integer getCat(SerialPipeline data, String name, Inmemory inmemory) {
        SerialObject object = getPipelineValue(data, name, PipelineConstants.CATEGORY, inmemory);
        SerialInteger list = (SerialInteger) object;
        if (list != null) {
            return list.getInteger();
        }
        return null;
    }

    public static Map<String, Long[]> getVolume(SerialPipeline data, String name, Inmemory inmemory) {
        SerialObject object = getPipelineValue(data, name, PipelineConstants.VOLUME, inmemory);
        SerialMapL list = (SerialMapL) object;
        if (list != null) {
            return list.getMap();
        }
        return null;
    }

    public static Map getCurrency(SerialPipeline data, String name, Inmemory inmemory) {
        SerialObject object = getPipelineValue(data, name, PipelineConstants.CURRENCY, inmemory);
        SerialMapPlain list = (SerialMapPlain) object;
        if (list != null) {
            return list.getMap();
        }
        return null;
    }

    public static String getCatTitle(SerialPipeline data, String name, Inmemory inmemory) {
        SerialObject object = getPipelineValue(data, name, PipelineConstants.CATEGORYTITLE, inmemory);
        SerialString string = (SerialString) object;
        if (string != null) {
            return string.getString();
        }
        return null;
    }

    public static List<SerialResultMeta> getResultMeta(SerialPipeline data, Inmemory inmemory) {
        SerialObject object = getPipelineValue(data, PipelineConstants.RESULTMETA, inmemory);
        SerialList<SerialResultMeta> list = (SerialList<SerialResultMeta>) object;
        if (list != null) {
            return list.getList();
        }
        return null;
    }

    public static SerialMapTA getMapTA(SerialPipeline data, String name, Inmemory inmemory) {
        SerialObject object = getPipelineValue(data, name, PipelineConstants.OBJECT, inmemory);
        SerialMapTA objectMap = (SerialMapTA) object;
        if (objectMap != null) {
            return objectMap;
        }
        return null;
    }

    /*
    // TODO not quite working?
    public static Map<String, SerialTA> getObjectMap(PipelineData data) {
        SerialMapTA objectMap = (SerialMapTA) data.get(PipelineConstants.OBJECT);
        if (objectMap != null) {
            return objectMap.getMap();
        }
        return null;
    }

     */

    public static Map<String, SerialTA> getObjectMap(SerialPipeline data, String name, Inmemory inmemory) {
        SerialObject object = getPipelineValue(data, name, PipelineConstants.OBJECT, inmemory);
        SerialMapTA objectMap = (SerialMapTA) object;
        if (objectMap != null) {
            return objectMap.getMap();
        }
        return null;
    }

    public static Map getMarketObjectMap(SerialPipeline data, String name, Inmemory inmemory) {
        // TODO if this is serialized
        SerialObject object = getPipelineValue(data, name, PipelineConstants.MARKETOBJECT, inmemory);
        SerialMap map = (SerialMap) object;
        if (map != null) {
            return map.getMap();
        }
        return null;
    }

    public static SerialMapD getResultMap(SerialPipeline data, String name, Inmemory inmemory) {
        SerialObject object = getPipelineValue(data, name, PipelineConstants.RESULT, inmemory);
        return (SerialMapD) object;
    }
/*
    public static Map getDatareader(PipelineData data) {
        SerialMap map = (SerialMap) data.get(PipelineConstants.DATAREADER);
        return map.getMap();
    }


 */
    public static List getMarketstocks(SerialPipeline data, String name, Inmemory inmemory) {
        SerialObject object = getPipelineValue(data, name, PipelineConstants.MARKETSTOCKS, inmemory);
        SerialList map = (SerialList) object;
        return map.getList();
    }

    public static Map getAccuracyMap(SerialPipeline data, String name, Inmemory inmemory) {
        SerialObject object = getPipelineValue(data, name, PipelineConstants.ACCURACY, inmemory);
        SerialMapPlain map = (SerialMapPlain) object;
        return map.getMap();
    }

    public static String getString(SerialPipeline data, String key, String secondKey, String thirdKey, Inmemory inmemory) {
        SerialObject object = getPipelineValue(data, new SerialPipelineKey(new String[] { key, secondKey, thirdKey, null, null }), inmemory);
        SerialString map = (SerialString) object;
        if (map != null) {
            return map.getString();
        }
        return null;
    }

    public static Double getDouble(SerialPipeline data, String name, String key, String secondKey, Inmemory inmemory) {
        SerialObject object = getPipelineValue(data, name, key, secondKey, inmemory);
        SerialDouble map = (SerialDouble) object;
        if (map != null) {
            return map.getAdouble();
        }
        return null;
    }

    public static Integer getInteger(SerialPipeline data, String name, String key, String secondKey, Inmemory inmemory) {
        SerialObject object = getPipelineValue(data, name, key, secondKey, inmemory);
        SerialInteger map = (SerialInteger) object;
        if (map != null) {
            return map.getInteger();
        }
        return null;
    }

    public static List<Object> getList(SerialPipeline data, String name, String key, String secondKey, Inmemory inmemory) {
        SerialObject object = getPipelineValue(data, name, key, secondKey, inmemory);
        SerialList map = (SerialList) object;
        if (map != null) {
            return map.getList();
        }
        return null;
    }

    public static List getListPlain(SerialPipeline data, String name, String key, String secondKey, Inmemory inmemory) {
        SerialObject object = getPipelineValue(data, name, key, secondKey, inmemory);
        SerialListPlain map = (SerialListPlain) object;
        if (map != null) {
            return map.getList();
        }
        return null;
    }

    public static List<SerialKeyValue> getListMap(SerialPipeline data, String name, String key, String secondKey, Inmemory inmemory) {
        SerialObject object = getPipelineValue(data, name, key, secondKey, inmemory);
        SerialListMap map = (SerialListMap) object;
        if (map != null) {
            return map.getMap();
        }
        return null;
    }

    public static Map<String, Object> getListMapAsMap(SerialPipeline data, String name, String key, String secondKey, Inmemory inmemory) {
        SerialObject object = getPipelineValue(data, name, key, secondKey, inmemory);
        SerialListMap list = (SerialListMap) object;
        Map<String, Object> map = new HashMap<>();
        for (SerialKeyValue entry : list.getMap()) {
            SerialObject value = entry.getValue();
            Object avalue = value;
            if (value instanceof SerialString s) {
                avalue = s.getString();
            }
            if (value instanceof SerialDouble d) {
                avalue = d.getAdouble();
            }
            if (value instanceof SerialInteger i) {
                avalue = i.getInteger();
            }
            map.put(entry.getKey(), avalue);
        }
        return map;
    }

    public static Map<String, SerialObject> getSerialListMapAsMap(SerialPipeline data, SerialPipelineKey key, Inmemory inmemory) {
        SerialObject object = getPipelineValue(data, key, inmemory);
        SerialListMap list = (SerialListMap) object;
        Map<String, SerialObject> map = new HashMap<>();
        for (SerialKeyValue entry : list.getMap()) {
            SerialObject avalue = entry.getValue();
            map.put(entry.getKey(), avalue);
        }
        return map;
    }

    public static Map<String, Object> getMapPlain(SerialPipeline data, String name, String key, String secondKey, Inmemory inmemory) {
        return getSerialMapPlain(data, name, key, secondKey, inmemory);
    }

    public static Map<String, Object> getSerialMapPlain(SerialPipeline data, String name, String key, String secondKey, Inmemory inmemory) {
        SerialObject object = getPipelineValue(data, name, key, secondKey, inmemory);
        SerialMapPlain map = (SerialMapPlain) object;
        if (map != null) {
            return map.getMap();
        }
        return null;
    }

    public static Map<String, Object> getSerialMapPlain(SerialPipeline data, SerialPipelineKey key, Inmemory inmemory) {
        SerialObject object = getPipelineValue(data, key, inmemory);
        SerialMapPlain map = (SerialMapPlain) object;
        if (map != null) {
            return map.getMap();
        }
        return null;
    }

    public static List<SerialKeyValue> getSerialListMap(SerialPipeline data, String name, String key, String secondKey, Inmemory inmemory) {
        SerialObject object = getPipelineValue(data, name, key, secondKey, inmemory);
        SerialListMap map = (SerialListMap) object;
        if (map != null) {
            return map.getMap();
        }
        return null;
    }

    /*
    public static Map getMap(PipelineData data, String key) {
        SerialMap list = (SerialMap) data.get(key);
        return list.getMap();
    }

     */
    
    public static void setPipelineMap(SerialPipeline datareaders, String id) {
        for (PipelineData datareader : datareaders) {
            datareader.setId(id);
        }
    }

    public static void setPipelineMap(SerialPipeline datareaders, boolean old) {
        for (PipelineData datareader : datareaders) {
            datareader.setOld(old);
        }
    }

    public static SerialPipeline setPipelineMap(SerialPipeline pipelineData, Inmemory inmemory, CuratorFramework curatorClient) {
        if (false) {
            try {
                String s = null;
                s.length();
            } catch (Exception e) {
                log.error(Constants.EXCEPTION, e);
            }
        }
        SerialPipeline newPipelineData = new SerialPipeline();
        for (PipelineData data : pipelineData) {
            if (!data.isOld()) {
                InmemoryMessage msg = null;
                try {
                    data.setOld(true);
                    if (!data.isUseInmemory()) {
                        log.info("Pipe not " + data + JsonUtil.convert(data.getValue()).length());
                        continue;
                    }
                    //PipelineData d = JsonUtil.convertAndBack(data, null);
                    //String s = JsonUtil.convert(data);
                    String md5 = null;
                    String serviceIdUuid = data.getId();
                    String[] split = serviceIdUuid.split("/");
                    String serviceId = split[0];
                    String id = split[1];
                    log.info("Pipe " + data + " " + " " + serviceId + " " + id);
                    //   todo
                    msg = inmemory.send(id + "-" + ArrayUtils.toString(data.getKey()), data.getValue(), md5);
                    log.info("Sent size {} {} {}", msg.getId(), msg.getCount(), JsonUtil.convert(data.getValue(), mapper).length());
                    //result.message = msg;
                    curatorClient.create().creatingParentsIfNeeded().forPath("/" + Constants.STOCKSTAT + "/" + Constants.PIPELINE + "/" + serviceId + "/" + id + "/" + msg.getId(), JsonUtil.convert(msg).getBytes());
                    log.info("Path write {}", "/" + Constants.STOCKSTAT + "/" + Constants.PIPELINE + "/" + serviceId + "/" + id + "/" + msg.getId());
                    data.setValue(null);
                    data.setMessage(JsonUtil.convert(msg));
                    data.setLoaded(false);
                } catch (Exception e) {
                    log.error(Constants.EXCEPTION, e);
                }
                /*
                PipelineData newDatum = new PipelineData();
                newDatum.setId(data.getId());
                newDatum.setName(data.getName());
                newDatum.setOld(true);
                newDatum.setLoaded(false);
                newDatum.setMessage(JsonUtil.convert(msg));
                newPipelineData.add(newDatum);
                log.info("Pipeline write {} {}", newDatum.getId(), newDatum.getName());

                 */
            } else {
                //newPipelineData.add(data);
            }
            //newPipelineData.add(data);
        }
        return pipelineData;
    }
}
