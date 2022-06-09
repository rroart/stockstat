package roart.iclij.component;

import java.text.ParseException;
import java.time.LocalDate;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Map.Entry;
import java.util.OptionalDouble;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.commons.lang3.tuple.ImmutableTriple;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import roart.common.cache.MyCache;
import roart.common.config.CacheConstants;
import roart.common.config.ConfigConstants;
import roart.common.constants.Constants;
import roart.common.constants.EvolveConstants;
import roart.common.constants.ServiceConstants;
import roart.common.model.MetaItem;
import roart.common.pipeline.PipelineConstants;
import roart.common.util.ArraysUtil;
import roart.common.util.JsonUtil;
import roart.common.util.MapUtil;
import roart.common.util.MathUtil;
import roart.common.util.MetaUtil;
import roart.common.util.TimeUtil;
import roart.common.util.ValidateUtil;
import roart.iclij.component.adviser.Adviser;
import roart.iclij.component.adviser.AdviserFactory;
import roart.component.model.ComponentData;
import roart.component.model.SimulateInvestData;
import roart.constants.IclijConstants;
import roart.constants.SimConstants;
import roart.db.IclijDbDao;
import roart.evolution.chromosome.AbstractChromosome;
import roart.iclij.evolution.chromosome.impl.ConfigMapChromosome2;
import roart.iclij.evolution.chromosome.winner.IclijConfigMapChromosomeWinner;
import roart.evolution.config.EvolutionConfig;
import roart.evolution.fitness.Fitness;
import roart.evolution.iclijconfigmap.genetics.gene.impl.IclijConfigMapChromosome;
import roart.evolution.iclijconfigmap.genetics.gene.impl.IclijConfigMapGene;
import roart.iclij.config.AutoSimulateInvestConfig;
import roart.iclij.config.IclijConfig;
import roart.iclij.config.IclijConfigConstants;
import roart.iclij.config.IclijXMLConfig;
import roart.iclij.config.MLConfigs;
import roart.iclij.config.Market;
import roart.iclij.config.SimulateFilter;
import roart.iclij.config.SimulateInvestConfig;
import roart.iclij.filter.Memories;
import roart.iclij.model.MLMetricsItem;
import roart.iclij.model.IncDecItem;
import roart.iclij.model.MemoryItem;
import roart.iclij.model.Parameters;
import roart.iclij.model.SimDataItem;
import roart.iclij.model.Trend;
import roart.iclij.model.action.MarketActionData;
import roart.iclij.service.IclijServiceResult;
import roart.iclij.verifyprofit.TrendUtil;
import roart.service.model.ProfitData;
import roart.simulate.model.Capital;
import roart.simulate.model.SimulateStock;
import roart.simulate.model.StockHistory;
import roart.simulate.util.SimUtil;

public class SimulateInvestComponent extends ComponentML {

    private static final boolean VERIFYCACHE = false;
    
    private static final int MAXARR = 0;
    
    private String datebreak = null;
    
    private String idbreak = null;
    
    @Override
    public void enable(Map<String, Object> valueMap) {
    }

    @Override
    public void disable(Map<String, Object> valueMap) {
    }

    @Override
    public ComponentData handle(MarketActionData action, Market market, ComponentData param, ProfitData profitdata,
            Memories positions, boolean evolve, Map<String, Object> aMap, String subcomponent, String mlmarket,
            Parameters parameters, boolean hasParent) {
        ComponentData componentData = new ComponentData(param);
        SimulateInvestData simulateParam;
        if (param instanceof SimulateInvestData) {
            simulateParam = (SimulateInvestData) param;
        } else {
            simulateParam = new SimulateInvestData(param);
        }
        IclijConfig config = param.getInput().getConfig();
	// four cases
	// autosimconfig set, both evolve and not, and simconfig is null
	// autoconfig is not set, and simconfig is set, both evolve and not
	// simconfig from siminvest
	// localsimconfig from market config file
	// simconfig and localsimconfig merges, with simconfig values win
        // four cases
        // sim: vl
        // imp: vl filt
        // auto: vl filt
        // impauto: vl filt
        // extradelay: delay for getting prices
        // extradelay: buy and sell is on the same date, just an approx
        // delay: days after buy/sell decision.
        
        String conffilters = (String) param.getConfigValueMap().remove(IclijConfigConstants.SIMULATEINVESTFILTERS);
        String confautofilters = (String) param.getConfigValueMap().remove(IclijConfigConstants.AUTOSIMULATEINVESTFILTERS);
        AutoSimulateInvestConfig autoSimConfig = getAutoSimConfig(config);
        SimulateInvestConfig simConfig = getSimConfig(config);
        // coming from improvesim
        List<SimulateFilter> filter = get(conffilters);
        List<SimulateFilter> autofilter = get(confautofilters);
        if (simConfig != null) {
            filter = simConfig.getFilters();
        }
        if (autoSimConfig != null) {
            autofilter = autoSimConfig.getFilters();
        }
        //Integer overrideAdviser = null;
        boolean evolving = param instanceof SimulateInvestData;
        if (!(param instanceof SimulateInvestData)) {
            // if not evolving, plain simulating
            SimulateInvestConfig localSimConfig = getSimulate(market.getSimulate());
	    // override with file config
            localSimConfig.merge(simConfig);
	    if (simConfig != null) {
		//simConfig.merge(localSimConfig);
	    }
            if (localSimConfig.getExtradelay() != null) {
                //extradelay = localSimConfig.getExtradelay();
            }
            simConfig = localSimConfig;
        } else {
            // if evolving
            /*
            if (simulateParam.getConfig() != null) {
                overrideAdviser = simulateParam.getConfig().getAdviser();
                simConfig.merge(simulateParam.getConfig());
            }
             */
            /*
            if (!simulateParam.getInput().getValuemap().isEmpty()) {
                overrideAdviser = simulateParam.getConfig().getAdviser();
                simConfig.merge(simulateParam.getConfig());
            }
             */
	    // file configured sim config
            SimulateInvestConfig localSimConfig = getSimulate(market.getSimulate());
            localSimConfig.merge(simConfig);
            if (localSimConfig != null && localSimConfig.getVolumelimits() != null && simConfig.getVolumelimits() == null) {
		// override base simconfig with file config volumelimits
                //simConfig.setVolumelimits(localSimConfig.getVolumelimits());
            }
            if (localSimConfig != null && localSimConfig.getExtradelay() != null) {
		// use file config extradelay
		//simConfig.setExtradelay(localSimConfig.getExtradelay());
                //extradelay = localSimConfig.getExtradelay();
            }
            simConfig = localSimConfig;
        }
        if (autoSimConfig != null/* && !evolving*/) {
            simConfig.setStartdate(autoSimConfig.getStartdate());
            simConfig.setEnddate(autoSimConfig.getEnddate());
            simConfig.setInterval(autoSimConfig.getInterval());
            if (autoSimConfig.getVolumelimits() != null) {
                simConfig.setVolumelimits(autoSimConfig.getVolumelimits());
            } else {
                autoSimConfig.setVolumelimits(simConfig.getVolumelimits());                
            }
        }
        int extradelay = 0;
        if (simConfig.getExtradelay() != null) {
            extradelay = simConfig.getExtradelay();
        }
        // coming from improvesim
        //List<SimulateFilter> filter = simConfig.getFilters();
        //simConfig.setFilters(null);
        Data data = new Data();
        if (simulateParam.getStockDates() != null) {
            data.stockDates = simulateParam.getStockDates();
        } else {
            data.stockDates = param.getService().getDates(market.getConfig().getMarket());           
        }
        if (!TimeUtil.rangeCheck(data.stockDates, TimeUtil.replace(simConfig.getStartdate()), TimeUtil.replace(simConfig.getEnddate()))) {
            Map<String, Object> map = new HashMap<>();
            map.put(SimConstants.EMPTY, true);
            map.put(EvolveConstants.TITLETEXT, emptyNull(simConfig.getStartdate(), "start") + "-" + emptyNull(simConfig.getEnddate(), "end") + " " + ("any"));
                     componentData.getUpdateMap().putAll(map);
            if (evolving) {
                componentData.setResultMap(new HashMap<>());
            }
            Double score = 0.0;
            Map<String, Double> scoreMap = new HashMap<>();
            if (score.isNaN()) {
                int jj = 0;
            }
            scoreMap.put("" + score, score);
            scoreMap.put(SimConstants.SCORE, score);
            componentData.setScoreMap(scoreMap);

            handle2(action, market, componentData, profitdata, positions, evolve, aMap, subcomponent, mlmarket, parameters, hasParent);
            return componentData;
        }
        data.categoryValueFillMap = param.getFillCategoryValueMap();
        data.categoryValueMap = param.getCategoryValueMap();
        data.volumeMap = param.getVolumeMap();
        BiMap<String, LocalDate> stockDatesBiMap = getStockDatesBiMap(market.getConfig().getMarket(), data.stockDates);

        //ComponentData componentData = component.improve2(action, param, market, profitdata, null, buy, subcomponent, parameters, mlTests);
        String mldate = getMlDate(market, simConfig, data, autoSimConfig);
        int investStartOffset = data.stockDates.size() - 1 - TimeUtil.getIndexEqualBefore(data.stockDates, mldate);
        String adatestring = data.stockDates.get(data.stockDates.size() - 1 - investStartOffset);
        LocalDate investStart = stockDatesBiMap.get(adatestring);
        /*
        try {
            investStart = TimeUtil.convertDate(mldate);
        } catch (ParseException e1) {
            log.error(Constants.EXCEPTION, e1);
        }
        */
        LocalDate investEnd = param.getFutureDate();
        try {
            String enddate;
            if (autoSimConfig != null) {
                enddate = autoSimConfig.getEnddate();
            } else {
                enddate = simConfig.getEnddate();
            }
            if (enddate != null) {
                enddate = enddate.replace('-', '.');
                investEnd = TimeUtil.convertDate(enddate);
            }
        } catch (ParseException e1) {
            log.error(Constants.EXCEPTION, e1);
        }
        int endIndexOffset = 0;
        if (investEnd != null) {
            String investEndStr = TimeUtil.convertDate2(investEnd);
            int investEndIndex = TimeUtil.getIndexEqualBefore(data.stockDates, investEndStr);
            endIndexOffset = data.stockDates.size() - 1 - investEndIndex;
        }

        boolean intervalwhole;
        if (autoSimConfig != null) {
            intervalwhole = config.getAutoSimulateInvestIntervalwhole();
        } else {
            intervalwhole = config.wantsSimulateInvestIntervalWhole();
        }
        int end = 1;
        if (intervalwhole) {
            end = autoSimConfig != null ? autoSimConfig.getInterval() : simConfig.getInterval();
        }

        List<String> parametersList = new ArrayList<>(); //adviser.getParameters();
        if (parametersList.isEmpty()) {
            parametersList.add(null);
        }

        List<Double> scores = new ArrayList<>();

        //LocalDate date = TimeUtil.getEqualBefore(data.stockDates, investStart);
        //int indexOffset = data.stockDates.size() - 1 - TimeUtil.getIndexEqualAfter(data.stockDates, datestring2);
        // TODO investend and other reset of more params
        Parameters realParameters = parameters;
        if (realParameters == null || realParameters.getThreshold() == 1.0) {
            String aParameter = JsonUtil.convert(realParameters);

            investEnd = getAdjustedInvestEnd(extradelay, data, investEnd);
            LocalDate lastInvestEnd = getAdjustedLastInvestEnd(data, investEnd, simConfig.getDelay());

            setDataIdx(data, investStart, lastInvestEnd);
            
            setExclusions(market, data, simConfig);

            setDataVolumeAndTrend(market, param, simConfig, data, investStart, investEnd, lastInvestEnd, evolving);

            long time0 = System.currentTimeMillis();
            Map<String, Object> resultMap = new HashMap<>();
            for (int offset = 0; offset < end; offset++) {
                Integer origAdviserId = (Integer) param.getInput().getValuemap().get(IclijConfigConstants.SIMULATEINVESTADVISER);
                Mydate mydate = new Mydate();
                mydate.indexOffset = investStartOffset - offset;
                if (mydate.indexOffset < 0) {
                    continue;
                }
                String adatestring2 = data.stockDates.get(data.stockDates.size() - 1 - mydate.indexOffset);
                mydate.date = stockDatesBiMap.get(adatestring2);
                //getAdjustedDate(data, investStart, offset, mydate);
                Map<Pair<LocalDate, LocalDate>, List<SimulateInvestConfig>> simConfigs;
                List<SimulateFilter[]> filters = new ArrayList<>();
                if (autoSimConfig == null) {
                    simConfigs = new HashMap<>();
                    List<SimulateFilter> listoverride = filter; //simConfig.getFilters();
                    List<SimulateFilter[]> list = getDefaultList();
                    if (list != null) {
                        filters.addAll(list);
                    }
                    if (listoverride != null) {
                        mergeFilterList(list, listoverride);
                    }
                    //simConfigs = new ArrayList<>();
                    //simConfigs.add(getSimConfig(config));
                } else {
                    simConfigs = getSimConfigs(market.getConfig().getMarket(), autoSimConfig, autofilter, filters, config);
                    simConfigs = new HashMap<>(simConfigs);
                }
                List<SimulateInvestConfig> simsConfigs = new ArrayList<>();
                if (autoSimConfig == null) {
                    simsConfigs.add(simConfig);
                } else {
                    Set<Pair<LocalDate, LocalDate>> keys = new HashSet<>();
                    if (mydate.date != null) {
                        simsConfigs = getSimConfigs(simConfigs, mydate, keys, market);
                        simConfigs.keySet().removeAll(keys);
                    }
                }
                List<Triple<SimulateInvestConfig, OneRun, Results>> simTriplets = getTriples(market, param, data,
                        investStart, investEnd, mydate, simsConfigs);
                if (evolving || offset > 0) {
                    investEnd = lastInvestEnd;
                }

                SimulateInvestConfig currentSimConfig = null;
                if (autoSimConfig == null) {
                    currentSimConfig = simConfig;
                }
                OneRun currentOneRun = getOneRun(market, param, simConfig, data, investStart, investEnd, null);
                Adviser selladviser = null;
                Adviser voteadviser = null;
                SimulateInvestConfig sell = null;
                SimulateInvestConfig vote = null;
                if (autoSimConfig == null) {
                    int adviserId = simConfig.getAdviser();
                    currentOneRun.adviser = new AdviserFactory().get(adviserId, market, investStart, investEnd, param, simConfig);
                    currentOneRun.adviser.getValueMap(data.stockDates, data.firstidx, data.lastidx, data.getCatValMap(currentOneRun.adviser.getInterpolate(simConfig.getInterpolate())));
                } else {
                    selladviser = new AdviserFactory().get(-1, market, investStart, investEnd, param, simConfig);
                    sell = JsonUtil.copy(simConfig);
                    sell.setConfidence(true);
                    sell.setConfidenceValue(2.0);
                    sell.setConfidenceFindTimes(0);
                    if (autoSimConfig.getVote() != null && autoSimConfig.getVote()) {
                        voteadviser = new AdviserFactory().get(-2, market, investStart, investEnd, param, simConfig);
                        vote = JsonUtil.copy(simConfig);
                        //vote.setConfidence(true);
                        //vote.setConfidenceValue(2.0);
                        //vote.setConfidenceFindTimes(0);
                        //currentSimConfig = vote;
                    }
                }
                Results mainResult = new Results();

                /*
                if (mydate.date != null) {
                    mydate.date = TimeUtil.getForwardEqualAfter2(mydate.date, 0 , data.stockDates);
                    String datestring2 = TimeUtil.convertDate2(mydate.date);
                    mydate.indexOffset = data.stockDates.size() - 1 - TimeUtil.getIndexEqualAfter(data.stockDates, datestring2);
                }
            */
                while (mydate.indexOffset >= endIndexOffset) {
                    if (datebreak != null && mydate.date.toString().equals(datebreak)) {
                        int jj = 0;
                    }
                    boolean lastInvest = offset == 0 && mydate.indexOffset == endIndexOffset;
                    /*
                    if (currentSimConfig != null) {
                        doOneRun(param, currentSimConfig, extradelay, evolving, aParameter, offset, currentOneRun, mainResult,
                                data, lastInvest, mydate, autoSimConfig != null, stockDatesBiMap);
                    }
                    */
                    if (autoSimConfig != null) {
                        if (MAXARR > 0 && !simTriplets.isEmpty() && simTriplets.get(0).getMiddle().autoscore != null && simTriplets.get(0).getMiddle().autoscore != 0) {
                            Collections.shuffle(simTriplets);
                        }
                        for (Triple<SimulateInvestConfig, OneRun, Results> aPair : simTriplets) {
                            SimulateInvestConfig aSimConfig = aPair.getLeft();
                            OneRun aOneRun = aPair.getMiddle();
                            Results aResult = aPair.getRight();

                            doOneRun(param, aSimConfig, extradelay, evolving, aParameter, offset, aOneRun, aResult,
                                    data, lastInvest, mydate, autoSimConfig != null, stockDatesBiMap, false, endIndexOffset);
                        }
                    }
                    if (currentSimConfig != null) {
                        if (!simTriplets.isEmpty() && simTriplets.get(0).getMiddle().autoscore != null && simTriplets.get(0).getMiddle().autoscore != 0 && autoSimConfig.getVote() != null && autoSimConfig.getVote()) {
                            List<String> buys = new ArrayList<>();
                            //Collections.sort(simTriplets, (o1, o2) -> Double.compare(o2.getMiddle().autoscore, o1.getMiddle().autoscore));
                            List<Triple<SimulateInvestConfig, OneRun, Results>> new2 = getAdviserTriplets(simTriplets);

                            List<Triple<SimulateInvestConfig, OneRun, Results>> others = new2;
                            for (Triple<SimulateInvestConfig, OneRun, Results> triplet : others) {
                                OneRun aresult = triplet.getMiddle();
                                buys.addAll(aresult.buys.stream().map(SimulateStock::getId).collect(Collectors.toList()));
                            }
                            currentOneRun.adviser.setExtra(buys);
                        }
                        doOneRun(param, currentSimConfig, extradelay, evolving, aParameter, offset, currentOneRun, mainResult,
                                data, lastInvest, mydate, autoSimConfig != null, stockDatesBiMap, true, endIndexOffset);
                    }
                    if (autoSimConfig != null) {
                        for (Triple<SimulateInvestConfig, OneRun, Results> aTriple : simTriplets) {
                            SimulateInvestConfig aSimConfig = aTriple.getLeft();
                            OneRun aOneRun = aTriple.getMiddle();
                            Results aResult = aTriple.getRight();
                            // best or best last
                            double score = getScore(autoSimConfig, aOneRun, aResult);
                            aOneRun.autoscore = score;
                            if (score < 0) {
                                int jj = 0;
                            }
                        }                        
                        // no. hits etc may be reset if changed
                        // winnerAdviser
                        Collections.sort(simTriplets, (o1, o2) -> Double.compare(o2.getMiddle().autoscore, o1.getMiddle().autoscore));
                        if (MAXARR > 0) {
                            if (MAXARR < 11) {
                                if (!simTriplets.isEmpty() && simTriplets.get(0).getMiddle().autoscore != 0) {
                                    List<Triple<SimulateInvestConfig, OneRun, Results>> new2 = getAdviserTriplets(
                                            simTriplets);
                                    if (simTriplets.size() != new2.size()) {
                                        for (Triple<SimulateInvestConfig, OneRun, Results> anew : new2) {
                                            SimulateInvestConfig aconf = anew.getLeft();
                                            log.info("" + aconf.asValuedMap());
                                        }
                                    }
                                    simTriplets = new2;
                                }
                            } else {
                                simTriplets = simTriplets.subList(0, Math.min(MAXARR, simTriplets.size()));
                            }
                        }
                        // overwrite/merge currentOnerun etc
                        // and get new date range
                        // and remove old or bad
                        Set<Pair<LocalDate, LocalDate>> keys = new HashSet<>();
                        simsConfigs = getSimConfigs(simConfigs, mydate, keys, market);
                        simConfigs.keySet().removeAll(keys);
                        List<Triple<SimulateInvestConfig, OneRun, Results>> newSimTriplets = getTriples(market, param, data,
                                investStart, investEnd, mydate, simsConfigs);
                        if (newSimTriplets.size() > 0) {
                            if (MAXARR > 0) {
                                //newSimTriplets = newSimTriplets.subList(0, Math.min(MAXARR, simTriplets.size()));
                            }
                            List<Double> alist = simTriplets.stream().map(o -> (o.getMiddle().capital.amount + getSum(o.getMiddle().mystocks).amount)).collect(Collectors.toList());
                            log.debug("alist {}", alist);
                            double autolimit = autoSimConfig.getDellimit();
                            simTriplets = simTriplets.stream().filter(o -> (o.getMiddle().capital.amount + getSum(o.getMiddle().mystocks).amount) > autolimit).collect(Collectors.toList());
                        }
                        simTriplets.addAll(newSimTriplets);
                    }
                    if (autoSimConfig != null && !simTriplets.isEmpty()) {
                        List<Double> alist = simTriplets.stream().map(o -> (o.getMiddle().autoscore)).collect(Collectors.toList());
                        List<Integer> alist2 = simTriplets.stream().map(o -> (o.getLeft() .hashCode())).collect(Collectors.toList());
                        List<Double> alist3 = simTriplets.stream().map(o -> o.getRight().plotCapital.size() > 0 ? o.getRight().plotCapital.get(o.getRight().plotCapital.size()-1) : 0.0).toList();
                        log.debug("alist {}", alist);
                        log.debug("alist {} {}", mydate.date, alist.subList(0, Math.min(5, alist.size())));
                        log.debug("alist {} {} {}", mydate.date, alist.size(), alist2.subList(0, Math.min(5, alist2.size())));
                        log.debug("alist {} {}", mydate.date, alist3.subList(0, Math.min(5, alist2.size())));
                        OneRun oneRun = simTriplets.get(0).getMiddle();
                        if (oneRun.runs > 1 && ((oneRun.autoscore != null && oneRun.autoscore > autoSimConfig.getAutoscorelimit()) || (autoSimConfig.getKeepAdviser() && currentOneRun.autoscore != null && currentOneRun.autoscore > autoSimConfig.getKeepAdviserLimit()))) {
                            if (autoSimConfig.getVote() != null && autoSimConfig.getVote()) {
                                currentOneRun.adviser = voteadviser;
                                //currentOneRun.hits = SerializationUtils.clone(oneRun.hits);
                                //currentOneRun.trendDec = SerializationUtils.clone(oneRun.trendDec);
                                //currentOneRun.trendInc = SerializationUtils.clone(oneRun.trendInc);
                                currentSimConfig = vote;                
                            } else {
                                // calc autoscore
                                Double score = getScore(autoSimConfig, currentOneRun, mainResult);
                                if (!(autoSimConfig.getKeepAdviser() && score != null && score > autoSimConfig.getKeepAdviserLimit())) {                                    
                                currentOneRun.adviser = oneRun.adviser;
                                currentOneRun.hits = SerializationUtils.clone(oneRun.hits);
                                currentOneRun.trendDec = SerializationUtils.clone(oneRun.trendDec);
                                currentOneRun.trendInc = SerializationUtils.clone(oneRun.trendInc);
                                //oneRun.
                                currentSimConfig = simTriplets.get(0).getLeft();
                                }
                            }
                        } else {
                            currentOneRun.adviser = selladviser;
                            if (autoSimConfig.getVote() == null || !autoSimConfig.getVote()) {
                            currentOneRun.hits = SerializationUtils.clone(oneRun.hits);
                            currentOneRun.trendDec = SerializationUtils.clone(oneRun.trendDec);
                            currentOneRun.trendInc = SerializationUtils.clone(oneRun.trendInc);
                            }
                            currentSimConfig = sell;
                        }
                    }
                    mydate.prevIndexOffset = mydate.indexOffset;
                    //int interval = autoSimConfig != null ? autoSimConfig.getInterval() : simConfig.getInterval();
                    if (mydate.indexOffset - simConfig.getInterval() < 0 * endIndexOffset) {
                        break;
                    }
                    mydate.indexOffset -= simConfig.getInterval();
                    String adatestring3 = data.stockDates.get(data.stockDates.size() - 1 - mydate.indexOffset);
                    mydate.date = stockDatesBiMap.get(adatestring3);
                }
                /*
            if (offset == 0) {
                try {
                    lastbuysell(stockDates, date, adviser, capital, simConfig, categoryValueMap, mystocks, extradelay, prevIndexOffset, hits, findTimes, aParameter, param.getUpdateMap(), trendInc, trendDec, configExcludeList, param, market, interval, filteredCategoryValueMap, volumeMap, delay);
                } catch (Exception e) {
                    log.error(Constants.EXCEPTION, e);
                }
            }
                 */
                //Collections.sort(simTriplets, (o1, o2) -> Double.compare(o2.getMiddle().autoscore, o1.getMiddle().autoscore));
                simTriplets = simTriplets.stream().filter(e -> ArraysUtil.getLast(e.getRight().plotCapital) != null).collect(Collectors.toList());
                Collections.sort(simTriplets, (o1, o2) -> Double.compare(ArraysUtil.getLast(o2.getRight().plotCapital), ArraysUtil.getLast(o1.getRight().plotCapital)));
                Map<Integer, Double> advMap = new HashMap<>();
                if (autoSimConfig != null) {
                    for (Triple<SimulateInvestConfig, OneRun, Results> triplet : simTriplets) {
                        SimulateInvestConfig aConfig = triplet.getLeft();
                        int adviser = aConfig.getAdviser();
                        if (!advMap.containsKey(adviser)) {
                            advMap.put(adviser, ArraysUtil.getLast(triplet.getRight().plotCapital));
                        }
                    }
                    log.info("Adviser map {}", advMap);
                }
                List<Double> alist = simTriplets.stream().map(o -> ArraysUtil.getLast(o.getRight().plotCapital)).toList();
                //log.debug("alist {}", alist);
                //log.debug("alist {} {}", mydate.date, alist.subList(0, Math.min(5, alist.size())));
                log.debug("alist {} {} {}", mydate.date, alist.size(), alist.subList(0, Math.min(5, alist.size())));
                //List<Double> alist3 = simTriplets.stream().map(o -> o.getRight().plotCapital.size() > 0 ? o.getRight().plotCapital.get(o.getRight().plotCapital.size()-1) : 0.0).toList();
                List<Triple<SimulateInvestConfig, OneRun, Results>> endSimTriplets = new ArrayList<>();
                Double mlast = ArraysUtil.getLast(mainResult.plotCapital);
                if (mlast == null) {
                    int jj = 0;
                }
                boolean autolost;
                if ((mlast == null || alist.isEmpty()) || mlast > alist.get(0)) {
                endSimTriplets.add(new ImmutableTriple(currentSimConfig, currentOneRun, mainResult));
                autolost = false;
                } else {
                    endSimTriplets.add(simTriplets.get(0));                    
                    autolost = true;
                }
                for (Triple<SimulateInvestConfig, OneRun, Results> aPair : endSimTriplets) {
                    SimulateInvestConfig aSimConfig = aPair.getLeft();
                    OneRun aOneRun = aPair.getMiddle();
                    Results aResult = aPair.getRight();
                    //boolean lastInvest = offset == 0 && mydate.date != null && lastInvestEnd != null && mydate.date.isAfter(lastInvestEnd);
                    // check
                    /*
                    if (aOneRun.saveLastInvest) {
                        aOneRun.mystocks = aOneRun.savedStocks;
                    }
                    */
		    // index == prev
                    update(data.getCatValMap(aOneRun.adviser.getInterpolate(simConfig.getInterpolate())), aOneRun.mystocks, endIndexOffset + extradelay, new ArrayList<>(), endIndexOffset + extradelay, endIndexOffset);
                    Capital sum = getSum(aOneRun.mystocks);
                    sum.amount += aOneRun.capital.amount;

                    long days = 0;
                    if (investStart != null && investEnd != null) {
                        days = ChronoUnit.DAYS.between(investStart, investEnd);
                    }
                    double years = (double) days / 365;
                    Double score = sum.amount / aOneRun.resultavg;
                    if (score < -0.1) {
                        log.error("Negative amount");
                    }
                    if (score < 0) {
                        score = 0.0;
                    }
                    if (years != 0) {
                        score = Math.pow(score, 1 / years);
                    } else {
                        score = 0.0;
                    }
                    if (score < -1) {
                        int jj = 0;
                    }
                    if (score > 10) {
                        int jj = 0;
                    }
                    if (score > 100) {
                        int jj = 0;
                    }
                    if (score.isNaN()) {
                        int jj = 0;
                    }

                    if (offset == 0) {
                        Map<String, Object> map = new HashMap<>();
                        if (!evolving) {
                            for (SimulateStock stock : aOneRun.mystocks) {
                                // wrong price in dev
                                stock.setSellprice(stock.getPrice());
                                stock.setStatus("END");
                            }
                            aResult.stockhistory.addAll(aOneRun.mystocks);

                            map.put(SimConstants.SUMHISTORY, aResult.sumHistory);
                            map.put(SimConstants.STOCKHISTORY, aResult.stockhistory);
                            map.put(SimConstants.PLOTDEFAULT, aResult.plotDefault);
                            map.put(SimConstants.PLOTDATES, aResult.plotDates);
                            map.put(SimConstants.PLOTCAPITAL, aResult.plotCapital);
                            map.put(SimConstants.STARTDATE, investStart);
                            map.put(SimConstants.ENDDATE, investEnd);
                            map.put(SimConstants.FILTER, JsonUtil.convert(filter));
			    map.put(SimConstants.LASTSTOCKS, aOneRun.mystocks.stream().map(SimulateStock::getId).toList());
                            List<Pair<String, Double>> tradeStocks = SimUtil.getTradeStocks(aResult.stockhistory);
                            map.put(SimConstants.TRADESTOCKS, tradeStocks);
                            if (autolost) {
                                map.put(SimConstants.AUTOMAX, "" + aSimConfig.asMap());
                            }
                            if (autoSimConfig != null && !advMap.isEmpty()) {
                                map.put(SimConstants.ADVISERS, advMap);
                            }
                            param.getUpdateMap().putAll(map);
                            param.getUpdateMap().putIfAbsent(SimConstants.LASTBUYSELL, "Not buying or selling today");
                            componentData.getUpdateMap().putAll(map);
                        } else {
                            if (autoSimConfig != null) {
                                map.put(EvolveConstants.TITLETEXT, emptyNull(autoSimConfig.getStartdate(), "start") + "-" + emptyNull(autoSimConfig.getEnddate(), "end") + " " + (emptyNull(origAdviserId, "all")));
                            } else {
                                map.put(EvolveConstants.TITLETEXT, emptyNull(simConfig.getStartdate(), "start") + "-" + emptyNull(simConfig.getEnddate(), "end") + " " + (emptyNull(origAdviserId, "all")));
                            }
                            map.put(SimConstants.FILTER, JsonUtil.convert(filter));
                            componentData.getUpdateMap().putAll(map);
                        }
                    }
                    if (evolving) {
                        for (SimulateStock stock : aOneRun.mystocks) {
                            stock.setSellprice(stock.getPrice());
                            stock.setStatus("END");
                        }
                        aResult.stockhistory.addAll(aOneRun.mystocks);
                        boolean doFilter = (autoSimConfig != null && autoSimConfig.getImproveFilters()) || (autoSimConfig == null && simConfig.getImproveFilters());
                        if (doFilter) {
                            SimulateFilter afilter;
                            if (autoSimConfig != null) {
                                afilter = filters.get(0)[0];
                            } else {
                                afilter = filters.get(0)[currentSimConfig.getAdviser()];
                            }
                            if (afilter.getStable() > 0) {
                                List<StockHistory> history = aResult.history;
                                if (!history.isEmpty()) {
                                    if (!SimUtil.isStable(afilter, history, null)) {
                                        score = 0.0;
                                    }
                                }
                            }
                            if (afilter.getCorrelation() > 0) {
                                if (!(aResult.plotCapital.size() < 2) && !SimUtil.isCorrelating(afilter, aResult.plotCapital, null)) {
                                    score = 0.0;
                                }
                            }                        
                            if (afilter.getLucky() > 0) {
                                List<StockHistory> history = aResult.history;
                                if (!history.isEmpty()) {
                                    StockHistory last = history.get(history.size() - 1);
                                    double total = last.getCapital().amount + last.getSum().amount - 1;
                                    if (total > 0.0) {
                                        List<Pair<String, Double>> list = SimUtil.getTradeStocks(aResult.stockhistory);
                                        double max = 0;
                                        if (!list.isEmpty()) {
                                            max = list.get(0).getValue();
                                        }
                                        if (max / total > afilter.getLucky()) {
                                            score = 0.0;
                                        }
                                    }
                                }
                            }
                            if (afilter.getShortrun() > 0) {
                                List<StockHistory> history = aResult.history;
                                if (history.size() < afilter.getShortrun()) {
                                    score = 0.0;
                                }
                            }
                        }
                        Map<String, Object> map = new HashMap<>();
                        map.put(SimConstants.HISTORY, aResult.history);
                        map.put(SimConstants.STOCKHISTORY, aResult.stockhistory);
                        map.put(SimConstants.PLOTCAPITAL, aResult.plotCapital);
                        map.put(SimConstants.SCORE, score);
                        map.put(SimConstants.STARTDATE, TimeUtil.convertDate2(investStart));
                        map.put(SimConstants.ENDDATE, TimeUtil.convertDate2(investEnd));
                        if (autoSimConfig != null) {
                            map.put(EvolveConstants.TITLETEXT, emptyNull(autoSimConfig.getStartdate(), "start") + "-" + emptyNull(autoSimConfig.getEnddate(), "end") + " " + (emptyNull(origAdviserId, "all")));
                        } else {
                            map.put(EvolveConstants.SIMTEXT, market.getConfig().getMarket() + " " + emptyNull(simConfig.getStartdate(), "start") + "-" + emptyNull(simConfig.getEnddate(), "end") + " " + (emptyNull(origAdviserId, "all")));
                        }
                        map.put(SimConstants.FILTER, JsonUtil.convert(filter));
                        //map.put("market", market.getConfig().getMarket());
                        resultMap.put("" + offset, map);
                    }
                    scores.add(score);
                    if (aOneRun.lastbuysell != null) {
                        param.getUpdateMap().put(SimConstants.LASTBUYSELL, aOneRun.lastbuysell);
                    }
                }
            }
            if (evolving) {
                componentData.setResultMap(resultMap);
            }
            log.debug("time0 {}", System.currentTimeMillis() - time0);
        }

        Double score = 0.0;
        if (!scores.isEmpty()) {
            OptionalDouble average = scores
                    .stream()
                    .mapToDouble(a -> a)
                    .average();
            score = average.getAsDouble();            
        }
        if (intervalwhole) {
            String stats = scores.stream().filter(Objects::nonNull).mapToDouble(e -> (Double) e).summaryStatistics().toString();
            Map<String, Object> map = new HashMap<>();
            map.put(SimConstants.SCORES, scores);
            map.put(SimConstants.STATS, stats);
            double min = Collections.min(scores);
            double max = Collections.max(scores);
            int minDay = scores.indexOf(min);
            int maxDay = scores.indexOf(max);
            map.put(SimConstants.MINMAX, "min " + min + " at " + minDay + " and max " + max + " at " + maxDay);
            param.getUpdateMap().putAll(map);
            componentData.getUpdateMap().putAll(map);
        }    
        Map<String, Double> scoreMap = new HashMap<>();
        if (score.isNaN()) {
            int jj = 0;
        }
        scoreMap.put("" + score, score);
        scoreMap.put(SimConstants.SCORE, score);
        componentData.setScoreMap(scoreMap);
        //componentData.setFuturedays(0);

        handle2(action, market, componentData, profitdata, positions, evolve, aMap, subcomponent, mlmarket, parameters, hasParent);
        return componentData;
    }

    private Double getScore(AutoSimulateInvestConfig autoSimConfig, OneRun aOneRun, Results aResult) {
        int numlast = autoSimConfig.getLastcount();
        Double score;
        if (numlast == 0 || aOneRun.runs == 1) {
            Capital sum = getSum(aOneRun.mystocks);
            sum.amount += aOneRun.capital.amount;
            score = (sum.amount - 1) / aOneRun.runs;
            if (aResult.plotCapital.size() > 0) {
                if (numlast == 0) {
                    score = (aResult.plotCapital.get(aResult.plotCapital.size() - 1) - 1)/ aResult.plotCapital.size();
                    if (autoSimConfig.getInterval() == 1 && score != ((aResult.plotCapital.get(aResult.plotCapital.size() - 1) - 1)/ aResult.plotCapital.size())) {
                        System.out.println("sc " + score + " " + aOneRun.runs);
                        System.out.println("" + aResult.plotCapital.get(aResult.plotCapital.size() - 1));
                        System.out.println("" + aResult.plotCapital.size());
                        System.out.println("" + (((aResult.plotCapital.get(aResult.plotCapital.size() - 1) - 1)/ aResult.plotCapital.size())));
                        System.out.println("ERRERR");
                    }
                }
                int firstidx = aResult.plotCapital.size() - 1 - numlast;
                if (firstidx < 0 || numlast == 0) {
                    firstidx = 0;
                }
                double newscore = aResult.plotCapital.get(aResult.plotCapital.size() - 1) - aResult.plotCapital.get(firstidx);
                newscore = newscore / (aResult.plotCapital.size() - firstidx);
                if (autoSimConfig.getInterval() == 1 && Math.abs(newscore - score) > 0.00000000001 ) {
                    System.out.println("ERRERR");                    
                }
            } else {
                int jj = 0;
            }
        } else {
            int firstidx = aResult.plotCapital.size() - 1 - numlast;
            if (firstidx < 0) {
                firstidx = 0;
            }
            score = 0.0;
            if (aResult.plotCapital.size() > 0) {
                score = aResult.plotCapital.get(aResult.plotCapital.size() - 1) - aResult.plotCapital.get(firstidx);
                score = score / (aResult.plotCapital.size() - firstidx);
            } else {
                int jj = 0;
            }
        }
        return score;
    }

    private List<Triple<SimulateInvestConfig, OneRun, Results>> getAdviserTriplets(
            List<Triple<SimulateInvestConfig, OneRun, Results>> simTriplets) {
        Integer[] advs = new Integer[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 };
        List<Triple<SimulateInvestConfig, OneRun, Results>> new2 = new ArrayList<>();
        int i = 0;
        for (int j = 0; i < 10 && j < simTriplets.size(); j++) {
            int adv = simTriplets.get(j).getLeft().getAdviser();
            if (advs[adv] == null) {
                continue;
            }
            advs[adv] = null;
            new2.add(simTriplets.get(j));
            i++;
        }
        return new2;
    }

    private List<Triple<SimulateInvestConfig, OneRun, Results>> getTriples(Market market, ComponentData param,
            Data data, LocalDate investStart, LocalDate investEnd, Mydate mydate, List<SimulateInvestConfig> simsConfigs) {
        List<Triple<SimulateInvestConfig, OneRun, Results>> simPairs = new ArrayList<>();
        for (SimulateInvestConfig aConfig : simsConfigs) {
            int adviserId = aConfig.getAdviser();
            OneRun onerun = getOneRun(market, param, aConfig, data, investStart, investEnd, adviserId);

            Results results = new Results();

            Triple<SimulateInvestConfig, OneRun, Results> aTriple = new ImmutableTriple(aConfig, onerun, results);
            simPairs.add(aTriple);
        }

       return simPairs;
    }

    private OneRun getOneRun(Market market, ComponentData param, SimulateInvestConfig simConfig, Data data,
            LocalDate investStart, LocalDate investEnd, Integer adviserId) {
        OneRun onerun = new OneRun();
        if (adviserId != null) {
        onerun.adviser = new AdviserFactory().get(adviserId, market, investStart, investEnd, param, simConfig);
        onerun.adviser.getValueMap(data.stockDates, data.firstidx, data.lastidx, data.getCatValMap(onerun.adviser.getInterpolate(simConfig.getInterpolate())));
        }
        onerun.capital = new Capital();
        onerun.capital.amount = 1;
        onerun.beatavg = 0;
        onerun.runs = 0;
        onerun.mystocks = new ArrayList<>();
        onerun.resultavg = 1;

        onerun.hits = new ImmutablePair[simConfig.getConfidenceFindTimes()];

        onerun.trendInc = new Integer[] { 0 };
        onerun.trendDec = new Integer[] { 0 };

        onerun.saveLastInvest = false;
        return onerun;
    }

    private List<SimulateInvestConfig> getSimConfigs(Map<Pair<LocalDate, LocalDate>, List<SimulateInvestConfig>> simConfigs, Mydate mydate,
            Set<Pair<LocalDate, LocalDate>> keys, Market market) {
        List<SimulateInvestConfig> simsConfigs = new ArrayList<>();
        for (Entry<Pair<LocalDate, LocalDate>, List<SimulateInvestConfig>> entry : simConfigs.entrySet()) {
            Pair<LocalDate, LocalDate> key = entry.getKey();
            List<SimulateInvestConfig> value = entry.getValue();
            int months = Period.between(key.getLeft(), key.getRight()).getMonths();
            LocalDate checkDate = mydate.date.minusMonths(months);
            if (!checkDate.isBefore(key.getLeft()) && checkDate.isBefore(key.getRight())) {
                // TODO value.merge...
                List<SimulateInvestConfig> configs = value;
                for (SimulateInvestConfig config : configs) {
                    SimulateInvestConfig defaultConfig = getSimulate(market.getSimulate());
                    defaultConfig.merge(config);
                    simsConfigs.add(defaultConfig);
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

    private void setExclusions(Market market, Data data, SimulateInvestConfig simConfig) {
        String[] excludes = null;
        if (market.getSimulate() != null) {
            excludes = market.getSimulate().getExcludes();
        }
        if (excludes == null) {
            excludes = new String[0];
        }
        data.configExcludeList = Arrays.asList(excludes);
        Set<String> configExcludeSet = new HashSet<>(data.configExcludeList);

        Set<String> abnormExcludes = getTrendExclude(data, market);
        data.abnormExcludes = abnormExcludes;
        
        data.filteredCategoryValueMap = new HashMap<>(data.getCatValMap(false));
        data.filteredCategoryValueMap.keySet().removeAll(configExcludeSet);
        data.filteredCategoryValueMap.keySet().removeAll(abnormExcludes);
        data.filteredCategoryValueFillMap = new HashMap<>(data.getCatValMap(true));
        data.filteredCategoryValueFillMap.keySet().removeAll(configExcludeSet);
        data.filteredCategoryValueFillMap.keySet().removeAll(abnormExcludes);
    }

    private Set<String> getTrendExclude(Data data, Market market) {
        IclijConfig instance = IclijXMLConfig.getConfigInstance();
        Double margin = instance.getAbnormalChange();
        if (margin == null) {
            return new HashSet<>();
        }
        Set<String> abnormExcludes = null;
        String key = CacheConstants.SIMULATEINVESTTRENDEXCLUDE + market.getConfig().getMarket() + "_" + data.firstidx + "_" + data.lastidx + "_" + margin;
        abnormExcludes = (Set<String>) MyCache.getInstance().get(key);
        Set<String> newAbnormExcludes = null;
        if (abnormExcludes == null || VERIFYCACHE) {
            long time0 = System.currentTimeMillis();
            try {
                newAbnormExcludes = new TrendUtil().getTrend(null, data.stockDates, null, null, data.getCatValMap(false), data.firstidx, data.lastidx, margin);
                log.info("Abnormal excludes {}", newAbnormExcludes);
            } catch (Exception e) {
                log.error(Constants.ERROR, e);
            }
            log.debug("time millis {}", System.currentTimeMillis() - time0);
        }
        if (VERIFYCACHE && abnormExcludes != null) {
            if (newAbnormExcludes != null && !newAbnormExcludes.equals(abnormExcludes)) {
                log.error("Difference with cache");
            }
        }
        if (abnormExcludes != null) {
            return abnormExcludes;
        }
        abnormExcludes = newAbnormExcludes;
        MyCache.getInstance().put(key, abnormExcludes);

        return abnormExcludes;
    }

    private Map<Pair<LocalDate, LocalDate>, List<SimulateInvestConfig>> getSimConfigs(String market, AutoSimulateInvestConfig autoSimConf, List<SimulateFilter> filter, List<SimulateFilter[]> filters, IclijConfig config) {
        List<SimDataItem> all = new ArrayList<>();
        try {
            String simkey = CacheConstants.SIMDATA + market + autoSimConf.getStartdate() + autoSimConf.getEnddate();
            all =  (List<SimDataItem>) MyCache.getInstance().get(simkey);
            if (all == null) {
                LocalDate startDate = TimeUtil.convertDate(TimeUtil.replace(autoSimConf.getStartdate()));
                LocalDate endDate = null;
                if (autoSimConf.getEnddate() != null) {
                    endDate = TimeUtil.convertDate(TimeUtil.replace(autoSimConf.getEnddate()));
                }
                all = SimDataItem.getAll(market, null, null); // fix later: , startDate, endDate);
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
            list = getDefaultList();
            if (list != null) {
                filters.addAll(list);
            }
            if (listoverride != null) {
                mergeFilterList(list, listoverride);
            }
        }
        String listString = JsonUtil.convert(list);
        String key = CacheConstants.AUTOSIMCONFIG + market + autoSimConf.getStartdate() + autoSimConf.getEnddate() + "_" + autoSimConf.getInterval() + "_" + autoSimConf.getPeriod() + "_" + autoSimConf.getScorelimit().doubleValue() + " " + listString;
        Map<Pair<LocalDate, LocalDate>, List<SimulateInvestConfig>> retMap = (Map<Pair<LocalDate, LocalDate>, List<SimulateInvestConfig>>) MyCache.getInstance().get(key);
        Map<Pair<LocalDate, LocalDate>, List<SimulateInvestConfig>> newRetMap = new HashMap<>();
        if (retMap == null || VERIFYCACHE) {
            for (SimDataItem data : all) {
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
                        String configStr = data.getConfig();
                        //SimulateInvestConfig s = JsonUtil.convert(configStr, SimulateInvestConfig.class);
                        Map defaultMap = config.getDeflt();
                        Map map = JsonUtil.convert(configStr, Map.class);
                        Map newMap = new HashMap<>();
                        newMap.putAll(defaultMap);
                        newMap.putAll(map);
                        IclijConfig dummy = new IclijConfig();
                        dummy.setConfigValueMap(newMap);
                        SimulateInvestConfig simConf = getSimConfig(dummy);
                        if (simConf.getInterval().intValue() != autoSimConf.getInterval().intValue()) {
                            continue;
                        }
                        simConf.setVolumelimits(autoSimConf.getVolumelimits());
                        int adviser = simConf.getAdviser();
                        String filterStr = data.getFilter();
                        SimulateFilter myFilter = JsonUtil.convert((String)filterStr, SimulateFilter.class);
                        SimulateFilter[] autoSimConfFilters = list.get(0);
                        if (autoSimConfFilters != null) {
                            SimulateFilter autoSimConfFilter = autoSimConfFilters[adviser];
                            if (myFilter != null && autoSimConfFilter != null) {
                                if (myFilter.getCorrelation() != null && autoSimConfFilter.getCorrelation() > 0 && autoSimConfFilter.getCorrelation() > myFilter.getCorrelation()) {
                                    continue;
                                }
                                if (autoSimConfFilter.getLucky() > 0 && autoSimConfFilter.getLucky() < myFilter.getLucky()) {
                                    continue;
                                }
                                if (autoSimConfFilter.getStable() > 0 && autoSimConfFilter.getStable() < myFilter.getStable()) {
                                    continue;
                                }
                                if (autoSimConfFilter.getShortrun() > 0 && autoSimConfFilter.getShortrun() > myFilter.getShortrun()) {
                                    continue;
                                }
                                if (autoSimConfFilter.getPopulationabove() > myFilter.getPopulationabove()) {
                                    continue;
                                }
                            }
                        }
                        Pair<LocalDate, LocalDate> aKey = new ImmutablePair(data.getStartdate(), data.getEnddate());
                        MapUtil.mapAddMe(newRetMap, aKey, simConf);
                    }
                }
            }
        }
        if (VERIFYCACHE && retMap != null) {
            for (Entry<Pair<LocalDate, LocalDate>, List<SimulateInvestConfig>> entry : newRetMap.entrySet()) {
                Pair<LocalDate, LocalDate> key2 = entry.getKey();
                List<SimulateInvestConfig> v2 = entry.getValue();
                List<SimulateInvestConfig> v = retMap.get(key2);
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

    private List<SimulateFilter[]> getDefaultList() {
        List<SimulateFilter[]> list = null;
        IclijConfig instance = IclijXMLConfig.getConfigInstance();
        try {
            list = IclijXMLConfig.getSimulate(instance);
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        return list;
    }

    private void getAdjustedDate(Data data, LocalDate investStart, int offset, Mydate mydate) {
        mydate.date = investStart;

        mydate.date = TimeUtil.getEqualBefore(data.stockDates, mydate.date);
        if (mydate.date == null) {
            try {
                mydate.date = TimeUtil.convertDate(data.stockDates.get(0));
            } catch (ParseException e) {
                log.error(Constants.ERROR, e);
            }
        }
        mydate.date = TimeUtil.getForwardEqualAfter2(mydate.date, offset, data.stockDates);
        mydate.prevIndexOffset = 0;
    }

    private void getAdjustedDate(Data data, LocalDate investStart, int offset, LocalDate date) {
        date = investStart;

        date = TimeUtil.getEqualBefore(data.stockDates, date);
        if (date == null) {
            try {
                date = TimeUtil.convertDate(data.stockDates.get(0));
            } catch (ParseException e) {
                log.error(Constants.ERROR, e);
            }
        }
        date = TimeUtil.getForwardEqualAfter2(date, offset, data.stockDates);
        //return data.stockDates.size() - 1 - data.stockDates.indexOf(date)
    }

    private void setDataVolumeAndTrend(Market market, ComponentData param, SimulateInvestConfig simConfig, Data data,
            LocalDate investStart, LocalDate investEnd, LocalDate lastInvestEnd, boolean evolving) {
        
        // vol lim w/ adviser?
        data.volumeExcludeMap = getVolumeExcludeMap(market, simConfig, data, investStart, investEnd, data.firstidx, data.lastidx, false);
        data.volumeExcludeFillMap = getVolumeExcludeMap(market, simConfig, data, investStart, investEnd, data.firstidx, data.lastidx, true);

        data.trendMap = getTrendIncDec(market, param, data.stockDates, simConfig.getInterval(), data.firstidx, data.lastidx, simConfig, data, false);
        data.trendFillMap = getTrendIncDec(market, param, data.stockDates, simConfig.getInterval(), data.firstidx, data.lastidx, simConfig, data, true);
        if (evolving) {
            data.trendStrMap = getTrendIncDecStr(market, param, data.stockDates, simConfig.getInterval(), data.firstidx, data.lastidx, simConfig, data, false, data.trendMap);
            data.trendStrFillMap = getTrendIncDecStr(market, param, data.stockDates, simConfig.getInterval(), data.firstidx, data.lastidx, simConfig, data, true, data.trendFillMap);
        }
        Set<Integer> keys = data.trendMap.keySet();
        List<Integer> keylist = new ArrayList<>(keys);
        Collections.sort(keylist);
        log.debug("keylist {}", keylist);
    }

    private void setDataIdx(Data data, LocalDate investStart, LocalDate lastInvestEnd) {
        LocalDate date = investStart;

        date = TimeUtil.getEqualBefore(data.stockDates, date);
        if (date == null) {
            try {
                date = TimeUtil.convertDate(data.stockDates.get(0));
            } catch (ParseException e) {
                log.error(Constants.ERROR, e);
            }
        }
        date = TimeUtil.getForwardEqualAfter2(date, 0 /* findTime */, data.stockDates);
        String datestring = TimeUtil.convertDate2(date);
        int firstidx = TimeUtil.getIndexEqualAfter(data.stockDates, datestring);
        int maxinterval = 20;
        firstidx -= maxinterval * 2;
        if (firstidx < 0) {
            firstidx = 0;
        }
        
        String lastInvestEndS = TimeUtil.convertDate2(lastInvestEnd);
        int lastidx = data.stockDates.indexOf(lastInvestEndS);
        lastidx += maxinterval;
        if (lastidx >= data.stockDates.size()) {
            lastidx = data.stockDates.size() - 1;
        }
        firstidx = data.stockDates.size() - 1 - firstidx;
        lastidx = data.stockDates.size() - 1 - lastidx;
        data.firstidx = firstidx;
        data.lastidx = lastidx;
    }

    private Map<Integer, List<String>> getVolumeExcludeMap(Market market, SimulateInvestConfig simConfig, Data data, LocalDate investStart,
            LocalDate investEnd, int firstidx, int lastidx, boolean interpolate) {
        String key = CacheConstants.SIMULATEINVESTVOLUMELIMITS + market.getConfig().getMarket() + "_" + simConfig.getInterval() + investStart + investEnd + interpolate + simConfig.getVolumelimits();
        Map<Integer, List<String>> verifyVolumeExcludeMap = data.getVolumeExcludeMap(interpolate);
        Map<Integer, List<String>> volumeExcludeMap = (Map<Integer, List<String>>) MyCache.getInstance().get(key);
        Map<Integer, List<String>> newVolumeExcludeMap = null;
        if (volumeExcludeMap == null || VERIFYCACHE) {
            long time00 = System.currentTimeMillis();
            newVolumeExcludeMap = getVolumeExcludesFull(simConfig, simConfig.getInterval(), data.getCatValMap(interpolate), data.volumeMap, firstidx, lastidx);
            log.debug("time0 {}", System.currentTimeMillis() - time00);
        }
        verifyVolumeExcludeMap(newVolumeExcludeMap, verifyVolumeExcludeMap);
        if (volumeExcludeMap == null) {
            volumeExcludeMap = newVolumeExcludeMap;
            MyCache.getInstance().put(key, volumeExcludeMap);
        }
        return volumeExcludeMap;
    }

    private void verifyVolumeExcludeMap(Map<Integer, List<String>> newVolumeExcludeMap,
            Map<Integer, List<String>> aVolumeExcludeMap) {
        if (VERIFYCACHE && aVolumeExcludeMap != null) {
            for (Entry<Integer, List<String>> entry : newVolumeExcludeMap.entrySet()) {
                int key2 = entry.getKey();
                List<String> v2 = entry.getValue();
                List<String> v = aVolumeExcludeMap.get(key2);
                if (v2 != null && !v2.equals(v)) {
                    log.error("Difference with cache");
                }
            }
        }
    }

    private LocalDate getAdjustedLastInvestEnd(Data data, LocalDate investEnd, int delay) {
        LocalDate lastInvestEnd = TimeUtil.getBackEqualBefore2(investEnd, 0 /* findTime */, data.stockDates);
        if (lastInvestEnd != null) {
            String aDate = TimeUtil.convertDate2(lastInvestEnd);
            if (aDate != null) {
                int idx = data.stockDates.indexOf(aDate) - delay;
                if (idx >=0 ) {
                    aDate = data.stockDates.get(idx);
                    try {
                        lastInvestEnd = TimeUtil.convertDate(aDate);
                    } catch (ParseException e) {
                        log.error(Constants.EXCEPTION, e);
                    }
                }
            }
        }
        return lastInvestEnd;
    }

    private LocalDate getAdjustedInvestEnd(int extradelay, Data data, LocalDate investEnd) {
        investEnd = TimeUtil.getBackEqualBefore2(investEnd, 0 /* findTime */, data.stockDates);
        if (investEnd != null) {
            String aDate = TimeUtil.convertDate2(investEnd);
            if (aDate != null) {
                int idx = data.stockDates.indexOf(aDate);
                if (idx >=0 ) {
                    aDate = data.stockDates.get(idx);
                    try {
                        investEnd = TimeUtil.convertDate(aDate);
                    } catch (ParseException e) {
                        log.error(Constants.EXCEPTION, e);
                    }
                }
            }
        }
        return investEnd;
    }

    private String getMlDate(Market market, SimulateInvestConfig simConfig, Data data, AutoSimulateInvestConfig autoSimConfig) {
        String mldate = null;
        if (simConfig.getMldate()) {
            mldate = market.getConfig().getMldate();
            if (mldate != null) {
                mldate = mldate.replace('-', '.');
            }
            //mldate = ((SimulateInvestActionData) action).getMlDate(market, stockDates);
            //mldate = ((ImproveSimulateInvestActionData) action).getMlDate(market, stockDates);
        } else {
            Short populate = market.getConfig().getPopulate();
            if (populate == null) {
                //mldate = ((SimulateInvestActionData) action).getMlDate(market, stockDates);                
                mldate = market.getConfig().getMldate();
                if (mldate != null) {
                    mldate = mldate.replace('-', '.');
                }
            } else {
                mldate = data.stockDates.get(populate);                
            }
        }
        if (mldate == null) {
            mldate = data.stockDates.get(0);
        }
        if (autoSimConfig != null && autoSimConfig.getStartdate() != null) {
            mldate = autoSimConfig.getStartdate();
            mldate = mldate.replace('-', '.');
            return mldate;
        }
        if (simConfig.getStartdate() != null) {
            mldate = simConfig.getStartdate();
            mldate = mldate.replace('-', '.');
        }
        return mldate;
    }

    private void doOneRun(ComponentData param, SimulateInvestConfig simConfig, int extradelay, boolean evolving,
            String aParameter, int offset, OneRun onerun,
            Results results, Data data, boolean lastInvest,
            Mydate mydate, boolean auto, BiMap<String, LocalDate> stockDatesBiMap, boolean isMain, int endIndexOffset) {
        if (getBSValueIndexOffset(mydate, simConfig) >= endIndexOffset) {
            doBuySell(simConfig, onerun, results, data, mydate.indexOffset, stockDatesBiMap);
        } else {
            int jj = 0;
        }
        /*
        if (lastInvest) {
            // not with evolving?
            onerun.savedStocks = copy(onerun.mystocks);
            onerun.saveLastInvest = true;
        }
        */
        //Trend trend = getTrendIncDec(market, param, stockDates, interval, filteredCategoryValueMap, trendInc, trendDec, indexOffset);
        // no interpolation for trend
        Trend trend = getTrendIncDec(data.stockDates, onerun.trendInc, onerun.trendDec, getValueIndexOffset(mydate, simConfig), data.getTrendMap(false /*simConfig.getInterpolate()*/));
        // get recommendations

        List<String> myExcludes = getExclusions(simConfig, data.stockDates, data.configExcludeList, data.abnormExcludes, getValueIndexOffset(mydate, simConfig), data.getVolumeExcludeMap(onerun.adviser.getInterpolate(simConfig.getInterpolate())));

        double myavg = increase(onerun.mystocks, getValueIndexOffset(mydate, simConfig), data.getCatValMap(onerun.adviser.getInterpolate(simConfig.getInterpolate())), mydate.prevIndexOffset + extradelay, endIndexOffset);

        List<SimulateStock> holdIncrease = new ArrayList<>();
        int up = update(data.getCatValMap(onerun.adviser.getInterpolate(simConfig.getInterpolate())), onerun.mystocks, getValueIndexOffset(mydate, simConfig), holdIncrease, mydate.prevIndexOffset + extradelay, endIndexOffset);
        
        List<SimulateStock> sells = new ArrayList<>();
        List<SimulateStock> buys = new ArrayList<>();
        onerun.buys = buys;
        
        double myreliability = getReliability(onerun.mystocks, onerun.hits, simConfig.getConfidenceFindTimes(), up);
        
        if (simConfig.getStoploss()) {
            stoploss(onerun.mystocks, data.stockDates, getValueIndexOffset(mydate, simConfig), data.getCatValMap(onerun.adviser.getInterpolate(simConfig.getInterpolate())), getValueIndexOffset(mydate, simConfig) + 1, sells, simConfig.getStoplossValue(), "STOP", stockDatesBiMap, endIndexOffset);
        }
        if (simConfig.getIntervalStoploss()) {
            stoploss(onerun.mystocks, data.stockDates, getValueIndexOffset(mydate, simConfig), data.getCatValMap(onerun.adviser.getInterpolate(simConfig.getInterpolate())), getValueIndexOffset(mydate, simConfig) + simConfig.getInterval(), sells, simConfig.getIntervalStoplossValue(), "ISTOP", stockDatesBiMap, endIndexOffset);
        }
        holdIncrease.removeAll(sells);

        boolean confidence = !simConfig.getConfidence() || myreliability >= simConfig.getConfidenceValue();
        boolean confidence1 = !simConfig.getConfidencetrendincrease() || onerun.trendInc[0] >= simConfig.getConfidencetrendincreaseTimes();
        boolean noconfidence2 = simConfig.getNoconfidencetrenddecrease() && onerun.trendDec[0] >= simConfig.getNoconfidencetrenddecreaseTimes();
        boolean noconfidence = !confidence || !confidence1 || noconfidence2;
        List<SimulateStock> nextstocks = new ArrayList<>();
        if (!noconfidence) {
            nextstocks = confidenceBuyHoldSell(simConfig, data.stockDates, data. getCatValMap(onerun.adviser.getInterpolate(simConfig.getInterpolate())), onerun.adviser, myExcludes,
                    aParameter, onerun.mystocks, sells, buys, holdIncrease, mydate);
        } else {
            nextstocks = noConfidenceHoldSell(onerun.mystocks, holdIncrease, sells, simConfig);
        }

        sells = addEvent(onerun, sells, "SELL", getBSIndexOffset(mydate, simConfig));
        buys = addEvent(onerun, buys, "BUY", getBSIndexOffset(mydate, simConfig));

        if (true) {
            if (getBSValueIndexOffset(mydate, simConfig) >= endIndexOffset) {
                doBuySell(simConfig, onerun, results, data, mydate.indexOffset, stockDatesBiMap);
            } else {
                int jj = 0;
            }
            if (true) {
                List<String> myids = onerun.mystocks.stream().map(SimulateStock::getId).collect(Collectors.toList());            
                if (myids.size() != onerun.mystocks.size()) {
                    log.error("Sizes");
                }

                // to delay?
                update(data.getCatValMap(onerun.adviser.getInterpolate(simConfig.getInterpolate())), onerun.mystocks, getValueIndexOffset(mydate, simConfig), new ArrayList<>(), mydate.prevIndexOffset + extradelay, endIndexOffset);

                if (trend != null && trend.incAverage != 0) {
                    onerun.resultavg *= trend.incAverage;
                }

                // depends on delay DELAY
                Capital sum = getSum(onerun.mystocks);

                //boolean noconf = simConfig.getConfidence() && myreliability < simConfig.getConfidenceValue();                
                String hasNoConf = noconfidence ? "NOCONF" : "";
                String historydatestring = TimeUtil.convertDate2(mydate.date); //data.stockDates.get(data.stockDates.size() - 1 - (mydate.indexOffset - extradelay - simConfig.getDelay()));

                List<String> ids = onerun.mystocks.stream().map(SimulateStock::getId).collect(Collectors.toList());
                if (!evolving) {
                    if (getBSValueIndexOffset(mydate, simConfig) >= endIndexOffset) {
                    if (offset == 0) {
                        String adv = auto ? " Adv" + simConfig.getAdviser() + " " + simConfig.getIndicatorReverse() + " " + simConfig.hashCode() : "";
                        results.sumHistory.add(historydatestring + " " + onerun.capital.toString() + " " + sum.toString() + " " + new MathUtil().round(onerun.resultavg, 2) + " " + hasNoConf + " " + ids + " " + trend + adv);
                        results.plotDates.add(historydatestring);
                        results.plotDefault.add(onerun.resultavg);
                        results.plotCapital.add(sum.amount + onerun.capital.amount);
                    } else {
                        results.plotDates.add(historydatestring);                        
                        results.plotCapital.add(sum.amount + onerun.capital.amount);
                    }
                    }
                } else {
                    results.plotDates.add(historydatestring);
                    results.plotCapital.add(sum.amount + onerun.capital.amount);
                    Integer adv = auto ? simConfig.getAdviser() : null;
                    Capital aCapital = new Capital();
                    aCapital.amount = onerun.capital.amount;
                    // no interpolation for trend
                    String trendStr = getTrendIncDecStr(data.stockDates, onerun.trendInc, onerun.trendDec, getValueIndexOffset(mydate, simConfig), data.getTrendStrMap(false /*simConfig.getInterpolate()*/));
                    StockHistory aHistory = new StockHistory(historydatestring, aCapital, sum, onerun.resultavg, hasNoConf, ids, trendStr, adv);
                    results.history.add(aHistory);
                }

                if (Double.isInfinite(onerun.resultavg)) {
                    int jj = 0;
                }

                onerun.runs++;
                if (myavg > trend.incAverage) {
                    onerun.beatavg++;
                }
            }

        }
        if (!lastInvest) {
         
            // in last too?
            // 1
            for (int j = 1; j < simConfig.getInterval(); j++) {
                if (mydate.indexOffset - j < endIndexOffset) {
                    break;
                }
                if (getBSValueIndexOffset(mydate, simConfig) - j >= endIndexOffset) {
                    doBuySell(simConfig, onerun, results, data, mydate.indexOffset - j, stockDatesBiMap);
                } else {
                    int jj = 0;
                }
                update(data.getCatValMap(onerun.adviser.getInterpolate(simConfig.getInterpolate())), onerun.mystocks, getValueIndexOffset(mydate, simConfig) - j, new ArrayList<>(), mydate.prevIndexOffset + extradelay, endIndexOffset);
                sells = new ArrayList<>();
                //System.out.println(interval + " " +  j);
                if (simConfig.getStoploss()) {
                    // TODO delay DELAY
                    stoploss(onerun.mystocks, data.stockDates, getValueIndexOffset(mydate, simConfig) - j, data.getCatValMap(onerun.adviser.getInterpolate(simConfig.getInterpolate())), getValueIndexOffset(mydate, simConfig) - j + 1, sells, simConfig.getStoplossValue(), "STOP", stockDatesBiMap, endIndexOffset);
                }

                sells = addEvent(onerun, sells, "SELL", getBSIndexOffset(mydate, simConfig) - j);

		//addEvent(onerun, buys, "BUY", mydate.indexOffset - j - simConfig.getDelay() - extradelay);
                if (getBSValueIndexOffset(mydate, simConfig) - j >= endIndexOffset) {
                    doBuySell(simConfig, onerun, results, data, mydate.indexOffset - j, stockDatesBiMap);
                } else {
                    int jj = 0;
                }
		//sell(data.stockDates, data.getCatValMap(simConfig.getInterpolate()), onerun.capital, sells, results.stockhistory, mydate.indexOffset - j - extradelay - simConfig.getDelay(), mydate.date, onerun.mystocks, stockDatesBiMap);
                if (offset == 0 && !sells.isEmpty()) {
                    boolean last = mydate.indexOffset - j == endIndexOffset;
                    boolean aLastInvest = offset == 0 && last /*date.isAfter(lastInvestEnd) && j == simConfig.getInterval() - 1*/;
                    if (aLastInvest /*&& isMain*/) {
                        List<String> ids = onerun.mystocks.stream().map(SimulateStock::getId).collect(Collectors.toList());
                        List<String> sellids = sells.stream().map(SimulateStock::getId).collect(Collectors.toList());
                        //ids.removeAll(sellids);
                        // TODO new portofolio?
                        String adv = auto ? " Adv" + simConfig.getAdviser() + " " + simConfig.getIndicatorReverse() + " " + simConfig.hashCode(): "";
                        onerun.lastbuysell = "Stoploss sell: " + sellids + " Stocks: " + ids + adv;
                    } else {
                        int jj = 0;
                    }
                }
            }
        } else {
            if (!evolving && offset == 0) {
                List<String> ids = onerun.mystocks.stream().map(SimulateStock::getId).collect(Collectors.toList());
                List<String> buyids = buys.stream().map(SimulateStock::getId).collect(Collectors.toList());
                List<String> sellids = sells.stream().map(SimulateStock::getId).collect(Collectors.toList());

                if (!noconfidence) {
                    int buyCnt = simConfig.getStocks() - nextstocks.size();
                    buyCnt = Math.min(buyCnt, buys.size());
                    //ids.addAll(buyids.subList(0, buyCnt));
                    buyids = buyids.subList(0, buyCnt);
                } else {
                    buyids.clear();
                }
                //ids.removeAll(sellids);
                if (true /*isMain*/) {
                    String newids = "";
                    String adv = auto ? " Adv" + simConfig.getAdviser() + " " + simConfig.getIndicatorReverse() + " " + simConfig.hashCode(): "";
                    if (extradelay == 0) {
                        List<String> idsnew = new ArrayList<>(ids);
                        idsnew.addAll(buyids);
                        idsnew.removeAll(sellids);
                        newids = " -> " + idsnew;
                    }
                    onerun.lastbuysell = "Buy: " + buyids + " Sell: " + sellids + " Stocks: " + ids + newids + adv;
                }
            }
        }
    }

    private List<SimulateStock> addEvent(OneRun onerun, List<SimulateStock> stocks, String bs, int myIndexoffset) {
        if (stocks == null || stocks.isEmpty()) {
            return stocks;
        }
        if (idbreak != null && !stocks.stream().filter(s -> s.getId().equals(idbreak)).toList().isEmpty()) {
            int jj = 0;
        }
        Map<String, List<SimulateStock>> aBSMap = new HashMap<>();
        aBSMap.put(bs, stocks);
        Map<String, List<SimulateStock>> bsMap = onerun.eventMap.computeIfAbsent(myIndexoffset, k -> new HashMap<>());
        bsMap.putAll(aBSMap);
        return stocks;
    }

    @Deprecated
    private List<SimulateStock> getFilterStocks(OneRun onerun, List<SimulateStock> stocks) {
        List<SimulateStock> filterStocks = new ArrayList<>();
        for (SimulateStock stock : stocks) {
            boolean found = false;
            for (Entry<Integer, Map<String, List<SimulateStock>>> entry : onerun.eventMap.entrySet()) {
                Map<String, List<SimulateStock>> aMap = entry.getValue();
                for (Entry<String, List<SimulateStock>> anEntry : aMap.entrySet()) {
                    if (anEntry.getValue().stream().map(SimulateStock::getId).toList().contains(stock.getId())) {
                        found = true;
                    }
                }
            }
            if (!found) {
                filterStocks.add(stock);
            }
        }
        return filterStocks;
    }

    private List<SimulateStock> getFilterStocks(List<SimulateStock> stocks, List<SimulateStock> buys, boolean buy) {
        List<SimulateStock> filterStocks = new ArrayList<>();
        for (SimulateStock stock : stocks) {
            if (buy != buys.stream().map(SimulateStock::getId).toList().contains(stock.getId())) {
                filterStocks.add(stock);
            }
        }
        return filterStocks;
    }

    private void doBuySell(SimulateInvestConfig simConfig, OneRun onerun, Results results, Data data, int indexOffset,
            BiMap<String, LocalDate> stockDatesBiMap) {
        Map<String, List<SimulateStock>> myMaps = onerun.eventMap.remove(indexOffset);
        if (myMaps != null) {
            List<SimulateStock> mySells = myMaps.remove("SELL");
            if (mySells != null) {
                mySells = getFilterStocks(mySells, onerun.mystocks, false);
                sell(data.stockDates, data.getCatValMap(onerun.adviser.getInterpolate(simConfig.getInterpolate())), onerun.capital, mySells, results.stockhistory, indexOffset, onerun.mystocks, stockDatesBiMap);
            }
            List<SimulateStock> myBuys = myMaps.remove("BUY");
            if (myBuys != null) {
                myBuys = getFilterStocks(myBuys, onerun.mystocks, true);
                buy(data.stockDates, data.getCatValMap(onerun.adviser.getInterpolate(simConfig.getInterpolate())), onerun.capital, simConfig.getStocks(), onerun.mystocks, myBuys, indexOffset, stockDatesBiMap);
            }
        }
    }

    private BiMap<String, LocalDate> getStockDatesBiMap(String market, List<String> stockDates) {
        String key = CacheConstants.DATESMAP + market; // + config.getDate();
        BiMap<String, LocalDate> list =  (BiMap<String, LocalDate>) MyCache.getInstance().get(key);
        if (list != null) {
            return list;
        }
        list = createStockDatesBiMap(stockDates);
        MyCache.getInstance().put(key, list);
        return list;
    }

    private BiMap<String, LocalDate> createStockDatesBiMap(List<String> stockDates) {
        BiMap<String, LocalDate> biMap = HashBiMap.create();
        for (String aDate : stockDates) {
            try {
                LocalDate date = TimeUtil.convertDate(aDate);
                biMap.put(aDate, date);
            } catch (ParseException e) {
                log.error(Constants.EXCEPTION, e);
            }            
        }
        return biMap;
    }

    private List<SimulateStock> copy(List<SimulateStock> mystocks) {
        List<SimulateStock> list = new ArrayList<>();
        for (SimulateStock stock : mystocks) {
            SimulateStock copy = stock.copy();
            list.add(copy);
        }
        return list;
    }

    private List<String> getExclusions(SimulateInvestConfig simConfig, List<String> stockDates, List<String> configExcludeList,
            Set<String> abnormExcludes, int indexOffset, Map<Integer, List<String>> newVolumeMap) {
        List<String> myExcludes = new ArrayList<>();
        List<String> volumeExcludes = new ArrayList<>();
        /*
        getVolumeExcludes(simConfig, extradelay, stockDates, interval, categoryValueMap, volumeMap, delay,
                indexOffset, volumeExcludes);
                */
        long time0 = System.currentTimeMillis();
        getVolumeExcludes(simConfig, stockDates, indexOffset, volumeExcludes, newVolumeMap);
        //log.info("timed0 {}", System.currentTimeMillis() - time0);        
        
        myExcludes.addAll(configExcludeList);
        myExcludes.addAll(abnormExcludes);
        myExcludes.addAll(volumeExcludes);
        return myExcludes;
    }

    private Trend getTrendIncDec(List<String> stockDates, Integer[] trendInc, Integer[] trendDec, int indexOffset,
            Map<Integer, Trend> trendMap) {
        int idx = stockDates.size() - 1 - indexOffset;
        Trend trend = trendMap.get(idx);
        if (trend == null) {
            int jj = 0;
        }
        if (trend != null && trend.incAverage < 0) {
            int jj = 0;
        }
        log.debug("Trend {}", trend);

        if (trend != null) {
            if (trend.incAverage > 1) {
                trendInc[0]++;
                trendDec[0] = 0;
            } else {
                trendInc[0] = 0;
                trendDec[0]++;
            }
        }
        return trend;
    }

    private String getTrendIncDecStr(List<String> stockDates, Integer[] trendInc, Integer[] trendDec, int indexOffset,
            Map<Integer, String> trendMap) {
        int idx = stockDates.size() - 1 - indexOffset;
        String trend = trendMap.get(idx);
        return trend;
    }

    private Map<Integer, Trend> getTrendIncDec(Market market, ComponentData param, List<String> stockDates, int interval,
            int firstidx, int lastidx, SimulateInvestConfig simConfig, Data data, boolean interpolate) {
        Map<Integer, Trend> trendMap = null;
        String key = CacheConstants.SIMULATEINVESTTREND + market.getConfig().getMarket() + "_" + interval + "_" + simConfig.getStartdate() + simConfig.getEnddate() + interpolate;
        trendMap = (Map<Integer, Trend>) MyCache.getInstance().get(key);
        Map<Integer, Trend> newTrendMap = null;
        if (trendMap == null || VERIFYCACHE) {
            long time0 = System.currentTimeMillis();
            try {
                newTrendMap = new TrendUtil().getTrend(interval, null, stockDates, param, market, data.getFilterCatValMap(interpolate), firstidx, lastidx);
            } catch (Exception e) {
                log.error(Constants.ERROR, e);
            }
            log.debug("time millis {}", System.currentTimeMillis() - time0);
        }
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
        if (trendMap != null) {
            return trendMap;
        }
        trendMap = newTrendMap;
        MyCache.getInstance().put(key, trendMap);
        return trendMap;
    }

    private Map<Integer, String> getTrendIncDecStr(Market market, ComponentData param, List<String> stockDates, int interval,
            int firstidx, int lastidx, SimulateInvestConfig simConfig, Data data, boolean interpolate, Map<Integer, Trend> trendMap) {
        Map<Integer, String> trendStrMap = null;
        String key = CacheConstants.SIMULATEINVESTTREND + market.getConfig().getMarket() + "_" + interval + "_" + simConfig.getStartdate() + simConfig.getEnddate() + interpolate + "str";
        trendStrMap = (Map<Integer, String>) MyCache.getInstance().get(key);
        if (trendStrMap != null) {
            return trendStrMap;
        }
        trendStrMap = new HashMap<>();
        for (Entry<Integer, Trend> entry : trendMap.entrySet()) {
            Integer aKey = entry.getKey();
            Trend trend = entry.getValue();
            trendStrMap.put(aKey, trend.toString());
        }
        MyCache.getInstance().put(key, trendStrMap);
        return trendStrMap;
    }

    @Deprecated
    private void getVolumeExcludes(SimulateInvestConfig simConfig, int extradelay, List<String> stockDates,
            int interval, Map<String, List<List<Double>>> categoryValueMap,
            Map<String, List<List<Object>>> volumeMap, int delay, int indexOffset, List<String> volumeExcludes) {
        if (simConfig.getVolumelimits() != null) {
            Map<String, Double> volumeLimits = simConfig.getVolumelimits();
            for (Entry<String, List<List<Double>>> entry : categoryValueMap.entrySet()) {
                String id = entry.getKey();
                int anOffset = indexOffset /* - extradelay - delay */;
                int len = interval * 2;
                List<List<Double>> resultList = categoryValueMap.get(id);
                if (resultList == null || resultList.isEmpty()) {
                    continue;
                }
                List<Double> mainList = resultList.get(0);
                //ValidateUtil.validateSizes(mainList, stockDates);
                if (mainList != null) {
                    int size = mainList.size();
                    int first = size - anOffset - len;
                    if (first < 0) {
                        first = 0;
                    }
                    List<List<Object>> list = volumeMap.get(id);
                    String currency = null;
                    for (int i = 0; i < list.size(); i++) {
                        currency = (String) list.get(i).get(1);
                        if (currency != null) {
                            break;
                        }
                    }
                    Double limit = volumeLimits.get(currency);
                    if (limit == null) {
                        continue;
                    }
                    double sum = 0.0;
                    int count = 0;
                    for (int i = first; i <= size - 1 - anOffset; i++) {
                        Integer volume = (Integer) list.get(i).get(0);
                        if (volume != null) {
                            Double price = mainList.get(i /* mainList.size() - 1 - indexOffset */);
                            if (price == null) {
                                if (volume > 0) {
                                    log.debug("Price null with volume > 0");
                                }
                                continue;
                            }
                            sum += volume * price;
                            count++;
                        }
                    }
                    if (count > 0 && sum / count < limit) {
                        volumeExcludes.add(id);
                    }
                }
            }
        }
    }

    @Deprecated
    private Map<String, double[]> getVolumeExcludesFull(SimulateInvestConfig simConfig, List<String> stockDates, int interval,
            Map<String, List<List<Double>>> categoryValueMap, Map<String, List<List<Object>>> volumeMap) {
        Map<String, double[]> newVolumeMap = new HashMap<>(); 
        if (simConfig.getVolumelimits() != null) {
            for (Entry<String, List<List<Double>>> entry : categoryValueMap.entrySet()) {
                String id = entry.getKey();
                int len = interval * 2;
                List<List<Double>> resultList = categoryValueMap.get(id);
                if (resultList == null || resultList.isEmpty()) {
                    continue;
                }
                List<Double> mainList = resultList.get(0);
                //ValidateUtil.validateSizes(mainList, stockDates);
                List<List<Object>> list = volumeMap.get(id);
                if (mainList != null) {
                    int size = mainList.size();
                    double[] newList = new double[size];
                    for (int i = 0; i < size; i++) {
                        Integer volume = (Integer) list.get(i).get(0);
                        Double price = mainList.get(i /* mainList.size() - 1 - indexOffset */);
                        if (volume != null) {
                            if (price == null) {
                                if (volume > 0) {
                                    log.debug("Price null with volume > 0");
                                }
                                continue;
                            }
                        } else {
                            continue; 
                        }
                        for (int j = 0; j < len; j++) {
                            int idx = i + j;
                            if (idx > size - 1) {
                                break;
                            }
                            newList[idx] += price * volume;
                        }
                    }
                    newVolumeMap.put(id, newList);
                }
            }
        }
        return newVolumeMap;
    }

    @Deprecated
    private void getVolumeExcludes(SimulateInvestConfig simConfig, int extradelay, List<String> stockDates,
            int interval, Map<String, List<List<Double>>> categoryValueMap,
            Map<String, List<List<Object>>> volumeMap, int delay, int indexOffset, List<String> volumeExcludes, Map<String, double[]> newVolumeMap) {
        if (simConfig.getVolumelimits() != null) {
            Map<String, Double> volumeLimits = simConfig.getVolumelimits();
            int len = interval * 2;
            for (Entry<String, List<List<Double>>> entry : categoryValueMap.entrySet()) {
                String id = entry.getKey();
                String currency = getCurrency(volumeMap, id);
                Double limit = volumeLimits.get(currency);
                if (limit == null) {
                    continue;
                }
                int count = len;
                double[] sums = newVolumeMap.get(id);
                int idx = sums.length - 1 - indexOffset;
                if (idx < count) {
                    count = idx;
                }
                double sum = sums[idx];
                if (count > 0 && sum / count < limit) {
                    volumeExcludes.add(id);
                }
            }
        }
    }

    private String getCurrency(Map<String, List<List<Object>>> volumeMap, String id) {
        String currency = null;
        List<List<Object>> list = volumeMap.get(id);
        for (int i = list.size() - 1; i >= 0; i--) {
            currency = (String) list.get(i).get(1);
            if (currency != null) {
                break;
            }
        }
        return currency;
    }

    Map<Integer, List<String>> getVolumeExcludesFull(SimulateInvestConfig simConfig, int interval, Map<String, List<List<Double>>> categoryValueMap,
            Map<String, List<List<Object>>> volumeMap, int firstidx, int lastidx) {
        Map<Integer, List<String>> listlist = new HashMap<>(); 
        if (simConfig.getVolumelimits() != null) {
            Map<String, Double> volumeLimits = simConfig.getVolumelimits();
            for (Entry<String, List<List<Double>>> entry : categoryValueMap.entrySet()) {
                String id = entry.getKey();
                String currency = getCurrency(volumeMap, id);
                Double limit = volumeLimits.get(currency);
                if (limit == null) {
                    continue;
                }
                int len = interval * 2;
                List<List<Double>> resultList = categoryValueMap.get(id);
                if (resultList == null || resultList.isEmpty()) {
                    continue;
                }
                List<Double> mainList = resultList.get(0);
                //ValidateUtil.validateSizes(mainList, stockDates);
                List<List<Object>> list = volumeMap.get(id);
                if (mainList != null) {
                    int size = mainList.size();
                    MutablePair<Double, Integer>[] newList = new MutablePair[size];
                    int start = size - 1 - firstidx;
                    int end = size - 1 - lastidx;
                    for (int i = start; i <= end; i++) {
                        Integer volume = (Integer) list.get(i).get(0);
                        Double price = mainList.get(i /* mainList.size() - 1 - indexOffset */);
                        if (volume != null) {
                            if (price == null) {
                                if (volume > 0) {
                                    log.debug("Price null with volume > 0");
                                }
                                continue;
                            }
                        } else {
                            continue; 
                        }
                        for (int j = 0; j < len; j++) {
                            int idx = i + j;
                            if (idx > size - 1) {
                                break;
                            }
                            MutablePair<Double, Integer> pair = newList[idx];
                            if (pair == null) {
                                pair = new MutablePair(0.0, 0);
                                newList[idx] = pair;
                            }
                            pair.setLeft(pair.getLeft() + price * volume);
                            pair.setRight(pair.getRight() + 1);
                        }
                    }
                    // now make the skip lists
                    int count = len;
                    Pair<Double, Integer>[] sums = newList; 
                    for (int i = start; i <= end; i++) {
                        List<String> datedlist = listlist.get(i);
                        if (datedlist == null) {
                            datedlist = new ArrayList<>();
                            listlist.put(i, datedlist);
                        }
                        int idx = i;
                        if (idx < count) {
                            count = idx;
                        }
                        Pair<Double, Integer> pair = sums[idx];
                        if (pair == null) {
                            datedlist.add(id);
                            continue;
                        }
                        Double sum = pair.getLeft();
                        Integer count2 = pair.getRight();
                        if (count2 != null && count != count2) {
                            int jj = 0;
                        }
                        if (count2 != null && count2 == 0) {
                            int jj = 0;
                        }
                        if (count2 != 0 && sum / count2 < limit) {
                            datedlist.add(id);
                        }
                    }
                }
            }
        }
        Set<Integer> keys = listlist.keySet();
        List<Integer> keylist = new ArrayList<>(keys);
        Collections.sort(keylist);
        log.debug("keylist {}", keylist);
        for (int key : keylist) {
            log.debug("keylist size {} {}", key, listlist.get(key).size());
        }
        return listlist;
    }

    private void getVolumeExcludes(SimulateInvestConfig simConfig, List<String> stockDates, int indexOffset,
            List<String> volumeExcludes, Map<Integer, List<String>> listlist) {
        if (simConfig.getVolumelimits() != null) {
            int idx = stockDates.size() - 1 - indexOffset;
            if (idx < 0) {
                return;
            }
            List<String> list = listlist.get(idx);
            if (list == null) {
                return;
            }
            volumeExcludes.addAll(list);
        }
    }

    private List<SimulateStock> confidenceBuyHoldSell(SimulateInvestConfig simConfig, List<String> stockDates,
            Map<String, List<List<Double>>> categoryValueMap, Adviser adviser, List<String> excludeList,
            String aParameter, List<SimulateStock> mystocks, List<SimulateStock> sells, List<SimulateStock> buys, List<SimulateStock> holdIncrease,
            Mydate mydate) {
        List<SimulateStock> hold;
        if (simConfig.getConfidenceholdincrease() == null || simConfig.getConfidenceholdincrease()) {
            hold = holdIncrease;
        } else {
            hold = new ArrayList<>();
        }

        List<String> anExcludeList = new ArrayList<>(excludeList);
        List<String> ids1 = sells.stream().map(SimulateStock::getId).collect(Collectors.toList());
        List<String> ids2 = hold.stream().map(SimulateStock::getId).collect(Collectors.toList());
        anExcludeList.addAll(ids1);
        anExcludeList.addAll(ids2);
        Set<String> anExcludeSet = new LinkedHashSet<>(anExcludeList);
        // full list
        List<String> myincl = adviser.getIncs(aParameter, simConfig.getStocks(), getValueIndexOffset(mydate, simConfig), stockDates, anExcludeList);
        Set<String> myincs = new LinkedHashSet<>(myincl);
        //myincs = new ArrayList<>(myincs);
        myincs.removeAll(anExcludeSet);
        //List<IncDecItem> myincs = ds.getIncs(valueList);
        //List<ValueList> valueList = ds.getValueList(categoryValueMap, indexOffset);

        // full list, except if null value
        //int delay = simConfig.getDelay();
        List<SimulateStock> buysTmp = getBuyList(categoryValueMap, myincs, simConfig.getStocks());
        myincs = null;
        //buysTmp = filter(buysTmp, sells);
        List<SimulateStock> keeps = keep(mystocks, buysTmp);
        keeps.addAll(hold);
        buysTmp = filter(buysTmp, mystocks);
        //buysTmp = buysTmp.subList(0, Math.min(buysTmp.size(), simConfig.getStocks() - mystocks.size()));
        buys.clear();
        buys.addAll(buysTmp);

        List<SimulateStock> newsells = filter(mystocks, keeps);

        //mystocks.removeAll(newsells);

        myAddAll(sells, newsells);

        mystocks = keeps;
        return mystocks;
    }

    private List<SimulateStock> noConfidenceHoldSell(List<SimulateStock> mystocks, List<SimulateStock> holdIncrease, List<SimulateStock> sells, SimulateInvestConfig simConfig) {
        List<SimulateStock> keeps;
        if (simConfig.getNoconfidenceholdincrease() == null || simConfig.getNoconfidenceholdincrease()) {
            keeps = holdIncrease;
        } else {
            keeps = new ArrayList<>();
        }

        List<SimulateStock> newsells = filter(mystocks, keeps);

        //mystocks.removeAll(newsells);

        myAddAll(sells, newsells);

        mystocks = keeps;
        return mystocks;
    }

    private SimulateInvestConfig getSimConfig(IclijConfig config) {
        if (config.getConfigValueMap().get(IclijConfigConstants.SIMULATEINVESTDELAY) == null || (int) config.getConfigValueMap().get(IclijConfigConstants.SIMULATEINVESTDELAY) == 0) {
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

    private List<SimulateFilter> get(String json) {
        SimulateFilter[] array = JsonUtil.convert(json, SimulateFilter[].class);
        List<SimulateFilter> list = null;
        if (array != null) {
            list = Arrays.asList(array);
        }
        return list;
    }
    
    private AutoSimulateInvestConfig getAutoSimConfig(IclijConfig config) {
        if (config.getConfigValueMap().get(IclijConfigConstants.AUTOSIMULATEINVESTINTERVAL) == null || (int) config.getConfigValueMap().get(IclijConfigConstants.AUTOSIMULATEINVESTINTERVAL) == 0) {
            return null;
        }
        AutoSimulateInvestConfig simConfig = new AutoSimulateInvestConfig();
        simConfig.setInterval(config.getAutoSimulateInvestInterval());
        simConfig.setIntervalwhole(config.getAutoSimulateInvestIntervalwhole());
        simConfig.setImproveFilters(config.getAutoSimulateInvestImproveFilters());
        simConfig.setPeriod(config.getAutoSimulateInvestPeriod());
        simConfig.setLastcount(config.getAutoSimulateInvestLastCount());
        simConfig.setDellimit(config.getAutoSimulateInvestDelLimit());
        simConfig.setScorelimit(config.getAutoSimulateInvestScoreLimit());
        simConfig.setAutoscorelimit(config.getAutoSimulateInvestAutoScoreLimit());
        simConfig.setKeepAdviser(config.getAutoSimulateInvestKeepAdviser());
        simConfig.setKeepAdviserLimit(config.getAutoSimulateInvestKeepAdviserLimit());
        simConfig.setVote(config.getAutoSimulateInvestVote());
        simConfig.setFuturecount(config.getAutoSimulateInvestFutureCount());
        simConfig.setFuturetime(config.getAutoSimulateInvestFutureTime());
        Map<String, Double> map = JsonUtil.convert(config.getAutoSimulateInvestVolumelimits(), Map.class);
        simConfig.setVolumelimits(map);
        SimulateFilter[] array = JsonUtil.convert(config.getAutoSimulateInvestFilters(), SimulateFilter[].class);
        List<SimulateFilter> list = null;
        if (array != null) {
            list = Arrays.asList(array);
        }
        simConfig.setFilters(list);
        try {
            simConfig.setEnddate(config.getAutoSimulateInvestEnddate());
        } catch (Exception e) {

        }
        try {
            simConfig.setStartdate(config.getAutoSimulateInvestStartdate());
        } catch (Exception e) {

        }
        return simConfig;
    }

    private String emptyNull(Object object) {
        if (object == null) {
            return "";
        } else {
            return "" + object;
        }
    }

    private String emptyNull(Object object, String defaults) {
        if (object == null) {
            return defaults;
        } else {
            return "" + object;
        }
    }
    private List<SimulateStock> filter(List<SimulateStock> stocks, List<SimulateStock> others) {
        List<String> ids = others.stream().map(SimulateStock::getId).collect(Collectors.toList());        
        return stocks.stream().filter(e -> !ids.contains(e.getId())).collect(Collectors.toList());
    }

    private List<SimulateStock> keep(List<SimulateStock> stocks, List<SimulateStock> others) {
        List<String> ids = others.stream().map(SimulateStock::getId).collect(Collectors.toList());        
        return stocks.stream().filter(e -> ids.contains(e.getId())).collect(Collectors.toList());
    }

    private Capital getSum(List<SimulateStock> mystocks) {
        Capital sum = new Capital();
        for (SimulateStock astock : mystocks) {
            double price = astock.getPrice();
            if (price == 0.0) {
                // TODO maybe run more update, after each doBuySell
                price = astock.getBuyprice();
            }
            sum.amount += astock.getCount() * price;
        }
        return sum;
    }

    @Override
    public ComponentData improve(MarketActionData action, ComponentData componentparam, Market market, ProfitData profitdata,
            Memories positions, Boolean buy, String subcomponent, Parameters parameters, boolean wantThree,
            List<MLMetricsItem> mlTests, Fitness fitness, boolean save) {
        return null;
    }

    @Override
    public MLConfigs getOverrideMLConfig(ComponentData componentdata) {
        return null;
    }

    @Override
    public void calculateIncDec(ComponentData param, ProfitData profitdata, Memories positions, Boolean above,
            List<MLMetricsItem> mlTests, Parameters parameters) {
    }

    @Override
    public List<MemoryItem> calculateMemory(ComponentData param, Parameters parameters) throws Exception {
        return null;
    }

    @Override
    public String getPipeline() {
        return PipelineConstants.SIMULATEINVEST;
    }

    @Override
    protected List<String> getConfList() {
        return null;
    }

    @Override
    public String getThreshold() {
        return null;
    }

    @Override
    public String getFuturedays() {
        return null;
    }

    public Object[] calculateAccuracy(ComponentData componentparam) throws Exception {
        return new Object[] { componentparam.getScoreMap().get(SimConstants.SCORE) };
    }

    private double increase(List<SimulateStock> mystocks, int indexOffset, Map<String, List<List<Double>>> categoryValueMap, int prevIndexOffset, int endIndexOffset) {
        List<Double> incs = new ArrayList<>();
        for (SimulateStock item : mystocks) {
            String id = item.getId();
            List<List<Double>> resultList = categoryValueMap.get(id);
            if (resultList == null || resultList.isEmpty()) {
                continue;
            }
            List<Double> mainList = resultList.get(0);
            //ValidateUtil.validateSizes(mainList, stockDates);
            if (mainList != null) {
                if (indexOffset < endIndexOffset) {
                    continue;
                }
                Double valNow = mainList.get(mainList.size() - 1 - indexOffset);
                if (prevIndexOffset < endIndexOffset) {
                    continue;
                }
                Double valWas = mainList.get(mainList.size() - 1 - prevIndexOffset);
                if (valWas != null && valNow != null) {
                    double inc = valNow / valWas;
                    incs.add(inc);
                }
            } else {
                // put back if unknown
                //mystocks.add(item);
            }
        }
        if (incs.isEmpty()) {
            return 1.0;
        }
        OptionalDouble average = incs
                .stream()
                .mapToDouble(a -> a)
                .average();
        return average.getAsDouble();
    }

    private double getReliability(List<SimulateStock> mystocks, Pair<Integer, Integer>[] hits, int findTimes, int up) {
        Pair<Integer, Integer> pair = new ImmutablePair(up, mystocks.size());
        for (int j = findTimes - 1; j > 0; j--) {
            hits[j] = hits[j - 1];
        }
        hits[0] = pair;
        double count = 0;
        int total = 0;
        for (Pair<Integer, Integer> aPair : hits) {
            if (aPair == null) {
                continue;
            }
            count += aPair.getLeft();
            total += aPair.getRight();
        }
        double reliability = 1;
        if (total > 0) {
            reliability = count / total;
        }

        return reliability;
    }

    private void stoploss(List<SimulateStock> mystocks, List<String> stockDates, int indexOffset, Map<String, List<List<Double>>> categoryValueMap, int prevIndexOffset,
            List<SimulateStock> sells, double stoploss, String stop, BiMap<String, LocalDate> stockDatesBiMap, int endIndexOffset) {
        List<SimulateStock> newSells = new ArrayList<>();
        for (SimulateStock item : mystocks) {
            String id = item.getId();
            List<List<Double>> resultList = categoryValueMap.get(id);
            if (resultList == null || resultList.isEmpty()) {
                continue;
            }
            List<Double> mainList = resultList.get(0);
            //ValidateUtil.validateSizes(mainList, stockDates);
            if (mainList != null) {
                String dateNowStr = stockDates.get(stockDates.size() - 1 - indexOffset);
                LocalDate dateNow = convertDate(stockDatesBiMap, dateNowStr);
                if (!dateNow.isAfter(item.getBuydate())) {
                    continue;
                }
                Double valNow = mainList.get(mainList.size() - 1 - indexOffset);
                if (prevIndexOffset < endIndexOffset) {
                    int jj = 0;
                    continue;
                }
                Double valWas = mainList.get(mainList.size() - 1 - prevIndexOffset);
                if (valWas != null && valNow != null && valNow != 0 && valWas != 0 && valNow / valWas < stoploss) {
                    log.debug("Id etc {} {} {} {} {} {} {}", stop, id, valWas, valNow, dateNowStr, prevIndexOffset, indexOffset);
                    item.setStatus(stop);
                    newSells.add(item);
                }
            }
        }
        //mystocks.removeAll(newSells);
        myAddAll(sells, newSells);
    }

    private LocalDate convertDate(BiMap<String, LocalDate> stockDatesBiMap, String dateStr) {
        LocalDate date = stockDatesBiMap.get(dateStr);
        if (date != null) {
            return date;
        }
        try {
            date = TimeUtil.convertDate(dateStr);
        } catch (ParseException e) {
            log.error(Constants.EXCEPTION, e);
        }
        return date;
    }
    
    private void buy(List<String> stockDates, Map<String, List<List<Double>>> categoryValueMap, Capital capital, int buytop, List<SimulateStock> mystocks, List<SimulateStock> newbuys, int indexOffset, BiMap<String, LocalDate> stockDatesBiMap) {
        int buys = buytop - mystocks.size();
        buys = Math.min(buys, newbuys.size());

        double totalamount = 0;
        for (int i = 0; i < buys; i++) {
            SimulateStock astock = newbuys.get(i);

            String id = astock.getId();

            List<List<Double>> resultList = categoryValueMap.get(id);
            if (resultList == null || resultList.isEmpty()) {
                continue;
            }
            List<Double> mainList = resultList.get(0);
            //ValidateUtil.validateSizes(mainList, stockDates);
            if (mainList != null) {
                Double valNow = mainList.get(mainList.size() - 1 - indexOffset);
                if (valNow != null) {

                    double amount = 0;
                    amount = capital.amount / buys;
                    astock.setBuyprice(valNow);
                    astock.setCount(amount / astock.getBuyprice());
                    String dateNow = stockDates.get(stockDates.size() - 1 - indexOffset);
                    LocalDate date = convertDate(stockDatesBiMap, dateNow);
                    astock.setBuydate(date);
                    mystocks.add(astock);
                    totalamount += amount;
                } else {
                    log.debug("Not found {}", id);
                }
            }
        }
        capital.amount -= totalamount;
    }

    private int update(Map<String, List<List<Double>>> categoryValueMap, List<SimulateStock> mystocks, int indexOffset,
            List<SimulateStock> noConfKeep, int prevIndexOffset, int endIndexOffset) {
        int up = 0;
        for (SimulateStock item : mystocks) {
            String id = item.getId();
            List<List<Double>> resultList = categoryValueMap.get(id);
            if (resultList == null || resultList.isEmpty()) {
                continue;
            }
            List<Double> mainList = resultList.get(0);
            //ValidateUtil.validateSizes(mainList, stockDates);
            if (mainList != null) {
                if (indexOffset < endIndexOffset) {
                    int jj = 0;
                }
                Double valNow = mainList.get(mainList.size() - 1 - indexOffset);
                if (valNow != null) {
                    item.setPrice(valNow);
                } else {
                    if (item.getPrice() == 0.0) {
                        item.setPrice(item.getBuyprice());
                    }
                }
                if (prevIndexOffset < endIndexOffset) {
                    continue;
                }
                Double valWas = mainList.get(mainList.size() - 1 - prevIndexOffset);
                if (valNow != null && valWas != null) {
                    if (valNow > valWas) {
                        up++;
                        noConfKeep.add(item);
                    }
                }
            }
        }
        return up;
    }

    private void sell(List<String> stockDates, Map<String, List<List<Double>>> categoryValueMap, Capital capital,
            List<SimulateStock> sells, List<SimulateStock> stockhistory, int indexOffset, List<SimulateStock> mystocks, BiMap<String, LocalDate> stockDatesBiMap) {
        for (SimulateStock item : sells) {
            String id = item.getId();
            List<List<Double>> resultList = categoryValueMap.get(id);
            if (resultList == null || resultList.isEmpty()) {
                continue;
            }
            List<Double> mainList = resultList.get(0);
            //ValidateUtil.validateSizes(mainList, stockDates);
            if (mainList != null) {
                Double valNow = mainList.get(mainList.size() - 1 - indexOffset);
                if (valNow == null) {
                    // we have to sell if the stock should disappear from the list
                    if (item.getPrice() > 0.0) {
                        valNow = item.getPrice();
                    }
                }
                if (valNow != null) {
                    item.setSellprice(valNow);
                    String dateNow = stockDates.get(stockDates.size() - 1 - indexOffset);
                    LocalDate date = convertDate(stockDatesBiMap, dateNow);
                    item.setSelldate(date);
                    stockhistory.add(item);
                    capital.amount += item.getCount() * item.getSellprice();
                    mystocks.remove(item);
                } else {
                    log.debug("Not found {}", id);
                    // put back if unknown
                    //mystocks.add(item);
                }
            } else {
                log.debug("Not found {}", id);
                // put back if unknown
                //mystocks.add(item);
            }
        }
    }

    private List<SimulateStock> getSellList(List<SimulateStock> mystocks, List<SimulateStock> newbuys) {
        List<String> myincids = newbuys.stream().map(SimulateStock::getId).collect(Collectors.toList());            
        return mystocks.stream().filter(e -> !myincids.contains(e.getId())).collect(Collectors.toList());
    }

    private List<SimulateStock> getBuyList(Map<String, List<List<Double>>> categoryValueMap, Set<String> myincs,
            Integer count) {
        List<SimulateStock> newbuys = new ArrayList<>();
        for (String id : myincs) {
            List<List<Double>> resultList = categoryValueMap.get(id);
            if (resultList == null || resultList.isEmpty()) {
                continue;
            }
            List<Double> mainList = resultList.get(0);
            //ValidateUtil.validateSizes(mainList, stockDates);
            if (mainList != null) {
                SimulateStock astock = new SimulateStock();
                astock.setId(id);
                //astock.price = -1;
                newbuys.add(astock);
                count--;
                if (count == 0) {
                    break;
                }
            }
        }
        return newbuys;
    }

    private List<IncDecItem> getAllIncDecs(Market market, LocalDate investStart, LocalDate investEnd) {
        try {
            return IclijDbDao.getAllIncDecs(market.getConfig().getMarket(), investStart, investEnd, null);
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        return new ArrayList<>();
    }

    private List<MemoryItem> getAllMemories(Market market, LocalDate investStart, LocalDate investEnd) {
        try {
            return IclijDbDao.getAllMemories(market.getConfig().getMarket(), IclijConstants.IMPROVEABOVEBELOW, PipelineConstants.ABOVEBELOW, null, null, investStart, investEnd);
            // also filter on params
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }        
        return new ArrayList<>();
    }

    private List<MetaItem> getAllMetas(ComponentData param) {
        return param.getService().getMetas();
    }

    private void myAddAll(List<SimulateStock> mainList, List<SimulateStock> list) {
        for (SimulateStock stock : list) {
            if (!mainList.contains(stock)) {
                mainList.add(stock);
            }
        }
    }
    
    private int getValueIndexOffset(Mydate mydate, SimulateInvestConfig simConfig) {
        int extradelay = 0;
        if (simConfig.getExtradelay() != null) {
            extradelay = simConfig.getExtradelay();
        }
        return mydate.indexOffset + extradelay;
    }
    
    private int getBSIndexOffset(Mydate mydate, SimulateInvestConfig simConfig) {
        int extradelay = 0;
        if (simConfig.getExtradelay() != null) {
            extradelay = simConfig.getExtradelay();
        }
        return mydate.indexOffset - extradelay - simConfig.getDelay();
    }
    
    private int getBSValueIndexOffset(Mydate mydate, SimulateInvestConfig simConfig) {
        int extradelay = 0;
        if (simConfig.getExtradelay() != null) {
            extradelay = simConfig.getExtradelay();
        }
        return mydate.indexOffset - extradelay;
    }
    
    class OneRun {
        Adviser adviser;
        Capital capital;
        int beatavg;
        int runs;
        List<SimulateStock> mystocks;
        double resultavg;
        List<SimulateStock> savedStocks;
        boolean saveLastInvest;
        Integer[] trendInc = new Integer[] { 0 };
        Integer[] trendDec = new Integer[] { 0 };
        Pair<Integer, Integer>[] hits;
        Double autoscore;
        List<SimulateStock> buys;
        Map<Integer, Map<String, List<SimulateStock>>> eventMap = new HashMap<>();
        String lastbuysell;
    }
    
    class Results {
        List<SimulateStock> stockhistory = new ArrayList<>();
        List<StockHistory> history = new ArrayList<>();
        List<String> sumHistory = new ArrayList<>();
        List<String> plotDates = new ArrayList<>();
        List<Double> plotCapital = new ArrayList<>();
        List<Double> plotDefault = new ArrayList<>();        
    }
    
    class Data {
        int lastidx;
        int firstidx;
        Map<String, List<List<Double>>> categoryValueMap;
        Map<String, List<List<Double>>> categoryValueFillMap;
        Map<String, List<List<Double>>> filteredCategoryValueMap;
        Map<String, List<List<Double>>> filteredCategoryValueFillMap;
        List<String> stockDates;
        Map<Integer, List<String>> volumeExcludeMap;
        Map<Integer, List<String>> volumeExcludeFillMap;
        List<String> configExcludeList;
        Set<String> abnormExcludes;
        Map<String, List<List<Object>>> volumeMap;
        Map<Integer, Trend> trendMap;
        Map<Integer, Trend> trendFillMap;
        Map<Integer, String> trendStrMap;
        Map<Integer, String> trendStrFillMap;
        public Map<String, List<List<Double>>> getCatValMap(boolean interpolate) {
            if (interpolate) {
                return categoryValueFillMap;
            } else {
                return categoryValueMap;
            }                
        }
        public Map<String, List<List<Double>>> getFilterCatValMap(boolean interpolate) {
            if (interpolate) {
                return filteredCategoryValueFillMap;
            } else {
                return filteredCategoryValueMap;
            }                
        }
        public Map<Integer, List<String>> getVolumeExcludeMap(boolean interpolate) {
            if (interpolate) {
                return volumeExcludeFillMap;
            } else {
                return volumeExcludeMap;
            }                
        }
        public Map<Integer, Trend> getTrendMap(boolean interpolate) {
            if (interpolate) {
                return trendFillMap;
            } else {
                return trendMap;
            }                
        }
        public Map<Integer, String> getTrendStrMap(boolean interpolate) {
            if (interpolate) {
                return trendStrFillMap;
            } else {
                return trendStrMap;
            }                
        }
    }
    
    class Mydate {
        LocalDate date;
        int indexOffset;
        int prevIndexOffset;
        
    }
    // loop
    // get u/n/d trend
    // get exclusions, defaults + current low volume traded
    // deprecated: get stocks increase
    // updated stock prices, return eventual hold list
    // check interval stoploss, move stocks to sell list
    // calculate reliability
    // calculate confidence
    // if confidence
    //// get recommendations
    //// remove exclusions
    //// eventually do hold
    //// all not recommended nor on hold will be sold
    // else
    //// all not on hold will be sold
    // sell
    // buy
    // update again
    // calculate resultavg
    // add to history lists
    // loop
    //// check 1 day stoploss, move stocks to sell list
    //// sell
    // lastbuy, get a new buy/sell recommendation

    /*
     */

    // sim will
    // lucky shots / short runs
    // config commons / invariants
    // one company lucky percentage value / rerun without
    // some lucky few days
    // too few good results
    /*
     */
    
    // loop
    // update values with indexoffset - extra
    // get advise based on indexoffset info (TODO extra)
    // get istoploss sells with indexoffset
    // get buy list with indexoffset - delay - extra
    // stoploss is based on indexoffset - extra

    // algo 0
    // loop
    // get recommend for buy sell on date1
    // sell buy on date1
    // date1 = date1 + 1w
    // calculate capital after 1w, new date1
    // endloop
    // #calculate capital after 1w

    // # remember stoploss

    // algo 1
    // loop
    // get findprofit recommendations from last week on date1
    // ignore sell buy if not reliable lately
    // sell buy
    // date += 1w
    // calc new capital
    // endloop

    // algo 2
    // incr date by 1w
    // loop
    // date1
    // stocklist
    // check pr date1 top 5 for last 1m 1w etc
    // if stocklist in top 5, keep, else sell
    // buy stocklist in top 5
    // date1 += 1w
    // calc new capital
    // endloop
    // #get stocklist value

    // use mach highest mom

    // improve with ds
}
