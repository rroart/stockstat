package roart.evolution;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import roart.config.MyMyConfig;
import roart.mutation.Mutate;
import roart.recommender.BuySellRecommend;
import roart.service.ControlService;

public class Populus  implements Comparable<Populus>{
    public MyMyConfig conf;
    double score;
    FitnessBuySellMACD scoring;
    BuySellRecommend recommend;
    public Populus(MyMyConfig conf, double score, FitnessBuySellMACD scoring, BuySellRecommend recommend) {
        this.conf = conf;
        this.score = score;
        this.scoring = scoring;
        this.recommend = recommend;
    }
    public Populus getNewWithValueCopyFactory(MyMyConfig conf, List<String> keys, boolean doScore, boolean doBuy) throws JsonParseException, JsonMappingException, IOException {
        MyMyConfig newConf = ControlService.getNewWithValueCopy(conf);
        double score = 0.0;
        if (doScore) {
            score = scoring.getScores(doBuy, conf, keys);
        }
        return new Populus(newConf, score, scoring, recommend);
    }
    
    public Populus getNewWithValueCopyAndRandomFactory(MyMyConfig conf, List<String> keys, boolean doBuy) throws JsonParseException, JsonMappingException, IOException {
        MyMyConfig newConf = ControlService.getNewWithValueCopy(conf);
        ControlService.getRandom(newConf.configValueMap, keys);
        recommend.getRandom(newConf.configValueMap, keys);
        double score = scoring.getScores(doBuy, newConf, keys);
        return new Populus(newConf, score, scoring, recommend);
    }
    
    public Populus crossover(Populus pop1, Populus pop2, List<String> keys, boolean doScore, boolean doBuy) throws JsonParseException, JsonMappingException, IOException {
        Random rand = new Random();
        Map<String, Object> configValueMap = new HashMap<>(pop1.conf.configValueMap);
        for (String key : keys) {
            Object value;
            if (rand.nextBoolean()) {
                value = pop1.conf.configValueMap.get(key);
            } else {
                value = pop2.conf.configValueMap.get(key);
            }
            configValueMap.put(key, value);
        }
        MyMyConfig config = new MyMyConfig(conf);
        ControlService.normalize(configValueMap, keys);
        config.configValueMap = configValueMap;
        double score = 0.0;
        if (doScore) {
            score = scoring.getScores(doBuy, conf, keys);
        }
        
        return new Populus(config, score, scoring, recommend);
    }

    @Override
    public int compareTo(Populus arg0) {
        return Double.compare(arg0.score, score);
    }
    
    public void mutate(List<String> keys, boolean doBuy) throws JsonParseException, JsonMappingException, IOException {
        recommend.mutate(conf.configValueMap, keys);
                 Mutate.mutate(conf.configValueMap, keys);
            score = scoring.getScores(doBuy, conf, keys);
    }
    public void recalculateScore(List<String> keys, boolean doBuy) throws JsonParseException, JsonMappingException, IOException {
         score = scoring.getScores(doBuy, conf, keys);
        
    }

    @Override
    public String toString() {
        return "" + score;
    }
}


