package roart.iclij.config;

public class SimulateFilter {

    private int shortrun;
    
    private double lucky;
    
    private double stable;
    
    private boolean allabove;
    
    private int populationabove;
    
    private boolean useclusters;
    
    public SimulateFilter(int shortrun, double lucky, double stable, boolean allabove, int populationabove, boolean useclusters) {
        super();
        this.shortrun = shortrun;
        this.lucky = lucky;
        this.stable = stable;
        this.allabove = allabove;
        this.populationabove = populationabove;
        this.useclusters = useclusters;
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

    public double getStable() {
        return stable;
    }

    public void setStable(double stable) {
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

    public boolean isUseclusters() {
        return useclusters;
    }

    public void setUseclusters(boolean useclusters) {
        this.useclusters = useclusters;
    }
    

}
