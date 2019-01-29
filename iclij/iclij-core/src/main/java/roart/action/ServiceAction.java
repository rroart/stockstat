package roart.action;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.common.constants.Constants;
import roart.service.MLService;
import roart.service.PredictionService;
import roart.service.RecommenderService;

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
                setMemory(new RecommenderService().doRecommender(market, days, null, save, new ArrayList<>(), true));
                break;
            case PREDICTOR:
                setMemory(new PredictionService().doPredict(market, days, null, save, true));
                break;
            case MLMACD:
                setMemory(new MLService().doMLMACD(market, days, null, save, true));
                break;
            case MLINDICATOR:
                setMemory(new MLService().doMLIndicator(market, days, null, save, true));
                break;
            }
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
    }
}



