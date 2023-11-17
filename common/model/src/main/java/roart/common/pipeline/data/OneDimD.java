package roart.common.pipeline.data;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

/*
@JsonTypeInfo(  
        use = JsonTypeInfo.Id.NAME,  
        include = JsonTypeInfo.As.PROPERTY,  
        property = "_class")
        */  
public class OneDimD {

    private Double[] array;
    
    public OneDimD() {
        super();
    }

    public OneDimD(Double[] array) {
        super();
        this.array = array;
    }

    public Double[] getArray() {
        return array;
    }

    public void setArray(Double[] array) {
        this.array = array;
    }
}
