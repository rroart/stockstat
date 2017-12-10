package roart.config;

public class Market {
    private String market;

    private Short time;
    
    private Short improvetime;
    
    private String id;

    public Market() {
        
    }
    
    public String getMarket() {
        return market;
    }

    public void setMarket(String market) {
        this.market = market;
    }

    public Short getTime() {
        return time;
    }

    public void setTime(Short time) {
        this.time = time;
    }

    public Short getImprovetime() {
        return improvetime;
    }

    public void setImprovetime(Short improvetime) {
        this.improvetime = improvetime;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

}
