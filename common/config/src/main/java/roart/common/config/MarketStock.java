package roart.common.config;

public class MarketStock {
    private String market;
    
    private String id;
    
    private String category;

    public MarketStock() {
        super();
    }

    public MarketStock(String market, String id, String category) {
        super();
        this.market = market;
        this.id = id;
        this.category = category;
    }

    public String getMarket() {
        return market;
    }

    public void setMarket(String market) {
        this.market = market;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}
