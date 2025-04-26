package roart.iclij.evolve;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
import roart.common.constants.Constants;
import roart.common.model.IncDecDTO;
import roart.common.model.MLMetricsDTO;
import roart.common.model.MetaDTO;
import roart.common.model.util.MetaUtil;
import roart.common.util.JsonUtil;
import roart.common.util.TimeUtil;
import roart.component.model.ComponentData;
import roart.db.dao.IclijDbDao;
import roart.evolution.config.EvolutionConfig;
import roart.evolution.marketfilter.jenetics.gene.impl.MarketFilterChromosome;
import roart.evolution.marketfilter.jenetics.gene.impl.MarketFilterCrossover;
import roart.evolution.marketfilter.jenetics.gene.impl.MarketFilterGene;
import roart.evolution.marketfilter.jenetics.gene.impl.MarketFilterMutate;
import roart.iclij.component.Component;
import roart.iclij.config.Market;
import roart.iclij.config.MarketFilter;
import roart.iclij.evolution.fitness.impl.FitnessMarketFilter2;
import roart.iclij.model.Parameters;
import roart.iclij.model.action.MarketActionData;
import roart.iclij.service.util.MiscUtil;
import roart.service.model.ProfitData;

public class MarketFilterEvolveJ extends EvolveJ {

    @Override
    public ComponentData evolve(MarketActionData action, ComponentData param, Market market, ProfitData profitdata, Boolean buy,
            String subcomponent, Parameters parameters, List<MLMetricsDTO> mlTests, Map<String, Object> confMap,
            EvolutionConfig evolutionConfig, String pipeline, Component component, List<String> confList) {
        List<MetaDTO> metas = param.getService().getMetas();
        MetaDTO meta = new MetaUtil().findMeta(metas, market.getConfig().getMarket());
        List<String> categories = new MetaUtil().getCategories(meta);
        double score = -1;
        
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

        FitnessMarketFilter2 fit = new FitnessMarketFilter2(action, new ArrayList<>(), param, profitdata, market, null, pipeline, buy, subcomponent, parameters, mlTests, stockDates, incdecsP);
        final Codec<MarketFilterChromosome, MarketFilterGene> codec = Codec.of(Genotype.of(new MarketFilterChromosome(new MarketFilter(categories))),gt -> (MarketFilterChromosome) gt.getChromosome());
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
            List<Phenotype<MarketFilterGene, Double>> population = result.getPopulation().asList();
            //print(market.getConfig().getMarket() + " " + pipeline + " " + subcomponent, population);
            print2(market.getConfig().getMarket() + " " + subcomponent, pipeline, population);
            List<Genotype<MarketFilterGene>> list2 = result.getGenotypes().asList();
            final Phenotype<MarketFilterGene, Double> pt = result.getBestPhenotype();

            System.out.println(pt);
            MarketFilter aConf = pt.getGenotype().getChromosome().getGene().getAllele();
            confMap.put("some", JsonUtil.convert(aConf));
            param.setUpdateMap(confMap);
            Map<String, Double> scoreMap = new HashMap<>();
            score = pt.getFitness();
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
    
    protected void print2(String title, String subtitle, List<Phenotype<MarketFilterGene, Double>> population) {
        Path path = Paths.get("" + System.currentTimeMillis() + ".txt");
        try (BufferedWriter writer = Files.newBufferedWriter(path)) {
            writer.write(title + "\n\n");
            if (subtitle != null) {
                writer.write(subtitle + "\n\n");
            }
            for (Phenotype<MarketFilterGene, Double> pt : population) {
                MarketFilter filter = pt.getGenotype().getChromosome().getGene().getAllele();
                String individual = pt.getFitness() + " #" + pt.hashCode() + " #" + pt.getGenotype().getChromosome().hashCode() + " #" + pt.getGenotype().getChromosome().getGene().hashCode() + " #" + filter.hashCode() + " " + filter.toString();
                writer.write(individual + "\n");            
                log.info("Individual {}", individual);
            }
            writer.write("\n");
        } catch (IOException e) {
            log.error(Constants.EXCEPTION, e);
        }
    }

}
