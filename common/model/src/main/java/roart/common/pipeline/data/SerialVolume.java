package roart.common.pipeline.data;

import com.fasterxml.jackson.annotation.JsonProperty;

@Deprecated
public class SerialVolume extends SerialObject {
    @JsonProperty("V")
    private Long volume;
    
    @JsonProperty("C")
    private String currency;

    public SerialVolume() {
        super();
    }

    public SerialVolume(Long volume, String currency) {
        super();
        this.volume = volume;
        this.currency = currency;
    }

    public Long getVolume() {
        return volume;
    }

    public void setVolume(Long volume) {
        this.volume = volume;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }
    
    
}
