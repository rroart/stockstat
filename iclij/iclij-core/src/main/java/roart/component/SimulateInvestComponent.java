package roart.component;

import java.text.ParseException;
import java.time.LocalDate;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
import roart.evolution.chromosome.impl.IclijConfigMapChromosome;
import roart.evolution.chromosome.impl.IclijConfigMapGene;
import roart.evolution.chromosome.winner.IclijConfigMapChromosomeWinner;
import roart.evolution.config.EvolutionConfig;
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
        double resultavg = 1;
        int beatavg = 0;
        int runs = 0;
        SimulateInvestConfig simConfig = new SimulateInvestConfig();
	simConfig.setAdviser(config.getSimulateInvestAdviser());
	simConfig.setBuyweight(config.wantsSimulateInvestBuyweight());
	simConfig.setConfidence(config.wantsSimulateInvestConfidence());
	simConfig.setConfidenceValue(config.getSimulateInvestCondidenceValue());
        simConfig.setConfidenceFindTimes(config.getSimulateInvestConfidenceFindtimes());
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
        try {
            simConfig.setEnddate(config.getSimulateInvestEnddate());
        } catch (Exception e) {
            
        }
        try {
            simConfig.setStartdate(config.getSimulateInvestStartdate());
        } catch (Exception e) {
            
        }
        SimulateInvestConfig localSimConfig = market.getSimulate();
        if (!(param instanceof SimulateInvestData)) {
            simConfig.merge(localSimConfig);
        }
        List<String> stockDates;
        if (simulateParam.getStockDates() != null) {
            stockDates = simulateParam.getStockDates();
        } else {
            stockDates = param.getService().getDates(market.getConfig().getMarket());           
        }
        int findTime = market.getConfig().getFindtime();
        int interval = config.getSimulateInvestInterval();
        
        //ComponentData componentData = component.improve2(action, param, market, profitdata, null, buy, subcomponent, parameters, mlTests);
        Map<String, List<List<Double>>> categoryValueMap;
        if (simConfig.getInterpolate()) {
            categoryValueMap = param.getFillCategoryValueMap();
        } else {
            categoryValueMap = param.getCategoryValueMap();
        }
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
        
        int i = simConfig.getAdviser();
        Adviser adviser = new AdviserFactory().get(i, market, investStart, investEnd, param, simConfig);

        String[] excludes = null;
        if (market.getSimulate() != null) {
            excludes = market.getSimulate().getExcludes();
        }
        if (excludes == null) {
            excludes = new String[0];
        }
        List<String> excludeList = Arrays.asList(excludes);

        Map<String, List<List<Double>>> filteredCategoryValueMap = new HashMap<>(categoryValueMap);
        filteredCategoryValueMap.keySet().removeAll(excludeList);

        List<String> plotDates = new ArrayList<>();
        List<Double> plotCapital = new ArrayList<>();
        List<Double> plotDefault = new ArrayList<>();
        
        List<String> parametersList = adviser.getParameters();
        if (parametersList.isEmpty()) {
            parametersList.add(null);
        }
        for (String aParameter : parametersList) {
            Parameters realParameters = JsonUtil.convert(aParameter, Parameters.class);
            if (realParameters != null && realParameters.getThreshold() != 1.0) {
                continue;
            }
            Capital capital = new Capital();
            capital.amount = 1;
            List<Astock> mystocks = new ArrayList<>();
            List<Astock> stockhistory = new ArrayList<>();
            List<String> sumHistory = new ArrayList<>();
            
            int findTimes = simConfig.getConfidenceFindTimes();
            Pair<Integer, Integer>[] hits = new ImmutablePair[findTimes];
           
            LocalDate date = investStart;
            int prevIndexOffset = 0;
            date = TimeUtil.getEqualBefore(stockDates, date);
            while (date.isBefore(investEnd)) {
                date = TimeUtil.getForwardEqualAfter2(date, 0 /* findTime */, stockDates);
                String datestring = TimeUtil.convertDate2(date);
                int indexOffset = stockDates.size() - 1 - TimeUtil.getIndexEqualAfter(stockDates, datestring);
                
                double myreliability = adviser.getReliability(date, true);

                // get recommendations

                double myavg = increase(capital, simConfig.getStocks(), mystocks, stockDates, indexOffset, findTime, categoryValueMap, prevIndexOffset);
                int up = update(stockDates, categoryValueMap, capital, mystocks, indexOffset);

                List<Astock> sells = new ArrayList<>();
                List<Astock> buys = new ArrayList<>();
                
                if (simConfig.getIntervalStoploss()) {
                    stoploss(capital, simConfig.getStocks(), mystocks, stockDates, indexOffset, findTime, categoryValueMap, prevIndexOffset, sells, simConfig.getIntervalStoplossValue(), "ISTOP");                       
                }
                
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
                double reliabilty = 1;
                if (total > 0) {
                    reliabilty = count / total;
                }

                myreliability = reliabilty;
                
                boolean noconf = simConfig.getConfidence() && myreliability < simConfig.getConfidenceValue();
                
                if (!simConfig.getConfidence() || myreliability >= simConfig.getConfidenceValue()) {
                    List<IncDecItem> myincs = adviser.getIncs(aParameter, simConfig.getStocks(), date, indexOffset, stockDates, excludeList);
                    //List<IncDecItem> myincs = ds.getIncs(valueList);
                    //List<ValueList> valueList = ds.getValueList(categoryValueMap, indexOffset);

                    buys = getBuyList(stockDates, categoryValueMap, myincs, indexOffset);
                    buys = filter(buys, sells);
                    List<Astock> keeps = keep(mystocks, buys);
                    buys = filter(buys, mystocks);
                    
                    List<Astock> newsells = filter(mystocks, keeps);

                    //mystocks.removeAll(newsells);
                    
                    sells.addAll(newsells);
                    
                    mystocks = keeps;
                }

                sell(stockDates, categoryValueMap, capital, sells, stockhistory, indexOffset, date, mystocks);

                buy(capital, simConfig.getStocks(), mystocks, buys, simConfig.getBuyweight(), date);

                List<String> myids = mystocks.stream().map(Astock::getId).collect(Collectors.toList());            
                if (myids.size() != mystocks.size()) {
                    log.error("Sizes");
                }
                
                Trend trend = new TrendUtil().getTrend(findTime, null /*TimeUtil.convertDate2(olddate)*/, indexOffset, stockDates /*, findTime*/, param, market, filteredCategoryValueMap);
                if (trend.incAverage < 0) {
                    int jj = 0;
                }
                log.debug("Trend {}", trend);
                
                List<String> ids = mystocks.stream().map(Astock::getId).collect(Collectors.toList());
                Capital sum = getSum(mystocks);
                String hasNoConf = noconf ? "NOCONF" : "";
                sumHistory.add(datestring + " " + capital.toString() + " " + sum.toString() + " " + new MathUtil().round(resultavg, 2) + " " + hasNoConf + " " + ids + " " + trend);

                if (trend.incAverage != 0) {
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
                        if (indexOffset - j - 1 < 0) {
                            break;
                        }
                        stoploss(capital, simConfig.getStocks(), mystocks, stockDates, indexOffset - j, 1, categoryValueMap, indexOffset - j - 1, sells, simConfig.getStoplossValue(), "STOP");                       
                        sell(stockDates, categoryValueMap, capital, sells, stockhistory, indexOffset - j, date, mystocks);
                    }                    
                }
                date = date.plusDays(interval);
                prevIndexOffset = indexOffset;
                //memoryList.add(memory);
            }
            Capital sum = getSum(mystocks);
            sum.amount += capital.amount;
            
            Period period = Period.between(investStart, date);
            long days = ChronoUnit.DAYS.between(investStart, date);
            double years = (double) days / 365;
            Double score = sum.amount / resultavg;
            score = Math.pow(score, 1 / years);
            if (score < -1) {
                int jj = 0;
            }
            if (score > 10) {
                int jj = 0;
            }
            if (score > 100) {
                int jj = 0;
            }
            Map<String, Double> scoreMap = new HashMap<>();
            scoreMap.put("" + score, score);
            scoreMap.put("score", score);
            componentData.setScoreMap(scoreMap);
            
            {
                int indexOffset = stockDates.size() - 1 - TimeUtil.getIndexEqualAfter(stockDates, mldate);
                Trend trend = new TrendUtil().getTrend(indexOffset - prevIndexOffset, null /*TimeUtil.convertDate2(olddate)*/, prevIndexOffset, stockDates /*, findTime*/, param, market, filteredCategoryValueMap);
                log.info(trend.toString());
                log.info("" + simConfig.asMap());
            }
            
            Map<String, Object> map = new HashMap<>();
            map.put("sumhistory", sumHistory);
            map.put("stockhistory", stockhistory);
            map.put("plotdefault", plotDefault);
            map.put("plotdates", plotDates);
            map.put("plotcapital", plotCapital);
            map.put("startdate", investStart);
            map.put("enddate", investEnd);
            map.put("titletext", getPipeline() + " " + investStart + " - " + investEnd);
            param.getUpdateMap().putAll(map);
            componentData.getUpdateMap().putAll(map);
        }

        //componentData.setFuturedays(0);

        handle2(action, market, componentData, profitdata, positions, evolve, aMap, subcomponent, mlmarket, parameters);
        return componentData;
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
        getResultMaps(param, market);
        List<String> stockDates = param.getService().getDates(market.getConfig().getMarket());
        param.setStockDates(stockDates);
        List<String> confList = getConfList();
        IclijConfigMapGene gene = new IclijConfigMapGene(confList, param.getInput().getConfig());
        IclijConfigMapChromosome chromosome = new IclijConfigMapChromosome(gene);
        //loadme(param, chromosome, market, confList, buy, subcomponent, action, parameters);
        FitnessIclijConfigMap fit = new FitnessIclijConfigMap(action, param, profitdata, market, null, getPipeline(), buy, subcomponent, parameters, gene, stockDates);
        return improve(action, param, chromosome, subcomponent, new IclijConfigMapChromosomeWinner(), buy, fit);
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
            int findTime, Map<String, List<List<Double>>> categoryValueMap, int prevIndexOffset) {
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

    private void stoploss(Capital capital, int buytop, List<Astock> mystocks, List<String> stockDates, int indexOffset,
            int findTime, Map<String, List<List<Double>>> categoryValueMap, int prevIndexOffset, List<Astock> sells, double stoploss, String stop) {
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
                Double valNow = mainList.get(mainList.size() - 1 - indexOffset);
                if (prevIndexOffset <= -1) {
                    int jj = 0;
                }
                Double valWas = mainList.get(mainList.size() - 1 - prevIndexOffset);
                if (valWas != null && valNow != null && valNow != 0 && valNow / valWas < stoploss) {
                    item.status = stop;
                    newSells.add(item);
                }
            }
        }
        mystocks.removeAll(newSells);
        sells.addAll(newSells);
    }

    private void buy(Capital capital, int buytop, List<Astock> mystocks, List<Astock> newbuys, boolean buyweight, LocalDate date) {
        int buys = buytop - mystocks.size();
        buys = Math.min(buys, newbuys.size());
        
        double totalweight = 0;
        if (buyweight) {
            for (int i = 0; i < buys; i++) {
                totalweight += newbuys.get(i).weight;
            }
        }
        double totalamount = 0;
        for (int i = 0; i < buys; i++) {
            Astock astock = newbuys.get(i);
            double amount = 0;
            if (!buyweight) {
                amount = capital.amount / buys;
            } else {
                amount = capital.amount * astock.weight / totalweight;
            }
            astock.buyprice = astock.price;
            astock.count = amount / astock.buyprice;
            astock.buydate = date;
            mystocks.add(astock);
            totalamount += amount;
        }
        capital.amount -= totalamount;
    }

    private int update(List<String> stockDates, Map<String, List<List<Double>>> categoryValueMap, Capital capital,
            List<Astock> mystocks, int indexOffset) {
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
                    if (valNow > item.price) {
                        up++;
                    }
                    item.price = valNow;
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
            List<IncDecItem> myincs, int indexOffset) {
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
                    astock.price = valNow;
                    astock.weight = item.getScore();
                    newbuys.add(astock);
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
