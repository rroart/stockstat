package roart.db.common;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

public abstract class DbAccess {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    public abstract List<StockItem> getAllStocks() throws Exception;

    public abstract List<StockItem> getStocksByMarket(String market) throws Exception;

    public abstract MetaItem getMetaByMarket(String market) throws Exception;

    public abstract List<MetaItem> getAllMetas();

    public abstract List<MemoryItem> getAllMemories();

    public abstract List<MemoryItem> getMemoriesByMarket(String market);

    public abstract List<MemoryItem> getMemories(String market, String action, String component, String subcomponent, String parameters, Date startDate, Date endDate);

    public abstract List<TimingItem> getAllTimings();

    public abstract List<TimingItem> getTimings(String market, String action, Date startDate, Date endDate);

    public abstract List<RelationItem> getAllRelations();

    public abstract List<IncDecItem> getAllIncDecs();

    public abstract List<IncDecItem> getIncDecs(String market, Date startDate, Date endDate, String parameters);

    public abstract List<ConfigItem> getAllConfigs();

    public abstract List<ConfigItem> getConfigs(String market, String action, String component, String subcomponent, String parameters, Date startDate, Date endDate);

    public abstract List<ConfigItem> getConfigsByMarket(String market);

    public abstract List<MLMetricsItem> getAllMLMetrics();

    public abstract List<MLMetricsItem> getMLMetrics(String market, Date startDate, Date endDate);

    public abstract List<SimDataItem> getAllSimData();

    public abstract List<SimDataItem> getAllSimData(String market);

    public abstract List<SimDataItem> getAllSimData(String market, LocalDate startDate, LocalDate endDate);

    public abstract List<AboveBelowItem> getAllAboveBelow();

    public abstract List<AboveBelowItem> getAllAboveBelow(String market, Date startDate, Date endDate);

    public abstract List<ActionComponentItem> getAllActionComponent();

    public abstract List<TimingBLItem> getAllTimingBL();

    public abstract List<ContItem> getAllConts();

    public abstract void save(Object object);

    public abstract void deleteById(Object object, String dbid);

    public abstract void delete(Object object, String market, String action, String component, String subcomponent, Date startDate, Date endDate);

    public abstract List<String> getMarkets();

    public abstract List<Date> getDates(String market);

}

