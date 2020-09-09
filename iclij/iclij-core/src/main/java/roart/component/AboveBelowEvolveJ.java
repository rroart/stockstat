package roart.component;

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
import io.jenetics.engine.Engine;
import io.jenetics.engine.EvolutionResult;
import roart.action.MarketAction;
import roart.common.constants.Constants;
import roart.common.util.JsonUtil;
import roart.component.model.ComponentData;
import roart.evolution.config.EvolutionConfig;
import roart.iclij.config.Market;
import roart.iclij.evolution.fitness.impl.FitnessAboveBelow2;
import roart.iclij.model.MLMetricsItem;
import roart.iclij.model.Parameters;
import roart.service.model.ProfitData;

public class AboveBelowEvolveJ extends EvolveJ {

    @Override
    public double evolve(MarketAction action, ComponentData param, Market market, ProfitData profitdata, Boolean buy,
            String subcomponent, Parameters parameters, List<MLMetricsItem> mlTests, Map<String, Object> confMap,
            EvolutionConfig evolutionConfig, String pipeline) {
        double score = -1;
        FitnessAboveBelow2 fit = new FitnessAboveBelow2(); //action, new ArrayList<>(), param, profitdata, market, null, pipeline, buy, subcomponent, parameters, mlTests, null);
        int size = 7;
        //final Codec<BitChromosome, BitGene> codec = Codec.of(Genotype.of(new BitChromosome(new Boolean(), size)),gt -> (BitChromosome) gt.chromosome());
        final Engine<BitGene, Double> engine = Engine
                //.bui
                .builder(fit::fitness3, BitChromosome.of(size))
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
            final EvolutionResult<BitGene, Double> result = engine.stream()
                    .limit(evolutionConfig.getGenerations())
                    .collect(EvolutionResult.toBestEvolutionResult());
            List<Phenotype<BitGene, Double>> population = result.population().asList();
            print2(market.getConfig().getMarket() + " " + pipeline + " " + subcomponent, population);
            List<Genotype<BitGene>> list2 = result.genotypes().asList();
            final Phenotype<BitGene, Double> pt = result.bestPhenotype();

            System.out.println(pt);
            Boolean aConf = pt.genotype().chromosome().gene().allele();
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
        return score;
    }
    
    protected void print2(String title, List<Phenotype<BitGene, Double>> population) {
        Path path = Paths.get("" + System.currentTimeMillis() + ".txt");
        try (BufferedWriter writer = Files.newBufferedWriter(path)) {
            writer.write(title + "\n\n");
            for (Phenotype<BitGene, Double> pt : population) {
                Boolean filter = pt.genotype().chromosome().gene().allele();
                String individual = pt.fitness() + " #" + pt.hashCode() + " " + filter.toString();
                writer.write(individual + "\n");            
            }
            writer.write("\n");
        } catch (IOException e) {
            log.error(Constants.EXCEPTION, e);
        }
    }

}
