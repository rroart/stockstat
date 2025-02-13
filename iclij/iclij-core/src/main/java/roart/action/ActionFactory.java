package roart.action;

import roart.constants.IclijConstants;
import roart.db.dao.IclijDbDao;
import roart.iclij.config.IclijConfig;

public class ActionFactory {
    public static MarketAction get(String action, IclijDbDao dbDao, IclijConfig iclijConfig) {
        switch (action) {
        case IclijConstants.FINDPROFIT:
            return new FindProfitAction(iclijConfig);
        case IclijConstants.IMPROVEPROFIT:
            return new ImproveProfitAction(iclijConfig);
        case IclijConstants.MACHINELEARNING:
            return new MachineLearningAction(iclijConfig);
        case IclijConstants.EVOLVE:
            return new EvolveAction(iclijConfig);
        case IclijConstants.DATASET:
            return new DatasetAction(iclijConfig);
        case IclijConstants.CROSSTEST:
            return new CrossTestAction(iclijConfig);
        case IclijConstants.IMPROVEFILTER:
            return new ImproveFilterAction(iclijConfig);
        case IclijConstants.IMPROVEABOVEBELOW:
            return new ImproveAboveBelowAction(iclijConfig);
        case IclijConstants.SIMULATEINVEST:
            return new SimulateInvestAction(iclijConfig);
        case IclijConstants.IMPROVESIMULATEINVEST:
            return new ImproveSimulateInvestAction(iclijConfig);
        case IclijConstants.IMPROVEAUTOSIMULATEINVEST:
            return new ImproveAutoSimulateInvestAction(iclijConfig);
        default:
             return null;
        }
    }
}
