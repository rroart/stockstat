package roart.iclij.evolve;

public class FilterEvolveFactory {

    public static Evolve factory(int ga) {
        Evolve evolve = null;
        switch(ga) {
        case 0:
            return new EvolveFilterMy();
        case 1:
            return new MarketFilterEvolveJ();
        }
        return evolve;
    }
}
