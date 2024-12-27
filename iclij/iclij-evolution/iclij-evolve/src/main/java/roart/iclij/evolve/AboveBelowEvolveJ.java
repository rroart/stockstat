package roart.iclij.evolve;

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
import io.jenetics.Chromosome;
import io.jenetics.Genotype;
import io.jenetics.Phenotype;
import io.jenetics.engine.Engine;
import io.jenetics.engine.EvolutionResult;
import roart.common.constants.Constants;
import roart.common.model.IncDecItem;
import roart.common.model.MLMetricsItem;
import roart.common.util.JsonUtil;
import roart.common.util.TimeUtil;
import roart.component.model.ComponentData;
import roart.db.dao.IclijDbDao;
import roart.evolution.config.EvolutionConfig;
import roart.iclij.component.Component;
import roart.iclij.config.Market;
import roart.iclij.evolution.fitness.impl.FitnessAboveBelow2;
import roart.iclij.model.Parameters;
import roart.iclij.model.action.MarketActionData;
import roart.iclij.service.util.MiscUtil;
import roart.service.model.ProfitData;

public class AboveBelowEvolveJ extends EvolveJ {

    @Override
    public ComponentData evolve(MarketActionData action, ComponentData param, Market market, ProfitData profitdata, Boolean buy,
            String subcomponent, Parameters parameters, List<MLMetricsItem> mlTests, Map<String, Object> confMap,
            EvolutionConfig evolutionConfig, String pipeline, Component component, List<String> confList) {
        double score = -1;
        List<String> stockDates = param.getService().getDates(market.getConfig().getMarket());
        int verificationdays = param.getConfig().verificationDays();
        List<IncDecItem> allIncDecs = null;
        LocalDate date = param.getFutureDate();
        date = TimeUtil.getBackEqualBefore2(date, verificationdays, stockDates);
        LocalDate prevDate = date.minusDays(market.getConfig().getFindtime());
        try {
            allIncDecs = action.getDbDao().getAllIncDecs(market.getConfig().getMarket(), prevDate, date, null);
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
        FitnessAboveBelow2 fit = new FitnessAboveBelow2(action, new ArrayList<>(), param, profitdata, market, null, pipeline, buy, subcomponent, realParameters, null, incdecsP, components, subcomponents, stockDates);
        List<String> compsub = new ArrayList<>();
        compsub.addAll(components);
        compsub.addAll(subcomponents);
        int size = compsub.size();
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
            List<Phenotype<BitGene, Double>> population = result.getPopulation().asList();
            print2(market.getConfig().getMarket() + " " + pipeline + " " + subcomponent, population);
            List<Genotype<BitGene>> list2 = result.getGenotypes().asList();
            final Phenotype<BitGene, Double> pt = result.getBestPhenotype();

            System.out.println(pt);
            List<Boolean> bits = new ArrayList<>(); 
            for (int i = 0; i < pt.getGenotype().getChromosome().length(); i++) {
                Boolean aConf = pt.getGenotype().getChromosome().getGene(i).getAllele();
                bits.add(aConf);
            }
            Map<String, Object> aConfMap = new HashMap<>();
            //int aConf = null;
            score = handleWinner(param, bits, compsub, aConfMap, aParameter, pt);
              //confMap.put("some", JsonUtil.convert(aConf));
            //param.setUpdateMap(confMap);
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
    
    protected void print2(String title, List<Phenotype<BitGene, Double>> population) {
        Path path = Paths.get("" + System.currentTimeMillis() + ".txt");
        try (BufferedWriter writer = Files.newBufferedWriter(path)) {
            writer.write(title + "\n\n");
            for (Phenotype<BitGene, Double> pt : population) {
                Boolean filter = pt.getGenotype().getChromosome().getGene().getAllele();
                String individual = pt.getFitness() + " #" + pt.hashCode() + " #" + pt.getGenotype().getChromosome().hashCode() + " #" + pt.getGenotype().getChromosome().getGene().hashCode() + " #" + filter.hashCode() + " " + filter.toString();
               // String individual = pt.fitness() + " #" + pt.hashCode() + " " + filter.toString();
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
    
    public double handleWinner(ComponentData param, List<Boolean> aConf, List<String> compsub, Map<String, Object> confMap, String parameters, Phenotype<BitGene, Double> pt) {
        String conf = "";
        for (int i1 = 0; i1 < compsub.size(); i1++) {
            Boolean b = aConf.get(i1);
            if (b) {
                conf = conf + compsub.get(i1) + ", ";
            }
        }
        confMap.put(parameters, conf);
        param.setUpdateMap(confMap);
        double score = pt.getFitness();
        return score;
    }

}