package roart.test;

import java.io.IOException;

import org.apache.commons.lang3.tuple.Triple;
import org.junit.jupiter.api.Test;

//import tools.jackson.core.JsonGenerator;
//import tools.jackson.databind.JsonSerializer;
//import tools.jackson.databind.SerializerProvider;
import roart.ml.spark.MLClassifySparkDS;
import roart.ml.spark.MLClassifySparkMLPCModel;
import roart.ml.common.MLClassifyModel;
import roart.ml.model.LearnTestClassifyDS;
import roart.common.util.JsonUtil;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import roart.common.config.Extra;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;

public class JsonTest {

    @Test
    public void test1() {
        String s0 = "{\"aggregators.indicator.extrasmacd\":true,\"aggregators.indicator.extrasdeltas\":3,\"aggregators.indicator.rsi\":true,\"aggregators.indicator.threshold\":\"[1.0]\",\"aggregators.indicator.macd\":false,\"aggregators.indicator.extrasrsi\":true,\"aggregators.indicator.intervaldays\":20,\"misc.threshold\":null,\"aggregators.indicator.extrasbits\":\"100\",\"aggregators.indicator.extraslist\":\"[{\"complex\":[{\"items\":[{\"market\":\"tradcomm\",\"id\":\"XAUUSD:CUR\",\"category\":\"Price\"},{\"market\":\"tradcomm\",\"id\":\"HG1:COM\",\"category\":\"Price\"}],\"expression\":\"1 2 /\"}],\"simple\":null},{\"complex\":null,\"simple\":[{\"market\":\"tradcomm\",\"id\":\"XAUUSD:CUR\",\"category\":\"Price\"}]},{\"complex\":null,\"simple\":[{\"market\":\"tradcomm\",\"id\":\"CL1:COM\",\"category\":\"Price\"}]}]\",\"aggregators.indicator.futuredays\":7}";
        String s1 = "{\"aggregators.indicator.extraslist\":\"[{\"complex\":[{\"items\":[{\"market\":\"tradcomm\",\"id\":\"XAUUSD:CUR\",\"category\":\"Price\"},{\"market\":\"tradcomm\",\"id\":\"HG1:COM\",\"category\":\"Price\"}],\"expression\":\"1 2 /\"}],\"simple\":null},{\"complex\":null,\"simple\":[{\"market\":\"tradcomm\",\"id\":\"XAUUSD:CUR\",\"category\":\"Price\"}]},{\"complex\":null,\"simple\":[{\"market\":\"tradcomm\",\"id\":\"CL1:COM\",\"category\":\"Price\"}]}]\"}";
        String s = "{\"aggregators.indicator.extraslist\":[{\"complex\":[{\"items\":[{\"market\":\"tradcomm\",\"id\":\"XAUUSD:CUR\",\"category\":\"Price\"},{\"market\":\"tradcomm\",\"id\":\"HG1:COM\",\"category\":\"Price\"}],\"expression\":\"1 2 /\"}],\"simple\":null},{\"complex\":null,\"simple\":[{\"market\":\"tradcomm\",\"id\":\"XAUUSD:CUR\",\"category\":\"Price\"}]},{\"complex\":null,\"simple\":[{\"market\":\"tradcomm\",\"id\":\"CL1:COM\",\"category\":\"Price\"}]}]}";
        Map m = JsonUtil.convertnostrip(s, Map.class);
        Extra[] extras = new Extra[1];
    }
    
    @Test
    public void test() {
        //MLClassifySparkDS m = new MLClassifySparkDS(null);
        //MLClassifyModel m0 = new MLClassifySparkMLPCModel(null);
        LearnTestClassifyDS param = new LearnTestClassifyDS();
        List<Triple<String, Object, Double>> learnTestMap = new ArrayList<>();
        learnTestMap.add(new ImmutableTriple("one", "two", 3.0));
        //param.learnTestMap = learnTestMap;
        String j = JsonUtil.convert(learnTestMap);
        System.out.println(j);
        LearnTestClassifyDS param2 = JsonUtil.convert(j, LearnTestClassifyDS.class);
        System.out.println(param2);
    }
    /*
    private static final class TripleSerializer extends JsonSerializer<Triple> {

        @Override
        public void serialize(Triple value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            gen.writeStartObject();
            gen.writeObjectField("left", value.getLeft());
            gen.writeObjectField("middle", value.getMiddle());
            gen.writeObjectField("right", value.getRight());
            gen.writeEndObject();
        }

    }
    */
}
