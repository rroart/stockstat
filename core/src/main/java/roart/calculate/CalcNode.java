package roart.calculate;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(  
        use = JsonTypeInfo.Id.NAME,  
        include = JsonTypeInfo.As.PROPERTY,  
        property = "_class")  
@JsonSubTypes({  
    @Type(value = CalcComplexNode.class, name = "roart.calculate.CalcMACDNode"),  
    @Type(value = CalcDoubleNode.class, name = "roart.calculate.CalcDoubleNode") })  
public abstract class CalcNode {

    public String className;

    public abstract double calc(double value, double minmaxthreshold);

    public abstract void randomize();

    public abstract void mutate();
}
