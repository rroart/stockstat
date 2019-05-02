package roart.evolution.chromosome.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import roart.common.config.ConfigConstants;
import roart.common.config.MyMyConfig;
import roart.common.util.JsonUtil;
import roart.component.ComponentRecommender;
import roart.component.model.ComponentData;
import roart.config.Market;
import roart.evolution.chromosome.AbstractChromosome;
import roart.evolution.species.Individual;
import roart.service.model.ProfitData;

public class RecommenderChromosome extends ConfigMapChromosome {
    private List<List<String>> listPerm = ComponentRecommender.getAllPerms(getBuy());
    
    private int listIdx;
    
    public RecommenderChromosome( List<String> confList, ComponentData param, ProfitData profitdata, Market market, List<Integer> positions, String component) {
        super(confList, param, profitdata, market, positions, component);
    }

    public List<String> getBuy() {
        List<String> buyList = new ArrayList<>();
        buyList.add(ConfigConstants.AGGREGATORSINDICATORRECOMMENDERCOMPLEXMACDBUYWEIGHTHISTOGRAMNODE);
        buyList.add(ConfigConstants.AGGREGATORSINDICATORRECOMMENDERCOMPLEXMACDBUYWEIGHTHISTOGRAMDELTANODE);
        buyList.add(ConfigConstants.AGGREGATORSINDICATORRECOMMENDERCOMPLEXMACDBUYWEIGHTMOMENTUMNODE);
        buyList.add(ConfigConstants.AGGREGATORSINDICATORRECOMMENDERCOMPLEXMACDBUYWEIGHTMOMENTUMDELTANODE);
        buyList.add(ConfigConstants.AGGREGATORSINDICATORRECOMMENDERCOMPLEXRSIBUYWEIGHTRSINODE);
        buyList.add(ConfigConstants.AGGREGATORSINDICATORRECOMMENDERCOMPLEXRSIBUYWEIGHTRSIDELTANODE);
        return buyList;
    }

    public List<String> getSell() {
        List<String> sellList = new ArrayList<>();
        sellList.add(ConfigConstants.AGGREGATORSINDICATORRECOMMENDERCOMPLEXMACDSELLWEIGHTHISTOGRAMNODE);
        sellList.add(ConfigConstants.AGGREGATORSINDICATORRECOMMENDERCOMPLEXMACDSELLWEIGHTHISTOGRAMDELTANODE);
        sellList.add(ConfigConstants.AGGREGATORSINDICATORRECOMMENDERCOMPLEXMACDSELLWEIGHTMOMENTUMNODE);
        sellList.add(ConfigConstants.AGGREGATORSINDICATORRECOMMENDERCOMPLEXMACDSELLWEIGHTMOMENTUMDELTANODE);
        sellList.add(ConfigConstants.AGGREGATORSINDICATORRECOMMENDERCOMPLEXRSISELLWEIGHTRSINODE);
        sellList.add(ConfigConstants.AGGREGATORSINDICATORRECOMMENDERCOMPLEXRSISELLWEIGHTRSIDELTANODE);
        return sellList;
    }
    
    @Override
    public double getFitness()
            throws JsonParseException, JsonMappingException, IOException {
        Map<String, Object> map = new HashMap<>();
        //String string = JsonUtil.convert(config);
        //map.put(ConfigConstants.MACHINELEARNINGTENSORFLOWLSTMCONFIG, string);
        //setMap(map);
        param.setDisableList(listPerm.get(listIdx));
        if (listIdx == 62) {
            int jj = 0;
        }
        return super.getFitness();
    }

    @Override
    public void getRandom() throws JsonParseException, JsonMappingException, IOException {
        Random random = new Random();
        listIdx = random.nextInt(listPerm.size());
    }
    
    @Override
    public void mutate() {
        Random random = new Random();
        int bit = random.nextInt(getBuy().size());
        //int bit = random.nextInt(bits);
        listIdx = listIdx ^ (1 << bit);
        if (listIdx >= listPerm.size()) {
            listIdx = 0;
        }
    }
    
    @Override
    public Individual crossover(AbstractChromosome other) {
        RecommenderChromosome chromosome = new RecommenderChromosome(confList, param, profitdata, market, positions, componentName);
        int idx = chromosome.listIdx ^ ((RecommenderChromosome) other).listIdx;
        chromosome.listIdx = idx;
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
    public AbstractChromosome copy() {
        ComponentData newparam = new ComponentData(param);
        RecommenderChromosome chromosome = new RecommenderChromosome(confList, newparam, profitdata, market, positions, componentName);
        //chromosome.config = new TensorflowLSTMConfig(config.getEpochs(), config.getWindowsize(), config.getHorizon());
        chromosome.listIdx = listIdx;
        return chromosome;
    }
    
}