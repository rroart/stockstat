package roart.evaluation;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import roart.config.MyConfig;
import roart.config.MyMyConfig;

public abstract class Evaluation {
    public List<String> keys;
    public List<String> getKeys() {
        return keys;
    }
    
    //public  abstract List<String> getBuyList();
    //public abstract List<String> getSellList();
        public abstract double getEvaluations(MyMyConfig conf, int j) throws JsonParseException, JsonMappingException, IOException;
        public abstract void mutate(Map<String, Object> configValueMap, List<String> keys);
        public abstract void getRandom(Map<String, Object> configValueMap, List<String> keys) throws JsonParseException, JsonMappingException, IOException;
        public abstract void transformToNode(MyConfig newConf, List<String> keys) throws JsonParseException, JsonMappingException, IOException;
        public abstract void normalize(Map<String, Object> configValueMap, List<String> keys);
        public abstract void transformFromNode(MyConfig conf, List<String> keys) throws JsonParseException, JsonMappingException, IOException;

        public abstract double getFitness(MyMyConfig testConfig, List<String> keys)
                throws JsonParseException, JsonMappingException, IOException;
}
