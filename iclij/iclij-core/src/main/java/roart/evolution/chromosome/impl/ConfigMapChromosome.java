package roart.evolution.chromosome.impl;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import roart.common.config.MyMyConfig;
import roart.evolution.chromosome.AbstractChromosome;
import roart.evolution.species.Individual;

@JsonTypeInfo(  
        use = JsonTypeInfo.Id.NAME,  
        include = JsonTypeInfo.As.PROPERTY,  
        property = "_class")  
@JsonSubTypes({  
    @Type(value = MLMACDChromosome.class, name = "roart.evolution.chromosome.impl.MLMACDChromosome") })  
public class ConfigMapChromosome extends AbstractChromosome {
    private Map<String, Object> map;
    
    private MyMyConfig conf;
    
    private List<String> confList;
    
    public ConfigMapChromosome(MyMyConfig conf, List<String> confList) {
        this.conf = conf;
        this.confList = confList;
    }
    
    public Map<String, Object> getMap() {
        return map;
    }

    public void setMap(Map<String, Object> map) {
        this.map = map;
    }

    public MyMyConfig getConf() {
        return conf;
    }

    public void setConf(MyMyConfig conf) {
        this.conf = conf;
    }

    public List<String> getConfList() {
        return confList;
    }

    public void setConfList(List<String> confList) {
        this.confList = confList;
    }

    @Override
    public void getRandom() throws JsonParseException, JsonMappingException, IOException {
        Random rand = new Random();
        for (int conf = 0; conf < confList.size(); conf++) {
            generateConfigNum(rand, conf);
        }
    }

    @Override
    public void mutate() {
        Random rand = new Random();
        int conf = rand.nextInt(confList.size());
        generateConfigNum(rand, conf);
    }

    private void generateConfigNum(Random rand, int conf) {
        String confName = confList.get(conf);
        Double[] range = this.conf.getRange().get(confName);
        Class type = this.conf.getType().get(confName);
        if (type == Integer.class) {
            Integer i = (range[0].intValue()) + rand.nextInt(range[1].intValue() - range[0].intValue());
            map.put(confName, i);
        }
        if (type == Double.class) {
            Double d = (range[0]) + rand.nextDouble() * (range[1] - range[0]);
            map.put(confName, d);
        }
    }
 
    @Override
    public Individual crossover(AbstractChromosome other) {
        ConfigMapChromosome chromosome = new ConfigMapChromosome(conf, confList);
        Random rand = new Random();
        for (int conf = 0; conf < confList.size(); conf++) {
            String confName = confList.get(conf);
            if (rand.nextBoolean()) {
                chromosome.map.put(confName, this.map.get(confName));
            } else {
                chromosome.map.put(confName, ((ConfigMapChromosome) other).map.get(confName));
            }
        }
        return new Individual(chromosome);
    }

    @Override
    public double getEvaluations(int j) throws JsonParseException, JsonMappingException, IOException {
        return 0;
    }

    @Override
    public void transformToNode() throws JsonParseException, JsonMappingException, IOException {
    }

    @Override
    public void normalize() {
    }

    @Override
    public void transformFromNode() throws JsonParseException, JsonMappingException, IOException {
    }

    @Override
    public double getFitness() throws JsonParseException, JsonMappingException, IOException {
        return 0;
    }

    @Override
    public AbstractChromosome copy() {
        // TODO Auto-generated method stub
        ConfigMapChromosome chromosome = new ConfigMapChromosome(conf, confList);
        chromosome.map = this.map;
        return chromosome;
    }

    @Override
    public boolean isEmpty() {
        return confList == null || confList.isEmpty();
    }

    @Override
    public String toString() {
        return confList.toString();
    }

    @Override
    public boolean isAscending() {
        return true;
    }

}
