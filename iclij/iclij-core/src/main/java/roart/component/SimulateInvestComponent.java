package roart.component;

import java.text.ParseException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.stream.Collectors;

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
import roart.component.model.ComponentData;
import roart.constants.IclijConstants;
import roart.db.IclijDbDao;
import roart.evolution.config.EvolutionConfig;
import roart.iclij.config.IclijConfig;
import roart.iclij.config.MLConfigs;
import roart.iclij.config.Market;
import roart.iclij.filter.Memories;
import roart.iclij.model.IncDecItem;
import roart.iclij.model.MLMetricsItem;
import roart.iclij.model.MemoryItem;
import roart.iclij.model.Parameters;
import roart.iclij.model.Trend;
import roart.iclij.model.action.SimulateInvestActionData;
import roart.iclij.util.MiscUtil;
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

        double resultavg = 1;
        int beatavg = 0;
        int runs = 0;
        int findTimes = 4;
        boolean useReliability = false;
        double reliability = 0.75;
        boolean useMldate = true;
        
        List<String> stockDates = param.getService().getDates(market.getConfig().getMarket());
        int verificationdays = param.getInput().getConfig().verificationDays();
        int findTime = market.getConfig().getFindtime();

        //ComponentData componentData = component.improve2(action, param, market, profitdata, null, buy, subcomponent, parameters, mlTests);
        Map<String, List<List<Double>>> categoryValueMap = param.getCategoryValueMap();
        {
            String period = "1w";
            List<MetaItem> metas = param.getService().getMetas();
            MetaItem meta = new MetaUtil().findMeta(metas, market.getConfig().getMarket());
            List<String> categories = new MetaUtil().getCategories(meta);
            aMap.put(ConfigConstants.MACHINELEARNING, false);
            aMap.put(ConfigConstants.AGGREGATORS, false);
            aMap.put(ConfigConstants.INDICATORS, true);
            aMap.put(ConfigConstants.INDICATORSMACD, true);
            aMap.put(ConfigConstants.MISCTHRESHOLD, null);        
            aMap.put(ConfigConstants.MISCMYTABLEDAYS, 0);
            aMap.put(ConfigConstants.MISCMYDAYS, 0);
            Integer cat = new MetaUtil().getCategory(meta, period);
            Map<String, Object> resultMaps = param.getResultMap(""+ cat, aMap);
            int jj = 0;
        }
        LocalDate investStart = null;
        LocalDate investEnd = null;
        String mldate = null;
        if (useMldate) {
            mldate = ((SimulateInvestActionData) action.getActionData()).getMlDate(market, stockDates);
        } else {
            
        }
        try {
            investStart = TimeUtil.convertDate(mldate);
        } catch (ParseException e1) {
            log.error(Constants.EXCEPTION);
        }
        List<MemoryItem> allMemories = null;
        try {
            allMemories = IclijDbDao.getAllMemories(market.getConfig().getMarket(), IclijConstants.IMPROVEABOVEBELOW, PipelineConstants.ABOVEBELOW, null, null, investStart, investEnd);
            // also filter on params
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        List<IncDecItem> allIncDecs = null;
        try {
            allIncDecs = IclijDbDao.getAllIncDecs(market.getConfig().getMarket(), investStart, investEnd, null);
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        List<String> parametersList = new MiscUtil().getParameters(allIncDecs);
        for (String aParameter : parametersList) {
            Parameters realParameters = JsonUtil.convert(aParameter, Parameters.class);
            if (realParameters != null && realParameters.getThreshold() != 1.0) {
                continue;
            }
            Capital capital = new Capital();
            capital.amount = 1;
            int buytop = 5;
            boolean buyequal = true;
            List<Astock> mystocks = new ArrayList<>();
            List<Astock> stockhistory = new ArrayList<>();
            
            LocalDate date = investStart;
            int prevIndexOffset = 0;
            date = TimeUtil.getEqualBefore(stockDates, date);
            while (date.isBefore(param.getFutureDate())) {
                date = TimeUtil.getBackEqualBefore2(date, 0 /* findTime */, stockDates);
                String datestring = TimeUtil.convertDate2(date);
                int indexOffset = stockDates.size() - 1 - TimeUtil.getIndexEqualAfter(stockDates, datestring);
                
                LocalDate oldDate = date.minusDays(verificationdays + findTime);
                List<MemoryItem> memories = new MiscUtil().getCurrentMemories(oldDate, allMemories, market, findTime * findTimes, false);
                double myreliability = getReliability(memories, true);

                //LocalDate prevDate = date.minusDays(market.getConfig().getFindtime());
                List<IncDecItem> incdecs = new MiscUtil().getCurrentIncDecs(date, allIncDecs, market, market.getConfig().getFindtime(), false);
                List<IncDecItem> incdecsP = new MiscUtil().getCurrentIncDecs(incdecs, aParameter);              

                double scoreFilter = 0;
                double score = 0;
                Pair<Long, Integer>[] scores = null;
                long scoreSize = 0;

                // get recommendations

                double myavg = increase(capital, buytop, mystocks, stockDates, indexOffset, findTime, categoryValueMap, prevIndexOffset);
                update(stockDates, categoryValueMap, capital, mystocks, indexOffset);

                if (!useReliability || myreliability < reliability) {
                    List<IncDecItem> myincdecs = incdecsP;
                    List<IncDecItem> myincs = myincdecs.stream().filter(m1 -> m1.isIncrease()).collect(Collectors.toList());
                    List<IncDecItem> mydecs = myincdecs.stream().filter(m2 -> !m2.isIncrease()).collect(Collectors.toList());
                    List<IncDecItem> mylocals = new MiscUtil().getIncDecLocals(myincdecs);

                    myincs = new MiscUtil().mergeList(myincs, true);
                    mydecs = new MiscUtil().mergeList(mydecs, true);
                    List<IncDecItem> myincdec = new MiscUtil().moveAndGetCommon(myincs, mydecs, true);

                    Comparator<IncDecItem> incDecComparator = (IncDecItem comp1, IncDecItem comp2) -> comp2.getScore().compareTo(comp1.getScore());

                    myincs.sort(incDecComparator);   
                    mydecs.sort(incDecComparator);   

                    int subListSize = Math.min(buytop, myincs.size());
                    myincs = myincs.subList(0, subListSize);


                    List<Astock> newbuys = getBuyList(stockDates, categoryValueMap, myincs, indexOffset);
                    List<Astock> sells = getSellList(mystocks, newbuys);

                    mystocks.removeAll(sells);

                    sell(stockDates, categoryValueMap, capital, sells, stockhistory, indexOffset, date);

                    buy(capital, buytop, mystocks, newbuys, buyequal, date);
                }

                Capital sum = new Capital();
                for (Astock astock : mystocks) {
                    sum.amount += astock.count * astock.price;
                }

                // remember reliability
                // remember trend

                Trend trend = new TrendUtil().getTrend(findTime, null /*TimeUtil.convertDate2(olddate)*/, indexOffset, stockDates /*, findTime*/, param, market, categoryValueMap);
                log.info("Trend {}", trend);
                
                resultavg *= trend.incAverage;

                runs++;
                if (myavg > trend.incAverage) {
                    beatavg++;
                }
                
                //AboveBelowGene gene = new AboveBelowGene();
                //AboveBelowChromosome chromosome = new AboveBelowChromosome(size);
                //action, new ArrayList<>(), param, profitdata, market, null, component.getPipeline(), buy, subcomponent, parameters, gene, mlTests);            

                MemoryItem memory = new MemoryItem();
                if (true || score < market.getFilter().getConfidence()) {
                    //ComponentData componentData = null; //component.improve(action, param, chromosome, subcomponent, new AboveBelowChromosomeWinner(aParameter, compsub), null, fit);
                    Map<String, Object> updateMap = componentData.getUpdateMap();
                    if (updateMap != null) {
                        param.getUpdateMap().putAll(updateMap);
                    }
                    memory.setDescription((String) updateMap.get(aParameter));
                    //List<Double> list = new ArrayList<>(param.getScoreMap().values());
                    //memory.setLearnConfidence(list.get(0));
                }
                memory.setAction(action.getName());
                memory.setMarket(market.getConfig().getMarket());
                memory.setDate(param.getBaseDate());
                memory.setRecord(LocalDate.now());
                memory.setUsedsec(param.getUsedsec());
                memory.setFuturedays(param.getFuturedays());
                memory.setFuturedate(param.getFutureDate());
                memory.setComponent(getPipeline());
                memory.setCategory(param.getCategoryTitle());
                //memory.setSubcomponent(meta.get(ResultMetaConstants.MLNAME) + " " + meta.get(ResultMetaConstants.MODELNAME));
                //memory.setDescription(getShort((String) meta.get(ResultMetaConstants.MLNAME)) + withComma(getShort((String) meta.get(ResultMetaConstants.MODELNAME))) + withComma(meta.get(ResultMetaConstants.SUBTYPE)) + withComma(meta.get(ResultMetaConstants.SUBSUBTYPE)));
                memory.setDescription("" + trend);
                memory.setParameters(aParameter);
                memory.setConfidence(score);
                memory.setSize(scoreSize);
                //memory.setAbovepositives((Long) scores[0].getLeft());
                //memory.setAbovesize((long) scores[0].getRight()); 
                //memory.setBelowpositives((Long) scores[1].getLeft());
                //memory.setBelowsize((long) scores[1].getRight()); 
                memory.setTestaccuracy(scoreFilter);
                if (true || param.isDoSave()) {
                    try {
                        //memory.save();
                    } catch (Exception e) {
                        log.error(Constants.EXCEPTION);
                    }
                }
                date = date.plusDays(findTime);
                prevIndexOffset = indexOffset;
                //memoryList.add(memory);
            }
            Capital sum = new Capital();
            for (Astock astock : mystocks) {
                sum.amount += astock.count * astock.price;
            }
            Double score = sum.amount;
            Map<String, Double> scoreMap = new HashMap<>();
            scoreMap.put("" + score, score);
            param.setScoreMap(scoreMap);
        }

        //componentData.setFuturedays(0);

        //handle2(action, market, componentData, profitdata, positions, evolve, aMap, subcomponent, mlmarket, parameters);
        return componentData;
    }

    public ComponentData handle3(MarketAction action, Market market, ComponentData param, ProfitData profitdata,
            Memories positions, boolean evolve, Map<String, Object> aMap, String subcomponent, String mlmarket,
            Parameters parameters) {
        ComponentData componentData = new ComponentData(param);

        double resultavg = 1;
        int beatavg = 0;
        int runs = 0;
        int findTimes = 4;
        boolean useReliability = false;
        double reliability = 0.75;
        boolean useMldate = true;
        
        String period = "1w";
        // for improve evolver
        List<MetaItem> metas = param.getService().getMetas();
        MetaItem meta = new MetaUtil().findMeta(metas, market.getConfig().getMarket());
        List<String> categories = new MetaUtil().getCategories(meta);
       
        List<String> stockDates = param.getService().getDates(market.getConfig().getMarket());
        int verificationdays = param.getInput().getConfig().verificationDays();
        int findTime = market.getConfig().getFindtime();

        //ComponentData componentData = component.improve2(action, param, market, profitdata, null, buy, subcomponent, parameters, mlTests);
        Map<String, List<List<Double>>> categoryValueMap = param.getCategoryValueMap();
        Map<String, Object> aMap2 = new HashMap<>();
        // don't need these both here and in getevolveml?
        /*
        aMap.put(ConfigConstants.MACHINELEARNING, false);
        aMap.put(ConfigConstants.AGGREGATORS, false);
        aMap.put(ConfigConstants.INDICATORS, false);
        aMap.put(ConfigConstants.MISCTHRESHOLD, null);        
        aMap.put(ConfigConstants.MISCMYTABLEDAYS, 0);
        aMap.put(ConfigConstants.MISCMYDAYS, 0);
        Integer cat = new MetaUtil().getCategory(meta, period);
        Map<String, Object> resultMaps = param.getResultMap(""+ cat, aMap);
        */
        //Map<String, List<Double>> categoryValueMap2 = (Map<String, List<Double>>) resultMaps;
        LocalDate investStart = null;
        LocalDate investEnd = null;
        String mldate = null;
        if (useMldate) {
            mldate = ((SimulateInvestActionData) action.getActionData()).getMlDate(market, stockDates);
        } else {
            
        }
        try {
            investStart = TimeUtil.convertDate(mldate);
        } catch (ParseException e1) {
            log.error(Constants.EXCEPTION);
        }
        List<MemoryItem> allMemories = null;
        try {
            allMemories = IclijDbDao.getAllMemories(market.getConfig().getMarket(), IclijConstants.IMPROVEABOVEBELOW, PipelineConstants.ABOVEBELOW, null, null, investStart, investEnd);
            // also filter on params
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        List<IncDecItem> allIncDecs = null;
        try {
            allIncDecs = IclijDbDao.getAllIncDecs(market.getConfig().getMarket(), investStart, investEnd, null);
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        List<String> parametersList = new MiscUtil().getParameters(allIncDecs);
        for (String aParameter : parametersList) {
            Parameters realParameters = JsonUtil.convert(aParameter, Parameters.class);
            if (realParameters != null && realParameters.getThreshold() != null) {
                continue;
            }
            Capital capital = new Capital();
            capital.amount = 1;
            int buytop = 5;
            boolean buyequal = true;
            List<Astock> mystocks = new ArrayList<>();
            List<Astock> stockhistory = new ArrayList<>();
            
            LocalDate date = investStart;
            int prevIndexOffset = 0;
            date = TimeUtil.getEqualBefore(stockDates, date);
            while (date.isBefore(param.getFutureDate())) {
                date = TimeUtil.getBackEqualBefore2(date, findTime, stockDates);
                String datestring = TimeUtil.convertDate2(date);
                int indexOffset = stockDates.size() - 1 - TimeUtil.getIndexEqualAfter(stockDates, datestring);
                
                LocalDate oldDate = date.minusDays(verificationdays + findTime);
                List<MemoryItem> memories = new MiscUtil().getCurrentMemories(oldDate, allMemories, market, findTime * findTimes, false);
                double myreliability = getReliability(memories, true);


                double scoreFilter = 0;
                double score = 0;
                Pair<Long, Integer>[] scores = null;
                long scoreSize = 0;

                // get recommendations

                double myavg = increase(capital, buytop, mystocks, stockDates, indexOffset, findTime, categoryValueMap, prevIndexOffset);
                update(stockDates, categoryValueMap, capital, mystocks, indexOffset);

                if (!useReliability || myreliability < reliability) {

                    List myincs = null;
                    
                    int subListSize = Math.min(buytop, myincs.size());

                    List<Astock> newbuys = getBuyList2(stockDates, categoryValueMap, myincs, indexOffset);
                    List<Astock> sells = getSellList(mystocks, newbuys);

                    mystocks.removeAll(sells);

                    sell(stockDates, categoryValueMap, capital, sells, stockhistory, indexOffset, date);

                    buy(capital, buytop, mystocks, newbuys, buyequal, date);
                }

                Capital sum = new Capital();
                for (Astock astock : mystocks) {
                    sum.amount += astock.count * astock.price;
                }

                // remember reliability
                // remember trend

                Trend trend = new TrendUtil().getTrend(findTime, null /*TimeUtil.convertDate2(olddate)*/, indexOffset, stockDates /*, findTime*/, param, market, categoryValueMap);
                log.info("Trend {}", trend);
                
                resultavg *= trend.incAverage;

                runs++;
                if (myavg > trend.incAverage) {
                    beatavg++;
                }
                
                //AboveBelowGene gene = new AboveBelowGene();
                //AboveBelowChromosome chromosome = new AboveBelowChromosome(size);
                //action, new ArrayList<>(), param, profitdata, market, null, component.getPipeline(), buy, subcomponent, parameters, gene, mlTests);            

                MemoryItem memory = new MemoryItem();
                if (true || score < market.getFilter().getConfidence()) {
                    //ComponentData componentData = null; //component.improve(action, param, chromosome, subcomponent, new AboveBelowChromosomeWinner(aParameter, compsub), null, fit);
                    Map<String, Object> updateMap = componentData.getUpdateMap();
                    if (updateMap != null) {
                        param.getUpdateMap().putAll(updateMap);
                    }
                    memory.setDescription((String) updateMap.get(aParameter));
                    //List<Double> list = new ArrayList<>(param.getScoreMap().values());
                    //memory.setLearnConfidence(list.get(0));
                }
                memory.setAction(action.getName());
                memory.setMarket(market.getConfig().getMarket());
                memory.setDate(param.getBaseDate());
                memory.setRecord(LocalDate.now());
                memory.setUsedsec(param.getUsedsec());
                memory.setFuturedays(param.getFuturedays());
                memory.setFuturedate(param.getFutureDate());
                memory.setComponent(getPipeline());
                memory.setCategory(param.getCategoryTitle());
                //memory.setSubcomponent(meta.get(ResultMetaConstants.MLNAME) + " " + meta.get(ResultMetaConstants.MODELNAME));
                //memory.setDescription(getShort((String) meta.get(ResultMetaConstants.MLNAME)) + withComma(getShort((String) meta.get(ResultMetaConstants.MODELNAME))) + withComma(meta.get(ResultMetaConstants.SUBTYPE)) + withComma(meta.get(ResultMetaConstants.SUBSUBTYPE)));
                memory.setDescription("" + trend);
                memory.setParameters(aParameter);
                memory.setConfidence(score);
                memory.setSize(scoreSize);
                //memory.setAbovepositives((Long) scores[0].getLeft());
                //memory.setAbovesize((long) scores[0].getRight()); 
                //memory.setBelowpositives((Long) scores[1].getLeft());
                //memory.setBelowsize((long) scores[1].getRight()); 
                memory.setTestaccuracy(scoreFilter);
                if (true || param.isDoSave()) {
                    try {
                        memory.save();
                    } catch (Exception e) {
                        log.error(Constants.EXCEPTION);
                    }
                }
                date = date.plusDays(findTime);
                prevIndexOffset = indexOffset;
                //memoryList.add(memory);
            }
            Capital sum = new Capital();
            for (Astock astock : mystocks) {
                sum.amount += astock.count * astock.price;
            }
       }

        //componentData.setFuturedays(0);

        //handle2(action, market, componentData, profitdata, positions, evolve, aMap, subcomponent, mlmarket, parameters);
        return componentData;
    }

    private List<Astock> getBuyList2(List<String> stockDates, Map<String, List<List<Double>>> categoryValueMap, List myincs,
            int indexOffset) {
        // TODO Auto-generated method stub
        return null;
    }

    public ComponentData handle4(MarketAction action, Market market, ComponentData param, ProfitData profitdata,
            Memories positions, boolean evolve, Map<String, Object> aMap, String subcomponent, String mlmarket,
            Parameters parameters) {
        ComponentData componentData = new ComponentData(param);

        double resultavg = 1;
        int beatavg = 0;
        int runs = 0;
        int findTimes = 4;
        boolean useReliability = false;
        double reliability = 0.75;
        boolean useMldate = true;
        
        String period = "1w";
        // for improve evolver
        List<MetaItem> metas = param.getService().getMetas();
        MetaItem meta = new MetaUtil().findMeta(metas, market.getConfig().getMarket());
        List<String> categories = new MetaUtil().getCategories(meta);
       
        List<String> stockDates = param.getService().getDates(market.getConfig().getMarket());
        int verificationdays = param.getInput().getConfig().verificationDays();
        int findTime = market.getConfig().getFindtime();

        //ComponentData componentData = component.improve2(action, param, market, profitdata, null, buy, subcomponent, parameters, mlTests);
        Map<String, List<List<Double>>> categoryValueMap = param.getCategoryValueMap();
        // don't need these both here and in getevolveml?
        aMap.put(ConfigConstants.MACHINELEARNING, false);
        aMap.put(ConfigConstants.AGGREGATORS, false);
        aMap.put(ConfigConstants.INDICATORS, true);
        aMap.put(ConfigConstants.INDICATORSMACD, true);
        aMap.put(ConfigConstants.MISCTHRESHOLD, null);        
        aMap.put(ConfigConstants.MISCMYTABLEDAYS, 0);
        aMap.put(ConfigConstants.MISCMYDAYS, 0);
        Integer cat = new MetaUtil().getCategory(meta, period);
        Map<String, Object> resultMaps = param.getResultMap(""+ cat, aMap);
        LocalDate investStart = null;
        LocalDate investEnd = null;
        String mldate = null;
        if (useMldate) {
            mldate = ((SimulateInvestActionData) action.getActionData()).getMlDate(market, stockDates);
        } else {
            
        }
        try {
            investStart = TimeUtil.convertDate(mldate);
        } catch (ParseException e1) {
            log.error(Constants.EXCEPTION);
        }
        List<MemoryItem> allMemories = null;
        try {
            allMemories = IclijDbDao.getAllMemories(market.getConfig().getMarket(), IclijConstants.IMPROVEABOVEBELOW, PipelineConstants.ABOVEBELOW, null, null, investStart, investEnd);
            // also filter on params
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        List<IncDecItem> allIncDecs = null;
        try {
            allIncDecs = IclijDbDao.getAllIncDecs(market.getConfig().getMarket(), investStart, investEnd, null);
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        List<String> parametersList = new MiscUtil().getParameters(allIncDecs);
        for (String aParameter : parametersList) {
            Parameters realParameters = JsonUtil.convert(aParameter, Parameters.class);
            if (realParameters != null && realParameters.getThreshold() != null) {
                continue;
            }
            Capital capital = new Capital();
            capital.amount = 1;
            int buytop = 5;
            boolean buyequal = true;
            List<Astock> mystocks = new ArrayList<>();
            List<Astock> stockhistory = new ArrayList<>();
            
            LocalDate date = investStart;
            int prevIndexOffset = 0;
            date = TimeUtil.getEqualBefore(stockDates, date);
            while (date.isBefore(param.getFutureDate())) {
                date = TimeUtil.getBackEqualBefore2(date, findTime, stockDates);
                String datestring = TimeUtil.convertDate2(date);
                int indexOffset = stockDates.size() - 1 - TimeUtil.getIndexEqualAfter(stockDates, datestring);
                
                LocalDate oldDate = date.minusDays(verificationdays + findTime);
                List<MemoryItem> memories = new MiscUtil().getCurrentMemories(oldDate, allMemories, market, findTime * findTimes, false);
                double myreliability = getReliability(memories, true);

                //LocalDate prevDate = date.minusDays(market.getConfig().getFindtime());

                double scoreFilter = 0;
                double score = 0;
                Pair<Long, Integer>[] scores = null;
                long scoreSize = 0;

                // get recommendations

                double myavg = increase(capital, buytop, mystocks, stockDates, indexOffset, findTime, categoryValueMap, prevIndexOffset);
                update(stockDates, categoryValueMap, capital, mystocks, indexOffset);

                if (!useReliability || myreliability < reliability) {

                    List myincs = null;

                    int subListSize = Math.min(buytop, myincs.size());

                    List<Astock> newbuys = getBuyList3(stockDates, resultMaps, myincs, indexOffset);
                    List<Astock> sells = getSellList(mystocks, newbuys);

                    mystocks.removeAll(sells);

                    sell(stockDates, categoryValueMap, capital, sells, stockhistory, indexOffset, date);

                    buy(capital, buytop, mystocks, newbuys, buyequal, date);
                }

                Capital sum = new Capital();
                for (Astock astock : mystocks) {
                    sum.amount += astock.count * astock.price;
                }

                // remember reliability
                // remember trend

                Trend trend = new TrendUtil().getTrend(findTime, null /*TimeUtil.convertDate2(olddate)*/, indexOffset, stockDates /*, findTime*/, param, market, categoryValueMap);
                log.info("Trend {}", trend);
                
                resultavg *= trend.incAverage;

                runs++;
                if (myavg > trend.incAverage) {
                    beatavg++;
                }
                
                //AboveBelowGene gene = new AboveBelowGene();
                //AboveBelowChromosome chromosome = new AboveBelowChromosome(size);
                //action, new ArrayList<>(), param, profitdata, market, null, component.getPipeline(), buy, subcomponent, parameters, gene, mlTests);            

                MemoryItem memory = new MemoryItem();
                if (true || score < market.getFilter().getConfidence()) {
                    //ComponentData componentData = null; //component.improve(action, param, chromosome, subcomponent, new AboveBelowChromosomeWinner(aParameter, compsub), null, fit);
                    Map<String, Object> updateMap = componentData.getUpdateMap();
                    if (updateMap != null) {
                        param.getUpdateMap().putAll(updateMap);
                    }
                    memory.setDescription((String) updateMap.get(aParameter));
                    //List<Double> list = new ArrayList<>(param.getScoreMap().values());
                    //memory.setLearnConfidence(list.get(0));
                }
                memory.setAction(action.getName());
                memory.setMarket(market.getConfig().getMarket());
                memory.setDate(param.getBaseDate());
                memory.setRecord(LocalDate.now());
                memory.setUsedsec(param.getUsedsec());
                memory.setFuturedays(param.getFuturedays());
                memory.setFuturedate(param.getFutureDate());
                memory.setComponent(getPipeline());
                memory.setCategory(param.getCategoryTitle());
                //memory.setSubcomponent(meta.get(ResultMetaConstants.MLNAME) + " " + meta.get(ResultMetaConstants.MODELNAME));
                //memory.setDescription(getShort((String) meta.get(ResultMetaConstants.MLNAME)) + withComma(getShort((String) meta.get(ResultMetaConstants.MODELNAME))) + withComma(meta.get(ResultMetaConstants.SUBTYPE)) + withComma(meta.get(ResultMetaConstants.SUBSUBTYPE)));
                memory.setDescription("" + trend);
                memory.setParameters(aParameter);
                memory.setConfidence(score);
                memory.setSize(scoreSize);
                //memory.setAbovepositives((Long) scores[0].getLeft());
                //memory.setAbovesize((long) scores[0].getRight()); 
                //memory.setBelowpositives((Long) scores[1].getLeft());
                //memory.setBelowsize((long) scores[1].getRight()); 
                memory.setTestaccuracy(scoreFilter);
                if (true || param.isDoSave()) {
                    try {
                        //memory.save();
                    } catch (Exception e) {
                        log.error(Constants.EXCEPTION);
                    }
                }
                date = date.plusDays(findTime);
                prevIndexOffset = indexOffset;
                //memoryList.add(memory);
            }
            Capital sum = new Capital();
            for (Astock astock : mystocks) {
                sum.amount += astock.count * astock.price;
            }
       }

        //componentData.setFuturedays(0);

        //handle2(action, market, componentData, profitdata, positions, evolve, aMap, subcomponent, mlmarket, parameters);
        return componentData;
    }

    private List<Astock> getBuyList3(List<String> stockDates, Map<String, Object> resultMaps, List myincs,
            int indexOffset) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ComponentData improve(MarketAction action, ComponentData param, Market market, ProfitData profitdata,
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
        return new Object[] { componentparam.getScoreMap().get("score") };
    }
    
    private double getReliability(List<MemoryItem> memories, Boolean above) {
        int abovepositives = 0;
        int abovesize = 0;
        int belowpositives = 0;
        int belowsize = 0;
        for (MemoryItem memory : memories) {
            if (memory.getAbovepositives() == null) {
                int jj = 0;
            }
            abovepositives += memory.getAbovepositives();
            abovesize += memory.getAbovesize();
            belowpositives += memory.getBelowpositives();
            belowsize += memory.getBelowsize();
        }
        double positives = 0;
        int size = 0;
        if (above == null || above == true) {
            positives += abovepositives;
            size += abovesize;
        }
        if (above == null || above == false) {
            positives += belowpositives;
            size += belowsize;
        }
        if (size > 0) {
            return positives / size;
        }
        return 0;
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
            log.debug("Sizes {} {}", stockDates.size(), mainList.size());
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

    private void buy(Capital capital, int buytop, List<Astock> mystocks, List<Astock> newbuys, boolean buyequal, LocalDate date) {
        int buys = buytop - mystocks.size();
        buys = Math.min(buys, newbuys.size());
        
        double totalweight = 0;
        if (!buyequal) {
            for (int i = 0; i < buys; i++) {
                totalweight += newbuys.get(i).weight;
            }
        }
        double totalamount = 0;
        for (int i = 0; i < buys; i++) {
            Astock astock = newbuys.get(i);
            double amount = 0;
            if (buyequal) {
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

    private void update(List<String> stockDates, Map<String, List<List<Double>>> categoryValueMap, Capital capital,
            List<Astock> mystocks, int indexOffset) {
        for (Astock item : mystocks) {
            String id = item.id;
            List<List<Double>> resultList = categoryValueMap.get(id);
            if (resultList == null || resultList.isEmpty()) {
                continue;
            }
            List<Double> mainList = resultList.get(0);
            log.debug("Sizes {} {}", stockDates.size(), mainList.size());
            if (mainList != null) {
                Double valNow = mainList.get(mainList.size() - 1 - indexOffset);
                if (valNow != null) {
                    item.price = valNow;
                }
            }
        }
    }

    private void sell(List<String> stockDates, Map<String, List<List<Double>>> categoryValueMap, Capital capital,
            List<Astock> sells, List<Astock> stockhistory, int indexOffset, LocalDate date) {
        for (Astock item : sells) {
            String id = item.id;
            List<List<Double>> resultList = categoryValueMap.get(id);
            if (resultList == null || resultList.isEmpty()) {
                continue;
            }
            List<Double> mainList = resultList.get(0);
            log.debug("Sizes {} {}", stockDates.size(), mainList.size());
            if (mainList != null) {
                Double valNow = mainList.get(mainList.size() - 1 - indexOffset);
                if (valNow != null) {
                    item.sellprice = valNow;
                    item.selldate = date;
                    stockhistory.add(item);
                    capital.amount += item.count * item.sellprice;
                }
            } else {
                // put back if unknown
                //mystocks.add(item);
            }
        }
    }

    private List<Astock> getSellList(List<Astock> mystocks, List<Astock> newbuys) {
        List<String> myincids = newbuys.stream().map(Astock::getId).collect(Collectors.toList());
            
             List<Astock> sells = mystocks.stream().filter(e -> myincids.contains(e.id)).collect(Collectors.toList());
        return sells;
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
            log.debug("Sizes {} {}", stockDates.size(), mainList.size());
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
}
