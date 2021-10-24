package roart.iclij.evolve;

public class AboveBelowEvolveFactory {

    public static Evolve factory(int ga) {
        Evolve evolve = null;
        switch(ga) {
        case 0:
            return new EvolveAboveBelowMy();
        case 1:
            return new AboveBelowEvolveJ();
        }
        return evolve;
    }
}
