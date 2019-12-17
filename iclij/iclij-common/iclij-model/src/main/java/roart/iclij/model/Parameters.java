package roart.iclij.model;

public class Parameters {
    private Integer futuredays;
    
    private Double threshold;
    
    public Parameters() {
        super();
    }

    public Integer getFuturedays() {
        return futuredays;
    }

    public void setFuturedays(Integer futuredays) {
        this.futuredays = futuredays;
    }

    public Double getThreshold() {
        return threshold;
    }

    public void setThreshold(Double threshold) {
        this.threshold = threshold;
    }
}
