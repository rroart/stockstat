package roart.action;

import java.util.ArrayList;
import java.util.Date;

import roart.util.ServiceUtil;

public class ServiceAction extends Action {
    public enum Task { RECOMMENDER, PREDICTOR, MLMACD, MLINDICATOR };

    public String market;
    public Task task;

    public ServiceAction(String market, Task task) {
        this.market = market;
        this.task = task;
    }
    @Override
    public void goal() throws InterruptedException {
        System.out.println("At " + new Date() + " : Updating " + market + " " + task);
        try {
            switch (task) {
            case RECOMMENDER:
                ServiceUtil.doRecommender(market, 0, null, true, new ArrayList<>(), true);
                break;
            case PREDICTOR:
                ServiceUtil.doPredict(market, 0, null, true, true);
                break;
            case MLMACD:
                ServiceUtil.doMLMACD(market, 0, null, true, true);
                break;
            case MLINDICATOR:
                ServiceUtil.doMLIndicator(market, 0, null, true, true);
                break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}



