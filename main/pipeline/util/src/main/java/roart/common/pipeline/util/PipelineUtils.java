package roart.common.pipeline.util;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.datatype.jsr310.JavaTimeModule;

import org.apache.curator.framework.CuratorFramework;

import roart.common.constants.Constants;
import roart.common.pipeline.PipelineConstants;
import roart.common.pipeline.data.MapOneDim;
import roart.common.pipeline.data.OneDim;
import roart.common.pipeline.data.OneDimD;
import roart.common.pipeline.data.OneDimd;
import roart.common.pipeline.data.PipelineData;
import roart.common.pipeline.data.SerialInteger;
import roart.common.pipeline.data.SerialKeyValue;
import roart.common.pipeline.data.SerialList;
import roart.common.pipeline.data.SerialListMap;
import roart.common.pipeline.data.SerialListPlain;
import roart.common.pipeline.data.SerialMap;
import roart.common.pipeline.data.SerialMapD;
import roart.common.pipeline.data.SerialMapDD;
import roart.common.pipeline.data.SerialMapL;
import roart.common.pipeline.data.SerialMapPlain;
import roart.common.pipeline.data.SerialMapTA;
import roart.common.pipeline.data.SerialMapVolume;
import roart.common.pipeline.data.SerialMapdd;
import roart.common.pipeline.data.SerialMeta;
import roart.common.pipeline.data.SerialObject;
import roart.common.pipeline.data.SerialResultMeta;
import roart.common.pipeline.data.SerialDouble;
import roart.common.pipeline.data.SerialString;
import roart.common.pipeline.data.SerialTA;
import roart.common.pipeline.data.SerialVolume;
import roart.common.pipeline.data.TwoDimD;
import roart.common.pipeline.data.TwoDimd;
import roart.common.util.ArraysUtil;
import roart.common.util.JsonUtil;
import roart.common.inmemory.model.Inmemory;
import roart.common.inmemory.model.InmemoryMessage;

public class PipelineUtils {
    private static Logger log = LoggerFactory.getLogger(PipelineUtils.class);
    
    private static final ObjectMapper mapper = JsonMapper.builder().build();

    public static Map<String, PipelineData> getPipelineMap(PipelineData[] datareaders) {
        Map<String, PipelineData> pipelineMap = new HashMap<>();
        for (PipelineData datareader : datareaders) {
            pipelineMap.put(datareader.getName(), datareader);
        }
        return pipelineMap;
    }

    public static Set<String> getPipelineMapKeys(PipelineData[] datareaders) {
        Set<String> pipelineKeys = new HashSet<>();
        for (PipelineData datareader : datareaders) {
            pipelineKeys.add(datareader.getName());
        }
        return pipelineKeys;
    }

    @Deprecated
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
            if (name == null) {
                int jj = 0;
            }
            if (name.equals(datareader.getName())) {
                return datareader;
            }
        }
        return null;
    }

    public static PipelineData getPipeline(PipelineData[] datareaders, String name, Inmemory inmemory) {
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
    
    public static void fixPipeline(PipelineData[] pipelineData, Class marketStockClass, Class stockDataClass) {
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
    
    public static void printkeys(PipelineData[] data) {
        log.info("Printkeys");
        for (PipelineData datum : data) {
            //Set<String> keys = datum.keySet();
            //log.info("Data {} {}", datum.getName(), keys);
            log.info("Data {} Y: {} N: {}", datum.getName(), datum.getUsedKeys(), datum.getUnusedKeys());
        }
    }
    
    // unused?
    public static Map<String, Object[][]> getVolumeMap(PipelineData data) {
        return null;
    }
    
    public static List<String> getDatelist(PipelineData data) {
        SerialListPlain list = (SerialListPlain) data.get(PipelineConstants.DATELIST);
        if (list != null) {
            return list.getList();
        }
        return null;
    }
    
    public static Map getNamemap(PipelineData data) {
        SerialMapPlain list = (SerialMapPlain) data.get(PipelineConstants.NAME);
        if (list != null) {
            return list.getMap();
        }
        return null;
    }

    public static Integer getWantedcat(PipelineData data) {
        SerialInteger list = (SerialInteger) data.get(PipelineConstants.WANTEDCAT);
        if (list != null) {
            return list.getInteger();
        }
        return null;
    }

    public static String getMetaCat(PipelineData data) {
        SerialString list = (SerialString) data.get(PipelineConstants.CATEGORY);
        if (list != null) {
            return list.getString();
        }
        return null;
    }

    public static SerialMeta getMeta(PipelineData data) {
        return (SerialMeta) data.get(PipelineConstants.META);
    }

    public static Integer getCat(PipelineData data) {
        SerialInteger list = (SerialInteger) data.get(PipelineConstants.CATEGORY);
        if (list != null) {
            return list.getInteger();
        }
        return null;
    }

    public static Map<String, Long[]> getVolume(PipelineData data) {
        SerialMapL list = (SerialMapL) data.get(PipelineConstants.VOLUME);
        if (list != null) {
            return list.getMap();
        }
        return null;
    }

    public static Map getCurrency(PipelineData data) {
        SerialMapPlain list = (SerialMapPlain) data.get(PipelineConstants.CURRENCY);
        if (list != null) {
            return list.getMap();
        }
        return null;
    }

    public static String getCatTitle(PipelineData data) {
        SerialString string = (SerialString) data.get(PipelineConstants.CATEGORYTITLE);
        if (string != null) {
            return string.getString();
        }
        return null;
    }

    public static List<SerialResultMeta> getResultMeta(PipelineData data) {
        SerialList<SerialResultMeta> list = (SerialList<SerialResultMeta>) data.get(PipelineConstants.RESULTMETA);
        if (list != null) {
            return list.getList();
        }
        return null;
    }

    public static SerialMapTA getMapTA(PipelineData data) {
        return (SerialMapTA) data.get(PipelineConstants.OBJECT);
    }

    public static Map<String, SerialTA> getObjectMap(PipelineData data) {
        SerialMapTA objectMap = (SerialMapTA) data.get(PipelineConstants.OBJECT);
        if (objectMap != null) {
            return objectMap.getMap();
        }
        return null;
    }
    
    public static Map getMarketObjectMap(PipelineData data) {
        // TODO if this is serialized
        SerialMap map = (SerialMap) data.get(PipelineConstants.MARKETOBJECT);
        if (map != null) {
            return map.getMap();
        }
        return null;
    }
    
    public static SerialMapD getResultMap(PipelineData data) {
        return (SerialMapD) data.get(PipelineConstants.RESULT);
    }

    public static Map getDatareader(PipelineData data) {
        SerialMap map = (SerialMap) data.get(PipelineConstants.DATAREADER);
        return map.getMap();
    }

    public static List getMarketstocks(PipelineData data) {
        SerialList map = (SerialList) data.get(PipelineConstants.MARKETSTOCKS);
        return map.getList();
    }

    public static Map getAccuracyMap(PipelineData data) {
        SerialMapPlain map = (SerialMapPlain) data.get(PipelineConstants.ACCURACY);
        return map.getMap();
    }

    public static String getString(PipelineData data, String key) {
        SerialString string = (SerialString) data.get(key);
        return string.getString();
    }

    public static Double getDouble(PipelineData data, String key) {
        SerialDouble adouble = (SerialDouble) data.get(key);
        return adouble.getAdouble();
    }

    public static List getList(PipelineData data, String key) {
        SerialList list = (SerialList) data.get(key);
        return list.getList();
    }

    public static List getListPlain(PipelineData data, String key) {
        SerialListPlain list = (SerialListPlain) data.get(key);
        return list.getList();
    }

    public static Map getMap(PipelineData data, String key) {
        SerialMap list = (SerialMap) data.get(key);
        return list.getMap();
    }

    public static List<SerialKeyValue> getListMap(PipelineData data, String key) {
        SerialListMap list = (SerialListMap) data.get(key);
        return list.getMap();
    }

    public static Map<String, Object> getListMapAsMap(PipelineData data, String key) {
        Map<String, Object> map = new HashMap<>();
        SerialListMap list = (SerialListMap) data.get(key);
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

    public static Map getMapPlain(PipelineData data, String key) {
        SerialMapPlain list = (SerialMapPlain) data.get(key);
        return list.getMap();
    }
    
    public static void setPipelineMap(PipelineData[] datareaders, String id) {
        for (PipelineData datareader : datareaders) {
            datareader.setId(id);
        }
    }

    public static void setPipelineMap(PipelineData[] datareaders, boolean old) {
        for (PipelineData datareader : datareaders) {
            datareader.setOld(old);
        }
    }

    public static PipelineData[] setPipelineMap(PipelineData[] pipelineData, Inmemory inmemory, CuratorFramework curatorClient) {
        if (false) {
            try {
                String s = null;
                s.length();
            } catch (Exception e) {
                log.error(Constants.EXCEPTION, e);
            }
        }
        PipelineData[] newPipelineData = new PipelineData[pipelineData.length];
        int i = 0;
        for (PipelineData data : pipelineData) {
            if (!data.isOld()) {
                InmemoryMessage msg = null;
                try {
                    data.setOld(true);
                    //PipelineData d = JsonUtil.convertAndBack(data, null);
                    //String s = JsonUtil.convert(data);
                    String md5 = null;
                    String serviceIdUuid = data.getId();
                    String[] split = serviceIdUuid.split("/");
                    String serviceId = split[0];
                    String id = split[1];
                    msg = inmemory.send(id + "-" + data.getName(), data, md5);
                    log.info("Sent size {} {} {}", msg.getId(), msg.getCount(), JsonUtil.convert(data, mapper).length());
                    //result.message = msg;
                    curatorClient.create().creatingParentsIfNeeded().forPath("/" + Constants.STOCKSTAT + "/" + Constants.PIPELINE + "/" + serviceId + "/" + id + "/" + msg.getId(), JsonUtil.convert(msg).getBytes());
                    log.info("Path write {}", "/" + Constants.STOCKSTAT + "/" + Constants.PIPELINE + "/" + serviceId + "/" + id + "/" + msg.getId());
                } catch (Exception e) {
                    log.error(Constants.EXCEPTION, e);
                }
                PipelineData newDatum = new PipelineData();
                newDatum.setId(data.getId());
                newDatum.setName(data.getName());
                newDatum.setOld(true);
                newDatum.setLoaded(false);
                newDatum.setMessage(JsonUtil.convert(msg));
                newPipelineData[i++] = newDatum;
                log.info("Pipeline write {} {}", newDatum.getId(), newDatum.getName());
            } else {
                newPipelineData[i++] = data;
            }
        }
        return newPipelineData;
    }
}
