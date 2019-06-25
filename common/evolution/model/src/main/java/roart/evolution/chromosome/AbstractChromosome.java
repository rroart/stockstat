package roart.evolution.chromosome;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import roart.evolution.species.Individual;

public abstract class AbstractChromosome {
    protected Logger log = LoggerFactory.getLogger(this.getClass());
    
    private boolean ascending = true;
    
    public void setAscending(boolean ascending) {
        this.ascending = ascending;
    }

    public abstract double getEvaluations(int j) throws JsonParseException, JsonMappingException, IOException;
    
    public abstract void mutate();
    
    public abstract void getRandom() throws JsonParseException, JsonMappingException, IOException;
    
    public abstract void transformToNode() throws JsonParseException, JsonMappingException, IOException;
    
    public abstract void normalize();
    
    public abstract void transformFromNode() throws JsonParseException, JsonMappingException, IOException;

    public abstract double getFitness()
            throws JsonParseException, JsonMappingException, IOException;

    public abstract Individual crossover(AbstractChromosome chromosome);

    public abstract AbstractChromosome copy();

    public abstract boolean isEmpty();
    
    public boolean isAscending() {
        return ascending;
    }
    
    public boolean validate() {
        return true;
    }
    
    public void fixValidation() { };
}
