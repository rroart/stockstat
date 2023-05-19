package roart.pipeline.impl;

import org.junit.jupiter.api.Test;

import roart.common.config.Extra;
import roart.common.util.JsonUtil;

public class ExtraReaderTest {
 
    @Test
    public void t() {
        
        String str3 = "{ \"simple\" : [ { \"market\" : \"tradstoc\", \"id\" : \"VIX:IND\" ,\"category\" : \"Index\" }, { \"market\" : \"tradcomm\" , \"id\" : \"CL1:COM\" , \"category\": \"Price\" }, { \"market\" : \"tradcomm\", \"id\" : \"XAUUSD:CUR\" , \"category\" : \"Price\"} ], \"complex\": [ \"items\" : [ { \"market\" : \"tradcomm\", \"id\" : \"XAUUSD:CUR\" ,\"category\" : \"Price\" }, { \"market\" : \"tradcomm\", \"id\" : \"HG1:COM\" , \"category\" :  \"Price\" } ], \"expression\" : \"1 2 /\" ] }";
        String str2 = "{ \"complex\": [ { \"items\" : [ { \"market\" : \"tradcomm\", \"id\" : \"XAUUSD:CUR\" ,\"category\" : \"Price\" }, { \"market\" : \"tradcomm\", \"id\" : \"HG1:COM\" , \"category\" :  \"Price\" } ], \"expression\" : \"1 2 /\" } ] }";
        String str = "{ \"complex\" : [ { \"items\" : [ { \"market\": \"ose\", \"id\": \"VIX\" } ], \"expression\": \"1\" } ] }";
        Extra extra = JsonUtil.convert(str2, Extra.class);
        System.out.println(extra.getComplex().get(0).getItems().get(0).getMarket());
        System.out.println(extra.getComplex().get(0).getExpression());
        int jj = 0;
    }
}