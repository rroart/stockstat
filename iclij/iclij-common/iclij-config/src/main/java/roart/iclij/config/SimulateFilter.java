package roart.iclij.config;

public class SimulateFilter {

    private int shortrun;
    
    private double lucky;
    
    private boolean stable;
    
    private boolean allabove;
    
    private int populationabove;
    
    public SimulateFilter(int shortrun, double lucky, boolean stable, boolean allabove, int populationabove) {
        super();
        this.shortrun = shortrun;
        this.lucky = lucky;
        this.stable = stable;
        this.allabove = allabove;
        this.populationabove = populationabove;
    }

    public SimulateFilter() {
        super();
    }

    public int getShortrun() {
        return shortrun;
    }

    public void setShortrun(int shortrun) {
        this.shortrun = shortrun;
    }

    public double getLucky() {
        return lucky;
    }

    public void setLucky(double lucky) {
        this.lucky = lucky;
    }

    public boolean isStable() {
        return stable;
    }

    public void setStable(boolean stable) {
        this.stable = stable;
    }

    public boolean isAllabove() {
        return allabove;
    }

    public void setAllabove(boolean allabove) {
        this.allabove = allabove;
    }

    public int getPopulationabove() {
        return populationabove;
    }

    public void setPopulationabove(int populationabove) {
        this.populationabove = populationabove;
    }
    

}
