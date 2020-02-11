package roart.iclij.config;

public class MarketFilter {
    private String inccategory;
    
    private Integer incdays;
    
    private Double incthreshold;

    private String deccategory;
    
    private Integer decdays;

    private Double decthreshold;

    private Double confidence;

    private Integer recordage;
    
    public MarketFilter() {
        
    }

    public String getInccategory() {
        return inccategory;
    }

    public void setInccategory(String inccategory) {
        this.inccategory = inccategory;
    }

    public Integer getIncdays() {
        return incdays;
    }

    public void setIncdays(Integer incdays) {
        this.incdays = incdays;
    }

    public Double getIncthreshold() {
        return incthreshold;
    }

    public void setIncthreshold(Double incthreshold) {
        this.incthreshold = incthreshold;
    }

    public String getDeccategory() {
        return deccategory;
    }

    public void setDeccategory(String deccategory) {
        this.deccategory = deccategory;
    }

    public Integer getDecdays() {
        return decdays;
    }

    public void setDecdays(Integer decdays) {
        this.decdays = decdays;
    }

    public void setDecthreshold(Double decthreshold) {
        this.decthreshold = decthreshold;
    }

    public Double getDecthreshold() {
        return decthreshold;
    }

    public Double getConfidence() {
        return confidence;
    }

    public void setConfidence(Double confidence) {
        this.confidence = confidence;
    }

    public Integer getRecordage() {
        return recordage;
    }

    public void setRecordage(Integer recordage) {
        this.recordage = recordage;
    }
    
}