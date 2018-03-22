package roart.action;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.component.ComponentFactory;
import roart.config.IclijConfig;
import roart.config.IclijXMLConfig;
import roart.config.Market;
import roart.db.IclijDbDao;
import roart.model.MemoryItem;
import roart.pipeline.PipelineConstants;
import roart.util.Constants;
import roart.util.TimeUtil;

public class UpdateDBAction extends Action {
    
    private Logger log = LoggerFactory.getLogger(this.getClass());
    
    private List<String> getComponents(IclijConfig config) {
        List<String> components = new ArrayList<>();
        if (config.wantsRecommender()) {
            components.add(PipelineConstants.AGGREGATORRECOMMENDERINDICATOR);
        }
        if (config.wantsPredictor()) {
            components.add(PipelineConstants.PREDICTORSLSTM);
        }
        if (config.wantsMLIndicator()) {
            components.add(PipelineConstants.MLMACD);
        }
        if (config.wantsMLIndicator()) {
            components.add(PipelineConstants.MLINDICATOR);
        }
        return components;
    }

    @Override
    public void goal(Action parent) throws InterruptedException {
        List<Market> markets = getMarkets();
        List<MemoryItem> toCheck = findMarketComponentsToCheck(markets);
        Queue<Action> goals = getGoals(toCheck);
        if (!toCheck.isEmpty() && parent != null) {
            parent.getGoals().addAll(goals);
            parent.getGoals().add(new FindProfitAction());
        }
    }

    private List<Market> getMarkets() {
        IclijConfig instance = IclijXMLConfig.getConfigInstance();
        List<Market> markets = null;
        try { 
            markets = IclijXMLConfig.getMarkets(instance);
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        return markets;
    }

    private Queue<Action> getGoals(List<MemoryItem> toCheck) {
        Queue<Action> goals = new LinkedList<>();
        for (MemoryItem memoryItem : toCheck) {
            String market = memoryItem.getMarket();
            log.info("Will update {} {} from {} with time spent {}", market, memoryItem.getComponent(), memoryItem.getRecord(), memoryItem.getUsedsec());
            String component = memoryItem.getComponent();
            Action serviceAction = new ComponentFactory().factory(market, component);
            if (serviceAction != null) {
                goals.add(serviceAction);
            }
        }
        return goals;
    }

    private List<MemoryItem> findMarketComponentsToCheck(List<Market> markets) {
        IclijConfig instance = IclijXMLConfig.getConfigInstance();
        List<MemoryItem> toCheck = new ArrayList<>();
        for (Market market : markets) {
            List<MemoryItem> marketMemory = null;
            try {
                marketMemory = IclijDbDao.getAll(market.getMarket());
            } catch (Exception e) {
                log.error(Constants.EXCEPTION, e);
            }
            if (marketMemory == null) {
                log.error("Marketmemory null for {}", market.getMarket());
                continue;
            }
            for (String component : getComponents(instance)) {
                List<MemoryItem> marketComponents = marketMemory.stream().filter(m -> component.equals(m.getComponent())).collect(Collectors.toList());
                Collections.sort(marketComponents, (o1, o2) -> (o2.getRecord().compareTo(o1.getRecord())));
                MemoryItem last = marketComponents.get(0);
                long time = TimeUtil.daysSince(last.getRecord());
                if (time > market.getTime()) {
                    toCheck.add(last);
                }
            }
        }
        for (MemoryItem memoryItem : toCheck) {
            if (memoryItem.getUsedsec() == null) {
                memoryItem.setUsedsec(0);
            }
        }
        Collections.sort(toCheck, (o1, o2) -> (o2.getUsedsec().compareTo(o1.getUsedsec())));
        return toCheck;
    }

    public Queue<Action> findAllMarketComponentsToCheck(String marketName, LocalDate date, int days, boolean save, IclijConfig config) {
        List<Market> markets = getMarkets();
        Queue<Action> goals = new LinkedList<>();
        for (Market market : markets) {
            if (!market.getMarket().equals(marketName)) {
                continue;
            }
            for (String component : getComponents(config)) {
                ServiceAction serviceAction = new ComponentFactory().factory(market.getMarket(), component);
                if (serviceAction != null) {
                    serviceAction.setDate(date);
                    serviceAction.setDays(days);
                    serviceAction.setSave(save);
                    goals.add(serviceAction);
                }
            }
        }
        return goals;
    }

}
