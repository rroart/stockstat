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

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import roart.iclij.model.IncDecItem;
import roart.iclij.model.MLMetricsItem;
import roart.iclij.model.MemoryItem;
import roart.iclij.model.Parameters;
import roart.iclij.model.WebData;
import roart.iclij.model.action.ImproveAboveBelowActionData;
import roart.iclij.util.MiscUtil;
import roart.iclij.util.VerifyProfitUtil;
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
        return prevdate.minusDays(market.getConfig().getImprovetime());
    }

    @Override
    public void setValMap(ComponentData param) {
        param.getAndSetWantedCategoryValueMap();
    }

    @Override
    protected ProfitInputData filterMemoryListMapsWithConfidence(Market market,
            Map<Pair<String, Integer>, List<MemoryItem>> listMap, IclijConfig config) {
        Map<Pair<String, Integer>, List<MemoryItem>> badListMap = new HashMap<>();
        Map<Pair<String, Integer>, Double> badConfMap = new HashMap<>();
        for(Pair<String, Integer> key : listMap.keySet()) {
            List<MemoryItem> memoryList = listMap.get(key);
            List<Double> confidences = memoryList.stream().map(MemoryItem::getConfidence).collect(Collectors.toList());
            if (confidences.isEmpty()) {
                int jj = 0;
                //continue;
            }
            confidences = confidences.stream().filter(m -> m != null && !m.isNaN()).collect(Collectors.toList());
            Optional<Double> minOpt = confidences.parallelStream().reduce(Double::min);
            if (!minOpt.isPresent()) {
                int jj = 0;
                //continue;
            }
            Double min = 0.0;
            if (minOpt.isPresent()) {
                min = minOpt.get();
            }
            // do the bad ones
            // do not yet improve on the good enough ones
            if (false /*min >= market.getConfidence()*/) {
                continue;
            }
            //Optional<Double> maxOpt = confidences.parallelStream().reduce(Double::max);
            //Double max = maxOpt.get();
            //System.out.println("Mark " + market.getConfig().getMarket() + " " + keys[0] + " " + min + " " + max );
            //Double conf = market.getConfidence();
            //System.out.println(conf);
            badListMap.put(key, listMap.get(key));
            badConfMap.put(key, min);
        }
        ProfitInputData input = new ProfitInputData();
        input.setConfMap(badConfMap);
        input.setListMap(badListMap);
        input.setAboveConfMap(badConfMap);
        input.setAboveListMap(badListMap);
        input.setBelowConfMap(badConfMap);
        input.setBelowListMap(badListMap);
        return input;
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
            Map<String, List<Integer>> listComponent, Map<String, Component> componentMap,
            Map<String, ComponentData> dataMap, Boolean buy, String subcomponent, WebData myData, IclijConfig config,
            Parameters parameters, boolean wantThree, List<MLMetricsItem> mlTests) {
        if (param.getUpdateMap() == null) {
            param.setUpdateMap(new HashMap<>());
        }
        List<String> stockDates = param.getService().getDates(market.getConfig().getMarket());
        int verificationdays = param.getInput().getConfig().verificationDays();
        param.getInput().setDoSave(false);
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
            try {
                allIncDecs = IclijDbDao.getAllIncDecs();
            } catch (Exception e) {
                log.error(Constants.EXCEPTION, e);
            }
            LocalDate date = param.getInput().getEnddate();
            String aDate = TimeUtil.convertDate2(date);
            int index = TimeUtil.getIndexEqualBefore(stockDates, aDate);
            int indexoffset = index - verificationdays;
            if (indexoffset < 0) {
                continue;
            }
            aDate = stockDates.get(indexoffset);
            try {
                date = TimeUtil.convertDate(aDate);
            } catch (ParseException e) {
                log.error(Constants.EXCEPTION, e);
            }
            List<IncDecItem> incdecs = new MiscUtil().getCurrentIncDecs(date, allIncDecs, market, market.getConfig().getFindtime());
            List<String> parametersList = new MiscUtil().getParameters(incdecs);
            for (String aParameter : parametersList) {
                List<IncDecItem> incdecsP = new MiscUtil().getCurrentIncDecs(incdecs, aParameter);              
                List<String> components = new ArrayList<>();
                List<String> subcomponents = new ArrayList<>();
                getComponentLists(incdecsP, components, subcomponents);
                Parameters realParamaters = JsonUtil.convert(aParameter, Parameters.class);
                
                FitnessAboveBelow fit = new FitnessAboveBelow(action, new ArrayList<>(), param, profitdata, market, null, component.getPipeline(), buy, subcomponent, realParamaters, mlTests, incdecsP, components, subcomponents, stockDates);

                double score = 0;
                {
                    List<IncDecItem> myincdecs = incdecsP;
                    List<IncDecItem> myincs = myincdecs.stream().filter(m1 -> m1.isIncrease()).collect(Collectors.toList());
                    List<IncDecItem> mydecs = myincdecs.stream().filter(m2 -> !m2.isIncrease()).collect(Collectors.toList());
                    myincs = new MiscUtil().mergeList(myincs, true);
                    mydecs = new MiscUtil().mergeList(mydecs, true);
                    List<IncDecItem> myincdec = new MiscUtil().moveAndGetCommon(myincs, mydecs, true);
                    Short mystartoffset = market.getConfig().getStartoffset();
                    short startoffset = mystartoffset != null ? mystartoffset : 0;
                    new VerifyProfitUtil().getVerifyProfit(verificationdays, param.getFutureDate(), param.getService(), param.getBaseDate(), myincs, mydecs, myincdec, startoffset, realParamaters.getThreshold(), stockDates, 0);
                    score = fit.fitness(myincs, mydecs, myincdec);
                }
                
                int size = components.size() + subcomponents.size();
                List<String> compsub = new ArrayList<>();
                compsub.addAll(components);
                compsub.addAll(subcomponents);
                //AboveBelowGene gene = new AboveBelowGene();
                AboveBelowChromosome chromosome = new AboveBelowChromosome(size);
                //action, new ArrayList<>(), param, profitdata, market, null, component.getPipeline(), buy, subcomponent, parameters, gene, mlTests);            

                ComponentData componentData = component.improve(action, param, chromosome, subcomponent, new AboveBelowChromosomeWinner(aParameter, compsub), null, fit);
                Map<String, Object> updateMap = componentData.getUpdateMap();
                if (updateMap != null) {
                    param.getUpdateMap().putAll(updateMap);
                }
                MemoryItem memory = new MemoryItem();
                memory.setAction(action.getName());
                memory.setMarket(market.getConfig().getMarket());
                memory.setDate(param.getBaseDate());
                memory.setRecord(LocalDate.now());
                memory.setUsedsec(param.getUsedsec());
                memory.setFuturedays(param.getFuturedays());
                memory.setFuturedate(param.getFutureDate());
                memory.setComponent(component.getPipeline());
                memory.setCategory(param.getCategoryTitle());
                memory.setDescription((String) updateMap.get(parameters));
                //memory.setSubcomponent(meta.get(ResultMetaConstants.MLNAME) + " " + meta.get(ResultMetaConstants.MODELNAME));
                //memory.setDescription(getShort((String) meta.get(ResultMetaConstants.MLNAME)) + withComma(getShort((String) meta.get(ResultMetaConstants.MODELNAME))) + withComma(meta.get(ResultMetaConstants.SUBTYPE)) + withComma(meta.get(ResultMetaConstants.SUBSUBTYPE)));
                memory.setParameters(aParameter);
                memory.setConfidence(score);
                List<Double> list = new ArrayList<>(param.getScoreMap().values());
                memory.setLearnConfidence(list.get(0));
                if (true || param.isDoSave()) {
                    try {
                        memory.save();
                    } catch (Exception e) {
                        log.error(Constants.EXCEPTION);
                    }
                }
                //memoryList.add(memory);
            }
            //component.calculateIncDec(componentData, profitdata, positions);
            //System.out.println("Buys: " + market.getMarket() + buys);
            //System.out.println("Sells: " + market.getMarket() + sells);           
        }

    }

}
