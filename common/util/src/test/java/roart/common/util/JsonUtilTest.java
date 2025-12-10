package roart.common.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import tools.jackson.databind.ObjectMapper;
//import tools.jackson.databind.ObjectMapper.DefaultTyping;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import tools.jackson.databind.jsontype.PolymorphicTypeValidator;
//import tools.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;

import roart.common.constants.Constants;
import roart.common.util.ArraysUtil;

public class JsonUtilTest {

    @Test
    public void test() {
        PolymorphicTypeValidator ptv = BasicPolymorphicTypeValidator
                .builder()
                //.allowIfBaseType(MyObject.class)
                .build();
        ptv = null; //new LaissezFaireSubTypeValidator();
        ObjectMapper mapper = JsonMapper.builder()
                .activateDefaultTyping(ptv, null /*DefaultTyping.NON_FINAL*/, As.PROPERTY)
                .build();
        Arr arr = new Arr();
        arr.arra = new double[][] { { 2.0 } };
        String ser = JsonUtil.convert(arr, mapper);
        System.out.println("ser" + ser);
        Arr arr2 = JsonUtil.convert(ser, Arr.class, mapper);
        System.out.println("" + arr2.arra[0][0]);
        Map<String, MyObject> map = new HashMap<>();
        map.put("1", arr);
        ser = JsonUtil.convert(map, mapper);
        System.out.println("ser2 " + ser);
        Map<String, MyObject> map2 = JsonUtil.convert(ser, Map.class, mapper);
        System.out.println(map2.get("1"));
        System.out.println(map2.get("1").getClass().getCanonicalName());
        System.out.println(arr.getClass().getCanonicalName());
        List<MyObject> list = new ArrayList<>();
        list.add(arr);
        ser = JsonUtil.convert(list, mapper);
        System.out.println("ser" + ser);
        Arr[] a = new Arr[] { arr };
        ser = JsonUtil.convert(a, mapper);
        System.out.println("ser" + ser);
        
    }

    @Test
    public void test1() {
	String s0 = "{\"aggregators.indicator.extrasmacd\":true,\"aggregators.indicator.extrasdeltas\":3,\"aggregators.indicator.rsi\":true,\"aggregators.indicator.threshold\":\"[1.0]\",\"aggregators.indicator.macd\":false,\"aggregators.indicator.extrasrsi\":true,\"aggregators.indicator.intervaldays\":20,\"misc.threshold\":null,\"aggregators.indicator.extrasbits\":\"100\",\"aggregators.indicator.extraslist\":\"[{\"complex\":[{\"items\":[{\"market\":\"tradcomm\",\"id\":\"XAUUSD:CUR\",\"category\":\"Price\"},{\"market\":\"tradcomm\",\"id\":\"HG1:COM\",\"category\":\"Price\"}],\"expression\":\"1 2 /\"}],\"simple\":null},{\"complex\":null,\"simple\":[{\"market\":\"tradcomm\",\"id\":\"XAUUSD:CUR\",\"category\":\"Price\"}]},{\"complex\":null,\"simple\":[{\"market\":\"tradcomm\",\"id\":\"CL1:COM\",\"category\":\"Price\"}]}]\",\"aggregators.indicator.futuredays\":7}";
        String s1 = "{\"aggregators.indicator.extraslist\":\"[{\"complex\":[{\"items\":[{\"market\":\"tradcomm\",\"id\":\"XAUUSD:CUR\",\"category\":\"Price\"},{\"market\":\"tradcomm\",\"id\":\"HG1:COM\",\"category\":\"Price\"}],\"expression\":\"1 2 /\"}],\"simple\":null},{\"complex\":null,\"simple\":[{\"market\":\"tradcomm\",\"id\":\"XAUUSD:CUR\",\"category\":\"Price\"}]},{\"complex\":null,\"simple\":[{\"market\":\"tradcomm\",\"id\":\"CL1:COM\",\"category\":\"Price\"}]}]\"}";
        String s = "{\"aggregators.indicator.extraslist\":[{\"complex\":[{\"items\":[{\"market\":\"tradcomm\",\"id\":\"XAUUSD:CUR\",\"category\":\"Price\"},{\"market\":\"tradcomm\",\"id\":\"HG1:COM\",\"category\":\"Price\"}],\"expression\":\"1 2 /\"}],\"simple\":null},{\"complex\":null,\"simple\":[{\"market\":\"tradcomm\",\"id\":\"XAUUSD:CUR\",\"category\":\"Price\"}]},{\"complex\":null,\"simple\":[{\"market\":\"tradcomm\",\"id\":\"CL1:COM\",\"category\":\"Price\"}]}]}";
        Map m = JsonUtil.convertnostrip(s, Map.class);
    }
    
    /*
    @Test
    public void test2() {
        Double[][] a = new Double[][]{ { 2.0 } };
        TwoDimD d = new TwoDimD(a);
        String s = JsonUtil.convert(d);
        System.out.println("ser" + s);
        TwoDimD d2 = JsonUtil.convert(s, TwoDimD.class);
        System.out.println("ser" + d2.getArray()[0][0]);
        System.out.println("ser" + d2.getArray().getClass().getCanonicalName());
    }
    */
}

