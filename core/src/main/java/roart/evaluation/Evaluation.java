package roart.evaluation;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import roart.config.MyConfig;
import roart.config.MyMyConfig;
import roart.evolution.Individual;

public abstract class Evaluation {
    public abstract double getEvaluations(int j) throws JsonParseException, JsonMappingException, IOException;
    
    public abstract void mutate();
    
    public abstract void getRandom() throws JsonParseException, JsonMappingException, IOException;
    
    public abstract void transformToNode() throws JsonParseException, JsonMappingException, IOException;
    
    public abstract void normalize();
    
    public abstract void transformFromNode() throws JsonParseException, JsonMappingException, IOException;

    public abstract double getFitness()
            throws JsonParseException, JsonMappingException, IOException;

    public abstract Individual crossover(Evaluation evaluation);

    public abstract Evaluation copy();

    public abstract boolean isEmpty();
}
