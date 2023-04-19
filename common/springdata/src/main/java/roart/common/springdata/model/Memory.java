package roart.common.springdata.model;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import roart.common.util.TimeUtil;

@Table
public class Memory {
    @Id
    private Long id;

    private String action;
    
    private LocalDate record;
    
    private LocalDate date;
    
    private Integer usedsec;
    
    private String market;
    
    private Double testaccuracy;
    
    private Double testloss;
    
    private Double confidence;
    
    private Double learnconfidence;
    
    private String category;
    
    private String type;
    
    private String component;
    
    private String subcomponent;
    
    private String localcomponent;
    
    private String description;
    
    private String info;
    
    private Integer futuredays;
    
    private LocalDate futuredate;
    
    private Long positives;
    
    private Long size;
    
    private Long abovepositives;
    
    private Long abovesize;
    
    private Long belowpositives;
    
    private Long belowsize;
    
    private String parameters;
    
    private Long tp;
    
    private Long tpsize;
    
    private Double tpconf;
    
    private Double tpprob;
    
    private Double tpprobconf;
    
    private Long tn;
    
    private Long tnsize;
    
    private Double tnconf;
    
    private Double tnprob;
    
    private Double tnprobconf;
    
    private Long fp;
    
    private Long fpsize;
    
    private Double fpconf;
    
    private Double fpprob;
    
    private Double fpprobconf;
    
    private Long fn;
    
    private Long fnsize;
    
    private Double fnconf;
    
    private Double fnprob;
    
    private Double fnprobconf;
    
    private Integer position;
    
    public Memory() {
    }
    
    @Override
    public String toString() {
        String ret = "Record " + record + "\n";
        ret += "Action : " + action + "\n";
        ret += "Market : " + market + "\n";
        if (usedsec != null) {
            ret += "Used time: " + usedsec + " seconds.\n";
        }
        if (type != null) {
            ret += type + "\n";
        }
        ret += component + " : " + category + " : " + date + " futuredays " + futuredays + " " + futuredate + "\n";
        if (position != null) {
            //ret += "Position : " + position + "\n";
        }
        if (subcomponent != null) {
            ret += subcomponent + "\n";
        }
        if (localcomponent != null) {
            ret += localcomponent + "\n";
        }
        if (description != null) {
            ret += description + "\n";
        }
        if (info != null) {
            ret += info + "\n";
        }
        if (testaccuracy != null) {
            ret += "Test accuracy " + testaccuracy + "\n";
        }
        if (testloss != null) {
            ret += "Test loss " + testloss + "\n";
        }
       if (parameters != null) {
            ret += "Threshold " + parameters + "\n";
        }
        ret += "Confidence " + nullToEmpty(confidence) + " (positives/size : " + positives + "/" + size + ")\n";
        ret += "AboveConfidence " + " (positives/size : " + abovepositives + "/" + abovesize + ")\n";
        ret += "BelowConfidence " + " (positives/size : " + belowpositives + "/" + belowsize + ")\n";
        if (learnconfidence != null) {
            ret += "Learning confidence " + nullToEmpty(learnconfidence) + "\n";
        }
        if (tpsize != null) {
            ret += "TP " + tp + " / " + tpsize + " " + nullToEmpty(tpconf) + "\n";
            if (tpprob != null) {
                ret += "Prob " + tpprob + " / " + tpsize + " " + nullToEmpty(tpprobconf) + "\n";
            }
        }
        if (tnsize != null) {
            ret += "TN " + tn + " / " + tnsize + " " + nullToEmpty(tnconf) + "\n";
            if (tnprob != null) {
                ret += "Prob " + tnprob + " / " + tnsize + " " + nullToEmpty(tnprobconf) + "\n";
            }
        }
        if (fpsize != null) {
            ret += "FP " + fp + " / " + fpsize + " " + nullToEmpty(fpconf) + "\n";
            if (fpprob != null) {
                ret += "Prob " + fpprob + " / " + fpsize + " " + nullToEmpty(fpprobconf) + "\n";
            }
        }
        if (fnsize != null) {
            ret += "FN " + fn + " / " + fnsize + " " + nullToEmpty(fnconf) + "\n";
            if (fnprob != null) {
                ret += "Prob " + fnprob + " / " + fnsize + " " + nullToEmpty(fnprobconf) + "\n";
            }
        }
        return ret;
    }
    
    private String nullToEmpty(Object obj) {
        return obj != null ? "" + obj : "";
    }
    /*
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    */
    
    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
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
    
    public Integer getUsedsec() {
        return usedsec;
    }
    
    public void setUsedsec(Integer usedsec) {
        this.usedsec = usedsec;
    }
    
    public String getMarket() {
        return market;
    }
    
    public void setMarket(String market) {
        this.market = market;
    }
    
    public Double getTestaccuracy() {
        return testaccuracy;
    }
    
    public void setTestaccuracy(Double testaccuracy) {
        this.testaccuracy = testaccuracy;
    }
    
    public Double getTestloss() {
        return testloss;
    }

    public void setTestloss(Double testloss) {
        this.testloss = testloss;
    }

    public Double getConfidence() {
        return confidence;
    }
    
    public void setConfidence(Double confidence) {
        this.confidence = confidence;
    }
    
    public Double getLearnConfidence() {
        return learnconfidence;
    }
    
    public void setLearnConfidence(Double learnConfidence) {
        this.learnconfidence = learnConfidence;
    }
    
    public String getCategory() {
        return category;
    }
    
    public void setCategory(String category) {
        this.category = category;
    }
    
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getInfo() {
        return info;
    }
    
    public void setInfo(String info) {
        this.info = info;
    }
    
    public Integer getFuturedays() {
        return futuredays;
    }
    
    public void setFuturedays(Integer futuredays) {
        this.futuredays = futuredays;
    }
    
    public LocalDate getFuturedate() {
        return futuredate;
    }
    
    public void setFuturedate(LocalDate futuredate) {
        this.futuredate = futuredate;
    }
    
    public Long getPositives() {
        return positives;
    }
    
    public void setPositives(Long positives) {
        this.positives = positives;
    }
    
    public Long getSize() {
        return size;
    }
    
    public void setSize(Long size) {
        this.size = size;
    }
    
    public Long getAbovepositives() {
        return abovepositives;
    }

    public void setAbovepositives(Long abovepositives) {
        this.abovepositives = abovepositives;
    }

    public Long getAbovesize() {
        return abovesize;
    }

    public void setAbovesize(Long abovesize) {
        this.abovesize = abovesize;
    }

    public Long getBelowpositives() {
        return belowpositives;
    }

    public void setBelowpositives(Long belowpositives) {
        this.belowpositives = belowpositives;
    }

    public Long getBelowsize() {
        return belowsize;
    }

    public void setBelowsize(Long belowsize) {
        this.belowsize = belowsize;
    }

    public String getParameters() {
        return parameters;
    }
    
    public void setParameters(String parameters) {
        this.parameters = parameters;
    }
    
    public Long getTp() {
        return tp;
    }
    
    public void setTp(Long tp) {
        this.tp = tp;
    }
    
    public Long getTpSize() {
        return tpsize;
    }
    
    public void setTpSize(Long tpSize) {
        this.tpsize = tpSize;
    }
    
    public Double getTpConf() {
        return tpconf;
    }
    
    public void setTpConf(Double tpConf) {
        this.tpconf = tpConf;
    }
    
    public Double getTpProb() {
        return tpprob;
    }
    
    public void setTpProb(Double tpProb) {
        this.tpprob = tpProb;
    }
    
    public Double getTpProbConf() {
        return tpprobconf;
    }
    
    public void setTpProbConf(Double tpProbConf) {
        this.tpprobconf = tpProbConf;
    }
    
    public Long getTn() {
        return tn;
    }
    
    public void setTn(Long tn) {
        this.tn = tn;
    }
    
    public Long getTnSize() {
        return tnsize;
    }
    
    public void setTnSize(Long tnSize) {
        this.tnsize = tnSize;
    }
    
    public Double getTnConf() {
        return tnconf;
    }
    
    public void setTnConf(Double tnConf) {
        this.tnconf = tnConf;
    }
    
    public Double getTnProb() {
        return tnprob;
    }
    
    public void setTnProb(Double tnProb) {
        this.tnprob = tnProb;
    }
    
    public Double getTnProbConf() {
        return tnprobconf;
    }
    
    public void setTnProbConf(Double tnProbConf) {
        this.tnprobconf = tnProbConf;
    }
    
    public Long getFp() {
        return fp;
    }
    
    public void setFp(Long fp) {
        this.fp = fp;
    }
    
    public Long getFpSize() {
        return fpsize;
    }
    
    public void setFpSize(Long fpSize) {
        this.fpsize = fpSize;
    }
    
    public Double getFpConf() {
        return fpconf;
    }
    
    public void setFpConf(Double fpConf) {
        this.fpconf = fpConf;
    }

    public Double getFpProb() {
        return fpprob;
    }
    
    public void setFpProb(Double fpProb) {
        this.fpprob = fpProb;
    }
    
    public Double getFpProbConf() {
        return fpprobconf;
    }
    
    public void setFpProbConf(Double fpProbConf) {
        this.fpprobconf = fpProbConf;
    }
    
    public Long getFn() {
        return fn;
    }
    
    public void setFn(Long fn) {
        this.fn = fn;
    }
    
    public Long getFnSize() {
        return fnsize;
    }
    
    public void setFnSize(Long fnSize) {
        this.fnsize = fnSize;
    }
    
    public Double getFnConf() {
        return fnconf;
    }
    
    public void setFnConf(Double fnConf) {
        this.fnconf = fnConf;
    }
    
    public Double getFnProb() {
        return fnprob;
    }
    
    public void setFnProb(Double fnProb) {
        this.fnprob = fnProb;
    }
    
    public Double getFnProbConf() {
        return fnprobconf;
    }
    
    public void setFnProbConf(Double fnProbConf) {
        this.fnprobconf = fnProbConf;
    }
    
    public Integer getPosition() {
        return position;
    }
    
    public void setPosition(Integer position) {
        this.position = position;
    }

}
