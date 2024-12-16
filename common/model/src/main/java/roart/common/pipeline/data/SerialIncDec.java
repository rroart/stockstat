package roart.common.pipeline.data;

import java.time.LocalDate;

public class SerialIncDec extends SerialObject {
    private String id;
    
    private String type;
    
    private Double probability;
    
    private String subComponent;
    
    private String localComponent;

    private LocalDate date;

    public SerialIncDec() {
        super();
    }

    public SerialIncDec(String id, String type, Double probability, String subComponent, String localComponent, LocalDate date) {
        super();
        this.id = id;
        this.type = type;
        this.probability = probability;
        this.subComponent = subComponent;
        this.localComponent = localComponent;
        this.date = date;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Double getProbability() {
        return probability;
    }

    public void setProbability(Double probability) {
        this.probability = probability;
    }

    public String getSubComponent() {
        return subComponent;
    }

    public void setSubComponent(String subComponent) {
        this.subComponent = subComponent;
    }

    public String getLocalComponent() {
        return localComponent;
    }

    public void setLocalComponent(String localComponent) {
        this.localComponent = localComponent;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }
}
