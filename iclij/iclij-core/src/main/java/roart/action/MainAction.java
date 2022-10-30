package roart.action;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.common.config.ConfigConstants;
import roart.component.model.ComponentData;
import roart.iclij.config.IclijConfig;
import roart.iclij.config.IclijXMLConfig;
import roart.iclij.service.ControlService;

public class MainAction extends Action {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    @SuppressWarnings("squid:S2189")
    @Override
    public void goal(Action parent, ComponentData param, Integer priority) throws InterruptedException {
        IclijConfig config = IclijXMLConfig.getConfigInstance();
        if (!config.wantsIclijSchedule()) {
            return;
        }
        System.out.println("Start");
        ControlService srv = new ControlService();
        boolean noException = false;
        while (noException == false) {
            try {
                srv.getConfig();
                noException = true;
            } catch (Exception e) {
                System.out.println("Ex, sleep 15");
                Thread.sleep(15000);
            }
        }
        System.out.println("Got config");
        String updateDB = System.getProperty("updatedata");
        boolean doUpdateDB = !"false".equals(updateDB);
        boolean firstRun = true;
        while (true) {
            if (firstRun) {
                firstRun = false;
                if (doUpdateDB) {
                    // not yet addIfNotContaining(updateDBACtion);
                }
                if (getGoals().isEmpty()) {
                    addGoals();
                }
            }
            if (getGoals().isEmpty()) {
                try {
                    Thread.sleep(3600 * 1000);
                } catch (Exception e) {
                }
                addGoals();
                //addIfNotContaining(updateDBACtion);
            } else {
                for (int pri = 0; pri < 100; pri += 10) {
                    for (Action anAction : getGoals()) {
                        MarketAction action = (MarketAction) anAction;
                        action.goal(this, param, pri);
                    }
                }
            }
            try {
                Thread.sleep(3600 * 1000);
            } catch (Exception e) {
            }
        }
    }

    private void addGoals() {
        if (IclijXMLConfig.getConfigInstance().wantsMachineLearningAutorun() ) {        
            getGoals().add(new MachineLearningAction());
        }
        if (IclijXMLConfig.getConfigInstance().wantsFindProfitAutorun() ) {        
            getGoals().add(new FindProfitAction());
        }
        if (IclijXMLConfig.getConfigInstance().wantsEvolveAutorun() ) {        
            getGoals().add(new EvolveAction());
        }
        if (IclijXMLConfig.getConfigInstance().wantsImproveProfitAutorun()) {        
            getGoals().add(new ImproveProfitAction());
        }
        if (IclijXMLConfig.getConfigInstance().wantsImproveFilterAutorun()) {        
            getGoals().add(new ImproveFilterAction());
        }
        if (IclijXMLConfig.getConfigInstance().wantsImproveAbovebelowAutorun()) {        
            getGoals().add(new ImproveAboveBelowAction());
        }
        if (IclijXMLConfig.getConfigInstance().wantsCrosstestAutorun()) {        
            getGoals().add(new CrossTestAction());
        }
        if (IclijXMLConfig.getConfigInstance().wantsSimulateInvestAutorun()) {        
            getGoals().add(new SimulateInvestAction());
        }
        if (IclijXMLConfig.getConfigInstance().wantsImproveSimulateInvestAutorun()) {        
            getGoals().add(new ImproveSimulateInvestAction());
        }
        if (IclijXMLConfig.getConfigInstance().wantsDatasetAutorun()) {        
            getGoals().add(new DatasetAction());
        }
    }

    public static boolean wantsGoals() {
        return IclijXMLConfig.getConfigInstance().wantsMachineLearningAutorun()
                || IclijXMLConfig.getConfigInstance().wantsFindProfitAutorun() 
                || IclijXMLConfig.getConfigInstance().wantsEvolveAutorun()      
                || IclijXMLConfig.getConfigInstance().wantsImproveProfitAutorun()      
                || IclijXMLConfig.getConfigInstance().wantsImproveFilterAutorun()      
                || IclijXMLConfig.getConfigInstance().wantsImproveAbovebelowAutorun()      
                || IclijXMLConfig.getConfigInstance().wantsCrosstestAutorun()       
                || IclijXMLConfig.getConfigInstance().wantsSimulateInvestAutorun()       
                || IclijXMLConfig.getConfigInstance().wantsImproveSimulateInvestAutorun()       
                || IclijXMLConfig.getConfigInstance().wantsDatasetAutorun();
    }

    public void addIfNotContaining(Action action) {
        if (!getGoals().contains(action)) {
            getGoals().add(action);
        }

    }
}
