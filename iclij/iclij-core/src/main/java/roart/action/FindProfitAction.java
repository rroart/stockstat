package roart.action;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
//import java.util.Collections;
//import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.Queue;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

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
import roart.constants.IclijPipelineConstants;
import roart.db.IclijDbDao;
import roart.iclij.config.IclijConfig;
import roart.iclij.config.MLConfig;
import roart.iclij.config.MLConfigs;
import roart.iclij.model.IncDecItem;
import roart.iclij.model.MemoryItem;
import roart.iclij.model.TimingItem;
import roart.result.model.ResultMeta;
import roart.service.ControlService;
import roart.service.model.ProfitData;
import roart.service.model.ProfitInputData;
import roart.util.ServiceUtil;

public class FindProfitAction extends Action {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    public static final String TP = "TP";
    public static final String TN = "TN";
    public static final String FP = "FP";
    public static final String FN = "FN";

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
        List<Market> markets = getMarkets();
        ComponentData param = null;
        try {
            param = ServiceUtil.getParam(input, 0);
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        return getMarkets(parent, param, markets, timings);
    }        
    
    public WebData getMarkets(Action parent, ComponentData paramTemplate, List<Market> markets, List<TimingItem> timings) {
        // find recommended picks
        WebData myData = new WebData();
        myData.updateMap = new HashMap<>();
	
        IclijConfig config = IclijXMLConfig.getConfigInstance();
        //boolean save = true;
        List<MarketTime> marketTimes = new ArrayList<>();
        Map<String, ComponentData> componentDataMap = new HashMap<>();
        for (Market market : markets) {
            String marketName = market.getConfig().getMarket();
            //LocalDate olddate = LocalDate.now();        
            //olddate = olddate.minusDays(market.getFilter().getRecordage());
            ComponentInput input = new ComponentInput(IclijXMLConfig.getConfigInstance(), null, marketName, null, 0, paramTemplate.getInput().isDoSave(), false, new ArrayList<>(), new HashMap<>());
            ComponentData param = null;
            try {
                param = ServiceUtil.getParam(input, 0);
            } catch (Exception e) {
                log.error(Constants.EXCEPTION, e);
            }
            List<String> stockDates = param.getService().getDates(marketName);
            if (stockDates == null || stockDates.isEmpty()) {
                continue;
            }
            //param.setMarket(market.getConfig().getMarket());
            //ComponentData param = new ComponentData();
            //ComponentInput input = new ComponentInput(config, null, market.getConfig().getMarket(), olddate, null, save, false, new ArrayList<>(), new HashMap<>());
            //param = new ComponentData(paramTemplate);
            param.setAction(IclijConstants.FINDPROFIT);
            ControlService srv = new ControlService();
            //srv.getConfig();
            param.setService(srv);
            //param.setOffset(0);
            srv.conf.setMarket(market.getConfig().getMarket());
            //srv.conf.setdate(TimeUtil.convertDate(input.getEnddate()));
            componentDataMap.put(marketName, param);
            LocalDate olddate = param.getInput().getEnddate();
            try {
                param.setFuturedays(market.getFilter().getRecordage());
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
            List<IncDecItem> currentIncDecs = ServiceUtil.getCurrentIncDecs(olddate, incdecitems, market);
            if (currentIncDecs == null || currentIncDecs.isEmpty()) {
                List<String> componentList = ServiceUtil.getFindProfitComponents(config);
                Map<String, Component> componentMap = getComponentMap(componentList, market);
                List<MarketTime> marketTime = getList(IclijConstants.FINDPROFIT, componentMap, timings, market, param);
                marketTimes.addAll(marketTime);
            } else {
                int jj = 0;
            }
        }
        Collections.sort(marketTimes, (o1, o2) -> (Double.valueOf(o2.time).compareTo(Double.valueOf(o1.time))));
        for (MarketTime marketTime : marketTimes) {
            if (marketTime.time == 0.0) {
                ComponentData param = componentDataMap.get(marketTime.market.getConfig().getMarket());
                getPicks(myData, param, config, marketTime);                
            }
        }
        for (MarketTime marketTime : marketTimes) {
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
                getPicks(myData, param, config, marketTime);                
            }            
        }       
        return myData;
    }

    private void getPicks(WebData myData, ComponentData param, IclijConfig config, MarketTime marketTime) {
        List<MemoryItem> newMemories;
        try {
            newMemories = getMemoryItems(myData, marketTime, param, 0, config);
            myData.memoryItems = newMemories;
        } catch (InterruptedException e) {
            log.error(Constants.EXCEPTION, e);
            return;
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
            return;
        }
        getPicksFiltered(myData, param, marketTime.market, marketTime);
    }

    private static final int AVERAGE_SIZE = 5;
    
    @Deprecated
    public List<MarketTime> getList(IclijConfig config, String action, Map<String, Component> componentMap, ComponentData param) throws Exception {
        List<MarketTime> marketTimes = new ArrayList<>();
        List<TimingItem> timings = TimingItem.getAll();
        List<Market> markets = getMarkets();
        for (Market market : markets) {
            List<MarketTime> moreTimes = getList(action, componentMap, timings, market, param);
            marketTimes.addAll(moreTimes);
        }
        return marketTimes;
    }

    private List<MarketTime> getList(String action, Map<String, Component> componentMap, List<TimingItem> timings, Market market, ComponentData param) {
        List<MarketTime> marketTimes = new ArrayList<>();
        String marketName = market.getConfig().getMarket();
        MarketTime marketTime = new MarketTime();
        marketTime.market = market;

        List<TimingItem> timingToDo = new ArrayList<>();
        
        //= ServiceUtil.getFindProfitComponents(config);
        for (Entry<String, Component> entry : componentMap.entrySet()) {
            String component = entry.getKey();
            boolean evolve = param.getInput().getConfig().wantEvolveML();
            List<TimingItem> filterTimingsEvolution = getMyTimings(timings, marketName, action, component, true);
            if (evolve) {
                handleFilterTimings(action, market, marketTime, timingToDo, component, filterTimingsEvolution, evolve);               
            }
            List<TimingItem> filterTimings = getMyTimings(timings, marketName, action, component, false);
            handleFilterTimings(action, market, marketTime, timingToDo, component, filterTimings, evolve);
        }
        marketTime.componentMap = componentMap;
        marketTime.timings = timingToDo;
        marketTimes.add(marketTime);
        return marketTimes;
    }

    private void handleFilterTimings(String action, Market market, MarketTime marketTime,
            List<TimingItem> timingToDo, String component, List<TimingItem> filterTimings, boolean evolve) {
        String marketName = market.getConfig().getMarket();
        if (!filterTimings.isEmpty()) {
            Collections.sort(filterTimings, (o1, o2) -> (o2.getDate().compareTo(o1.getDate())));
            LocalDate olddate = LocalDate.now();
            olddate = olddate.minusDays(market.getFilter().getRecordage());
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
    
    private List<TimingItem> getMyTimings(List<TimingItem> timings, String market, String action, String component, boolean evolve) {
        List<TimingItem> filterTimings = new ArrayList<>();
        for (TimingItem timing : timings) {
            if (market.equals(timing.getMarket()) && action.equals(timing.getAction()) && component.equals(timing.getComponent()) && evolve == timing.isEvolve()) {
                filterTimings.add(timing);
            }
        }
        return filterTimings;
    }

    @Deprecated
    public void getPicks(ComponentData param, List<MemoryItem> memoryItems) {
        Market foundFilterMarket = findMarket(param);
        if (foundFilterMarket == null) {
            //return new ProfitData();
        }
        List<MarketTime> marketTimes = new ArrayList<>();
        MarketTime marketTime = new MarketTime();
        marketTimes.add(marketTime);
        marketTime.market = foundFilterMarket;
        marketTime.timings = new ArrayList<>();
        IclijConfig config = IclijXMLConfig.getConfigInstance();
        List<String> componentList = ServiceUtil.getFindProfitComponents(config);
        Map<String, Component> componentMap = getComponentMap(componentList, foundFilterMarket);

        List<TimingItem> timingToDo = new ArrayList<>();
        
        String action = IclijConstants.FINDPROFIT;
        Market market = foundFilterMarket;
        String marketName = market.getConfig().getMarket();
        
        //= ServiceUtil.getFindProfitComponents(config);
        for (Entry<String, Component> entry : componentMap.entrySet()) {
            String component = entry.getKey();
            boolean evolve = param.getInput().getConfig().wantEvolveML();
            List<TimingItem> timings = new ArrayList<>();
            
            if (evolve) {
                TimingItem timing = new TimingItem();
                timing.setMarket(marketName);
                timing.setAction(action);
                timing.setComponent(component);
                timing.setEvolve(evolve);
                timing.setTime(Double.valueOf(0));
                timingToDo.add(timing);
            }
            TimingItem timing = new TimingItem();
            timing.setMarket(marketName);
            timing.setAction(action);
            timing.setComponent(component);
            timing.setEvolve(false);
            timing.setTime(Double.valueOf(0));
            timingToDo.add(timing);
        }
        marketTime.timings = timingToDo;
        marketTime.componentMap = componentMap;
        marketTimes.add(marketTime);        
        
        getPicksFiltered(null, param, foundFilterMarket, marketTime);
    }

    public static Market findMarket(ComponentData param) {
        List<Market> markets = getMarkets();
        Market foundFilterMarket = null;
        // TODO check if two markets
        for (Market aMarket : markets) {
            if (param.getInput().getMarket().equals(aMarket.getConfig().getMarket())) {
                foundFilterMarket = aMarket;
                break;
            }
        }
        return foundFilterMarket;
    }
    
    private void getPicksFiltered(WebData myData, ComponentData param, Market market, MarketTime marketTime) {
        log.info("Getting picks for date {}", param.getInput().getEnddate());
        List<MemoryItem> marketMemory = getMarketMemory(marketTime.market.getConfig().getMarket());
        if (marketMemory == null) {
            myData.profitData = new ProfitData();
        }
        marketMemory.addAll(myData.memoryItems);
        
        List<MemoryItem> currentList = filterKeepRecent(marketMemory, param.getInput().getEnddate());
        // or make a new object instead of the object array. use this as a pair
        //System.out.println(currentList.get(0).getRecord());
        Map<Object[], List<MemoryItem>> listMap = new HashMap<>();
        // map subcat + posit -> list
        currentList.forEach(m -> listGetterAdder(listMap, new Object[]{m.getComponent(), m.getPosition() }, m));
        ProfitInputData inputdata = filterMemoryListMapsWithConfidence(market, listMap);
        ProfitData profitdata = new ProfitData();
        profitdata.setInputdata(inputdata);
        Map<String, List<Integer>> listComponent = createComponentPositionListMap(inputdata.getListMap());
       
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
        
        Map<String, Component> componentMap = marketTime.componentMap;

        //Component.disabler(param.getService().conf);

        /*
        Map<String, Map<String, Object>> result0 = param.getService().getContent();
        Map<String, Map<String, Object>> maps = result0;
        inputdata.setResultMaps(maps);
        */
        param.getAndSetCategoryValueMap();
        Map<String, Map<String, Object>> maps = param.getResultMaps();
        Map<String, String> nameMap = getNameMap(maps);
        inputdata.setNameMap(nameMap);
        
        handleComponent(market, profitdata, listComponent, param, componentMap);
                
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
                Map<String, List<List>> listMap3 = getCategoryList(maps, category);
                Map<String, IncDecItem> buysFilter = buyFilterOnIncreaseValue(market, profitdata.getBuys(), maps, threshold, categoryMap,
                        listMap3, offsetDays);
                profitdata.setBuys(buysFilter);
            }
        }
        if (param.getInput().isDoSave()) {
            IncDecItem myitem = null;
        try {
            for (IncDecItem item : profitdata.getBuys().values()) {
                myitem = item;
                System.out.println(item);
                item.save();
            }
            for (IncDecItem item : profitdata.getSells().values()) {
                myitem = item;
                System.out.println(item);
                item.save();
            }
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
            log.error("Could not save {}", myitem);
        }
        }
        //buys = buys.values().stream().filter(m -> olddate.compareTo(m.getRecord()) <= 0).collect(Collectors.toList());        
        myData.profitData = profitdata;
    }

    private Map<String, List<List>> getCategoryList(Map<String, Map<String, Object>> maps, String category) {
        String newCategory = null;
        if (Constants.PRICE.equals(category)) {
            newCategory = "" + Constants.PRICECOLUMN;
        }
        if (Constants.INDEX.equals(category)) {
            newCategory = "" + Constants.INDEXVALUECOLUMN;
        }
        if (newCategory != null) {
            Map<String, Object> map = maps.get(newCategory);
            return (Map<String, List<List>>) map.get(PipelineConstants.LIST);
        }
        Map<String, List<List>> listMap3 = null;
        for (Entry<String, Map<String, Object>> entry : maps.entrySet()) {
            Map<String, Object> map = entry.getValue();
            if (category.equals(map.get(PipelineConstants.CATEGORYTITLE))) {
                listMap3 = (Map<String, List<List>>) map.get(PipelineConstants.LIST);
            }
        }
        return listMap3;
    }

    private Map<String, IncDecItem> buyFilterOnIncreaseValue(Market market, Map<String, IncDecItem> buys,
            Map<String, Map<String, Object>> maps, Double threshold, Map<String, Object> categoryMap,
            Map<String, List<List>> listMap3, Integer offsetDays) {
        Map<String, IncDecItem> buysFilter = new HashMap<>();
        for(IncDecItem item : buys.values()) {
            String key = item.getId();
            if (listMap3 == null) {
                if (categoryMap != null) {
                    System.out.println(categoryMap.keySet());
                }
                if (maps != null) {
                    System.out.println(maps.keySet());
                }
                System.out.println("market" + market.getConfig().getMarket() + "null map");
                continue;
            }
            List<List> list = listMap3.get(key);
            List<Double> list0 = list.get(0);
            Double value = null;
            if (offsetDays == null) {
                value = list0.get(list0.size() - 1);
            } else {
                Double curValue = list0.get(list0.size() - 1);
                Double oldValue = list0.get(list0.size() - 1 - offsetDays);
                if (curValue != null && oldValue != null) {
                    value = (curValue / oldValue - 1) * 100;
                }
            }
            if (value == null) {
                continue;
            }
            if (value < threshold) {
                continue;
            }
            buysFilter.put(key, item);
        }
        return buysFilter;
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

    private void handleComponent(Market market, ProfitData profitdata, Map<String, List<Integer>> listComponent, ComponentData param, Map<String, Component> componentMap) {
        for (Entry<String, List<Integer>> entry : listComponent.entrySet()) {
            List<Integer> positions = entry.getValue();
            String componentName = entry.getKey();
            Component component = componentMap.get(componentName);
            if (component == null) {
                continue;
            }
            boolean evolve = false; // param.getInput().getConfig().wantEvolveML();
            //component.set(market, param, profitdata, positions, evolve);
            ComponentData componentData = component.handle(market, param, profitdata, positions, evolve);
            component.calculateIncDec(componentData, profitdata, positions);
            //System.out.println("Buys: " + market.getMarket() + buys);
            //System.out.println("Sells: " + market.getMarket() + sells);           
        }
    }

    public Map<String, Component> getComponentMap(Collection<String> listComponent, Market market) {
        Map<String, Component> componentMap = new HashMap<>();
        for (String componentName : listComponent) {
            Component component = ComponentFactory.factory(componentName);
            if (market != null && componentName.equals(PipelineConstants.PREDICTORSLSTM)) {
                MLConfigs mlConfigs = market.getMlconfig();
                if (mlConfigs != null) {
                    MLConfig mlConfig = mlConfigs.getLstm();
                    if (mlConfig != null) {
                        if (!mlConfig.getEnable()) {
                            continue;
                        }
                    }
                }
            }
            componentMap.put(componentName, component);
        }
        return componentMap;
    }

    private Map<String, List<Integer>> createComponentPositionListMap(Map<Object[], List<MemoryItem>> okListMap) {
        Map<String, List<Integer>> listComponent = new HashMap<>();
        for (Object[] keys : okListMap.keySet()) {
            String component = (String) keys[0];
            Integer position = (Integer) keys[1];
            listGetterAdder(listComponent, component, position);
        }
        return listComponent;
    }

    private ProfitInputData filterMemoryListMapsWithConfidence(Market market, Map<Object[], List<MemoryItem>> listMap) {
        Map<Object[], List<MemoryItem>> okListMap = new HashMap<>();
        Map<Object[], Double> okConfMap = new HashMap<>();
        for(Entry<Object[], List<MemoryItem>> entry : listMap.entrySet()) {
            Object[] keys = entry.getKey();
            List<MemoryItem> memoryList = entry.getValue();
            List<Double> confidences = memoryList.stream().map(MemoryItem::getConfidence).collect(Collectors.toList());
            confidences = confidences.stream().filter(m -> m != null && !m.isNaN()).collect(Collectors.toList());
            Optional<Double> minOpt = confidences.parallelStream().reduce(Double::min);
            if (!minOpt.isPresent()) {
                continue;
            }
            Double min = minOpt.get();
            if (min >= market.getFilter().getConfidence()) {
                okListMap.put(keys, memoryList);
                okConfMap.put(keys, min);
            }
        }
        ProfitInputData input = new ProfitInputData();
        input.setConfMap(okConfMap);
        input.setListMap(listMap);
        return input;
    }

    public List<MemoryItem> filterKeepRecent(List<MemoryItem> marketMemory, LocalDate olddate) {
        for (MemoryItem item : marketMemory) {
            if (item.getRecord() == null) {
                item.setRecord(LocalDate.now());
            }
        }
        // temp workaround
        if (olddate == null) {
            return marketMemory;
        }
        List<MemoryItem> currentList = marketMemory.stream().filter(m -> olddate.compareTo(m.getRecord()) <= 0).collect(Collectors.toList());
        return currentList;
    }

    public List<MemoryItem> getMarketMemory(String market) {
        List<MemoryItem> marketMemory = null;
        try {
            marketMemory = IclijDbDao.getAll(market);
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        return marketMemory;
    }

    public static List<Market> getMarkets() {
        IclijConfig instance = IclijXMLConfig.getConfigInstance();
        //instance.
        List<Market> markets = null;
        try { 
            markets = IclijXMLConfig.getMarkets(instance);
        } catch (Exception e) {
            //log.error(Constants.EXCEPTION, e);
        }
        return markets;
    }

    private <K, E> List<E> listGetter(Map<K, List<E>> listMap, K key) {
        return listMap.computeIfAbsent(key, k -> new ArrayList<>());
    }

    private <K, E> void listGetterAdder(Map<K, List<E>> listMap, K key, E element) {
        List<E> list = listGetter(listMap, key);
        list.add(element);
    }

    private <T> String nullToEmpty(T s) {
        return s != null ? "" + s : "";
    }

    public List<MemoryItem> getMemoryItems(WebData myData, MarketTime marketTime, ComponentData param, int days, IclijConfig config) throws InterruptedException {
        List<MemoryItem> allMemoryItems = findAllMarketComponentsToCheck(myData, param, days, config, marketTime);
        return allMemoryItems;
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
            ComponentData componentData = component.handle(marketTime.market, param, profitdata, new ArrayList<>(), true);
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


}
