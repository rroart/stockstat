package roart.common.pipeline.data;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

/*
@JsonTypeInfo(  
        use = JsonTypeInfo.Id.NAME,  
        include = JsonTypeInfo.As.PROPERTY,  
        property = "_class")
        */  
public class TwoDimD {

    private Double[][] array;
    
    public TwoDimD() {
        super();
    }

    public TwoDimD(Double[][] array) {
        super();
        this.array = array;
    }

    public Double[][] getArray() {
        return array;
    }

    public void setArray(Double[][] array) {
        this.array = array;
    }
}
