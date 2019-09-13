package roart.gene.ml.impl;

import roart.common.ml.GemGEMConfig;
import roart.common.util.RandomUtil;
import roart.gene.AbstractGene;

public class GemGEMConfigGene extends GemConfigGene {

    public GemGEMConfigGene(GemGEMConfig config) {
        super(config);
    }

    @Override
    public void randomize() {
        super.randomize();
        generateMemories();
        generateMemorystrength();
    }

    @Override
    public void mutate() {
        int task = random.nextInt(RANDOMS + 2);
        if (task < RANDOMS) {
            super.mutate(task);
            return;
        }
        task = task - RANDOMS;
        switch (task) {
            case 0:
                generateMemories();
                break;
            case 1:
                generateMemorystrength();
                break;
        }
    }

    @Override
    public AbstractGene crossover(AbstractGene otherNN) {
        GemGEMConfigGene offspring = new GemGEMConfigGene((GemGEMConfig) getConfig());
        GemGEMConfigGene other = (GemGEMConfigGene) otherNN;
        GemGEMConfig offspringconfig = (GemGEMConfig) offspring.getConfig();
        GemGEMConfig otherconfig = (GemGEMConfig) other.getConfig();

        ((GemConfigGene) offspring).crossover(otherNN);
        if (random.nextBoolean()) {
            offspringconfig.setMemories(otherconfig.getMemories());
        }
        if (random.nextBoolean()) {
            offspringconfig.setMemorystrength(otherconfig.getMemorystrength());
        }
        return offspring;
    }

    private void generateMemories() {
        GemGEMConfig myconfig = (GemGEMConfig) getConfig();
        myconfig.setMemories(getMemories());
    }

    private void generateMemorystrength() {
        GemGEMConfig myconfig = (GemGEMConfig) getConfig();
        myconfig.setMemorystrength(getMemorystrength());
    }

}