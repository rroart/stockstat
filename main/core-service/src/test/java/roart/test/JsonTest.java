package roart.test;

import java.io.IOException;

import org.apache.commons.lang3.tuple.Triple;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import roart.ml.spark.MLClassifySparkAccess;
import roart.ml.spark.MLClassifySparkMLPCModel;
import roart.ml.common.MLClassifyModel;
import roart.ml.model.LearnTestClassifyAccess;
import roart.common.util.JsonUtil;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.commons.lang3.tuple.ImmutableTriple;

import java.util.List;
import java.util.ArrayList;

public class JsonTest {

    @Test
    public void test() {
        //MLClassifySparkAccess m = new MLClassifySparkAccess(null);
        //MLClassifyModel m0 = new MLClassifySparkMLPCModel(null);
        LearnTestClassifyAccess param = new LearnTestClassifyAccess();
        List<Triple<String, Object, Double>> learnTestMap = new ArrayList<>();
        learnTestMap.add(new ImmutableTriple("one", "two", 3.0));
        //param.learnTestMap = learnTestMap;
        String j = JsonUtil.convert(learnTestMap);
        System.out.println(j);
        LearnTestClassifyAccess param2 = JsonUtil.convert(j, LearnTestClassifyAccess.class);
        System.out.println(param2);
    }
    
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
}
