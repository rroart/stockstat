package roart.action;

import roart.service.ControlService;

public class TestRecommenderAction extends Action {

    @Override
    public void goal() throws InterruptedException {
        ControlService srv = new ControlService();
        srv.conf.setMarket("cboevol");
        srv.conf.configValueMap.put("predictors[@enable]", Boolean.FALSE);
        srv.conf.configValueMap.put("machinelearning[@enable]", Boolean.FALSE);
        srv.getContent();
        System.out.println("goaled");

    }

}
