package roart.action;

import java.text.ParseException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
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
import roart.common.util.JsonUtil;
import roart.common.util.TimeUtil;
import roart.component.AboveBelowChromosomeWinner;
import roart.component.Component;
import roart.component.FitnessAboveBelow;
import roart.component.model.ComponentData;
import roart.db.IclijDbDao;
import roart.evolution.marketfilter.chromosome.impl.AboveBelowChromosome;
import roart.iclij.config.IclijConfig;
import roart.iclij.config.Market;
import roart.iclij.filter.Memories;
import roart.iclij.model.IncDecItem;
import roart.iclij.model.MLMetricsItem;
import roart.iclij.model.MemoryItem;
import roart.iclij.model.Parameters;
import roart.iclij.model.Trend;
import roart.iclij.model.WebData;
import roart.iclij.model.action.ImproveAboveBelowActionData;
import roart.iclij.util.MarketUtil;
import roart.iclij.util.MiscUtil;
import roart.iclij.verifyprofit.TrendUtil;
import roart.iclij.verifyprofit.VerifyProfitUtil;
import roart.service.model.ProfitData;
import roart.service.model.ProfitInputData;

public class ImproveAboveBelowAction extends MarketAction {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    public ImproveAboveBelowAction() {
        setActionData(new ImproveAboveBelowActionData());
    }
    
    @Override
    protected List<IncDecItem> getIncDecItems() {
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
    protected List<MemoryItem> getMemItems(MarketComponentTime marketTime, WebData myData, ComponentData param,
            IclijConfig config, Boolean evolve, Map<String, ComponentData> dataMap) {
        return new ArrayList<>();
    }

    @Override
    protected LocalDate getPrevDate(ComponentData param, Market market) {
        LocalDate prevdate = param.getInput().getEnddate();
        return prevdate.minusDays(getActionData().getTime(market));
    }

    @Override
    public void setValMap(ComponentData param) {
        param.getAndSetWantedCategoryValueMap();
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
    
    @Override
    protected void handleComponent(MarketAction action, Market market, ProfitData profitdata, ComponentData param,
            Memories listComponent, Map<String, Component> componentMap,
            Map<String, ComponentData> dataMap, Boolean buy, String subcomponent, WebData myData, IclijConfig config,
            Parameters parameters, boolean wantThree, List<MLMetricsItem> mlTests) {
        if (param.getUpdateMap() == null) {
            param.setUpdateMap(new HashMap<>());
        }
        List<String> stockDates = param.getService().getDates(market.getConfig().getMarket());
        int verificationdays = param.getInput().getConfig().verificationDays();
        //param.getInput().setDoSave(false);

        try {
            param.setFuturedays(0);
            param.setOffset(0);
            param.setDates(null, null, action.getActionData(), market);
        } catch (ParseException e) {
            log.error(Constants.EXCEPTION, e);
        }
        //List<MemoryItem> memories = findAllMarketComponentsToCheckNew(myData, param, 0, config, false, dataMap, componentMap, subcomponent, parameters, market);
        
        for (Entry<String, Component> entry : componentMap.entrySet()) {
            Component component = entry.getValue();
            if (component == null) {
                continue;
            }
            
            boolean evolve = false; // param.getInput().getConfig().wantEvolveML();
            //component.set(market, param, profitdata, positions, evolve);
            //ComponentData componentData = component.handle(market, param, profitdata, positions, evolve, new HashMap<>());
            // 0 ok?
            Map<String, Object> aMap = new HashMap<>();
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
                
                param.getAndSetCategoryValueMap();
                Map<String, List<List<Double>>> categoryValueMap = param.getCategoryValueMap();
                
                FitnessAboveBelow fit = new FitnessAboveBelow(action, new ArrayList<>(), param, profitdata, market, null, component.getPipeline(), buy, subcomponent, realParameters, mlTests, incdecsP, components, subcomponents, stockDates);

                double scoreFilter = 0;
                {
                    Memories listComponent2 = new Memories(market);
                    LocalDate olddate = prevDate.minusDays(((int) AVERAGE_SIZE) * getActionData().getTime(market));
                    ProfitInputData inputdata = new ProfitInputData();
                    getListComponents(myData, param, config, realParameters, evolve, market, dataMap, listComponent2, olddate, prevDate);

                    List<IncDecItem> mylocals = new MiscUtil().getIncDecLocals(incdecsP);
                    List<IncDecItem> allCurrentIncDecs = mylocals
                            .stream()
                            .filter(e -> !listComponent2.containsBelow(e.getComponent(), new ImmutablePair(e.getSubcomponent(), e.getLocalcomponent()), null, null, true))
                            .collect(Collectors.toList());
                    List<IncDecItem> myincdecs = allCurrentIncDecs;
                    List<IncDecItem> myincs = myincdecs.stream().filter(m1 -> m1.isIncrease()).collect(Collectors.toList());
                    List<IncDecItem> mydecs = myincdecs.stream().filter(m2 -> !m2.isIncrease()).collect(Collectors.toList());

                    myincs = new MiscUtil().mergeList(myincs, true);
                    mydecs = new MiscUtil().mergeList(mydecs, true);
                    List<IncDecItem> myincdec = new MiscUtil().moveAndGetCommon(myincs, mydecs, true);
                    short startoffset = new MarketUtil().getStartoffset(market);

                    new VerifyProfitUtil().getVerifyProfit(verificationdays, param.getFutureDate(), myincs, mydecs, myincdec, startoffset, realParameters.getThreshold(), stockDates, categoryValueMap);
                    
                    scoreFilter = fit.fitness(myincs, mydecs, myincdec, 0);


                }
                
                double score = 0;
                long scoreSize = 0;
                Pair<Long, Integer>[] scores = null;
                {
                    List<IncDecItem> myincdecs = incdecsP;
                    List<IncDecItem> myincs = myincdecs.stream().filter(m1 -> m1.isIncrease()).collect(Collectors.toList());
                    List<IncDecItem> mydecs = myincdecs.stream().filter(m2 -> !m2.isIncrease()).collect(Collectors.toList());
                    List<IncDecItem> mylocals = new MiscUtil().getIncDecLocals(myincdecs);

                    myincs = new MiscUtil().mergeList(myincs, true);
                    mydecs = new MiscUtil().mergeList(mydecs, true);
                    List<IncDecItem> myincdec = new MiscUtil().moveAndGetCommon(myincs, mydecs, true);
                    short startoffset = new MarketUtil().getStartoffset(market);
                    new VerifyProfitUtil().getVerifyProfit(verificationdays, param.getFutureDate(), myincs, mydecs, myincdec, startoffset, realParameters.getThreshold(), stockDates, param.getCategoryValueMap());
                    score = fit.fitness(myincs, mydecs, myincdec, 0);
                    scoreSize = myincs.size() + mydecs.size() + myincdec.size();
                    scores = fit.fitness2(myincs, mydecs, myincdec, 0);
                    {
                        Memories listComponentMap = new Memories(market);
                        LocalDate prevdate = getPrevDate(param, market);
                        LocalDate olddate = prevdate.minusDays(((int) AVERAGE_SIZE) * getActionData().getTime(market));
                        
                        getListComponentsNew(myData, param, config, realParameters, evolve, market, dataMap, listComponentMap, prevdate, olddate, mylocals);
                        /*
                        // todo make memories of confidence
                        Set<Triple<String, String, String>> keys = i.getAboveListMap().keySet();
                        System.out.println(keys);
                        // memory.save?
                         * 
                         */
                    }
                }
                
                int size = components.size() + subcomponents.size();
                List<String> compsub = new ArrayList<>();
                compsub.addAll(components);
                compsub.addAll(subcomponents);
                
                
                short startoffset = new MarketUtil().getStartoffset(market);
                int findTime = market.getConfig().getFindtime();
                Trend trend = new TrendUtil().getTrend(verificationdays, null /*TimeUtil.convertDate2(olddate)*/, startoffset, stockDates /*, findTime*/, param, market, categoryValueMap);
                log.info("Trend {}", trend);                
                
                //AboveBelowGene gene = new AboveBelowGene();
                AboveBelowChromosome chromosome = new AboveBelowChromosome(size);
                //action, new ArrayList<>(), param, profitdata, market, null, component.getPipeline(), buy, subcomponent, parameters, gene, mlTests);            

                MemoryItem memory = new MemoryItem();
                if (true || score < market.getFilter().getConfidence()) {
                    ComponentData componentData = component.improve(action, param, chromosome, subcomponent, new AboveBelowChromosomeWinner(aParameter, compsub), null, fit);
                    Map<String, Object> updateMap = componentData.getUpdateMap();
                    if (updateMap != null) {
                        param.getUpdateMap().putAll(updateMap);
                    }
                    memory.setDescription((String) updateMap.get(aParameter));
                    List<Double> list = new ArrayList<>(param.getScoreMap().values());
                    memory.setLearnConfidence(list.get(0));
                }
                memory.setAction(action.getName());
                memory.setMarket(market.getConfig().getMarket());
                memory.setDate(param.getBaseDate());
                memory.setRecord(LocalDate.now());
                memory.setUsedsec(param.getUsedsec());
                memory.setFuturedays(param.getFuturedays());
                memory.setFuturedate(param.getFutureDate());
                memory.setComponent(component.getPipeline());
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
                        log.error(Constants.EXCEPTION);
                    }
                }
                //memoryList.add(memory);
            }
            //component.handle(this, market, param, profitdata, listComponent, evolve, aMap, subcomponent, null, null);
            //component.calculateIncDec(componentData, profitdata, positions);
            //System.out.println("Buys: " + market.getMarket() + buys);
            //System.out.println("Sells: " + market.getMarket() + sells);           
        }

    }

    public List<MemoryItem> findAllMarketComponentsToCheck(WebData myData, ComponentData param, int days, IclijConfig config, boolean evolve, Map<String, ComponentData> dataMap, Map<String, Component> componentMap, String subcomponent, Parameters parameters, Market market) {
        List<MemoryItem> allMemories = new ArrayList<>();
        Short startOffset = market.getConfig().getStartoffset();
        if (startOffset != null) {
            System.out.println("Using offset " + startOffset);
            log.info("Using offset {}", startOffset);
            days += startOffset;
        }
        for (Entry<String, Component> entry : componentMap.entrySet()) {
            Component component = entry.getValue();
            long time0 = System.currentTimeMillis();
            //Market market = FindProfitAction.findMarket(componentparam);
            ProfitData profitdata = new ProfitData();
            evolve = false; // param.getInput().getConfig().wantEvolveML();
            Map<String, Object> aMap = new HashMap<>();
            aMap.put(ConfigConstants.MACHINELEARNINGMLDYNAMIC, config.wantsFindProfitMLDynamic());
            aMap.put(ConfigConstants.MACHINELEARNINGMLCLASSIFY, true);
            aMap.put(ConfigConstants.MACHINELEARNINGMLLEARN, config.wantsFindProfitMLDynamic());
            aMap.put(ConfigConstants.MACHINELEARNINGMLCROSS, false);

            String key = component.getThreshold();
            aMap.put(key, "[" + parameters.getThreshold() + "]");
            String key2 = component.getFuturedays();
            aMap.put(key2, parameters.getFuturedays());
                        
            aMap.put(ConfigConstants.MISCTHRESHOLD, null);
            
            ComponentData componentData = component.handle(this, market, param, profitdata, new Memories(market), evolve, aMap, subcomponent, null, parameters);
            dataMap.put(entry.getKey(), componentData);
            componentData.setUsedsec(time0);
            myData.getUpdateMap().putAll(componentData.getUpdateMap());
            List<MemoryItem> memories;
            try {
                memories = component.calculateMemory(componentData, parameters);
                allMemories.addAll(memories);
           } catch (Exception e) {
                log.error(Constants.EXCEPTION, e);
            }
         }
        return allMemories;
    }

    public List<MemoryItem> findAllMarketComponentsToCheckNew(WebData myData, ComponentData param, int days, IclijConfig config, boolean evolve, Map<String, ComponentData> dataMap, Map<String, Component> componentMap, String subcomponent, Parameters parameters, Market market) {
        List<MemoryItem> allMemories = new ArrayList<>();
        Short startOffset = market.getConfig().getStartoffset();
        if (startOffset != null) {
            System.out.println("Using offset " + startOffset);
            log.info("Using offset {}", startOffset);
            days += startOffset;
        }
        for (Entry<String, Component> entry : componentMap.entrySet()) {
            Component component = entry.getValue();
            long time0 = System.currentTimeMillis();
            //Market market = FindProfitAction.findMarket(componentparam);
            ProfitData profitdata = new ProfitData();
            evolve = false; // param.getInput().getConfig().wantEvolveML();
            Map<String, Object> aMap = new HashMap<>();
            aMap.put(ConfigConstants.MACHINELEARNINGMLDYNAMIC, config.wantsFindProfitMLDynamic());
            aMap.put(ConfigConstants.MACHINELEARNINGMLCLASSIFY, true);
            aMap.put(ConfigConstants.MACHINELEARNINGMLLEARN, config.wantsFindProfitMLDynamic());
            aMap.put(ConfigConstants.MACHINELEARNINGMLCROSS, false);

            String key = component.getThreshold();
            aMap.put(key, "[" + parameters.getThreshold() + "]");
            String key2 = component.getFuturedays();
            aMap.put(key2, parameters.getFuturedays());
                        
            aMap.put(ConfigConstants.MISCTHRESHOLD, null);
            
            ComponentData componentData = component.handle(this, market, param, profitdata, new Memories(market), evolve, aMap, subcomponent, null, parameters);
            dataMap.put(entry.getKey(), componentData);
            componentData.setUsedsec(time0);
            myData.getUpdateMap().putAll(componentData.getUpdateMap());
            List<MemoryItem> memories;
            try {
                memories = component.calculateMemory(componentData, parameters);
                allMemories.addAll(memories);
           } catch (Exception e) {
                log.error(Constants.EXCEPTION, e);
            }
         }
        return allMemories;
    }

    private void getListComponentsNew(WebData myData, ComponentData param, IclijConfig config,
            Parameters parameters, Boolean evolve, Market market, Map<String, ComponentData> dataMap,
            Memories listComponentMap, LocalDate prevdate, LocalDate olddate, List<IncDecItem> mylocals) {
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
        

        //List<MemoryItem> currentList = new MiscUtil().filterKeepRecent(marketMemory, prevdate, ((int) AVERAGE_SIZE) * getActionData().getTime(market));
        // or make a new object instead of the object array. use this as a pair
        //System.out.println(currentList.get(0).getRecord());
        Map<Triple<String, String, String>, List<IncDecItem>> listMap = new HashMap<>();
        // map subcat + posit -> list
        incdecsL.forEach(m -> new MiscUtil().listGetterAdder(listMap, new ImmutableTriple<String, String, String>(m.getComponent(), m.getSubcomponent(), m.getLocalcomponent()), m));
        filterMemoryListMapsWithConfidenceNew(market, listMap, config, param, parameters);        
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

    private void filterMemoryListMapsWithConfidenceNew(Market market, Map<Triple<String, String, String>, List<IncDecItem>> listMap, IclijConfig config, ComponentData param, Parameters parameters) {
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
            Pair<Long, Integer> abovecnt = FitnessAboveBelow.countsize(incdecList.stream().filter(e -> e.isIncrease()).collect(Collectors.toList()));
            Pair<Long, Integer> belowcnt = FitnessAboveBelow.countsize(incdecList.stream().filter(e -> !e.isIncrease()).collect(Collectors.toList()));
            Pair<Long, Integer> cnt = FitnessAboveBelow.countsize(incdecList);
            {
                MemoryItem memory = new MemoryItem();
                memory.setAction(this.getName());
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
            List<String> incIds = incdecList.stream().filter(e -> e.isIncrease()).map(IncDecItem::getId).collect(Collectors.toList());
            List<String> decIds = incdecList.stream().filter(e -> !e.isIncrease()).map(IncDecItem::getId).collect(Collectors.toList());
            incIds.retainAll(decIds);
            //long listBoolean = memoryList.stream().filter(e -> e.isIncrease()).count();
            //long listBoolean2 = memoryList.stream().filter(e -> !e.isIncrease()).count();
            if (!incIds.isEmpty()) {
                MemoryItem memory = new MemoryItem();
                memory.setAction(this.getName());
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

}

