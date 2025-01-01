package roart.common.pipeline.data;

import com.fasterxml.jackson.annotation.JsonIgnore;

import roart.evolution.chromosome.AbstractChromosome;

public class SerialScoreChromosome extends SerialObject {
    private Double score;
    
    private AbstractChromosome chromosome;

    public SerialScoreChromosome() {
        super();
    }

    public SerialScoreChromosome(Double score, AbstractChromosome chromosome) {
        super();
        this.score = score;
        this.chromosome = chromosome;
    }

    public Double getScore() {
        return score;
    }

    @JsonIgnore
    public Double getLeft() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
    }

    public AbstractChromosome getChromosome() {
        return chromosome;
    }

    @JsonIgnore
    public AbstractChromosome getRight() {
        return chromosome;
    }

    public void setChromosome(AbstractChromosome chromosome) {
        this.chromosome = chromosome;
    }
    
    
}
