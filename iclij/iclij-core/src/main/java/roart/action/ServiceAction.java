package roart.action;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.util.Constants;
import roart.util.ServiceUtil;

public class ServiceAction extends Action {
    private Logger log = LoggerFactory.getLogger(this.getClass());
    
    public enum Task { RECOMMENDER, PREDICTOR, MLMACD, MLINDICATOR }

    private String market;
    
    private Task task;

    private LocalDate date;
    
    private Integer days;
    
    private boolean save = false;

    public ServiceAction(String market, Task task) {
        this.market = market;
        this.task = task;
    }
    
    public String getMarket() {
        return market;
    }
    
    public void setMarket(String market) {
        this.market = market;
    }
    
    public Task getTask() {
        return task;
    }
    
    public void setTask(Task task) {
        this.task = task;
    }
    
    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public Integer getDays() {
        return days;
    }

    public void setDays(Integer days) {
        this.days = days;
    }

    public boolean isSave() {
        return save;
    }

    public void setSave(boolean save) {
        this.save = save;
    }

    @Override
    public void goal(Action parent) throws InterruptedException {
        log.info("At {} : Updating {} {}", new Date(), market, task);
        try {
            switch (task) {
            case RECOMMENDER:
                setMemory(ServiceUtil.doRecommender(market, days, null, true, new ArrayList<>(), true));
                break;
            case PREDICTOR:
                setMemory(ServiceUtil.doPredict(market, days, null, true, true));
                break;
            case MLMACD:
                setMemory(ServiceUtil.doMLMACD(market, days, null, true, true));
                break;
            case MLINDICATOR:
                setMemory(ServiceUtil.doMLIndicator(market, days, null, true, true));
                break;
            }
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
    }
}



