package roart.evolution.algorithm;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import roart.executor.MyExecutors;
import roart.common.constants.Constants;
import roart.evolution.chromosome.AbstractChromosome;
import roart.evolution.config.EvolutionConfig;
import roart.evolution.species.Individual;
import roart.evolution.species.Population;
import org.apache.commons.lang3.tuple.Pair;

public abstract class EvolutionAlgorithm {
    protected static Logger log = LoggerFactory.getLogger(EvolutionAlgorithm.class);

    private EvolutionConfig evolutionConfig;
    
    private boolean doParallel = true;

    protected Random rand = new Random();

    private int calc = 0;
    
    private double totalTime = 0;
    
    public Function<AbstractChromosome, Double> fittest;

    public EvolutionAlgorithm(EvolutionConfig evolutionConfig) {
        super();
        this.evolutionConfig = evolutionConfig;
    }

    public EvolutionConfig getEvolutionConfig() {
        return evolutionConfig;
    }

    public void setEvolutionConfig(EvolutionConfig evolutionConfig) {
        this.evolutionConfig = evolutionConfig;
    }

    public boolean doParallel() {
        return doParallel;
    }

    public void setParallel(boolean doParallel) {
        this.doParallel = doParallel;
    }

    public abstract Individual getFittest(EvolutionConfig evolutionConfig, AbstractChromosome recommender, List<String> individuals, List<Pair<Double, AbstractChromosome>> results) throws Exception;

    protected void printmap(Map<String, Object> map) throws JsonProcessingException {
        for (String key : new ArrayList<String>()) {
            Object obj = map.get(key);
            if (obj.getClass().getName().contains("Integer")) {
                int tmpNum = (int) map.get(key);
                System.out.print(" " + tmpNum);
            } else {
                ObjectMapper mapper = new ObjectMapper();
                String jsonInString = mapper.writeValueAsString(obj);
                System.out.println(jsonInString);
            }
        }
        System.out.println("");
    }   

    protected void mutateList(List<Individual> population, int start, int size,
            int testRecommendMutate, boolean scoreAll, boolean doBuy) throws JsonParseException, JsonMappingException, IOException {
        List<Individual> populationCopies = new ArrayList<>(population);
        int populationSize = Math.min(size, population.size());
        int randMax = populationSize;
        for (int i = 0; i < testRecommendMutate && randMax > 1 ; i++, randMax--) {
            int idx = start + rand.nextInt(randMax - start);
            Individual pop = populationCopies.get(idx);
            populationCopies.remove(idx);
            pop.mutate();
        }
    }

    protected List<Individual> crossover(int childrenNum, List<Individual> population, boolean doBuy2, AbstractChromosome recommend) throws JsonParseException, JsonMappingException, IOException {
        List<Individual> children = new ArrayList<>();
        List<Individual> populationCopies = new ArrayList<>(population);
        int populationSize = populationCopies.size();
        int randMax = populationSize;
        for (int i = 0; i < childrenNum * 2 && randMax > 2; i += 2) {
            int idx1 = rand.nextInt(randMax--);
            Individual pop1 = populationCopies.get(idx1);
            populationCopies.remove(idx1);
            int idx2 = rand.nextInt(randMax--);
            Individual pop2= populationCopies.get(idx2);
            populationCopies.remove(idx2);
            Individual pop = pop2.crossover(pop1);
            children.add(pop);
        }
        return children;
    }

    protected List<Individual> created(Integer evolutionGenerationCreate, AbstractChromosome evaluation) throws JsonParseException, JsonMappingException, IOException {
        Population population = new Population(evolutionGenerationCreate, evolutionConfig, evaluation, false);
        return population.getIndividuals();
    }

    protected List<Individual> clonedmutated(Integer evolutionEliteCloneAndMutate, AbstractChromosome evaluation) throws JsonParseException, JsonMappingException, IOException {
        Population population = new Population(evolutionEliteCloneAndMutate, evolutionConfig, evaluation, true);
        List<Individual> list = population.getIndividuals();
        for (Individual individual : list) {
            individual.mutate();
        }
        return list;
    }

    protected boolean calculate(List<Individual> pop) throws InterruptedException, ExecutionException, JsonParseException, JsonMappingException, IOException {
        if (doParallel) {
            calculateParallel(pop);
            return false;
        } else {
            return calculateSeq(pop);
        }
    }
    
    private boolean calculateSeq(List<Individual> pop) throws InterruptedException, ExecutionException, JsonParseException, JsonMappingException, IOException {
        boolean interrupted = false;
    	for (Individual individual : pop) {
            Integer shutdownhour = evolutionConfig.getShutdownhour();
            if (shutdownhour != null) {
            	LocalTime now = LocalTime.now();
            	int minutes = 60 * now.getHour() + now.getMinute();
            	if (calc > 0) {
            		minutes += (totalTime / calc) / 60;
            	}
            	if (minutes >= shutdownhour * 60) {
            		log.error("Interrupting evolution due to time");
            		if (individual.getFitness() == null) {
            			individual.setFitness(-1.0);
            		}
            		interrupted = true;
            		continue;
            	}
            }
            if (individual.getFitness() == null) {
                long start = System.currentTimeMillis();
                if (fittest != null) {
                    individual.setFitness(fittest.apply(individual.getEvaluation()));                   
                } else {
                    individual.recalculateScore();
                }
                individual.setCalculateTime(System.currentTimeMillis() - start);
                calc++;
                totalTime += ((double) individual.getCalculateTime()) / 1000;                        
            }
        }
    	return interrupted;
    }
    
    private void calculateParallel(List<Individual> pop) throws InterruptedException, ExecutionException {
        List<Future<Individual>> futureList = new ArrayList<>();
        for (Individual individual : pop) {
            if (individual.getFitness() == null) {
                Callable callable = new EvolutionCallable(individual);
                Future<Individual> future = MyExecutors.run(callable, 0);
                futureList.add(future);
            }
        }
        for (Future<Individual> future : futureList) {
            Individual individual = future.get();
        }
        if (true) {
            int jj = 0;
        }
    }
    
    class EvolutionCallable implements Callable {
        private Individual individual;
        
        public EvolutionCallable(Individual individual) {
            super();
            this.individual = individual;
         }

        public Individual getIndividual() {
            return individual;
        }

        public void setIndividual(Individual individual) {
            this.individual = individual;
        }

        @Override
        public Object call() throws Exception {
            long start = System.currentTimeMillis();
            if (fittest != null) {
                individual.setFitness(fittest.apply(individual.getEvaluation()));
            } else {
                individual.recalculateScore();
            }
            individual.setCalculateTime(System.currentTimeMillis() - start);
            return null;
        }

    }

    protected void printmap(List<Individual> individuals, List<String> stringIndividuals) {
        for (Individual individual : individuals) {
            log.info("Individual {}", individual);
            if (stringIndividuals != null) {
                stringIndividuals.add(individual.toString());
            }
        }
    }

    public String print(String title, String subtitle, List<String> individuals) {
        Path path = Paths.get("" + System.currentTimeMillis() + ".txt");
        try (BufferedWriter writer = Files.newBufferedWriter(path)) {
            writer.write(title + "\n\n");
            if (subtitle != null) {
                writer.write(subtitle + "\n\n");
            }
            for (String individual : individuals) {
                writer.write(individual + "\n");            
            }
            writer.write("\n");
        } catch (IOException e) {
            log.error(Constants.EXCEPTION, e);
        }
        return path.getFileName().toString();
    }
    
    public String printtext(String title, String subtitle, List<String> individuals) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(title + "\n\n");
        if (subtitle != null) {
            stringBuilder.append(subtitle + "\n\n");
        }
        for (String individual : individuals) {
            stringBuilder.append(individual + "\n");            
        }
        stringBuilder.append("\n");
        return stringBuilder.toString();
    }

}
