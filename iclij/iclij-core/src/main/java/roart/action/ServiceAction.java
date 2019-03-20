package roart.action;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.common.constants.Constants;
import roart.component.model.ComponentInput;
import roart.component.model.ComponentData;
import roart.service.ControlService;
import roart.service.MLService;
import roart.service.PredictorService;
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
    public void goal(Action parent, ComponentData param) throws InterruptedException {
        log.info("At {} : Updating {} {}", new Date(), market, task);
        /*
        ControlService srv = new ControlService();
        srv.getConfig();
        srv.conf.setMarket(market);
        ComponentInput input = new ComponentInput(market, date, 0, save, true);
        ComponentData param = new ComponentData(input);
        param.setService(srv);
        param.setFutureDate(date);
        */
        try {
            switch (task) {
            case RECOMMENDER:
                setMemory(new RecommenderService().doRecommender(param, new ArrayList<>()));
                break;
            case PREDICTOR:
                setMemory(new PredictorService().doPredict(param));
                break;
            case MLMACD:
                setMemory(new MLService().doMLMACD(param));
                break;
            case MLINDICATOR:
                setMemory(new MLService().doMLIndicator(param));
                break;
            }
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
    }
}



