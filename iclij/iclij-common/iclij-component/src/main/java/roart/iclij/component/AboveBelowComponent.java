package roart.iclij.component;

import java.text.ParseException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;

import roart.common.constants.Constants;
import roart.common.constants.EvolveConstants;
import roart.common.pipeline.PipelineConstants;
import roart.common.util.JsonUtil;
import roart.common.util.TimeUtil;
import roart.component.model.ComponentData;
import roart.constants.IclijConstants;
import roart.db.IclijDbDao;
import roart.evolution.config.EvolutionConfig;
import roart.evolution.fitness.Fitness;
import roart.iclij.config.IclijConfig;
import roart.iclij.config.MLConfigs;
import roart.iclij.config.Market;
import roart.iclij.filter.Memories;
import roart.iclij.model.IncDecItem;
import roart.iclij.model.MLMetricsItem;
import roart.iclij.model.MemoryItem;
import roart.iclij.model.Parameters;
import roart.iclij.model.Trend;
import roart.iclij.model.WebData;
import roart.iclij.model.action.MarketActionData;
import roart.iclij.util.MarketUtil;
import roart.iclij.util.MiscUtil;
import roart.iclij.verifyprofit.TrendUtil;
import roart.iclij.verifyprofit.VerifyProfitUtil;
import roart.service.model.ProfitData;
import roart.service.model.ProfitInputData;

public class AboveBelowComponent extends ComponentML {

    private static final int AVERAGE_SIZE = 5;

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
        long time0 = System.currentTimeMillis();
        ComponentData componentData = new ComponentData(param);

        WebData myData = new WebData();
        List<String> stockDates = param.getService().getDates(market.getConfig().getMarket());
        int verificationdays = param.getInput().getConfig().verificationDays();

        //ComponentData componentData = component.improve2(action, param, market, profitdata, null, buy, subcomponent, parameters, mlTests);
        List<IncDecItem> allIncDecs = null;
        LocalDate date = param.getFutureDate();
        date = TimeUtil.getBackEqualBefore2(date, verificationdays, stockDates);
        LocalDate prevDate = date.minusDays(market.getConfig().getFindtime());
        try {
            allIncDecs = IclijDbDao.getAllIncDecs(market.getConfig().getMarket(), prevDate, date, null);
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        List<IncDecItem> incdecs = allIncDecs; // new MiscUtil().getCurrentIncDecs(date, allIncDecs, market, market.getConfig().getFindtime(), false);
        List<String> parametersList = new MiscUtil().getParameters(incdecs);
        for (String aParameter : parametersList) {
            List<IncDecItem> incdecsP = new MiscUtil().getCurrentIncDecs(incdecs, aParameter);              
            List<String> components = new ArrayList<>();
            List<String> subcomponents = new ArrayList<>();
            getComponentLists(incdecsP, components, subcomponents);
            Parameters realParameters = JsonUtil.convert(aParameter, Parameters.class);
	    Double threshold = realParameters.getThreshold();
            
            param.getAndSetCategoryValueMap();
            Map<String, List<List<Double>>> categoryValueMap = param.getCategoryValueMap();
            
            //FitnessAboveBelow fit = new FitnessAboveBelow(action, new ArrayList<>(), param, profitdata, market, null, this.getPipeline(), null, subcomponent, realParameters, null, incdecsP, components, subcomponents, stockDates);
            //FitnessAboveBelowCommon fitCommon = new FitnessAboveBelowCommon();
            MiscUtil fitCommon = new MiscUtil();
            
            double scoreFilter = 0;
            {
                Memories listComponent2 = new Memories(market);
                LocalDate olddate = prevDate.minusDays(((int) AVERAGE_SIZE) * action.getTime(market));
                ProfitInputData inputdata = new ProfitInputData();
                getListComponents(action, myData, param, null, realParameters, evolve, market, null, listComponent2, olddate, prevDate);

                List<IncDecItem> mylocals = new MiscUtil().getIncDecLocals(incdecsP);
                List<IncDecItem> allCurrentIncDecs = mylocals
                        .stream()
                        .filter(e -> !listComponent2.containsBelow(e.getComponent(), new ImmutablePair(e.getSubcomponent(), e.getLocalcomponent()), null, null, true))
                        .collect(Collectors.toList());
                List<IncDecItem> myincdecs = allCurrentIncDecs;
		if (threshold != 1.0) {
		    myincdecs = myincdecs
			.stream()
			.filter(e -> e.isIncrease() == threshold > 1.0)
			.collect(Collectors.toList());
		}
                Set<IncDecItem> myincs = myincdecs.stream().filter(m1 -> m1.isIncrease()).collect(Collectors.toSet());
                Set<IncDecItem> mydecs = myincdecs.stream().filter(m2 -> !m2.isIncrease()).collect(Collectors.toSet());

                myincs = new MiscUtil().mergeList(myincs, true);
                mydecs = new MiscUtil().mergeList(mydecs, true);
                Set<IncDecItem> myincdec = new MiscUtil().moveAndGetCommon(myincs, mydecs, true);
                short startoffset = new MarketUtil().getStartoffset(market);

                new VerifyProfitUtil().getVerifyProfit(verificationdays, param.getFutureDate(), myincs, mydecs, myincdec, startoffset, realParameters.getThreshold(), stockDates, categoryValueMap);
                
                scoreFilter = fitCommon.fitness(myincs, mydecs, myincdec, 0, null);


            }
            
            double score = 0;
            long scoreSize = 0;
            Pair<Long, Integer>[] scores = null;
            {
                List<IncDecItem> myincdecs = incdecsP;
		if (threshold != 1.0) {
		    myincdecs = myincdecs
			.stream()
			.filter(e -> e.isIncrease() == threshold > 1.0)
			.collect(Collectors.toList());
		}
                Set<IncDecItem> myincs = myincdecs.stream().filter(m1 -> m1.isIncrease()).collect(Collectors.toSet());
                Set<IncDecItem> mydecs = myincdecs.stream().filter(m2 -> !m2.isIncrease()).collect(Collectors.toSet());
                List<IncDecItem> mylocals = new MiscUtil().getIncDecLocals(myincdecs);

                myincs = new MiscUtil().mergeList(myincs, true);
                mydecs = new MiscUtil().mergeList(mydecs, true);
                Set<IncDecItem> myincdec = new MiscUtil().moveAndGetCommon(myincs, mydecs, true);
                short startoffset = new MarketUtil().getStartoffset(market);
                new VerifyProfitUtil().getVerifyProfit(verificationdays, param.getFutureDate(), myincs, mydecs, myincdec, startoffset, realParameters.getThreshold(), stockDates, param.getCategoryValueMap());
                score = fitCommon.fitness(myincs, mydecs, myincdec, 0, null);
                scoreSize = myincs.size() + mydecs.size() + myincdec.size();
                scores = fitCommon.fitness2(myincs, mydecs, myincdec, 0, null);
                scoreSize = (long) scores[0].getRight() + scores[1].getRight();
                {
                    Memories listComponentMap = new Memories(market);
                    //LocalDate prevdate = action.getPrevDate(param, market);
                    LocalDate prevdate = param.getInput().getEnddate();
                    prevdate = prevdate.minusDays(action.getTime(market));
                    LocalDate olddate = prevdate.minusDays(((int) AVERAGE_SIZE) * action.getTime(market));
                    
                    // making separate memories the components and subcomponents
                    
                    getListComponentsNew(myData, param, realParameters, evolve, market, listComponentMap, prevdate, olddate, mylocals, action);

                    // todo make memories of confidence
                    //Set<Triple<String, String, String>> keys = i.getAboveListMap().keySet();
                   // System.out.println(keys);
                    // memory.save?
                }
            }
            
            
            short startoffset = new MarketUtil().getStartoffset(market);
            int findTime = market.getConfig().getFindtime();
            Trend trend = new TrendUtil().getTrend(verificationdays, null , startoffset, stockDates, param, market, categoryValueMap);
            log.info("Trend {}", trend);                
            
            MemoryItem memory = new MemoryItem();
            if (true || score < market.getFilter().getConfidence()) {
                //int ga = param.getInput().getConfig().getEvolveGA();
                //Evolve evolve2 = AboveBelowEvolveFactory.factory(ga);
                String evolutionConfigString = param.getInput().getConfig().getImproveAbovebelowEvolutionConfig();
                EvolutionConfig evolutionConfig = JsonUtil.convert(evolutionConfigString, EvolutionConfig.class);

                Map<String, Object> confMap = new HashMap<>();
                List<String> confList = new ArrayList<>();
                Boolean buy = null;
                List<MLMetricsItem> mlTests = null;
                //componentData = evolve2.evolve(action, param, market, profitdata, buy, subcomponent, parameters, mlTests , confMap , evolutionConfig, this.getPipeline(), this, confList );
                //componentData = this.improve(action, param, market, profitdata, listComponent, evolve, subcomponent, parameters, false, mlTests, fitness, action.getParent() != null);
                Fitness fitness = null;
                componentData = this.improve(action, param, market, profitdata, positions, evolve, subcomponent, parameters, false, mlTests, fitness, action.doSaveTiming());
           
                componentData.getResultMap().put(EvolveConstants.DEFAULT, score);
                Map<String, Object> updateMap = componentData.getUpdateMap();
                if (updateMap != null) {
                    param.getUpdateMap().putAll(updateMap);
                }
                memory.setDescription((String) updateMap.get(aParameter));
                List<Double> list = new ArrayList<>(param.getScoreMap().values());
                memory.setLearnConfidence(list.get(0));
                componentData.getResultMap().put("learned", list.get(0));
                componentData.getResultMap().put(EvolveConstants.DATE, TimeUtil.convertDate2(param.getFutureDate()));
            }
            memory.setAction(action.getName());
            memory.setMarket(market.getConfig().getMarket());
            memory.setDate(param.getBaseDate());
            memory.setRecord(LocalDate.now());
            memory.setUsedsec(param.getUsedsec());
            memory.setFuturedays(param.getFuturedays());
            memory.setFuturedate(param.getFutureDate());
            memory.setComponent(this.getPipeline());
            memory.setCategory(param.getCategoryTitle());
            //memory.setSubcomponent(meta.get(ResultMetaConstants.MLNAME) + " " + meta.get(ResultMetaConstants.MODELNAME));
            //memory.setDescription(getShort((String) meta.get(ResultMetaConstants.MLNAME)) + withComma(getShort((String) meta.get(ResultMetaConstants.MODELNAME))) + withComma(meta.get(ResultMetaConstants.SUBTYPE)) + withComma(meta.get(ResultMetaConstants.SUBSUBTYPE)));
            memory.setDescription("" + trend);
            memory.setParameters(aParameter);
            memory.setConfidence(score);
            memory.setSize(scoreSize);
            memory.setAbovepositives(scores[0].getLeft());
            memory.setAbovesize((long) scores[0].getRight()); 
            memory.setBelowpositives(scores[1].getLeft());
            memory.setBelowsize((long) scores[1].getRight()); 
            if (score < 0.9) {
            }
            memory.setTestaccuracy(scoreFilter);
            if (true || param.isDoSave()) {
                try {
                    memory.save();
                } catch (Exception e) {
                    log.error(Constants.EXCEPTION, e);
                }
            }
            //memoryList.add(memory);
        }
        //int futuredays = (int) param.getService().conf.getAggregatorsIndicatorFuturedays();
        //componentData.setFuturedays(0);

        //handle2(action, market, componentData, profitdata, positions, evolve, aMap, subcomponent, mlmarket, parameters);
        //Map resultMaps = param.getResultMap();
        //handleMLMeta(param, resultMaps);
        //Map<String, Object> resultMap = param.getResultMap();
        
        if (parametersList.isEmpty()) {
            saveTiming(param, true, time0, null, null, subcomponent, null, null, null, action.doSaveTiming());
        }

        return componentData;
    }

    @Override
    public ComponentData improve(MarketActionData action, ComponentData param, Market market, ProfitData profitdata,
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
        return PipelineConstants.ABOVEBELOW;
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

    private void getComponentLists(List<IncDecItem> incdecs, List<String> components, List<String> subcomponents) {
        Set<String> componentSet = new HashSet<>();
        Set<String> subcomponentSet = new HashSet<>();

        for (IncDecItem item : incdecs) {
            componentSet.add(item.getComponent());
            subcomponentSet.add(item.getSubcomponent());
        }
        components.addAll(componentSet);
        subcomponents.addAll(subcomponentSet);
    }
    
    private void getListComponentsNew(WebData myData, ComponentData param, Parameters parameters,
            Boolean evolve, Market market, Memories listComponentMap, LocalDate prevdate,
            LocalDate olddate, List<IncDecItem> mylocals, MarketActionData action) {
        ProfitInputData inputdata = null;
        /*
        List<MemoryItem> marketMemory = new MarketUtil().getMarketMemory(marketTime.market, getName(), marketTime.componentName, marketTime.subcomponent, JsonUtil.convert(marketTime.parameters), olddate, prevdate);
        if (marketMemory == null) {
            myData.setProfitData(new ProfitData());
        }
        marketMemory.addAll(myData.getMemoryItems());
        */
        
        List<String> stockDates = param.getService().getDates(market.getConfig().getMarket());
        int verificationdays = param.getInput().getConfig().verificationDays();
        /*
        List<IncDecItem> allIncDecs = null;
        try {
            allIncDecs = IclijDbDao.getAllIncDecs();
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        */
        
        LocalDate date = param.getInput().getEnddate();
        String aDate = TimeUtil.convertDate2(date);
        int index = TimeUtil.getIndexEqualBefore(stockDates, aDate);
        int indexoffset = index - verificationdays;
        if (indexoffset < 0) {
            return;
        }
        aDate = stockDates.get(indexoffset);
        try {
            date = TimeUtil.convertDate(aDate);
        } catch (ParseException e) {
            log.error(Constants.EXCEPTION, e);
        }
        /*
        List<IncDecItem> incdecs = new MiscUtil().getCurrentIncDecs(date, allIncDecs, market, AVERAGE_SIZE * market.getConfig().getFindtime());
        List<IncDecItem> incdecsP = new MiscUtil().getCurrentIncDecs(incdecs, JsonUtil.convert(parameters));              
        List<IncDecItem> incdecsL = new MiscUtil().getIncDecLocals(incdecsP);              
        */
        List<IncDecItem> incdecsL = mylocals;
        List<IncDecItem> myincs = mylocals.stream().filter(m1 -> m1.isIncrease()).collect(Collectors.toList());
        List<IncDecItem> mydecs = mylocals.stream().filter(m2 -> !m2.isIncrease()).collect(Collectors.toList());
        short startoffset = new MarketUtil().getStartoffset(market);
        new VerifyProfitUtil().getVerifyProfit(verificationdays, param.getFutureDate(), myincs, mydecs, new ArrayList<>(), startoffset, parameters.getThreshold(), stockDates, param.getCategoryValueMap());
        

        //List<MemoryItem> currentList = new MiscUtil().filterKeepRecent(marketMemory, prevdate, ((int) AVERAGE_SIZE) *.getTime(market));
        // or make a new object instead of the object array. use this as a pair
        //System.out.println(currentList.get(0).getRecord());
        Map<Triple<String, String, String>, List<IncDecItem>> listMap = new HashMap<>();
        // map subcat + posit -> list
        incdecsL.forEach(m -> new MiscUtil().listGetterAdder(listMap, new ImmutableTriple<String, String, String>(m.getComponent(), m.getSubcomponent(), m.getLocalcomponent()), m));
        filterMemoryListMapsWithConfidenceNew(market, listMap, param, parameters, action);        
        /*
        Map<String, List<Pair<String, String>>> listComponent = createComponentPositionListMap(inputdata.getListMap());
        Map<String, List<Pair<String, String>>> aboveListComponent = createComponentPositionListMap(inputdata.getAboveListMap());
        Map<String, List<Pair<String, String>>> belowListComponent = createComponentPositionListMap(inputdata.getBelowListMap());
        listComponentMap.put(null, listComponent);
        listComponentMap.put(true, aboveListComponent);
        listComponentMap.put(false, belowListComponent);
        return inputdata;
        */
    }

    private void filterMemoryListMapsWithConfidenceNew(Market market, Map<Triple<String, String, String>, List<IncDecItem>> listMap, ComponentData param, Parameters parameters, MarketActionData action) {
        Map<Triple<String, String, String>, List<MemoryItem>> okListMap = new HashMap<>();
        Map<Triple<String, String, String>, Double> okConfMap = new HashMap<>();
        Map<Triple<String, String, String>, List<MemoryItem>> aboveOkListMap = new HashMap<>();
        Map<Triple<String, String, String>, Double> aboveOkConfMap = new HashMap<>();
        Map<Triple<String, String, String>, List<MemoryItem>> belowOkListMap = new HashMap<>();
        Map<Triple<String, String, String>, Double> belowOkConfMap = new HashMap<>();
        for(Entry<Triple<String, String, String>, List<IncDecItem>> entry : listMap.entrySet()) {
            Triple<String, String, String> keys = entry.getKey();
            List<IncDecItem> incdecList = entry.getValue();
            //List<Double> confidences = memoryList.stream().map(MemoryItem::getConfidence).collect(Collectors.toList());
            //confidences = confidences.stream().filter(m -> m != null && !m.isNaN()).collect(Collectors.toList());
            //List<Double> aboveConfidenceList = new ArrayList<>();
            //List<Double> belowConfidenceList = new ArrayList<>();
            // TODO refactor
            MiscUtil FitnessAboveBelowCommon = new MiscUtil();
            Pair<Long, Integer> abovecnt = FitnessAboveBelowCommon.countsize(incdecList.stream().filter(e -> e.isIncrease()).collect(Collectors.toList()));
            Pair<Long, Integer> belowcnt = FitnessAboveBelowCommon.countsize(incdecList.stream().filter(e -> !e.isIncrease()).collect(Collectors.toList()));
            Pair<Long, Integer> cnt = FitnessAboveBelowCommon.countsize(incdecList);
            {
                MemoryItem memory = new MemoryItem();
                memory.setAction(action.getName());
                memory.setMarket(market.getConfig().getMarket());
                memory.setDate(incdecList.get(0).getDate());
                memory.setRecord(LocalDate.now());
                memory.setUsedsec(param.getUsedsec());
                memory.setFuturedays(param.getFuturedays());
                memory.setFuturedate(param.getFutureDate());
                memory.setComponent(keys.getLeft());
                memory.setSubcomponent(keys.getMiddle());
                memory.setLocalcomponent(keys.getRight());
                memory.setCategory(param.getCategoryTitle());
                /*
                List<IncDecItem> metalist = memoryList
                        .stream()
                        .filter(Objects::nonNull)
                        .filter(e -> e.getLocalcomponent() != null)
                        .collect(Collectors.toList());
                        */
                memory.setType("Confidence");
                memory.setParameters(JsonUtil.convert(parameters));
                if (cnt.getRight() != null) {
                    memory.setPositives(cnt.getLeft());
                    memory.setSize((long) cnt.getRight()); 
                    memory.setConfidence(((double) cnt.getLeft() / cnt.getRight()));
                }
                if (abovecnt.getRight() != null) {
                    memory.setAbovepositives(abovecnt.getLeft());
                    memory.setAbovesize((long) abovecnt.getRight()); 
                    //memory.setConfidence(((double) cnt.getLeft() / cnt.getRight()));
                }
                if (belowcnt.getRight() != null) {
                    memory.setBelowpositives(belowcnt.getLeft());
                    memory.setBelowsize((long) belowcnt.getRight()); 
                    //memory.setConfidence(((double) cnt.getLeft() / cnt.getRight()));
                }
                //memory.setSize((long) incIds.size());
                //List<Double> list = new ArrayList<>(param.getScoreMap().values());
                try {
                    memory.save();
                } catch (Exception e) {
                    log.error(Constants.EXCEPTION, e);
                }

            }
            Set<String> incIds = incdecList.stream().filter(e -> e.isIncrease()).map(IncDecItem::getId).collect(Collectors.toSet());
            Set<String> decIds = incdecList.stream().filter(e -> !e.isIncrease()).map(IncDecItem::getId).collect(Collectors.toSet());
            incIds.retainAll(decIds);
            //long listBoolean = memoryList.stream().filter(e -> e.isIncrease()).count();
            //long listBoolean2 = memoryList.stream().filter(e -> !e.isIncrease()).count();
            if (!incIds.isEmpty()) {
                MemoryItem memory = new MemoryItem();
                memory.setAction(action.getName());
                memory.setMarket(market.getConfig().getMarket());
                memory.setDate(incdecList.get(0).getDate());
                memory.setRecord(LocalDate.now());
                memory.setUsedsec(param.getUsedsec());
                memory.setFuturedays(param.getFuturedays());
                memory.setFuturedate(param.getFutureDate());
                memory.setComponent(keys.getLeft());
                memory.setSubcomponent(keys.getMiddle());
                memory.setLocalcomponent(keys.getRight());
                memory.setCategory(param.getCategoryTitle());
                /*
                List<IncDecItem> metalist = memoryList
                        .stream()
                        .filter(Objects::nonNull)
                        .filter(e -> e.getLocalcomponent() != null)
                        .collect(Collectors.toList());
                        */
                memory.setType("Both");
                memory.setParameters(JsonUtil.convert(parameters));
                memory.setSize((long) incIds.size());
                //List<Double> list = new ArrayList<>(param.getScoreMap().values());
                try {
                    memory.save();
                } catch (Exception e) {
                    log.error(Constants.EXCEPTION, e);
                }
            }
            
            /*
            Optional<Double> minOpt = cnt.getRight() != null ? Optional.of(((double) cnt.getLeft()) / cnt.getRight()) : Optional.ofNullable(null);
            Optional<Double> aboveMinOpt = abovecnt.getRight() != null ? Optional.of(((double) abovecnt.getLeft()) / abovecnt.getRight()) : Optional.ofNullable(null);
            Optional<Double> belowMinOpt = belowcnt.getRight() != null ? Optional.of(((double) belowcnt.getLeft()) / belowcnt.getRight()) : Optional.ofNullable(null);
            handleMin(market, okListMap, okConfMap, keys, new ArrayList<>(), minOpt);
            handleMin(market, aboveOkListMap, aboveOkConfMap, keys, new ArrayList<>(), aboveMinOpt);
            handleMin(market, belowOkListMap, belowOkConfMap, keys, new ArrayList<>(), belowMinOpt);
            */
        }
        /*
        ProfitInputData input = new ProfitInputData();
        input.setConfMap(okConfMap);
        input.setListMap(okListMap);
        input.setAboveConfMap(aboveOkConfMap);
        input.setAboveListMap(aboveOkListMap);
        input.setBelowConfMap(belowOkConfMap);
        input.setBelowListMap(belowOkListMap);
        return input;
        */
    }

    public void getListComponents(MarketActionData action, WebData myData, ComponentData param, IclijConfig config,
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
        List<MemoryItem> currentList = new MiscUtil().filterKeepRecent3(marketMemory, prevdate, ((int) AVERAGE_SIZE) * action.getTime(market), false);
        // map subcat + posit -> list
        currentList = currentList.stream().filter(e -> !e.getComponent().equals(PipelineConstants.ABOVEBELOW)).collect(Collectors.toList());
        memories.method(currentList, config);
     }

}
