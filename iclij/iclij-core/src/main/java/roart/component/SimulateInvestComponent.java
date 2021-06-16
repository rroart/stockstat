package roart.component;

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

import roart.action.MarketAction;
import roart.common.cache.MyCache;
import roart.common.config.CacheConstants;
import roart.common.config.ConfigConstants;
import roart.common.constants.Constants;
import roart.common.constants.EvolveConstants;
import roart.common.constants.ServiceConstants;
import roart.common.model.MetaItem;
import roart.common.pipeline.PipelineConstants;
import roart.common.util.JsonUtil;
import roart.common.util.MapUtil;
import roart.common.util.MathUtil;
import roart.common.util.MetaUtil;
import roart.common.util.TimeUtil;
import roart.common.util.ValidateUtil;
import roart.component.adviser.Adviser;
import roart.component.adviser.AdviserFactory;
import roart.component.model.ComponentData;
import roart.component.model.SimulateInvestData;
import roart.constants.IclijConstants;
import roart.constants.SimConstants;
import roart.db.IclijDbDao;
import roart.evolution.chromosome.AbstractChromosome;
import roart.iclij.evolution.chromosome.impl.ConfigMapChromosome2;
import roart.iclij.evolution.chromosome.winner.IclijConfigMapChromosomeWinner;
import roart.evolution.config.EvolutionConfig;
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
import roart.iclij.evolution.fitness.impl.FitnessIclijConfigMap;
import roart.iclij.filter.Memories;
import roart.iclij.model.MLMetricsItem;
import roart.iclij.model.IncDecItem;
import roart.iclij.model.MemoryItem;
import roart.iclij.model.Parameters;
import roart.iclij.model.SimDataItem;
import roart.iclij.model.Trend;
import roart.iclij.service.IclijServiceResult;
import roart.iclij.verifyprofit.TrendUtil;
import roart.service.model.ProfitData;
import roart.simulate.model.Capital;
import roart.simulate.model.SimulateStock;
import roart.simulate.model.StockHistory;
import roart.simulate.util.SimUtil;

public class SimulateInvestComponent extends ComponentML {

    private static final boolean VERIFYCACHE = false;
    
    @Override
    public void enable(Map<String, Object> valueMap) {
    }

    @Override
    public void disable(Map<String, Object> valueMap) {
    }

    @Override
    public ComponentData handle(MarketAction action, Market market, ComponentData param, ProfitData profitdata,
            Memories positions, boolean evolve, Map<String, Object> aMap, String subcomponent, String mlmarket,
            Parameters parameters) {
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
        
        AutoSimulateInvestConfig autoSimConfig = getAutoSimConfig(config);
        SimulateInvestConfig simConfig = getSimConfig(config);
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
            simConfig.setVolumelimits(autoSimConfig.getVolumelimits());
        }
        int extradelay = 0;
        if (simConfig.getExtradelay() != null) {
            extradelay = simConfig.getExtradelay();
        }
        // coming from improvesim
        List<SimulateFilter> filter = simConfig.getFilters();
        simConfig.setFilters(null);
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

            handle2(action, market, componentData, profitdata, positions, evolve, aMap, subcomponent, mlmarket, parameters);
            return componentData;
        }
        data.categoryValueFillMap = param.getFillCategoryValueMap();
        data.categoryValueMap = param.getCategoryValueMap();
        data.volumeMap = param.getVolumeMap();
        BiMap<String, LocalDate> stockDatesBiMap = getStockDatesBiMap(config, data.stockDates);

        //ComponentData componentData = component.improve2(action, param, market, profitdata, null, buy, subcomponent, parameters, mlTests);
        String mldate = getMlDate(market, simConfig, data, autoSimConfig);
        LocalDate investStart = null;
        try {
            investStart = TimeUtil.convertDate(mldate);
        } catch (ParseException e1) {
            log.error(Constants.EXCEPTION, e1);
        }
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

        setExclusions(market, data, simConfig);

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

        // TODO investend and other reset of more params
        Parameters realParameters = parameters;
        if (realParameters == null || realParameters.getThreshold() == 1.0) {
            String aParameter = JsonUtil.convert(realParameters);

            investEnd = getAdjustedInvestEnd(extradelay, data, investEnd);
            LocalDate lastInvestEnd = getAdjustedLastInvestEnd(data, investEnd, simConfig.getDelay());

            setDataVolumeAndTrend(market, param, simConfig, data, investStart, investEnd, lastInvestEnd, evolving);

            long time0 = System.currentTimeMillis();
            Map<String, Object> resultMap = new HashMap<>();
            for (int offset = 0; offset < end; offset++) {
                Integer origAdviserId = (Integer) param.getInput().getValuemap().get(IclijConfigConstants.SIMULATEINVESTADVISER);
                Mydate mydate = new Mydate();
                getAdjustedDate(data, investStart, offset, mydate);
                Map<Pair<LocalDate, LocalDate>, List<SimulateInvestConfig>> simConfigs;
                if (autoSimConfig == null) {
                    simConfigs = new HashMap<>();
                    //simConfigs = new ArrayList<>();
                    //simConfigs.add(getSimConfig(config));
                } else {
                    simConfigs = getSimConfigs(market.getConfig().getMarket(), autoSimConfig, filter);
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
                SimulateInvestConfig sell = null;
                if (autoSimConfig == null) {
                    int adviserId = simConfig.getAdviser();
                    currentOneRun.adviser = new AdviserFactory().get(adviserId, market, investStart, investEnd, param, simConfig);
                    currentOneRun.adviser.getValueMap(data.stockDates, data.firstidx, data.lastidx, data.getCatValMap(simConfig.getInterpolate()));
                } else {
                    selladviser = new AdviserFactory().get(-1, market, investStart, investEnd, param, simConfig);
                    sell = JsonUtil.copy(simConfig);
                    sell.setConfidence(true);
                    sell.setConfidenceValue(2.0);
                    sell.setConfidenceFindTimes(0);
                }
                Results mainResult = new Results();

                
                while (mydate.date != null && investEnd != null && !mydate.date.isAfter(investEnd)) {
                    boolean lastInvest = offset == 0 && mydate.date.isAfter(lastInvestEnd);
                    mydate.date = TimeUtil.getForwardEqualAfter2(mydate.date, 0 /* findTime */, data.stockDates);
                    String datestring = TimeUtil.convertDate2(mydate.date);
                    mydate.indexOffset = data.stockDates.size() - 1 - TimeUtil.getIndexEqualAfter(data.stockDates, datestring);
                    if (currentSimConfig != null) {
                        doOneRun(param, currentSimConfig, extradelay, evolving, aParameter, offset, currentOneRun, mainResult,
                                data, lastInvest, mydate, autoSimConfig != null, stockDatesBiMap);
                    }
                    if (autoSimConfig != null) {
                        for (Triple<SimulateInvestConfig, OneRun, Results> aPair : simTriplets) {
                            SimulateInvestConfig aSimConfig = aPair.getLeft();
                            OneRun aOneRun = aPair.getMiddle();
                            Results aResult = aPair.getRight();

                            doOneRun(param, aSimConfig, extradelay, evolving, aParameter, offset, aOneRun, aResult,
                                    data, lastInvest, mydate, autoSimConfig != null, stockDatesBiMap);
                        }
                    }
                    if (autoSimConfig != null) {
                        for (Triple<SimulateInvestConfig, OneRun, Results> aTriple : simTriplets) {
                            SimulateInvestConfig aSimConfig = aTriple.getLeft();
                            OneRun aOneRun = aTriple.getMiddle();
                            Results aResult = aTriple.getRight();
                            // best or best last
                            int numlast = autoSimConfig.getLastcount();
                            double score;
                            if (numlast == 0 || aOneRun.runs == 1) {
                                Capital sum = getSum(aOneRun.mystocks);
                                sum.amount += aOneRun.capital.amount;
                                score = (sum.amount - 1) / aOneRun.runs;
                            } else {
                                int firstidx = aResult.plotCapital.size() - 1 - numlast;
                                if (firstidx < 0) {
                                    firstidx = 0;
                                }
                                score = 0;
                                if (aResult.plotCapital.size() > 0) {
                                    score = aResult.plotCapital.get(aResult.plotCapital.size() - 1) - aResult.plotCapital.get(firstidx);
                                    score = score / (aResult.plotCapital.size() - firstidx);
                                } else {
                                    int jj = 0;
                                }
                            }
                            aOneRun.autoscore = score;
                            if (score < 0) {
                                int jj = 0;
                            }
                        }                        
                        // no. hits etc may be reset if changed
                        // winnerAdviser
                        Collections.sort(simTriplets, (o1, o2) -> Double.compare(o2.getMiddle().autoscore, o1.getMiddle().autoscore));
                        // overwrite/merge currentOnerun etc
                        // and get new date range
                        // and remove old or bad
                        Set<Pair<LocalDate, LocalDate>> keys = new HashSet<>();
                        simsConfigs = getSimConfigs(simConfigs, mydate, keys, market);
                        simConfigs.keySet().removeAll(keys);
                        List<Triple<SimulateInvestConfig, OneRun, Results>> newSimTriplets = getTriples(market, param, data,
                                investStart, investEnd, mydate, simsConfigs);
                        if (newSimTriplets.size() > 0) {
                            List<Double> alist = simTriplets.stream().map(o -> (o.getMiddle().capital.amount + getSum(o.getMiddle().mystocks).amount)).collect(Collectors.toList());
                            log.info("alist {}", alist);
                            double autolimit = autoSimConfig.getDellimit();
                            simTriplets = simTriplets.stream().filter(o -> (o.getMiddle().capital.amount + getSum(o.getMiddle().mystocks).amount) > autolimit).collect(Collectors.toList());
                        }
                        simTriplets.addAll(newSimTriplets);
                    }
                    if (autoSimConfig != null && !simTriplets.isEmpty()) {
                        List<Double> alist = simTriplets.stream().map(o -> (o.getMiddle().autoscore)).collect(Collectors.toList());
                        log.info("alist {}", alist);
                        OneRun oneRun = simTriplets.get(0).getMiddle();
                        if (oneRun.autoscore != null && oneRun.autoscore > autoSimConfig.getAutoscorelimit()) {
                            currentOneRun.adviser = oneRun.adviser;
                            currentOneRun.hits = SerializationUtils.clone(oneRun.hits);
                            currentOneRun.trendDec = SerializationUtils.clone(oneRun.trendDec);
                            currentOneRun.trendInc = SerializationUtils.clone(oneRun.trendInc);
                            //oneRun.
                            currentSimConfig = simTriplets.get(0).getLeft();
                        } else {
                            currentOneRun.adviser = selladviser;
                            currentOneRun.hits = SerializationUtils.clone(oneRun.hits);
                            currentOneRun.trendDec = SerializationUtils.clone(oneRun.trendDec);
                            currentOneRun.trendInc = SerializationUtils.clone(oneRun.trendInc);
                            currentSimConfig = sell;
                        }
                    }
                    mydate.prevIndexOffset = mydate.indexOffset;
                    //int interval = autoSimConfig != null ? autoSimConfig.getInterval() : simConfig.getInterval();
                    if (mydate.indexOffset - simConfig.getInterval() < 0) {
                        break;
                    }
                    String adatestring = data.stockDates.get(data.stockDates.size() - 1 - (mydate.indexOffset - simConfig.getInterval()));
                    try {
                        mydate.date = TimeUtil.convertDate(adatestring);
                    } catch (ParseException e) {
                        log.error(Constants.EXCEPTION, e);
                    }
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
                List<Triple<SimulateInvestConfig, OneRun, Results>> endSimTriplets = new ArrayList<>();
                endSimTriplets.add(new ImmutableTriple(currentSimConfig, currentOneRun, mainResult));
                for (Triple<SimulateInvestConfig, OneRun, Results> aPair : endSimTriplets) {
                    SimulateInvestConfig aSimConfig = aPair.getLeft();
                    OneRun aOneRun = aPair.getMiddle();
                    Results aResult = aPair.getRight();
                    boolean lastInvest = offset == 0 && mydate.date != null && lastInvestEnd != null && mydate.date.isAfter(lastInvestEnd);
                    if (aOneRun.saveLastInvest) {
                        aOneRun.mystocks = aOneRun.savedStocks;
                    }
                    if (mydate.prevIndexOffset - extradelay - simConfig.getDelay() >= 0) {
                        update(data.getCatValMap(simConfig.getInterpolate()), aOneRun.mystocks, mydate.indexOffset - extradelay - simConfig.getDelay(), new ArrayList<>(), mydate.prevIndexOffset - extradelay - simConfig.getDelay());
                    }
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
                    scores.add(score);
                    if (score.isNaN()) {
                        int jj = 0;
                    }

                    if (offset == 0) {
                        Map<String, Object> map = new HashMap<>();
                        if (!evolving) {
                            for (SimulateStock stock : aOneRun.mystocks) {
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
                            List<Pair<String, Double>> tradeStocks = SimUtil.getTradeStocks(map);
                            map.put(SimConstants.TRADESTOCKS, tradeStocks);
                            param.getUpdateMap().putAll(map);
                            param.getUpdateMap().putIfAbsent("lastbuysell", "Not buying or selling today");
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

                        Map<String, Object> map = new HashMap<>();
                        map.put(SimConstants.HISTORY, aResult.history);
                        map.put(SimConstants.STOCKHISTORY, aResult.stockhistory);
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
                }
            }
            if (evolving) {
                componentData.setResultMap(resultMap);
            }
            log.info("time0 {}", System.currentTimeMillis() - time0);
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

        handle2(action, market, componentData, profitdata, positions, evolve, aMap, subcomponent, mlmarket, parameters);
        return componentData;
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
        onerun.adviser.getValueMap(data.stockDates, data.firstidx, data.lastidx, data.getCatValMap(simConfig.getInterpolate()));
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
        return JsonUtil.copy(simulate);
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

        data.filteredCategoryValueMap = new HashMap<>(data.getCatValMap(false));
        data.filteredCategoryValueMap.keySet().removeAll(configExcludeSet);
        data.filteredCategoryValueFillMap = new HashMap<>(data.getCatValMap(true));
        data.filteredCategoryValueFillMap.keySet().removeAll(configExcludeSet);
    }

    private Map<Pair<LocalDate, LocalDate>, List<SimulateInvestConfig>> getSimConfigs(String market, AutoSimulateInvestConfig autoSimConf, List<SimulateFilter> filter) {
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
            IclijConfig instance = IclijXMLConfig.getConfigInstance();
            try {
                list = IclijXMLConfig.getSimulate(instance);
            } catch (Exception e) {
                log.error(Constants.EXCEPTION, e);
            }
            List<SimulateFilter> listoverride = autoSimConf.getFilters();
            if (listoverride != null) {
                for (int i = 0; i < listoverride.size(); i++) {
                    SimulateFilter afilter = list.get(0)[i];
                    SimulateFilter otherfilter = listoverride.get(i);
                    afilter.merge(otherfilter);
                }
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
                        Map map = JsonUtil.convert(configStr, Map.class);
                        IclijConfig dummy = new IclijConfig();
                        dummy.setConfigValueMap(map);
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
                                if (autoSimConfFilter.getCorrelation() > 0 && autoSimConfFilter.getCorrelation() > myFilter.getCorrelation()) {
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

    private void setDataVolumeAndTrend(Market market, ComponentData param, SimulateInvestConfig simConfig, Data data,
            LocalDate investStart, LocalDate investEnd, LocalDate lastInvestEnd, boolean evolving) {
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
        
        // vol lim w/ adviser?
        data.volumeExcludeMap = getVolumeExcludeMap(market, simConfig, data, investStart, investEnd, firstidx, lastidx, false);
        data.volumeExcludeFillMap = getVolumeExcludeMap(market, simConfig, data, investStart, investEnd, firstidx, lastidx, true);

        long time00 = System.currentTimeMillis();
        log.info("timeee0 {}", System.currentTimeMillis() - time00);
        
        data.trendMap = getTrendIncDec(market, param, data.stockDates, simConfig.getInterval(), firstidx, lastidx, simConfig, data, false);
        data.trendFillMap = getTrendIncDec(market, param, data.stockDates, simConfig.getInterval(), firstidx, lastidx, simConfig, data, true);
        if (evolving) {
            data.trendStrMap = getTrendIncDecStr(market, param, data.stockDates, simConfig.getInterval(), firstidx, lastidx, simConfig, data, false, data.trendMap);
            data.trendStrFillMap = getTrendIncDecStr(market, param, data.stockDates, simConfig.getInterval(), firstidx, lastidx, simConfig, data, true, data.trendFillMap);
        }
        Set<Integer> keys = data.trendMap.keySet();
        List<Integer> keyl = new ArrayList<>(keys);
        Collections.sort(keyl);
        log.info("keyl {}", keyl);
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
            log.info("timee0 {}", System.currentTimeMillis() - time00);
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
                int idx = data.stockDates.indexOf(aDate) - extradelay;
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
            //mldate = ((SimulateInvestActionData) action.getActionData()).getMlDate(market, stockDates);
            //mldate = ((ImproveSimulateInvestActionData) action.getActionData()).getMlDate(market, stockDates);
        } else {
            Short populate = market.getConfig().getPopulate();
            if (populate == null) {
                //mldate = ((SimulateInvestActionData) action.getActionData()).getMlDate(market, stockDates);                
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
            Mydate mydate, boolean auto, BiMap<String, LocalDate> stockDatesBiMap) {
        if (lastInvest) {
            // not with evolving?
            onerun.savedStocks = copy(onerun.mystocks);
            onerun.saveLastInvest = true;
        }
        //Trend trend = getTrendIncDec(market, param, stockDates, interval, filteredCategoryValueMap, trendInc, trendDec, indexOffset);
        Trend trend = getTrendIncDec(data.stockDates, onerun.trendInc, onerun.trendDec, mydate.indexOffset, data.getTrendMap(simConfig.getInterpolate()));
        // get recommendations

        List<String> myExcludes = getExclusions(simConfig, extradelay, data.stockDates, simConfig.getInterval(), data.getCatValMap(simConfig.getInterpolate()),
                data.volumeMap, data.configExcludeList, simConfig.getDelay(), mydate.indexOffset, data.getVolumeExcludeMap(simConfig.getInterpolate()));

        double myavg = increase(onerun.mystocks, mydate.indexOffset - extradelay, data.getCatValMap(simConfig.getInterpolate()), mydate.prevIndexOffset);

        List<SimulateStock> holdIncrease = new ArrayList<>();
        int up;
        if (mydate.indexOffset - extradelay >= 0) {
            up = update(data.getCatValMap(simConfig.getInterpolate()), onerun.mystocks, mydate.indexOffset - extradelay, holdIncrease, mydate.prevIndexOffset - extradelay);
        } else {
            up = onerun.mystocks.size();
        }
        
        List<SimulateStock> sells = new ArrayList<>();
        List<SimulateStock> buys = new ArrayList<>();

        if (simConfig.getIntervalStoploss()) {
            // TODO delay
            if (mydate.indexOffset - extradelay - simConfig.getDelay() >= 0) {
                stoploss(onerun.mystocks, data.stockDates, mydate.indexOffset - extradelay, data.getCatValMap(simConfig.getInterpolate()), mydate.prevIndexOffset - extradelay, sells, simConfig.getIntervalStoplossValue(), "ISTOP", stockDatesBiMap);
            }
        }

        double myreliability = getReliability(onerun.mystocks, onerun.hits, simConfig.getConfidenceFindTimes(), up);

        boolean confidence = !simConfig.getConfidence() || myreliability >= simConfig.getConfidenceValue();
        boolean confidence1 = !simConfig.getConfidencetrendincrease() || onerun.trendInc[0] >= simConfig.getConfidencetrendincreaseTimes();
        boolean noconfidence2 = simConfig.getNoconfidencetrenddecrease() && onerun.trendDec[0] >= simConfig.getNoconfidencetrenddecreaseTimes();
        boolean noconfidence = !confidence || !confidence1 || noconfidence2;
        if (!noconfidence) {
            int adelay = simConfig.getDelay();
            if (lastInvest) {
                adelay = 0;
            }
            if (mydate.indexOffset - extradelay - adelay >= 0) {
                onerun.mystocks = confidenceBuyHoldSell(simConfig, data.stockDates, data.getCatValMap(simConfig.getInterpolate()), onerun.adviser, myExcludes,
                        aParameter, onerun.mystocks, mydate.indexOffset, sells, buys, holdIncrease, extradelay, adelay);
            }
        } else {
            int adelay = simConfig.getDelay();
            if (lastInvest) {
                adelay = 0;
            }
            if (mydate.indexOffset - extradelay - adelay >= 0) {
                onerun.mystocks = noConfidenceHoldSell(onerun.mystocks, holdIncrease, sells, simConfig);
            }
        }

        if (!lastInvest) {
            if (mydate.indexOffset - extradelay - simConfig.getDelay() >= 0) {
                // TODO delay DELAY
                sell(data.stockDates, data.getCatValMap(simConfig.getInterpolate()), onerun.capital, sells, results.stockhistory, mydate.indexOffset - extradelay - simConfig.getDelay(), mydate.date, onerun.mystocks, stockDatesBiMap);

                // TODO delay DELAY
                buy(data.stockDates, data.getCatValMap(simConfig.getInterpolate()), onerun.capital, simConfig.getStocks(), onerun.mystocks, buys, mydate.date, mydate.indexOffset - extradelay - simConfig.getDelay(), stockDatesBiMap);

                List<String> myids = onerun.mystocks.stream().map(SimulateStock::getId).collect(Collectors.toList());            
                if (myids.size() != onerun.mystocks.size()) {
                    log.error("Sizes");
                }

                // to delay?
                update(data.getCatValMap(simConfig.getInterpolate()), onerun.mystocks, mydate.indexOffset - extradelay - simConfig.getDelay(), new ArrayList<>(), mydate.prevIndexOffset - extradelay - simConfig.getDelay());

                if (trend != null && trend.incAverage != 0) {
                    onerun.resultavg *= trend.incAverage;
                }

                // depends on delay DELAY
                Capital sum = getSum(onerun.mystocks);

                //boolean noconf = simConfig.getConfidence() && myreliability < simConfig.getConfidenceValue();                
                String hasNoConf = noconfidence ? "NOCONF" : "";
                String historydatestring = data.stockDates.get(data.stockDates.size() - 1 - (mydate.indexOffset - extradelay - simConfig.getDelay()));

                List<String> ids = onerun.mystocks.stream().map(SimulateStock::getId).collect(Collectors.toList());
                if (!evolving) {
                    if (offset == 0) {
                        String adv = auto ? " Adv" + simConfig.getAdviser() : "";
                        results.sumHistory.add(historydatestring + " " + onerun.capital.toString() + " " + sum.toString() + " " + new MathUtil().round(onerun.resultavg, 2) + " " + hasNoConf + " " + ids + " " + trend + adv);
                        results.plotDates.add(historydatestring);
                        results.plotDefault.add(onerun.resultavg);
                        results.plotCapital.add(sum.amount + onerun.capital.amount);
                    } else {
                        results.plotDates.add(historydatestring);                        
                    }
                } else {
                    results.plotDates.add(historydatestring);
                    results.plotCapital.add(sum.amount + onerun.capital.amount);
                    Integer adv = auto ? simConfig.getAdviser() : null;
                    Capital aCapital = new Capital();
                    aCapital.amount = onerun.capital.amount;
                    String trendStr = getTrendIncDecStr(data.stockDates, onerun.trendInc, onerun.trendDec, mydate.indexOffset, data.getTrendStrMap(simConfig.getInterpolate()));
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

            if (simConfig.getStoploss()) {
                for (int j = 0; j < simConfig.getInterval(); j++) {
                    sells = new ArrayList<>();
                    //System.out.println(interval + " " +  j);
                    if (mydate.indexOffset - j - 1 - extradelay < 0) {
                        break;
                    }
                    // TODO delay DELAY
                    stoploss(onerun.mystocks, data.stockDates, mydate.indexOffset - j - extradelay, data.getCatValMap(simConfig.getInterpolate()), mydate.indexOffset - j - 1 - extradelay, sells, simConfig.getStoplossValue(), "STOP", stockDatesBiMap);                       
                    sell(data.stockDates, data.getCatValMap(simConfig.getInterpolate()), onerun.capital, sells, results.stockhistory, mydate.indexOffset - j - extradelay, mydate.date, onerun.mystocks, stockDatesBiMap);
                }                    
            }
        } else {
            if (offset == 0) {
                List<String> ids = onerun.mystocks.stream().map(SimulateStock::getId).collect(Collectors.toList());
                List<String> buyids = buys.stream().map(SimulateStock::getId).collect(Collectors.toList());
                List<String> sellids = sells.stream().map(SimulateStock::getId).collect(Collectors.toList());

                if (!noconfidence) {
                    int buyCnt = simConfig.getStocks() - onerun.mystocks.size();
                    buyCnt = Math.min(buyCnt, buys.size());
                    ids.addAll(buyids.subList(0, buyCnt));
                    buyids = buyids.subList(0, buyCnt);
                } else {
                    buyids.clear();
                }
                ids.removeAll(sellids);
                param.getUpdateMap().put(SimConstants.LASTBUYSELL, "Buy: " + buyids + " Sell: " + sellids + " Stocks: " +ids);

            }
        }
    }

    private BiMap<String, LocalDate> getStockDatesBiMap(IclijConfig config, List<String> stockDates) {
        String key = CacheConstants.DATESMAP + config.getMarket(); // + config.getDate();
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

    private List<String> getExclusions(SimulateInvestConfig simConfig, int extradelay, List<String> stockDates,
            int interval, Map<String, List<List<Double>>> categoryValueMap, Map<String, List<List<Object>>> volumeMap,
            List<String> configExcludeList, int delay, int indexOffset, Map<Integer, List<String>> newVolumeMap) {
        List<String> myExcludes = new ArrayList<>();
        List<String> volumeExcludes = new ArrayList<>();
        /*
        getVolumeExcludes(simConfig, extradelay, stockDates, interval, categoryValueMap, volumeMap, delay,
                indexOffset, volumeExcludes);
                */
        long time0 = System.currentTimeMillis();
        getVolumeExcludes(simConfig, extradelay, stockDates, interval, categoryValueMap, volumeMap, delay,
                indexOffset, volumeExcludes, null, newVolumeMap);
        //log.info("timed0 {}", System.currentTimeMillis() - time0);        
        
        myExcludes.addAll(configExcludeList);
        myExcludes.addAll(volumeExcludes);
        return myExcludes;
    }

    private Trend getTrendIncDec(Market market, ComponentData param, List<String> stockDates, int interval,
            Map<String, List<List<Double>>> filteredCategoryValueMap, Integer[] trendInc, Integer[] trendDec,
            int indexOffset) {
        Trend trend = null;
        try {
            trend = new TrendUtil().getTrend(interval, null /*TimeUtil.convertDate2(olddate)*/, indexOffset, stockDates /*, findTime*/, param, market, filteredCategoryValueMap);
        } catch (Exception e) {
            log.error(Constants.ERROR, e);
        }
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
            log.info("time millis {}", System.currentTimeMillis() - time0);
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

    private Map<Integer, List<String>> getVolumeExcludesFull(SimulateInvestConfig simConfig, int interval, Map<String, List<List<Double>>> categoryValueMap,
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
        List<Integer> keyl = new ArrayList<>(keys);
        Collections.sort(keyl);
        log.info("kkkk {}", keyl);
        for (int key : keyl) {
            log.info("kkksize {} {}", key, listlist.get(key).size());
        }
        return listlist;
    }

    private void getVolumeExcludes(SimulateInvestConfig simConfig, int extradelay, List<String> stockDates,
            int interval, Map<String, List<List<Double>>> categoryValueMap,
            Map<String, List<List<Object>>> volumeMap, int delay, int indexOffset, List<String> volumeExcludes, Map<String, double[]> newVolumeMap, Map<Integer, List<String>> listlist) {
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
            String aParameter, List<SimulateStock> mystocks, int indexOffset, List<SimulateStock> sells, List<SimulateStock> buys,
            List<SimulateStock> holdIncrease, int extradelay, int delay) {
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
        List<String> myincl = adviser.getIncs(aParameter, simConfig.getStocks(), indexOffset + extradelay, stockDates, anExcludeList);
        Set<String> myincs = new LinkedHashSet<>(myincl);
        //myincs = new ArrayList<>(myincs);
        myincs.removeAll(anExcludeSet);
        //List<IncDecItem> myincs = ds.getIncs(valueList);
        //List<ValueList> valueList = ds.getValueList(categoryValueMap, indexOffset);

        // full list, except if null value
        //int delay = simConfig.getDelay();
        List<SimulateStock> buysTmp;
        if (indexOffset < delay + extradelay) {
            buysTmp = new ArrayList<>();
        } else {
            buysTmp = getBuyList(categoryValueMap, myincs, indexOffset - delay - extradelay, simConfig.getStocks());
        }
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

        sells.addAll(newsells);

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

        sells.addAll(newsells);

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
        simConfig.setConfidenceholdincrease(config.wantsSimulateInvestConfidenceHoldIncrease());
        simConfig.setNoconfidenceholdincrease(config.wantsSimulateInvestNoConfidenceHoldIncrease());
        simConfig.setConfidencetrendincrease(config.wantsSimulateInvestConfidenceTrendIncrease());
        simConfig.setConfidencetrendincreaseTimes(config.wantsSimulateInvestConfidenceTrendIncreaseTimes());
        simConfig.setNoconfidencetrenddecrease(config.wantsSimulateInvestNoConfidenceTrendDecrease());
        simConfig.setNoconfidencetrenddecreaseTimes(config.wantsSimulateInvestNoConfidenceTrendDecreaseTimes());
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

        }
        simConfig.setStoploss(config.wantsSimulateInvestStoploss());
        simConfig.setStoplossValue(config.getSimulateInvestStoplossValue());
        simConfig.setIntervalStoploss(config.wantsSimulateInvestIntervalStoploss());
        simConfig.setIntervalStoplossValue(config.getSimulateInvestIntervalStoplossValue());
        simConfig.setStocks(config.getSimulateInvestStocks());
        simConfig.setInterpolate(config.wantsSimulateInvestInterpolate());
        simConfig.setDay(config.getSimulateInvestDay());
        simConfig.setDelay(config.getSimulateInvestDelay());
        simConfig.setFuturecount(config.getSimulateInvestFutureCount());
        simConfig.setFuturetime(config.getSimulateInvestFutureTime());
        Map<String, Double> map = JsonUtil.convert(config.getSimulateInvestVolumelimits(), Map.class);
        simConfig.setVolumelimits(map);

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

    private AutoSimulateInvestConfig getAutoSimConfig(IclijConfig config) {
        if (config.getConfigValueMap().get(IclijConfigConstants.AUTOSIMULATEINVESTINTERVAL) == null || (int) config.getConfigValueMap().get(IclijConfigConstants.AUTOSIMULATEINVESTINTERVAL) == 0) {
            return null;
        }
        AutoSimulateInvestConfig simConfig = new AutoSimulateInvestConfig();
        simConfig.setInterval(config.getAutoSimulateInvestInterval());
        simConfig.setIntervalwhole(config.getAutoSimulateInvestIntervalwhole());
        simConfig.setPeriod(config.getAutoSimulateInvestPeriod());
        simConfig.setLastcount(config.getAutoSimulateInvestLastCount());
        simConfig.setDellimit(config.getAutoSimulateInvestDelLimit());
        simConfig.setScorelimit(config.getAutoSimulateInvestScoreLimit());
        simConfig.setAutoscorelimit(config.getAutoSimulateInvestAutoScoreLimit());
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
            sum.amount += astock.getCount() * astock.getPrice();
        }
        return sum;
    }

    @Override
    public ComponentData improve(MarketAction action, ComponentData componentparam, Market market, ProfitData profitdata,
            Memories positions, Boolean buy, String subcomponent, Parameters parameters, boolean wantThree,
            List<MLMetricsItem> mlTests) {
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

    @Override
    protected EvolutionConfig getImproveEvolutionConfig(IclijConfig config) {
        String evolveString = config.getImproveAbovebelowEvolutionConfig();
        return JsonUtil.convert(evolveString, EvolutionConfig.class);
    }

    public Object[] calculateAccuracy(ComponentData componentparam) throws Exception {
        return new Object[] { componentparam.getScoreMap().get(SimConstants.SCORE) };
    }

    private double increase(List<SimulateStock> mystocks, int indexOffset, Map<String, List<List<Double>>> categoryValueMap, int prevIndexOffset) {
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
                if (indexOffset <= -1) {
                    continue;
                }
                Double valNow = mainList.get(mainList.size() - 1 - indexOffset);
                if (prevIndexOffset <= -1) {
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

    private void lastbuysell(List<String> stockDates, LocalDate date, Adviser adviser, Capital capital, SimulateInvestConfig simConfig, Map<String, List<List<Double>>> categoryValueMap, List<SimulateStock> mystocks, int extradelay, int prevIndexOffset, Pair<Integer, Integer>[] hits, int findTimes, Parameters realParameters, Map<String, Object> map, Integer[] trendInc, Integer[] trendDec, List<String> configExcludeList, ComponentData param, Market market, int interval, Map<String, List<List<Double>>> filteredCategoryValueMap, Map<String, List<List<Object>>> volumeMap, int delay, BiMap<String, LocalDate> stockDatesBiMap) {
        mystocks = new ArrayList<>(mystocks);
        String aParameter = JsonUtil.convert(realParameters);
        date = TimeUtil.getForwardEqualAfter2(date, 0 /* findTime */, stockDates);
        String datestring = TimeUtil.convertDate2(date);
        int indexOffset = stockDates.size() - 1 - TimeUtil.getIndexEqualAfter(stockDates, datestring);

        Trend trend = getTrendIncDec(market, param, stockDates, interval, filteredCategoryValueMap, trendInc, trendDec, indexOffset);
        // get recommendations

        List<String> myExcludes = getExclusions(simConfig, extradelay, stockDates, interval, categoryValueMap,
                volumeMap, configExcludeList, delay, indexOffset, null);

        double myavg = increase(mystocks, indexOffset - extradelay, categoryValueMap, prevIndexOffset);

        List<SimulateStock> holdIncrease = new ArrayList<>();
        int up = update(categoryValueMap, mystocks, indexOffset - extradelay, holdIncrease, prevIndexOffset - extradelay);

        List<SimulateStock> sells = new ArrayList<>();
        List<SimulateStock> buys = new ArrayList<>();

        if (simConfig.getIntervalStoploss()) {
            // TODO delay
            stoploss(mystocks, stockDates, indexOffset - extradelay, categoryValueMap, prevIndexOffset - extradelay, sells, simConfig.getIntervalStoplossValue(), "ISTOP", stockDatesBiMap);                       
        }

        double myreliability = getReliability(mystocks, hits, findTimes, up);

        boolean confidence = !simConfig.getConfidence() || myreliability >= simConfig.getConfidenceValue();
        boolean confidence1 = !simConfig.getConfidencetrendincrease() || trendInc[0] >= simConfig.getConfidencetrendincreaseTimes();
        boolean noconfidence2 = simConfig.getNoconfidencetrenddecrease() && trendDec[0] >= simConfig.getNoconfidencetrenddecreaseTimes();
        boolean noconfidence = !confidence || !confidence1 || noconfidence2;
        if (!noconfidence) {
            int delay0 = 0;
            mystocks = confidenceBuyHoldSell(simConfig, stockDates, categoryValueMap, adviser, myExcludes, aParameter,
                    mystocks, indexOffset, sells, buys, holdIncrease, extradelay, delay0);
        } else {
            mystocks = noConfidenceHoldSell(mystocks, holdIncrease, sells, simConfig);
        }

        List<String> ids = mystocks.stream().map(SimulateStock::getId).collect(Collectors.toList());
        List<String> buyids = buys.stream().map(SimulateStock::getId).collect(Collectors.toList());
        List<String> sellids = sells.stream().map(SimulateStock::getId).collect(Collectors.toList());

        if (!noconfidence) {
            int buyCnt = simConfig.getStocks() - mystocks.size();
            buyCnt = Math.min(buyCnt, buys.size());
            ids.addAll(buyids.subList(0, buyCnt));
            buyids = buyids.subList(0, buyCnt);
        } else {
            buyids.clear();
        }
        ids.removeAll(sellids);
        map.put(SimConstants.LASTBUYSELL, "Buy: " + buyids + " Sell: " + sellids + " Stocks: " +ids);
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
            List<SimulateStock> sells, double stoploss, String stop, BiMap<String, LocalDate> stockDatesBiMap) {
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
                if (prevIndexOffset <= -1) {
                    int jj = 0;
                    continue;
                }
                Double valWas = mainList.get(mainList.size() - 1 - prevIndexOffset);
                if (valWas != null && valNow != null && valNow != 0 && valWas != 0 && valNow / valWas < stoploss) {
                    item.setStatus(stop);
                    newSells.add(item);
                }
            }
        }
        mystocks.removeAll(newSells);
        sells.addAll(newSells);
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
    
    private void buy(List<String> stockDates, Map<String, List<List<Double>>> categoryValueMap, Capital capital, int buytop, List<SimulateStock> mystocks, List<SimulateStock> newbuys, LocalDate date, int indexOffset, BiMap<String, LocalDate> stockDatesBiMap) {
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
                    date = convertDate(stockDatesBiMap, dateNow);
                    astock.setBuydate(date);
                    mystocks.add(astock);
                    totalamount += amount;
                } else {
                    log.error("Not found");
                }
            }
        }
        capital.amount -= totalamount;
    }

    private int update(Map<String, List<List<Double>>> categoryValueMap, List<SimulateStock> mystocks, int indexOffset,
            List<SimulateStock> noConfKeep, int prevIndexOffset) {
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
                if (indexOffset < 0) {
                    int jj = 0;
                }
                Double valNow = mainList.get(mainList.size() - 1 - indexOffset);
                if (valNow != null) {
                    item.setPrice(valNow);
                }
                if (prevIndexOffset <= -1) {
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
            List<SimulateStock> sells, List<SimulateStock> stockhistory, int indexOffset, LocalDate date, List<SimulateStock> mystocks, BiMap<String, LocalDate> stockDatesBiMap) {
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
                if (valNow != null) {
                    item.setSellprice(valNow);
                    String dateNow = stockDates.get(stockDates.size() - 1 - indexOffset);
                    date = convertDate(stockDatesBiMap, dateNow);
                    item.setSelldate(date);
                    stockhistory.add(item);
                    capital.amount += item.getCount() * item.getSellprice();
                } else {
                    // put back if unknown
                    mystocks.add(item);
                }
            } else {
                // put back if unknown
                mystocks.add(item);
            }
        }
    }

    private List<SimulateStock> getSellList(List<SimulateStock> mystocks, List<SimulateStock> newbuys) {
        List<String> myincids = newbuys.stream().map(SimulateStock::getId).collect(Collectors.toList());            
        return mystocks.stream().filter(e -> !myincids.contains(e.getId())).collect(Collectors.toList());
    }

    private List<SimulateStock> getBuyList(Map<String, List<List<Double>>> categoryValueMap, Set<String> myincs,
            int indexOffset, Integer count) {
        List<SimulateStock> newbuys = new ArrayList<>();
        for (String id : myincs) {
            List<List<Double>> resultList = categoryValueMap.get(id);
            if (resultList == null || resultList.isEmpty()) {
                continue;
            }
            List<Double> mainList = resultList.get(0);
            //ValidateUtil.validateSizes(mainList, stockDates);
            if (mainList != null) {
                Double valNow = mainList.get(mainList.size() - 1 - indexOffset);                    
                if (valNow != null) {
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
