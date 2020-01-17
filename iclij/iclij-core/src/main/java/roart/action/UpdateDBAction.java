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

import roart.iclij.config.IclijConfig;
import roart.iclij.config.Market;
import roart.iclij.config.MarketConfig;
import roart.common.constants.Constants;
import roart.common.util.TimeUtil;
import roart.common.pipeline.PipelineConstants;
import roart.component.ComponentFactory;
import roart.component.FindProfitComponentFactory;
import roart.component.model.ComponentData;
import roart.config.IclijXMLConfig;
import roart.db.IclijDbDao;
import roart.iclij.model.MemoryItem;
import roart.iclij.model.TimingItem;
import roart.util.ServiceUtil;

public class UpdateDBAction extends Action {
    
    private Logger log = LoggerFactory.getLogger(this.getClass());
    
    @Override
    public void goal(Action parent, ComponentData param, Integer priority) throws InterruptedException {
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
            Action serviceAction = new FindProfitComponentFactory().factory(market, component);
            if (serviceAction != null) {
                ((ServiceAction) serviceAction).setDays(0);
                ((ServiceAction) serviceAction).setSave(true);
                goals.add(serviceAction);
            }
        }
        return goals;
    }

    @Deprecated
    private List<MemoryItem> findMarketComponentsToCheck3(List<Market> markets) {
        IclijConfig instance = IclijXMLConfig.getConfigInstance();
        List<MemoryItem> toCheck = new ArrayList<>();
        for (Market market : markets) {
            List<TimingItem> marketMemory = null;
            try {
                marketMemory = IclijDbDao.getAllTiming(); //(market.getConfig().getMarket());
            } catch (Exception e) {
                log.error(Constants.EXCEPTION, e);
            }
            if (marketMemory == null) {
                log.error("Marketmemory null for {}", market.getConfig().getMarket());
                continue;
            }
            
        }
        return toCheck;
    }
    
    private List<MemoryItem> findMarketComponentsToCheck(List<Market> markets) {
        IclijConfig instance = IclijXMLConfig.getConfigInstance();
        List<MemoryItem> toCheck = new ArrayList<>();
        for (Market market : markets) {
            List<MemoryItem> marketMemory = null;
            try {
                marketMemory = IclijDbDao.getAll(market.getConfig().getMarket());
            } catch (Exception e) {
                log.error(Constants.EXCEPTION, e);
            }
            if (marketMemory == null) {
                log.error("Marketmemory null for {}", market.getConfig().getMarket());
                continue;
            }
            for (String component : ServiceUtil.getFindProfitComponents(instance, market.getConfig().getMarket())) {
                List<MemoryItem> marketComponents = marketMemory.stream().filter(m -> component.equals(m.getComponent())).collect(Collectors.toList());
                Collections.sort(marketComponents, (o1, o2) -> (o2.getRecord().compareTo(o1.getRecord())));
                if (marketComponents == null || marketComponents.isEmpty()) {
                    MemoryItem memoryItem = new MemoryItem();
                    memoryItem.setComponent(component);
                    memoryItem.setMarket(market.getConfig().getMarket());
                    toCheck.add(memoryItem);
                } else {
                    MemoryItem last = marketComponents.get(0);
                    long time = TimeUtil.daysSince(last.getRecord());
                    if (time > market.getConfig().getFindtime()) {
                        toCheck.add(last);
                    }
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

    public Queue<Action> findAllMarketComponentsToCheck(ComponentData param, int days, IclijConfig config, List<String> components) {
        days += param.getLoopoffset();
        List<Market> markets = getMarkets();
        Queue<Action> goals = new LinkedList<>();
        for (Market market : markets) {
            if (!market.getConfig().getMarket().equals(param.getMarket())) {
                continue;
            }
            Short startOffset = market.getConfig().getStartoffset();
            if (startOffset != null) {
                System.out.println("Using offset " + startOffset);
                log.info("Using offset {}", startOffset);
                days += startOffset;
            }
            for (String component : components) {
                ServiceAction serviceAction = new FindProfitComponentFactory().factory(market.getConfig().getMarket(), component);
                if (serviceAction != null) {
                    serviceAction.setDate(param.getFutureDate());
                    serviceAction.setDays(days);
                    serviceAction.setSave(param.isDoSave());
                    goals.add(serviceAction);
                }
            }
        }
        return goals;
    }

}
