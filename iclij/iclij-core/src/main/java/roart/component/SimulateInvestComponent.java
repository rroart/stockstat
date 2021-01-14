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

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

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
import roart.evolution.chromosome.impl.ConfigMapChromosome2;
import roart.evolution.chromosome.winner.IclijConfigMapChromosomeWinner;
import roart.evolution.config.EvolutionConfig;
import roart.evolution.iclijconfigmap.genetics.gene.impl.IclijConfigMapChromosome;
import roart.evolution.iclijconfigmap.genetics.gene.impl.IclijConfigMapGene;
import roart.iclij.config.IclijConfig;
import roart.iclij.config.IclijConfigConstants;
import roart.iclij.config.MLConfigs;
import roart.iclij.config.Market;
import roart.iclij.config.SimulateInvestConfig;
import roart.iclij.evolution.fitness.impl.FitnessIclijConfigMap;
import roart.iclij.filter.Memories;
import roart.iclij.model.MLMetricsItem;
import roart.iclij.model.IncDecItem;
import roart.iclij.model.MemoryItem;
import roart.iclij.model.Parameters;
import roart.iclij.model.Trend;
import roart.iclij.service.IclijServiceResult;
import roart.iclij.verifyprofit.TrendUtil;
import roart.service.model.ProfitData;
import roart.simulate.SimulateStock;
import roart.simulate.Capital;
import roart.simulate.StockHistory;

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
        int beatavg = 0;
        int runs = 0;
        SimulateInvestConfig simConfig = getSimConfig(config);
        int extradelay = 0;
        //Integer overrideAdviser = null;
        boolean evolving = param instanceof SimulateInvestData;
        if (!(param instanceof SimulateInvestData)) {
            SimulateInvestConfig localSimConfig = market.getSimulate();
            simConfig.merge(localSimConfig);
            if (simConfig.getExtradelay() != null) {
                extradelay = simConfig.getExtradelay();
            }
        } else {
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
            SimulateInvestConfig localSimConfig = market.getSimulate();
            if (localSimConfig != null && localSimConfig.getVolumelimits() != null && simConfig.getVolumelimits() == null) {
                simConfig.setVolumelimits(localSimConfig.getVolumelimits());
            }
            if (localSimConfig != null && localSimConfig.getExtradelay() != null) {
                extradelay = localSimConfig.getExtradelay();
            }
        }
        List<String> stockDates;
        if (simulateParam.getStockDates() != null) {
            stockDates = simulateParam.getStockDates();
        } else {
            stockDates = param.getService().getDates(market.getConfig().getMarket());           
        }
        BiMap<String, LocalDate> stockDatesBiMap = getStockDatesBiMap(config, stockDates);
        int interval = simConfig.getInterval();

        //ComponentData componentData = component.improve2(action, param, market, profitdata, null, buy, subcomponent, parameters, mlTests);
        Map<String, List<List<Double>>> categoryValueMap;
        if (simConfig.getInterpolate()) {
            categoryValueMap = param.getFillCategoryValueMap();
        } else {
            categoryValueMap = param.getCategoryValueMap();
        }
        Map<String, List<List<Object>>> volumeMap = param.getVolumeMap();
        LocalDate investStart = null;
        LocalDate investEnd = param.getFutureDate();
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
                mldate = stockDates.get(populate);                
            }
        }
        if (mldate == null) {
            mldate = stockDates.get(0);
        }
        if (simConfig.getStartdate() != null) {
            mldate = simConfig.getStartdate();
            mldate = mldate.replace('-', '.');
        }
        try {
            investStart = TimeUtil.convertDate(mldate);
        } catch (ParseException e1) {
            log.error(Constants.EXCEPTION, e1);
        }
        try {
            String enddate = simConfig.getEnddate();
            if (enddate != null) {
                enddate = enddate.replace('-', '.');
                investEnd = TimeUtil.convertDate(enddate);
            }
        } catch (ParseException e1) {
            log.error(Constants.EXCEPTION, e1);
        }

        Integer origAdviserId = (Integer) param.getInput().getValuemap().get(IclijConfigConstants.SIMULATEINVESTADVISER);
        int adviserId = simConfig.getAdviser();
        Adviser adviser = new AdviserFactory().get(adviserId, market, investStart, investEnd, param, simConfig);;

        String[] excludes = null;
        if (market.getSimulate() != null) {
            excludes = market.getSimulate().getExcludes();
        }
        if (excludes == null) {
            excludes = new String[0];
        }
        List<String> configExcludeList = Arrays.asList(excludes);
        Set<String> configExcludeSet = new HashSet<>(configExcludeList);

        Map<String, List<List<Double>>> filteredCategoryValueMap = new HashMap<>(categoryValueMap);
        filteredCategoryValueMap.keySet().removeAll(configExcludeSet);

        boolean intervalwhole = config.wantsSimulateInvestIntervalWhole();
        int end = 1;
        if (intervalwhole) {
            end = simConfig.getInterval();
        }

        List<String> parametersList = adviser.getParameters();
        if (parametersList.isEmpty()) {
            parametersList.add(null);
        }

        List<Double> scores = new ArrayList<>();

        // TODO investend and other reset of more params
        Parameters realParameters = parameters;
        if (realParameters == null || realParameters.getThreshold() == 1.0) {
            String aParameter = JsonUtil.convert(realParameters);

            int delay = simConfig.getDelay();
            int totalDelays = extradelay + delay;
            investEnd = TimeUtil.getBackEqualBefore2(investEnd, 0 /* findTime */, stockDates);
            if (investEnd != null) {
                String aDate = TimeUtil.convertDate2(investEnd);
                if (aDate != null) {
                    int idx = stockDates.indexOf(aDate) - extradelay;
                    if (idx >=0 ) {
                        aDate = stockDates.get(idx);
                        try {
                            investEnd = TimeUtil.convertDate(aDate);
                        } catch (ParseException e) {
                            log.error(Constants.EXCEPTION, e);
                        }
                    }
                }
            }
            LocalDate lastInvestEnd = TimeUtil.getBackEqualBefore2(investEnd, 0 /* findTime */, stockDates);
            if (lastInvestEnd != null) {
                String aDate = TimeUtil.convertDate2(lastInvestEnd);
                if (aDate != null) {
                    int idx = stockDates.indexOf(aDate) - delay;
                    if (idx >=0 ) {
                        aDate = stockDates.get(idx);
                        try {
                            lastInvestEnd = TimeUtil.convertDate(aDate);
                        } catch (ParseException e) {
                            log.error(Constants.EXCEPTION, e);
                        }
                    }
                }
            }

            Map<Integer, List<String>> volumeExcludeMap = null;
            Map<Integer, Trend> trendMap = null;
            {
                LocalDate date = investStart;

                date = TimeUtil.getEqualBefore(stockDates, date);
                if (date == null) {
                    try {
                        date = TimeUtil.convertDate(stockDates.get(0));
                    } catch (ParseException e) {
                        log.error(Constants.ERROR, e);
                    }
                }
                date = TimeUtil.getForwardEqualAfter2(date, 0 /* findTime */, stockDates);
                String datestring = TimeUtil.convertDate2(date);
                int firstidx = TimeUtil.getIndexEqualAfter(stockDates, datestring);
                int maxinterval = 20;
                firstidx -= maxinterval * 2;
                if (firstidx < 0) {
                    firstidx = 0;
                }
                
                String lastInvestEndS = TimeUtil.convertDate2(lastInvestEnd);
                int lastidx = stockDates.indexOf(lastInvestEndS);
                lastidx += maxinterval;
                if (lastidx >= stockDates.size()) {
                    lastidx = stockDates.size() - 1;
                }
                firstidx = stockDates.size() - 1 - firstidx;
                lastidx = stockDates.size() - 1 - lastidx;

                // vol lim w/ adviser?
                String key = CacheConstants.SIMULATEINVESTVOLUMELIMITS + market.getConfig().getMarket() + adviser.getClass().getName() + simConfig.getInterval() + investStart + investEnd + simConfig.getInterpolate();
                volumeExcludeMap = (Map<Integer, List<String>>) MyCache.getInstance().get(key);
                Map<Integer, List<String>> newVolumeExcludeMap = null;
                if (volumeExcludeMap == null || VERIFYCACHE) {
                    long time00 = System.currentTimeMillis();
                    newVolumeExcludeMap = getVolumeExcludesFull(simConfig, interval, categoryValueMap, volumeMap, firstidx, lastidx);
                    log.info("timee0 {}", System.currentTimeMillis() - time00);
                }
                if (VERIFYCACHE && volumeExcludeMap != null) {
                    for (Entry<Integer, List<String>> entry : newVolumeExcludeMap.entrySet()) {
                        int key2 = entry.getKey();
                        List<String> v2 = entry.getValue();
                        List<String> v = volumeExcludeMap.get(key2);
                        if (v2 != null && !v2.equals(v)) {
                            log.error("Difference with cache");
                        }
                    }
                }
              if (volumeExcludeMap == null) {
                    volumeExcludeMap = newVolumeExcludeMap;
                    MyCache.getInstance().put(key, volumeExcludeMap);
                }

                long time00 = System.currentTimeMillis();
                adviser.getValueMap(stockDates, firstidx, lastidx, categoryValueMap);
                log.info("timeee0 {}", System.currentTimeMillis() - time00);

                trendMap = getTrendIncDec(market, param, stockDates, interval, filteredCategoryValueMap, firstidx, lastidx, simConfig);
                Set<Integer> keys = trendMap.keySet();
                List<Integer> keyl = new ArrayList<>(keys);
                Collections.sort(keyl);
                log.info("keyl {}", keyl);
            }
            
            long time0 = System.currentTimeMillis();
            Map<String, Object> resultMap = new HashMap<>();
            for (int offset = 0; offset < end; offset++) {

                Capital capital = new Capital();
                capital.amount = 1;
                List<SimulateStock> mystocks = new ArrayList<>();
                List<SimulateStock> stockhistory = new ArrayList<>();
                List<StockHistory> history = new ArrayList<>();
                List<String> sumHistory = new ArrayList<>();
                List<String> plotDates = new ArrayList<>();
                List<Double> plotCapital = new ArrayList<>();
                List<Double> plotDefault = new ArrayList<>();
                double resultavg = 1;

                int findTimes = simConfig.getConfidenceFindTimes();
                Pair<Integer, Integer>[] hits = new ImmutablePair[findTimes];

                Integer[] trendInc = new Integer[] { 0 };
                Integer[] trendDec = new Integer[] { 0 };
                Pair<Integer, Integer> trendIncDec = new ImmutablePair<>(0, 0);

                int prevIndexOffset = 0;
                //try {
                LocalDate date = investStart;

                date = TimeUtil.getEqualBefore(stockDates, date);
                if (date == null) {
                    try {
                        date = TimeUtil.convertDate(stockDates.get(0));
                    } catch (ParseException e) {
                        log.error(Constants.ERROR, e);
                    }
                }
                date = TimeUtil.getForwardEqualAfter2(date, offset, stockDates);
                /*    
        } catch (Exception e) {
                log.error(Constants.ERROR, e);
                date = null;
            }
                 */
                int indexOffset = totalDelays;
                List<SimulateStock> savedStocks = new ArrayList<>();
                if (evolving || offset > 0) {
                    investEnd = lastInvestEnd;
                }
                boolean saveLastInvest = false;
                while (date != null && investEnd != null && !date.isAfter(investEnd)) {
                    boolean lastInvest = offset == 0 && date.isAfter(lastInvestEnd);
                    if (lastInvest) {
                        // not with evolving?
                        savedStocks = copy(mystocks);
                        saveLastInvest = true;
                    }
                    date = TimeUtil.getForwardEqualAfter2(date, 0 /* findTime */, stockDates);
                    String datestring = TimeUtil.convertDate2(date);
                    indexOffset = stockDates.size() - 1 - TimeUtil.getIndexEqualAfter(stockDates, datestring);

                    //Trend trend = getTrendIncDec(market, param, stockDates, interval, filteredCategoryValueMap, trendInc, trendDec, indexOffset);
                    Trend trend = getTrendIncDec(stockDates, trendInc, trendDec, indexOffset, trendMap);
                    // get recommendations

                    List<String> myExcludes = getExclusions(simConfig, extradelay, stockDates, interval, categoryValueMap,
                            volumeMap, configExcludeList, delay, indexOffset, volumeExcludeMap);

                    double myavg = increase(mystocks, indexOffset - extradelay, categoryValueMap, prevIndexOffset);

                    List<SimulateStock> holdIncrease = new ArrayList<>();
                    int up;
                    if (indexOffset - extradelay >= 0) {
                        up = update(categoryValueMap, mystocks, indexOffset - extradelay, holdIncrease, prevIndexOffset - extradelay);
                    } else {
                        up = mystocks.size();
                    }
                    
                    List<SimulateStock> sells = new ArrayList<>();
                    List<SimulateStock> buys = new ArrayList<>();

                    if (simConfig.getIntervalStoploss()) {
                        // TODO delay
                        if (indexOffset - extradelay - delay >= 0) {
                            stoploss(mystocks, stockDates, indexOffset - extradelay, categoryValueMap, prevIndexOffset - extradelay, sells, simConfig.getIntervalStoplossValue(), "ISTOP");
                        }
                    }

                    double myreliability = getReliability(mystocks, hits, findTimes, up);

                    boolean confidence = !simConfig.getConfidence() || myreliability >= simConfig.getConfidenceValue();
                    boolean confidence1 = !simConfig.getConfidencetrendincrease() || trendInc[0] >= simConfig.getConfidencetrendincreaseTimes();
                    boolean noconfidence2 = simConfig.getNoconfidencetrenddecrease() && trendDec[0] >= simConfig.getNoconfidencetrenddecreaseTimes();
                    boolean noconfidence = !confidence || !confidence1 || noconfidence2;
                    if (!noconfidence) {
                        int adelay = delay;
                        if (lastInvest) {
                            adelay = 0;
                        }
                        if (indexOffset - extradelay - adelay >= 0) {
                            mystocks = confidenceBuyHoldSell(simConfig, stockDates, categoryValueMap, adviser, myExcludes,
                                    aParameter, mystocks, indexOffset, sells, buys, holdIncrease, extradelay, adelay);
                        }
                    } else {
                        if (indexOffset - extradelay - delay >= 0) {
                            mystocks = noConfidenceHoldSell(mystocks, holdIncrease, sells, simConfig);
                        }
                    }

                    if (!lastInvest) {
                        if (indexOffset - extradelay - delay >= 0) {
                            // TODO delay DELAY
                            sell(stockDates, categoryValueMap, capital, sells, stockhistory, indexOffset - extradelay - delay, date, mystocks);

                            // TODO delay DELAY
                            buy(stockDates, categoryValueMap, capital, simConfig.getStocks(), mystocks, buys, date, indexOffset - extradelay - delay);

                            List<String> myids = mystocks.stream().map(SimulateStock::getId).collect(Collectors.toList());            
                            if (myids.size() != mystocks.size()) {
                                log.error("Sizes");
                            }

                            // to delay?
                            update(categoryValueMap, mystocks, indexOffset - extradelay - delay, new ArrayList<>(), prevIndexOffset - extradelay - delay);

                            if (trend != null && trend.incAverage != 0) {
                                resultavg *= trend.incAverage;
                            }

                            // depends on delay DELAY
                            Capital sum = getSum(mystocks);

                            //boolean noconf = simConfig.getConfidence() && myreliability < simConfig.getConfidenceValue();                
                            String hasNoConf = noconfidence ? "NOCONF" : "";
                            String historydatestring = stockDates.get(stockDates.size() - 1 - (indexOffset - extradelay - delay));

                            List<String> ids = mystocks.stream().map(SimulateStock::getId).collect(Collectors.toList());
                            if (!evolving) {
                                if (offset == 0) {
                                    sumHistory.add(datestring + " " + capital.toString() + " " + sum.toString() + " " + new MathUtil().round(resultavg, 2) + " " + hasNoConf + " " + ids + " " + trend);
                                    plotDates.add(historydatestring);
                                    plotDefault.add(resultavg);
                                    plotCapital.add(sum.amount + capital.amount);
                                }
                            } else {
                                StockHistory aHistory = new StockHistory(historydatestring, capital, sum, resultavg, hasNoConf, ids, trend.toString());
                                history.add(aHistory);
                            }

                            if (Double.isInfinite(resultavg)) {
                                int jj = 0;
                            }

                            runs++;
                            if (myavg > trend.incAverage) {
                                beatavg++;
                            }
                        }

                        if (simConfig.getStoploss()) {
                            for (int j = 0; j < interval; j++) {
                                sells = new ArrayList<>();
                                //System.out.println(interval + " " +  j);
                                if (indexOffset - j - 1 - extradelay < 0) {
                                    break;
                                }
                                // TODO delay DELAY
                                stoploss(mystocks, stockDates, indexOffset - j - extradelay, categoryValueMap, indexOffset - j - 1 - extradelay, sells, simConfig.getStoplossValue(), "STOP");                       
                                sell(stockDates, categoryValueMap, capital, sells, stockhistory, indexOffset - j - extradelay, date, mystocks);
                            }                    
                        }
                    } else {
                        if (offset == 0) {
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
                            param.getUpdateMap().put(SimConstants.LASTBUYSELL, "Buy: " + buyids + " Sell: " + sellids + " Stocks: " +ids);

                        }
                    }
                    prevIndexOffset = indexOffset;
                    if (indexOffset - interval < 0) {
                        break;
                    }
                    datestring = stockDates.get(stockDates.size() - 1 - (indexOffset - interval));
                    try {
                        date = TimeUtil.convertDate(datestring);
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
                boolean lastInvest = offset == 0 && date != null && lastInvestEnd != null && date.isAfter(lastInvestEnd);
                if (saveLastInvest) {
                    mystocks = savedStocks;
                }
                if (prevIndexOffset - extradelay - delay >= 0) {
                    update(categoryValueMap, mystocks, indexOffset - extradelay - delay, new ArrayList<>(), prevIndexOffset - extradelay - delay);
                }
                Capital sum = getSum(mystocks);
                sum.amount += capital.amount;

                long days = 0;
                if (investStart != null && investEnd != null) {
                    days = ChronoUnit.DAYS.between(investStart, investEnd);
                }
                double years = (double) days / 365;
                Double score = sum.amount / resultavg;
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
                    int myIndexOffset = stockDates.size() - 1 - TimeUtil.getIndexEqualAfter(stockDates, mldate);
                    Trend trend = new TrendUtil().getTrend(myIndexOffset - prevIndexOffset, null /*TimeUtil.convertDate2(olddate)*/, prevIndexOffset, stockDates /*, findTime*/, param, market, filteredCategoryValueMap);
                    log.info(trend.toString());
                    log.info("" + simConfig.asMap());
                }

                if (offset == 0) {
                    Map<String, Object> map = new HashMap<>();
                    if (!evolving) {
                        map.put(SimConstants.SUMHISTORY, sumHistory);
                        map.put(SimConstants.STOCKHISTORY, stockhistory);
                        map.put(SimConstants.PLOTDEFAULT, plotDefault);
                        map.put(SimConstants.PLOTDATES, plotDates);
                        map.put(SimConstants.PLOTCAPITAL, plotCapital);
                        map.put(SimConstants.STARTDATE, investStart);
                        map.put(SimConstants.ENDDATE, investEnd);
                        param.getUpdateMap().putAll(map);
                        param.getUpdateMap().putIfAbsent("lastbuysell", "Not buying or selling today");
                        componentData.getUpdateMap().putAll(map);
                    } else {
                        map.put(EvolveConstants.TITLETEXT, getPipeline() + " " + market.getConfig().getMarket() + " " + emptyNull(simConfig.getStartdate(), "start") + "-" + emptyNull(simConfig.getEnddate(), "end") + " " + (emptyNull(origAdviserId, "all")));
                        componentData.getUpdateMap().putAll(map);
                    }
                }
                if (evolving) {
                    for (SimulateStock stock : mystocks) {
                        stock.setSellprice(stock.getPrice());
                        stock.setStatus("END");
                    }
                    stockhistory.addAll(mystocks);
                    
                    Map<String, Object> map = new HashMap<>();
                    map.put(SimConstants.HISTORY, history);
                    map.put(SimConstants.STOCKHISTORY, stockhistory);
                    map.put(SimConstants.SCORE, score);
                    map.put(SimConstants.STARTDATE, TimeUtil.convertDate2(investStart));
                    map.put(SimConstants.ENDDATE, TimeUtil.convertDate2(investEnd));
                    //map.put("market", market.getConfig().getMarket());
                    resultMap.put("" + offset, map);
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

    private BiMap<String, LocalDate> getStockDatesBiMap(IclijConfig config, List<String> stockDates) {
        String key = CacheConstants.DATESMAP + config.getMarket() + config.getDate();
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

    private Map<Integer, Trend> getTrendIncDec(Market market, ComponentData param, List<String> stockDates, int interval,
            Map<String, List<List<Double>>> filteredCategoryValueMap, int firstidx, int lastidx, SimulateInvestConfig simConfig) {
        Map<Integer, Trend> trendMap = null;
        String key = CacheConstants.SIMULATEINVESTTREND + market.getConfig().getMarket() + "_" + simConfig.getAdviser() + "_" + interval + "_" + simConfig.getStartdate() + simConfig.getEnddate() + simConfig.getInterpolate();
        trendMap = (Map<Integer, Trend>) MyCache.getInstance().get(key);
        Map<Integer, Trend> newTrendMap = null;
        if (trendMap == null || VERIFYCACHE) {
            long time0 = System.currentTimeMillis();
            try {
                newTrendMap = new TrendUtil().getTrend(interval, null, stockDates, param, market, filteredCategoryValueMap, firstidx, lastidx);
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
                    double[] newList = new double[size];
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
                            newList[idx] += price * volume;
                        }
                    }
                    // now make the skip lists
                    int count = len;
                    double[] sums = newList; 
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
                        double sum = sums[idx];
                        if (count > 0 && sum / count < limit) {
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
        List<String> myincl = adviser.getIncs(aParameter, simConfig.getStocks(), indexOffset, stockDates, anExcludeList);
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
        SimulateInvestData param = new SimulateInvestData(componentparam);
        param.setAllIncDecs(getAllIncDecs(market, null, null));
        //param.setAllMemories(getAllMemories(market, null, null));
        param.setAllMetas(getAllMetas(componentparam));
        /*
        Integer adviser = (Integer) param.getInput().getConfig().getConfigValueMap().get(IclijConfigConstants.SIMULATEINVESTADVISER);
        if (adviser != null) {
            SimulateInvestConfig config = new SimulateInvestConfig();
            config.setAdviser(adviser);
            param.setConfig(config);
        }
         */
        getResultMaps(param, market);
        List<String> stockDates = param.getService().getDates(market.getConfig().getMarket());
        param.setStockDates(stockDates);
        List<String> confList = getConfList();

        int ga = param.getInput().getConfig().getEvolveGA();
        Evolve evolve = SimulateInvestEvolveFactory.factory(ga);
        String evolutionConfigString = param.getInput().getConfig().getImproveAbovebelowEvolutionConfig();
        EvolutionConfig evolutionConfig = JsonUtil.convert(evolutionConfigString, EvolutionConfig.class);
        //evolutionConfig.setGenerations(3);
        //evolutionConfig.setSelect(6);

        Map<String, Object> confMap = new HashMap<>();
        // confmap
        ComponentData e = evolve.evolve(action, param, market, profitdata, buy, subcomponent, parameters, mlTests, confMap , evolutionConfig, getPipeline(), this, confList);
        Map<String, Object> results = (Map<String, Object>) e.getResultMap();
        e.getService().send(ServiceConstants.SIMFILTER, results);
        return e;
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
        List<String> confList = new ArrayList<>();
        confList.add(IclijConfigConstants.SIMULATEINVESTCONFIDENCE);
        confList.add(IclijConfigConstants.SIMULATEINVESTCONFIDENCEVALUE);
        confList.add(IclijConfigConstants.SIMULATEINVESTCONFIDENCEFINDTIMES);
        confList.add(IclijConfigConstants.SIMULATEINVESTCONFIDENCEHOLDINCREASE);
        confList.add(IclijConfigConstants.SIMULATEINVESTNOCONFIDENCEHOLDINCREASE);
        confList.add(IclijConfigConstants.SIMULATEINVESTCONFIDENCETRENDINCREASE);
        confList.add(IclijConfigConstants.SIMULATEINVESTCONFIDENCETRENDINCREASETIMES);
        confList.add(IclijConfigConstants.SIMULATEINVESTNOCONFIDENCETRENDDECREASE);
        confList.add(IclijConfigConstants.SIMULATEINVESTNOCONFIDENCETRENDDECREASETIMES);
        confList.add(IclijConfigConstants.SIMULATEINVESTSTOPLOSS);
        confList.add(IclijConfigConstants.SIMULATEINVESTSTOPLOSSVALUE);
        confList.add(IclijConfigConstants.SIMULATEINVESTINTERVALSTOPLOSS);
        confList.add(IclijConfigConstants.SIMULATEINVESTINTERVALSTOPLOSSVALUE);
        confList.add(IclijConfigConstants.SIMULATEINVESTINDICATORPURE);
        confList.add(IclijConfigConstants.SIMULATEINVESTINDICATORREBASE);
        confList.add(IclijConfigConstants.SIMULATEINVESTINDICATORREVERSE);
        confList.add(IclijConfigConstants.SIMULATEINVESTINDICATORDIRECTION);
        confList.add(IclijConfigConstants.SIMULATEINVESTINDICATORDIRECTIONUP);
        confList.add(IclijConfigConstants.SIMULATEINVESTMLDATE);
        confList.add(IclijConfigConstants.SIMULATEINVESTSTOCKS);
        confList.add(IclijConfigConstants.SIMULATEINVESTBUYWEIGHT);
        confList.add(IclijConfigConstants.SIMULATEINVESTINTERVAL);
        confList.add(IclijConfigConstants.SIMULATEINVESTINTERPOLATE);
        confList.add(IclijConfigConstants.SIMULATEINVESTADVISER);
        confList.add(IclijConfigConstants.SIMULATEINVESTPERIOD);
        confList.add(IclijConfigConstants.SIMULATEINVESTDAY);
        return confList;
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

    private void lastbuysell(List<String> stockDates, LocalDate date, Adviser adviser, Capital capital, SimulateInvestConfig simConfig, Map<String, List<List<Double>>> categoryValueMap, List<SimulateStock> mystocks, int extradelay, int prevIndexOffset, Pair<Integer, Integer>[] hits, int findTimes, Parameters realParameters, Map<String, Object> map, Integer[] trendInc, Integer[] trendDec, List<String> configExcludeList, ComponentData param, Market market, int interval, Map<String, List<List<Double>>> filteredCategoryValueMap, Map<String, List<List<Object>>> volumeMap, int delay) {
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
            stoploss(mystocks, stockDates, indexOffset - extradelay, categoryValueMap, prevIndexOffset - extradelay, sells, simConfig.getIntervalStoplossValue(), "ISTOP");                       
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
            List<SimulateStock> sells, double stoploss, String stop) {
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
                LocalDate dateNow = null;
                try {
                    dateNow = TimeUtil.convertDate(dateNowStr);
                } catch (ParseException e) {
                    log.error(Constants.EXCEPTION, e);
                }
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

    private void buy(List<String> stockDates, Map<String, List<List<Double>>> categoryValueMap, Capital capital, int buytop, List<SimulateStock> mystocks, List<SimulateStock> newbuys, LocalDate date, int indexOffset) {
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
                    try {
                        date = TimeUtil.convertDate(dateNow);
                    } catch (ParseException e) {
                        log.error(Constants.EXCEPTION, e);
                    }
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
            List<SimulateStock> sells, List<SimulateStock> stockhistory, int indexOffset, LocalDate date, List<SimulateStock> mystocks) {
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
                    try {
                        date = TimeUtil.convertDate(dateNow);
                    } catch (ParseException e) {
                        log.error(Constants.EXCEPTION, e);
                    }
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

    private void getResultMaps(SimulateInvestData param, Market market) {
        //Map<String, List<Object>> objectMap = new HashMap<>();
        IclijConfig config = param.getInput().getConfig();

        Map<String, Object> aMap = new HashMap<>();
        // for improve evolver
        //List<MetaItem> metas = param.getService().getMetas();
        //MetaItem meta = new MetaUtil().findMeta(metas, market.getConfig().getMarket());
        //ComponentData componentData = component.improve2(action, param, market, profitdata, null, buy, subcomponent, parameters, mlTests);
        // don't need these both here and in getevolveml?
        aMap.put(ConfigConstants.MACHINELEARNING, false);
        aMap.put(ConfigConstants.AGGREGATORS, false);
        aMap.put(ConfigConstants.INDICATORS, true);
        aMap.put(ConfigConstants.INDICATORSMACD, true);
        aMap.put(ConfigConstants.MISCTHRESHOLD, null);        
        aMap.put(ConfigConstants.MISCMYTABLEDAYS, 0);
        aMap.put(ConfigConstants.MISCMYDAYS, 0);
        aMap.put(ConfigConstants.MISCPERCENTIZEPRICEINDEX, true);
        aMap.put(ConfigConstants.MISCINTERPOLATIONMETHOD, market.getConfig().getInterpolate());
        aMap.put(ConfigConstants.MISCMERGECY, false);
        // different line
        param.getResultMap(null, aMap);
        Map<String, Map<String, Object>> mapsRebase = param.getResultMaps();
        param.setResultRebaseMaps(mapsRebase);

        aMap.put(ConfigConstants.MISCPERCENTIZEPRICEINDEX, false);
        // different line
        param.getResultMap(null, aMap);
        //Map<String, Map<String, Object>> maps = param.getResultMaps();
        //param.getAndSetWantedCategoryValueMap();
        /*
        for (Entry<String, Map<String, Object>> entry : maps.entrySet()) {
            String key = entry.getKey();
            System.out.println("key " + key);
            System.out.println("keys " + entry.getValue().keySet());
        }
         */
        //Integer cat = (Integer) maps.get(PipelineConstants.META).get(PipelineConstants.WANTEDCAT);
        //String catName = new MetaUtil().getCategory(meta, cat);
        //Map<String, Object> resultMaps = maps.get(catName);
        /*
        if (resultMaps != null) {
            Map<String, Object> macdMaps = (Map<String, Object>) resultMaps.get(PipelineConstants.INDICATORMACD);
            //System.out.println("macd"+ macdMaps.keySet());
            objectMap = (Map<String, List<Object>>) macdMaps.get(PipelineConstants.OBJECT);
        }
         */
        //return resultMaps;
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
