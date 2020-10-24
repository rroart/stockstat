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
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Map.Entry;
import java.util.OptionalDouble;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import roart.action.MarketAction;
import roart.common.config.ConfigConstants;
import roart.common.constants.Constants;
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
import roart.db.IclijDbDao;
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
import roart.iclij.model.IncDecItem;
import roart.iclij.model.MLMetricsItem;
import roart.iclij.model.MemoryItem;
import roart.iclij.model.Parameters;
import roart.iclij.model.Trend;
import roart.iclij.verifyprofit.TrendUtil;
import roart.service.model.ProfitData;

public class SimulateInvestComponent extends ComponentML {

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
            if (localSimConfig != null && localSimConfig.getVolumelimits() != null) {
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
        Adviser adviser = new AdviserFactory().get(adviserId, market, investStart, investEnd, param, simConfig);

        String[] excludes = null;
        if (market.getSimulate() != null) {
            excludes = market.getSimulate().getExcludes();
        }
        if (excludes == null) {
            excludes = new String[0];
        }
        List<String> configExcludeList = Arrays.asList(excludes);

        Map<String, List<List<Double>>> filteredCategoryValueMap = new HashMap<>(categoryValueMap);
        filteredCategoryValueMap.keySet().removeAll(configExcludeList);

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
        
        for (String aParameter : parametersList) {
            Parameters realParameters = JsonUtil.convert(aParameter, Parameters.class);
            if (realParameters != null && realParameters.getThreshold() != 1.0) {
                continue;
            }

            int delay = simConfig.getDelay();
            int totalDelays = extradelay + delay;
            investEnd = TimeUtil.getBackEqualBefore2(investEnd, 0 /* findTime */, stockDates);
            if (investEnd != null) {
                String aDate = TimeUtil.convertDate2(investEnd);
                if (aDate != null) {
                    int idx = stockDates.indexOf(aDate) - totalDelays;
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
            
            for (int offset = 0; offset < end; offset++) {
                
            Capital capital = new Capital();
            capital.amount = 1;
            List<Astock> mystocks = new ArrayList<>();
            List<Astock> stockhistory = new ArrayList<>();
            List<String> sumHistory = new ArrayList<>();
            List<String> plotDates = new ArrayList<>();
            List<Double> plotCapital = new ArrayList<>();
            List<Double> plotDefault = new ArrayList<>();
            double resultavg = 1;
                        
            int findTimes = simConfig.getConfidenceFindTimes();
            Pair<Integer, Integer>[] hits = new ImmutablePair[findTimes];
           
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
            while (date != null && investEnd != null && date.isBefore(investEnd)) {
                date = TimeUtil.getForwardEqualAfter2(date, 0 /* findTime */, stockDates);
                String datestring = TimeUtil.convertDate2(date);
                indexOffset = stockDates.size() - 1 - TimeUtil.getIndexEqualAfter(stockDates, datestring);
                
                // get recommendations

                List<String> volumeExcludes = new ArrayList<>();
                getVolumeExcludes(simConfig, extradelay, stockDates, interval, categoryValueMap, volumeMap, delay,
                        indexOffset, volumeExcludes);
                
                List<String> myExcludes = new ArrayList<>();
                myExcludes.addAll(configExcludeList);
                myExcludes.addAll(volumeExcludes);
                double myavg = increase(capital, simConfig.getStocks(), mystocks, stockDates, indexOffset - extradelay, categoryValueMap, prevIndexOffset);
                List<Astock> holdIncrease = new ArrayList<>();
                int up = update(stockDates, categoryValueMap, capital, mystocks, indexOffset - extradelay, holdIncrease, prevIndexOffset - extradelay);

                List<Astock> sells = new ArrayList<>();
                List<Astock> buys = new ArrayList<>();
                
                if (simConfig.getIntervalStoploss()) {
                    // TODO delay
                    stoploss(capital, simConfig.getStocks(), mystocks, stockDates, indexOffset - extradelay, categoryValueMap, prevIndexOffset - extradelay, sells, simConfig.getIntervalStoplossValue(), "ISTOP");                       
                }
                
                double myreliability = getReliability(mystocks, hits, findTimes, up);
                
                if (!simConfig.getConfidence() || myreliability >= simConfig.getConfidenceValue()) {
                    mystocks = confidenceBuyHoldSell(simConfig, stockDates, categoryValueMap, adviser, myExcludes,
                            aParameter, mystocks, date, indexOffset, sells, buys, holdIncrease, extradelay, delay);
                } else {
                    mystocks = noConfidenceHoldSell(mystocks, holdIncrease, sells, simConfig);
                }

                // TODO delay DELAY
                sell(stockDates, categoryValueMap, capital, sells, stockhistory, indexOffset - extradelay - delay, date, mystocks);

                // TODO delay DELAY
                buy(stockDates, categoryValueMap, capital, simConfig.getStocks(), mystocks, buys, simConfig.getBuyweight(), date, indexOffset - extradelay - delay);

                List<String> myids = mystocks.stream().map(Astock::getId).collect(Collectors.toList());            
                if (myids.size() != mystocks.size()) {
                    log.error("Sizes");
                }
                
                Trend trend = null;
                try {
                    trend = new TrendUtil().getTrend(interval, null /*TimeUtil.convertDate2(olddate)*/, indexOffset, stockDates /*, findTime*/, param, market, filteredCategoryValueMap);
                } catch (Exception e) {
                    log.error(Constants.ERROR, e);
                }
                if (trend != null && trend.incAverage < 0) {
                    int jj = 0;
                }
                log.debug("Trend {}", trend);
                
                List<String> ids = mystocks.stream().map(Astock::getId).collect(Collectors.toList());
                // to delay?
                update(stockDates, categoryValueMap, capital, mystocks, indexOffset - extradelay - delay, new ArrayList<>(), prevIndexOffset - extradelay - delay);
                // depends on delay DELAY
                Capital sum = getSum(mystocks);

                boolean noconf = simConfig.getConfidence() && myreliability < simConfig.getConfidenceValue();                
                String hasNoConf = noconf ? "NOCONF" : "";
                datestring = stockDates.get(stockDates.size() - 1 - (indexOffset - extradelay - delay));
                sumHistory.add(datestring + " " + capital.toString() + " " + sum.toString() + " " + new MathUtil().round(resultavg, 2) + " " + hasNoConf + " " + ids + " " + trend);

                if (trend != null && trend.incAverage != 0) {
                    resultavg *= trend.incAverage;
                }
                
                plotDates.add(datestring);
                plotDefault.add(resultavg);
                plotCapital.add(sum.amount + capital.amount);
                
                if (Double.isInfinite(resultavg)) {
                    int jj = 0;
                }

                runs++;
                if (myavg > trend.incAverage) {
                    beatavg++;
                }
                
                if (simConfig.getStoploss()) {
                    for (int j = 0; j < interval; j++) {
                        sells = new ArrayList<>();
                        //System.out.println(interval + " " +  j);
                        if (indexOffset - j - 1 - extradelay < 0) {
                            break;
                        }
                        // TODO delay DELAY
                        stoploss(capital, simConfig.getStocks(), mystocks, stockDates, indexOffset - j - extradelay, categoryValueMap, indexOffset - j - 1 - extradelay, sells, simConfig.getStoplossValue(), "STOP");                       
                        sell(stockDates, categoryValueMap, capital, sells, stockhistory, indexOffset - j - extradelay, date, mystocks);
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
            if (offset == 0) {
                try {
                    List<String> myExcludes = new ArrayList<>();
                    myExcludes.addAll(configExcludeList);
                    List<String> volumeExcludes = new ArrayList<>();
                    getVolumeExcludes(simConfig, extradelay, stockDates, interval, categoryValueMap, volumeMap, delay,
                            indexOffset, volumeExcludes);
                    myExcludes.addAll(volumeExcludes);
                    lastbuysell(stockDates, date, adviser, capital, simConfig, categoryValueMap, mystocks, extradelay, prevIndexOffset, hits, findTimes, aParameter, myExcludes, param.getUpdateMap());
                } catch (Exception e) {
                    log.error(Constants.EXCEPTION, e);
                }
            }
            update(stockDates, categoryValueMap, capital, mystocks, indexOffset - extradelay - delay, new ArrayList<>(), prevIndexOffset - extradelay - delay);
            Capital sum = getSum(mystocks);
            sum.amount += capital.amount;
            
            long days = 0;
            if (investStart != null && investEnd != null) {
                days = ChronoUnit.DAYS.between(investStart, investEnd);
            }
            double years = (double) days / 365;
            Double score = sum.amount / resultavg;
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
            
            {
                int myIndexOffset = stockDates.size() - 1 - TimeUtil.getIndexEqualAfter(stockDates, mldate);
                Trend trend = new TrendUtil().getTrend(myIndexOffset - prevIndexOffset, null /*TimeUtil.convertDate2(olddate)*/, prevIndexOffset, stockDates /*, findTime*/, param, market, filteredCategoryValueMap);
                log.info(trend.toString());
                log.info("" + simConfig.asMap());
            }
            
            if (offset == 0) {
            Map<String, Object> map = new HashMap<>();
            map.put("sumhistory", sumHistory);
            map.put("stockhistory", stockhistory);
            map.put("plotdefault", plotDefault);
            map.put("plotdates", plotDates);
            map.put("plotcapital", plotCapital);
            map.put("startdate", investStart);
            map.put("enddate", investEnd);
            map.put("titletext", getPipeline() + " " + emptyNull(simConfig.getStartdate(), "start") + "-" + emptyNull(simConfig.getEnddate(), "end") + " " + (emptyNull(origAdviserId, "all")));
            param.getUpdateMap().putAll(map);
            componentData.getUpdateMap().putAll(map);
            }
        }
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
            map.put("scores", scores);
            map.put("stats", stats);
            double min = Collections.min(scores);
            double max = Collections.max(scores);
            int minDay = scores.indexOf(min);
            int maxDay = scores.indexOf(max);
            map.put("minmax", "min " + min + " at " + minDay + " and max " + max + " at " + maxDay);
            param.getUpdateMap().putAll(map);
            componentData.getUpdateMap().putAll(map);
        }    
        Map<String, Double> scoreMap = new HashMap<>();
        scoreMap.put("" + score, score);
        scoreMap.put("score", score);
        componentData.setScoreMap(scoreMap);

        //componentData.setFuturedays(0);

        handle2(action, market, componentData, profitdata, positions, evolve, aMap, subcomponent, mlmarket, parameters);
        return componentData;
    }

    private void getVolumeExcludes(SimulateInvestConfig simConfig, int extradelay, List<String> stockDates,
            int interval, Map<String, List<List<Double>>> categoryValueMap,
            Map<String, List<List<Object>>> volumeMap, int delay, int indexOffset, List<String> volumeExcludes) {
        if (simConfig.getVolumelimits() != null) {
            Map<String, Double> volumeLimits = simConfig.getVolumelimits();
            for (Entry<String, List<List<Double>>> entry : categoryValueMap.entrySet()) {
                String id = entry.getKey();
                int anOffset = indexOffset - extradelay - delay;
                int len = interval * 2;
                List<List<Double>> resultList = categoryValueMap.get(id);
                if (resultList == null || resultList.isEmpty()) {
                    continue;
                }
                List<Double> mainList = resultList.get(0);
                ValidateUtil.validateSizes(mainList, stockDates);
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
                    Double sum = 0.0;
                    int count = 0;
                    for (int i = first; i < size - 1 - anOffset; i++) {
                        Integer volume = (Integer) list.get(i).get(0);
                        if (volume != null) {
                            Double price = mainList.get(mainList.size() - 1 - indexOffset);
                            if (price == null) {
                                if (volume > 0) {
                                    log.error("Price null with volume > 0");
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

    private List<Astock> confidenceBuyHoldSell(SimulateInvestConfig simConfig, List<String> stockDates,
            Map<String, List<List<Double>>> categoryValueMap, Adviser adviser, List<String> excludeList,
            String aParameter, List<Astock> mystocks, LocalDate date, int indexOffset, List<Astock> sells,
            List<Astock> buys, List<Astock> holdIncrease, int extradelay, int delay) {
        List<Astock> hold;
        if (simConfig.getConfidenceholdincrease() == null || simConfig.getConfidenceholdincrease()) {
            hold = holdIncrease;
        } else {
            hold = new ArrayList<>();
        }

        List<String> anExcludeList = new ArrayList<>(excludeList);
        List<String> ids1 = sells.stream().map(Astock::getId).collect(Collectors.toList());
        List<String> ids2 = hold.stream().map(Astock::getId).collect(Collectors.toList());
        anExcludeList.addAll(ids1);
        anExcludeList.addAll(ids2);
        
        // full list
        List<IncDecItem> myincs = adviser.getIncs(aParameter, simConfig.getStocks(), date, indexOffset, stockDates, anExcludeList);
        //List<IncDecItem> myincs = ds.getIncs(valueList);
        //List<ValueList> valueList = ds.getValueList(categoryValueMap, indexOffset);

        // full list, except if null value
        //int delay = simConfig.getDelay();
        List<Astock> buysTmp;
        if (indexOffset < delay + extradelay) {
            buysTmp = new ArrayList<>();
        } else {
            buysTmp = getBuyList(stockDates, categoryValueMap, myincs, indexOffset - delay - extradelay, simConfig.getStocks());
        }
        //buysTmp = filter(buysTmp, sells);
        List<Astock> keeps = keep(mystocks, buysTmp);
        keeps.addAll(hold);
        buysTmp = filter(buysTmp, mystocks);
        //buysTmp = buysTmp.subList(0, Math.min(buysTmp.size(), simConfig.getStocks() - mystocks.size()));
        buys.clear();
        buys.addAll(buysTmp);
        
        List<Astock> newsells = filter(mystocks, keeps);

        //mystocks.removeAll(newsells);
        
        sells.addAll(newsells);
        
        mystocks = keeps;
        return mystocks;
    }

    private List<Astock> noConfidenceHoldSell(List<Astock> mystocks, List<Astock> holdIncrease, List<Astock> sells, SimulateInvestConfig simConfig) {
        List<Astock> keeps;
        if (simConfig.getNoconfidenceholdincrease() == null || simConfig.getNoconfidenceholdincrease()) {
            keeps = holdIncrease;
        } else {
            keeps = new ArrayList<>();
        }
        
        List<Astock> newsells = filter(mystocks, keeps);

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
        simConfig.setInterval(config.getSimulateInvestInterval());
        simConfig.setIndicatorPure(config.wantsSimulateInvestIndicatorPure());
        simConfig.setIndicatorRebase(config.wantsSimulateInvestIndicatorRebase());
	simConfig.setIndicatorReverse(config.wantsSimulateInvestIndicatorReverse());
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
    private List<Astock> filter(List<Astock> stocks, List<Astock> others) {
        List<String> ids = others.stream().map(Astock::getId).collect(Collectors.toList());        
        return stocks.stream().filter(e -> !ids.contains(e.id)).collect(Collectors.toList());
    }

    private List<Astock> keep(List<Astock> stocks, List<Astock> others) {
        List<String> ids = others.stream().map(Astock::getId).collect(Collectors.toList());        
        return stocks.stream().filter(e -> ids.contains(e.id)).collect(Collectors.toList());
    }

    private Capital getSum(List<Astock> mystocks) {
        Capital sum = new Capital();
        for (Astock astock : mystocks) {
            sum.amount += astock.count * astock.price;
        }
        return sum;
    }

    @Override
    public ComponentData improve(MarketAction action, ComponentData componentparam, Market market, ProfitData profitdata,
            Memories positions, Boolean buy, String subcomponent, Parameters parameters, boolean wantThree,
            List<MLMetricsItem> mlTests) {
        SimulateInvestData param = new SimulateInvestData(componentparam);
        param.setAllIncDecs(getAllIncDecs(market, null, null));
        param.setAllMemories(getAllMemories(market, null, null));
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
        return evolve.evolve(action, param, market, profitdata, buy, subcomponent, parameters, mlTests, confMap , evolutionConfig, getPipeline(), this, confList);
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
        confList.add(IclijConfigConstants.SIMULATEINVESTSTOPLOSS);
        confList.add(IclijConfigConstants.SIMULATEINVESTSTOPLOSSVALUE);
        confList.add(IclijConfigConstants.SIMULATEINVESTINTERVALSTOPLOSS);
        confList.add(IclijConfigConstants.SIMULATEINVESTINTERVALSTOPLOSSVALUE);
        confList.add(IclijConfigConstants.SIMULATEINVESTINDICATORPURE);
        confList.add(IclijConfigConstants.SIMULATEINVESTINDICATORREBASE);
        confList.add(IclijConfigConstants.SIMULATEINVESTINDICATORREVERSE);
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
        return new Object[] { componentparam.getScoreMap().get("score") };
    }
    
    private double increase(Capital capital, int buytop, List<Astock> mystocks, List<String> stockDates, int indexOffset,
            Map<String, List<List<Double>>> categoryValueMap, int prevIndexOffset) {
        List<Double> incs = new ArrayList<>();
        for (Astock item : mystocks) {
            String id = item.id;
            List<List<Double>> resultList = categoryValueMap.get(id);
            if (resultList == null || resultList.isEmpty()) {
                continue;
            }
            List<Double> mainList = resultList.get(0);
            ValidateUtil.validateSizes(mainList, stockDates);
            if (mainList != null) {
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

    private void lastbuysell(List<String> stockDates, LocalDate date, Adviser adviser, Capital capital, SimulateInvestConfig simConfig, Map<String, List<List<Double>>> categoryValueMap, List<Astock> mystocks, int extradelay, int prevIndexOffset, Pair<Integer, Integer>[] hits, int findTimes, String aParameter, List<String> excludeList, Map<String, Object> map) {
        mystocks = new ArrayList<>(mystocks);
        date = TimeUtil.getForwardEqualAfter2(date, 0 /* findTime */, stockDates);
        String datestring = TimeUtil.convertDate2(date);
        int indexOffset = stockDates.size() - 1 - TimeUtil.getIndexEqualAfter(stockDates, datestring);
        
        // get recommendations

        List<Astock> holdIncrease = new ArrayList<>();
        int up = update(stockDates, categoryValueMap, capital, mystocks, indexOffset - extradelay, holdIncrease, prevIndexOffset - extradelay);

        List<Astock> sells = new ArrayList<>();
        List<Astock> buys = new ArrayList<>();
        
        if (simConfig.getIntervalStoploss()) {
            // TODO delay
            stoploss(capital, simConfig.getStocks(), mystocks, stockDates, indexOffset - extradelay, categoryValueMap, prevIndexOffset - extradelay, sells, simConfig.getIntervalStoplossValue(), "ISTOP");                       
        }
        
        double myreliability = getReliability(mystocks, hits, findTimes, up);
        
        if (!simConfig.getConfidence() || myreliability >= simConfig.getConfidenceValue()) {
            

            //mystocks.removeAll(newsells);
            
            int delay = 0;
            mystocks = confidenceBuyHoldSell(simConfig, stockDates, categoryValueMap, adviser, excludeList, aParameter,
                    mystocks, date, indexOffset, sells, buys, holdIncrease, extradelay, delay);
        } else {
            mystocks = noConfidenceHoldSell(mystocks, holdIncrease, sells, simConfig);
        }

        List<String> ids = mystocks.stream().map(Astock::getId).collect(Collectors.toList());
        List<String> buyids = buys.stream().map(Astock::getId).collect(Collectors.toList());
        List<String> sellids = sells.stream().map(Astock::getId).collect(Collectors.toList());
        
        if (!simConfig.getConfidence() || myreliability >= simConfig.getConfidenceValue()) {
            int buyCnt = simConfig.getStocks() - mystocks.size();
            buyCnt = Math.min(buyCnt, buys.size());
            ids.addAll(buyids.subList(0, buyCnt));
        }
        ids.removeAll(sellids);
        map.put("lastbuysell", "Buy: " + buyids + " Sell: " + sellids + " Stocks: " +ids);
    }

    private double getReliability(List<Astock> mystocks, Pair<Integer, Integer>[] hits, int findTimes, int up) {
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
    
    private void stoploss(Capital capital, int buytop, List<Astock> mystocks, List<String> stockDates, int indexOffset,
            Map<String, List<List<Double>>> categoryValueMap, int prevIndexOffset, List<Astock> sells, double stoploss, String stop) {
        List<Astock> newSells = new ArrayList<>();
        for (Astock item : mystocks) {
            String id = item.id;
            List<List<Double>> resultList = categoryValueMap.get(id);
            if (resultList == null || resultList.isEmpty()) {
                continue;
            }
            List<Double> mainList = resultList.get(0);
            ValidateUtil.validateSizes(mainList, stockDates);
            if (mainList != null) {
                String dateNowStr = stockDates.get(stockDates.size() - 1 - indexOffset);
                LocalDate dateNow = null;
                try {
                    dateNow = TimeUtil.convertDate(dateNowStr);
                } catch (ParseException e) {
                    log.error(Constants.EXCEPTION, e);
                }
                if (!dateNow.isAfter(item.buydate)) {
                    continue;
                }
                Double valNow = mainList.get(mainList.size() - 1 - indexOffset);
                if (prevIndexOffset <= -1) {
                    int jj = 0;
                    continue;
                }
                Double valWas = mainList.get(mainList.size() - 1 - prevIndexOffset);
                if (valWas != null && valNow != null && valNow != 0 && valWas != 0 && valNow / valWas < stoploss) {
                    item.status = stop;
                    newSells.add(item);
                }
            }
        }
        mystocks.removeAll(newSells);
        sells.addAll(newSells);
    }

    private void buy(List<String> stockDates, Map<String, List<List<Double>>> categoryValueMap, Capital capital, int buytop, List<Astock> mystocks, List<Astock> newbuys, boolean buyweight, LocalDate date, int indexOffset) {
        int buys = buytop - mystocks.size();
        buys = Math.min(buys, newbuys.size());
        
        double totalweight = 0;
        if (buyweight) {
            for (int i = 0; i < buys; i++) {
                totalweight += Math.abs(newbuys.get(i).weight);
            }
        }
        double totalamount = 0;
        for (int i = 0; i < buys; i++) {
            Astock astock = newbuys.get(i);

            String id = astock.id;
           
            List<List<Double>> resultList = categoryValueMap.get(id);
            if (resultList == null || resultList.isEmpty()) {
                continue;
            }
            List<Double> mainList = resultList.get(0);
            ValidateUtil.validateSizes(mainList, stockDates);
            if (mainList != null) {
                Double valNow = mainList.get(mainList.size() - 1 - indexOffset);
                if (valNow != null) {

                    double amount = 0;
                    if (!buyweight) {
                        amount = capital.amount / buys;
                    } else {
                        amount = capital.amount * astock.weight / totalweight;
                    }
                    astock.buyprice = valNow;
                    astock.count = amount / astock.buyprice;
                    String dateNow = stockDates.get(stockDates.size() - 1 - indexOffset);
                    try {
                        date = TimeUtil.convertDate(dateNow);
                    } catch (ParseException e) {
                        log.error(Constants.EXCEPTION, e);
                    }
                    astock.buydate = date;
                    mystocks.add(astock);
                    totalamount += amount;
                } else {
                    log.error("Not found");
                }
            }
       }
        capital.amount -= totalamount;
    }

    private int update(List<String> stockDates, Map<String, List<List<Double>>> categoryValueMap, Capital capital,
            List<Astock> mystocks, int indexOffset, List<Astock> noConfKeep, int prevIndexOffset) {
        int up = 0;
        for (Astock item : mystocks) {
            String id = item.id;
            List<List<Double>> resultList = categoryValueMap.get(id);
            if (resultList == null || resultList.isEmpty()) {
                continue;
            }
            List<Double> mainList = resultList.get(0);
            ValidateUtil.validateSizes(mainList, stockDates);
            if (mainList != null) {
                Double valNow = mainList.get(mainList.size() - 1 - indexOffset);
                if (valNow != null) {
                    item.price = valNow;
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
            List<Astock> sells, List<Astock> stockhistory, int indexOffset, LocalDate date, List<Astock> mystocks) {
        for (Astock item : sells) {
            String id = item.id;
            List<List<Double>> resultList = categoryValueMap.get(id);
            if (resultList == null || resultList.isEmpty()) {
                continue;
            }
            List<Double> mainList = resultList.get(0);
            ValidateUtil.validateSizes(mainList, stockDates);
            if (mainList != null) {
                Double valNow = mainList.get(mainList.size() - 1 - indexOffset);
                if (valNow != null) {
                    item.sellprice = valNow;
                    String dateNow = stockDates.get(stockDates.size() - 1 - indexOffset);
                    try {
                        date = TimeUtil.convertDate(dateNow);
                    } catch (ParseException e) {
                        log.error(Constants.EXCEPTION, e);
                    }
                    item.selldate = date;
                    stockhistory.add(item);
                    capital.amount += item.count * item.sellprice;
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

    private List<Astock> getSellList(List<Astock> mystocks, List<Astock> newbuys) {
        List<String> myincids = newbuys.stream().map(Astock::getId).collect(Collectors.toList());            
        return mystocks.stream().filter(e -> !myincids.contains(e.id)).collect(Collectors.toList());
    }

    private List<Astock> getBuyList(List<String> stockDates, Map<String, List<List<Double>>> categoryValueMap,
            List<IncDecItem> myincs, int indexOffset, Integer count) {
        List<Astock> newbuys = new ArrayList<>();
        for (IncDecItem item : myincs) {
            String id = item.getId();
            List<List<Double>> resultList = categoryValueMap.get(id);
            if (resultList == null || resultList.isEmpty()) {
                continue;
            }
            List<Double> mainList = resultList.get(0);
            ValidateUtil.validateSizes(mainList, stockDates);
            if (mainList != null) {
                Double valNow = mainList.get(mainList.size() - 1 - indexOffset);
                if (valNow != null) {
                    Astock astock = new Astock();
                    astock.id = id;
                    //astock.price = -1;
                    astock.weight = Math.abs(item.getScore());
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
    
    class Astock {
        public String id;
        
        public double price;
        
        public double count;
        
        public double buyprice;
        
        public double sellprice;
        
        public LocalDate buydate;
        
        public LocalDate selldate;
        
        public double weight;
        
        public String status;
        
        public String getId() {
            return id;
        }
        
        public String toString() {
            MathUtil mu = new MathUtil();
            return id + " " + mu.round(price, 3) + " " + count + " " + mu.round(buyprice,  3) + " " + mu.round(sellprice, 3) + " " + buydate + " " + selldate;  
        }
    }
    
    class Capital {
        public double amount;
        
        public String toString() {
            return "" + new MathUtil().round(amount, 2);
        }
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
