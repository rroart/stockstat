package roart.evolution.species;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import roart.evolution.model.Evaluation;

public class Individual  implements Comparable<Individual>{
    private Double fitness;
    
    private Evaluation evaluation;

    private long calculatetime;

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

    public void setCalculateTime(long time) {
        this.calculatetime = time;
    }
    
    public long getCalculateTime() {
        return calculatetime;
    }
    
    public Individual getNewWithValueCopyFactory() throws JsonParseException, JsonMappingException, IOException {
        if (evaluation == null) {
            int j = 0;
        }
        Evaluation newEval = evaluation.copy();
        newEval.transformToNode();
        return new Individual(newEval);
    }

    public Individual getNewWithValueCopyAndRandomFactory() throws JsonParseException, JsonMappingException, IOException {
        Evaluation newEval = evaluation.copy();
        newEval.transformToNode();
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
        return "" + fitness + " " + calculatetime + " " + evaluation;
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


