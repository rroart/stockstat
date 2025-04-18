package roart.db.spark;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.iclij.config.IclijConfig;
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
import roart.db.common.DbAccess;

public class DbSparkAccess extends DbAccess {

	private Logger log = LoggerFactory.getLogger(this.getClass());

	private static DbAccess instance;
	
	@Override
	public List<StockItem> getStocksByMarket(String market) throws Exception {
		return DbSpark.getAll(market);
	}

	@Override
	public MetaItem getMetaByMarket(String market) {
		return DbSpark.getMarket(market);
	}

    public static DbAccess instance(IclijConfig conf) {
        if (instance == null) {
            instance = new DbSparkAccess();
            new DbSpark(conf);
        }
        return instance;
    }

    @Override
    public List<String> getMarkets() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<MetaItem> getAllMetas() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<MemoryItem> getAllMemories() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<MemoryItem> getMemoriesByMarket(String market) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<MemoryItem> getMemories(String market, String action, String component, String subcomponent,
            String parameters, Date startDate, Date endDate) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<TimingItem> getAllTimings() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<TimingItem> getTimings(String market, String action, Date startDate, Date endDate) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<RelationItem> getAllRelations() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<IncDecItem> getAllIncDecs() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<IncDecItem> getIncDecs(String market, Date startDate, Date endDate, String parameters) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<ConfigItem> getConfigs(String market, String action, String component, String subcomponent,
            String parameters, Date startDate, Date endDate) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<ConfigItem> getConfigsByMarket(String market) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<MLMetricsItem> getAllMLMetrics() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<MLMetricsItem> getMLMetrics(String market, Date startDate, Date endDate) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void deleteById(Object object, String dbid) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void delete(Object object, String market, String action, String component, String subcomponent,
            Date startDate, Date endDate) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void save(Object object) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public List<SimDataItem> getAllSimData(String market, LocalDate startDate, LocalDate endDate) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<AboveBelowItem> getAllAboveBelow(String market, Date startDate, Date endDate) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<ActionComponentItem> getAllActionComponent() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<TimingBLItem> getAllTimingBL() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<Date> getDates(String market) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<ContItem> getAllConts() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<StockItem> getAllStocks() throws Exception {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<ConfigItem> getAllConfigs() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<SimDataItem> getAllSimData() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<SimDataItem> getAllSimData(String market) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<AboveBelowItem> getAllAboveBelow() {
        // TODO Auto-generated method stub
        return null;
    }

}

