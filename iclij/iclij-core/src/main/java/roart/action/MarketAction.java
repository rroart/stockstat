package roart.action;

import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.OptionalDouble;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.common.config.ConfigConstants;
import roart.common.constants.Constants;
import roart.common.model.MetaItem;
import roart.common.pipeline.PipelineConstants;
import roart.common.util.JsonUtil;
import roart.common.util.MetaUtil;
import roart.common.util.TimeUtil;
import roart.component.Component;
import roart.component.ComponentFactory;
import roart.component.FitnessAboveBelow;
import roart.component.model.ComponentData;
import roart.constants.IclijConstants;
import roart.db.IclijDbDao;
import roart.iclij.config.IclijConfig;
import roart.iclij.config.IclijConfigConstants;
import roart.iclij.config.IclijXMLConfig;
import roart.iclij.config.MLConfig;
import roart.iclij.config.MLConfigs;
import roart.iclij.config.Market;
import roart.iclij.factory.actioncomponentconfig.ActionComponentConfigFactory;
import roart.iclij.filter.Memories;
import roart.iclij.model.IncDecItem;
import roart.iclij.model.MLMetricsItem;
import roart.iclij.model.MapList;
import roart.iclij.model.MemoryItem;
import roart.iclij.model.Parameters;
import roart.iclij.model.TimingItem;
import roart.iclij.model.Trend;
import roart.iclij.model.WebData;
import roart.iclij.model.action.MarketActionData;
import roart.iclij.model.component.ComponentInput;
import roart.iclij.model.config.ActionComponentConfig;
import roart.iclij.service.ControlService;
import roart.service.model.ProfitData;
import roart.service.model.ProfitInputData;
import roart.iclij.util.MarketUtil;
import roart.iclij.util.MiscUtil;
import roart.iclij.verifyprofit.VerifyProfitUtil;
import roart.util.ServiceUtil;

public abstract class MarketAction extends Action {

    private Logger log = LoggerFactory.getLogger(this.getClass());
    
    private MarketActionData actionData;
    
    protected abstract List<IncDecItem> getIncDecItems();

    protected abstract List getAnArray();

    protected abstract Boolean getBool();

    private Action parent;
    
    public Action getParent() {
        return parent;
    }

    public void setParent(Action parent) {
        this.parent = parent;
    }

    public String getName() {
        return getActionData().getName();
    }
    
    protected List<String> getProfitComponents(IclijConfig config, boolean wantThree) {
        return getActionData().getComponents(config, wantThree);
    }

    protected abstract boolean getEvolve(Component component, ComponentData param);

    protected abstract List<MemoryItem> getMemItems(MarketComponentTime marketTime, WebData myData, ComponentData param, IclijConfig config, Boolean evolve, Map<String, ComponentData> dataMap);

    protected abstract LocalDate getPrevDate(ComponentData param, Market market);

    public abstract void setValMap(ComponentData param);
    
    public ComponentFactory getComponentFactory() {
        return new ComponentFactory();
    }
    
    public int getPriority(IclijConfig conf, String key) {
        Integer value = (Integer) conf.getConfigValueMap().get(key + "[@priority]");
        return value != null ? value : 0;
    }
    
    @Override
    public void goal(Action parent, ComponentData param, Integer priority) {
        getMarkets(parent, new ComponentInput(IclijXMLConfig.getConfigInstance(), null, null, null, null, true, false, new ArrayList<>(), new HashMap<>()), null, priority);
    }

    public WebData getMarket(Action parent, ComponentData param, Market market, Boolean evolve, Integer priority, List<TimingItem> timingsdone) {
        List<Market> markets = new ArrayList<>();
        if (market != null) {
            markets.add(market);
        } else {
            markets = new MarketUtil().getMarkets(false);
        }
        return getMarkets(parent, param, markets, new ArrayList<>(), evolve, priority, timingsdone);
    }        
    
    public WebData getMarkets(Action parent, ComponentInput input, Boolean evolve, Integer priority) {
        List<TimingItem> timings = null;
        try {
            timings = IclijDbDao.getAllTiming();
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        List<Market> markets = new MarketUtil().getMarkets(actionData.isDataset());
        ComponentData param = null;
        try {
            param = ComponentData.getParam(input, 0);
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        return getMarkets(parent, param, markets, timings, evolve, priority, new ArrayList<>());
    }        
    
    private WebData getMarkets(Action parent, ComponentData paramTemplate, List<Market> markets, List<TimingItem> timings, Boolean evolve, Integer priority, List<TimingItem> timingsdone) {
	// test picks for aggreg recommend, predict etc
        // remember and make confidence
        // memory is with date, confidence %, inc/dec, semantic item
        // find recommended picks
        this.parent = parent;
        WebData myData = new WebData();
        myData.setIncs(new ArrayList<>());
        myData.setDecs(new ArrayList<>());
        myData.setUpdateMap(new HashMap<>());
        myData.setTimingMap(new HashMap<>());
        myData.setUpdateMap2(new HashMap<>());
        myData.setTimingMap2(new HashMap<>());

        markets = new MarketUtil().filterMarkets(markets, getActionData().isDataset());
        
        List<MetaItem> metas = paramTemplate.getService().getMetas();
            
        IclijConfig config = paramTemplate.getInput().getConfig();
        List<MarketComponentTime> marketTimes = new ArrayList<>();
        Map<String, ComponentData> componentDataMap = new HashMap<>();
        for (Market market : markets) {
            String marketName = market.getConfig().getMarket();
            MetaItem meta = new MetaUtil().findMeta(metas, marketName);
            boolean wantThree = meta != null && Boolean.TRUE.equals(meta.isLhc());
            ComponentInput input = new ComponentInput(config, null, marketName, null, 0, paramTemplate.getInput().isDoSave(), false, new ArrayList<>(), new HashMap<>());
            ComponentData param = null;
            try {
                param = ComponentData.getParam(input, 0);
            } catch (Exception e) {
                log.error(Constants.EXCEPTION, e);
            }
            param.setAction(getName());
            ControlService srv = new ControlService();
            //srv.getConfig();
            param.setService(srv);
            srv.conf.setMarket(market.getConfig().getMarket());
            boolean skipIsDataset = !timingsdone.isEmpty();
            List<String> stockDates = null;
            if (skipIsDataset || !isDataset()) {
                stockDates = param.getService().getDates(marketName);
                if (stockDates == null || stockDates.isEmpty()) {
                    continue;
                }

                String date = getActionData().getParamDateFromConfig(market, stockDates);
                try {
                    param.setFuturedays(0);
                    param.setOffset(0);
                    param.setDates(date, stockDates, getActionData(), market);
                } catch (ParseException e) {
                    log.error(Constants.EXCEPTION, e);
                }

                /*
                try {
                    param.setDates(null, stockDates, this.getActionData(), market);
                } catch (ParseException e) {
                    log.error(Constants.EXCEPTION, e);
                }
                */
            }
            componentDataMap.put(marketName, param);
            LocalDate olddate = param.getInput().getEnddate();

            try {
                //param.setFuturedays(market.getFilter().getRecordage());
                //param.setDates(market.getFilter().getRecordage(), 0, TimeUtil.convertDate2(olddate));
            } catch (Exception e) {
                log.error(Constants.EXCEPTION, e);
            }
            Short time = getActionData().getTime(market);
            if (getActionData().getTime(market) == null) {
                continue;
            }
            List<TimingItem> currentTimings = getCurrentTimings(olddate, timings, market, getName(), time, false, stockDates);
            List<IncDecItem> currentIncDecs = null; // ServiceUtil.getCurrentIncDecs(olddate, incdecitems, market);
            if (true) {
                List<String> componentList = getProfitComponents(config, wantThree);
                Map<String, Component> componentMap = getComponentMap(componentList, market);
                Map<String, Component> componentMapFiltered = new HashMap<>();
                for (Entry<String, Component> entry : componentMap.entrySet()) {
                    String mypriorityKey = actionData.getPriority();
                    int aPriority = getPriority(config, mypriorityKey);
                    int mypriority = aPriority + entry.getValue().getConfig().getPriority(config);
                    if (priority == null || (mypriority >= priority && mypriority < (priority + 9))) {
                        componentMapFiltered.put(entry.getKey(),  entry.getValue());
                    }
                }
                List<MarketComponentTime> marketTime = getList(getName(), componentMapFiltered, timings, market, param, currentTimings, timingsdone);
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
                MetaItem meta = new MetaUtil().findMeta(metas, marketTime.market.getConfig().getMarket());
                boolean wantThree = meta != null && Boolean.TRUE.equals(meta.isLhc());
                getPicksFiltered(myData, param, config, marketTime, evolve, wantThree);                
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
                MetaItem meta = new MetaUtil().findMeta(metas, marketTime.market.getConfig().getMarket());
                boolean wantThree = meta != null && Boolean.TRUE.equals(meta.isLhc());
                getPicksFiltered(myData, param, config, marketTime, evolve, wantThree);                
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
                MetaItem meta = new MetaUtil().findMeta(metas, marketTime.market.getConfig().getMarket());
                boolean wantThree = meta != null && Boolean.TRUE.equals(meta.isLhc());
                getPicksFiltered(myData, param, config, marketTime, evolve, wantThree);                
            }            
        }       
        return myData;
    }
    
    protected List<TimingItem> getCurrentTimings(LocalDate olddate, List<TimingItem> timings, Market market, String name,
            Short time, boolean b, List<String> stockDates) {
        return new MiscUtil().getCurrentTimings(olddate, timings, market, getName(), time, false);
    }

    protected boolean isDataset() {
        return getActionData().isDataset();
    }

    protected static final int AVERAGE_SIZE = 5;

    private List<MarketComponentTime> getList(String action, Map<String, Component> componentMapFiltered, List<TimingItem> timings, Market market, ComponentData param, List<TimingItem> currentTimings, List<TimingItem> timingsdone) {
        List<MLMetricsItem> mltests = null;
        try {
            mltests = IclijDbDao.getAllMLMetrics(market.getConfig().getMarket(), null, null);
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        List<MarketComponentTime> marketTimes = new ArrayList<>();
        String marketName = market.getConfig().getMarket();
        Double confidence = null;
        if (!isDataset()) {
            confidence = market.getFilter().getConfidence();
        }
        //= ServiceUtil.getFindProfitComponents(config);
        for (Entry<String, Component> entry : componentMapFiltered.entrySet()) {
            String componentName = entry.getKey();
            if (!isDataset()) {
                boolean skipComponent = getSkipComponent(mltests, confidence, componentName);
                if (skipComponent) {
                    log.info("Skipping component {} {}", market.getConfig().getMarket(), componentName);
                    continue;
                }
                boolean skipDoneComponent = getSkipComponent(timingsdone, componentName);
                if (false && skipDoneComponent) {
                    log.info("Skipping done component {} {}", market.getConfig().getMarket(), componentName);
                    continue;
                }
            }
            Component component = entry.getValue();
            boolean evolve = getEvolve(component, param);

            Double[] thresholds = getThresholds(param.getInput().getConfig());
            Integer[] futuredays = getFuturedays(param.getInput().getConfig());
            List<String> subComponents = component.getConfig().getSubComponents(market, param.getInput().getConfig(), null);
            for(String subComponent : subComponents) {
                if (!isDataset()) {
                    boolean skipSubcomponent = getSkipSubComponent(mltests, confidence, componentName, subComponent);
                    if (skipSubcomponent) {
                        log.info("Skipping component subcomponent {} {} {}", market.getConfig().getMarket(), componentName, subComponent);
                        continue;
                    }
                    boolean skipDoneSubcomponent = getSkipSubComponent(timingsdone, componentName, subComponent);
                    if (skipDoneSubcomponent) {
                        log.info("Skipping done component subcomponent {} {} {}", market.getConfig().getMarket(), componentName, subComponent);
                        continue;
                    }
                }
                for (Integer aFutureday : futuredays) {
                    for (Double threshold : thresholds) {
                        Parameters parameters = new Parameters();
                        parameters.setThreshold(threshold);
                        parameters.setFuturedays(aFutureday);
                        String parameterString = JsonUtil.convert(parameters);
                        Boolean[] booleans = getActionData().getBooleans();
                        for (Boolean buy : booleans) {
                            List<TimingItem> currentTimingFiltered = currentTimings.stream().filter(m -> m != null 
                                    && componentName.equals(m.getComponent()) 
                                    && (subComponents == null || myequals(subComponent, m.getSubcomponent())) 
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

    private boolean myequals(String subComponent, String otherSubcomponent) {
        if (subComponent == null) {
            return otherSubcomponent == null;
        } else {
            return subComponent.equals(otherSubcomponent);
        }
    }

    protected boolean getSkipComponent(List<MLMetricsItem> mltests, Double confidence, String componentName) {
        return false;
    }
    
    protected boolean getSkipSubComponent(List<MLMetricsItem> mltests, Double confidence, String componentName,
            String subComponent) {
        return false;
    }
    
    private void handleFilterTimings(String action, Market market, MarketComponentTime marketTime,
            String component, List<TimingItem> filterTimings, boolean evolve, LocalDate date, Boolean buy, List<TimingItem> timings, String subComponent, Parameters parameters) {
        if (!filterTimings.isEmpty()) {
            Collections.sort(filterTimings, (o1, o2) -> (o2.getDate().compareTo(o1.getDate())));
            LocalDate olddate = date.minusDays(((long) AVERAGE_SIZE) * getActionData().getTime(market));
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
    
    public MarketComponentTime getMCT(String componentName, Component component, String subcomponent, Market market, double time, boolean haverun, Boolean buy, Parameters parameters) {
        MarketComponentTime mct = new MarketComponentTime();
        mct.componentName = componentName;
        mct.component = component;
        mct.subcomponent = subcomponent;
        mct.market = market;
        mct.time = time;
        mct.haverun = haverun;
        mct.buy = buy;
        mct.parameters = parameters;
        return mct;
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
            if (market.equals(timing.getMarket()) && action.equals(timing.getAction()) && component.equals(timing.getComponent()) && myequals(subcomponent, timing.getSubcomponent()) && evolve == timing.isEvolve() && paramString == timing.getParameters()) {
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
            if (action.equals(timing.getAction()) && component.equals(timing.getComponent()) && myequals(subcomponent, timing.getSubcomponent()) && evolve == timing.isEvolve() && paramString == timing.getParameters()) {
                filterTimings.add(timing);
            }
        }
        return filterTimings;
    }

    public void getPicksFiltered(WebData myData, ComponentData param, IclijConfig config, MarketComponentTime marketTime, Boolean evolve, boolean wantThree) {
        log.info("Getting picks for date {}", param.getInput().getEnddate());
        Market market = marketTime.market;
        Map<String, ComponentData> dataMap = new HashMap<>();
        ProfitData profitdata = new ProfitData();
        Memories listComponentMap = new Memories(market);
        myData.setMemoryItems(getMemItems(marketTime, myData, param, config, evolve, dataMap));
        LocalDate prevdate = getPrevDate(param, market);
        LocalDate olddate = prevdate.minusDays(((int) AVERAGE_SIZE) * getActionData().getTime(market));
        ProfitInputData inputdata = new ProfitInputData();
        getListComponents(myData, param, config, marketTime.parameters, evolve, market, dataMap, listComponentMap, olddate, prevdate);
        profitdata.setInputdata(inputdata);
        
        Map<String, Component> componentMap = new HashMap<>();
        componentMap.put(marketTime.componentName, marketTime.component);

        setValMap(param);
        Map<String, Map<String, Object>> maps = param.getResultMaps();
        Map<String, String> nameMap = getNameMap(maps);
        inputdata.setNameMap(nameMap);

        param.setTimings(new ArrayList<>());
        
        List<TimingItem> timings = null;
        try {
            timings = IclijDbDao.getAllTiming(market.getConfig().getMarket(), IclijConstants.MACHINELEARNING, olddate, prevdate);
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        List<MLMetricsItem> mltests = null;
        try {
            mltests = IclijDbDao.getAllMLMetrics(market.getConfig().getMarket(), null, null);
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        List<MLMetricsItem> mlTests = null;
        if (true || config.getFindProfitMemoryFilter()) {
        mlTests = getMLMetrics(timings, mltests, market.getFilter().getConfidence());
        }
        handleComponent(this, market, profitdata, param, listComponentMap, componentMap, dataMap, marketTime.buy, marketTime.subcomponent, myData, config, marketTime.parameters, wantThree, mlTests);
        
        if (!getActionData().isDataset()) {
        filterIncDecs(param, market, profitdata, maps, true, null);
        filterIncDecs(param, market, profitdata, maps, false, null);
        }
        //filterDecs(param, market, profitdata, maps);
        //buys = buys.values().stream().filter(m -> olddate.compareTo(m.getRecord()) <= 0).collect(Collectors.toList());        
        myData.setProfitData(profitdata);

        Map<String, List<TimingItem>> timingMap = new HashMap<>();
        timingMap.put(market.getConfig().getMarket(), param.getTimings());
        if (marketTime.buy == null || marketTime.buy) {
            myData.setTimingMap(timingMap);
            if (marketTime.buy != null) {
                myData.getUpdateMap().putAll(param.getUpdateMap());
            }
        } else {
            myData.setTimingMap2(timingMap);
            myData.getUpdateMap2().putAll(param.getUpdateMap());
        }
    }

    private List<MLMetricsItem> getMLMetrics(List<TimingItem> timings, List<MLMetricsItem> mltests, Double confidence) {
        List<MLMetricsItem> returnedMLMetrics = new ArrayList<>();
        // don't need timings anymore
        timings = new ArrayList<>();
        for (TimingItem item : timings) {
            MLMetricsItem test = new MLMetricsItem();
            test.setRecord(item.getRecord());
            test.setDate(item.getDate());
            test.setMarket(item.getMarket());
            test.setComponent(item.getComponent());
            test.setSubcomponent(item.getSubcomponent());
            String param = item.getParameters();
            Parameters parameters = JsonUtil.convert(param, Parameters.class);
            test.setThreshold(parameters.getThreshold());
            test.setTestAccuracy(item.getScore());
            addNewest(returnedMLMetrics, test, confidence);
        }
        for (MLMetricsItem test : mltests) {
            addNewest(returnedMLMetrics, test, confidence);
        }
        return returnedMLMetrics;
    }

    protected Map<Pair<String, String>, List<MLMetricsItem>> getMLMetrics(List<MLMetricsItem> mltests, Double confidence) {
        List<MLMetricsItem> returnedMLMetrics = new ArrayList<>();
        for (MLMetricsItem test : mltests) {
            addNewest(returnedMLMetrics, test, 0.0);
        }
        Map<Pair<String, String>, List<MLMetricsItem>> moreReturnedMLMetrics = new HashMap<>();
        for (MLMetricsItem metric : returnedMLMetrics) {
            Pair key = new ImmutablePair(metric.getComponent(), metric.getSubcomponent());
            new MiscUtil().listGetterAdder(moreReturnedMLMetrics, key, metric);  
        }
        return moreReturnedMLMetrics;
    }

    protected Map<String, List<MLMetricsItem>> getMLMetrics2(List<MLMetricsItem> mltests, Double confidence) {
        List<MLMetricsItem> returnedMLMetrics = new ArrayList<>();
        for (MLMetricsItem test : mltests) {
            addNewest(returnedMLMetrics, test, 0.0);
        }
        Map<String, List<MLMetricsItem>> moreReturnedMLMetrics = new HashMap<>();
        for (MLMetricsItem metric : returnedMLMetrics) {
            String key = metric.getComponent();
            new MiscUtil().listGetterAdder(moreReturnedMLMetrics, key, metric);  
        }
        return moreReturnedMLMetrics;
    }

    private void addNewest(List<MLMetricsItem> mlTests, MLMetricsItem test, Double confidence) {
        if (test.getTestAccuracy() == null || test.getTestAccuracy() < confidence) {
            return;
        }
        if (test.getThreshold() == null || test.getThreshold() != 1.0) {
            return;
        }
        MLMetricsItem replace = null;
        for (MLMetricsItem aTest : mlTests) {
            Boolean moregeneralthan = aTest.moreGeneralThan(test);
            // we don't need this anymore
            if (false && moregeneralthan != null && moregeneralthan) {
                replace = aTest;
                break;
            }
            Boolean olderthan = aTest.olderThan(test);
            if (olderthan != null && olderthan) {
                replace = aTest;
                break;
            }
        }
        if (replace != null) {
            int index = mlTests.indexOf(replace);
            mlTests.set(index, test);
            return;
        }
        mlTests.add(test);
    }

    public void getListComponents(WebData myData, ComponentData param, IclijConfig config,
            Parameters parameters, Boolean evolve, Market market, Map<String, ComponentData> dataMap,
            Memories memories, LocalDate olddate, LocalDate prevdate) {
        List<MemoryItem> marketMemory = new MarketUtil().getMarketMemory(market, IclijConstants.IMPROVEABOVEBELOW, null, null, JsonUtil.convert(parameters), olddate, prevdate);
        marketMemory = marketMemory.stream().filter(e -> "Confidence".equals(e.getType())).collect(Collectors.toList());
        if (!marketMemory.isEmpty()) {
            int jj = 0;
        }
        if (marketMemory == null) {
            myData.setProfitData(new ProfitData());
        }
        //marketMemory.addAll(myData.getMemoryItems());
        List<MemoryItem> currentList = new MiscUtil().filterKeepRecent3(marketMemory, prevdate, ((int) AVERAGE_SIZE) * getActionData().getTime(market), false);
        // map subcat + posit -> list
        currentList = currentList.stream().filter(e -> !e.getComponent().equals(PipelineConstants.ABOVEBELOW)).collect(Collectors.toList());
        memories.method(currentList, config);
     }

    private <T> String nullToEmpty(T s) {
        return s != null ? "" + s : "";
    }

    public void filterIncDecs(ComponentData param, Market market, ProfitData profitdata,
            Map<String, Map<String, Object>> maps, boolean inc, List<String> mydates) {
        List<String> dates;
        if (mydates == null) {
            dates = param.getService().getDates(param.getService().conf.getMarket());        
        } else {
            dates = mydates;
        }
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
                        year = param.getFutureDate().getYear();
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
                Map<String, IncDecItem> buysFilter = incdecFilterOnIncreaseValue(market, inc ? profitdata.getBuys() : profitdata.getSells(), maps, threshold, categoryMap,
                        listMap3, offsetDays, inc);
                if (inc) {
                    profitdata.setBuys(buysFilter);
                } else {
                    profitdata.setSells(buysFilter);
                }
            }
        }
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
    
    protected abstract void handleComponent(MarketAction action, Market market, ProfitData profitdata, ComponentData param, Memories listComponent, Map<String, Component> componentMap, Map<String, ComponentData> dataMap, Boolean buy, String subcomponent, WebData myData, IclijConfig config, Parameters parameters, boolean wantThree, List<MLMetricsItem> mlTests);
 
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
            if (list == null) {
                continue;
            }
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
            if (value == null || threshold == null) {
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

    public Map<String, Component> getComponentMap(Collection<String> listComponent, Market market) {
        Map<String, Component> componentMap = new HashMap<>();
        for (String componentName : listComponent) {
            Component component = getComponentFactory().factory(componentName);
            ActionComponentConfig config = ActionComponentConfigFactory.factoryfactory(getName()).factory(component.getPipeline());
            component.setConfig(config);
            componentMap.put(componentName, component);
        }
        return componentMap;
    }
    
    public Integer[] getFuturedays(IclijConfig conf) {
        String thresholdString = actionData.getFuturedays(conf);
        try {
            Double.valueOf(thresholdString);
            log.error("Using old format {}", thresholdString);
            thresholdString = "[" + thresholdString + "]";
        } catch (Exception e) {            
        }
        return JsonUtil.convert(thresholdString, Integer[].class);
    }

    private Double[] getThresholds(IclijConfig conf) {
        String thresholdString = getActionData().getThreshold(conf);
        try {
            Double.valueOf(thresholdString);
            log.error("Using old format {}", thresholdString);
            thresholdString = "[" + thresholdString + "]";
        } catch (Exception e) {            
        }
        return JsonUtil.convert(thresholdString, Double[].class);
    }

    public MarketActionData getActionData() {
        return actionData;
    }

    public void setActionData(MarketActionData actionData) {
        this.actionData = actionData;
    }

    protected boolean getSkipComponent(List<TimingItem> mltests, String componentName) {
        Map<String, List<TimingItem>> metricsMap = getTiming2(mltests);
        return metricsMap.containsKey(componentName);
    }

    protected boolean getSkipSubComponent(List<TimingItem> mltests, String componentName,
            String subComponent) {
        Map<Pair<String, String>, List<TimingItem>> metricsMap2 = getTiming(mltests);
        return metricsMap2.containsKey(new ImmutablePair(componentName, subComponent));
    }

    protected Map<Pair<String, String>, List<TimingItem>> getTiming(List<TimingItem> mltests) {
        Map<Pair<String, String>, List<TimingItem>> moreReturnedTiming = new HashMap<>();
        for (TimingItem metric : mltests) {
            Pair key = new ImmutablePair(metric.getComponent(), metric.getSubcomponent());
            new MiscUtil().listGetterAdder(moreReturnedTiming, key, metric);  
        }
        return moreReturnedTiming;
    }

    protected Map<String, List<TimingItem>> getTiming2(List<TimingItem> mltests) {
        Map<String, List<TimingItem>> moreReturnedTiming = new HashMap<>();
        for (TimingItem metric : mltests) {
            String key = metric.getComponent();
            new MiscUtil().listGetterAdder(moreReturnedTiming, key, metric);  
        }
        return moreReturnedTiming;
    }

    public void fillProfitdata(ProfitData profitdata, List<IncDecItem> incdecitems) {
        for (IncDecItem item : incdecitems) {
            String id = item.getId() + item.getDate().toString();
            if (item.isIncrease()) {
                profitdata.getBuys().put(id, item);
            } else {
                profitdata.getSells().put(id, item);
            }
        }
    }

}

