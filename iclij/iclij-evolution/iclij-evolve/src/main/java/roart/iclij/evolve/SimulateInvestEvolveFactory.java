package roart.iclij.evolve;

public class SimulateInvestEvolveFactory {

    public static Evolve factory(int ga) {
        Evolve evolve = null;
        switch(ga) {
        case 0:
            return new EvolveIclijConfigMapMy();
        case 1:
            return new IclijConfigMapEvolveJ();
        case 2:
            return new IclijConfigMapEvolveA();
        }
        return evolve;
    }

}
