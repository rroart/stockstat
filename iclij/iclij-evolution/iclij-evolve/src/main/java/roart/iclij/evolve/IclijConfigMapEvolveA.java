package roart.iclij.evolve;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.genetics.Chromosome;
import org.apache.commons.math3.genetics.ElitisticListPopulation;
import org.apache.commons.math3.genetics.GeneticAlgorithm;
import org.apache.commons.math3.genetics.OnePointCrossover;
import org.apache.commons.math3.genetics.Population;
import org.apache.commons.math3.genetics.StoppingCondition;
import org.apache.commons.math3.genetics.TournamentSelection;
import org.apache.commons.math3.util.Precision;

import tools.jackson.core.exc.StreamReadException;
import tools.jackson.databind.DatabindException;

import roart.common.constants.Constants;
import roart.common.model.MLMetricsDTO;
import roart.common.util.JsonUtil;
import roart.component.model.ComponentData;
import roart.component.model.SimulateInvestData;
import roart.evolution.config.EvolutionConfig;
import roart.evolution.iclijconfigmap.genetics.gene.impl.IclijConfigMapChromosome;
import roart.evolution.iclijconfigmap.genetics.gene.impl.IclijConfigMapGene;
import roart.iclij.component.Component;
import roart.iclij.config.Market;
import roart.iclij.evolution.fitness.impl.FitnessIclijConfigMap;
import roart.iclij.evolution.fitness.impl.FitnessIclijConfigMap3;
import roart.iclij.evolution.fitness.impl.IclijConfigMapChromosome3;
import roart.iclij.model.Parameters;
import roart.iclij.model.action.MarketActionData;
import roart.service.model.ProfitData;

public class IclijConfigMapEvolveA extends EvolveA {

    public static final int    POPULATION_SIZE   = 40;
    public static final double CROSSOVER_RATE    = 0.9;
    public static final double MUTATION_RATE     = 0.03;
    public static final double ELITISM_RATE      = 0.1;
    public static final int    TOURNAMENT_ARITY  = 2;
    public static final int DIMENSION = 42;

    @Override
    public ComponentData evolve(MarketActionData action, ComponentData param, Market market, ProfitData profitdata, Boolean buy,
            String subcomponent, Parameters parameters, List<MLMetricsDTO> mlTests, Map<String, Object> confMap,
            EvolutionConfig evolutionConfig, String pipeline, Component component, List<String> confList) {
        double score = -1;
        long startTime = System.currentTimeMillis();

        SimulateInvestData param2 = (SimulateInvestData) param;
        List<String> stockDates = param2.getStockDates();
        IclijConfigMapGene gene = new IclijConfigMapGene(confList, param.getConfig());
        IclijConfigMapChromosome chromosome = new IclijConfigMapChromosome(gene);
        //loadme(param, chromosome, market, confList, buy, subcomponent, action, parameters);
        FitnessIclijConfigMap3 fit = new FitnessIclijConfigMap3(action, param, profitdata, market, null, component.getPipeline(), buy, subcomponent, parameters, gene, stockDates);

        // initialize a new genetic algorithm
        GeneticAlgorithm ga = new GeneticAlgorithm(new IclijConfigMapCrossoverA(), CROSSOVER_RATE,
                new IclijConfigMapMutateA(), MUTATION_RATE,
                new TournamentSelection(TOURNAMENT_ARITY));

        // initial population
        Population initial = getInitialPopulation(fit, gene);

        // stopping condition
        StoppingCondition stoppingCondition = new StoppingCondition() {

            int generation = 0;

            @Override
            public boolean isSatisfied(Population population) {
                Chromosome fittestChromosome = population.getFittestChromosome();

                if (generation == 1 || generation % 10 == 0) {
                    System.out.println("Generation " + generation + ": " + fittestChromosome.toString());
                }
                generation++;

                if (true) {
                    return generation > 100;
                }
                double fitness = fittestChromosome.fitness();
                if (Precision.equals(fitness, 0.0, 1e-6)) {
                    return true;
                } else {
                    return false;
                }
            }
        };

        System.out.println("Starting evolution ...");

        // run the algorithm
        Population finalPopulation = ga.evolve(initial, stoppingCondition);

        // Get the end time for the simulation.
        long endTime = System.currentTimeMillis();

        // best chromosome from the final population
        Chromosome best = finalPopulation.getFittestChromosome();
        System.out.println("Generation " + ga.getGenerationsEvolved() + ": " + best.toString());
        System.out.println("Total execution time: " + (endTime - startTime) + "ms");
        print2(market.getConfig().getMarket() + " " + subcomponent, pipeline, finalPopulation);
        //List<Genotype<IclijConfigMapGene>> list2 = finalPopulation.
        //final Phenotype<IclijConfigMapGene, Double> pt = result.bestPhenotype();

        System.out.println(best);
        Map aConf = ((IclijConfigMapChromosome3)best).getGene().getMap();
        confMap.put("some", JsonUtil.convert(aConf));
        param.setUpdateMap(confMap);
        Map<String, Double> scoreMap = new HashMap<>();
        score = best.getFitness();
        //confMap.put("score", "" + score);
        scoreMap.put("" + score, score);
        param.setScoreMap(scoreMap);
        param.setFutureDate(LocalDate.now());
        return param;
    }

    private Population getInitialPopulation(FitnessIclijConfigMap3 fit, IclijConfigMapGene gene) {
        List<Chromosome> popList = new LinkedList<Chromosome>();

        for (int i = 0; i < POPULATION_SIZE; i++) {
            IclijConfigMapChromosome3 aChromosome = new IclijConfigMapChromosome3(gene.copy(), fit);
            try {
                aChromosome.getRandom();
            } catch (Exception e) {
                log.error(Constants.EXCEPTION, e);
            }
            popList.add(aChromosome);
        }
        return new ElitisticListPopulation(popList, 2 * popList.size(), ELITISM_RATE);
    }

    protected void print2(String title, String subtitle, Population finalPopulation) {
        Path path = Paths.get("" + System.currentTimeMillis() + ".txt");
        try (BufferedWriter writer = Files.newBufferedWriter(path)) {
            writer.write(title + "\n\n");
            if (subtitle != null) {
                writer.write(subtitle + "\n\n");
            }
            List<Chromosome> finalPopulation2 = new ArrayList<>();
            for (Chromosome aChromosome : finalPopulation) {
                finalPopulation2.add(aChromosome);
            }
            Comparator<Chromosome> compareById = new Comparator<>() {
                @Override
                public int compare(Chromosome o1, Chromosome o2) {
                    return Double.valueOf(o1.getFitness()).compareTo(Double.valueOf(o2.getFitness()));
                }
            };
            Collections.sort(finalPopulation2, compareById);
            for (Chromosome aChromosome : finalPopulation2) {
                Map filter = ((IclijConfigMapChromosome3) aChromosome).getGene().getMap();
                String individual = aChromosome.fitness() + " #" + aChromosome.hashCode() + " #" + ((IclijConfigMapChromosome3)aChromosome).getGene().hashCode() + " #" + ((IclijConfigMapChromosome3)aChromosome).getGene().getMap().hashCode() + " #" + filter.hashCode() + " " + filter.toString();
                writer.write(individual + "\n");            
                log.info("Individual {}", individual);
            }
            writer.write("\n");
        } catch (IOException e) {
            log.error(Constants.EXCEPTION, e);
        }
    }

}
