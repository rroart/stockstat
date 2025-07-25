package roart.common.pipeline.data;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    @Type(value = SerialKeyValue.class, name = "SerialKeyValue"),
    @Type(value = SerialList.class, name = "SerialList"),
    @Type(value = SerialListMap.class, name = "SerialListMap"),
    @Type(value = SerialListMapPlain.class, name = "SerialListMapPlain"),
    @Type(value = SerialListPlain.class, name = "SerialListPlain"),
    @Type(value = SerialListSimulateStock.class, name = "SerialListSimulateStock"),
    @Type(value = SerialListStockHistory.class, name = "SerialListStockHistory"),
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
    @Type(value = SerialMapL.class, name = "SerialMapL"),
    @Type(value = SerialMapTA.class, name = "SerialMapTA"),
    @Type(value = SerialMapVolume.class, name = "SerialMapVolume"),
    @Type(value = SerialNeuralNetConfig.class, name = "SerialNeuralNetworkConfig"),
    @Type(value = SerialPair.class, name = "SerialPair"),
    @Type(value = SerialPairPlain.class, name = "SerialPairPlain"),
    @Type(value = SerialScoreChromosome.class, name = "SerialScoreChromosome"),
    @Type(value = SerialSimulateStock.class, name = "SerialSimulateStock"),
    @Type(value = SerialStockHistory.class, name = "SerialStockHistory"),
    @Type(value = SerialTA.class, name = "SerialTA"),
    @Type(value = SerialVolume.class, name = "SerialVolume"),
    @Type(value = TwoDimd.class, name = "TwoDimd"),
    @Type(value = TwoDimD.class, name = "TwoDimD")
})
public abstract class SerialObject {
    protected Logger log = LoggerFactory.getLogger(this.getClass());

    protected SerialObject() {
        super();
    }

}
