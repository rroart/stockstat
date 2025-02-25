package roart.evolution.fitness;

import roart.evolution.chromosome.AbstractChromosome;

public abstract class Fitness {

    public abstract double fitness(AbstractChromosome chromosome);
    
    public String titleText() {
        return null;
    }
    
    public String subTitleText() {
        return null;
    }
}
