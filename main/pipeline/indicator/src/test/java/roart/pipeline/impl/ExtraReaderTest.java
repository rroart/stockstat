package roart.pipeline.impl;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import roart.common.config.Extra;
import roart.common.util.JsonUtil;

public class ExtraReaderTest {

    String str2 = "{ \"complex\": [ { \"items\" : [ { \"market\" : \"tradcomm\", \"id\" : \"XAUUSD:CUR\" ,\"category\" : \"Price\" }, { \"market\" : \"tradcomm\", \"id\" : \"HG1:COM\" , \"category\" :  \"Price\" } ], \"expression\" : \"1 2 /\" } ] }";

    @Test
    public void t() {
        
        String str3 = "{ \"simple\" : [ { \"market\" : \"tradstoc\", \"id\" : \"VIX:IND\" ,\"category\" : \"Index\" }, { \"market\" : \"tradcomm\" , \"id\" : \"CL1:COM\" , \"category\": \"Price\" }, { \"market\" : \"tradcomm\", \"id\" : \"XAUUSD:CUR\" , \"category\" : \"Price\"} ], \"complex\": [ \"items\" : [ { \"market\" : \"tradcomm\", \"id\" : \"XAUUSD:CUR\" ,\"category\" : \"Price\" }, { \"market\" : \"tradcomm\", \"id\" : \"HG1:COM\" , \"category\" :  \"Price\" } ], \"expression\" : \"1 2 /\" ] }";
        String str = "{ \"complex\" : [ { \"items\" : [ { \"market\": \"ose\", \"id\": \"VIX\" } ], \"expression\": \"1\" } ] }";
        Extra extra = JsonUtil.convert(str2, Extra.class);
        System.out.println(extra.getComplex().get(0).getItems().get(0).getMarket());
        System.out.println(extra.getComplex().get(0).getExpression());
        int jj = 0;
    }

    @Test
    public void t0() {
        Extra extra = JsonUtil.convert(str2, Extra.class);
        Map<String, Object> map = new HashMap<>();
        map.put("aggregators.indicator.extrasmacd", true);
        map.put("aggregators.indicator.extraslist", extra);
        Map<String, Object> map2 = new HashMap<>();
        map2.put("aggregators.indicator.extrasmacd", true);
        map2.put("aggregators.indicator.extraslist", JsonUtil.convert(extra));
        String astr = JsonUtil.convert(map);
        System.out.println("map " + astr);
        String astr2 = JsonUtil.convert(map2);
        System.out.println("map2 " + astr2);
        System.out.println("ok1 " + JsonUtil.convert(astr, Map.class));
        System.out.println("ok2 " + JsonUtil.convert(astr2, Map.class));
        System.out.println("ok3 " + JsonUtil.convertnostrip(astr, Map.class));
        System.out.println("ok4 " + JsonUtil.convertnostrip(astr2, Map.class));
    }
    
    @Test
    public void test1() {
        String s0 = "{\"aggregators.indicator.extrasmacd\":true,\"aggregators.indicator.extrasdeltas\":3,\"aggregators.indicator.rsi\":true,\"aggregators.indicator.threshold\":\"[1.0]\",\"aggregators.indicator.macd\":false,\"aggregators.indicator.extrasrsi\":true,\"aggregators.indicator.intervaldays\":20,\"misc.threshold\":null,\"aggregators.indicator.extrasbits\":\"100\",\"aggregators.indicator.extraslist\":\"[{\"complex\":[{\"items\":[{\"market\":\"tradcomm\",\"id\":\"XAUUSD:CUR\",\"category\":\"Price\"},{\"market\":\"tradcomm\",\"id\":\"HG1:COM\",\"category\":\"Price\"}],\"expression\":\"1 2 /\"}],\"simple\":null},{\"complex\":null,\"simple\":[{\"market\":\"tradcomm\",\"id\":\"XAUUSD:CUR\",\"category\":\"Price\"}]},{\"complex\":null,\"simple\":[{\"market\":\"tradcomm\",\"id\":\"CL1:COM\",\"category\":\"Price\"}]}]\",\"aggregators.indicator.futuredays\":7}";
        String s1 = "{\"aggregators.indicator.extraslist\":\"[{\"complex\":[{\"items\":[{\"market\":\"tradcomm\",\"id\":\"XAUUSD:CUR\",\"category\":\"Price\"},{\"market\":\"tradcomm\",\"id\":\"HG1:COM\",\"category\":\"Price\"}],\"expression\":\"1 2 /\"}],\"simple\":null},{\"complex\":null,\"simple\":[{\"market\":\"tradcomm\",\"id\":\"XAUUSD:CUR\",\"category\":\"Price\"}]},{\"complex\":null,\"simple\":[{\"market\":\"tradcomm\",\"id\":\"CL1:COM\",\"category\":\"Price\"}]}]\"}";
        String s = "{\"aggregators.indicator.extraslist\":[{\"complex\":[{\"items\":[{\"market\":\"tradcomm\",\"id\":\"XAUUSD:CUR\",\"category\":\"Price\"},{\"market\":\"tradcomm\",\"id\":\"HG1:COM\",\"category\":\"Price\"}],\"expression\":\"1 2 /\"}],\"simple\":null},{\"complex\":null,\"simple\":[{\"market\":\"tradcomm\",\"id\":\"XAUUSD:CUR\",\"category\":\"Price\"}]},{\"complex\":null,\"simple\":[{\"market\":\"tradcomm\",\"id\":\"CL1:COM\",\"category\":\"Price\"}]}]}";
        Map m = JsonUtil.convertnostrip(s, Map.class);
        Extra[] extras = new Extra[1];
    }
    
}