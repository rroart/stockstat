package roart.common.springdata.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import roart.common.util.JsonUtil;
import roart.common.util.TimeUtil;

@Table
public class Relation {
    @Id
    private Long dbid;

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

    @Override
    public String toString() {
        return market + " " + id + " " + altId + " " + type + " " + value + " " + otherMarket + " " + otherId + " " + otherAltId + "\n";
    }
}
