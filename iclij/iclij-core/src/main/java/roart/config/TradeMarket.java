package roart.config;

public class TradeMarket {
    private String market;

    private String inccategory;
    
    private Double incthreshold;

    private String deccategory;
    
    private Double decthreshold;

    private Double confidence;

    private Integer recordage;
    
    public TradeMarket() {
        
    }

    public String getMarket() {
        return market;
    }

    public void setMarket(String market) {
        this.market = market;
    }

    public String getInccategory() {
        return inccategory;
    }

    public void setInccategory(String inccategory) {
        this.inccategory = inccategory;
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