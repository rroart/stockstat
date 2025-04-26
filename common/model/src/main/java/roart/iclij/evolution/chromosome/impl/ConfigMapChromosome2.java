package roart.iclij.evolution.chromosome.impl;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import roart.evolution.chromosome.AbstractChromosome;
import roart.evolution.species.Individual;
import roart.gene.impl.ConfigMapGene;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        property = "_class")
@JsonSubTypes({  
    @Type(value = MLAggregatorChromosome.class, name = "MLAggregatorChromosome"),
    @Type(value = MLIndicatorChromosome.class, name = "MLIndicatorChromosome"),
    @Type(value = PredictorChromosome.class, name = "PredictorChromosome"),
    @Type(value = RecommenderChromosome2.class, name = "RecommenderChromosome2")
})
public class ConfigMapChromosome2 extends AbstractChromosome {
    protected ConfigMapGene gene;
    
    public ConfigMapChromosome2(ConfigMapGene gene) {
        this.gene = gene;
    }

    public ConfigMapChromosome2(ConfigMapChromosome2 chromosome) {
        this(chromosome.gene);
    }

    public ConfigMapChromosome2() {
        // Json
    }

    public ConfigMapGene getGene() {
        return gene;
    }

    public void setGene(ConfigMapGene gene) {
        this.gene = gene;
    }

    public Map<String, Object> getMap() {
        return gene.getMap();
    }

    public void setMap(Map<String, Object> map) {
        gene.setMap(map);
    }

    public List<String> getConfList() {
        return gene.getConfList();
    }

    public void setConfList(List<String> confList) {
        gene.setConfList(confList);
    }

    @Override
    public void mutate() {
        gene.mutate();
    }

    @Override
    public void getRandom() throws JsonParseException, JsonMappingException, IOException {
        gene.randomize();
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

    /*
    private void configSaves(ComponentData param, Map<String, Object> anUpdateMap) {
        for (Entry<String, Object> entry : anUpdateMap.entrySet()) {
            String key = entry.getKey();
            Object object = entry.getValue();
            ConfigDTO configItem = new ConfigDTO();
            configItem.setAction(param.getAction());
            configItem.setComponent(componentName);
            configItem.setDate(LocalDate.now());
            configItem.setId(key);
            configItem.setMarket(param.getMarket());
            configItem.setRecord(LocalDate.now());
            configItem.setSubcomponent(subcomponent);
            String value = JsonUtil.convert(object);
            configItem.setValue(value);
            try {
                configItem.save();
            } catch (Exception e) {
                log.info(Constants.EXCEPTION, e);
            }
        }
    }
    */

    @Override
    public Individual crossover(AbstractChromosome chromosome) {
        ConfigMapGene newNNConfig =  (ConfigMapGene) gene.crossover(((ConfigMapChromosome2) chromosome).gene);
        ConfigMapChromosome2 eval = new ConfigMapChromosome2(newNNConfig);
        //MarketFilterChromosome eval = new MarketFilterChromosome(conf, ml, dataReaders, categories, key, newNNConfig, catName, cat, neuralnetcommand);
        return new Individual(eval);
    }

    @Override
    public AbstractChromosome copy() {
        ConfigMapChromosome2 chromosome = new ConfigMapChromosome2(gene);
        chromosome.gene = gene.copy();
        return chromosome;
    }

    @JsonIgnore
    @Override
    public boolean isEmpty() {
        return gene.isEmpty();
    }

    @Override
    public String toString() {
        return gene.toString();
    }

    @JsonIgnore
    @Override
    public double getFitness() throws JsonParseException, JsonMappingException, IOException {
        // TODO Auto-generated method stub
        return 0;
    }

}
