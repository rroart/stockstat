package roart.common.pipeline.data;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

/*
@JsonTypeInfo(  
        use = JsonTypeInfo.Id.NAME,  
        include = JsonTypeInfo.As.PROPERTY,  
        property = "_class")  
        */
@JsonTypeInfo(
        use = JsonTypeInfo.Id.CLASS,
        property = "_class")
public abstract class SerialObject {

    public SerialObject() {
        super();
    }

}
