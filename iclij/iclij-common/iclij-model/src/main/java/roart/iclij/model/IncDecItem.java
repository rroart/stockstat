package roart.iclij.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import roart.common.util.TimeUtil;
import roart.db.model.IncDec;

public class IncDecItem {
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
    
    public IncDecItem() {
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
    
    public void save() throws Exception {
        IncDec incdec = new IncDec();
        incdec.setComponent(getComponent());
        incdec.setDate(TimeUtil.convertDate(getDate()));
        incdec.setDescription(getDescription());
        incdec.setId(getId());
        incdec.setIncrease(isIncrease());
        incdec.setLocalcomponent(getLocalcomponent());
        incdec.setMarket(getMarket());
        incdec.setName(getName());
        incdec.setParameters(getParameters());
        incdec.setRecord(TimeUtil.convertDate(getRecord()));
        incdec.setScore(getScore());
        incdec.setSubcomponent(getSubcomponent());
        incdec.save();
    }
    
    public static List<IncDecItem> getAll() throws Exception {
        List<IncDec> incdecs = IncDec.getAll();
        List<IncDecItem> incdecItems = new ArrayList<>();
        for (IncDec incdec : incdecs) {
            IncDecItem memoryItem = getIncdecItem(incdec);
            incdecItems.add(memoryItem);
        }
        return incdecItems;
    }

   public static List<IncDecItem> getAll(String market) throws Exception {
        List<IncDec> incdecs = IncDec.getAll(market);
        List<IncDecItem> incdecItems = new ArrayList<>();
        for (IncDec incdec : incdecs) {
            IncDecItem memoryItem = getIncdecItem(incdec);
            incdecItems.add(memoryItem);
        }
        return incdecItems;
    }

    private static IncDecItem getIncdecItem(IncDec incdec) {
        IncDecItem incdecItem = new IncDecItem();
        incdecItem.setComponent(incdec.getComponent());
        incdecItem.setDate(TimeUtil.convertDate(incdec.getDate()));
        incdecItem.setDescription(incdec.getDescription());
        incdecItem.setId(incdec.getId());
        incdecItem.setIncrease(incdec.isIncrease());
        incdecItem.setLocalcomponent(incdec.getLocalcomponent());
        incdecItem.setMarket(incdec.getMarket());
        incdecItem.setName(incdec.getName());
        incdecItem.setParameters(incdec.getParameters());
        incdecItem.setRecord(TimeUtil.convertDate(incdec.getRecord()));
        incdecItem.setScore(incdec.getScore());
        incdecItem.setSubcomponent(incdec.getSubcomponent());;
        return incdecItem;
    }

    public static List<IncDecItem> getAll(String market, Date startDate, Date endDate, String parameters) throws Exception {
        List<IncDec> configs = IncDec.getAll(market, startDate, endDate, parameters);
        List<IncDecItem> configItems = new ArrayList<>();
        for (IncDec config : configs) {
            IncDecItem memoryItem = getIncdecItem(config);
            configItems.add(memoryItem);
        }
        return configItems;
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
    
    public void delete(String market, String component, String subcomponent, Date startDate, Date endDate) throws Exception {
        IncDec.delete(market, component, subcomponent, startDate, endDate);
    }

}
