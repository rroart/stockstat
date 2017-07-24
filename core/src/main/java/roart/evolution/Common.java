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
import roart.recommender.BuySellRecommend;
import roart.util.MarketData;

public abstract class Common {

    public abstract List<Populus> doit(MyMyConfig conf,Map<String, MarketData> marketdatamap,Map<String, Double[]> listMap,Map<String, Object[]> objectMACDMap,Map<String, Object[]> objectRSIMap, BuySellRecommend recommender) throws Exception;
	
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
    
    protected void mutateList(List<Populus> population, int start, int size,
            int testRecommendMutate, boolean scoreAll, List<String> keys, boolean doBuy) throws JsonParseException, JsonMappingException, IOException {
    Random rand = new Random();
    List<Populus> populationCopies = new ArrayList<>(population);
    int populationSize = Math.min(size, population.size());
    int randMax = populationSize;
    for (int i = 0; i < testRecommendMutate && randMax > 1 ; i--, randMax--) {
        int idx = start + rand.nextInt(randMax - start);
        Populus pop = populationCopies.get(idx);
        populationCopies.remove(idx);
        pop.mutate(keys, doBuy);
   }
    if (!scoreAll) {
        return;
    }
    for (Populus pop : populationCopies) {
        pop.recalculateScore(keys, doBuy);
    }
    }

    protected List<Populus> crossover(int childrenNum, List<Populus> population, List<String> keys, MyMyConfig conf, FitnessBuySellMACD scoring, boolean doScore, boolean doBuy, BuySellRecommend recommend) throws JsonParseException, JsonMappingException, IOException {
        List<Populus> children = new ArrayList<>();
        Random rand = new Random();
        List<Populus> populationCopies = new ArrayList<>(population);
        int populationSize = populationCopies.size();
        int randMax = populationSize;
        for (int i = 0; i < childrenNum * 2 && randMax > 2; i += 2) {
            int idx1 = rand.nextInt(randMax--);
             Populus pop1 = populationCopies.get(idx1);
            populationCopies.remove(idx1);
            int idx2 = rand.nextInt(randMax--);
            Populus pop2= populationCopies.get(idx2);
           populationCopies.remove(idx2);
           Populus pop = new Populus(conf, 0, scoring, recommend).crossover(pop1, pop2, keys, doScore, doBuy);
           children.add(pop);
       }
        return children;
    }

}
