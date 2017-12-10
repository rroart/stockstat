package roart.action;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import roart.config.ConfigConstants;
import roart.config.IclijXMLConfig;
import roart.service.ControlService;

public class MainAction extends Action {
    public static Queue<Action> goals = new LinkedList<>();
    public static Action updateDBACtion;
    @Override
    public void goal() throws InterruptedException {
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
                    addIfNotContaining(updateDBACtion);
                }
                if (goals.isEmpty()) {
                    goals.add(new FindProfitAction());
                    goals.add(new ImproveProfitAction());  
                }
            }
            if (goals.isEmpty()) {
                try {
                    Thread.sleep(3600 * 1000);
                } catch (Exception e) {
                }
                addIfNotContaining(updateDBACtion);
            } else {
                Action action = goals.poll();
                action.goal();
            }
        }
    }
    
    public void addIfNotContaining(Action action) {
        if (!goals.contains(action)) {
            goals.add(action);
        }

    }
}
