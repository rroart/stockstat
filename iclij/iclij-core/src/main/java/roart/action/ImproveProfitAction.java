package roart.action;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.iclij.config.IclijConfig;
import roart.action.FindProfitAction.MarketTime;
import roart.common.constants.Constants;
import roart.common.pipeline.PipelineConstants;
import roart.common.util.TimeUtil;
import roart.component.Component;
import roart.component.ComponentFactory;
import roart.component.model.ComponentInput;
import roart.component.model.ComponentData;
import roart.config.IclijXMLConfig;
import roart.config.Market;
import roart.config.MarketConfig;
import roart.config.MarketFilter;
import roart.constants.IclijConstants;
import roart.db.IclijDbDao;
import roart.iclij.model.IncDecItem;
import roart.iclij.model.MapList;
import roart.iclij.model.MemoryItem;
import roart.iclij.model.TimingItem;
import roart.service.ControlService;
import roart.service.model.ProfitData;
import roart.service.model.ProfitInputData;
import roart.util.ServiceUtil;

public class ImproveProfitAction extends Action {

    private Logger log = LoggerFactory.getLogger(this.getClass());
    
    @Override
    public void goal(Action parent, ComponentData param) {
        getMarkets(parent, new ComponentInput(IclijXMLConfig.getConfigInstance(), null, null, null, 0, true, false, new ArrayList<>(), new HashMap<>()));
    }

    public WebData getMarket(Action parent, ComponentData param, Market market) {
        List<Market> markets = new ArrayList<>();
        markets.add(market);
        return getMarkets(parent, param, markets, new ArrayList<>());
    }        
    
    public WebData getMarkets(Action parent, ComponentInput input) {
        List<TimingItem> timings = null;
        try {
            timings = TimingItem.getAll();
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        List<Market> markets = FindProfitAction.getMarkets();
        ComponentData param = null;
        try {
            param = ServiceUtil.getParam(input, 0);
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        return getMarkets(parent, param, markets, timings);
    }        
    
    private WebData getMarkets(Action parent, ComponentData paramTemplate, List<Market> markets, List<TimingItem> timings) {
	// test picks for aggreg recommend, predict etc
        // remember and make confidence
        // memory is with date, confidence %, inc/dec, semantic item
        // find recommended picks
        WebData myData = new WebData();
        myData.updateMap = new HashMap<>();
        myData.timingMap = new HashMap<>();
        
        IclijConfig config = paramTemplate.getInput().getConfig();
        //IclijXMLConfig.getConfigInstance();
        //instance.
        List<MarketComponentTime> marketTimes = new ArrayList<>();
        Map<String, ComponentData> componentDataMap = new HashMap<>();
        for (Market market : markets) {
            String marketName = market.getConfig().getMarket();
            ComponentInput input = new ComponentInput(config, null, marketName, null, 0, paramTemplate.getInput().isDoSave(), false, new ArrayList<>(), new HashMap<>());
            ComponentData param = null;
            try {
                param = ServiceUtil.getParam(input, 0);
            } catch (Exception e) {
                log.error(Constants.EXCEPTION, e);
            }
            param.setAction(IclijConstants.IMPROVEPROFIT);
            ControlService srv = new ControlService();
            //srv.getConfig();
            param.setService(srv);
            srv.conf.setMarket(market.getConfig().getMarket());
            List<String> stockDates = param.getService().getDates(marketName);
            if (stockDates == null || stockDates.isEmpty()) {
                continue;
            }
            componentDataMap.put(marketName, param);
            LocalDate olddate = LocalDate.now().minusDays(market.getConfig().getImprovetime()); // enddays?

            try {
                //param.setFuturedays(market.getFilter().getRecordage());
                //param.setDates(market.getFilter().getRecordage(), 0, TimeUtil.convertDate2(olddate));
            } catch (Exception e) {
                log.error(Constants.EXCEPTION, e);
            }
            List<IncDecItem> incdecitems = null;
            try {
                incdecitems = IncDecItem.getAll(marketName);
            } catch (Exception e) {
                log.error(Constants.EXCEPTION, e);
            }
            List<IncDecItem> currentIncDecs = null; // ServiceUtil.getCurrentIncDecs(olddate, incdecitems, market);
            if (currentIncDecs == null || currentIncDecs.isEmpty() || timings.isEmpty()) {
                List<String> componentList = ServiceUtil.getImproveProfitComponents(config);
                Map<String, Component> componentMap = FindProfitAction.getComponentMap(componentList, market);
                List<MarketComponentTime> marketTime = getList2(IclijConstants.IMPROVEPROFIT, componentMap, timings, market, param);
                marketTimes.addAll(marketTime);
            } else {
                int jj = 0;
            }
        }
        Collections.sort(marketTimes, (o1, o2) -> (Double.valueOf(o2.time).compareTo(Double.valueOf(o1.time))));
        for (MarketComponentTime marketTime : marketTimes) {
            log.info("MarketTime {}", marketTime);
        }
        /*
        for (MarketComponentTime marketTime : marketTimes) {
            myData.timingMap.put(marketTime.market.getConfig().getMarket(), marketTime.timings);
        }
        */
        for (MarketComponentTime marketTime : marketTimes) {
            if (marketTime.time == 0.0) {
                ComponentData param = componentDataMap.get(marketTime.market.getConfig().getMarket());
                getPicksFiltered(myData, param, config, marketTime);                
            }
        }
        for (MarketComponentTime marketTime : marketTimes) {
            if (marketTime.time > 0.0) {
                if (config.serverShutdownHour() != null) {
                    int shutdown = config.serverShutdownHour();
                    shutdown --;
                    LocalTime now = LocalTime.now();
                    int minutes = 60 * now.getHour() + now.getMinute();
                    minutes += marketTime.time;
                    if (minutes >= shutdown * 60) {
                        continue;
                    }
                }
                ComponentData param = componentDataMap.get(marketTime.market.getConfig().getMarket());
                getPicksFiltered(myData, param, config, marketTime);                
            }            
        }       
        return myData;
        /*
        {
            Market market = null;
            boolean save = true;
            List<MemoryItem> marketMemory = getMarketMemory(market);
            Map<String, String> retMap = new HashMap<>();
            List<MemoryItem> currentList = marketMemory.stream().filter(m -> olddate.compareTo(m.getRecord()) <= 0).collect(Collectors.toList());
            // or make a new object instead of the object array. use this as a pair
            Map<Object[], List<MemoryItem>> listMap = new HashMap<>();
            // map subcat + posit -> list
            currentList.forEach(m -> { listGetterAdder(listMap, new Object[]{m.getComponent(), m.getPosition() }, m); } );
            ProfitInputData inputdata = filterMemoryListMapsWithConfidence(market, listMap);
            ProfitData profitdata = new ProfitData();
            profitdata.setInputdata(inputdata);
            Map<String, List<Integer>> listComponent = new HashMap<>();

            for (Object[] keys : inputdata.getListMap().keySet()) {
                String component = (String) keys[0];
                Integer position = (Integer) keys[1];
                listGetterAdder(listComponent, component, position);
            }
            Map<String, Component> componentMap = new HashMap<>();
            for (String componentName : listComponent.keySet()) {
                Component component = ComponentFactory.factory(componentName);
                componentMap.put(componentName, component);
            }
            
            //Component.disabler(srv.conf);
            /*
            Map<String, Map<String, Object>> result0 = srv.getContent();
            Map<String, Map<String, Object>> maps = result0;
            */
/*
            param.getAndSetCategoryValueMap();
            Map<String, Map<String, Object>> maps = param.getResultMaps();

            inputdata.setResultMaps(maps);
            Map<String, String> nameMap = getNameMap(maps);
            inputdata.setNameMap(nameMap);
            for (Entry<String, List<Integer>> entry : listComponent.entrySet()) {
                List<Integer> positions = entry.getValue();
                Component component = componentMap.get(entry.getKey());
                Map<String, Object> valueMap = new HashMap<>();
                Component.disabler(valueMap);
                component.enable(valueMap);
                Map<String, String> map = component.improve(market, srv.conf, profitdata, positions);
                retMap.putAll(map);
            }
        }
        return null;
        */
    }

    private void getPicksFiltered(WebData myData, ComponentData param, IclijConfig config, MarketTime marketTime) {
        log.info("Getting picks for date {}", param.getInput().getEnddate());
        try {
            // List<MemoryItem> newMemories = findAllMarketComponentsToCheck(myData, param, 0, config, marketTime);
            myData.memoryItems = new ArrayList<>(); // newMemories;
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
            return;
        }
        Market market = marketTime.market;
        List<MemoryItem> marketMemory = new FindProfitAction().getMarketMemory(marketTime.market.getConfig().getMarket());
        if (marketMemory == null) {
            myData.profitData = new ProfitData();
        }
        marketMemory.addAll(myData.memoryItems);
        LocalDate prevdate = param.getInput().getEnddate();
        prevdate = prevdate.minusDays(market.getFilter().getRecordage());
        List<MemoryItem> currentList = new FindProfitAction().filterKeepRecent(marketMemory, prevdate);
        // or make a new object instead of the object array. use this as a pair
        //System.out.println(currentList.get(0).getRecord());
        Map<Object[], List<MemoryItem>> listMap = new HashMap<>();
        // map subcat + posit -> list
        currentList.forEach(m -> listGetterAdder(listMap, new Object[]{m.getComponent(), m.getPosition() }, m));
        ProfitInputData inputdata = filterMemoryListMapsWithConfidence(market, listMap);        
        ProfitData profitdata = new ProfitData();
        profitdata.setInputdata(inputdata);
        Map<String, List<Integer>> listComponent = new FindProfitAction().createComponentPositionListMap(inputdata.getListMap());
       
        Map<String, Component> componentMap = marketTime.componentMap;

        param.getAndSetCategoryValueMap();
        Map<String, Map<String, Object>> maps = param.getResultMaps();
        Map<String, String> nameMap = getNameMap(maps);
        inputdata.setNameMap(nameMap);
        
        handleComponent(market, profitdata, param, componentMap);
                
        filterBuys(param, market, profitdata, maps);
        //buys = buys.values().stream().filter(m -> olddate.compareTo(m.getRecord()) <= 0).collect(Collectors.toList());        
        myData.profitData = profitdata;

        /*
        ComponentParam param = new ComponentParam(input);
        ControlService srv = new ControlService();
        srv.getConfig();
        srv.conf.setMarket(market.getConfig().getMarket());
        srv.conf.setdate(TimeUtil.convertDate(input.getEnddate()));
        param.setService(srv);
        param.getInput().setMarket(srv.conf.getMarket());
        param.getInput().setDoPrint(false);
        param.getInput().setDoSave(false);        
        */
        
        //Component.disabler(param.getService().conf);

        /*
        Map<String, Map<String, Object>> result0 = param.getService().getContent();
        Map<String, Map<String, Object>> maps = result0;
        inputdata.setResultMaps(maps);
        */
}

    private void getPicksFiltered(WebData myData, ComponentData param, IclijConfig config, MarketComponentTime marketTime) {
        log.info("Getting picks for date {}", param.getInput().getEnddate());
        try {
            // List<MemoryItem> newMemories = findAllMarketComponentsToCheck(myData, param, 0, config, marketTime);
            myData.memoryItems = new ArrayList<>(); // newMemories;
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
            return;
        }
        Market market = marketTime.market;
        List<MemoryItem> marketMemory = new FindProfitAction().getMarketMemory(marketTime.market.getConfig().getMarket());
        if (marketMemory == null) {
            myData.profitData = new ProfitData();
        }
        marketMemory.addAll(myData.memoryItems);
        LocalDate prevdate = param.getInput().getEnddate();
        prevdate = prevdate.minusDays(market.getFilter().getRecordage());
        List<MemoryItem> currentList = new FindProfitAction().filterKeepRecent(marketMemory, prevdate);
        // or make a new object instead of the object array. use this as a pair
        //System.out.println(currentList.get(0).getRecord());
        Map<Object[], List<MemoryItem>> listMap = new HashMap<>();
        // map subcat + posit -> list
        currentList.forEach(m -> listGetterAdder(listMap, new Object[]{m.getComponent(), m.getPosition() }, m));
        ProfitInputData inputdata = filterMemoryListMapsWithConfidence(market, listMap);        
        ProfitData profitdata = new ProfitData();
        profitdata.setInputdata(inputdata);
        Map<String, List<Integer>> listComponent = new FindProfitAction().createComponentPositionListMap(inputdata.getListMap());
       
        Map<String, Component> componentMap = new HashMap<>();
        componentMap.put(marketTime.componentName, marketTime.component);

        param.getAndSetCategoryValueMap();
        Map<String, Map<String, Object>> maps = param.getResultMaps();
        Map<String, String> nameMap = getNameMap(maps);
        inputdata.setNameMap(nameMap);
        
        handleComponent(market, profitdata, param, componentMap);
                
        filterBuys(param, market, profitdata, maps);
        //buys = buys.values().stream().filter(m -> olddate.compareTo(m.getRecord()) <= 0).collect(Collectors.toList());        
        myData.profitData = profitdata;

        Map<String, Object> timingMap = new HashMap<>();
        timingMap.put(market.getConfig().getMarket(), param.getTimings());
        myData.timingMap = timingMap;

        myData.updateMap.putAll(param.getUpdateMap());

        /*
        ComponentParam param = new ComponentParam(input);
        ControlService srv = new ControlService();
        srv.getConfig();
        srv.conf.setMarket(market.getConfig().getMarket());
        srv.conf.setdate(TimeUtil.convertDate(input.getEnddate()));
        param.setService(srv);
        param.getInput().setMarket(srv.conf.getMarket());
        param.getInput().setDoPrint(false);
        param.getInput().setDoSave(false);        
        */
        
        //Component.disabler(param.getService().conf);

        /*
        Map<String, Map<String, Object>> result0 = param.getService().getContent();
        Map<String, Map<String, Object>> maps = result0;
        inputdata.setResultMaps(maps);
        */
    }

    private void filterBuys(ComponentData param, Market market, ProfitData profitdata,
            Map<String, Map<String, Object>> maps) {
        List<String> dates = param.getService().getDates(param.getService().conf.getMarket());        
        String category = market.getFilter().getInccategory();
        if (category != null) {
            Map<String, Object> categoryMap = maps.get(category);
            if (categoryMap != null) {
                Integer offsetDays = null;
                Integer incdays = market.getFilter().getIncdays();
                if (incdays != null) {
                    if (incdays == 0) {
                        int year = param.getInput().getEnddate().getYear();
                        List<String> yearDates = new ArrayList<>();
                        for (String date : dates) {
                            int aYear = Integer.valueOf(date.substring(0, 4));
                            if (year == aYear) {
                                yearDates.add(date);
                            }
                        }
                        Collections.sort(yearDates);
                        String oldestDate = yearDates.get(0);
                        int index = dates.indexOf(oldestDate);
                        offsetDays = dates.size() - 1 - index;
                    } else {
                       offsetDays = incdays;
                    }
                }
                Double threshold = market.getFilter().getIncthreshold();
                Map<String, List<List>> listMap3 = new FindProfitAction().getCategoryList(maps, category);
                Map<String, IncDecItem> buysFilter = new FindProfitAction().buyFilterOnIncreaseValue(market, profitdata.getBuys(), maps, threshold, categoryMap,
                        listMap3, offsetDays);
                profitdata.setBuys(buysFilter);
            }
        }
    }

    private List<MarketTime> getList(String action, Map<String, Component> componentMap, List<TimingItem> timings, Market market, ComponentData param) {
        List<MarketTime> marketTimes = new ArrayList<>();
        String marketName = market.getConfig().getMarket();
        MarketTime marketTime = new MarketTime();
        marketTime.market = market;

        List<TimingItem> timingToDo = new ArrayList<>();
        
        //= ServiceUtil.getFindProfitComponents(config);
        for (Entry<String, Component> entry : componentMap.entrySet()) {
            String componentName = entry.getKey();
            Component component = entry.getValue();
            boolean evolve = component.wantEvolve(param.getInput().getConfig());
            List<TimingItem> filterTimingsEvolution = getMyTimings(timings, marketName, action, componentName, true);
            if (evolve) {
                handleFilterTimings(action, market, marketTime, timingToDo, componentName, filterTimingsEvolution, evolve);               
            }
            List<TimingItem> filterTimings = getMyTimings(timings, marketName, action, componentName, false);
            handleFilterTimings(action, market, marketTime, timingToDo, componentName, filterTimings, evolve);
        }
        marketTime.componentMap = componentMap;
        marketTime.timings = timingToDo;
        marketTimes.add(marketTime);
        return marketTimes;
    }

    private List<MarketComponentTime> getList2(String action, Map<String, Component> componentMap, List<TimingItem> timings, Market market, ComponentData param) {
        List<MarketComponentTime> marketTimes = new ArrayList<>();
        String marketName = market.getConfig().getMarket();

        //= ServiceUtil.getFindProfitComponents(config);
        for (Entry<String, Component> entry : componentMap.entrySet()) {
            String componentName = entry.getKey();
            Component component = entry.getValue();
            List<TimingItem> timingToDo = new ArrayList<>();
            MarketComponentTime marketTime = new MarketComponentTime();
            marketTime.market = market;
            marketTime.componentName = componentName;
            marketTime.component = component;
            marketTime.timings = timingToDo;
            boolean evolve = component.wantEvolve(param.getInput().getConfig());
            List<TimingItem> filterTimingsEvolution = getMyTimings(timings, marketName, action, componentName, true);
            if (evolve) {
                handleFilterTimings(action, market, marketTime, timingToDo, componentName, filterTimingsEvolution, evolve);               
            }
            List<TimingItem> filterTimings = getMyTimings(timings, marketName, action, componentName, false);
            handleFilterTimings(action, market, marketTime, timingToDo, componentName, filterTimings, evolve);
            marketTimes.add(marketTime);
        }
        return marketTimes;
    }

    private static final int AVERAGE_SIZE = 5;
    
    private void handleFilterTimings(String action, Market market, MarketTime marketTime,
            List<TimingItem> timingToDo, String component, List<TimingItem> filterTimings, boolean evolve) {
        String marketName = market.getConfig().getMarket();
        if (!filterTimings.isEmpty()) {
            Collections.sort(filterTimings, (o1, o2) -> (o2.getDate().compareTo(o1.getDate())));
            LocalDate olddate = LocalDate.now();
            olddate = olddate.minusDays(market.getConfig().getImprovetime());
            int size = Math.min(AVERAGE_SIZE, filterTimings.size());
            OptionalDouble average = filterTimings
                    .subList(0, size)
                    .stream()
                    .mapToDouble(TimingItem::getTime)
                    .average();

            if (olddate.isBefore(filterTimings.get(0).getDate())) {
                // no recent enough is found
                marketTime.time += average.getAsDouble();
                if (!evolve) {
                    marketTime.time += average.getAsDouble();
                }
                TimingItem timing = new TimingItem();
                timing.setMarket(marketName);
                timing.setAction(action);
                timing.setComponent(component);
                timing.setEvolve(evolve);
                timing.setTime(average.getAsDouble());
                timingToDo.add(timing);
            } else {
                // recent enough is found
                // nothing to do
            }
        } else {
            TimingItem timing = new TimingItem();
            timing.setMarket(marketName);
            timing.setAction(action);
            timing.setComponent(component);
            timing.setEvolve(evolve);
            timing.setTime(Double.valueOf(0));
            timingToDo.add(timing);
        }
    }

    private void handleFilterTimings(String action, Market market, MarketComponentTime marketTime,
            List<TimingItem> timingToDo, String component, List<TimingItem> filterTimings, boolean evolve) {
        String marketName = market.getConfig().getMarket();
        if (!filterTimings.isEmpty()) {
            Collections.sort(filterTimings, (o1, o2) -> (o2.getDate().compareTo(o1.getDate())));
            LocalDate olddate = LocalDate.now();
            olddate = olddate.minusDays(market.getConfig().getImprovetime());
            int size = Math.min(AVERAGE_SIZE, filterTimings.size());
            OptionalDouble average = filterTimings
                    .subList(0, size)
                    .stream()
                    .mapToDouble(TimingItem::getTime)
                    .average();

            if (olddate.isBefore(filterTimings.get(0).getDate())) {
                // no recent enough is found
                marketTime.time += average.getAsDouble();
                if (!evolve) {
                    marketTime.time += average.getAsDouble();
                }
                TimingItem timing = new TimingItem();
                timing.setMarket(marketName);
                timing.setAction(action);
                timing.setComponent(component);
                timing.setEvolve(evolve);
                timing.setTime(average.getAsDouble());
                timingToDo.add(timing);
            } else {
                // recent enough is found
                // nothing to do
            }
        } else {
            TimingItem timing = new TimingItem();
            timing.setMarket(marketName);
            timing.setAction(action);
            timing.setComponent(component);
            timing.setEvolve(evolve);
            timing.setTime(Double.valueOf(0));
            timingToDo.add(timing);
        }
    }

    public class MarketTime {
        public Map<String, Component> componentMap;
        Market market;
        double time;
        List<TimingItem> timings;
    }
    
    public class MarketComponentTime {
        String componentName;
        Component component;
        Market market;
        double time;
        List<TimingItem> timings;
        
        @Override
        public String toString() {
            return market.getConfig().getMarket() + " " + componentName + " " + time;
        }
    }
    
    private List<TimingItem> getMyTimings(List<TimingItem> timings, String market, String action, String component, boolean evolve) {
        List<TimingItem> filterTimings = new ArrayList<>();
        for (TimingItem timing : timings) {
            if (market.equals(timing.getMarket()) && action.equals(timing.getAction()) && component.equals(timing.getComponent()) && evolve == timing.isEvolve()) {
                filterTimings.add(timing);
            }
        }
        return filterTimings;
    }

    /*
    public Map<String, String> getImprovements(ComponentData param, List<MemoryItem> memoryItems) {
        Market foundMarket = findMarket(param);
        return getImprovementInfo(foundMarket, memoryItems, param);
    }
    */

    public Market findMarket(ComponentData param) {
        IclijConfig instance = IclijXMLConfig.getConfigInstance();
        List<Market> markets = getMarkets(instance);
        Market foundMarket = null;
        for (Market aMarket : markets) {
            if (param.getMarket().equals(aMarket.getConfig().getMarket())) {
                foundMarket = aMarket;
                break;
            }
        }
        return foundMarket;
    }
    
    public ProfitInputData filterMemoryListMapsWithConfidence(Market market, Map<Object[], List<MemoryItem>> listMap) {
        Map<Object[], List<MemoryItem>> badListMap = new HashMap<>();
        Map<Object[], Double> badConfMap = new HashMap<>();
        for(Object[] keys : listMap.keySet()) {
            List<MemoryItem> memoryList = listMap.get(keys);
            List<Double> confidences = memoryList.stream().map(MemoryItem::getConfidence).collect(Collectors.toList());
            if (confidences.isEmpty()) {
                int jj = 0;
                //continue;
            }
            confidences = confidences.stream().filter(m -> m != null && !m.isNaN()).collect(Collectors.toList());
            Optional<Double> minOpt = confidences.parallelStream().reduce(Double::min);
            if (!minOpt.isPresent()) {
                int jj = 0;
                //continue;
            }
            Double min = 0.0;
            if (minOpt.isPresent()) {
                min = minOpt.get();
            }
            // do the bad ones
            // do not yet improve on the good enough ones
            if (false /*min >= market.getConfidence()*/) {
                continue;
            }
            //Optional<Double> maxOpt = confidences.parallelStream().reduce(Double::max);
            //Double max = maxOpt.get();
            //System.out.println("Mark " + market.getConfig().getMarket() + " " + keys[0] + " " + min + " " + max );
            //Double conf = market.getConfidence();
            //System.out.println(conf);
            badListMap.put(keys, listMap.get(keys));
            badConfMap.put(keys, min);
        }
        ProfitInputData input = new ProfitInputData();
        input.setConfMap(badConfMap);
        input.setListMap(badListMap);
        return input;
    }

    private List<MemoryItem> getMarketMemory(Market market) {
        List<MemoryItem> marketMemory = null;
        try {
            marketMemory = IclijDbDao.getAll(market.getConfig().getMarket());
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        return marketMemory;
    }

    private List<Market> getMarkets(IclijConfig instance) {
        List<Market> markets = null;
        try { 
            markets = IclijXMLConfig.getMarkets(instance);
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        return markets;
    }

    private Map<String, String> getNameMap(Map<String, Map<String, Object>> maps) {
        Map<String, String> nameMap = null;
        for (Entry<String, Map<String, Object>> entry : maps.entrySet()) {
            Map<String, Object> map = entry.getValue();
            nameMap = (Map<String, String>) map.get(PipelineConstants.NAME);
            if (nameMap != null) {
                break;
            }
        }
        return nameMap;
    }
    
    @Deprecated
    private List<MarketFilter> getTradeMarkets(IclijConfig instance) {
        List<MarketFilter> markets = null;
        try { 
            markets = IclijXMLConfig.getFilterMarkets(instance);
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        return markets;
    }
    private <K, E> List<E> listGetter(Map<K, List<E>> listMap, K key) {
        return listMap.computeIfAbsent(key, k -> new ArrayList<>());
    }

    public <K, E> void listGetterAdder(Map<K, List<E>> listMap, K key, E element) {
        List<E> list = listGetter(listMap, key);
        list.add(element);
    }

    private <T> String nullToEmpty(T s) {
        return s != null ? "" + s : "";
    }

    public List<MapList> getList(Map<String, Object> map) {
        List<MapList> retList = new ArrayList<>();
        for (Entry<String, Object> entry : map.entrySet()) {
            MapList ml = new MapList();
            ml.setKey(entry.getKey());
            ml.setValue((String) entry.getValue().toString());
            retList.add(ml);
        }
        return retList;
    }
    
    public List<MemoryItem> findAllMarketComponentsToCheck(WebData myData, ComponentData param, int days, IclijConfig config, MarketTime marketTime) {
        List<MemoryItem> allMemories = new ArrayList<>();
        Short startOffset = marketTime.market.getConfig().getStartoffset();
        if (startOffset != null) {
            System.out.println("Using offset " + startOffset);
            log.info("Using offset {}", startOffset);
            days += startOffset;
        }
        for (Entry<String, Component> entry : marketTime.componentMap.entrySet()) {
            Component component = entry.getValue();
            long time0 = System.currentTimeMillis();
            //Market market = FindProfitAction.findMarket(componentparam);
            ProfitData profitdata = new ProfitData();
            ComponentData componentData = null; //component.improve(marketTime.market, param, profitdata, new ArrayList<>(), true);
            componentData.setUsedsec(time0);
            myData.updateMap.putAll(componentData.getUpdateMap());
            List<MemoryItem> memories;
            try {
                memories = component.calculateMemory(componentData);
                allMemories.addAll(memories);
           } catch (Exception e) {
                log.error(Constants.EXCEPTION, e);
            }
         }
        return allMemories;
    }

    private void handleComponent(Market market, ProfitData profitdata, ComponentData param, Map<String, Component> componentMap) {
        if (param.getUpdateMap() == null) {
            param.setUpdateMap(new HashMap<>());
        }
        for (Entry<String, Component> entry : componentMap.entrySet()) {
            Component component = entry.getValue();
            if (component == null) {
                continue;
            }
            boolean evolve = false; // param.getInput().getConfig().wantEvolveML();
            //component.set(market, param, profitdata, positions, evolve);
            //ComponentData componentData = component.handle(market, param, profitdata, positions, evolve, new HashMap<>());
            ComponentData componentData = component.improve(param, market, profitdata, null);
            Map<String, Object> updateMap = componentData.getUpdateMap();
            if (updateMap != null) {
                param.getUpdateMap().putAll(updateMap);
            }
            //component.calculateIncDec(componentData, profitdata, positions);
            //System.out.println("Buys: " + market.getMarket() + buys);
            //System.out.println("Sells: " + market.getMarket() + sells);           
        }
    }

 }

