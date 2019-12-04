package roart.component;

import java.util.ArrayList;
import java.util.List;

import roart.common.pipeline.PipelineConstants;

public class CrosstestComponentFactory extends ComponentFactory {

    public Component factory(String component) {
        switch (component) {
        case PipelineConstants.AGGREGATORRECOMMENDERINDICATOR:
            return new CrosstestComponentRecommender();
        case PipelineConstants.PREDICTOR:
            return new CrosstestComponentPredictor();
        case PipelineConstants.MLMACD:
            return new CrosstestComponentMLMACD();
        case PipelineConstants.MLRSI:
            return new CrosstestComponentMLRSI();
        case PipelineConstants.MLATR:
            return new CrosstestComponentMLATR();
        case PipelineConstants.MLCCI:
            return new CrosstestComponentMLCCI();
        case PipelineConstants.MLSTOCH:
            return new CrosstestComponentMLSTOCH();
        case PipelineConstants.MLMULTI:
            return new CrosstestComponentMLMulti();
        case PipelineConstants.MLINDICATOR:
            return new CrosstestComponentMLIndicator();
        default:
            return null;
        }
    }

    public List<Component> getAllComponents() {
        List<Component> list = new ArrayList<>();
        list.add(new CrosstestComponentRecommender());
        list.add(new CrosstestComponentPredictor());
        list.add(new CrosstestComponentMLMACD());
        list.add(new CrosstestComponentMLRSI());
        list.add(new CrosstestComponentMLATR());
        list.add(new CrosstestComponentMLCCI());
        list.add(new CrosstestComponentMLSTOCH());
        list.add(new CrosstestComponentMLMulti());
        list.add(new CrosstestComponentMLIndicator());
        return list;
    }
}
