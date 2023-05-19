package roart.db.common;

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
import roart.pipeline.common.Calculatable;

public abstract class DbAccess {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    public abstract List<StockItem> getStocksByMarket(String market) throws Exception;

	public abstract MetaItem getMetaByMarket(String market) throws Exception;

    public abstract Map<String, Object[]> doCalculationsArr(IclijConfig conf, Map<String, double[][]> listMap, String key,
            Calculatable indicator, boolean wantPercentizedPriceIndex);

    public abstract List<String> getMarkets();

    public abstract List<MetaItem> getMetas();

    public abstract List<MemoryItem> getMemories();

    public abstract List<MemoryItem> getMemoriesByMarket(String market);
    
    public abstract List<MemoryItem> getMemories(String market, String action, String component, String subcomponent, String parameters, Date startDate, Date endDate);
    
    public abstract List<TimingItem> getTimings();
    
    public abstract List<TimingItem> getTimings(String market, String action, Date startDate, Date endDate);
        
    public abstract List<RelationItem> getRelations();
    
    public abstract List<IncDecItem> getIncDecs();
    
    public abstract List<IncDecItem> getIncDecs(String market, Date startDate, Date endDate, String parameters);

    public abstract List<ConfigItem> getConfigs(String market, String action, String component, String subcomponent, String parameters, Date startDate, Date endDate);

    public abstract List<ConfigItem> getConfigsByMarket(String market);
    
    public abstract List<MLMetricsItem> getMLMetrics();
    
    public abstract List<MLMetricsItem> getMLMetrics(String market, Date startDate, Date endDate);
    
    public abstract void deleteById(Object object, String dbid);
    
    public abstract void delete(Object object, String market, String action, String component, String subcomponent, Date startDate, Date endDate);

    public abstract void save(Object object);

    public abstract List<SimDataItem> getAllSimData(String market, LocalDate startDate, LocalDate endDate);

    public abstract List<AboveBelowItem> getAllAboveBelow(String market, Date startDate, Date endDate);

    public abstract List<ActionComponentItem> getAllActionComponent();

    public abstract List<TimingBLItem> getAllTimingBLItem();

    public abstract List<Date> getDates(String market);

    public abstract List<ContItem> getAllContItem();
}

