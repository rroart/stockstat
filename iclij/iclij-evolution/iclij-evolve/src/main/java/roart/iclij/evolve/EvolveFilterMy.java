package roart.iclij.evolve;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import roart.common.constants.Constants;
import roart.common.model.IncDecItem;
import roart.common.model.MLMetricsItem;
import roart.common.model.MetaItem;
import roart.common.model.util.MetaUtil;
import roart.common.util.TimeUtil;
import roart.component.model.ComponentData;
import roart.db.dao.IclijDbDao;
import roart.iclij.evolution.chromosome.winner.MarketFilterChromosomeWinner;
import roart.evolution.config.EvolutionConfig;
import roart.iclij.evolution.marketfilter.chromosome.impl.MarketFilterChromosome2;
import roart.evolution.marketfilter.genetics.gene.impl.MarketFilterGene;
import roart.iclij.component.Component;
import roart.iclij.config.Market;
import roart.iclij.evolution.fitness.impl.FitnessMarketFilter;
import roart.iclij.model.Parameters;
import roart.iclij.model.action.MarketActionData;
import roart.iclij.service.util.MiscUtil;
import roart.service.model.ProfitData;

public class EvolveFilterMy extends EvolveMy {

    @Override
    public ComponentData evolve(MarketActionData action, ComponentData param, Market market, ProfitData profitdata,
            Boolean buy, String subcomponent, Parameters parameters, List<MLMetricsItem> mlTests,
            Map<String, Object> confMap, EvolutionConfig evolutionConfig, String pipeline, Component component,
            List<String> confList) {
        List<String> stockDates = param.getService().getDates(market.getConfig().getMarket(), param.getId());
        int verificationdays = param.getConfig().verificationDays();

        List<IncDecItem> allIncDecs = null;
        LocalDate date = param.getFutureDate();
        date = TimeUtil.getBackEqualBefore2(date, verificationdays, stockDates);
        LocalDate prevDate = date.minusDays(market.getConfig().getFindtime());
        try {
            allIncDecs = param.getService().getIo().getIdbDao().getAllIncDecs(market.getConfig().getMarket(), prevDate, date, null);
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        List<IncDecItem> incdecs = allIncDecs; // new MiscUtil().getCurrentIncDecs(date, allIncDecs, market, market.getConfig().getFindtime(), false);
        List<String> parametersList = new MiscUtil().getParameters(incdecs);
        for (String aParameter : parametersList) {
            List<IncDecItem> incdecsP = new MiscUtil().getCurrentIncDecs(incdecs, aParameter);              
        List<MetaItem> metas = param.getService().getMetas();
        MetaItem meta = new MetaUtil().findMeta(metas, market.getConfig().getMarket());
        List<String> categories = new MetaUtil().getCategories(meta);
        MarketFilterGene gene = new MarketFilterGene(market.getFilter(), categories);
        gene.getMarketfilter().categories = categories;
        //MarketFilterChromosome chromosome = new MarketFilterChromosome(action, new ArrayList<>(), param, profitdata, market, null, component.getPipeline(), buy, subcomponent, parameters, gene, mlTests);
        MarketFilterChromosome2 chromosome2 = new MarketFilterChromosome2(new ArrayList<>(), gene);
        FitnessMarketFilter fit = new FitnessMarketFilter(action, new ArrayList<>(), param, profitdata, market, null, component.getPipeline(), buy, subcomponent, parameters, mlTests, stockDates, incdecsP);
        boolean save = false;
        ComponentData componentData = component.improve(action, param, chromosome2, subcomponent, new MarketFilterChromosomeWinner(), buy, fit, save, null);
        param = componentData;
        }
        return param;
    }

}
