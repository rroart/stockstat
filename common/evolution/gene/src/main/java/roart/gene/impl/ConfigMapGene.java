package roart.gene.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.fasterxml.jackson.annotation.JsonIgnore;

import roart.common.config.MyMyConfig;
import roart.common.util.MathUtil;
import roart.gene.AbstractGene;

public class ConfigMapGene extends AbstractGene {
    private Map<String, Object> map = new HashMap<>();

    protected List<String> confList;

    private MyMyConfig conf;
    
    public ConfigMapGene(List<String> confList, MyMyConfig conf) {
        this.confList = confList;
        this.conf = conf;
    }

    public ConfigMapGene() {
        // TODO Auto-generated constructor stub
    }

    public Map<String, Object> getMap() {
        return map;
    }

    public void setMap(Map<String, Object> map) {
        this.map = map;
    }

    public List<String> getConfList() {
        return confList;
    }

    public void setConfList(List<String> confList) {
        this.confList = confList;
    }

    @Override
    public void randomize() {
        for (int conf = 0; conf < confList.size(); conf++) {
            generateConfigNum(random, conf);
        }
        /*
        if (!validate()) {
            fixValidation();
        }
        */
    }

    @Override
    public void mutate() {
        int conf = random.nextInt(confList.size());
        generateConfigNum(random, conf);
        /*
        if (!validate()) {
            fixValidation();
        }
        */
    }

    private void generateConfigNum(Random rand, int confint) {
        String confName = confList.get(confint);
        Double[] range = conf.getRange().get(confName);
        Class type = conf.getType().get(confName);
        if (type == Boolean.class) {
            Boolean b = rand.nextBoolean();
            map.put(confName, b);
            return;
        }
        if (type == Integer.class) {
            Integer i = (range[0].intValue()) + rand.nextInt(range[1].intValue() - range[0].intValue());
            map.put(confName, i);
            return;
        }
        if (type == Double.class) {
            Double d = (range[0]) + rand.nextDouble() * (range[1] - range[0]);
            map.put(confName, d);
            return;
        }
        if (type == String.class) {
            Double d = (range[0]) + rand.nextDouble() * (range[1] - range[0]);
            d = MathUtil.round(d, range[2].intValue());
            map.put(confName, "[" + d + "]");
            return;
        }
        //log.error("Unknown type for {}", confName);
    }

    @Override
    public AbstractGene crossover(AbstractGene other) {
        ConfigMapGene newGene = new ConfigMapGene();
        newGene.conf = this.conf;
        newGene.confList = this.confList;
        for (int conf = 0; conf < confList.size(); conf++) {
            String confName = confList.get(conf);
            if (random.nextBoolean()) {
                newGene.map.put(confName, this.map.get(confName));
            } else {
                newGene.map.put(confName, ((ConfigMapGene) other).map.get(confName));
            }
        }
        /*
        if (!chromosome.validate()) {
            chromosome.fixValidation();
        }
        */
        return newGene;
    }
    
    public ConfigMapGene copy() {
        ConfigMapGene gene = new ConfigMapGene();
        gene.map = new HashMap<>(this.map);
        gene.conf = this.conf;
        gene.confList = this.confList;
        return gene;
    }
    
    @JsonIgnore
    public boolean isEmpty() {
        return confList == null || confList.isEmpty() || map == null || map.isEmpty();
    }
    
    @Override
    public String toString() {
        return map.toString();
    }

}
