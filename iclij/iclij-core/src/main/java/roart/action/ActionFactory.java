package roart.action;

import roart.constants.IclijConstants;
import roart.db.dao.IclijDbDao;
import roart.iclij.config.IclijConfig;

public class ActionFactory {
    public static MarketAction get(String action, IclijDbDao dbDao, IclijConfig iclijConfig) {
        switch (action) {
        case IclijConstants.FINDPROFIT:
            return new FindProfitAction(iclijConfig, dbDao);
        case IclijConstants.IMPROVEPROFIT:
            return new ImproveProfitAction(dbDao, iclijConfig);
        case IclijConstants.MACHINELEARNING:
            return new MachineLearningAction(iclijConfig, dbDao);
        case IclijConstants.EVOLVE:
            return new EvolveAction(iclijConfig, dbDao);
        case IclijConstants.DATASET:
            return new DatasetAction(iclijConfig, dbDao);
        case IclijConstants.CROSSTEST:
            return new CrossTestAction(iclijConfig, dbDao);
        case IclijConstants.IMPROVEFILTER:
            return new ImproveFilterAction(iclijConfig, dbDao);
        case IclijConstants.IMPROVEABOVEBELOW:
            return new ImproveAboveBelowAction(iclijConfig, dbDao);
        case IclijConstants.SIMULATEINVEST:
            return new SimulateInvestAction(dbDao, iclijConfig);
        case IclijConstants.IMPROVESIMULATEINVEST:
            return new ImproveSimulateInvestAction(iclijConfig, dbDao);
        case IclijConstants.IMPROVEAUTOSIMULATEINVEST:
            return new ImproveAutoSimulateInvestAction(iclijConfig, dbDao);
        default:
             return null;
        }
    }
}
