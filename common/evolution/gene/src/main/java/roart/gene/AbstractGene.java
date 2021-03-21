package roart.gene;

import java.util.Random;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

import roart.gene.impl.ConfigMapGene;

@JsonTypeInfo(  
        use = JsonTypeInfo.Id.NAME,  
        include = JsonTypeInfo.As.PROPERTY,  
        property = "_class")  
@JsonSubTypes({  
    @Type(value = CalcGene.class, name = "CalcGene"),
    @Type(value = ConfigMapGene.class, name = "ConfigMapGene"),
    @Type(value = NeuralNetConfigGene.class, name = "NeuralNetConfigGene") })  
public abstract class AbstractGene {
    protected Random random = new Random();

    public String className;

    public abstract void randomize();

    public abstract void mutate();

    public abstract AbstractGene crossover(AbstractGene other);
 
}
