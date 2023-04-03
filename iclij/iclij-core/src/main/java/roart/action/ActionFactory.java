package roart.action;

import roart.constants.IclijConstants;
import roart.db.dao.IclijDbDao;

public class ActionFactory {
    public static MarketAction get(String action, IclijDbDao dbDao) {
        switch (action) {
        case IclijConstants.FINDPROFIT:
            return new FindProfitAction(dbDao);
        case IclijConstants.IMPROVEPROFIT:
            return new ImproveProfitAction(dbDao);
        case IclijConstants.MACHINELEARNING:
            return new MachineLearningAction(dbDao);
        case IclijConstants.EVOLVE:
            return new EvolveAction(dbDao);
        case IclijConstants.DATASET:
            return new DatasetAction(dbDao);
        case IclijConstants.CROSSTEST:
            return new CrossTestAction(dbDao);
        case IclijConstants.IMPROVEFILTER:
            return new ImproveFilterAction(dbDao);
        case IclijConstants.IMPROVEABOVEBELOW:
            return new ImproveAboveBelowAction(dbDao);
        case IclijConstants.SIMULATEINVEST:
            return new SimulateInvestAction(dbDao);
        case IclijConstants.IMPROVESIMULATEINVEST:
            return new ImproveSimulateInvestAction(dbDao);
        case IclijConstants.IMPROVEAUTOSIMULATEINVEST:
            return new ImproveAutoSimulateInvestAction(dbDao);
        default:
             return null;
        }
    }
}
