package roart.common.ml;

import org.junit.jupiter.api.Test;

import roart.common.config.MLConstants;
import roart.common.util.JsonUtil;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.MapperFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

public class SerializerTest {

    private static final ObjectMapper mapper = JsonMapper.builder()
            .configure(MapperFeature.USE_BASE_TYPE_AS_DEFAULT_IMPL, true)
            // handles tensorflow batchsize not present
            .configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, false)
            //.configure(MapperFeature.REQUIRE_TYPE_ID_FOR_SUBTYPES, false)
            .build();

    @Test
    public void test() throws Exception {
        //SparkOVRConfig conf = new TensorflowCNNConfig(10, 1E-6, true);
        //ObjectMapper mapper = new ObjectMapper();
        TensorflowCNNConfig nn = JsonUtil.convert(MLConstants.TENSORFLOWCNNCONFIG, TensorflowCNNConfig.class, mapper);
        System.out.println("nn" + nn);
        System.out.println("json" + JsonUtil.convert(nn));
        //nn = JsonUtil.convert(MLConstants.PYTORCHCOMMONCLASSIFY, TensorflowCNNConfig.class, mapper);
        //String newNNConfigstring = mapper.writeValueAsString(conf);
        //System.out.println(newNNConfigstring);
    }
}
