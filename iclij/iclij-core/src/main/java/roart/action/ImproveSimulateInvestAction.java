package roart.action;

import java.text.ParseException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.common.constants.Constants;
import roart.common.util.JsonUtil;
import roart.common.util.TimeUtil;
import roart.component.Component;
import roart.component.model.ComponentData;
import roart.db.IclijDbDao;
import roart.iclij.config.IclijConfig;
import roart.iclij.config.Market;
import roart.iclij.filter.Memories;
import roart.iclij.model.IncDecItem;
import roart.iclij.model.MLMetricsItem;
import roart.iclij.model.MemoryItem;
import roart.iclij.model.Parameters;
import roart.iclij.model.Trend;
import roart.iclij.model.WebData;
import roart.iclij.model.action.ImproveSimulateInvestActionData;
import roart.iclij.model.action.SimulateInvestActionData;
import roart.iclij.util.MarketUtil;
import roart.iclij.util.MiscUtil;
import roart.iclij.verifyprofit.TrendUtil;
import roart.service.model.ProfitData;

public class ImproveSimulateInvestAction extends MarketAction {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    public ImproveSimulateInvestAction() {
        setActionData(new ImproveSimulateInvestActionData());
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
        return param.getInput().getEnddate();
    }

    @Override
    public void setValMap(ComponentData param) {
        param.getAndSetWantedCategoryValueMap();
    }

    @Override
    protected void handleComponent(MarketAction action, Market market, ProfitData profitdata, ComponentData param,
            Memories listComponent, Map<String, Component> componentMap, Map<String, ComponentData> dataMap,
            Boolean buy, String subcomponent, WebData myData, IclijConfig config, Parameters parameters,
            boolean wantThree, List<MLMetricsItem> mlTests) {
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
                Parameters realParameters = JsonUtil.convert(aParameter, Parameters.class);
                
                param.getAndSetCategoryValueMap();
                Map<String, List<List<Double>>> categoryValueMap = param.getCategoryValueMap();
                
                //FitnessAboveBelow fit = new FitnessAboveBelow(action, new ArrayList<>(), param, profitdata, market, null, component.getPipeline(), buy, subcomponent, realParameters, mlTests, incdecsP, components, subcomponents, stockDates);

                double scoreFilter = 0;
                double score = 0;
                Pair[] scores = null;;
                long scoreSize = 0;
                int size = components.size() + subcomponents.size();
                List<String> compsub = new ArrayList<>();
                compsub.addAll(components);
                compsub.addAll(subcomponents);
                
                
                short startoffset = new MarketUtil().getStartoffset(market);
                int findTime = market.getConfig().getFindtime();
                Trend trend = new TrendUtil().getTrend(verificationdays, null /*TimeUtil.convertDate2(olddate)*/, startoffset, stockDates /*, findTime*/, param, market, categoryValueMap);
                log.info("Trend {}", trend);                
                
                //AboveBelowGene gene = new AboveBelowGene();
                //AboveBelowChromosome chromosome = new AboveBelowChromosome(size);
                //action, new ArrayList<>(), param, profitdata, market, null, component.getPipeline(), buy, subcomponent, parameters, gene, mlTests);            

                MemoryItem memory = new MemoryItem();
                if (true || score < market.getFilter().getConfidence()) {
                    ComponentData componentData = null; //component.improve(action, param, chromosome, subcomponent, new AboveBelowChromosomeWinner(aParameter, compsub), null, fit);
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
                memory.setAbovepositives((Long) scores[0].getLeft());
                memory.setAbovesize((long) scores[0].getRight()); 
                memory.setBelowpositives((Long) scores[1].getLeft());
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

}
