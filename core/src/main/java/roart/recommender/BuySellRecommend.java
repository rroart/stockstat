package roart.recommender;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import roart.config.MyConfig;
import roart.config.MyMyConfig;

public abstract class BuySellRecommend {
    public  abstract List<String> getBuyList();
    public abstract List<String> getSellList();
        public abstract void getBuySellRecommendations(Map<String, Double> buyMap, Map<String, Double> sellMap, MyConfig conf, List<Double> macdLists[] , Map<String, Double[]> listMap, Map<String, Double[]> momMap, List<String> buyList, List<String> sellList) throws JsonParseException, JsonMappingException, IOException;
        public abstract void mutate(Map<String, Object> configValueMap, List<String> keys);
        public abstract void getRandom(Map<String, Object> configValueMap, List<String> keys) throws JsonParseException, JsonMappingException, IOException;
        public abstract void transform(MyConfig newConf, List<String> keys) throws JsonParseException, JsonMappingException, IOException;
        public abstract void normalize(Map<String, Object> configValueMap, List<String> keys);

}
