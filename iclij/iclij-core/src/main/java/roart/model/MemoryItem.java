package roart.model;

import java.util.Date;

public class MemoryItem {
    //private Long id;
    private Date record;
    private Date date;
    private String market;
    private Double testaccuracy;
    private Double confidence;
    private Double learnConfidence;
    private String category;
    private String component;
    private String subcomponent;
    private String info;
    private Integer futuredays;
    private Date futuredate;
    private Long positives;
    private Long size;
    private Double threshold;
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
    public MemoryItem() {
        this.record = new Date();
    }
    @Override
    public String toString() {
        String ret = "Record " + record + "\n";
        ret += component + " : " + category + " : " + date + " futuredays " + futuredays + " " + futuredate + "\n";
        if (subcomponent != null) {
            ret += subcomponent + "\n";
        }
        if (info != null) {
            ret += info + "\n";
        }
        if (testaccuracy != null) {
            ret += "Test accuracy " + testaccuracy + "\n";
        }
        if (threshold != null) {
            ret += "Threshold " + threshold + "\n";
        }
        ret += "Confidence " + nullToEmpty(confidence) + " (positives/size : " + positives + "/" + size + ")\n";
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
    public Date getRecord() {
        return record;
    }
    public void setRecord(Date record) {
        this.record = record;
    }
    public Date getDate() {
        return date;
    }
    public void setDate(Date date) {
        this.date = date;
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
    public Date getFuturedate() {
        return futuredate;
    }
    public void setFuturedate(Date futuredate) {
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
    public Double getThreshold() {
        return threshold;
    }
    public void setThreshold(Double threshold) {
        this.threshold = threshold;
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
    public void save() throws Exception {
        Memory memory = new Memory();
        memory.setCategory(getCategory());
        memory.setComponent(getComponent());
        memory.setConfidence(getConfidence());
        memory.setDate(getDate());
        memory.setFn(getFn());
        memory.setFnConf(getFnConf());
        memory.setFnProb(getFnProb());
        memory.setFnProbConf(getFnProbConf());
        memory.setFnSize(getFnSize());
        memory.setFp(getFp());
        memory.setFpConf(getFpConf());
        memory.setFpProb(getFpProb());
        memory.setFpProbConf(getFpProbConf());
        memory.setFpSize(getFpSize());
        memory.setFuturedate(getFuturedate());
        memory.setFuturedays(getFuturedays());
        memory.setInfo(getInfo());
        memory.setLearnConfidence(getLearnConfidence());
        memory.setMarket(getMarket());
        memory.setPositives(getPositives());
        memory.setSize(getSize());
        memory.setSubcomponent(getSubcomponent());
        memory.setTestaccuracy(getTestaccuracy());
        memory.setThreshold(getThreshold());
        memory.setTn(getTn());
        memory.setTnConf(getTnConf());
        memory.setTnProb(getTnProb());
        memory.setTnProbConf(getTnProbConf());
        memory.setTnSize(getTnSize());
        memory.setTp(getTp());
        memory.setTpConf(getTpConf());
        memory.setTpProb(getTpProb());
        memory.setTpProbConf(getTpProbConf());
        memory.setTpSize(getTpSize());
        memory.save();
    }
}
