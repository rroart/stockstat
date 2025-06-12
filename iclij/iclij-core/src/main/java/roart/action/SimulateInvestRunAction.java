package roart.action;

import java.text.ParseException;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.common.cache.MyCache;
import roart.common.config.CacheConstants;
import roart.common.config.ConfigConstants;
import roart.common.constants.Constants;
import roart.common.constants.EvolveConstants;
import roart.common.constants.ServiceConstants;
import roart.common.inmemory.model.Inmemory;
import roart.common.inmemory.model.InmemoryMessage;
import roart.common.model.ActionComponentDTO;
import roart.common.model.IncDecDTO;
import roart.common.model.MLMetricsDTO;
import roart.common.model.MemoryDTO;
import roart.common.model.SimRunDataDTO;
import roart.common.model.SimDataDTO;
import roart.common.pipeline.PipelineConstants;
import roart.common.pipeline.data.PipelineData;
import roart.common.pipeline.data.SerialList;
import roart.common.pipeline.data.SerialListMap;
import roart.common.pipeline.data.SerialListPlain;
import roart.common.pipeline.data.SerialListSimulateStock;
import roart.common.pipeline.data.SerialListStockHistory;
import roart.common.pipeline.data.SerialScoreChromosome;
import roart.common.pipeline.util.PipelineThreadUtils;
import roart.common.pipeline.util.PipelineUtils;
import roart.common.queue.QueueElement;
import roart.common.util.JsonUtil;
import roart.common.util.MapUtil;
import roart.common.util.TimeUtil;
import roart.component.model.ComponentData;
import roart.constants.SimConstants;
import roart.iclij.component.Component;
import roart.iclij.config.AutoSimulateInvestConfig;
import roart.iclij.config.IclijConfig;
import roart.iclij.config.IclijConfigConstants;
import roart.iclij.config.IclijXMLConfig;
import roart.iclij.config.Market;
import roart.iclij.config.SimulateFilter;
import roart.iclij.config.SimulateInvestConfig;
import roart.iclij.filter.Memories;
import roart.iclij.model.Parameters;
import roart.iclij.model.WebData;
import roart.iclij.model.action.MarketActionData;
import roart.iclij.model.action.SimulateInvestActionData;
import roart.service.model.ProfitData;
import roart.simulate.model.SimulateStock;
import roart.simulate.model.StockHistory;
import roart.simulate.util.SimUtil;

import java.util.function.Function;
import roart.common.pipeline.data.SerialObject;

public class SimulateInvestRunAction extends MarketAction {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    public SimulateInvestRunAction(IclijConfig iclijConfig) {
        setActionData(new SimulateInvestActionData(iclijConfig));
    }

    @Override
    protected List<IncDecDTO> getIncDecDTOs() {
        return null;
    }

    @Override
    protected List getAnArray() {
        return null;
    }

    @Override
    protected Boolean getBool() {
        return null;
    }

    @Override
    protected boolean getEvolve(Component component, ComponentData param) {
        return true;
    }

    @Override
    protected List<MemoryDTO> getMemDTOs(ActionComponentDTO marketTime, WebData myData, ComponentData param,
            IclijConfig config, Boolean evolve, Map<String, ComponentData> dataMap) {
        return new ArrayList<>();
    }

    @Override
    protected LocalDate getPrevDate(ComponentData param, Market market) {
        return param.getInput().getEnddate();
    }

    @Override
    public void setValMap(ComponentData param) {
        param.getAndSetWantedCategoryValueMap(false);
    }

    @Override
    protected void handleComponent(MarketAction action, Market market, ProfitData profitdata, ComponentData param,
            Memories listComponent, Map<String, Component> componentMap, Map<String, ComponentData> dataMap,
            Boolean buy, String subcomponent, WebData myData, IclijConfig config, Parameters parameters,
            boolean wantThree, List<MLMetricsDTO> mlTests) {
        log.info("Component {}", componentMap.keySet());
        for (Entry<String, Component> entry : componentMap.entrySet()) {
            log.info("Component {}", entry.getKey());
            Component component = entry.getValue();
            if (component == null) {
                continue;
            }
            String startDate = config.getSimulateInvestStartdate();
            String endDate = config.getSimulateInvestEnddate();
            LocalDate startLocalDate = null;
            LocalDate endLocalDate = null;
            try {
                startLocalDate = TimeUtil.convertDate(TimeUtil.replace(startDate));
                endLocalDate = TimeUtil.convertDate(TimeUtil.replace(endDate));
            } catch (ParseException e) {
                log.error(Constants.EXCEPTION, e);
            }
            final LocalDate aStartLocalDate = startLocalDate;
            final LocalDate anEndLocalDate = endLocalDate;

            String[] otherMarkets = market.getConfig().getMlmarkets();
            List<String> markets = new ArrayList<>(1 + otherMarkets.length);
            Collections.addAll(markets, market.getConfig().getMarket());
            Collections.addAll(markets, otherMarkets);

            List<SimDataDTO> all = param.getService().getIo().getIdbDao().getAllSimData(market.getConfig().getMarket(), null, null); // fix later: , startDate, endDate);
            List<SimRunDataDTO> all2 = param.getService().getIo().getIdbDao().getAllSimDataRun(market.getConfig().getMarket(), null, null); // fix later: , startDate, endDate);

            Map<Long, SimDataDTO> map = all.stream().collect(Collectors.toMap(SimDataDTO::getDbid, Function.identity()));
            Map<Long, SimRunDataDTO> map2 = all2.stream().collect(Collectors.toMap(SimRunDataDTO::getDbid, Function.identity()));
            Map<Long, SimRunDataDTO> map2sim = all2.stream().filter(e -> e.getStartdate().equals(aStartLocalDate)).filter(e -> e.getEnddate().equals(anEndLocalDate)).collect(Collectors.toMap(SimRunDataDTO::getSimdatadbid, Function.identity()));

            // TODO date check

            Set<Long> set = new HashSet<>(map.keySet());
            Set<Long> set2 = new HashSet<>(map2sim.keySet());
            log.info("set {}", set);
            log.info("set2 {}", set2);
            set.removeAll(set2);
            Map<Long, SimDataDTO> restmap = new HashMap(map);
            restmap.keySet().retainAll(set);
            log.info("set {}", restmap.keySet());

            for (String amarket : markets) {

                /*
                AutoSimulateInvestConfig autoSimConfig = new AutoSimulateInvestConfig();
                autoSimConfig.setStartdate(startDate);
                autoSimConfig.setEnddate(endDate);
                autoSimConfig.setScorelimit(0.5);
                autoSimConfig.setPeriod(0); // meaning 1 month
                autoSimConfig.setInterval(1);
                List<SimulateFilter> autofilter = new ArrayList<>();
                List<SimulateFilter[]> filters = new ArrayList<>();
                */
                // TODO Auto-generated method stub
                /*
        Map<Pair<LocalDate, LocalDate>, List<Pair<Long, SimulateInvestConfig>>> simConfigs = getSimConfigs(market.getConfig().getMarket(), autoSimConfig, autofilter, filters, config, getActionData(), param);
        Mydate mydate = new Mydate();
        try {
            mydate.date = TimeUtil.convertDate("2020.07.01");
            mydate.date = TimeUtil.convertDate("2024.01.01");
        } catch (ParseException e) {
            log.error(Constants.EXCEPTION, e);
        }
        Set<Pair<LocalDate, LocalDate>> keys = simConfigs.keySet();
        log.info("aconf" + simConfigs.size());
        log.info("aconf" + simConfigs.keySet().stream().map(k -> k.getLeft().toString() + " " + k.getRight().toString()).toList());
                 */
                /*
        List<Pair<Long, SimulateInvestConfig>> simsConfigs = new ArrayList<>();
        for (Entry<Pair<LocalDate, LocalDate>, List<Pair<Long, SimulateInvestConfig>>> entry : simConfigs.entrySet()) {
            log.info("aconf" + entry.getValue().size());
            simsConfigs.addAll(entry.getValue());
        }
                 */

                //List<Pair<Long, SimulateInvestConfig>> simsConfigs = getSimConfigs(simConfigs, mydate, keys, market);
                /*
        log.info("aconf" + simsConfigs.size());
        for (Pair<Long, SimulateInvestConfig> pair : simsConfigs) {
            SimulateInvestConfig aConf = pair.getRight();
            log.info("aconf" + aConf);
        }
                 */
                //if (true) return;

                //param.getAndSetCategoryValueMap();
                //component.set(market, param, profitdata, positions, evolve);
                //ComponentData componentData = component.handle(market, param, profitdata, positions, evolve, new HashMap<>());
                // 0 ok?
                param.getConfigValueMap().put(ConfigConstants.MISCMYTABLEDAYS, 0);
                param.getConfigValueMap().put(ConfigConstants.MISCMYDAYS, 0);

                Map<String, Object> aMap = new HashMap<>();
                aMap.put(ConfigConstants.MISCMYTABLEDAYS, 0);
                aMap.put(ConfigConstants.MISCMYDAYS, 0);
                //valueMap.put(ConfigConstants.MACHINELEARNING, false);
                aMap.put(ConfigConstants.AGGREGATORS, false);
                aMap.put(ConfigConstants.INDICATORSRSIRECOMMEND, false);
                aMap.put(ConfigConstants.AGGREGATORSINDICATORRECOMMENDER, false);
                aMap.put(ConfigConstants.AGGREGATORS, false);

                // TODO
                // TODO getName() gives null
                log.info("cache disabled {}", param.isDisableCache());
                param.getResultMap(getName(), aMap, false, param.isKeepPipeline()); // TODO cache

                Map defaultMap = config.getConfigData().getConfigMaps().deflt;

                boolean evolve = false; // param.getInput().getConfig().wantEvolveML();
                int i = 0;
                for (Entry<Long, SimDataDTO> entry2 : restmap.entrySet()) {
                    //if (i++ > 3) break;
                    SimulateInvestConfig aConf; // = simsConfigs.get(i).getRight();
                    SimDataDTO simData = entry2.getValue();
                    SimulateInvestConfig simConf = new SimUtil().getSimulateInvestConfig(config, simData);
                    aConf = simConf;

                    aConf.setStartdate(startDate);
                    aConf.setEnddate(endDate);

                    log.info("Param id {}", param.getId());
                    log.info("My sim u {}", aConf.asValuedMap());
                    if (param.getUpdateMap() == null) {
                        param.setUpdateMap(new HashMap<>());
                    }
                    //param.getInput().setDoSave(false);

                    try {
                        param.setFuturedays(0);
                        param.setOffset(0);
                        param.setDates(null, null, action.getActionData(), market);
                    } catch (ParseException e) {
                        log.error(Constants.EXCEPTION, e);
                    }
                    //List<MemoryDTO> memories = findAllMarketComponentsToCheckNew(myData, param, 0, config, false, dataMap, componentMap, subcomponent, parameters, market);

                    //aMap.putAll(aConf.asValuedMap()); // TODO
                    param.getConfig().getConfigData().getConfigValueMap().putAll(aConf.asValuedMap());

                    Inmemory inmemory = param.getService().getIo().getInmemoryFactory().get(param.getService().getIclijConfig());
                    //PipelineData metaData = PipelineUtils.getPipeline(param.getResultMaps(), PipelineConstants.META, inmemory);
                    //SerialMeta meta = PipelineUtils.getMeta(metaData);
                    //String catName = new MetaUtil().getCategory(meta,  cat);
                    PipelineData pipelineDatum = PipelineUtils.getPipeline(param.getResultMaps(), PipelineConstants.META, inmemory);
                    Integer cat = PipelineUtils.getWantedcat(pipelineDatum);
                    String catName = PipelineUtils.getMetaCat(pipelineDatum);
                    log.info("cats {} {}", cat, catName);
                    param.setCategory(cat);
                    param.setCategoryTitle(catName);
                    param.getAndSetCategoryValueMapAlt();


                    ComponentData componentData = component.handle(getActionData(), market, param, profitdata, listComponent, evolve, aMap, subcomponent, null, null, getParent() != null);
                    log.info("acont" + componentData.getResultMap());
                    //log.info("acont" + componentData.getResultMap().keySet());

                    Map<String, Object> updateMap = componentData.getUpdateMap();
                    if (updateMap != null) {
                        param.getUpdateMap().putAll(updateMap);
                        log.info("acont" + componentData.getUpdateMap());
                        log.info("acont" + componentData.getUpdateMap().keySet());
                    }

                    String filename = UUID.randomUUID().toString();
                    List<SerialScoreChromosome> result = new ArrayList<>();
                    //PipelineData results = componentData.getResultMap();
                    PipelineData results = new PipelineData();
                    results.setName("name");
                    results.put(filename, new SerialList(result));
                    results.put(EvolveConstants.TITLETEXT, "simrun " + amarket + " " + simData.getDbid() + " " +  startDate + "-" + endDate);
                    results.put(EvolveConstants.SUBTITLETEXT, "subtitle");
                    results.put(EvolveConstants.ID, filename);
                    results.put(EvolveConstants.DEFAULT, JsonUtil.convert(aConf.asValuedMap()));
                    //componentData.getUpdateMap().keySet()
                    Object filters2 = param.getConfigValueMap().remove(IclijConfigConstants.SIMULATEINVESTFILTERS);
                    // filters is already a serialized string
                    filters2 = param.getInput().getValuemap().get(IclijConfigConstants.SIMULATEINVESTFILTERS);
                    results.put(SimConstants.FILTER, filters2);
                    log.info("Misc {} {}", simData.getDbid(), simData.getScore());
                    log.info("TODO {}", updateMap.keySet());
                    Map<String, Object> resultMap = new HashMap<>();
                    //resultMap.put(SimConstants.HISTORY, new SerialListStockHistory((List<StockHistory>) updateMap.get(SimConstants.HISTORY)));
                    resultMap.put(SimConstants.STOCKHISTORY, new SerialListSimulateStock((List<SimulateStock>) updateMap.get(SimConstants.STOCKHISTORY)));
                    resultMap.put(SimConstants.PLOTCAPITAL, new SerialListPlain((List) updateMap.get(SimConstants.PLOTCAPITAL)));
                    //resultMap.put(SimConstants.SCORE, updateMap.get(SimConstants.SCORE));
                    //resultMap.put("" + updateMap.get(SimConstants.SCORE), updateMap.get(SimConstants.SCORE));
                    //resultMap.put(SimConstants.STARTDATE, TimeUtil.convertDate2((LocalDate)updateMap.get(SimConstants.STARTDATE)));
                    //resultMap.put(SimConstants.ENDDATE, TimeUtil.convertDate2((LocalDate)updateMap.get(SimConstants.ENDDATE)));
                    Map<String, Object> anotherResultMap =  Map.of("0", new SerialListMap(resultMap));
                    results.put(PipelineConstants.RESULT, new SerialListMap(anotherResultMap));
                    QueueElement element = new QueueElement();
                    //log.info("Content {}", JsonUtil.convert(results));
                    InmemoryMessage msg = inmemory.send(ServiceConstants.SIMRUN + UUID.randomUUID(), results, null);
                    element.setOpid(ServiceConstants.SIM);
                    element.setMessage(msg);
                    componentData.getService().send(ServiceConstants.SIMRUN, element, param.getConfig());



                }
                //component.calculateIncDec(componentData, profitdata, positions);
                //System.out.println("Buys: " + market.getMarket() + buys);
                //System.out.println("Sells: " + market.getMarket() + sells);           
            }    
            Inmemory inmemory = param.getService().getIo().getInmemoryFactory().get(config.getInmemoryServer(), config.getInmemoryHazelcast(), config.getInmemoryRedis());
            new PipelineThreadUtils(config, inmemory, param.getService().getIo().getCuratorClient()).cleanPipeline(param.getService().id, param.getId());
            MyCache.getInstance().invalidate(param.getId());
        }
    }

    private static final boolean VERIFYCACHE = false;

    private static final int MAXARR = 0;

    @Deprecated
    private List<Pair<Long, SimulateInvestConfig>> getSimConfigs(Map<Pair<LocalDate, LocalDate>, List<Pair<Long, SimulateInvestConfig>>> simConfigs, Mydate mydate,
            Set<Pair<LocalDate, LocalDate>> keys, Market market) {
        List<Pair<Long, SimulateInvestConfig>> simsConfigs = new ArrayList<>();
        for (Entry<Pair<LocalDate, LocalDate>, List<Pair<Long, SimulateInvestConfig>>> entry : simConfigs.entrySet()) {
            Pair<LocalDate, LocalDate> key = entry.getKey();
            List<Pair<Long, SimulateInvestConfig>> value = entry.getValue();
            int months = Period.between(key.getLeft(), key.getRight()).getMonths();
            LocalDate checkDate = mydate.date.minusMonths(months);
            log.info("aconf" + checkDate + " " + key.getLeft() + " " + key.getRight());
            if (!checkDate.isBefore(key.getLeft()) && checkDate.isBefore(key.getRight())) {
                // TODO value.merge...
                List<Pair<Long, SimulateInvestConfig>> configs = value;
                for (Pair<Long, SimulateInvestConfig> config : configs) {
                    SimulateInvestConfig defaultConfig = getSimulate(market.getSimulate());
                    defaultConfig.merge(config.getRight());
                    simsConfigs.add(new ImmutablePair(config.getLeft(), defaultConfig));
                }
                keys.add(key);
            }
        }
        return simsConfigs;
    }

    private SimulateInvestConfig getSimulate(SimulateInvestConfig simulate) {
        if (simulate == null) {
            return new SimulateInvestConfig();
        }
        SimulateInvestConfig newSimConfig = new SimulateInvestConfig(simulate);
        if (!newSimConfig.equals(simulate)) {
            log.error("Unequal clone");
        }
        return newSimConfig;
    }

    private Map<Pair<LocalDate, LocalDate>, List<Pair<Long, SimulateInvestConfig>>> getSimConfigs(String market, AutoSimulateInvestConfig autoSimConf, List<SimulateFilter> filter, List<SimulateFilter[]> filters, IclijConfig config, MarketActionData actionData, ComponentData param) {
        List<SimDataDTO> all = new ArrayList<>();
        try {
            String simkey = CacheConstants.SIMDATA + market + autoSimConf.getStartdate() + autoSimConf.getEnddate();
            all =  (List<SimDataDTO>) MyCache.getInstance().get(simkey);
            if (all == null) {
                LocalDate startDate = TimeUtil.convertDate(TimeUtil.replace(autoSimConf.getStartdate()));
                LocalDate endDate = null;
                if (autoSimConf.getEnddate() != null) {
                    endDate = TimeUtil.convertDate(TimeUtil.replace(autoSimConf.getEnddate()));
                }
                all = param.getService().getIo().getIdbDao().getAllSimData(market, null, null); // fix later: , startDate, endDate);
                MyCache.getInstance().put(simkey, all);
            }
            /*
            if (VERIFYCACHE && trendMap != null) {
                for (Entry<Integer, Trend> entry : newTrendMap.entrySet()) {
                    int key2 = entry.getKey();
                    Trend v2 = entry.getValue();
                    Trend v = trendMap.get(key2);
                    if (v2 != null && !v2.toString().equals(v.toString())) {
                        log.error("Difference with cache");
                    }
                }
            }
             */
        } catch (Exception e) {
            log.error(Constants.ERROR, e);
        }
        List<SimulateFilter[]> list = null;
        if (autoSimConf != null) {
            List<SimulateFilter> listoverride = filter; //autoSimConf.getFilters();
            list = getDefaultList(actionData);
            if (list != null) {
                filters.addAll(list);
            }
            if (listoverride != null) {
                mergeFilterList(list, listoverride);
            }
        }
        String listString = JsonUtil.convert(list);
        String key = CacheConstants.AUTOSIMCONFIG + market + autoSimConf.getStartdate() + autoSimConf.getEnddate() + "_" + autoSimConf.getInterval() + "_" + autoSimConf.getPeriod() + "_" + autoSimConf.getScorelimit().doubleValue() + " " + listString;
        Map<Pair<LocalDate, LocalDate>, List<Pair<Long, SimulateInvestConfig>>> retMap = (Map<Pair<LocalDate, LocalDate>, List<Pair<Long, SimulateInvestConfig>>>) MyCache.getInstance().get(key);
        Map<Pair<LocalDate, LocalDate>, List<Pair<Long, SimulateInvestConfig>>> newRetMap = new HashMap<>();
        if (retMap == null || VERIFYCACHE) {
            for (SimDataDTO data : all) {
                if (autoSimConf.getScorelimit().doubleValue() > data.getScore().doubleValue()) {
                    continue;
                }
                Integer period = null;
                int months = Period.between(data.getStartdate(), data.getEnddate()).getMonths();
                switch (months) {
                case 1:
                    period = 0;
                    break;
                case 3:
                    period = 1;
                    break;
                case 12:
                    period = 2;
                    break;
                }
                if (period == null) {
                    continue;
                }
                if (period.intValue() == autoSimConf.getPeriod().intValue()) {
                    String amarket = data.getMarket();
                    if (market.equals(amarket)) {
                        Long dbid = data.getDbid();
                        SimulateInvestConfig simConf = new SimUtil().getSimulateInvestConfig(config, data);
                        // different
                        /*
                        log.info("aconf" + simConf.getInterval() + " " + autoSimConf.getInterval());
                        if (simConf.getInterval().intValue() != autoSimConf.getInterval().intValue()) {
                            continue;
                        }
                         */
                        simConf.setVolumelimits(autoSimConf.getVolumelimits());
                        int adviser = simConf.getAdviser();
                        String filterStr = data.getFilter();
                        SimulateFilter myFilter = JsonUtil.convert((String)filterStr, SimulateFilter.class);
                        SimulateFilter[] autoSimConfFilters = list.get(0);
                        log.info("aconf");
                        if (autoSimConfFilters != null) {
                            SimulateFilter autoSimConfFilter = autoSimConfFilters[adviser];
                            if (myFilter != null && autoSimConfFilter != null) {
                                log.info("aconf");
                                if (myFilter.getCorrelation() != null && autoSimConfFilter.getCorrelation() > 0 && autoSimConfFilter.getCorrelation() > myFilter.getCorrelation()) {
                                    continue;
                                }
                                log.info("aconf");
                                if (autoSimConfFilter.getLucky() > 0 && autoSimConfFilter.getLucky() < myFilter.getLucky()) {
                                    continue;
                                }
                                log.info("aconf");
                                if (autoSimConfFilter.getStable() > 0 && autoSimConfFilter.getStable() < myFilter.getStable()) {
                                    continue;
                                }
                                log.info("aconf");
                                if (autoSimConfFilter.getShortrun() > 0 && autoSimConfFilter.getShortrun() > myFilter.getShortrun()) {
                                    continue;
                                }
                                log.info("aconf");
                                if (autoSimConfFilter.getPopulationabove() > myFilter.getPopulationabove()) {
                                    continue;
                                }
                            }
                        }
                        Pair<LocalDate, LocalDate> aKey = new ImmutablePair(data.getStartdate(), data.getEnddate());
                        MapUtil.mapAddMe(newRetMap, aKey, new ImmutablePair(dbid, simConf));
                    }
                }
            }
        }
        if (VERIFYCACHE && retMap != null) {
            for (Entry<Pair<LocalDate, LocalDate>, List<Pair<Long, SimulateInvestConfig>>> entry : newRetMap.entrySet()) {
                Pair<LocalDate, LocalDate> key2 = entry.getKey();
                List<Pair<Long, SimulateInvestConfig>> v2 = entry.getValue();
                List<Pair<Long, SimulateInvestConfig>> v = retMap.get(key2);
                if (v == null || v2 == null || v.size() != v2.size()) {
                    log.error("Difference with cache");
                    continue;
                }
                for (int i = 0; i < v.size(); i++) {
                    if (!v.get(i).equals(v2.get(i))) {
                        log.error("Difference with cache");
                    }
                }
            }
        }
        if (retMap != null) {
            return retMap;
        }
        retMap = newRetMap;
        MyCache.getInstance().put(key, retMap);
        return retMap;
    }

    private void mergeFilterList(List<SimulateFilter[]> list, List<SimulateFilter> listoverride) {
        for (int i = 0; i < listoverride.size(); i++) {
            SimulateFilter afilter = list.get(0)[i];
            SimulateFilter otherfilter = listoverride.get(i);
            afilter.merge(otherfilter);
        }
    }

    private List<SimulateFilter[]> getDefaultList(MarketActionData action) {
        List<SimulateFilter[]> list = null;
        try {
            list = IclijXMLConfig.getSimulate(action.getIclijConfig());
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        return list;
    }

    private SimulateInvestConfig getSimConfig(IclijConfig config) {
        if (config.getConfigData().getConfigValueMap().get(IclijConfigConstants.SIMULATEINVESTDELAY) == null || (int) config.getConfigData().getConfigValueMap().get(IclijConfigConstants.SIMULATEINVESTDELAY) == 0) {
            return null;
        }
        SimulateInvestConfig simConfig = new SimulateInvestConfig();
        simConfig.setAdviser(config.getSimulateInvestAdviser());
        simConfig.setBuyweight(config.wantsSimulateInvestBuyweight());
        simConfig.setConfidence(config.wantsSimulateInvestConfidence());
        simConfig.setConfidenceValue(config.getSimulateInvestConfidenceValue());
        simConfig.setConfidenceFindTimes(config.getSimulateInvestConfidenceFindtimes());
        simConfig.setAbovebelow(config.getSimulateInvestAboveBelow());
        simConfig.setConfidenceholdincrease(config.wantsSimulateInvestConfidenceHoldIncrease());
        simConfig.setNoconfidenceholdincrease(config.wantsSimulateInvestNoConfidenceHoldIncrease());
        simConfig.setConfidencetrendincrease(config.wantsSimulateInvestConfidenceTrendIncrease());
        simConfig.setConfidencetrendincreaseTimes(config.wantsSimulateInvestConfidenceTrendIncreaseTimes());
        simConfig.setNoconfidencetrenddecrease(config.wantsSimulateInvestNoConfidenceTrendDecrease());
        simConfig.setNoconfidencetrenddecreaseTimes(config.wantsSimulateInvestNoConfidenceTrendDecreaseTimes());
        try {
            simConfig.setImproveFilters(config.getSimulateInvestImproveFilters());
        } catch (Exception e) {
            int jj = 0;
        }
        simConfig.setInterval(config.getSimulateInvestInterval());
        simConfig.setIndicatorPure(config.wantsSimulateInvestIndicatorPure());
        simConfig.setIndicatorRebase(config.wantsSimulateInvestIndicatorRebase());
        simConfig.setIndicatorReverse(config.wantsSimulateInvestIndicatorReverse());
        simConfig.setIndicatorDirection(config.wantsSimulateInvestIndicatorDirection());
        simConfig.setIndicatorDirectionUp(config.wantsSimulateInvestIndicatorDirectionUp());
        simConfig.setMldate(config.wantsSimulateInvestMLDate());
        try {
            simConfig.setPeriod(config.getSimulateInvestPeriod());
        } catch (Exception e) {
            int jj = 0;
        }
        simConfig.setStoploss(config.wantsSimulateInvestStoploss());
        simConfig.setStoplossValue(config.getSimulateInvestStoplossValue());
        simConfig.setIntervalStoploss(config.wantsSimulateInvestIntervalStoploss());
        simConfig.setIntervalStoplossValue(config.getSimulateInvestIntervalStoplossValue());
        simConfig.setStocks(config.getSimulateInvestStocks());
        simConfig.setInterpolate(config.wantsSimulateInvestInterpolate());
        simConfig.setDay(config.getSimulateInvestDay());
        simConfig.setDelay(config.getSimulateInvestDelay());
        try {
            simConfig.setFuturecount(config.getSimulateInvestFutureCount());
            simConfig.setFuturetime(config.getSimulateInvestFutureTime());
        } catch (Exception e) {

        }
        Map<String, Double> map = JsonUtil.convert(config.getSimulateInvestVolumelimits(), Map.class);
        simConfig.setVolumelimits(map);

        SimulateFilter[] array = JsonUtil.convert(config.getSimulateInvestFilters(), SimulateFilter[].class);
        List<SimulateFilter> list = null;
        if (array != null) {
            list = Arrays.asList(array);
        }
        simConfig.setFilters(list);
        try {
            simConfig.setEnddate(config.getSimulateInvestEnddate());
        } catch (Exception e) {

        }
        try {
            simConfig.setStartdate(config.getSimulateInvestStartdate());
        } catch (Exception e) {

        }
        return simConfig;
    }

    class Mydate {
        LocalDate date;
        int indexOffset;
        int prevIndexOffset;

    }

    @Deprecated
    private Map<Pair<LocalDate, LocalDate>, List<Pair<Long, SimulateInvestConfig>>> getSimConfigs2(String market, AutoSimulateInvestConfig autoSimConf, List<SimulateFilter> filter, List<SimulateFilter[]> filters, IclijConfig config, MarketActionData actionData, ComponentData param) {
        List<SimDataDTO> all = new ArrayList<>();
        try {
            String simkey = CacheConstants.SIMDATA + market + autoSimConf.getStartdate() + autoSimConf.getEnddate();
            all =  (List<SimDataDTO>) MyCache.getInstance().get(simkey);
            if (all == null) {
                LocalDate startDate = TimeUtil.convertDate(TimeUtil.replace(autoSimConf.getStartdate()));
                LocalDate endDate = null;
                if (autoSimConf.getEnddate() != null) {
                    endDate = TimeUtil.convertDate(TimeUtil.replace(autoSimConf.getEnddate()));
                }
                all = param.getService().getIo().getIdbDao().getAllSimData(market, null, null); // fix later: , startDate, endDate);
                MyCache.getInstance().put(simkey, all);
            }
        } catch (Exception e) {
            log.error(Constants.ERROR, e);
        }
        List<SimulateFilter[]> list = null;
        if (autoSimConf != null) {
            List<SimulateFilter> listoverride = filter; //autoSimConf.getFilters();
            list = getDefaultList(actionData);
            if (list != null) {
                filters.addAll(list);
            }
            if (listoverride != null) {
                mergeFilterList(list, listoverride);
            }
        }
        String listString = JsonUtil.convert(list);
        Map<Pair<LocalDate, LocalDate>, List<Pair<Long, SimulateInvestConfig>>> retMap = null;
        Map<Pair<LocalDate, LocalDate>, List<Pair<Long, SimulateInvestConfig>>> newRetMap = new HashMap<>();
        if (retMap == null || VERIFYCACHE) {
            for (SimDataDTO data : all) {
                if (autoSimConf.getScorelimit().doubleValue() > data.getScore().doubleValue()) {
                    continue;
                }
                Integer period = null;
                int months = Period.between(data.getStartdate(), data.getEnddate()).getMonths();
                switch (months) {
                case 1:
                    period = 0;
                    break;
                case 3:
                    period = 1;
                    break;
                case 12:
                    period = 2;
                    break;
                }
                if (period == null) {
                    continue;
                }
                if (period.intValue() == autoSimConf.getPeriod().intValue()) {
                    String amarket = data.getMarket();
                    if (market.equals(amarket)) {
                        Long dbid = data.getDbid();
                        SimulateInvestConfig simConf = new SimUtil().getSimulateInvestConfig(config, data);
                        // different
                        /*
                        log.info("aconf" + simConf.getInterval() + " " + autoSimConf.getInterval());
                        if (simConf.getInterval().intValue() != autoSimConf.getInterval().intValue()) {
                            continue;
                        }
                         */
                        simConf.setVolumelimits(autoSimConf.getVolumelimits());
                        int adviser = simConf.getAdviser();
                        String filterStr = data.getFilter();
                        SimulateFilter myFilter = JsonUtil.convert((String)filterStr, SimulateFilter.class);
                        SimulateFilter[] autoSimConfFilters = list.get(0);
                        log.info("aconf");
                        if (autoSimConfFilters != null) {
                            SimulateFilter autoSimConfFilter = autoSimConfFilters[adviser];
                            if (myFilter != null && autoSimConfFilter != null) {
                                log.info("aconf");
                                if (myFilter.getCorrelation() != null && autoSimConfFilter.getCorrelation() > 0 && autoSimConfFilter.getCorrelation() > myFilter.getCorrelation()) {
                                    continue;
                                }
                                log.info("aconf");
                                if (autoSimConfFilter.getLucky() > 0 && autoSimConfFilter.getLucky() < myFilter.getLucky()) {
                                    continue;
                                }
                                log.info("aconf");
                                if (autoSimConfFilter.getStable() > 0 && autoSimConfFilter.getStable() < myFilter.getStable()) {
                                    continue;
                                }
                                log.info("aconf");
                                if (autoSimConfFilter.getShortrun() > 0 && autoSimConfFilter.getShortrun() > myFilter.getShortrun()) {
                                    continue;
                                }
                                log.info("aconf");
                                if (autoSimConfFilter.getPopulationabove() > myFilter.getPopulationabove()) {
                                    continue;
                                }
                            }
                        }
                        Pair<LocalDate, LocalDate> aKey = new ImmutablePair(data.getStartdate(), data.getEnddate());
                        MapUtil.mapAddMe(newRetMap, aKey, new ImmutablePair(dbid, simConf));
                    }
                }
            }
        }
        retMap = newRetMap;
        return retMap;
    }

}
