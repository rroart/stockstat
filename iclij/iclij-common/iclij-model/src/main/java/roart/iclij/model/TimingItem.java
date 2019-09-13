package roart.iclij.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import roart.common.util.TimeUtil;
import roart.db.model.Timing;

public class TimingItem {
    private LocalDate record;
    
    private LocalDate date;
    
    private String market;

    private String action;
    
    private boolean evolve;
    
    private String component;
    
    private String subcomponent;
    
    private Double mytime;

    private Double score;
    
    private Boolean buy;
    
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

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public boolean isEvolve() {
        return evolve;
    }

    public void setEvolve(boolean evolve) {
        this.evolve = evolve;
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

    public Double getMytime() {
        return mytime;
    }

    public void setMytime(Double time) {
        this.mytime = time;
    }

    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
    }

    public Boolean getBuy() {
        return buy;
    }

    public void setBuy(Boolean buy) {
        this.buy = buy;
    }

    @Override
    public String toString() {
        return market + " " + component + " " + subcomponent + " " + action + " " + record + " " + date + " " + evolve + " " + mytime + " " + score + "\n"; 
    }
    
    public void save() throws Exception {
        Timing timing = new Timing();
        timing.setAction(getAction());
        timing.setBuy(getBuy());
        timing.setComponent(getComponent());
        timing.setDate(TimeUtil.convertDate(getDate()));
        timing.setEvolve(isEvolve());
        timing.setMarket(getMarket());
        timing.setRecord(TimeUtil.convertDate(getRecord()));
        timing.setTime(getMytime());
        timing.setScore(getScore());
        timing.setSubcomponent(getSubcomponent());
        timing.save();
    }
    
    public static List<TimingItem> getAll() throws Exception {
        List<Timing> timings = Timing.getAll();
        List<TimingItem> timingItems = new ArrayList<>();
        for (Timing timing : timings) {
            TimingItem memoryItem = getTimingItem(timing);
            timingItems.add(memoryItem);
        }
        return timingItems;
    }

   public static List<TimingItem> getAll(String market) throws Exception {
        List<Timing> timings = Timing.getAll(market);
        List<TimingItem> timingItems = new ArrayList<>();
        for (Timing timing : timings) {
            TimingItem memoryItem = getTimingItem(timing);
            timingItems.add(memoryItem);
        }
        return timingItems;
    }

    private static TimingItem getTimingItem(Timing timing) {
        TimingItem timingItem = new TimingItem();
        timingItem.setAction(timing.getAction());
        timingItem.setBuy(timing.getBuy());
        timingItem.setComponent(timing.getComponent());
        timingItem.setDate(TimeUtil.convertDate(timing.getDate()));
        timingItem.setEvolve(timing.isEvolve());
        timingItem.setMarket(timing.getMarket());
        timingItem.setRecord(TimeUtil.convertDate(timing.getRecord()));
        timingItem.setMytime(timing.getTime());
        timingItem.setScore(timing.getScore());
        timingItem.setSubcomponent(timing.getSubcomponent());
        return timingItem;
    }

    public void setTime(long time0) {
        this.mytime = ((double) (System.currentTimeMillis()) - time0) / 1000;
    }
}
