package roart.common.model;

public class OHLC {
    private Double open;
    
    private Double high;

    private Double low;
    
    private Double close;

    public OHLC(Double open, Double high, Double low, Double close) {
        super();
        this.open = open;
        this.high = high;
        this.low = low;
        this.close = close;
    }

    public Double getOpen() {
        return open;
    }

    public void setOpen(Double open) {
        this.open = open;
    }

    public Double getHigh() {
        return high;
    }

    public void setHigh(Double high) {
        this.high = high;
    }

    public Double getLow() {
        return low;
    }

    public void setLow(Double low) {
        this.low = low;
    }

    public Double getClose() {
        return close;
    }

    public void setClose(Double close) {
        this.close = close;
    }
    
}
