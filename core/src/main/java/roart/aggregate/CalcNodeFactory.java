package roart.aggregate;

import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import roart.calculate.CalcComplexNode;
import roart.calculate.CalcDoubleNode;
import roart.calculate.CalcNode;

public class CalcNodeFactory {
    public static CalcNode get(String name, String jsonValue, List<Double>[] macdrsiMinMax, int index, boolean useMax) throws JsonParseException, JsonMappingException, IOException {
        CalcComplexNode anode;
        if (name != null && name.equals("Double")) {
            return new CalcDoubleNode();
        }
        if (jsonValue == null) {
            anode = new CalcComplexNode();
        } else {
            ObjectMapper mapper = new ObjectMapper();
            anode = mapper.readValue(jsonValue, CalcComplexNode.class);
        }
        List<Double> minmax = macdrsiMinMax[index];
        double minMutateThresholdRange =  minmax.get(0);
        double maxMutateThresholdRange = minmax.get(1);
        anode.setMinMutateThresholdRange(minMutateThresholdRange);
        anode.setMaxMutateThresholdRange(maxMutateThresholdRange);
        anode.setUseMax(useMax);
        return anode;
    }
}
