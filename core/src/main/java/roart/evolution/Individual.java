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
        MyMyConfig newConf = Individual.getNewWithValueCopy(conf);
        evaluation.transformToNode(newConf, keys);
        double myfitness = 0.0;
        if (doScore) {
            myfitness = evaluation.getFitness(newConf, keys);
        }
        return new Individual(newConf, myfitness, evaluation);
    }

    public Individual getNewWithValueCopyAndRandomFactory(MyMyConfig conf, List<String> keys) throws JsonParseException, JsonMappingException, IOException {
        MyMyConfig newConf = Individual.getNewWithValueCopy(conf);
        evaluation.transformToNode(newConf, keys);
        evaluation.getRandom(newConf.configValueMap, keys);
        double myfitness = evaluation.getFitness(newConf, keys);
        return new Individual(newConf, myfitness, evaluation);
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
        double myfitness = 0.0;
        if (doScore) {
            myfitness = evaluation.getFitness(conf, keys);
        }

        return new Individual(config, myfitness, evaluation);
    }

    @Override
    public int compareTo(Individual arg0) {
        return Double.compare(arg0.fitness, fitness);
    }

    public void mutate(List<String> keys) throws JsonParseException, JsonMappingException, IOException {
        evaluation.mutate(conf.configValueMap, keys);
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
    public  static MyMyConfig getNewWithValueCopy(MyMyConfig conf) {
        MyMyConfig newConf = new MyMyConfig(conf);
        Map<String, Object> configValueMap = new HashMap<>(conf.configValueMap);
        newConf.configValueMap = configValueMap;
        return newConf;
    }
}


