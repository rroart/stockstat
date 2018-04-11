package roart.evolution;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import roart.evaluation.Evaluation;

public class Individual  implements Comparable<Individual>{
    private Double fitness;
    
    private Evaluation evaluation;
    
    public Individual(Evaluation evaluation) {
        this.evaluation = evaluation;
    }
    
    public Evaluation getEvaluation() {
        return evaluation;
    }

    public void setEvaluation(Evaluation evaluation) {
        this.evaluation = evaluation;
    }

    public void setFitness(Double fitness) {
        this.fitness = fitness;
    }

    public Individual getNewWithValueCopyFactory() throws JsonParseException, JsonMappingException, IOException {
        Evaluation newEval = evaluation.copy();
        return new Individual(newEval);
    }

    public Individual getNewWithValueCopyAndRandomFactory() throws JsonParseException, JsonMappingException, IOException {
        Evaluation newEval = evaluation.copy();
        newEval.getRandom();
        return new Individual(newEval);
    }

    public Individual crossover(Individual pop) throws JsonParseException, JsonMappingException, IOException {
        return evaluation.crossover(pop.evaluation);
    }

    @Override
    public int compareTo(Individual arg0) {
        return Double.compare(arg0.fitness, fitness);
    }

    public void mutate() throws JsonParseException, JsonMappingException, IOException {
        evaluation.mutate();
        evaluation.normalize();
        //fitness = evaluation.getFitness();
    }
    public void recalculateScore() throws JsonParseException, JsonMappingException, IOException {
        fitness = evaluation.getFitness();

    }

    @Override
    public String toString() {
        return "" + fitness;
    }

    public Double getFitness() {
        return fitness;
    }
    
    /*
    public  static MyMyConfig getNewWithValueCopy(MyMyConfig conf) {
        MyMyConfig newConf = new MyMyConfig(conf);
        Map<String, Object> configValueMap = new HashMap<>(conf.getConfigValueMap());
        newConf.setConfigValueMap(configValueMap);
        return newConf;
    }
    */
}


