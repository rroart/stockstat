package roart.gene;

import java.util.Random;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;

import roart.gene.impl.CalcComplexGene;
import roart.gene.impl.CalcDoubleGene;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(  
        use = JsonTypeInfo.Id.NAME,  
        include = JsonTypeInfo.As.PROPERTY,
        property = "_class")  
@JsonSubTypes({  
    @Type(value = CalcComplexGene.class, name = "CalcComplexGene"),  
    @Type(value = CalcDoubleGene.class, name = "CalcDoubleGene") })  
public abstract class CalcGene extends AbstractGene {

    public abstract double calc(double value, double minmaxthreshold);

}
