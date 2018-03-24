package roart.action;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.service.ControlService;

public class TestRecommenderAction extends Action {

    private Logger log = LoggerFactory.getLogger(this.getClass());
    
    @Override
    public void goal(Action parent) throws InterruptedException {
        ControlService srv = new ControlService();
        srv.conf.setMarket("cboevol");
        srv.conf.configValueMap.put("predictors[@enable]", Boolean.FALSE);
        srv.conf.configValueMap.put("machinelearning[@enable]", Boolean.FALSE);
        srv.getContent();
        System.out.println("goaled");

    }

}
