package roart.controller;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Component;

import roart.common.model.AboveBelowItem;
import roart.common.model.ActionComponentItem;
import roart.common.model.ConfigItem;
import roart.common.model.ContItem;
import roart.common.model.IncDecItem;
import roart.common.model.MLMetricsItem;
import roart.common.model.MemoryItem;
import roart.common.model.MetaItem;
import roart.common.model.RelationItem;
import roart.common.model.SimDataItem;
import roart.common.model.StockItem;
import roart.common.model.TimingBLItem;
import roart.common.model.TimingItem;
import roart.db.common.DbDS;
import roart.iclij.config.IclijConfig;

@Component
public class TestDbDS extends DbDS {

    @Override
    public List<StockItem> getAllStocks() throws Exception {
        return null;
    }

    @Override
    public List<StockItem> getStocksByMarket(String market) throws Exception {
        return null;
    }

    @Override
    public MetaItem getMetaByMarket(String market) throws Exception {
        return null;
    }

    @Override
    public List<MetaItem> getAllMetas() {
        return null;
    }

    @Override
    public List<MemoryItem> getAllMemories() {
        return null;
    }

    @Override
    public List<MemoryItem> getMemoriesByMarket(String market) {
        return null;
    }

    @Override
    public List<MemoryItem> getMemories(String market, String action, String component, String subcomponent,
            String parameters, Date startDate, Date endDate) {
        return null;
    }

    @Override
    public List<TimingItem> getAllTimings() {
        return null;
    }

    @Override
    public List<TimingItem> getTimings(String market, String action, Date startDate, Date endDate) {
        return null;
    }

    @Override
    public List<RelationItem> getAllRelations() {
        return null;
    }

    @Override
    public List<IncDecItem> getAllIncDecs() {
        return null;
    }

    @Override
    public List<IncDecItem> getIncDecs(String market, Date startDate, Date endDate, String parameters) {
        return null;
    }

    @Override
    public List<ConfigItem> getAllConfigs() {
        return null;
    }

    @Override
    public List<ConfigItem> getConfigs(String market, String action, String component, String subcomponent,
            String parameters, Date startDate, Date endDate) {
        return null;
    }

    @Override
    public List<ConfigItem> getConfigsByMarket(String market) {
        return null;
    }

    @Override
    public List<MLMetricsItem> getAllMLMetrics() {
        return null;
    }

    @Override
    public List<MLMetricsItem> getMLMetrics(String market, Date startDate, Date endDate) {
        return null;
    }

    @Override
    public List<SimDataItem> getAllSimData() {
        return null;
    }

    @Override
    public List<SimDataItem> getAllSimData(String market) {
        return null;
    }

    @Override
    public List<SimDataItem> getAllSimData(String market, LocalDate startDate, LocalDate endDate) {
        return null;
    }

    @Override
    public List<AboveBelowItem> getAllAboveBelow() {
        return null;
    }

    @Override
    public List<AboveBelowItem> getAllAboveBelow(String market, Date startDate, Date endDate) {
        return null;
    }

    @Override
    public List<ActionComponentItem> getAllActionComponent() {
        return null;
    }

    @Override
    public List<TimingBLItem> getAllTimingBL() {
        return null;
    }

    @Override
    public List<ContItem> getAllConts() {
        return null;
    }

    @Override
    public void save(Object object) {
        
    }

    @Override
    public void deleteById(Object object, String dbid) {
        
    }

    @Override
    public void delete(Object object, String market, String action, String component, String subcomponent,
            Date startDate, Date endDate) {
        
    }

    @Override
    public List<String> getMarkets() {
        return null;
    }

    @Override
    public List<Date> getDates(String market) {
        return null;
    }

}
