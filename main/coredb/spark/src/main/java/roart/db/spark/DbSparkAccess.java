package roart.db.spark;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.common.config.MyMyConfig;
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
import roart.pipeline.common.Calculatable;

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

    @Override
    public Map<String, Object[]> doCalculationsArr(MyMyConfig conf, Map<String, double[][]> listMap, String key,
            Calculatable indicator, boolean wantPercentizedPriceIndex) {
        return DbSpark.doCalculationsArrNonNull(listMap, key, indicator, wantPercentizedPriceIndex);
    }

    public static DbAccess instance(MyMyConfig conf) {
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
    public List<MetaItem> getMetas() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<MemoryItem> getMemories() {
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
    public List<TimingItem> getTimings() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<TimingItem> getTimings(String market, String action, Date startDate, Date endDate) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<RelationItem> getRelations() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<IncDecItem> getIncDecs() {
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
    public List<MLMetricsItem> getMLMetrics() {
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
    public List<TimingBLItem> getAllTimingBLItem() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<Date> getDates(String market) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<ContItem> getAllContItem() {
        // TODO Auto-generated method stub
        return null;
    }

}

