package roart.gene.impl;

import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import roart.common.config.MLConstants;
import roart.common.config.MyConfig;
import roart.common.ml.NeuralNetConfig;
import roart.common.ml.SparkLORConfig;
import roart.common.ml.SparkMLPCConfig;
import roart.common.ml.SparkOVRConfig;
import roart.common.ml.TensorflowDNNConfig;
import roart.common.ml.TensorflowLICConfig;
import roart.gene.CalcGene;

public class CalcGeneUtils {
    public static void transformToNode(MyConfig conf, List<String> keys, boolean useMax, List<Double>[] minMax, List<String> disableList) throws JsonParseException, JsonMappingException, IOException {
        ObjectMapper mapper = new ObjectMapper();
        for (int i = 0; i < keys.size(); i++) {
            String key = keys.get(i);
            if (disableList.contains(key)) {
                continue;
            }
            Object value = conf.getConfigValueMap().get(key);
            // this if is added to the original
            if (value instanceof CalcGene) {
                continue;
            }
            if (value instanceof Integer) {
                CalcGene anode = new CalcDoubleGene();
                conf.getConfigValueMap().put(key, anode);
                continue;
            }
            String jsonValue = (String) conf.getConfigValueMap().get(key);
            if (jsonValue == null || jsonValue.isEmpty()) {
                jsonValue = (String) conf.getConfigMaps().deflt.get(key);
            }
            CalcGene anode;
            CalcGene node;
            if (jsonValue == null || jsonValue.isEmpty()) {
                anode = new CalcComplexGene();
                node = CalcGeneFactory.get(anode.className, jsonValue, minMax, i, useMax);
                node.randomize();
            } else {
                // temp workaround for (I33)
                // anode = mapper.readValue(jsonValue, CalcGene.class);
                if (jsonValue.contains(CalcComplexGene.class.getSimpleName())) {
                    node = CalcGeneFactory.get("NotDouble", jsonValue, minMax, i, useMax);                    
                } else {
                    node = CalcGeneFactory.get("Double", jsonValue, minMax, i, useMax);
                }
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
            if (value instanceof NeuralNetConfig) {
                continue;
            }
            if (value instanceof String) {
                Class classs = NeuralNetConfig.class;
                NeuralNetConfig anode = mapper.readValue((String) value, NeuralNetConfig.class);
                switch (anode.getName()) {
                case MLConstants.MLPC:
                    classs = SparkMLPCConfig.class;
                    break;
                case MLConstants.LOR:
                    classs = SparkLORConfig.class;
                    break;
                case MLConstants.OVR:
                    classs = SparkOVRConfig.class;
                    break;
                case MLConstants.DNN:
                    classs = TensorflowDNNConfig.class;
                    break;
                case MLConstants.LIC:
                    classs = TensorflowLICConfig.class;
                    break;
                        
                }
                anode = (NeuralNetConfig) mapper.readValue((String) value, classs);
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
            CalcGene node = (CalcGene) conf.getConfigValueMap().get(key);
            if (node instanceof CalcComplexGene) {
                String string = mapper.writeValueAsString(node);
                conf.getConfigValueMap().put(key, string);
            } else {
                CalcDoubleGene anode = (CalcDoubleGene) node;
                conf.getConfigValueMap().put(key, anode.getWeight());
            }
        }
    }

    public static void transformFromNode(MyConfig conf, List<String> keys) throws JsonParseException, JsonMappingException, IOException {
        ObjectMapper mapper = new ObjectMapper();
        for (String key : keys) {
            NeuralNetConfig node = (NeuralNetConfig) conf.getConfigValueMap().get(key);
            String string = mapper.writeValueAsString(node);
            conf.getConfigValueMap().put(key, string);
        }
    }

}