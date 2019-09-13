package roart.gene.ml.impl;

import roart.common.ml.GemEWCConfig;
import roart.common.util.RandomUtil;
import roart.gene.AbstractGene;

public class GemEWCConfigGene extends GemConfigGene {

    public GemEWCConfigGene(GemEWCConfig config) {
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
        GemEWCConfigGene offspring = new GemEWCConfigGene((GemEWCConfig) getConfig());
        GemEWCConfigGene other = (GemEWCConfigGene) otherNN;
        GemEWCConfig offspringconfig = (GemEWCConfig) offspring.getConfig();
        GemEWCConfig otherconfig = (GemEWCConfig) other.getConfig();

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
        GemEWCConfig myconfig = (GemEWCConfig) getConfig();
        myconfig.setMemories(getMemories());
    }

    private void generateMemorystrength() {
        GemEWCConfig myconfig = (GemEWCConfig) getConfig();
        myconfig.setMemorystrength(getMemorystrength());
    }

}
