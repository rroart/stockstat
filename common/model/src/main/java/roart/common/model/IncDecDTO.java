package roart.common.model;

import java.time.LocalDate;

public class IncDecDTO {
    private LocalDate record;
    
    private LocalDate date;
    
    private String market;

    private boolean increase;
    
    private String id;
    
    private String name;
    
    private String description;

    private Double score;

    private String parameters;
    
    private String component;
    
    private String subcomponent;
    
    private String localcomponent;
    
    // not saved
    private Boolean verified;
    
    // not saved
    private String verificationComment;
    
    public IncDecDTO() {
        super();
    }
    
    public LocalDate getRecord() {
        return record;
    }

    public void setRecord(LocalDate record) {
        this.record = record;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getMarket() {
        return market;
    }

    public void setMarket(String market) {
        this.market = market;
    }

    public boolean isIncrease() {
        return increase;
    }

    public void setIncrease(boolean increase) {
        this.increase = increase;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
    }

    public String getParameters() {
        return parameters;
    }

    public void setParameters(String parameters) {
        this.parameters = parameters;
    }

    public String getComponent() {
        return component;
    }

    public void setComponent(String component) {
        this.component = component;
    }

    public String getSubcomponent() {
        return subcomponent;
    }

    public void setSubcomponent(String subcomponent) {
        this.subcomponent = subcomponent;
    }

    public String getLocalcomponent() {
        return localcomponent;
    }

    public void setLocalcomponent(String localcomponent) {
        this.localcomponent = localcomponent;
    }

    public Boolean getVerified() {
        return verified;
    }

    public void setVerified(Boolean verified) {
        this.verified = verified;
    }

    public String getVerificationComment() {
        return verificationComment;
    }

    public void setVerificationComment(String verificationComment) {
        this.verificationComment = verificationComment;
    }

    @Override
    public String toString() {
        return market + " " + record + " " + date + " " + increase + " " + id + " " + name + " " + score + " " + component + " " + subcomponent + " " + localcomponent + " " + description + " " + verified + " " + verificationComment + " " + parameters + "\n"; 
    }
    
    @Override
    public boolean equals(Object other) {
        if (other == null) {
            return false;
        }
        return this.toString().equals(other.toString());
    }
    
    @Override
    public int hashCode() {
        return this.toString().hashCode();
    }
    

}
