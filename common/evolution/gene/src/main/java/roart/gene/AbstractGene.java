package roart.gene;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(  
        use = JsonTypeInfo.Id.NAME,  
        include = JsonTypeInfo.As.PROPERTY,  
        property = "_class")  
@JsonSubTypes({  
    @Type(value = CalcGene.class, name = "roart.gene.CalcGene") })  
public abstract class AbstractGene {

    public String className;

    public abstract void randomize();

    public abstract void mutate();

    public abstract AbstractGene crossover(AbstractGene other);
 
}
