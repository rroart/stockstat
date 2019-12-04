package roart.component;

import java.util.ArrayList;
import java.util.List;

import roart.common.pipeline.PipelineConstants;

public class MachineLearningComponentFactory extends ComponentFactory {

    public Component factory(String component) {
        switch (component) {
        case PipelineConstants.AGGREGATORRECOMMENDERINDICATOR:
            return new MachineLearningComponentRecommender();
        case PipelineConstants.PREDICTOR:
            return new MachineLearningComponentPredictor();
        case PipelineConstants.MLMACD:
            return new MachineLearningComponentMLMACD();
        case PipelineConstants.MLRSI:
            return new MachineLearningComponentMLRSI();
        case PipelineConstants.MLATR:
            return new MachineLearningComponentMLATR();
        case PipelineConstants.MLCCI:
            return new MachineLearningComponentMLCCI();
        case PipelineConstants.MLSTOCH:
            return new MachineLearningComponentMLSTOCH();
        case PipelineConstants.MLMULTI:
            return new MachineLearningComponentMLMulti();
        case PipelineConstants.MLINDICATOR:
            return new MachineLearningComponentMLIndicator();
        default:
            return null;
        }
    }

    public List<Component> getAllComponents() {
        List<Component> list = new ArrayList<>();
        list.add(new MachineLearningComponentRecommender());
        list.add(new MachineLearningComponentPredictor());
        list.add(new MachineLearningComponentMLMACD());
        list.add(new MachineLearningComponentMLRSI());
        list.add(new MachineLearningComponentMLATR());
        list.add(new MachineLearningComponentMLCCI());
        list.add(new MachineLearningComponentMLSTOCH());
        list.add(new MachineLearningComponentMLMulti());
        list.add(new MachineLearningComponentMLIndicator());
        return list;
    }

}
