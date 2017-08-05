package roart.calculate;

import java.util.Random;

public class CalcDoubleNode extends CalcNode {

    private int weight;

    @Override
    public double calc(double value, double minmax) {
        return weight;
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

}
