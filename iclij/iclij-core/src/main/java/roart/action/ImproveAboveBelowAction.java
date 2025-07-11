package roart.action;

import java.text.ParseException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.common.config.ConfigConstants;
import roart.common.constants.Constants;
import roart.common.constants.EvolveConstants;
import roart.common.constants.ServiceConstants;
import roart.common.inmemory.factory.InmemoryFactory;
import roart.common.inmemory.model.Inmemory;
import roart.common.inmemory.model.InmemoryMessage;
import roart.common.model.ActionComponentDTO;
import roart.common.model.IncDecDTO;
import roart.common.model.MLMetricsDTO;
import roart.common.model.MemoryDTO;
import roart.common.pipeline.PipelineConstants;
import roart.common.pipeline.data.PipelineData;
import roart.common.pipeline.util.PipelineThreadUtils;
import roart.common.pipeline.util.PipelineUtils;
import roart.common.queue.QueueElement;
import roart.common.util.JsonUtil;
import roart.common.util.TimeUtil;
import roart.iclij.component.Component;
import roart.component.model.ComponentData;
import roart.constants.IclijConstants;
import roart.evolution.config.EvolutionConfig;
import roart.iclij.config.IclijConfig;
import roart.iclij.config.Market;
import roart.iclij.evolution.fitness.impl.FitnessAboveBelowCommon;
import roart.iclij.evolve.AboveBelowEvolveFactory;
import roart.iclij.evolve.Evolve;
import roart.iclij.filter.Memories;
import roart.iclij.model.Parameters;
import roart.iclij.model.Trend;
import roart.iclij.model.WebData;
import roart.iclij.model.action.ImproveAboveBelowActionData;
import roart.iclij.model.action.MarketActionData;
import roart.iclij.service.util.MarketUtil;
import roart.iclij.service.util.MiscUtil;
import roart.iclij.verifyprofit.TrendUtil;
import roart.iclij.verifyprofit.VerifyProfitUtil;
import roart.service.model.ProfitData;
import roart.service.model.ProfitInputData;

public class ImproveAboveBelowAction extends MarketAction {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    public ImproveAboveBelowAction(IclijConfig iclijConfig) {
        setActionData(new ImproveAboveBelowActionData(iclijConfig));
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
        LocalDate prevdate = param.getInput().getEnddate();
        return prevdate.minusDays(getActionData().getTime(market));
    }

    @Override
    public void setValMap(ComponentData param) {
        param.getAndSetWantedCategoryValueMap(false);
    }

    @Override
    protected void handleComponent(MarketAction action, Market market, ProfitData profitdata, ComponentData param,
            Memories listComponent, Map<String, Component> componentMap,
            Map<String, ComponentData> dataMap, Boolean buy, String subcomponent, WebData myData, IclijConfig config,
            Parameters parameters, boolean wantThree, List<MLMetricsDTO> mlTests) {
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

            // TODO param.stockdates?
            List<String> stockDates = param.getService().getDates(market.getConfig().getMarket(), param.getId());
            int verificationdays = param.getConfig().verificationDays();

            //ComponentData componentData = component.improve2(action, param, market, profitdata, null, buy, subcomponent, parameters, mlTests);
            List<IncDecDTO> allIncDecs = null;
            LocalDate date = param.getFutureDate();
            date = TimeUtil.getBackEqualBefore2(date, verificationdays, stockDates);
            LocalDate prevDate = date.minusDays(market.getConfig().getFindtime());
            try {
                allIncDecs = param.getService().getIo().getIdbDao().getAllIncDecs(market.getConfig().getMarket(), prevDate, date, null);
            } catch (Exception e) {
                log.error(Constants.EXCEPTION, e);
            }
            List<IncDecDTO> incdecs = allIncDecs; // new MiscUtil().getCurrentIncDecs(date, allIncDecs, market, market.getConfig().getFindtime(), false);
            List<String> parametersList = new MiscUtil().getParameters(incdecs);
            if (parametersList.isEmpty()) {
                component.saveTiming(action.getActionData(), param, true, System.currentTimeMillis(), Double.valueOf(0.0), null, subcomponent, null, null, null, true);
            }
            for (String aParameter : parametersList) {
                List<IncDecDTO> incdecsP = new MiscUtil().getCurrentIncDecs(incdecs, aParameter);              
                List<String> components = new ArrayList<>();
                List<String> subcomponents = new ArrayList<>();
                getComponentLists(incdecsP, components, subcomponents);
                Parameters realParameters = JsonUtil.convert(aParameter, Parameters.class);
                Double threshold = realParameters.getThreshold();

                // uses getcontent
                // todo
                // done clean
                // todo ok?
                param.getAndSetCategoryValueMap(false);
                Inmemory inmemory = param.getService().getIo().getInmemoryFactory().get(config.getInmemoryServer(), config.getInmemoryHazelcast(), config.getInmemoryRedis());
                PipelineData pipelineDatum = PipelineUtils.getPipeline(param.getResultMaps(), PipelineConstants.META, inmemory);
                Integer cat = PipelineUtils.getWantedcat(pipelineDatum);
                String catName = PipelineUtils.getMetaCat(pipelineDatum);
                log.info("cats {} {}", cat, catName);
                param.setCategory(cat);
                param.setCategoryTitle(catName);
                param.getAndSetCategoryValueMapAlt();

                //FitnessAboveBelow fit = new FitnessAboveBelow(action, new ArrayList<>(), param, profitdata, market, null, this.getPipeline(), null, subcomponent, realParameters, null, incdecsP, components, subcomponents, stockDates);
                FitnessAboveBelowCommon fitCommon = new FitnessAboveBelowCommon();

                double scoreFilter = 0;
                {
                    Memories listComponent2 = new Memories(market);
                    LocalDate olddate = prevDate.minusDays(((int) AVERAGE_SIZE) * action.getActionData().getTime(market));
                    ProfitInputData inputdata = new ProfitInputData();
                    getListComponents(action.getActionData(), myData, param, null, realParameters, evolve, market, null, listComponent2, olddate, prevDate);

                    List<IncDecDTO> mylocals = new MiscUtil().getIncDecLocals(incdecsP);
                    List<IncDecDTO> allCurrentIncDecs = mylocals
                            .stream()
                            .filter(e -> !listComponent2.containsBelow(e.getComponent(), new ImmutablePair(e.getSubcomponent(), e.getLocalcomponent()), null, null, true))
                            .collect(Collectors.toList());
                    List<IncDecDTO> myincdecs = allCurrentIncDecs;
                    if (threshold != 1.0) {
                        myincdecs = myincdecs
                                .stream()
                                .filter(e -> e.isIncrease() == threshold > 1.0)
                                .collect(Collectors.toList());
                    }
                    Set<IncDecDTO> myincs = myincdecs.stream().filter(m1 -> m1.isIncrease()).collect(Collectors.toSet());
                    Set<IncDecDTO> mydecs = myincdecs.stream().filter(m2 -> !m2.isIncrease()).collect(Collectors.toSet());

                    myincs = new MiscUtil().mergeList(myincs, true);
                    mydecs = new MiscUtil().mergeList(mydecs, true);
                    Set<IncDecDTO> myincdec = new MiscUtil().moveAndGetCommon(myincs, mydecs, true);
                    short startoffset = new MarketUtil().getStartoffset(market);

                    new VerifyProfitUtil().getVerifyProfit(verificationdays, param.getFutureDate(), myincs, mydecs, myincdec, startoffset, realParameters.getThreshold(), param.getStockDates(), param.getCategoryValueMap());

                    scoreFilter = fitCommon.fitness(myincs, mydecs, myincdec, 0, null);


                }

                double score = 0;
                long scoreSize = 0;
                Pair<Long, Integer>[] scores = null;
                {
                    List<IncDecDTO> myincdecs = incdecsP;
                    if (threshold != 1.0) {
                        myincdecs = myincdecs
                                .stream()
                                .filter(e -> e.isIncrease() == threshold > 1.0)
                                .collect(Collectors.toList());
                    }
                    Set<IncDecDTO> myincs = myincdecs.stream().filter(m1 -> m1.isIncrease()).collect(Collectors.toSet());
                    Set<IncDecDTO> mydecs = myincdecs.stream().filter(m2 -> !m2.isIncrease()).collect(Collectors.toSet());
                    List<IncDecDTO> mylocals = new MiscUtil().getIncDecLocals(myincdecs);

                    myincs = new MiscUtil().mergeList(myincs, true);
                    mydecs = new MiscUtil().mergeList(mydecs, true);
                    Set<IncDecDTO> myincdec = new MiscUtil().moveAndGetCommon(myincs, mydecs, true);
                    short startoffset = new MarketUtil().getStartoffset(market);
                    new VerifyProfitUtil().getVerifyProfit(verificationdays, param.getFutureDate(), myincs, mydecs, myincdec, startoffset, realParameters.getThreshold(), param.getStockDates(), param.getCategoryValueMap());
                    score = fitCommon.fitness(myincs, mydecs, myincdec, 0, null);
                    scoreSize = myincs.size() + mydecs.size() + myincdec.size();
                    scores = fitCommon.fitness2(myincs, mydecs, myincdec, 0, null);
                    scoreSize = (long) scores[0].getRight() + scores[1].getRight();
                    {
                        Memories listComponentMap = new Memories(market);
                        //LocalDate prevdate = action.getPrevDate(param, market);
                        LocalDate prevdate = param.getInput().getEnddate();
                        prevdate = prevdate.minusDays(action.getActionData().getTime(market));
                        LocalDate olddate = prevdate.minusDays(((int) AVERAGE_SIZE) * action.getActionData().getTime(market));

                        // making separate memories the components and subcomponents

                        getListComponentsNew(myData, param, realParameters, evolve, market, listComponentMap, prevdate, olddate, mylocals, action.getActionData());

                        // todo make memories of confidence
                        //Set<Triple<String, String, String>> keys = i.getAboveListMap().keySet();
                        // System.out.println(keys);
                        // memory.save?
                    }
                }

                short startoffset = new MarketUtil().getStartoffset(market);
                int findTime = market.getConfig().getFindtime();
                Trend trend = new TrendUtil().getTrend(verificationdays, null , startoffset, param.getStockDates(), param, market, param.getCategoryValueMap());
                log.info("Trend {}", trend);                

                MemoryDTO memory = new MemoryDTO();
                if (true) {
                    int ga = param.getConfig().getEvolveGA();
                    Evolve evolve2 = AboveBelowEvolveFactory.factory(ga);
                    String evolutionConfigString = param.getConfig().getImproveAbovebelowEvolutionConfig();
                    EvolutionConfig evolutionConfig = JsonUtil.convert(evolutionConfigString, EvolutionConfig.class);

                    Map<String, Object> confMap = new HashMap<>();
                    List<String> confList = new ArrayList<>();
                    //Boolean buy = null;
                    //List<MLMetricsDTO> mlTests = null;
                    ComponentData componentData = evolve2.evolve(action.getActionData(), param, market, profitdata, buy, subcomponent, parameters, mlTests , confMap , evolutionConfig, component.getPipeline(), component, confList );

                    //component.handle(this, market, param, profitdata, listComponent, evolve, aMap, subcomponent, null, null);
                    //ComponentData componentData = component.handle(getActionData(), market, param, profitdata, listComponent, evolve, aMap, subcomponent, null, null, getParent() != null);
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
                        param.getService().getIo().getIdbDao().save(memory);
                    } catch (Exception e) {
                        log.error(Constants.EXCEPTION, e);
                    }
                }
            }

            PipelineData results = param.getResultMap();
            if (results != null) {
                log.info("Content {}", JsonUtil.convert(results));
                Inmemory inmemory = param.getService().getIo().getInmemoryFactory().get(config.getInmemoryServer(), config.getInmemoryHazelcast(), config.getInmemoryRedis());
                QueueElement element = new QueueElement();
                InmemoryMessage msg = inmemory.send(ServiceConstants.EVOLVEFILTERABOVEBELOW + UUID.randomUUID(), results, null);
                element.setOpid(ServiceConstants.EVOLVE);
                element.setMessage(msg);
                
                param.getService().send(ServiceConstants.EVOLVEFILTERABOVEBELOW, element, param.getConfig());
            }
            //component.calculateIncDec(componentData, profitdata, positions);
            //System.out.println("Buys: " + market.getMarket() + buys);
            //System.out.println("Sells: " + market.getMarket() + sells);           
        }
        // Done in runaction
        //Inmemory inmemory = param.getService().getIo().getInmemoryFactory().get(config.getInmemoryServer(), config.getInmemoryHazelcast(), config.getInmemoryRedis());
        //new PipelineThreadUtils(config, inmemory, param.getService().getIo().getCuratorClient()).cleanPipeline(param.getService().id, param.getId());

    }

    public List<MemoryDTO> findAllMarketComponentsToCheck(WebData myData, ComponentData param, int days, IclijConfig config, boolean evolve, Map<String, ComponentData> dataMap, Map<String, Component> componentMap, String subcomponent, Parameters parameters, Market market) {
        List<MemoryDTO> allMemories = new ArrayList<>();
        Short startOffset = market.getConfig().getStartoffset();
        if (startOffset != null) {
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
            
            ComponentData componentData = component.handle(getActionData(), market, param, profitdata, new Memories(market), evolve, aMap, subcomponent, null, parameters, getParent() != null);
            dataMap.put(entry.getKey(), componentData);
            componentData.setUsedsec(time0);
            myData.getUpdateMap().putAll(componentData.getUpdateMap());
            List<MemoryDTO> memories;
            try {
                memories = component.calculateMemory(getActionData(), componentData, parameters);
                allMemories.addAll(memories);
           } catch (Exception e) {
                log.error(Constants.EXCEPTION, e);
            }
         }
        return allMemories;
    }

    public List<MemoryDTO> findAllMarketComponentsToCheckNew(WebData myData, ComponentData param, int days, IclijConfig config, boolean evolve, Map<String, ComponentData> dataMap, Map<String, Component> componentMap, String subcomponent, Parameters parameters, Market market) {
        List<MemoryDTO> allMemories = new ArrayList<>();
        Short startOffset = market.getConfig().getStartoffset();
        if (startOffset != null) {
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
            
            ComponentData componentData = component.handle(getActionData(), market, param, profitdata, new Memories(market), evolve, aMap, subcomponent, null, parameters, getParent() != null);
            dataMap.put(entry.getKey(), componentData);
            componentData.setUsedsec(time0);
            myData.getUpdateMap().putAll(componentData.getUpdateMap());
            List<MemoryDTO> memories;
            try {
                memories = component.calculateMemory(getActionData(), componentData, parameters);
                allMemories.addAll(memories);
           } catch (Exception e) {
                log.error(Constants.EXCEPTION, e);
            }
         }
        return allMemories;
    }

    private void getComponentLists(List<IncDecDTO> incdecs, List<String> components, List<String> subcomponents) {
        Set<String> componentSet = new HashSet<>();
        Set<String> subcomponentSet = new HashSet<>();

        for (IncDecDTO item : incdecs) {
            componentSet.add(item.getComponent());
            subcomponentSet.add(item.getSubcomponent());
        }
        components.addAll(componentSet);
        subcomponents.addAll(subcomponentSet);
    }
    
    private void getListComponentsNew(WebData myData, ComponentData param, Parameters parameters,
            Boolean evolve, Market market, Memories listComponentMap, LocalDate prevdate,
            LocalDate olddate, List<IncDecDTO> mylocals, MarketActionData action) {
        ProfitInputData inputdata = null;
        /*
        List<MemoryDTO> marketMemory = new MarketUtil().getMarketMemory(marketTime.market, getName(), marketTime.componentName, marketTime.subcomponent, JsonUtil.convert(marketTime.parameters), olddate, prevdate);
        if (marketMemory == null) {
            myData.setProfitData(new ProfitData());
        }
        marketMemory.addAll(myData.getMemoryDTOs());
        */
        
        List<String> stockDates = param.getService().getDates(market.getConfig().getMarket(), param.getId());
        int verificationdays = param.getConfig().verificationDays();
        /*
        List<IncDecDTO> allIncDecs = null;
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
        List<IncDecDTO> incdecs = new MiscUtil().getCurrentIncDecs(date, allIncDecs, market, AVERAGE_SIZE * market.getConfig().getFindtime());
        List<IncDecDTO> incdecsP = new MiscUtil().getCurrentIncDecs(incdecs, JsonUtil.convert(parameters));              
        List<IncDecDTO> incdecsL = new MiscUtil().getIncDecLocals(incdecsP);              
        */
        List<IncDecDTO> incdecsL = mylocals;
        List<IncDecDTO> myincs = mylocals.stream().filter(m1 -> m1.isIncrease()).collect(Collectors.toList());
        List<IncDecDTO> mydecs = mylocals.stream().filter(m2 -> !m2.isIncrease()).collect(Collectors.toList());
        short startoffset = new MarketUtil().getStartoffset(market);
        new VerifyProfitUtil().getVerifyProfit(verificationdays, param.getFutureDate(), myincs, mydecs, new ArrayList<>(), startoffset, parameters.getThreshold(), param.getStockDates(), param.getCategoryValueMap());
        

        //List<MemoryDTO> currentList = new MiscUtil().filterKeepRecent(marketMemory, prevdate, ((int) AVERAGE_SIZE) *.getTime(market));
        // or make a new object instead of the object array. use this as a pair
        //System.out.println(currentList.get(0).getRecord());
        Map<Triple<String, String, String>, List<IncDecDTO>> listMap = new HashMap<>();
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

    private void filterMemoryListMapsWithConfidenceNew(Market market, Map<Triple<String, String, String>, List<IncDecDTO>> listMap, ComponentData param, Parameters parameters, MarketActionData action) {
        Map<Triple<String, String, String>, List<MemoryDTO>> okListMap = new HashMap<>();
        Map<Triple<String, String, String>, Double> okConfMap = new HashMap<>();
        Map<Triple<String, String, String>, List<MemoryDTO>> aboveOkListMap = new HashMap<>();
        Map<Triple<String, String, String>, Double> aboveOkConfMap = new HashMap<>();
        Map<Triple<String, String, String>, List<MemoryDTO>> belowOkListMap = new HashMap<>();
        Map<Triple<String, String, String>, Double> belowOkConfMap = new HashMap<>();
        for(Entry<Triple<String, String, String>, List<IncDecDTO>> entry : listMap.entrySet()) {
            Triple<String, String, String> keys = entry.getKey();
            List<IncDecDTO> incdecList = entry.getValue();
            //List<Double> confidences = memoryList.stream().map(MemoryDTO::getConfidence).collect(Collectors.toList());
            //confidences = confidences.stream().filter(m -> m != null && !m.isNaN()).collect(Collectors.toList());
            //List<Double> aboveConfidenceList = new ArrayList<>();
            //List<Double> belowConfidenceList = new ArrayList<>();
            // TODO refactor
            //MiscUtil FitnessAboveBelowCommon = new FitnessAboveBelowCommon();
            Pair<Long, Integer> abovecnt = FitnessAboveBelowCommon.countsize(incdecList.stream().filter(e -> e.isIncrease()).collect(Collectors.toList()));
            Pair<Long, Integer> belowcnt = FitnessAboveBelowCommon.countsize(incdecList.stream().filter(e -> !e.isIncrease()).collect(Collectors.toList()));
            Pair<Long, Integer> cnt = FitnessAboveBelowCommon.countsize(incdecList);
            {
                MemoryDTO memory = new MemoryDTO();
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
                List<IncDecDTO> metalist = memoryList
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
                    param.getService().getIo().getIdbDao().save(memory);
                } catch (Exception e) {
                    log.error(Constants.EXCEPTION, e);
                }

            }
            Set<String> incIds = incdecList.stream().filter(e -> e.isIncrease()).map(IncDecDTO::getId).collect(Collectors.toSet());
            Set<String> decIds = incdecList.stream().filter(e -> !e.isIncrease()).map(IncDecDTO::getId).collect(Collectors.toSet());
            incIds.retainAll(decIds);
            //long listBoolean = memoryList.stream().filter(e -> e.isIncrease()).count();
            //long listBoolean2 = memoryList.stream().filter(e -> !e.isIncrease()).count();
            if (!incIds.isEmpty()) {
                MemoryDTO memory = new MemoryDTO();
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
                List<IncDecDTO> metalist = memoryList
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
                    param.getService().getIo().getIdbDao().save(memory);
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
        List<MemoryDTO> marketMemory = new MarketUtil().getMarketMemory(market, IclijConstants.IMPROVEABOVEBELOW, null, null, JsonUtil.convert(parameters), olddate, prevdate, param.getService().getIo().getIdbDao());
        marketMemory = marketMemory.stream().filter(e -> "Confidence".equals(e.getType())).collect(Collectors.toList());
        if (!marketMemory.isEmpty()) {
            int jj = 0;
        }
        if (marketMemory == null) {
            myData.setProfitData(new ProfitData());
        }
        //marketMemory.addAll(myData.getMemoryDTOs());
        List<MemoryDTO> currentList = new MiscUtil().filterKeepRecent3(marketMemory, prevdate, ((int) AVERAGE_SIZE) * action.getTime(market), false);
        // map subcat + posit -> list
        currentList = currentList.stream().filter(e -> !e.getComponent().equals(PipelineConstants.ABOVEBELOW)).collect(Collectors.toList());
        memories.method(currentList, config);
     }

}

