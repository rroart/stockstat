package roart.iclij.model.action;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;

import roart.common.util.JsonUtil;
import roart.common.util.TimeUtil;
import roart.db.model.ActionComponent;
import roart.iclij.config.Market;
import roart.iclij.model.Parameters;

public class ActionComponentItem {

    private Long dbid;
    private String action;
    private String component;
    private String subcomponent;
    private String market;
    private double time;
    private boolean haverun;
    private int priority;
    //List<TimingItem> timings;
    private Boolean buy;
    private String parameters;
    private LocalDate record;
    private BlockingQueue result;
    
    public Long getDbid() {
        return dbid;
    }

    public void setDbid(Long dbid) {
        this.dbid = dbid;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
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

    public String getMarket() {
        return market;
    }

    public void setMarket(String market) {
        this.market = market;
    }

    public double getTime() {
        return time;
    }

    public void setTime(double time) {
        this.time = time;
    }

    public boolean isHaverun() {
        return haverun;
    }

    public void setHaverun(boolean haverun) {
        this.haverun = haverun;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public Boolean getBuy() {
        return buy;
    }

    public void setBuy(Boolean buy) {
        this.buy = buy;
    }

    public String getParameters() {
        return parameters;
    }

    public void setParameters(String parameters) {
        this.parameters = parameters;
    }

    public LocalDate getRecord() {
        return record;
    }

    public void setRecord(LocalDate record) {
        this.record = record;
    }

    public BlockingQueue getResult() {
        return result;
    }

    public void setResult(BlockingQueue result) {
        this.result = result;
    }

    public void save() throws Exception {
        ActionComponent config = new ActionComponent();
        config.setAction(getAction());
        config.setBuy(getBuy());
        config.setComponent(getComponent());
        config.setMarket(getMarket());
        config.setRecord(getRecord());
        config.setParameters(getParameters());
        config.setPriority(getPriority());
        config.setSubcomponent(getSubcomponent());
        config.save();
    }
    
    @Override
    public String toString() {
        String paramString = JsonUtil.convert(parameters);
        return record != null ? record.toString() : "" + " " + " " + market + " " + action + " " + component + " " + subcomponent + " " + paramString + " " + buy + " " + priority + " " + time + " " + haverun;
    }

    public String toStringId() {
        String paramString = JsonUtil.convert(parameters);
        return market + " " + action + " " + component + " " + subcomponent + " " + paramString + " " + buy + " " + priority;
    }

    public static List<ActionComponentItem> getAll() throws Exception {
        List<ActionComponent> configs = ActionComponent.getAll();
        List<ActionComponentItem> configItems = new ArrayList<>();
        for (ActionComponent config : configs) {
            ActionComponentItem memoryItem = getActionComponentItem(config);
            configItems.add(memoryItem);
        }
        return configItems;
    }

   public static List<ActionComponentItem> getAll(String market) throws Exception {
        List<ActionComponent> configs = ActionComponent.getAll(market);
        List<ActionComponentItem> configItems = new ArrayList<>();
        for (ActionComponent config : configs) {
            ActionComponentItem memoryItem = getActionComponentItem(config);
            configItems.add(memoryItem);
        }
        return configItems;
    }

   public static List<ActionComponentItem> getAll(String market, String action, String component, String subcomponent, String parameters, LocalDate startDate, LocalDate endDate) throws Exception {
       List<ActionComponent> configs = ActionComponent.getAll(market, action, component, subcomponent, parameters/*, startDate, endDate*/);
       List<ActionComponentItem> configItems = new ArrayList<>();
       for (ActionComponent config : configs) {
           ActionComponentItem memoryItem = getActionComponentItem(config);
           configItems.add(memoryItem);
       }
       return configItems;
   }

    private static ActionComponentItem getActionComponentItem(ActionComponent config) {
        ActionComponentItem item = new ActionComponentItem();
        item.setAction(config.getAction());
        item.setBuy(config.getBuy());
        item.setDbid(config.getDbid());
        //configItem.setDate(TimeUtil.convertDate(config.getDate()));
        //configItem.setId(config.getId());
        item.setComponent(config.getComponent());
        item.setMarket(config.getMarket());
        item.setRecord(config.getRecord());
        item.setParameters(config.getParameters());
        //configItem.setScore(config.getScore());
        item.setSubcomponent(config.getSubcomponent());
        item.setPriority(config.getPriority());
        //configItem.setValue(JsonUtil.strip(config.getValue()));
        return item;
    }

    public void delete() throws Exception {
        ActionComponent.delete(dbid);
    }

}
