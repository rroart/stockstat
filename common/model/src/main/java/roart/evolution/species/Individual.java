package roart.evolution.species;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import roart.evolution.chromosome.AbstractChromosome;

public class Individual  implements Comparable<Individual>{
    private Double fitness;
    
    private AbstractChromosome chromosome;

    private long calculatetime;

    public Individual(AbstractChromosome chromosome) {
        this.chromosome = chromosome;
    }
    
    public AbstractChromosome getEvaluation() {
        return chromosome;
    }

    public void setEvaluation(AbstractChromosome chromosome) {
        this.chromosome = chromosome;
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
        if (chromosome == null) {
            int j = 0;
        }
        AbstractChromosome newChromosome = chromosome.copy();
        newChromosome.transformToNode();
        return new Individual(newChromosome);
    }

    public Individual getNewWithValueCopyAndRandomFactory() throws JsonParseException, JsonMappingException, IOException {
        AbstractChromosome newChromosome = chromosome.copy();
        newChromosome.transformToNode();
        newChromosome.getRandom();
        return new Individual(newChromosome);
    }

    public Individual crossover(Individual pop) throws JsonParseException, JsonMappingException, IOException {
        return chromosome.crossover(pop.chromosome);
    }

    @Override
    public int compareTo(Individual arg0) {
        return Double.compare(arg0.fitness, fitness);
    }

    public void mutate() throws JsonParseException, JsonMappingException, IOException {
        chromosome.mutate();
        chromosome.normalize();
        fitness = null;
        //fitness = evaluation.getFitness();
    }
    public void recalculateScore() throws JsonParseException, JsonMappingException, IOException {
        fitness = chromosome.getFitness();

    }

    @Override
    public String toString() {
        return "" + fitness + " " + calculatetime + " #" + chromosome.hashCode() + " " + chromosome;
    }

    public Double getFitness() {
        return fitness;
    }

    /*
    public  static MyMyConfig getNewWithValueCopy(IclijConfig conf) {
        MyMyConfig newConf = new MyMyConfig(conf);
        Map<String, Object> configValueMap = new HashMap<>(conf.getConfigValueMap());
        newConf.setConfigValueMap(configValueMap);
        return newConf;
    }
    */
}


