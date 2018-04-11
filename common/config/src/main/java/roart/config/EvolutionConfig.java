package roart.config;

public class EvolutionConfig {
    private Integer generations;
    
    private Integer crossover;
    
    private Integer elite;
    
    private Integer elitecloneandmutate;
    
    private Integer select;
    
    private Integer mutate;
    
    private Integer generationcreate;

    public EvolutionConfig() {
        super();
    }
    
    public EvolutionConfig(Integer generations, Integer crossover, Integer elite,
            Integer elitecloneandmutate, Integer select, Integer mutate, Integer generationcreate) {
        super();
        this.generations = generations;
        this.crossover = crossover;
        this.elite = elite;
        this.elitecloneandmutate = elitecloneandmutate;
        this.select = select;
        this.mutate = mutate;
        this.generationcreate = generationcreate;
    }

    public Integer getGenerations() {
        return generations;
    }

    public void setGenerations(Integer generations) {
        this.generations = generations;
    }

    public Integer getCrossover() {
        return crossover;
    }

    public void setCrossover(Integer crossover) {
        this.crossover = crossover;
    }

    public Integer getElite() {
        return elite;
    }

    public void setElite(Integer elite) {
        this.elite = elite;
    }

    public Integer getElitecloneandmutate() {
        return elitecloneandmutate;
    }

    public void setElitecloneandmutate(Integer elitecloneandmutate) {
        this.elitecloneandmutate = elitecloneandmutate;
    }

    public Integer getSelect() {
        return select;
    }

    public void setSelect(Integer select) {
        this.select = select;
    }

    public Integer getMutate() {
        return mutate;
    }

    public void setMutate(Integer mutate) {
        this.mutate = mutate;
    }

    public Integer getGenerationcreate() {
        return generationcreate;
    }

    public void setGenerationcreate(Integer generationcreate) {
        this.generationcreate = generationcreate;
    }

}
