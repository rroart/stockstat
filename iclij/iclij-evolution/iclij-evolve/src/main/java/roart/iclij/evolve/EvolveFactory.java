package roart.iclij.evolve;

public class EvolveFactory {
    public static EvolveFactory factory(int ga) {
        EvolveFactory factory = null;
        /*
        switch(ga) {
        case 0:
            return new MyEvolve();
            break;
        case 1:
            return new JEvolve();
            break;
        }
        */
        return factory;
    }
}
