package roart.common.pipeline.data;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/*
@JsonTypeInfo(  
        use = JsonTypeInfo.Id.NAME,  
        include = JsonTypeInfo.As.PROPERTY,  
        property = "_class")  
        */
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        property = "_class")
@JsonSubTypes({  
    @Type(value = SerialDouble.class, name = "SerialDouble"),
    @Type(value = SerialIncDec.class, name = "SerialIncDec"),
    @Type(value = SerialInteger.class, name = "SerialInteger"),
    @Type(value = SerialList.class, name = "SerialList"),
    @Type(value = SerialMap.class, name = "SerialMap"),
    @Type(value = SerialMapPlain.class, name = "SerialMapPlain"),
    @Type(value = SerialOneDim.class, name = "SerialOneDim"),
    @Type(value = SerialResultMeta.class, name = "SerialResultMeta"),
    @Type(value = SerialString.class, name = "SerialString"),
    @Type(value = SerialMapdd.class, name = "SerialMapdd"),
    @Type(value = SerialMapDD.class, name = "SerialMapDD"),
    @Type(value = TwoDimd.class, name = "TwoDimd"),
    @Type(value = TwoDimD.class, name = "TwoDimD")
})
public abstract class SerialObject {

    protected SerialObject() {
        super();
    }

}
