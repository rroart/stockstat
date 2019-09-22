package roart.component;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.action.Action;
import roart.action.ServiceAction;
import roart.common.pipeline.PipelineConstants;

public abstract class ComponentFactory {
    private Logger log = LoggerFactory.getLogger(this.getClass());

    public abstract Component factory(String component);
    
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

    public abstract List<Component> getAllComponents();
    
}
