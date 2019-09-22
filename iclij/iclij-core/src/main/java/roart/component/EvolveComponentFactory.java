package roart.component;

import java.util.ArrayList;
import java.util.List;

import roart.common.pipeline.PipelineConstants;

public class EvolveComponentFactory extends ComponentFactory {

    public Component factory(String component) {
        switch (component) {
        case PipelineConstants.AGGREGATORRECOMMENDERINDICATOR:
            return new EvolveComponentRecommender();
        case PipelineConstants.PREDICTORSLSTM:
            return new EvolveComponentPredictor();
        case PipelineConstants.MLMACD:
            return new EvolveComponentMLMACD();
        case PipelineConstants.MLRSI:
            return new EvolveComponentMLRSI();
        case PipelineConstants.MLATR:
            return new EvolveComponentMLATR();
        case PipelineConstants.MLCCI:
            return new EvolveComponentMLCCI();
        case PipelineConstants.MLSTOCH:
            return new EvolveComponentMLSTOCH();
        case PipelineConstants.MLMULTI:
            return new EvolveComponentMLMulti();
        case PipelineConstants.MLINDICATOR:
            return new EvolveComponentMLIndicator();
        default:
            return null;
        }
    }

    public List<Component> getAllComponents() {
        List<Component> list = new ArrayList<>();
        list.add(new EvolveComponentRecommender());
        list.add(new EvolveComponentPredictor());
        list.add(new EvolveComponentMLMACD());
        list.add(new EvolveComponentMLRSI());
        list.add(new EvolveComponentMLATR());
        list.add(new EvolveComponentMLCCI());
        list.add(new EvolveComponentMLSTOCH());
        list.add(new EvolveComponentMLMulti());
        list.add(new EvolveComponentMLIndicator());
        return list;
    }
}
