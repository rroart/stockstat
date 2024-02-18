package roart.action;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.common.config.ConfigConstants;
import roart.common.constants.Constants;
import roart.common.leader.MyLeader;
import roart.common.leader.impl.MyLeaderFactory;
import roart.component.model.ComponentData;
import roart.db.dao.IclijDbDao;
import roart.iclij.config.IclijConfig;
import roart.iclij.config.IclijXMLConfig;
import roart.iclij.service.ControlService;

public class MainAction extends Action {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    private IclijDbDao dbDao;

    private IclijConfig iclijConfig;

    private ControlService controlService;

    public MainAction(IclijConfig iclijConfig, IclijDbDao dbDao) {
        this.iclijConfig = iclijConfig;
        this.dbDao = dbDao;
        this.controlService = new ControlService(iclijConfig);
        String updateDB = System.getProperty("updatedata");
        boolean doUpdateDB = !"false".equals(updateDB);
        if (doUpdateDB) {
            // not yet addIfNotContaining(updateDBACtion);
        }
        if (getGoals().isEmpty()) {
            addGoals();
        }
    }

    @SuppressWarnings("squid:S2189")
    @Override
    public void goal(Action parent, ComponentData param, Integer priority, IclijConfig iclijConfig) throws InterruptedException {
        IclijConfig config = iclijConfig;
        if (!config.wantsIclijSchedule()) {
            return;
        }
        /*
        log.debug("Start");
        ControlService srv = new ControlService(iclijConfig);
        boolean noException = false;
        while (noException == false) {
            try {
                srv.getAndSetCoreConfig();
                noException = true;
            } catch (Exception e) {
                log.debug("Ex, sleep 15");
                Thread.sleep(15000);
            }
        }
        log.debug("Got config");
        */
        /*
        String hostname = "localhost";
        try {
            hostname = InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        */
        if (true) {
            if (getGoals().isEmpty()) {
                try {
                    //Thread.sleep(3600 * 1000);
                } catch (Exception e) {
                }
                //addGoals();
                //addIfNotContaining(updateDBACtion);
            } else {
                for (int pri = 0; pri < 100; pri += 10) {
                    for (Action anAction : getGoals()) {
                        MarketAction action = (MarketAction) anAction;
                        action.goal(this, param, pri, iclijConfig);
                    }
                }
            }
            /*
            try {
                Thread.sleep(3600 * 1000);
            } catch (Exception e) {
            }
            */
        }
    }

    private void addGoals() {
        if (iclijConfig.wantsMachineLearningAutorun() ) {        
            getGoals().add(new MachineLearningAction(iclijConfig, dbDao));
        }
        if (iclijConfig.wantsFindProfitAutorun() ) {        
            getGoals().add(new FindProfitAction(iclijConfig, dbDao));
        }
        if (iclijConfig.wantsEvolveAutorun() ) {        
            getGoals().add(new EvolveAction(iclijConfig, dbDao));
        }
        if (iclijConfig.wantsImproveProfitAutorun()) {        
            getGoals().add(new ImproveProfitAction(dbDao, iclijConfig));
        }
        if (iclijConfig.wantsImproveFilterAutorun()) {        
            getGoals().add(new ImproveFilterAction(iclijConfig, dbDao));
        }
        if (iclijConfig.wantsImproveAbovebelowAutorun()) {        
            getGoals().add(new ImproveAboveBelowAction(iclijConfig, dbDao));
        }
        if (iclijConfig.wantsCrosstestAutorun()) {        
            getGoals().add(new CrossTestAction(iclijConfig, dbDao));
        }
        if (iclijConfig.wantsSimulateInvestAutorun()) {        
            getGoals().add(new SimulateInvestAction(dbDao, iclijConfig));
        }
        if (iclijConfig.wantsImproveSimulateInvestAutorun()) {        
            getGoals().add(new ImproveSimulateInvestAction(iclijConfig, dbDao));
        }
        if (iclijConfig.wantsDatasetAutorun()) {        
            getGoals().add(new DatasetAction(iclijConfig, dbDao));
        }
    }

    public static boolean wantsGoals(IclijConfig iclijConfig) {
        return iclijConfig.wantsMachineLearningAutorun()
                || iclijConfig.wantsFindProfitAutorun() 
                || iclijConfig.wantsEvolveAutorun()      
                || iclijConfig.wantsImproveProfitAutorun()      
                || iclijConfig.wantsImproveFilterAutorun()      
                || iclijConfig.wantsImproveAbovebelowAutorun()      
                || iclijConfig.wantsCrosstestAutorun()       
                || iclijConfig.wantsSimulateInvestAutorun()       
                || iclijConfig.wantsImproveSimulateInvestAutorun()       
                || iclijConfig.wantsDatasetAutorun();
    }

    public void addIfNotContaining(Action action) {
        if (!getGoals().contains(action)) {
            getGoals().add(action);
        }

    }
}
