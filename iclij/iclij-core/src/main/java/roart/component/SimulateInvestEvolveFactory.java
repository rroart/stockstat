package roart.component;

public class SimulateInvestEvolveFactory {

    public static Evolve factory(int ga) {
        Evolve evolve = null;
        switch(ga) {
        case 0:
            return new EvolveIclijConfigMapMy();
        case 1:
            return new IclijConfigMapEvolveJ();
        }
        return evolve;
    }

}
