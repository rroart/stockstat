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
        Random rand = new Random();
        getWeight(rand);
    }

    private void getWeight(Random rand) {
        weight = 1 + rand.nextInt(100);
    }

    @Override
    public void mutate() {
        Random rand = new Random();
        int task = rand.nextInt(1);
        switch (task) {
        case 0:
            getWeight(rand);
            break;
        default:
            System.out.println("Too many");
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
        Random rand = new Random();
        if (rand.nextBoolean()) {
            node.setWeight(weight);
        } else {
            node.setWeight(((CalcDoubleGene) other).getWeight());
        }
        return node;
    }

}
