package roart.iclij.config;

public class MarketConfig {
    private String market;

    private String mlmarket;
    
    private Short findtime;
    
    private Short improvetime;
    
    private Short persisttime;

    private Short mldays;
    
    private String mldate;
    
    private Short continuoustime;

    private Short evolvetime;

    private Short datasettime;

    private Short crosstime;

    private Short filtertime;
    
    private Short abovebelowtime;
    
    private String id;

    private Short startoffset;
    
    private Boolean dataset;
    
    private String[] mlmarkets;
    
    private Short populate;
    
    private String interpolate;
    
    private Boolean enable;
    
    private Boolean binary;
    
    public MarketConfig() {
        super();
    }
    
    public String getMarket() {
        return market;
    }

    public void setMarket(String market) {
        this.market = market;
    }

    public String getMlmarket() {
        return mlmarket;
    }

    public void setMlmarket(String mlmarket) {
        this.mlmarket = mlmarket;
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

    public Short getMldays() {
        return mldays;
    }

    public void setMldays(Short mldays) {
        this.mldays = mldays;
    }

    public String getMldate() {
        String adate = mldate;
        if (adate != null) {
            adate = adate.replace('-', '.');
        }
        return adate;
    }

    public void setMldate(String mldate) {
        this.mldate = mldate;
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

    public Short getFiltertime() {
        return filtertime;
    }

    public void setFiltertime(Short filtertime) {
        this.filtertime = filtertime;
    }

    public Short getAbovebelowtime() {
        return abovebelowtime;
    }

    public void setAbovebelowtime(Short abovebelowtime) {
        this.abovebelowtime = abovebelowtime;
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

    public String[] getMlmarkets() {
        return mlmarkets;
    }

    public void setMlmarkets(String[] mlmarkets) {
        this.mlmarkets = mlmarkets;
    }

    public Short getPopulate() {
        return populate;
    }

    public void setPopulate(Short populate) {
        this.populate = populate;
    }

    public String getInterpolate() {
        return interpolate;
    }

    public void setInterpolate(String interpolate) {
        this.interpolate = interpolate;
    }

    public Boolean getEnable() {
        return enable;
    }

    public void setEnable(Boolean enable) {
        this.enable = enable;
    }

    public Boolean getBinary() {
        return binary;
    }

    public void setBinary(Boolean binary) {
        this.binary = binary;
    }

}
