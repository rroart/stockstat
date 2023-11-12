package roart.gene.impl;

import java.util.Random;

import roart.gene.AbstractGene;
import roart.gene.CalcGene;

public class CalcDoubleGene extends CalcGene {

    private int weight;

    @Override
    public double calc(double value, double minmax) {
        return weight * value / 100;
    }

    @Override
    public void randomize() {
        getWeight(random);
    }

    private void getWeight(Random rand) {
        weight = 1 + rand.nextInt(100);
    }

    @Override
    public void mutate() {
        int task = random.nextInt(1);
        switch (task) {
        case 0:
            getWeight(random);
            break;
        default:
            log.error("Too many");
            break;
        }

    }
    
    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    @Override
    public AbstractGene crossover(AbstractGene other) {
        CalcDoubleGene node = new CalcDoubleGene();
        if (random.nextBoolean()) {
            node.setWeight(weight);
        } else {
            node.setWeight(((CalcDoubleGene) other).getWeight());
        }
        return node;
    }

}
