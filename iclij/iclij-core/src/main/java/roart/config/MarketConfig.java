package roart.config;

public class MarketConfig {
    private String market;

    private Short findtime;
    
    private Short improvetime;
    
    private Short persisttime;

    private Short continuoustime;

    private Short evolvetime;

    private Short datasettime;

    private Short crosstime;

    private String id;

    private Short startoffset;
    
    private Boolean dataset;
    
    public MarketConfig() {
        super();
    }
    
    public String getMarket() {
        return market;
    }

    public void setMarket(String market) {
        this.market = market;
    }

    public Short getFindtime() {
        return findtime;
    }

    public void setFindtime(Short time) {
        this.findtime = time;
    }

    public Short getImprovetime() {
        return improvetime;
    }

    public void setImprovetime(Short improvetime) {
        this.improvetime = improvetime;
    }

    public Short getPersisttime() {
        return persisttime;
    }

    public void setPersisttime(Short persisttime) {
        this.persisttime = persisttime;
    }

    public Short getContinuoustime() {
        return continuoustime;
    }

    public void setContinuoustime(Short continuoustime) {
        this.continuoustime = continuoustime;
    }

    public Short getEvolvetime() {
        return evolvetime;
    }

    public void setEvolvetime(Short evolvetime) {
        this.evolvetime = evolvetime;
    }

    public Short getDatasettime() {
        return datasettime;
    }

    public void setDatasettime(Short datasettime) {
        this.datasettime = datasettime;
    }

    public Short getCrosstime() {
        return crosstime;
    }

    public void setCrosstime(Short crosstime) {
        this.crosstime = crosstime;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Short getStartoffset() {
        return startoffset;
    }

    public void setStartoffset(Short startoffset) {
        this.startoffset = startoffset;
    }

    public Boolean getDataset() {
        return dataset;
    }

    public void setDataset(Boolean dataset) {
        this.dataset = dataset;
    }

}
