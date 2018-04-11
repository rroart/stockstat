package roart.calculate;

import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import roart.aggregate.CalcNodeFactory;
import roart.config.MLConstants;
import roart.config.MyConfig;
import roart.ml.NNConfig;
import roart.ml.SparkLRConfig;
import roart.ml.SparkMCPConfig;
import roart.ml.SparkOVRConfig;
import roart.ml.TensorflowDNNConfig;
import roart.ml.TensorflowDNNLConfig;
import roart.ml.TensorflowLConfig;

public class CalcNodeUtils {
    public static void transformToNode(MyConfig conf, List<String> keys, boolean useMax, List<Double>[] minMax, List<String> disableList) throws JsonParseException, JsonMappingException, IOException {
        ObjectMapper mapper = new ObjectMapper();
        for (int i = 0; i < keys.size(); i++) {
            String key = keys.get(i);
            if (disableList.contains(key)) {
                continue;
            }
            Object value = conf.getConfigValueMap().get(key);
            // this if is added to the original
            if (value instanceof CalcNode) {
                continue;
            }
            if (value instanceof Integer) {
                CalcNode anode = new CalcDoubleNode();
                conf.getConfigValueMap().put(key, anode);
                return;
            }
            String jsonValue = (String) conf.getConfigValueMap().get(key);
            if (jsonValue == null || jsonValue.isEmpty()) {
                jsonValue = (String) conf.getDeflt().get(key);
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
            conf.getConfigValueMap().put(key, node);
        }
    }

    public static void transformToNode(MyConfig conf, List<String> keys) throws JsonParseException, JsonMappingException, IOException {
        ObjectMapper mapper = new ObjectMapper();
        for (int i = 0; i < keys.size(); i++) {
            String key = keys.get(i);
            Object value = conf.getConfigValueMap().get(key);
            // this if is added to the original
            if (value instanceof NNConfig) {
                continue;
            }
            if (value instanceof String) {
                Class classs = NNConfig.class;
                NNConfig anode = mapper.readValue((String) value, NNConfig.class);
                switch (anode.getName()) {
                case MLConstants.MCP:
                    classs = SparkMCPConfig.class;
                    break;
                case MLConstants.LR:
                    classs = SparkLRConfig.class;
                    break;
                case MLConstants.OVR:
                    classs = SparkOVRConfig.class;
                    break;
                case MLConstants.DNN:
                    classs = TensorflowDNNConfig.class;
                    break;
                case MLConstants.DNNL:
                    classs = TensorflowDNNLConfig.class;
                    break;
                case MLConstants.L:
                    classs = TensorflowLConfig.class;
                    break;
                        
                }
                anode = (NNConfig) mapper.readValue((String) value, classs);
                conf.getConfigValueMap().put(key, anode);
                return;
            }
        }
    }

    public static void transformFromNode(MyConfig conf, List<String> keys, List<String> disableList) throws JsonParseException, JsonMappingException, IOException {
        ObjectMapper mapper = new ObjectMapper();
        for (String key : keys) {
            if (disableList.contains(key)) {
                continue;
            }
            CalcNode node = (CalcNode) conf.getConfigValueMap().get(key);
            if (node instanceof CalcComplexNode) {
                String string = mapper.writeValueAsString(node);
                conf.getConfigValueMap().put(key, string);
            } else {
                CalcDoubleNode anode = (CalcDoubleNode) node;
                conf.getConfigValueMap().put(key, anode.getWeight());
            }
        }
    }

    public static void transformFromNode(MyConfig conf, List<String> keys) throws JsonParseException, JsonMappingException, IOException {
        ObjectMapper mapper = new ObjectMapper();
        for (String key : keys) {
            NNConfig node = (NNConfig) conf.getConfigValueMap().get(key);
            String string = mapper.writeValueAsString(node);
            conf.getConfigValueMap().put(key, string);
        }
    }

}