package roart.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import roart.util.TimeUtil;

public class IncDecItem {
    private LocalDate record;
    
    private LocalDate date;
    
    private String market;

    private boolean increase;
    
    private String id;
    
    private String name;
    
    private String description;

    private Double score;

    // not saved
    private Boolean verified;
    
    // not saved
    private String verificationComment;
    
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
        return market + " " + record + " " + date + " " + increase + " " + id + " " + name + " " + score + " " + description + " " + verified + " " + verificationComment; 
    }
    
    public void save() throws Exception {
        IncDec incdec = new IncDec();
        incdec.setDate(TimeUtil.convertDate(getDate()));
        incdec.setDescription(getDescription());
        incdec.setId(getId());
        incdec.setIncrease(isIncrease());
        incdec.setMarket(getMarket());
        incdec.setName(getName());
        incdec.setRecord(TimeUtil.convertDate(getRecord()));
        incdec.setScore(getScore());
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
        incdecItem.setDate(TimeUtil.convertDate(incdec.getDate()));
        incdecItem.setDescription(incdec.getDescription());
        incdecItem.setId(incdec.getId());
        incdecItem.setIncrease(incdec.isIncrease());
        incdecItem.setMarket(incdec.getMarket());
        incdecItem.setName(incdec.getName());
        incdecItem.setRecord(TimeUtil.convertDate(incdec.getRecord()));
        incdecItem.setScore(incdec.getScore());
        return incdecItem;
    }

}
