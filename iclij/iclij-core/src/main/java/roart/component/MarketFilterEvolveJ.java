package roart.component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.jenetics.Genotype;
import io.jenetics.Phenotype;
import io.jenetics.engine.Codec;
import io.jenetics.engine.Engine;
import io.jenetics.engine.EvolutionResult;
import roart.action.MarketAction;
import roart.common.constants.Constants;
import roart.common.model.MetaItem;
import roart.common.util.JsonUtil;
import roart.common.util.MetaUtil;
import roart.common.util.TimeUtil;
import roart.component.model.ComponentData;
import roart.db.IclijDbDao;
import roart.evolution.config.EvolutionConfig;
import roart.evolution.marketfilter.jenetics.gene.impl.MarketFilterChromosome;
import roart.evolution.marketfilter.jenetics.gene.impl.MarketFilterCrossover;
import roart.evolution.marketfilter.jenetics.gene.impl.MarketFilterGene;
import roart.evolution.marketfilter.jenetics.gene.impl.MarketFilterMutate;
import roart.iclij.config.Market;
import roart.iclij.config.MarketFilter;
import roart.iclij.evolution.fitness.impl.FitnessMarketFilter2;
import roart.iclij.model.IncDecItem;
import roart.iclij.model.MLMetricsItem;
import roart.iclij.model.Parameters;
import roart.iclij.util.MiscUtil;
import roart.service.model.ProfitData;

public class MarketFilterEvolveJ extends EvolveJ {

    @Override
    public ComponentData evolve(MarketAction action, ComponentData param, Market market, ProfitData profitdata, Boolean buy,
            String subcomponent, Parameters parameters, List<MLMetricsItem> mlTests, Map<String, Object> confMap,
            EvolutionConfig evolutionConfig, String pipeline, Component component, List<String> confList) {
        List<MetaItem> metas = param.getService().getMetas();
        MetaItem meta = new MetaUtil().findMeta(metas, market.getConfig().getMarket());
        List<String> categories = new MetaUtil().getCategories(meta);
        double score = -1;
        
        List<String> stockDates = param.getService().getDates(market.getConfig().getMarket());
        int verificationdays = param.getInput().getConfig().verificationDays();

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

        FitnessMarketFilter2 fit = new FitnessMarketFilter2(action, new ArrayList<>(), param, profitdata, market, null, pipeline, buy, subcomponent, parameters, mlTests, stockDates, incdecsP);
        final Codec<MarketFilterChromosome, MarketFilterGene> codec = Codec.of(Genotype.of(new MarketFilterChromosome(new MarketFilter(categories))),gt -> (MarketFilterChromosome) gt.chromosome());
        final Engine<MarketFilterGene, Double> engine = Engine
                .builder(fit::fitness, codec)
                .populationSize(evolutionConfig.getSelect())
                .alterers(
                        //new MeanAlterer<>(0.175),
                        new MarketFilterMutate(),
                        new MarketFilterCrossover<>(0.5)
                        )
                .build();
        
        Map<String, String> retMap = new HashMap<>();
        try {
            final EvolutionResult<MarketFilterGene, Double> result = engine.stream()
                    .limit(evolutionConfig.getGenerations())
                    .collect(EvolutionResult.toBestEvolutionResult());
            List<Phenotype<MarketFilterGene, Double>> population = result.population().asList();
            print(market.getConfig().getMarket() + " " + pipeline + " " + subcomponent, population);
            List<Genotype<MarketFilterGene>> list2 = result.genotypes().asList();
            final Phenotype<MarketFilterGene, Double> pt = result.bestPhenotype();

            System.out.println(pt);
            MarketFilter aConf = pt.genotype().chromosome().gene().allele();
            confMap.put("some", JsonUtil.convert(aConf));
            param.setUpdateMap(confMap);
            Map<String, Double> scoreMap = new HashMap<>();
            score = pt.fitness();
            //confMap.put("score", "" + score);
            scoreMap.put("" + score, score);
            param.setScoreMap(scoreMap);
            param.setFutureDate(LocalDate.now());
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        }
        return param;
    }
    
}
