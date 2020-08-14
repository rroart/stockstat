package roart.iclij.factory.actioncomponentconfig;

import java.util.List;

import roart.iclij.config.IclijConfigConstants;
import roart.iclij.model.config.ActionComponentConfig;

public abstract class ActionComponentConfigFactory {
    public static ActionComponentConfigFactory factoryfactory(String action) {
        switch (action) {
        case IclijConfigConstants.CROSSTEST:
            return new CrossTestActionComponentConfigFactory();
        case IclijConfigConstants.DATASET:
            return new DatasetActionComponentConfigFactory();
        case IclijConfigConstants.EVOLVE:
            return new EvolveActionComponentConfigFactory();
        case IclijConfigConstants.FINDPROFIT:
            return new FindProfitActionComponentConfigFactory();
        case IclijConfigConstants.IMPROVEFILTER:
            return new ImproveFilterActionComponentConfigFactory();
        case IclijConfigConstants.IMPROVEPROFIT:
            return new ImproveProfitActionComponentConfigFactory();
        case IclijConfigConstants.IMPROVEABOVEBELOW:
            return new ImproveAbovebelowActionComponentConfigFactory();
        case IclijConfigConstants.MACHINELEARNING:
            return new MachineLearningActionComponentConfigFactory();
        case IclijConfigConstants.SIMULATEINVEST:
            return new SimulateInvestActionComponentConfigFactory();
        case IclijConfigConstants.IMPROVESIMULATEINVEST:
            return new ImproveSimulateInvestActionComponentConfigFactory();
        default:
            return null;
        }                
    }
    
    public abstract ActionComponentConfig factory(String component);

    public abstract List<ActionComponentConfig> getAllComponents();

}
