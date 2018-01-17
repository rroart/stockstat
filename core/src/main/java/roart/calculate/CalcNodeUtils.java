package roart.calculate;

import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import roart.aggregate.CalcNodeFactory;
import roart.config.MyConfig;

public class CalcNodeUtils {
    public static void transformToNode(MyConfig conf, List<String> keys, boolean useMax, List<Double>[] minMax, List<String> disableList) throws JsonParseException, JsonMappingException, IOException {
        ObjectMapper mapper = new ObjectMapper();
        for (int i = 0; i < keys.size(); i++) {
            String key = keys.get(i);
            if (disableList.contains(key)) {
                continue;
            }
            Object value = conf.configValueMap.get(key);
            // this if is added to the original
            if (value instanceof CalcNode) {
                continue;
            }
            if (value instanceof Integer) {
                CalcNode anode = new CalcDoubleNode();
                conf.configValueMap.put(key, anode);
                return;
            }
            String jsonValue = (String) conf.configValueMap.get(key);
            if (jsonValue == null || jsonValue.isEmpty()) {
                jsonValue = (String) conf.deflt.get(key);
            }
            CalcNode anode;
            CalcNode node;
            if (jsonValue == null || jsonValue.isEmpty()) {
                anode = new CalcComplexNode();
                node = CalcNodeFactory.get(anode.className, jsonValue, minMax, i, useMax);
                node.randomize();
            } else {
                anode = mapper.readValue(jsonValue, CalcNode.class);
                node = CalcNodeFactory.get(anode.className, jsonValue, minMax, i, useMax);
            }
            conf.configValueMap.put(key, node);
        }
    }

    public static void transformFromNode(MyConfig conf, List<String> keys, List<String> disableList) throws JsonParseException, JsonMappingException, IOException {
        ObjectMapper mapper = new ObjectMapper();
        for (String key : keys) {
            if (disableList.contains(key)) {
                continue;
            }
            CalcNode node = (CalcNode) conf.configValueMap.get(key);
            if (node instanceof CalcComplexNode) {
                String string = mapper.writeValueAsString(node);
                conf.configValueMap.put(key, string);
            } else {
                CalcDoubleNode anode = (CalcDoubleNode) node;
                conf.configValueMap.put(key, anode.getWeight());
            }
        }
    }

}