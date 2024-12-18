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
    @Type(value = PipelineData.class, name = "PipelineData"),
    @Type(value = SerialDouble.class, name = "SerialDouble"),
    @Type(value = SerialIncDec.class, name = "SerialIncDec"),
    @Type(value = SerialInteger.class, name = "SerialInteger"),
    @Type(value = SerialList.class, name = "SerialList"),
    @Type(value = SerialListPlain.class, name = "SerialListPlain"),
    @Type(value = SerialMap.class, name = "SerialMap"),
    @Type(value = SerialMapPlain.class, name = "SerialMapPlain"),
    @Type(value = SerialMarketStock.class, name = "SerialMarketStock"),
    @Type(value = SerialMeta.class, name = "SerialMeta"),
    @Type(value = SerialOneDim.class, name = "SerialOneDim"),
    @Type(value = SerialPlain.class, name = "SerialPlain"),
    @Type(value = SerialResultMeta.class, name = "SerialResultMeta"),
    @Type(value = SerialString.class, name = "SerialString"),
    @Type(value = SerialMapD.class, name = "SerialMapD"),
    @Type(value = SerialMapdd.class, name = "SerialMapdd"),
    @Type(value = SerialMapDD.class, name = "SerialMapDD"),
    @Type(value = SerialMapTA.class, name = "SerialMapTA"),
    @Type(value = SerialMapVolume.class, name = "SerialMapVolume"),
    @Type(value = SerialTA.class, name = "SerialTA"),
    @Type(value = SerialVolume.class, name = "SerialVolume"),
    @Type(value = TwoDimd.class, name = "TwoDimd"),
    @Type(value = TwoDimD.class, name = "TwoDimD")
})
public abstract class SerialObject {

    protected SerialObject() {
        super();
    }

}
