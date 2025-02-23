package roart.db.spring;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;

import roart.iclij.config.IclijConfig;
import roart.common.constants.Constants;
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
import roart.common.springdata.repository.SpringStockRepository;
import roart.common.springdata.repository.StockRepository;
import roart.db.common.DbAccess;

@Component
public class DbSpringAccess extends DbAccess {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    DbSpring service;
    
    @Override
    public List<StockItem> getStocksByMarket(String market) throws Exception {
    	return service.getStocksByMarket(market);
    }

	@Override
	public MetaItem getMetaByMarket(String market) throws Exception {
		return service.getMetaByMarket(market);
	}

    @Override
    public List<String> getMarkets() {
        return service.getMarkets();
    }

    @Override
    public List<MetaItem> getAllMetas() {
        return service.getMetas();
    }

    @Override
    public void save(Object object) {
        service.save(object);
    }
    public void deleteById(Object object, String dbid) {
        service.deleteById(object, dbid);
    }
    
    public void delete(Object object, String market, String action, String component, String subcomponent, Date startDate, Date endDate) {
        service.delete(object, market, action, component, subcomponent, startDate, endDate);
    }

    @Override
    public List<MemoryItem> getAllMemories() {
        return service.getMemories();
    }

    @Override
    public List<MemoryItem> getMemoriesByMarket(String market) {
        return service.getMemoriesByMarket(market);
    }

    @Override
    public List<MemoryItem> getMemories(String market, String action, String component, String subcomponent,
            String parameters, Date startDate, Date endDate) {
        return service.getMemories(market, action, component, subcomponent, parameters, startDate, endDate);
    }

    @Override
    public List<TimingItem> getAllTimings() {
        return service.getTimings();
    }

    @Override
    public List<TimingItem> getTimings(String market, String action, Date startDate, Date endDate) {
        return service.getTimings(market, action, startDate, endDate);
    }

    @Override
    public List<RelationItem> getAllRelations() {
        return service.getRelations();
    }

    @Override
    public List<IncDecItem> getAllIncDecs() {
        return service.getIncDecs();
    }

    @Override
    public List<IncDecItem> getIncDecs(String market, Date startDate, Date endDate, String parameters) {
        return service.getIncDecs(market, startDate, endDate, parameters);
    }

    @Override
    public List<ConfigItem> getConfigs(String market, String action, String component, String subcomponent,
            String parameters, Date startDate, Date endDate) {
        return service.getConfigs(market, action, component, subcomponent, parameters, startDate, endDate);
    }

    @Override
    public List<ConfigItem> getConfigsByMarket(String market) {
        return service.getConfigsByMarket(market);
    }

    @Override
    public List<MLMetricsItem> getAllMLMetrics() {
        return service.getMLMetrics();
    }

    @Override
    public List<MLMetricsItem> getMLMetrics(String market, Date startDate, Date endDate) {
        return service.getMLMetrics(market, startDate, endDate);
    }

    @Override
    public List<SimDataItem> getAllSimData(String market, LocalDate startDate, LocalDate endDate) {
        try {
            return service.getSimData(market, startDate, endDate);
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
            return null;
        }
    }

    @Override
    public List<AboveBelowItem> getAllAboveBelow(String market, Date startDate, Date endDate) {
       try {
        return service.getAboveBelow(market, startDate, endDate);
    } catch (Exception e) {
        log.error(Constants.EXCEPTION, e);
        return null;
    }
    }

    @Override
    public List<ActionComponentItem> getAllActionComponent() {
        return service.getActionComponent();
    }

    @Override
    public List<TimingBLItem> getAllTimingBL() {
        return service.getTimingBL();
    }

    @Override
    public List<Date> getDates(String market) {
        return service.getDates(market);
    }

    @Override
    public List<ContItem> getAllConts() {
        return service.getCont();
    }

    @Override
    public List<StockItem> getAllStocks() throws Exception {
        return service.getAllStocks();
    }

    @Override
    public List<ConfigItem> getAllConfigs() {
        return service.getAllConfigs();
    }

    @Override
    public List<SimDataItem> getAllSimData(String market) {
        return service.getAllSimData();
    }

    @Override
    public List<AboveBelowItem> getAllAboveBelow() {
        return service.getAllAboveBelow();
    }

    @Override
    public List<SimDataItem> getAllSimData() {
        return service.getAllSimData();
    }

}

