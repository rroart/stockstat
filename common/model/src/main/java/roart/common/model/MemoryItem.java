package roart.common.model;

import java.time.LocalDate;

public class MemoryItem {
    //private Long id;

    private String action;
    
    private LocalDate record;
    
    private LocalDate date;
    
    private Integer usedsec;
    
    private String market;
    
    private Double testaccuracy;
    
    private Double testloss;
    
    private Double confidence;
    
    private Double learnConfidence;
    
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
    
    private Long tpSize;
    
    private Double tpConf;
    
    private Double tpProb;
    
    private Double tpProbConf;
    
    private Long tn;
    
    private Long tnSize;
    
    private Double tnConf;
    
    private Double tnProb;
    
    private Double tnProbConf;
    
    private Long fp;
    
    private Long fpSize;
    
    private Double fpConf;
    
    private Double fpProb;
    
    private Double fpProbConf;
    
    private Long fn;
    
    private Long fnSize;
    
    private Double fnConf;
    
    private Double fnProb;
    
    private Double fnProbConf;
    
    private Integer position;
    
    public MemoryItem() {
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
        if (learnConfidence != null) {
            ret += "Learning confidence " + nullToEmpty(learnConfidence) + "\n";
        }
        if (tpSize != null) {
            ret += "TP " + tp + " / " + tpSize + " " + nullToEmpty(tpConf) + "\n";
            if (tpProb != null) {
                ret += "Prob " + tpProb + " / " + tpSize + " " + nullToEmpty(tpProbConf) + "\n";
            }
        }
        if (tnSize != null) {
            ret += "TN " + tn + " / " + tnSize + " " + nullToEmpty(tnConf) + "\n";
            if (tnProb != null) {
                ret += "Prob " + tnProb + " / " + tnSize + " " + nullToEmpty(tnProbConf) + "\n";
            }
        }
        if (fpSize != null) {
            ret += "FP " + fp + " / " + fpSize + " " + nullToEmpty(fpConf) + "\n";
            if (fpProb != null) {
                ret += "Prob " + fpProb + " / " + fpSize + " " + nullToEmpty(fpProbConf) + "\n";
            }
        }
        if (fnSize != null) {
            ret += "FN " + fn + " / " + fnSize + " " + nullToEmpty(fnConf) + "\n";
            if (fnProb != null) {
                ret += "Prob " + fnProb + " / " + fnSize + " " + nullToEmpty(fnProbConf) + "\n";
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
        return learnConfidence;
    }
    
    public void setLearnConfidence(Double learnConfidence) {
        this.learnConfidence = learnConfidence;
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
        return tpSize;
    }
    
    public void setTpSize(Long tpSize) {
        this.tpSize = tpSize;
    }
    
    public Double getTpConf() {
        return tpConf;
    }
    
    public void setTpConf(Double tpConf) {
        this.tpConf = tpConf;
    }
    
    public Double getTpProb() {
        return tpProb;
    }
    
    public void setTpProb(Double tpProb) {
        this.tpProb = tpProb;
    }
    
    public Double getTpProbConf() {
        return tpProbConf;
    }
    
    public void setTpProbConf(Double tpProbConf) {
        this.tpProbConf = tpProbConf;
    }
    
    public Long getTn() {
        return tn;
    }
    
    public void setTn(Long tn) {
        this.tn = tn;
    }
    
    public Long getTnSize() {
        return tnSize;
    }
    
    public void setTnSize(Long tnSize) {
        this.tnSize = tnSize;
    }
    
    public Double getTnConf() {
        return tnConf;
    }
    
    public void setTnConf(Double tnConf) {
        this.tnConf = tnConf;
    }
    
    public Double getTnProb() {
        return tnProb;
    }
    
    public void setTnProb(Double tnProb) {
        this.tnProb = tnProb;
    }
    
    public Double getTnProbConf() {
        return tnProbConf;
    }
    
    public void setTnProbConf(Double tnProbConf) {
        this.tnProbConf = tnProbConf;
    }
    
    public Long getFp() {
        return fp;
    }
    
    public void setFp(Long fp) {
        this.fp = fp;
    }
    
    public Long getFpSize() {
        return fpSize;
    }
    
    public void setFpSize(Long fpSize) {
        this.fpSize = fpSize;
    }
    
    public Double getFpConf() {
        return fpConf;
    }
    
    public void setFpConf(Double fpConf) {
        this.fpConf = fpConf;
    }

    public Double getFpProb() {
        return fpProb;
    }
    
    public void setFpProb(Double fpProb) {
        this.fpProb = fpProb;
    }
    
    public Double getFpProbConf() {
        return fpProbConf;
    }
    
    public void setFpProbConf(Double fpProbConf) {
        this.fpProbConf = fpProbConf;
    }
    
    public Long getFn() {
        return fn;
    }
    
    public void setFn(Long fn) {
        this.fn = fn;
    }
    
    public Long getFnSize() {
        return fnSize;
    }
    
    public void setFnSize(Long fnSize) {
        this.fnSize = fnSize;
    }
    
    public Double getFnConf() {
        return fnConf;
    }
    
    public void setFnConf(Double fnConf) {
        this.fnConf = fnConf;
    }
    
    public Double getFnProb() {
        return fnProb;
    }
    
    public void setFnProb(Double fnProb) {
        this.fnProb = fnProb;
    }
    
    public Double getFnProbConf() {
        return fnProbConf;
    }
    
    public void setFnProbConf(Double fnProbConf) {
        this.fnProbConf = fnProbConf;
    }
    
    public Integer getPosition() {
        return position;
    }
    
    public void setPosition(Integer position) {
        this.position = position;
    }
    

}
