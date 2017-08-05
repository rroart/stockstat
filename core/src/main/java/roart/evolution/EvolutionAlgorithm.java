package roart.evolution;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import roart.config.MyMyConfig;
import roart.evaluation.Evaluation;

public abstract class EvolutionAlgorithm {

    public abstract Individual getFittest(MyMyConfig conf,Evaluation recommender) throws Exception;
	
    protected void printmap(Map<String, Object> map, List<String> keys) throws JsonProcessingException {
        for (String key : keys) {
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
            int testRecommendMutate, boolean scoreAll, List<String> keys, boolean doBuy) throws JsonParseException, JsonMappingException, IOException {
    Random rand = new Random();
    List<Individual> populationCopies = new ArrayList<>(population);
    int populationSize = Math.min(size, population.size());
    int randMax = populationSize;
    for (int i = 0; i < testRecommendMutate && randMax > 1 ; i--, randMax--) {
        int idx = start + rand.nextInt(randMax - start);
        Individual pop = populationCopies.get(idx);
        populationCopies.remove(idx);
        pop.mutate(keys);
   }
    if (!scoreAll) {
        return;
    }
    for (Individual pop : populationCopies) {
        pop.recalculateScore(keys);
    }
    }

    protected List<Individual> crossover(int childrenNum, List<Individual> population, List<String> keys, MyMyConfig conf, boolean doScore, boolean doBuy2, Evaluation recommend) throws JsonParseException, JsonMappingException, IOException {
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
            Individual pop = new Individual(conf, 0, recommend).crossover(pop1, pop2, keys, doScore);
            children.add(pop);
        }
        return children;
    }

    protected List<Individual> created(Integer evolutionGenerationCreate, MyMyConfig conf, Evaluation evaluation, List<String> keys) throws JsonParseException, JsonMappingException, IOException {
        Population population = new Population(evolutionGenerationCreate, conf, evaluation, keys);
        return population.getIndividuals();
    }
    
}
