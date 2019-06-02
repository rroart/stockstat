package roart.iclij.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import roart.common.util.JsonUtil;
import roart.common.util.TimeUtil;
import roart.db.model.Relation;

public class RelationItem {

    private LocalDate record;

    private String market;

    private String id;

    private String altId;

    private String type;

    private String otherMarket;

    private String otherId;

    private String otherAltId;

    private Double value;

    public LocalDate getRecord() {
        return record;
    }

    public void setRecord(LocalDate record) {
        this.record = record;
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

    public String getAltId() {
        return altId;
    }

    public void setAltId(String altId) {
        this.altId = altId;
    }

    public String getOtherMarket() {
        return otherMarket;
    }

    public void setOtherMarket(String otherMarket) {
        this.otherMarket = otherMarket;
    }

    public String getOtherId() {
        return otherId;
    }

    public void setOtherId(String otherId) {
        this.otherId = otherId;
    }

    public String getOtherAltId() {
        return otherAltId;
    }

    public void setOtherAltId(String otherAltId) {
        this.otherAltId = otherAltId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
    }

    public void save() throws Exception {
        Relation relation = new Relation();
        relation.setAltId(getAltId());
        relation.setId(getId());
        relation.setMarket(getMarket());
        relation.setOtherAltId(getOtherAltId());
        relation.setOtherId(getOtherId());
        relation.setOtherMarket(getOtherMarket());
        relation.setRecord(getRecord());
        relation.setType(getType());
        relation.setValue(getValue());
        relation.save();
    }

    public static List<RelationItem> getAll() throws Exception {
        List<Relation> relations = Relation.getAll();
        List<RelationItem> relationItems = new ArrayList<>();
        for (Relation relation : relations) {
            RelationItem memoryItem = getRelationItem(relation);
            relationItems.add(memoryItem);
        }
        return relationItems;
    }

    public static List<RelationItem> getAll(String market) throws Exception {
        List<Relation> configs = Relation.getAll(market);
        List<RelationItem> configItems = new ArrayList<>();
        for (Relation config : configs) {
            RelationItem memoryItem = getRelationItem(config);
            configItems.add(memoryItem);
        }
        return configItems;
    }

    private static RelationItem getRelationItem(Relation relation) {
        RelationItem relationItem = new RelationItem();
        relationItem.setAltId(relation.getAltId());
        relationItem.setId(relation.getId());
        relationItem.setMarket(relation.getMarket());
        relationItem.setOtherAltId(relation.getOtherAltId());
        relationItem.setOtherId(relation.getOtherId());
        relationItem.setOtherMarket(relation.getOtherMarket());
        relationItem.setRecord(relation.getRecord());
        relationItem.setType(relation.getType());
        relationItem.setValue(relation.getValue());
        return relationItem;
    }

    @Override
    public String toString() {
        return market + " " + id + " " + altId + " " + type + " " + value + " " + otherMarket + " " + otherId + " " + otherAltId;
    }
}
