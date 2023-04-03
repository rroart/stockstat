package roart.iclij.evolve;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
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
import roart.common.model.MLMetricsItem;
import roart.common.util.JsonUtil;
import roart.component.model.ComponentData;
import roart.evolution.config.EvolutionConfig;
import roart.evolution.marketfilter.jenetics.gene.impl.ConfigMapChromosome;
import roart.evolution.marketfilter.jenetics.gene.impl.ConfigMapGene;
import roart.iclij.component.Component;
import roart.iclij.config.Market;
import roart.iclij.evolution.fitness.impl.FitnessAboveBelow2;
import roart.iclij.evolution.fitness.impl.FitnessConfigMap;
import roart.iclij.evolution.fitness.impl.FitnessConfigMap2;
import roart.iclij.model.Parameters;
import roart.iclij.model.action.MarketActionData;
import roart.service.model.ProfitData;

public class ConfigMapEvolveJ extends EvolveJ {

    @Override
    public ComponentData evolve(MarketActionData action, ComponentData param, Market market, ProfitData profitdata, Boolean buy,
            String subcomponent, Parameters parameters, List<MLMetricsItem> mlTests, Map<String, Object> confMap,
            EvolutionConfig evolutionConfig, String pipeline, Component component, List<String> confList) {
        double score = -1;
        FitnessConfigMap2 fit = new FitnessConfigMap2(); //action, new ArrayList<>(), param, profitdata, market, null, pipeline, buy, subcomponent, parameters, mlTests, null);
        int size = 7;
        final Codec<ConfigMapChromosome, ConfigMapGene> codec = null; //Codec.of(Genotype.of(new ConfigMapChromosome()),gt -> (ConfigMapChromosome) gt.chromosome());
        final Engine<ConfigMapGene, Double> engine = Engine
                //.bui
                .builder(fit::fitness, codec /*ConfigMapChromosome.of(size)*/)
                .populationSize(evolutionConfig.getSelect())
                /*
                .alterers(
                        //new MeanAlterer<>(0.175),
                        new BitMutate(),
                        new BitCrossover<>(0.5)
                        )
                        */
                .build();
        
        Map<String, String> retMap = new HashMap<>();
        try {
            final EvolutionResult<ConfigMapGene, Double> result = engine.stream()
                    .limit(evolutionConfig.getGenerations())
                    .collect(EvolutionResult.toBestEvolutionResult());
            List<Phenotype<ConfigMapGene, Double>> population = result.getPopulation().asList();
            print2(market.getConfig().getMarket() + " " + pipeline + " " + subcomponent, population);
            List<Genotype<ConfigMapGene>> list2 = result.getGenotypes().asList();
            final Phenotype<ConfigMapGene, Double> pt = result.getBestPhenotype();

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

    protected void print2(String title, List<Phenotype<ConfigMapGene, Double>> population) {
        Path path = Paths.get("" + System.currentTimeMillis() + ".txt");
        try (BufferedWriter writer = Files.newBufferedWriter(path)) {
            writer.write(title + "\n\n");
            for (Phenotype<ConfigMapGene, Double> pt : population) {
                Map filter = pt.getGenotype().getChromosome().getGene().getAllele();
                String individual = pt.getFitness() + " #" + pt.hashCode() + " " + filter.toString();
                writer.write(individual + "\n");            
            }
            writer.write("\n");
        } catch (IOException e) {
            log.error(Constants.EXCEPTION, e);
        }
    }

}
