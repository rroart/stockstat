package roart.action;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.OptionalDouble;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.common.constants.Constants;
import roart.common.pipeline.PipelineConstants;
import roart.common.util.JsonUtil;
import roart.component.Component;
import roart.component.ComponentFactory;
import roart.component.model.ComponentData;
import roart.component.model.ComponentInput;
import roart.config.IclijXMLConfig;
import roart.config.Market;
import roart.constants.IclijConstants;
import roart.db.IclijDbDao;
import roart.iclij.config.IclijConfig;
import roart.iclij.config.MLConfig;
import roart.iclij.config.MLConfigs;
import roart.iclij.model.IncDecItem;
import roart.iclij.model.MapList;
import roart.iclij.model.MemoryItem;
import roart.iclij.model.Parameters;
import roart.iclij.model.TimingItem;
import roart.service.ControlService;
import roart.service.model.ProfitData;
import roart.service.model.ProfitInputData;
import roart.util.ServiceUtil;

public abstract class MarketAction extends Action {

    private Logger log = LoggerFactory.getLogger(this.getClass());
    
    protected abstract List<IncDecItem> getIncDecItems();

    protected abstract List getAnArray();

    protected abstract Boolean getBool();

    public abstract String getName();
    
    protected abstract List<String> getProfitComponents(IclijConfig config, String marketName);

    public abstract Short getTime(Market market);
    
    public abstract Boolean[] getBooleans();
    
    protected abstract boolean getEvolve(Component component, ComponentData param);

    protected abstract List<MemoryItem> getMemItems(MarketComponentTime marketTime, WebData myData, ComponentData param, IclijConfig config, Boolean evolve, Map<String, ComponentData> dataMap);

    protected abstract LocalDate getPrevDate(ComponentData param, Market market);

    protected abstract void setValMap(ComponentData param);
    
    public abstract ComponentFactory getComponentFactory();
    
    public abstract int getPriority(IclijConfig conf);
    
    public int getPriority(IclijConfig conf, String key) {
        Integer value = (Integer) conf.getConfigValueMap().get(key + "[@priority]");
        return value != null ? value : 0;
    }
    
    @Override
    public void goal(Action parent, ComponentData param, Integer priority) {
        getMarkets(parent, new ComponentInput(IclijXMLConfig.getConfigInstance(), null, null, null, 0, true, false, new ArrayList<>(), new HashMap<>()), null, priority);
    }

    public WebData getMarket(Action parent, ComponentData param, Market market, Boolean evolve, Integer priority) {
        List<Market> markets = new ArrayList<>();
        markets.add(market);
        return getMarkets(parent, param, markets, new ArrayList<>(), evolve, priority);
    }        
    
    public WebData getMarkets(Action parent, ComponentInput input, Boolean evolve, Integer priority) {
        List<TimingItem> timings = null;
        try {
            timings = IclijDbDao.getAllTiming();
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
        return getMarkets(parent, param, markets, timings, evolve, priority);
    }        
    
    private WebData getMarkets(Action parent, ComponentData paramTemplate, List<Market> markets, List<TimingItem> timings, Boolean evolve, Integer priority) {
	// test picks for aggreg recommend, predict etc
        // remember and make confidence
        // memory is with date, confidence %, inc/dec, semantic item
        // find recommended picks
        WebData myData = new WebData();
        myData.incs = new ArrayList<>();
        myData.decs = new ArrayList<>();
        myData.updateMap = new HashMap<>();
        myData.timingMap = new HashMap<>();
        myData.updateMap2 = new HashMap<>();
        myData.timingMap2 = new HashMap<>();

        markets = filterMarkets(markets);
        
        IclijConfig config = paramTemplate.getInput().getConfig();
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
            param.setAction(getName());
            ControlService srv = new ControlService();
            //srv.getConfig();
            param.setService(srv);
            srv.conf.setMarket(market.getConfig().getMarket());
            if (!isDataset()) {
                List<String> stockDates = param.getService().getDates(marketName);
                if (stockDates == null || stockDates.isEmpty()) {
                    continue;
                }
            }
            componentDataMap.put(marketName, param);
            LocalDate olddate = param.getInput().getEnddate();

            try {
                //param.setFuturedays(market.getFilter().getRecordage());
                //param.setDates(market.getFilter().getRecordage(), 0, TimeUtil.convertDate2(olddate));
            } catch (Exception e) {
                log.error(Constants.EXCEPTION, e);
            }
            Short time = getTime(market);
            if (getTime(market) == null) {
                continue;
            }
            List<TimingItem> currentTimings = ServiceUtil.getCurrentTimings(olddate, timings, market, getName(), time);
            List<IncDecItem> currentIncDecs = null; // ServiceUtil.getCurrentIncDecs(olddate, incdecitems, market);
            if (true) {
                List<String> componentList = getProfitComponents(config, marketName);
                Map<String, Component> componentMap = getComponentMap(componentList, market);
                Map<String, Component> componentMapFiltered = new HashMap<>();
                for (Entry<String, Component> entry : componentMap.entrySet()) {
                    int mypriority = getPriority(config) + entry.getValue().getPriority(config);
                    if (priority == null || (mypriority >= priority && mypriority < (priority + 9))) {
                        componentMapFiltered.put(entry.getKey(),  entry.getValue());
                    }
                }
                List<MarketComponentTime> marketTime = getList(getName(), componentMapFiltered, timings, market, param, currentTimings);
                marketTimes.addAll(marketTime);
            } else {
                int jj = 0;
            }
        }
        Collections.sort(marketTimes, (o1, o2) -> (Double.valueOf(o2.time).compareTo(Double.valueOf(o1.time))));
        List<MarketComponentTime> run = marketTimes.stream().filter(m -> m.haverun).collect(Collectors.toList());
        List<MarketComponentTime> notrun = marketTimes.stream().filter(m -> !m.haverun).collect(Collectors.toList());

        for (MarketComponentTime marketTime : marketTimes) {
            log.info("MarketTime {}", marketTime);
        }
        for (MarketComponentTime marketTime : notrun) {
            if (marketTime.time == 0.0) {
                ComponentData param = componentDataMap.get(marketTime.market.getConfig().getMarket());
                getPicksFiltered(myData, param, config, marketTime, evolve);                
            }
        }
        for (MarketComponentTime marketTime : notrun) {
            if (marketTime.time > 0.0) {
                if (config.serverShutdownHour() != null) {
                    int shutdown = config.serverShutdownHour();
                    shutdown --;
                    LocalTime now = LocalTime.now();
                    int minutes = 60 * now.getHour() + now.getMinute();
                    minutes += marketTime.time / 60;
                    if (minutes >= shutdown * 60) {
                        continue;
                    }
                }
                ComponentData param = componentDataMap.get(marketTime.market.getConfig().getMarket());
                getPicksFiltered(myData, param, config, marketTime, evolve);                
            }            
        }       
        for (MarketComponentTime marketTime : run) {
            if (marketTime.time == 0.0) {
                log.error("should not be here");
            }
            if (marketTime.time > 0.0) {
                if (config.serverShutdownHour() != null) {
                    int shutdown = config.serverShutdownHour();
                    shutdown --;
                    LocalTime now = LocalTime.now();
                    int minutes = 60 * now.getHour() + now.getMinute();
                    minutes += marketTime.time / 60;
                    if (minutes >= shutdown * 60) {
                        continue;
                    }
                }
                ComponentData param = componentDataMap.get(marketTime.market.getConfig().getMarket());
                getPicksFiltered(myData, param, config, marketTime, evolve);                
            }            
        }       
        return myData;
    }

    protected boolean isDataset() {
        return false;
    }
    
    private List<Market> filterMarkets(List<Market> markets) {
        List<Market> filtered = new ArrayList<>();
        for (Market market : markets) {
            Boolean dataset = market.getConfig().getDataset();
            boolean adataset = dataset != null && dataset;
            if (adataset == isDataset()) {
                filtered.add(market);
            }
        }
        return filtered;
    }

    private static final int AVERAGE_SIZE = 5;
    
    private List<MarketComponentTime> getList(String action, Map<String, Component> componentMap, List<TimingItem> timings, Market market, ComponentData param, List<TimingItem> currentTimings) {
        List<MarketComponentTime> marketTimes = new ArrayList<>();
        String marketName = market.getConfig().getMarket();

        //= ServiceUtil.getFindProfitComponents(config);
        for (Entry<String, Component> entry : componentMap.entrySet()) {
            String componentName = entry.getKey();
            Component component = entry.getValue();
            boolean evolve = getEvolve(component, param);

            Double[] thresholds = getThresholds(param.getInput().getConfig());
            Integer[] futuredays = getFuturedays(param.getInput().getConfig());
            List<String> subComponents = component.getSubComponents(market, param, null);
            for(String subComponent : subComponents) {
                for (Integer aFutureday : futuredays) {
                    for (Double threshold : thresholds) {
                        Parameters parameters = new Parameters();
                        parameters.setThreshold(threshold);
                        parameters.setFuturedays(aFutureday);
                        String parameterString = JsonUtil.convert(parameters);
                        Boolean[] booleans = getBooleans();
                        for (Boolean buy : booleans) {
                            List<TimingItem> currentTimingFiltered = currentTimings.stream().filter(m -> m != null 
                                    && componentName.equals(m.getComponent()) 
                                    && (subComponents == null || subComponent.equals(m.getSubcomponent())) 
                                    && (m.getParameters() == null || parameterString.equals(m.getParameters())) 
                                    && (buy == null || ((m.getBuy() == null || m.getBuy() == buy)))).collect(Collectors.toList());
                            if (!currentTimingFiltered.isEmpty()) {
                                continue;
                            }
                            //List<TimingItem> timingToDo = new ArrayList<>();
                            MarketComponentTime marketTime = new MarketComponentTime();
                            marketTime.market = market;
                            marketTime.componentName = componentName;
                            marketTime.component = component;
                            marketTime.subcomponent = subComponent;
                            marketTime.parameters = parameters;
                            //marketTime.timings = timingToDo;
                            marketTime.buy = buy;
                            List<TimingItem> filterTimingsEvolution = getMyTimings(timings, marketName, action, componentName, true, buy, subComponent, parameters);
                            if (evolve) {
                                handleFilterTimings(action, market, marketTime, componentName, filterTimingsEvolution, evolve, param.getInput().getEnddate(), buy, timings, subComponent, parameters);               
                            }
                            // evolve is not false
                            if (getName().equals(IclijConstants.FINDPROFIT)) {
                                List<TimingItem> filterTimings = getMyTimings(timings, marketName, action, componentName, false, buy, subComponent, parameters);
                                handleFilterTimings(action, market, marketTime, componentName, filterTimings, evolve, param.getInput().getEnddate(), buy, timings, subComponent, parameters);
                            }
                            marketTimes.add(marketTime);
                        }
                    }
                }
            }
        }
        return marketTimes;
    }

    private void handleFilterTimings(String action, Market market, MarketComponentTime marketTime,
            String component, List<TimingItem> filterTimings, boolean evolve, LocalDate date, Boolean buy, List<TimingItem> timings, String subComponent, Parameters parameters) {
        if (!filterTimings.isEmpty()) {
            Collections.sort(filterTimings, (o1, o2) -> (o2.getDate().compareTo(o1.getDate())));
            LocalDate olddate = date.minusDays(((long) AVERAGE_SIZE) * getTime(market));
            OptionalDouble average = getAverage(filterTimings);

            if (olddate.isBefore(filterTimings.get(0).getDate())) {
                // no recent enough is found
                marketTime.time += average.orElse(0);
                if (!evolve) {
                    marketTime.time += average.orElse(0);
                }
                marketTime.haverun = true;
            } else {
                // recent enough is found
                // nothing to do
                log.error("should not be here");
            }
        } else {
            List<TimingItem> filterTimingsEvolution = getMyTimings(timings, action, component, evolve, buy, subComponent, parameters);
            OptionalDouble average = getAverage(filterTimingsEvolution);
            marketTime.time += average.orElse(0);
            if (!evolve) {
                marketTime.time += average.orElse(0);
            }
            marketTime.haverun = false;
        }
    }

    private OptionalDouble getAverage(List<TimingItem> timings) {
        Collections.sort(timings, (o1, o2) -> (o2.getDate().compareTo(o1.getDate())));
        int size = Math.min(AVERAGE_SIZE, timings.size());
        return timings
                .subList(0, size)
                .stream()
                .mapToDouble(TimingItem::getMytime)
                .average();
    }

    public class MarketComponentTime {
        String componentName;
        Component component;
        String subcomponent;
        Market market;
        double time;
        boolean haverun;
        //List<TimingItem> timings;
        Boolean buy;
        Parameters parameters;

        @Override
        public String toString() {
            String paramString = JsonUtil.convert(parameters);
            return market.getConfig().getMarket() + " " + componentName + " " + subcomponent + " " + paramString + " " + buy + " " + time + " " + haverun;
        }
    }
    
    private List<TimingItem> getMyTimings(List<TimingItem> timings, String market, String action, String component, boolean evolve, Boolean buy, String subcomponent, Parameters parameters) {
        List<TimingItem> filterTimings = new ArrayList<>();
        String paramString = JsonUtil.convert(parameters);
        for (TimingItem timing : timings) {
            if (buy != null && timing.getBuy() != null && buy != timing.getBuy()) {
                continue;
            }
            if (market.equals(timing.getMarket()) && action.equals(timing.getAction()) && component.equals(timing.getComponent()) && subcomponent.equals(timing.getSubcomponent()) && evolve == timing.isEvolve() && paramString == timing.getParameters()) {
                filterTimings.add(timing);
            }
        }
        return filterTimings;
    }

    private List<TimingItem> getMyTimings(List<TimingItem> timings, String action, String component, boolean evolve, Boolean buy, String subcomponent, Parameters parameters) {
        List<TimingItem> filterTimings = new ArrayList<>();
        String paramString = JsonUtil.convert(parameters);
        for (TimingItem timing : timings) {
            if (timing.getBuy() != null && buy != timing.getBuy()) {
                continue;
            }
            if (action.equals(timing.getAction()) && component.equals(timing.getComponent()) && subcomponent.equals(timing.getSubcomponent()) && evolve == timing.isEvolve() && paramString == timing.getParameters()) {
                filterTimings.add(timing);
            }
        }
        return filterTimings;
    }

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
    
    private void getPicksFiltered(WebData myData, ComponentData param, IclijConfig config, MarketComponentTime marketTime, Boolean evolve) {
        log.info("Getting picks for date {}", param.getInput().getEnddate());
        Map<String, ComponentData> dataMap = new HashMap<>();
        myData.memoryItems = getMemItems(marketTime, myData, param, config, evolve, dataMap);
        Market market = marketTime.market;
        List<MemoryItem> marketMemory = getMarketMemory(marketTime.market);
        if (marketMemory == null) {
            myData.profitData = new ProfitData();
        }
        marketMemory.addAll(myData.memoryItems);
        LocalDate prevdate = getPrevDate(param, market);
        List<MemoryItem> currentList = filterKeepRecent(marketMemory, prevdate, ((int) AVERAGE_SIZE) * getTime(market));
        // or make a new object instead of the object array. use this as a pair
        //System.out.println(currentList.get(0).getRecord());
        Map<Pair<String, Integer>, List<MemoryItem>> listMap = new HashMap<>();
        // map subcat + posit -> list
        currentList.forEach(m -> listGetterAdder(listMap, new ImmutablePair<String, Integer>(m.getComponent(), m.getPosition()), m));
        ProfitInputData inputdata = filterMemoryListMapsWithConfidence(market, listMap);        
        ProfitData profitdata = new ProfitData();
        profitdata.setInputdata(inputdata);
        Map<String, List<Integer>> listComponent = createComponentPositionListMap(inputdata.getListMap());
       
        Map<String, Component> componentMap = new HashMap<>();
        componentMap.put(marketTime.componentName, marketTime.component);

        setValMap(param);
        Map<String, Map<String, Object>> maps = param.getResultMaps();
        Map<String, String> nameMap = getNameMap(maps);
        inputdata.setNameMap(nameMap);

        param.setTimings(new ArrayList<>());
        
        handleComponent(this, market, profitdata, param, listComponent, componentMap, dataMap, marketTime.buy, marketTime.subcomponent, myData, config, marketTime.parameters);
        
        if (!isDataset()) {
        filterIncDecs(param, market, profitdata, maps, true);
        filterIncDecs(param, market, profitdata, maps, false);
        }
        //filterDecs(param, market, profitdata, maps);
        //buys = buys.values().stream().filter(m -> olddate.compareTo(m.getRecord()) <= 0).collect(Collectors.toList());        
        myData.profitData = profitdata;

        Map<String, Object> timingMap = new HashMap<>();
        timingMap.put(market.getConfig().getMarket(), param.getTimings());
        if (marketTime.buy == null || marketTime.buy) {
            myData.timingMap = timingMap;
            if (marketTime.buy != null) {
                myData.updateMap.putAll(param.getUpdateMap());
            }
        } else {
            myData.timingMap2 = timingMap;
            myData.updateMap2.putAll(param.getUpdateMap());
        }
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

    private List<Market> getMarkets(IclijConfig instance) {
        List<Market> markets = null;
        try { 
            markets = IclijXMLConfig.getMarkets(instance);
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        return markets;
    }

    public Map<String, List<Integer>> createComponentPositionListMapOld(Map<Object[], List<MemoryItem>> okListMap) {
        Map<String, List<Integer>> listComponent = new HashMap<>();
        for (Object[] keys : okListMap.keySet()) {
            String component = (String) keys[0];
            Integer position = (Integer) keys[1];
            listGetterAdder(listComponent, component, position);
        }
        return listComponent;
    }

    public Map<String, List<Integer>> createComponentPositionListMap(Map<Pair<String, Integer>, List<MemoryItem>> okListMap) {
        Map<String, List<Integer>> listComponent = new HashMap<>();
        for (Pair<String, Integer> key : okListMap.keySet()) {
            /*
            String component = (String) keys[0];
            String subcomponent = (String) keys[1];
            Integer position = (Integer) keys[2];
            */
            listGetterAdder(listComponent, key.getLeft(), key.getRight());
        }
        return listComponent;
    }

    private void filterIncDecs(ComponentData param, Market market, ProfitData profitdata,
            Map<String, Map<String, Object>> maps, boolean inc) {
        List<String> dates = param.getService().getDates(param.getService().conf.getMarket());        
        String category;
        if (inc) {
            category = market.getFilter().getInccategory();
        } else {
            category = market.getFilter().getDeccategory();
        }
        if (category != null) {
            Map<String, Object> categoryMap = maps.get(category);
            if (categoryMap != null) {
                Integer offsetDays = null;
                Integer days;
                if (inc) {
                    days = market.getFilter().getIncdays();
                } else {
                    days = market.getFilter().getDecdays();
                }
                if (days != null) {
                    if (days == 0) {
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
                       offsetDays = days;
                    }
                }
                Double threshold;
                if (inc) {
                    threshold = market.getFilter().getIncthreshold();
                } else {
                    threshold = market.getFilter().getDecthreshold();
                }
                Map<String, List<List>> listMap3 = getCategoryList(maps, category);
                Map<String, IncDecItem> buysFilter = incdecFilterOnIncreaseValue(market, profitdata.getBuys(), maps, threshold, categoryMap,
                        listMap3, offsetDays, inc);
                profitdata.setBuys(buysFilter);
            }
        }
    }

    protected abstract ProfitInputData filterMemoryListMapsWithConfidence(Market market, Map<Pair<String, Integer>, List<MemoryItem>> listMap);
    
    public List<MemoryItem> getMarketMemory(Market market) {
        List<MemoryItem> marketMemory = null;
        try {
            marketMemory = IclijDbDao.getAll(market.getConfig().getMarket());
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        return marketMemory;
    }

    protected Map<String, String> getNameMap(Map<String, Map<String, Object>> maps) {
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
    
    protected abstract void handleComponent(MarketAction action, Market market, ProfitData profitdata, ComponentData param, Map<String, List<Integer>> listComponent, Map<String, Component> componentMap, Map<String, ComponentData> dataMap, Boolean buy, String subcomponent, WebData myData, IclijConfig config, Parameters parameters);
 
    public List<MemoryItem> filterKeepRecent(List<MemoryItem> marketMemory, LocalDate date, int days) {
        LocalDate olddate = date.minusDays(days);
        for (MemoryItem item : marketMemory) {
            if (item.getRecord() == null) {
                item.setRecord(LocalDate.now());
            }
        }
        // temp workaround
        if (date == null) {
            return marketMemory;
        }
        List<MemoryItem> currentList = marketMemory.stream().filter(m -> olddate.compareTo(m.getFuturedate()) <= 0).collect(Collectors.toList());
        currentList = currentList.stream().filter(m -> date.compareTo(m.getFuturedate()) >= 0).collect(Collectors.toList());
        return currentList;
    }

    public Map<String, List<List>> getCategoryList(Map<String, Map<String, Object>> maps, String category) {
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

    public Map<String, IncDecItem> incdecFilterOnIncreaseValue(Market market, Map<String, IncDecItem> incdecs,
            Map<String, Map<String, Object>> maps, Double threshold, Map<String, Object> categoryMap,
            Map<String, List<List>> listMap3, Integer offsetDays, boolean inc) {
        Map<String, IncDecItem> incdecsFilter = new HashMap<>();
        for(IncDecItem item : incdecs.values()) {
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
                if (value != null) {
                    value = 1 + (value / 100);
                }
            } else {
                Double curValue = list0.get(list0.size() - 1);
                Double oldValue = list0.get(list0.size() - 1 - offsetDays);
                if (curValue != null && oldValue != null) {
                    value = curValue / oldValue;
                }
            }
            if (value == null) {
                continue;
            }
            if (inc && value < threshold) {
                continue;
            }
            if (!inc && value > threshold) {
                continue;
            }
            incdecsFilter.put(key, item);
        }
        return incdecsFilter;
    }

    public List<Market> getMarkets() {
        IclijConfig instance = IclijXMLConfig.getConfigInstance();
        List<Market> markets = null;
        try { 
            markets = IclijXMLConfig.getMarkets(instance);
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        return markets;
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

    public Map<String, Component> getComponentMap(Collection<String> listComponent, Market market) {
        Map<String, Component> componentMap = new HashMap<>();
        for (String componentName : listComponent) {
            Component component = getComponentFactory().factory(componentName);
            if (market != null && componentName.equals(PipelineConstants.PREDICTOR)) {
                MLConfigs mlConfigs = market.getMlconfig();
                if (mlConfigs != null) {
                    MLConfig mlConfig = mlConfigs.getTensorflow().getLstm();
                    if (mlConfig != null) {
                        if (!mlConfig.getEnable()) {
                            //continue;
                        }
                    }
                }
            }
            componentMap.put(componentName, component);
        }
        return componentMap;
    }
    
    public Map<Boolean, String> getBooleanTexts() {
        Map<Boolean, String> map = new HashMap<>();
        map.put(null, "");
        map.put(false, "down");
        map.put(true, "up");
        return map;
    }

    protected abstract String getFuturedays0(IclijConfig conf);
    
    public Integer[] getFuturedays(IclijConfig conf) {
        String thresholdString = getFuturedays0(conf);
        try {
            Double.valueOf(thresholdString);
            log.error("Using old format {}", thresholdString);
            thresholdString = "[" + thresholdString + "]";
        } catch (Exception e) {            
        }
        return JsonUtil.convert(thresholdString, Integer[].class);
    }

    public abstract String getThreshold(IclijConfig conf);
    
    private Double[] getThresholds(IclijConfig conf) {
        String thresholdString = getThreshold(conf);
        try {
            Double.valueOf(thresholdString);
            log.error("Using old format {}", thresholdString);
            thresholdString = "[" + thresholdString + "]";
        } catch (Exception e) {            
        }
        return JsonUtil.convert(thresholdString, Double[].class);
    }

}

