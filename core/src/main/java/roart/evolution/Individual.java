package roart.evolution;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import roart.config.MyMyConfig;
import roart.evaluation.Evaluation;
import roart.mutation.Mutate;
import roart.service.ControlService;

public class Individual  implements Comparable<Individual>{
    public MyMyConfig conf;
    double fitness;
    Evaluation evaluation;
    public Individual(MyMyConfig conf, double fitness, Evaluation evaluation) {
        this.conf = conf;
        this.fitness = fitness;
        this.evaluation = evaluation;
    }
    public Individual getNewWithValueCopyFactory(MyMyConfig conf, List<String> keys, boolean doScore) throws JsonParseException, JsonMappingException, IOException {
        MyMyConfig newConf = ControlService.getNewWithValueCopy(conf);
        evaluation.transformToNode(newConf, keys);
        double fitness = 0.0;
        if (doScore) {
            fitness = evaluation.getFitness(newConf, keys);
        }
        return new Individual(newConf, fitness, evaluation);
    }
    
    public Individual getNewWithValueCopyAndRandomFactory(MyMyConfig conf, List<String> keys) throws JsonParseException, JsonMappingException, IOException {
        MyMyConfig newConf = ControlService.getNewWithValueCopy(conf);
        evaluation.transformToNode(newConf, keys);
        //ControlService.getRandom(newConf.configValueMap, keys);
        evaluation.getRandom(newConf.configValueMap, keys);
        double fitness = evaluation.getFitness(newConf, keys);
        return new Individual(newConf, fitness, evaluation);
    }
    
    public Individual crossover(Individual pop1, Individual pop2, List<String> keys, boolean doScore) throws JsonParseException, JsonMappingException, IOException {
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
        evaluation.normalize(configValueMap, keys);
        config.configValueMap = configValueMap;
        double fitness = 0.0;
        if (doScore) {
            fitness = evaluation.getFitness(conf, keys);
        }
        
        return new Individual(config, fitness, evaluation);
    }

    @Override
    public int compareTo(Individual arg0) {
        return Double.compare(arg0.fitness, fitness);
    }
    
    public void mutate(List<String> keys) throws JsonParseException, JsonMappingException, IOException {
        evaluation.mutate(conf.configValueMap, keys);
                 //Mutate.mutate(conf.configValueMap, keys);
        evaluation.normalize(conf.configValueMap, keys);
            fitness = evaluation.getFitness(conf, keys);
    }
    public void recalculateScore(List<String> keys) throws JsonParseException, JsonMappingException, IOException {
         fitness = evaluation.getFitness(conf, keys);
        
    }

    @Override
    public String toString() {
        return "" + fitness;
    }
    
    public double getFitness() {
        return fitness;
    }
}


