package roart.component;

import java.util.ArrayList;
import java.util.List;

import roart.common.pipeline.PipelineConstants;

public class ImproveFilterComponentFactory extends ComponentFactory {

    public Component factory(String component) {
        switch (component) {
        case PipelineConstants.AGGREGATORRECOMMENDERINDICATOR:
            return new ImproveFilterComponentRecommender();
        case PipelineConstants.PREDICTOR:
            return new ImproveFilterComponentPredictor();
        case PipelineConstants.MLMACD:
            return new ImproveFilterComponentMLMACD();
        case PipelineConstants.MLRSI:
            return new ImproveFilterComponentMLRSI();
        case PipelineConstants.MLATR:
            return new ImproveFilterComponentMLATR();
        case PipelineConstants.MLCCI:
            return new ImproveFilterComponentMLCCI();
        case PipelineConstants.MLSTOCH:
            return new ImproveFilterComponentMLSTOCH();
        case PipelineConstants.MLMULTI:
            return new ImproveFilterComponentMLMulti();
        case PipelineConstants.MLINDICATOR:
            return new ImproveFilterComponentMLIndicator();
        default:
            return null;
        }
    }

    public List<Component> getAllComponents() {
        List<Component> list = new ArrayList<>();
        list.add(new ImproveFilterComponentRecommender());
        list.add(new ImproveFilterComponentPredictor());
        list.add(new ImproveFilterComponentMLMACD());
        list.add(new ImproveFilterComponentMLRSI());
        list.add(new ImproveFilterComponentMLATR());
        list.add(new ImproveFilterComponentMLCCI());
        list.add(new ImproveFilterComponentMLSTOCH());
        list.add(new ImproveFilterComponentMLMulti());
        list.add(new ImproveFilterComponentMLIndicator());
        return list;
    }

}
