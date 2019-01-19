package roart.evolution.chromosome;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import roart.evolution.species.Individual;

public abstract class AbstractChromosome {
    public abstract double getEvaluations(int j) throws JsonParseException, JsonMappingException, IOException;
    
    public abstract void mutate();
    
    public abstract void getRandom() throws JsonParseException, JsonMappingException, IOException;
    
    public abstract void transformToNode() throws JsonParseException, JsonMappingException, IOException;
    
    public abstract void normalize();
    
    public abstract void transformFromNode() throws JsonParseException, JsonMappingException, IOException;

    public abstract double getFitness()
            throws JsonParseException, JsonMappingException, IOException;

    public abstract Individual crossover(AbstractChromosome evaluation);

    public abstract AbstractChromosome copy();

    public abstract boolean isEmpty();
}
