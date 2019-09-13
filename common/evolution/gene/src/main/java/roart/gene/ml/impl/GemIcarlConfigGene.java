package roart.gene.ml.impl;

import roart.common.ml.GemIcarlConfig;
import roart.common.ml.NeuralNetConfig;
import roart.common.util.RandomUtil;
import roart.gene.AbstractGene;

public class GemIcarlConfigGene extends GemConfigGene {

    public GemIcarlConfigGene(GemIcarlConfig config) {
        super(config);
    }

    @Override
    public void randomize() {
        super.randomize();
        generateMemories();
        generateMemorystrength();
        generateSamplespertask();
    }

    @Override
    public void mutate() {
        int task = random.nextInt(RANDOMS + 3);
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
            case 2:
                generateSamplespertask();
                break;
        }
    }

    @Override
    public AbstractGene crossover(AbstractGene otherNN) {
        GemIcarlConfigGene offspring = new GemIcarlConfigGene((GemIcarlConfig) getConfig());
        GemIcarlConfigGene other = (GemIcarlConfigGene) otherNN;
        GemIcarlConfig offspringconfig = (GemIcarlConfig) offspring.getConfig();
        GemIcarlConfig otherconfig = (GemIcarlConfig) other.getConfig();

        ((GemConfigGene) offspring).crossover(otherNN);
        if (random.nextBoolean()) {
            offspringconfig.setMemories(otherconfig.getMemories());
        }
        if (random.nextBoolean()) {
            offspringconfig.setMemorystrength(otherconfig.getMemorystrength());
        }
        if (random.nextBoolean()) {
            offspringconfig.setSamplespertask(otherconfig.getSamplespertask());
        }
        return offspring;
    }

    private void generateMemories() {
        GemIcarlConfig myconfig = (GemIcarlConfig) getConfig();
        myconfig.setMemories(getMemories());
    }

    private void generateMemorystrength() {
        GemIcarlConfig myconfig = (GemIcarlConfig) getConfig();
        myconfig.setMemorystrength(getMemorystrength());
    }

    private void generateSamplespertask() {
        GemIcarlConfig myconfig = (GemIcarlConfig) getConfig();
        myconfig.setSamplespertask(RandomUtil.random(random, 5, 5, 10));
    }

}
