package roart.component;

import java.util.ArrayList;
import java.util.List;

import roart.common.pipeline.PipelineConstants;

public class FindProfitComponentFactory extends ComponentFactory {

    public Component factory(String component) {
        switch (component) {
        case PipelineConstants.AGGREGATORRECOMMENDERINDICATOR:
            return new FindProfitComponentRecommender();
        case PipelineConstants.PREDICTORSLSTM:
            return new FindProfitComponentPredictor();
        case PipelineConstants.MLMACD:
            return new FindProfitComponentMLMACD();
        case PipelineConstants.MLRSI:
            return new FindProfitComponentMLRSI();
        case PipelineConstants.MLATR:
            return new FindProfitComponentMLATR();
        case PipelineConstants.MLCCI:
            return new FindProfitComponentMLCCI();
        case PipelineConstants.MLSTOCH:
            return new FindProfitComponentMLSTOCH();
        case PipelineConstants.MLMULTI:
            return new FindProfitComponentMLMulti();
        case PipelineConstants.MLINDICATOR:
            return new FindProfitComponentMLIndicator();
        default:
            return null;
        }
    }

    public List<Component> getAllComponents() {
        List<Component> list = new ArrayList<>();
        list.add(new FindProfitComponentRecommender());
        list.add(new FindProfitComponentPredictor());
        list.add(new FindProfitComponentMLMACD());
        list.add(new FindProfitComponentMLRSI());
        list.add(new FindProfitComponentMLATR());
        list.add(new FindProfitComponentMLCCI());
        list.add(new FindProfitComponentMLSTOCH());
        list.add(new FindProfitComponentMLMulti());
        list.add(new FindProfitComponentMLIndicator());
        return list;
    }

}
