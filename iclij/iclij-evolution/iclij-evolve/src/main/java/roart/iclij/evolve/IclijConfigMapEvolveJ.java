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

import io.jenetics.BitChromosome;
import io.jenetics.BitGene;
import io.jenetics.Genotype;
import io.jenetics.Phenotype;
import io.jenetics.engine.Codec;
import io.jenetics.engine.Engine;
import io.jenetics.engine.EvolutionResult;
import roart.common.constants.Constants;
import roart.common.model.MLMetricsDTO;
import roart.common.util.JsonUtil;
import roart.component.model.ComponentData;
import roart.evolution.config.EvolutionConfig;
import roart.evolution.iclijconfigmap.jenetics.gene.impl.IclijConfigMapChromosome;
import roart.evolution.iclijconfigmap.jenetics.gene.impl.IclijConfigMapCrossover;
import roart.evolution.iclijconfigmap.jenetics.gene.impl.IclijConfigMapGene;
import roart.evolution.iclijconfigmap.jenetics.gene.impl.IclijConfigMapMutate;
import roart.iclij.component.Component;
import roart.iclij.config.Market;
import roart.iclij.config.MarketFilter;
import roart.iclij.evolution.fitness.impl.FitnessAboveBelow2;
import roart.iclij.evolution.fitness.impl.FitnessIclijConfigMap2;
import roart.iclij.model.Parameters;
import roart.iclij.model.action.MarketActionData;
import roart.service.model.ProfitData;

public class IclijConfigMapEvolveJ extends EvolveJ {

    @Override
    public ComponentData evolve(MarketActionData action, ComponentData param, Market market, ProfitData profitdata, Boolean buy,
            String subcomponent, Parameters parameters, List<MLMetricsDTO> mlTests, Map<String, Object> confMap,
            EvolutionConfig evolutionConfig, String pipeline, Component component, List<String> confList) {
        double score = -1;
        Map<String, Object> map = new HashMap<>();
        List<String> stockDates = param.getService().getDates(market.getConfig().getMarket(), param.getId());
        FitnessIclijConfigMap2 fit = new FitnessIclijConfigMap2(action, param, profitdata, market, null, pipeline, buy, subcomponent, parameters, null, stockDates);
        int size = 7;
        //final Codec<IclijConfigMapChromosome, IclijConfigMapGene> codec = Codec.of(Genotype.of(new BitChromosome(new Boolean(), size)),gt -> (BitChromosome) gt.chromosome());
        final Codec<IclijConfigMapChromosome, IclijConfigMapGene> codec = Codec.of(Genotype.of(new IclijConfigMapChromosome(confList, param.getConfig())),gt -> (IclijConfigMapChromosome) gt.getChromosome());
        final Engine<IclijConfigMapGene, Double> engine = Engine
                //.bui
                .builder(fit::fitness, codec /*IclijConfigMapChromosome.of(size)*/)
                .populationSize(evolutionConfig.getSelect())
                .alterers(
                        //new MeanAlterer<>(0.175),
                        //new IclijConfigMapMutate()
                        new IclijConfigMapCrossover(0.5)
                        )
                .build();
        
        Map<String, String> retMap = new HashMap<>();
        try {
            final EvolutionResult<IclijConfigMapGene, Double> result = engine.stream()
                    .limit(evolutionConfig.getGenerations())
                    .collect(EvolutionResult.toBestEvolutionResult());
            List<Phenotype<IclijConfigMapGene, Double>> population = result.getPopulation().asList();
            print2(market.getConfig().getMarket() + " " + subcomponent, pipeline, population);
            List<Genotype<IclijConfigMapGene>> list2 = result.getGenotypes().asList();
            final Phenotype<IclijConfigMapGene, Double> pt = result.getBestPhenotype();

            System.out.println(pt);
            Map aConf = pt.getGenotype().getChromosome().getGene().getAllele();
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
        return param;
    }

    protected void print2(String title, String subtitle, List<Phenotype<IclijConfigMapGene, Double>> population) {
        Path path = Paths.get("" + System.currentTimeMillis() + ".txt");
        try (BufferedWriter writer = Files.newBufferedWriter(path)) {
            writer.write(title + "\n\n");
            if (subtitle != null) {
                writer.write(subtitle + "\n\n");
            }
            for (Phenotype<IclijConfigMapGene, Double> pt : population) {
                Map filter = pt.getGenotype().getChromosome().getGene().getAllele();
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
