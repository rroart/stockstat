package roart.common.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

import roart.common.constants.Constants;
import roart.common.pipeline.data.MapOneDim;
import roart.common.pipeline.data.OneDim;
import roart.common.pipeline.data.SerialMap;
import roart.common.pipeline.data.SerialOneDim;
import roart.common.pipeline.data.SerialString;
import roart.common.util.ArraysUtil;

import static org.junit.jupiter.api.Assertions.*;

public class PipelineUtilsTest {
    private static Double[] array = { 1.0,-1.0 };
    
    //@Test
    public void test() {
        OneDim od = new OneDim(array);
        
        Map<String, OneDim> newMap = new HashMap<>();
        newMap.put("k", od);
        MapOneDim odm = new MapOneDim(newMap );
        Map<String, Object> map = new HashMap<>();
        map.put("k", odm);
        String json = JsonUtil.convert(odm);
        String json2 = JsonUtil.convert(map);
        Map<String, Object> map2 = JsonUtil.convert(json2, Map.class);
        MapOneDim map1 = JsonUtil.convert(json, MapOneDim.class);
        System.out.println("json" + json);
        System.out.println("json2"+ json2);
        System.out.println(map2);
        System.out.println(map2.get("k").getClass().getCanonicalName());
        System.out.println("map" + map1);
    }

    //@Test
    /*
    public void test2() {
        SerialMap map = new SerialMap();
        map.list = List.of(List.of(11, "blbl"));
        SerialMap map2 = new SerialMap();
        map2.list = List.of(List.of(1, map));
        Object o = (Object) map2;
        System.out.println("Obj" + JsonUtil.convert(o));
        List l = List.of(o);
        System.out.println("Obj" + JsonUtil.convert(l));
        
        String json = JsonUtil.convert(map);
        String json2 = JsonUtil.convert(map2);
        
        System.out.println("js" + json);
        System.out.println("js2" + json2);
                
        SerialMap smap = JsonUtil.convert(json, SerialMap.class);
        SerialMap smap2 = JsonUtil.convert(json2, SerialMap.class);

        System.out.println("json" + JsonUtil.convert(smap));
        System.out.println("json2"+ JsonUtil.convert(smap2));
     }
     */
    
    @Test
    public void test3() {
        SerialOneDim array = new SerialOneDim();
        array.array = new Integer[] { 1, 2, 3 };
        SerialMap map = new SerialMap();
        //map.list = List.of(List.of(11, "blbl"));
        SerialMap map2 = new SerialMap();
        map2.map.put("a", array);
        map.map.put("m", map2);
        SerialString s = new SerialString();
        s.string = "bla";
        map.map.put("s", s);
        //map2.list = List.of(List.of(1, map));
        //Object o = (Object) map2;
        //System.out.println("Obj" + JsonUtil.convert(o));
        //List l = List.of(o);
        //System.out.println("Obj" + JsonUtil.convert(l));
        
        String json = JsonUtil.convert(map);
        String json2 = JsonUtil.convert(map2);
        
        System.out.println("js" + json);
        System.out.println("js2" + json2);
                
        SerialMap smap = JsonUtil.convert(json, SerialMap.class);
        SerialMap smap2 = JsonUtil.convert(json2, SerialMap.class);

        String newjson = JsonUtil.convert(smap);
        String newjson2 = JsonUtil.convert(smap2);
        
        System.out.println("json" + newjson);
        System.out.println("json2"+ newjson2);
        
        assertEquals(json, newjson);
        assertEquals(json2, newjson2);
        
        System.out.println(((SerialOneDim)((SerialMap)map.map.get("m")).map.get("a")).array.getClass().getCanonicalName());
        System.out.println(((SerialOneDim)((SerialMap)smap.map.get("m")).map.get("a")).array.getClass().getCanonicalName());
    }
    
    
    /*
    */
    
            
}
