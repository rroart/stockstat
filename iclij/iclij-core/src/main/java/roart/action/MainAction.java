package roart.action;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.common.config.ConfigConstants;
import roart.component.model.ComponentData;
import roart.config.IclijXMLConfig;
import roart.service.ControlService;

public class MainAction extends Action {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    public static Action updateDBACtion;

    @SuppressWarnings("squid:S2189")
    @Override
    public void goal(Action parent, ComponentData param) throws InterruptedException {
        IclijXMLConfig.getConfigInstance();
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
        updateDBACtion = new UpdateDBAction();
        boolean firstRun = true;
        while (true) {
            if (firstRun) {
                firstRun = false;
                if (doUpdateDB) {
                    // not yet addIfNotContaining(updateDBACtion);
                }
                if (getGoals().isEmpty()) {
                    if (IclijXMLConfig.getConfigInstance().wantsFindProfitAutorun() ) {        
                        getGoals().add(new FindProfitAction());
                    }
                    if (IclijXMLConfig.getConfigInstance().wantsImproveProfitAutorun()) {        
                        getGoals().add(new ImproveProfitAction());
                    }
                }
            }
            if (getGoals().isEmpty()) {
                try {
                    Thread.sleep(3600 * 1000);
                } catch (Exception e) {
                }
                addIfNotContaining(updateDBACtion);
            } else {
                Action action = getGoals().poll();
                action.goal(this, param);
            }
        }
    }

    public void addIfNotContaining(Action action) {
        if (!getGoals().contains(action)) {
            getGoals().add(action);
        }

    }
}
