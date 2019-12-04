package roart.component;

import java.util.ArrayList;
import java.util.List;

import roart.common.pipeline.PipelineConstants;

public class ImproveProfitComponentFactory extends ComponentFactory {

    public Component factory(String component) {
        switch (component) {
        case PipelineConstants.AGGREGATORRECOMMENDERINDICATOR:
            return new ImproveProfitComponentRecommender();
        case PipelineConstants.PREDICTOR:
            return new ImproveProfitComponentPredictor();
        case PipelineConstants.MLMACD:
            return new ImproveProfitComponentMLMACD();
        case PipelineConstants.MLRSI:
            return new ImproveProfitComponentMLRSI();
        case PipelineConstants.MLATR:
            return new ImproveProfitComponentMLATR();
        case PipelineConstants.MLCCI:
            return new ImproveProfitComponentMLCCI();
        case PipelineConstants.MLSTOCH:
            return new ImproveProfitComponentMLSTOCH();
        case PipelineConstants.MLMULTI:
            return new ImproveProfitComponentMLMulti();
        case PipelineConstants.MLINDICATOR:
            return new ImproveProfitComponentMLIndicator();
        default:
            return null;
        }
    }

    public List<Component> getAllComponents() {
        List<Component> list = new ArrayList<>();
        list.add(new ImproveProfitComponentRecommender());
        list.add(new ImproveProfitComponentPredictor());
        list.add(new ImproveProfitComponentMLMACD());
        list.add(new ImproveProfitComponentMLRSI());
        list.add(new ImproveProfitComponentMLATR());
        list.add(new ImproveProfitComponentMLCCI());
        list.add(new ImproveProfitComponentMLSTOCH());
        list.add(new ImproveProfitComponentMLMulti());
        list.add(new ImproveProfitComponentMLIndicator());
        return list;
    }

}
