package roart.action;

import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
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
import org.springframework.beans.factory.annotation.Autowired;

import roart.common.config.ConfigConstants;
import roart.common.constants.Constants;
import roart.common.inmemory.model.Inmemory;
import roart.common.model.ActionComponentItem;
import roart.common.model.IncDecItem;
import roart.common.model.MLMetricsItem;
import roart.common.model.MemoryItem;
import roart.common.model.MetaItem;
import roart.common.model.TimingItem;
import roart.common.model.util.MetaUtil;
import roart.common.pipeline.PipelineConstants;
import roart.common.pipeline.data.PipelineData;
import roart.common.pipeline.data.SerialMeta;
import roart.common.pipeline.util.PipelineUtils;
import roart.common.util.JsonUtil;
import roart.common.util.TimeUtil;
import roart.common.webflux.WebFluxUtil;
import roart.iclij.component.Component;
import roart.iclij.component.factory.ComponentFactory;
import roart.component.model.ComponentData;
import roart.component.util.IncDecUtil;
import roart.constants.IclijConstants;
import roart.controller.IclijController;
import roart.db.dao.IclijDbDao;
import roart.filesystem.FileSystemDao;
import roart.iclij.config.IclijConfig;
import roart.iclij.config.IclijConfigConstants;
import roart.iclij.config.IclijXMLConfig;
import roart.iclij.config.MLConfig;
import roart.iclij.config.MLConfigs;
import roart.iclij.config.Market;
import roart.iclij.evolution.fitness.impl.FitnessAboveBelow;
import roart.iclij.factory.actioncomponentconfig.ActionComponentConfigFactory;
import roart.iclij.filter.Memories;
import roart.iclij.model.MapList;
import roart.iclij.model.Parameters;
import roart.iclij.model.Trend;
import roart.iclij.model.WebData;
import roart.iclij.model.action.MarketActionData;
import roart.iclij.model.component.ComponentInput;
import roart.iclij.model.config.ActionComponentConfig;
import roart.iclij.service.ControlService;
import roart.service.model.ProfitData;
import roart.service.model.ProfitInputData;
import roart.iclij.service.util.MarketUtil;
import roart.iclij.service.util.MiscUtil;
import roart.iclij.verifyprofit.VerifyProfitUtil;
import roart.util.ServiceUtil;
import roart.model.io.IO;

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

    protected abstract List<MemoryItem> getMemItems(ActionComponentItem marketTime, WebData myData, ComponentData param, IclijConfig config, Boolean evolve, Map<String, ComponentData> dataMap);

    protected abstract LocalDate getPrevDate(ComponentData param, Market market);

    public abstract void setValMap(ComponentData param);
    
    public int getPriority(IclijConfig conf, String key) {
        Integer value = (Integer) conf.getConfigData().getConfigValueMap().get(key + "[@priority]");
        return value != null ? value : 0;
    }
    
    @Override
    public void goal(Action parent, ComponentData param, Integer priority, IclijConfig iclijConfig, IO io) {
        getMarkets(iclijConfig, parent, new ComponentInput(iclijConfig.getConfigData(), null, null, null, null, true, false, new ArrayList<>(), new HashMap<>()), null, priority, io);
    }

    public WebData getMarket(IclijConfig iclijConfig, Action parent, ComponentData param, Market market, Boolean evolve, Integer priority, List<TimingItem> timingsdone) {
        List<Market> markets = new ArrayList<>();
        if (market != null) {
            markets.add(market);
        } else {
            markets = new MarketUtil().getMarkets(false, iclijConfig);
        }
        return getMarkets(iclijConfig, parent, param, markets, new ArrayList<>(), evolve, priority, timingsdone, false);
    }        
    
    public WebData getMarkets(IclijConfig iclijConfig, Action parent, ComponentInput input, Boolean evolve, Integer priority, IO io) {
        ComponentData param = null;
        try {
            param = ComponentData.getParam(iclijConfig, input, 0, io);
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        List<TimingItem> timings = null;
        try {
            timings = param.getService().getIo().getIdbDao().getAllTiming();
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        List<Market> markets = new MarketUtil().getMarkets(actionData.isDataset(), iclijConfig);
        return getMarkets(iclijConfig, parent, param, markets, timings, evolve, priority, new ArrayList<>(), true);
    }        
    
    private WebData getMarkets(IclijConfig iclijConfig, Action parent, ComponentData paramTemplate, List<Market> markets, List<TimingItem> timings, Boolean evolve, Integer priority, List<TimingItem> timingsdone, boolean auto) {
	// test picks for aggreg recommend, predict etc
        // remember and make confidence
        // memory is with date, confidence %, inc/dec, semantic item
        // find recommended picks
        this.parent = parent;
        WebData myData = getWebData();

        markets = new MarketUtil().filterMarkets(markets, getActionData().isDataset());
        
        List<MetaItem> metas = paramTemplate.getService().getMetas();
            
        IclijConfig config = iclijConfig; //paramTemplate.getInput().getConfigData();
        List<ActionComponentItem> marketTimes = new ArrayList<>();
        Map<String, ComponentData> componentDataMap = new HashMap<>();
        for (Market market : markets) {
            if (market.getConfig().getEnable() != null && !market.getConfig().getEnable()) {
                continue;
            }
            String marketName = market.getConfig().getMarket();
            MetaItem meta = new MetaUtil().findMeta(metas, marketName);
            boolean wantThree = meta != null && Boolean.TRUE.equals(meta.isLhc());
            ComponentInput input = new ComponentInput(config.getConfigData(), null, marketName, null, 0, paramTemplate.getInput().isDoSave(), false, new ArrayList<>(), paramTemplate.getInput().getValuemap());
            ComponentData param = null;
            try {
                param = ComponentData.getParam(iclijConfig, input, 0, market, paramTemplate.getService().getIo());
            } catch (Exception e) {
                log.error(Constants.EXCEPTION, e);
            }
            param.setAction(getName());
            ControlService srv = new ControlService(iclijConfig, param.getService().getIo());
            //srv.getConfig();
            param.setService(srv);
            srv.coremlconf.getConfigData().setMarket(market.getConfig().getMarket());
            boolean skipIsDataset = !timingsdone.isEmpty();
            List<String> stockDates = null;
            if (skipIsDataset || !isDataset()) {
                stockDates = param.getService().getDates(marketName);
                if (stockDates == null || stockDates.isEmpty()) {
                    continue;
                }

                getParamDates(market, param, stockDates);
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
                List<ActionComponentItem> marketTime = getList(getName(), componentMapFiltered, timings, market, param, currentTimings, timingsdone, iclijConfig);
                marketTimes.addAll(marketTime);
            } else {
                int jj = 0;
            }
        }
        if (auto) {
            List<ActionComponentItem> marketTimesFiltered = new ArrayList<>();;
            Set<String> marketTimesIds = new HashSet<>();
            for (ActionComponentItem marketTime : marketTimes) {
                String id = marketTime.toStringId();
                if (!ActionThread.queued.contains(id)) {
                    marketTimesIds.add(id);
                    marketTimesFiltered.add(marketTime);
                } else {
                    log.info("Skipping already queued {}", id);
                }
            }
            ActionThread.queued.addAll(marketTimesIds);
            ActionThread.queue.addAll(marketTimesFiltered);
            return null;
        }
        
        Collections.sort(marketTimes, (o1, o2) -> (Double.valueOf(o2.getTime()).compareTo(Double.valueOf(o1.getTime()))));
        List<ActionComponentItem> run = marketTimes.stream().filter(m -> m.isHaverun()).collect(Collectors.toList());
        List<ActionComponentItem> notrun = marketTimes.stream().filter(m -> !m.isHaverun()).collect(Collectors.toList());

        // Event-ish
        
        for (ActionComponentItem marketTime : marketTimes) {
            log.info("MarketTime {}", marketTime);
        }
        for (ActionComponentItem marketTime : notrun) {
            if (marketTime.getTime() == 0.0) {
                ComponentData param = componentDataMap.get(marketTime.getMarket());
                MetaItem meta = new MetaUtil().findMeta(metas, marketTime.getMarket());
                boolean wantThree = meta != null && Boolean.TRUE.equals(meta.isLhc());
                String actionItem = LocalTime.now() + " " + Thread.currentThread().getId() + " " + marketTime.toStringId();
                getPicksFilteredOuter(myData, param, config, marketTime, evolve, wantThree, actionItem);                
            }
        }
        for (ActionComponentItem marketTime : notrun) {
            if (marketTime.getTime() > 0.0) {
                if (!enoughTime(config, marketTime)) {
                    continue;
                }
                ComponentData param = componentDataMap.get(marketTime.getMarket());
                MetaItem meta = new MetaUtil().findMeta(metas, marketTime.getMarket());
                boolean wantThree = meta != null && Boolean.TRUE.equals(meta.isLhc());
                String actionItem = LocalTime.now() + " " + Thread.currentThread().getId() + " " + marketTime.toStringId();
                getPicksFilteredOuter(myData, param, config, marketTime, evolve, wantThree, actionItem);                
            }            
        }       
        for (ActionComponentItem marketTime : run) {
            if (marketTime.getTime() == 0.0) {
                log.error("should not be here");
            }
            if (marketTime.getTime() > 0.0) {
                if (!enoughTime(config, marketTime)) {
                    continue;
                }
                ComponentData param = componentDataMap.get(marketTime.getMarket());
                MetaItem meta = new MetaUtil().findMeta(metas, marketTime.getMarket());
                boolean wantThree = meta != null && Boolean.TRUE.equals(meta.isLhc());
                String actionItem = LocalTime.now() + " " + Thread.currentThread().getId() + " " + marketTime.toStringId();
                getPicksFilteredOuter(myData, param, config, marketTime, evolve, wantThree, actionItem);                
            }            
        }       
        return myData;
    }

    public void getParamDates(Market market, ComponentData param, List<String> stockDates) {
        String date = getActionData().getParamDateFromConfig(market, stockDates);
        try {
            param.setFuturedays(0);
            param.setOffset(0);
            param.setDates(date, stockDates, getActionData(), market);
        } catch (ParseException e) {
            log.error(Constants.EXCEPTION, e);
        }
    }

    public static boolean enoughTime(IclijConfig config, ActionComponentItem marketTime) {
        if (marketTime.getTime() == 0) {
            return true;
        }
        boolean enoughTime = true;
        if (config.serverShutdownHour() != null) {
            int shutdown = config.serverShutdownHour();
            shutdown --;
            LocalTime now = LocalTime.now();
            int minutes = 60 * now.getHour() + now.getMinute();
            minutes += marketTime.getTime() / 60;
            if (minutes >= shutdown * 60) {
                enoughTime = false;
            }
        }
        return enoughTime;
    }

    public WebData getWebData() {
        WebData myData = new WebData();
        myData.setIncs(new ArrayList<>());
        myData.setDecs(new ArrayList<>());
        myData.setUpdateMap(new HashMap<>());
        myData.setTimingMap(new HashMap<>());
        myData.setUpdateMap2(new HashMap<>());
        myData.setTimingMap2(new HashMap<>());
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

    private List<ActionComponentItem> getList(String action, Map<String, Component> componentMapFiltered, List<TimingItem> timings, Market market, ComponentData param, List<TimingItem> currentTimings, List<TimingItem> timingsdone, IclijConfig iclijConfig) {
        List<MLMetricsItem> mltests = null;
        try {
            mltests = param.getService().getIo().getIdbDao().getAllMLMetrics(market.getConfig().getMarket(), null, null);
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        List<ActionComponentItem> marketTimes = new ArrayList<>();
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

            Double[] thresholds = getThresholds(iclijConfig);
            Integer[] futuredays = getFuturedays(iclijConfig);
            List<String> subComponents = component.getConfig().getSubComponents(market, iclijConfig, null, getActionData().getMLConfig(iclijConfig));
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
                            ActionComponentItem marketTime = new ActionComponentItem();
                            // TODO
                            marketTime.setMarket(market.getConfig().getMarket());
                            //marketTime.componentName = componentName;
                            marketTime.setAction(this.getActionData().getName());
                            marketTime.setComponent(componentName);
                            marketTime.setSubcomponent(subComponent);
                            marketTime.setParameters(JsonUtil.convert(parameters));
                            //marketTime.timings = timingToDo;
                            marketTime.setBuy(buy);
                            IclijConfig config = iclijConfig; //param.getInput().getConfigData();
                            String mypriorityKey = actionData.getPriority();
                            int aPriority = getPriority(config, mypriorityKey);
                            int mypriority = aPriority + entry.getValue().getConfig().getPriority(config);
                            marketTime.setPriority(mypriority);
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
    
    private void handleFilterTimings(String action, Market market, ActionComponentItem marketTime,
            String component, List<TimingItem> filterTimings, boolean evolve, LocalDate date, Boolean buy, List<TimingItem> timings, String subComponent, Parameters parameters) {
        if (!filterTimings.isEmpty()) {
            Collections.sort(filterTimings, (o1, o2) -> (o2.getDate().compareTo(o1.getDate())));
            LocalDate olddate = date.minusDays(((long) AVERAGE_SIZE) * getActionData().getTime(market));
            OptionalDouble average = getAverage(filterTimings);

            if (true || olddate.isBefore(filterTimings.get(0).getDate())) {
                // no recent enough is found
                marketTime.setTime(marketTime.getTime() + average.orElse(0));
                if (!evolve) {
                    marketTime.setTime(marketTime.getTime() + average.orElse(0));
                }
                marketTime.setHaverun(true);
            } else {
                // recent enough is found
                // nothing to do
                log.error("should not be here");
            }
        } else {
            List<TimingItem> filterTimingsEvolution = getMyTimings(timings, action, component, evolve, buy, subComponent, parameters);
            OptionalDouble average = getAverage(filterTimingsEvolution);
            marketTime.setTime(marketTime.getTime() + average.orElse(0));
            if (!evolve) {
                marketTime.setTime(marketTime.getTime() + average.orElse(0));
            }
            marketTime.setHaverun(false);
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
    
    @Deprecated
    public ActionComponentItem getActionComponent(String componentName, Component component, String subcomponent, Market market, double time, boolean haverun, Boolean buy, Parameters parameters) {
        ActionComponentItem mct = new ActionComponentItem();
        //mct.componentName = componentName;
        mct.setComponent(componentName);
        mct.setSubcomponent(subcomponent);
        mct.setMarket(market.getConfig().getMarket());
        mct.setTime(time);
        mct.setHaverun(haverun);
        mct.setBuy(buy);
        mct.setParameters(JsonUtil.convert(parameters));
        return mct;
    }
    
    private List<TimingItem> getMyTimings(List<TimingItem> timings, String market, String action, String component, boolean evolve, Boolean buy, String subcomponent, Parameters parameters) {
        List<TimingItem> filterTimings = new ArrayList<>();
        String paramString = JsonUtil.convert(parameters);
        for (TimingItem timing : timings) {
            if (!Objects.equals(buy,timing.getBuy())) {
                continue;
            }
            if (market.equals(timing.getMarket()) && action.equals(timing.getAction()) && component.equals(timing.getComponent()) && myequals(subcomponent, timing.getSubcomponent()) && evolve == timing.isEvolve() && Objects.equals(paramString, timing.getParameters())) {
                filterTimings.add(timing);
            }
        }
        return filterTimings;
    }

    private List<TimingItem> getMyTimings(List<TimingItem> timings, String action, String component, boolean evolve, Boolean buy, String subcomponent, Parameters parameters) {
        List<TimingItem> filterTimings = new ArrayList<>();
        String paramString = JsonUtil.convert(parameters);
        for (TimingItem timing : timings) {
            if (!Objects.equals(buy, timing.getBuy())) {
                continue;
            }
            if (action.equals(timing.getAction()) && component.equals(timing.getComponent()) && myequals(subcomponent, timing.getSubcomponent()) && evolve == timing.isEvolve() && Objects.equals(paramString, timing.getParameters())) {
                filterTimings.add(timing);
            }
        }
        return filterTimings;
    }
    
    public void getPicksFilteredOuter(WebData myData, ComponentData param, IclijConfig config, ActionComponentItem marketTime, Boolean evolve, Boolean wantThree, String actionItem) {
        IclijController.taskList.add(actionItem);
        try {
            getPicksFiltered(myData, param, config, marketTime, evolve, wantThree);                
        } catch (Exception e) {
            throw e;
        } finally {
            IclijController.taskList.remove(actionItem);
        }
    }

    public void getPicksFiltered(WebData myData, ComponentData param, IclijConfig config, ActionComponentItem marketTime, Boolean evolve, boolean wantThree) {
        log.info("Getting picks for date {}", param.getInput().getEnddate());
        Market market = new MarketUtil().findMarket(marketTime.getMarket(), config);
        Map<String, ComponentData> dataMap = new HashMap<>();
        ProfitData profitdata = new ProfitData();
        Memories listComponentMap = new Memories(market);
        myData.setMemoryItems(getMemItems(marketTime, myData, param, config, evolve, dataMap));
        LocalDate prevdate = getPrevDate(param, market);
        LocalDate olddate = prevdate.minusDays(((int) AVERAGE_SIZE) * getActionData().getTime(market));
        ProfitInputData inputdata = new ProfitInputData();
        Parameters parameters = JsonUtil.convert(marketTime.getParameters(), Parameters.class);
        getListComponents(myData, param, config, parameters, evolve, market, dataMap, listComponentMap, olddate, prevdate);
        profitdata.setInputdata(inputdata);
        
        Map<String, Component> componentMap = new HashMap<>();
        Component acomponent = new ComponentFactory().factory(marketTime.getComponent());
        ActionComponentConfig aconfig = ActionComponentConfigFactory.factoryfactory(getName()).factory(acomponent.getPipeline());
        acomponent.setConfig(aconfig);
        componentMap.put(marketTime.getComponent(), acomponent);

        setValMap(param);
        PipelineData[] maps = param.getResultMaps();
        // TODO bad
        Inmemory inmemory = param.getService().getIo().getInmemoryFactory().get(config);
        PipelineData metadata = PipelineUtils.getPipeline(maps, PipelineConstants.META, inmemory);
        PipelineData pipeline = PipelineUtils.getPipeline(maps, PipelineUtils.getMetaCat(metadata), inmemory);
        String catName = PipelineUtils.getMetaCat(metadata);
        Map<String, String> nameMap = PipelineUtils.getNamemap(PipelineUtils.getPipeline(maps, catName, inmemory));
        log.info("TODO names {}", nameMap.size());
        inputdata.setNameMap(nameMap);

        param.setTimings(new ArrayList<>());
        
        List<TimingItem> timings = null;
        try {
            timings = param.getService().getIo().getIdbDao().getAllTiming(market.getConfig().getMarket(), IclijConstants.MACHINELEARNING, olddate, prevdate);
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        List<MLMetricsItem> mltests = null;
        try {
            mltests = param.getService().getIo().getIdbDao().getAllMLMetrics(market.getConfig().getMarket(), null, null);
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        List<MLMetricsItem> mlTests = null;
        if (true || config.getFindProfitMemoryFilter()) {
        mlTests = getMLMetrics(timings, mltests, market.getFilter().getConfidence());
        }
        Parameters parameters2 = JsonUtil.convert(marketTime.getParameters(), Parameters.class);
        handleComponent(this, market, profitdata, param, listComponentMap, componentMap, dataMap, marketTime.getBuy(), marketTime.getSubcomponent(), myData, config, parameters2, wantThree, mlTests);
        //handleMLMeta(this, param, valueMap, component.getPipeline());
        //saveTiming(this, param, subcomponent, mlmarket, parameters, scoreMap, time0, false);
        PipelineUtils.printkeys(param.getResultMaps());
        //log.info("Printkeys {} {}", componentMap.keySet(), dataMap.keySet());
        for (Entry<String, ComponentData> entry : dataMap.entrySet() ) {
            ComponentData component = entry.getValue();
            PipelineUtils.printkeys(component.getResultMaps());
        }
        
        if (!getActionData().isDataset()) {
        new IncDecUtil().filterIncDecs(param, market, profitdata, maps, true, null, inmemory);
        new IncDecUtil().filterIncDecs(param, market, profitdata, maps, false, null, inmemory);
        }
        //filterDecs(param, market, profitdata, maps);
        //buys = buys.values().stream().filter(m -> olddate.compareTo(m.getRecord()) <= 0).collect(Collectors.toList());        
        myData.setProfitData(profitdata);

        Map<String, List<TimingItem>> timingMap = new HashMap<>();
        timingMap.put(market.getConfig().getMarket(), param.getTimings());
        if (marketTime.getBuy() == null || marketTime.getBuy()) {
            myData.setTimingMap(timingMap);
            if (param.getUpdateMap() != null) {
                myData.getUpdateMap().putAll(param.getUpdateMap());
            }
        } else {
            myData.setTimingMap2(timingMap);
            if (param.getUpdateMap() != null) {
                myData.getUpdateMap2().putAll(param.getUpdateMap());
            }
        }
    }

    private List<MLMetricsItem> getMLMetrics(List<TimingItem> timings, List<MLMetricsItem> mltests, Double confidence) {
        mltests = filterMetrics(mltests, confidence);
        if (true) {
            List<MLMetricsItem> list = new ArrayList<>();
            Map<Pair<String, String>, List<MLMetricsItem>> map = getMLMetricsNew(mltests, confidence, false);
            for (Entry<Pair<String, String>, List<MLMetricsItem>> entry : map.entrySet()) {
                list.add(entry.getValue().get(0));
            }
            return list;
        }
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

    protected Map<Pair<String, String>, List<MLMetricsItem>> getMLMetricsNew(List<MLMetricsItem> mltests, Double confidence, boolean component) {
        if (confidence != null) {
            mltests = filterMetrics(mltests, confidence);
        } else {
            confidence = 0.0;
        }
        Map<Triple<String, String, String>, List<MLMetricsItem>> moreReturnedMLMetrics = new HashMap<>();
        for (MLMetricsItem metric : mltests) {
            if (metric.getTestAccuracy() == null || metric.getTestAccuracy() < confidence) {
                continue;
            }
            if (metric.getThreshold() == null || metric.getThreshold() != 1.0) {
                continue;
            }
            Triple key = new ImmutableTriple(metric.getComponent(), metric.getSubcomponent(), metric.getLocalcomponent());
            new MiscUtil().listGetterAdder(moreReturnedMLMetrics, key, metric);  
        }
        Comparator<MLMetricsItem> compareById = new Comparator<>() {
            @Override
            public int compare(MLMetricsItem o1, MLMetricsItem o2) {
                return o1.getRecord().compareTo(o2.getRecord());
            }
        };
        Map<Pair<String, String>, List<MLMetricsItem>> moreReturnedMLMetrics2 = new HashMap<>();
        for (Entry<Triple<String, String, String>, List<MLMetricsItem>> entry : moreReturnedMLMetrics.entrySet()) {
            Triple<String, String, String> key = entry.getKey();
            List<MLMetricsItem> value = entry.getValue();
            Collections.sort(value, compareById);
            Collections.reverse(value);
            Pair<String, String> newkey = new ImmutablePair(key.getLeft(), key.getMiddle());
            new MiscUtil().listGetterAdder(moreReturnedMLMetrics2, newkey, value.get(0));              
        }
        if (component) {
            Map<Pair<String, String>, List<MLMetricsItem>> moreReturnedMLMetrics3 = new HashMap<>();
            for (Entry<Pair<String, String>, List<MLMetricsItem>> entry : moreReturnedMLMetrics2.entrySet()) {
                Pair<String, String> key = entry.getKey();
                List<MLMetricsItem> value = entry.getValue();
                Pair<String, String> newkey = new ImmutablePair(key.getLeft(), null);
                new MiscUtil().listGetterAdder(moreReturnedMLMetrics3, newkey, value.get(0));              
            }

            moreReturnedMLMetrics2.putAll(moreReturnedMLMetrics3);
        }
        return moreReturnedMLMetrics2;
    }

    protected Map<Pair<String, String>, List<MLMetricsItem>> getMLMetrics(List<MLMetricsItem> mltests, Double confidence) {
        if (true) {
            return getMLMetricsNew(mltests, confidence, false);
        }
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

    protected Map<Pair<String, String>, List<MLMetricsItem>> getMLMetrics2(List<MLMetricsItem> mltests, Double confidence) {
        if (true) {
            return getMLMetricsNew(mltests, confidence, true);
        }
        List<MLMetricsItem> returnedMLMetrics = new ArrayList<>();
        for (MLMetricsItem test : mltests) {
            addNewest(returnedMLMetrics, test, 0.0);
        }
        Map<Pair<String, String>, List<MLMetricsItem>> moreReturnedMLMetrics = new HashMap<>();
        for (MLMetricsItem metric : returnedMLMetrics) {
            Pair<String, String> key = new ImmutablePair(metric.getComponent(), null);
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

    private List<MLMetricsItem> filterMetrics(List<MLMetricsItem> items, double confidence) {
        List<MLMetricsItem> retList = new ArrayList<>();
        return items.stream()
                .filter(e -> (e.getTestAccuracy() != null && e.getTestAccuracy() >= confidence))
                .collect(Collectors.toList());
    }
    
    public void getListComponents(WebData myData, ComponentData param, IclijConfig config,
            Parameters parameters, Boolean evolve, Market market, Map<String, ComponentData> dataMap,
            Memories memories, LocalDate olddate, LocalDate prevdate) {
        List<MemoryItem> marketMemory = new MarketUtil().getMarketMemory(market, IclijConstants.IMPROVEABOVEBELOW, null, null, JsonUtil.convert(parameters), olddate, prevdate, param.getService().getIo().getIdbDao());
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

    protected Map<String, String> getNameMap(PipelineData[] maps) {
        Map<String, String> nameMap = null;
        for (Entry<String, PipelineData> entry : PipelineUtils.getPipelineMap(maps).entrySet()) {
            PipelineData map = entry.getValue();
            nameMap = PipelineUtils.getNamemap(map);
            if (nameMap != null) {
                break;
            }
        }
        return nameMap;
    }
    
    protected abstract void handleComponent(MarketAction action, Market market, ProfitData profitdata, ComponentData param, Memories listComponent, Map<String, Component> componentMap, Map<String, ComponentData> dataMap, Boolean buy, String subcomponent, WebData myData, IclijConfig config, Parameters parameters, boolean wantThree, List<MLMetricsItem> mlTests);
 
    public Map<String, Component> getComponentMap(Collection<String> listComponent, Market market) {
        Map<String, Component> componentMap = new HashMap<>();
        for (String componentName : listComponent) {
            Component component = new ComponentFactory().factory(componentName);
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

}

