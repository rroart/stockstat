package roart.component;

import java.util.ArrayList;
import java.util.List;

import roart.common.pipeline.PipelineConstants;

public class DatasetComponentFactory extends ComponentFactory {

    public Component factory(String component) {
        switch (component) {
        case PipelineConstants.DATASET:
            return new DatasetComponent();
            /*
        case PipelineConstants.AGGREGATORRECOMMENDERINDICATOR:
            return new DatasetComponentRecommender();
        case PipelineConstants.PREDICTOR:
            return new DatasetComponentPredictor();
        case PipelineConstants.MLMACD:
            return new DatasetComponentMLMACD();
        case PipelineConstants.MLRSI:
            return new DatasetComponentMLRSI();
        case PipelineConstants.MLATR:
            return new DatasetComponentMLATR();
        case PipelineConstants.MLCCI:
            return new DatasetComponentMLCCI();
        case PipelineConstants.MLSTOCH:
            return new DatasetComponentMLSTOCH();
        case PipelineConstants.MLMULTI:
            return new DatasetComponentMLMulti();
        case PipelineConstants.MLINDICATOR:
            return new DatasetComponentMLIndicator();
            */
        default:
            return null;
        }
    }

    public List<Component> getAllComponents() {
        List<Component> list = new ArrayList<>();
        list.add(new DatasetComponent());
        /*
        list.add(new DatasetComponentRecommender());
        list.add(new DatasetComponentPredictor());
        list.add(new DatasetComponentMLMACD());
        list.add(new DatasetComponentMLRSI());
        list.add(new DatasetComponentMLATR());
        list.add(new DatasetComponentMLCCI());
        list.add(new DatasetComponentMLSTOCH());
        list.add(new DatasetComponentMLMulti());
        list.add(new DatasetComponentMLIndicator());
        */
        return list;
    }
}
