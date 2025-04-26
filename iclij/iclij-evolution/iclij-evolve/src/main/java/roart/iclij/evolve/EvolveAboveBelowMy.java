package roart.iclij.evolve;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import roart.common.constants.Constants;
import roart.common.model.IncDecDTO;
import roart.common.model.MLMetricsDTO;
import roart.common.util.JsonUtil;
import roart.common.util.TimeUtil;
import roart.component.model.ComponentData;
import roart.db.dao.IclijDbDao;
import roart.iclij.evolution.chromosome.winner.AboveBelowChromosomeWinner;
import roart.evolution.config.EvolutionConfig;
import roart.iclij.evolution.marketfilter.chromosome.impl.AboveBelowChromosome;
import roart.iclij.component.Component;
import roart.iclij.config.Market;
import roart.iclij.evolution.fitness.impl.FitnessAboveBelow;
import roart.iclij.evolution.fitness.impl.FitnessAboveBelow2;
import roart.iclij.model.Parameters;
import roart.iclij.model.action.MarketActionData;
import roart.iclij.service.util.MiscUtil;
import roart.service.model.ProfitData;

public class EvolveAboveBelowMy extends EvolveMy {

    @Override
    public ComponentData evolve(MarketActionData action, ComponentData param, Market market, ProfitData profitdata,
            Boolean buy, String subcomponent, Parameters parameters, List<MLMetricsDTO> mlTests,
            Map<String, Object> confMap, EvolutionConfig evolutionConfig, String pipeline, Component component,
            List<String> confList) {
        List<String> stockDates = param.getService().getDates(market.getConfig().getMarket(), param.getId());
        int verificationdays = param.getConfig().verificationDays();

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
        for (String aParameter : parametersList) {
            List<IncDecDTO> incdecsP = new MiscUtil().getCurrentIncDecs(incdecs, aParameter);              
            List<String> components = new ArrayList<>();
            List<String> subcomponents = new ArrayList<>();
            getComponentLists(incdecsP, components, subcomponents);
            Parameters realParameters = JsonUtil.convert(aParameter, Parameters.class);
            FitnessAboveBelow fit = new FitnessAboveBelow(action, new ArrayList<>(), param, profitdata, market, null, pipeline, null, subcomponent, realParameters, null, incdecsP, components, subcomponents, stockDates);

        //AboveBelowGene gene = new AboveBelowGene();
            int size = components.size() + subcomponents.size();
        AboveBelowChromosome chromosome = new AboveBelowChromosome(size);
        //action, new ArrayList<>(), param, profitdata, market, null, component.getPipeline(), buy, subcomponent, parameters, gene, mlTests);            

        // Making a memory for the last findtime period
        
        List<String> compsub = new ArrayList<>();
        compsub.addAll(components);
        compsub.addAll(subcomponents);
        
       boolean save = false;
       ComponentData componentData = component.improve(action, param, chromosome, subcomponent, new AboveBelowChromosomeWinner(aParameter, compsub), null, fit, save, null);

        //ComponentData componentData2 = component.improve(action, param, chromosome2, subcomponent, new MarketFilterChromosomeWinner(), buy, fit);
        param = componentData;
        }
        return param;
    }

    // dup
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
    
}
