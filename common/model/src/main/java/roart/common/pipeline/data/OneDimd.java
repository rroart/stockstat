package roart.common.pipeline.data;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

/*
@JsonTypeInfo(  
        use = JsonTypeInfo.Id.NAME,  
        include = JsonTypeInfo.As.PROPERTY,  
        property = "_class")
        */  
public class OneDimd {

    private double[] array;
    
    public OneDimd() {
        super();
    }

    public OneDimd(double[] array) {
        super();
        this.array = array;
    }

    public double[] getArray() {
        return array;
    }

    public void setArray(double[] array) {
        this.array = array;
    }
    
}
