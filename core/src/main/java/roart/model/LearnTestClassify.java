package roart.model;

public class LearnTestClassify {
    private Object[][] trainingarray;

    private Object[] trainingcatarray;
    
    private Object[][] classifyarray;

    private Object[] classifycatarray;

    private int modelInt;
    
    private int size;
    
    private String period;
    
    private String mapname;
    
    private int outcomes;
    
    private Double accuracy;

    public LearnTestClassify() {
        super();
    }

    public Object[][] getTrainingarray() {
        return trainingarray;
    }

    public void setTrainingarray(Object[][] trainingarray) {
        this.trainingarray = trainingarray;
    }

    public Object[] getTrainingcatarray() {
        return trainingcatarray;
    }

    public void setTrainingcatarray(Object[] catarray) {
        this.trainingcatarray = catarray;
    }

    public Object[][] getClassifyarray() {
        return classifyarray;
    }

    public void setClassifyarray(Object[][] classifyarray) {
        this.classifyarray = classifyarray;
    }

    public Object[] getClassifycatarray() {
        return classifycatarray;
    }

    public void setClassifycatarray(Object[] classifycatarray) {
        this.classifycatarray = classifycatarray;
    }

    public int getModelInt() {
        return modelInt;
    }

    public void setModelInt(int modelInt) {
        this.modelInt = modelInt;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public String getMapname() {
        return mapname;
    }

    public void setMapname(String mapname) {
        this.mapname = mapname;
    }

    public int getOutcomes() {
        return outcomes;
    }

    public void setOutcomes(int outcomes) {
        this.outcomes = outcomes;
    }

    public Double getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(Double accuracy) {
        this.accuracy = accuracy;
    }

}
