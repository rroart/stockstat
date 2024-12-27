package roart.evolution.chromosome;

import java.io.IOException;
import java.util.Map;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;

import roart.evolution.species.Individual;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        property = "_class")
@JsonSubTypes({  
    })  
public abstract class AbstractChromosome {
    protected Logger log = LoggerFactory.getLogger(this.getClass());
    
    protected Random random = new Random();

    protected Map<String, Object> resultMap;
    
    public Map<String, Object> getResultMap() {
        return resultMap;
    }

    public void setResultMap(Map<String, Object> resultMap) {
        this.resultMap = resultMap;
    }

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
