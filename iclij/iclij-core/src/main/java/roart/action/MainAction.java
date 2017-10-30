package roart.action;

import java.util.ArrayList;
import java.util.List;

import roart.config.ConfigConstants;
import roart.service.ControlService;

public class MainAction extends Action {
    @Override
    public void goal() throws InterruptedException {
        List<Action> goals = new ArrayList<>();
        goals.add(new FindProfitAction());
        goals.add(new ImproveProfitAction());
        System.out.println("start");
        ControlService srv = new ControlService();
        boolean noException = false;
        while (noException == false) {
        try {
        srv.getConfig();
        noException = true;
        System.out.println("got");
        } catch (Exception e) {
            System.out.println("Ex, sleep 15");
            Thread.sleep(15000);
        }
        }
        srv.conf.setMarket("cboevol");
        srv.conf.configValueMap.put(ConfigConstants.PREDICTORS, Boolean.FALSE);
        srv.conf.configValueMap.put(ConfigConstants.MACHINELEARNING, Boolean.FALSE);
        srv.getContent();
        System.out.println("goaled");
    }
}
