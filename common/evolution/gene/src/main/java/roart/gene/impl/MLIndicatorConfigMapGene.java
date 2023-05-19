package roart.gene.impl;

import java.util.List;
import java.util.Map;

import roart.common.config.ConfigConstants;
import roart.iclij.config.IclijConfig;
import roart.gene.AbstractGene;

public class MLIndicatorConfigMapGene extends ConfigMapGene {
    
    public MLIndicatorConfigMapGene(List<String> confList, IclijConfig conf) {
        super(confList, conf);
    }

    public MLIndicatorConfigMapGene() {
        // TODO Auto-generated constructor stub
    }

    protected boolean changedSpecial(Map<String, Object> map, String confName, Class type) {
        if (confName.equals(ConfigConstants.AGGREGATORSINDICATOREXTRASLIST) || confName.equals(ConfigConstants.AGGREGATORSINDICATOREXTRASBITS)) {
            String bits = (String) map.get(ConfigConstants.AGGREGATORSINDICATOREXTRASBITS);
            int len = bits.length();
            int bit = random.nextInt(len);
            StringBuilder newBits = new StringBuilder(bits);
            newBits.setCharAt(bit, '1' == bits.charAt(bit) ? '0' : '1');
            map.put(ConfigConstants.AGGREGATORSINDICATOREXTRASBITS, newBits.toString());
            return true;
        }
        return false;
    }

    @Override
    public ConfigMapGene copy() {
        ConfigMapGene gene = new MLIndicatorConfigMapGene();
        copyInner(gene);
        return gene;
    }
    
    @Override
    public AbstractGene crossover(AbstractGene other) {
        ConfigMapGene newGene = new MLIndicatorConfigMapGene();
        crossoverInner(other, newGene);
        return newGene;
    }

}
