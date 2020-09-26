package roart.component;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.jenetics.BitChromosome;
import io.jenetics.BitGene;
import io.jenetics.Genotype;
import io.jenetics.Phenotype;
import io.jenetics.engine.Engine;
import io.jenetics.engine.EvolutionResult;
import roart.action.MarketAction;
import roart.common.constants.Constants;
import roart.common.util.JsonUtil;
import roart.common.util.TimeUtil;
import roart.component.model.ComponentData;
import roart.db.IclijDbDao;
import roart.evolution.config.EvolutionConfig;
import roart.iclij.config.Market;
import roart.iclij.evolution.fitness.impl.FitnessAboveBelow2;
import roart.iclij.model.IncDecItem;
import roart.iclij.model.MLMetricsItem;
import roart.iclij.model.Parameters;
import roart.iclij.util.MiscUtil;
import roart.service.model.ProfitData;

public class AboveBelowEvolveJ extends EvolveJ {

    @Override
    public ComponentData evolve(MarketAction action, ComponentData param, Market market, ProfitData profitdata, Boolean buy,
            String subcomponent, Parameters parameters, List<MLMetricsItem> mlTests, Map<String, Object> confMap,
            EvolutionConfig evolutionConfig, String pipeline, Component component, List<String> confList) {
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
            List<String> components = new ArrayList<>();
            List<String> subcomponents = new ArrayList<>();
            getComponentLists(incdecsP, components, subcomponents);
            Parameters realParameters = JsonUtil.convert(aParameter, Parameters.class);
        FitnessAboveBelow2 fit = new FitnessAboveBelow2(action, new ArrayList<>(), param, profitdata, market, null, pipeline, buy, subcomponent, parameters, null, incdecsP, components, subcomponents, stockDates);
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
        }
        return param;
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

    // dup
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
    
}
