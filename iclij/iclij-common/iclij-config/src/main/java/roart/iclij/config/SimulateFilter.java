package roart.iclij.config;

import java.util.Set;

public class SimulateFilter {

    private Integer shortrun;
    
    private Double lucky;
    
    private Double stable;
    
    private Boolean allabove;
    
    private Integer populationabove;
    
    private Boolean useclusters;
    
    private Set<String> printconfig;
    
    public SimulateFilter(Integer shortrun, Double lucky, Double stable, Boolean allabove, Integer populationabove, Boolean useclusters, Set<String> printconfig) {
        super();
        this.shortrun = shortrun;
        this.lucky = lucky;
        this.stable = stable;
        this.allabove = allabove;
        this.populationabove = populationabove;
        this.useclusters = useclusters;
        this.printconfig = printconfig;
    }

    public SimulateFilter() {
        super();
    }

    public Integer getShortrun() {
        return shortrun;
    }

    public void setShortrun(Integer shortrun) {
        this.shortrun = shortrun;
    }

    public Double getLucky() {
        return lucky;
    }

    public void setLucky(Double lucky) {
        this.lucky = lucky;
    }

    public Double getStable() {
        return stable;
    }

    public void setStable(Double stable) {
        this.stable = stable;
    }

    public Boolean isAllabove() {
        return allabove;
    }

    public void setAllabove(Boolean allabove) {
        this.allabove = allabove;
    }

    public Integer getPopulationabove() {
        return populationabove;
    }

    public void setPopulationabove(Integer populationabove) {
        this.populationabove = populationabove;
    }

    public Boolean isUseclusters() {
        return useclusters;
    }

    public void setUseclusters(Boolean useclusters) {
        this.useclusters = useclusters;
    }

    public Set<String> getPrintconfig() {
        return printconfig;
    }

    public void setPrintconfig(Set<String> printconfig) {
        this.printconfig = printconfig;
    }
    
    public void merge(SimulateFilter other) {
        if (other == null) {
            return;
        }
        if (other.shortrun != null) {
            this.shortrun = other.shortrun;
        }
        if (other.lucky != null) {
            this.lucky = other.lucky;
        }
        if (other.stable != null) {
            this.stable = other.stable;
        }
        if (other.allabove != null) {
            this.allabove = other.allabove;
        }
        if (other.populationabove != null) {
            this.populationabove = other.populationabove;
        }
        if (other.useclusters != null) {
            this.useclusters = other.useclusters;
        }
        if (other.printconfig != null) {
            this.printconfig = other.printconfig;
        }
    }
}
