package roart.gene.ml.impl;

import roart.common.ml.GemIcarlConfig;
import roart.common.ml.GemIcarlConfig;
import roart.common.ml.NeuralNetConfig;
import roart.common.util.RandomUtil;
import roart.gene.AbstractGene;
import roart.gene.NeuralNetConfigGene;

public class GemIcarlConfigGene extends GemConfigGene {

    public GemIcarlConfigGene(GemIcarlConfig config) {
        super(config);
    }

    @Override
    public NeuralNetConfigGene copy() {
        return new GemIcarlConfigGene(new GemIcarlConfig((GemIcarlConfig) getConfig()));
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

        offspring.crossover((GemConfigGene) otherNN);
        if (random.nextBoolean()) {
            offspringconfig.setN_memories(otherconfig.getN_memories());
        }
        if (random.nextBoolean()) {
            offspringconfig.setMemory_strength(otherconfig.getMemory_strength());
        }
        if (random.nextBoolean()) {
            offspringconfig.setSamples_per_task(otherconfig.getSamples_per_task());
        }
        return offspring;
    }

    private void generateMemories() {
        GemIcarlConfig myconfig = (GemIcarlConfig) getConfig();
        myconfig.setN_memories(getMemories());
    }

    private void generateMemorystrength() {
        GemIcarlConfig myconfig = (GemIcarlConfig) getConfig();
        myconfig.setMemory_strength(getMemorystrength());
    }

    private void generateSamplespertask() {
        GemIcarlConfig myconfig = (GemIcarlConfig) getConfig();
        myconfig.setSamples_per_task(RandomUtil.random(random, 5, 5, 10));
    }

}
