package roart.db.hibernate;

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
import roart.db.model.MLMetrics;
import roart.db.model.Stock;

public class DbHibernateAccess extends DbAccess {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    private static DbAccess instance;
    
    @Override
    public List<StockItem> getStocksByMarket(String market) throws Exception {
    	return DbHibernate.getAll(market);
    }

	@Override
	public MetaItem getMetaByMarket(String market) throws Exception {
		return DbHibernate.getMarket(market);
	}

    public static DbAccess instance() {
        if (instance == null) {
            instance = new DbHibernateAccess();
        }
        return instance;
    }

    @Override
    public List<String> getMarkets() {
        try {
        return DbHibernate.getMarkets();
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public List<MetaItem> getAllMetas() {
        try {
        return DbHibernate.getMetas();
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public void save(Object object) {
        DbHibernate.save(object);
    }
    
    @Override
    public void deleteById(Object object, String dbid) {
        DbHibernate.deleteById(object, dbid);
    }
    public void delete(Object object, String market, String action, String component, String subcomponent, Date startDate, Date endDate) {
        DbHibernate.delete(object, market, action, component, subcomponent, startDate, endDate);
    }

    @Override
    public List<MemoryItem> getAllMemories() {
        return DbHibernate.getMemories();
    }

    @Override
    public List<MemoryItem> getMemoriesByMarket(String market) {
        return DbHibernate.getMemoriesByMarket(market);
    }

    @Override
    public List<MemoryItem> getMemories(String market, String action, String component, String subcomponent,
            String parameters, Date startDate, Date endDate) {
        return DbHibernate.getMemories(market, null, null, null, null, startDate, endDate);
    }

    @Override
    public List<TimingItem> getAllTimings() {
        return DbHibernate.getTimings();
    }

    @Override
    public List<TimingItem> getTimings(String market, String action, Date startDate, Date endDate) {
        return DbHibernate.getTiming(market, action, startDate, endDate);
    }

    @Override
    public List<RelationItem> getAllRelations() {
        return DbHibernate.getRelations();
    }

    @Override
    public List<IncDecItem> getAllIncDecs() {
        return DbHibernate.getIncDecs();
    }

    @Override
    public List<IncDecItem> getIncDecs(String market, Date startDate, Date endDate, String parameters) {
        return DbHibernate.getIncDecs(market, startDate, endDate, parameters);
    }

    @Override
    public List<ConfigItem> getConfigs(String market, String action, String component, String subcomponent,
            String parameters, Date startDate, Date endDate) {
        return DbHibernate.getConfigs(market, action, component, subcomponent, parameters, startDate, endDate);
    }

    @Override
    public List<ConfigItem> getConfigsByMarket(String market) {
        try {
            return DbHibernate.getConfigsByMarket(market);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public List<MLMetricsItem> getAllMLMetrics() {
        return DbHibernate.getMLMetrics();
    }

    @Override
    public List<MLMetricsItem> getMLMetrics(String market, Date startDate, Date endDate) {
        return DbHibernate.getMLMetrics(market, startDate, endDate);
    }

    @Override
    public List<SimDataItem> getAllSimData(String market, LocalDate startDate, LocalDate endDate) {
        return DbHibernate.getSimData(market, startDate, endDate);
    }

    @Override
    public List<AboveBelowItem> getAllAboveBelow(String market, Date startDate, Date endDate) {
        return DbHibernate.getAllAboveBelow(market, startDate, endDate);
    }

    @Override
    public List<ActionComponentItem> getAllActionComponent() {
        return DbHibernate.getAllActionComponent();
    }

    @Override
    public List<TimingBLItem> getAllTimingBL() {
        return DbHibernate.getAllTimingBLItem();
    }

    @Override
    public List<Date> getDates(String market) {
        return DbHibernate.getDates(market);
    }

    @Override
    public List<ContItem> getAllConts() {
        return DbHibernate.getAllCont();
    }

    @Override
    public List<StockItem> getAllStocks() throws Exception {
        return DbHibernate.getAllStocks();
    }

    @Override
    public List<ConfigItem> getAllConfigs() {
        return DbHibernate.getAllConfigs();
    }

    @Override
    public List<SimDataItem> getAllSimData(String market) {
        return DbHibernate.getAllSimData(market);
    }

    @Override
    public List<AboveBelowItem> getAllAboveBelow() {
        return DbHibernate.getAllAboveBelow();
    }

    @Override
    public List<SimDataItem> getAllSimData() {
        return DbHibernate.getAllSimData();
    }
}

