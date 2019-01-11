package roart.evolution;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.jfree.util.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import roart.common.config.EvolutionConfig;
import roart.evaluation.Evaluation;
import roart.queue.MyExecutors;

public abstract class EvolutionAlgorithm {
    protected static Logger log = LoggerFactory.getLogger(EvolutionAlgorithm.class);

    private EvolutionConfig evolutionConfig;
    
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

    public abstract Individual getFittest(EvolutionConfig evolutionConfig, Evaluation recommender) throws Exception;

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
        Random rand = new Random();
        List<Individual> populationCopies = new ArrayList<>(population);
        int populationSize = Math.min(size, population.size());
        int randMax = populationSize;
        for (int i = 0; i < testRecommendMutate && randMax > 1 ; i--, randMax--) {
            int idx = start + rand.nextInt(randMax - start);
            Individual pop = populationCopies.get(idx);
            populationCopies.remove(idx);
            pop.mutate();
        }
    }

    protected List<Individual> crossover(int childrenNum, List<Individual> population, boolean doBuy2, Evaluation recommend) throws JsonParseException, JsonMappingException, IOException {
        List<Individual> children = new ArrayList<>();
        Random rand = new Random();
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

    protected List<Individual> created(Integer evolutionGenerationCreate, Evaluation evaluation) throws JsonParseException, JsonMappingException, IOException {
        Population population = new Population(evolutionGenerationCreate, evolutionConfig, evaluation, false);
        return population.getIndividuals();
    }

    protected List<Individual> clonedmutated(Integer evolutionEliteCloneAndMutate, Evaluation evaluation) throws JsonParseException, JsonMappingException, IOException {
        Population population = new Population(evolutionEliteCloneAndMutate, evolutionConfig, evaluation, true);
        List<Individual> list = population.getIndividuals();
        for (Individual individual : list) {
            individual.mutate();
        }
        return list;
    }

    protected void calculate(List<Individual> pop) throws InterruptedException, ExecutionException {
        List<Future<Individual>> futureList = new ArrayList<>();
        for (Individual individual : pop) {
            if (individual.getFitness() == null) {
                Callable callable = new EvolutionCallable(individual);
                Future<Individual> future = MyExecutors.run(callable);
                futureList.add(future);
            }
        }
        for (Future<Individual> future : futureList) {
            Individual individual = future.get();
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
            individual.recalculateScore();
            individual.setCalculateTime(System.currentTimeMillis() - start);
            return null;
        }
    }

    protected void printmap(List<Individual> individuals) {
        for (Individual individual : individuals) {
            log.info("Individual {}", individual);
        }
    }

}
