package roart.component;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.action.Action;
import roart.action.ServiceAction;
import roart.common.pipeline.PipelineConstants;

public class ComponentFactory {
    private Logger log = LoggerFactory.getLogger(this.getClass());

    public static Component factory(String component) {
        switch (component) {
        case PipelineConstants.AGGREGATORRECOMMENDERINDICATOR:
            return new ComponentRecommender();
        case PipelineConstants.PREDICTORSLSTM:
            return new ComponentPredictor();
        case PipelineConstants.MLMACD:
            return new ComponentMLMACD();
        case PipelineConstants.MLRSI:
            return new ComponentMLRSI();
        case PipelineConstants.MLATR:
            return new ComponentMLATR();
        case PipelineConstants.MLCCI:
            return new ComponentMLCCI();
        case PipelineConstants.MLSTOCH:
            return new ComponentMLSTOCH();
        case PipelineConstants.MLMULTI:
            return new ComponentMLMulti();
        case PipelineConstants.MLINDICATOR:
            return new ComponentMLIndicator();
        default:
            return null;
        }
    }

    @Deprecated
    public ServiceAction factory(String market, String component) {
        ServiceAction serviceAction = null;
        switch (component) {
        case PipelineConstants.AGGREGATORRECOMMENDERINDICATOR:
            serviceAction = new ServiceAction(market, ServiceAction.Task.RECOMMENDER);
            break;
        case PipelineConstants.PREDICTORSLSTM:
            serviceAction = new ServiceAction(market, ServiceAction.Task.PREDICTOR);
            break;
        case PipelineConstants.MLMACD:
            serviceAction = new ServiceAction(market, ServiceAction.Task.MLMACD);
            break;
        case PipelineConstants.MLINDICATOR:
            serviceAction = new ServiceAction(market, ServiceAction.Task.MLINDICATOR);
            break;
        default:
            log.error("Non-existing component");
        }
        return serviceAction;
    }

    public static List<Component> getAllComponents() {
        List<Component> list = new ArrayList<>();
        list.add(new ComponentRecommender());
        list.add(new ComponentPredictor());
        list.add(new ComponentMLMACD());
        list.add(new ComponentMLRSI());
        list.add(new ComponentMLATR());
        list.add(new ComponentMLCCI());
        list.add(new ComponentMLSTOCH());
        list.add(new ComponentMLMulti());
        list.add(new ComponentMLIndicator());
        return list;
    }

}
