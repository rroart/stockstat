package roart.action;

import roart.constants.IclijConstants;

public class ActionFactory {
    public static MarketAction get(String action) {
        switch (action) {
        case IclijConstants.FINDPROFIT:
            return new FindProfitAction();
        case IclijConstants.IMPROVEPROFIT:
            return new ImproveProfitAction();
        case IclijConstants.MACHINELEARNING:
            return new MachineLearningAction();
        case IclijConstants.EVOLVE:
            return new EvolveAction();
        case IclijConstants.DATASET:
            return new DatasetAction();
        case IclijConstants.CROSSTEST:
            return new CrossTestAction();
        case IclijConstants.IMPROVEFILTER:
            return new ImproveFilterAction();
        case IclijConstants.IMPROVEABOVEBELOW:
            return new ImproveAboveBelowAction();
        case IclijConstants.SIMULATEINVEST:
            return new SimulateInvestAction();
        case IclijConstants.IMPROVESIMULATEINVEST:
            return new ImproveSimulateInvestAction();
        case IclijConstants.IMPROVEAUTOSIMULATEINVEST:
            return new ImproveAutoSimulateInvestAction();
        default:
             return null;
        }
    }
}
