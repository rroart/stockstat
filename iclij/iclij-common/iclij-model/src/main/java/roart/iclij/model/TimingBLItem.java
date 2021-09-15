package roart.iclij.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import roart.common.util.TimeUtil;
import roart.db.model.TimingBL;

public class TimingBLItem {

    private Long dbid;

    private LocalDate record;
    
    private String id;
    
    private int count;

    public Long getDbid() {
        return dbid;
    }

    public void setDbid(Long dbid) {
        this.dbid = dbid;
    }

    public LocalDate getRecord() {
        return record;
    }

    public void setRecord(LocalDate record) {
        this.record = record;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
    
    public void save() throws Exception {
        TimingBL timing = new TimingBL();
        timing.setCount(getCount());
        timing.setId(getId());
        timing.setRecord(getRecord());
        timing.save();
    }
    
    public static List<TimingBLItem> getAll() throws Exception {
        List<TimingBL> timings = TimingBL.getAll();
        List<TimingBLItem> timingItems = new ArrayList<>();
        for (TimingBL timing : timings) {
            TimingBLItem memoryItem = getTimingBLItem(timing);
            timingItems.add(memoryItem);
        }
        return timingItems;
    }

   public static List<TimingBLItem> getAll(String market) throws Exception {
        List<TimingBL> timings = TimingBL.getAll(market);
        List<TimingBLItem> timingItems = new ArrayList<>();
        for (TimingBL timing : timings) {
            TimingBLItem memoryItem = getTimingBLItem(timing);
            timingItems.add(memoryItem);
        }
        return timingItems;
    }

   public static List<TimingBLItem> getAll(String market, String action, Date startDate, Date endDate) throws Exception {
       List<TimingBL> configs = TimingBL.getAll(market, action, startDate, endDate);
       List<TimingBLItem> configItems = new ArrayList<>();
       for (TimingBL config : configs) {
           TimingBLItem memoryItem = getTimingBLItem(config);
           configItems.add(memoryItem);
       }
       return configItems;
   }

   private static TimingBLItem getTimingBLItem(TimingBL timing) {
        TimingBLItem timingItem = new TimingBLItem();
        timingItem.setCount(timing.getCount());
        timingItem.setDbid(timing.getDbid());
        timingItem.setId(timing.getId());
        timingItem.setRecord(timing.getRecord());
        return timingItem;
   }
   
   public void delete(String id) throws Exception {
       TimingBL.delete(id);
   }

}
