package roart.db.hibernate;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.iclij.config.IclijConfig;
import roart.common.model.AboveBelowDTO;
import roart.common.model.ActionComponentDTO;
import roart.common.model.ConfigDTO;
import roart.common.model.ContDTO;
import roart.common.model.IncDecDTO;
import roart.common.model.MLMetricsDTO;
import roart.common.model.MemoryDTO;
import roart.common.model.MetaDTO;
import roart.common.model.RelationDTO;
import roart.common.model.SimRunDataDTO;
import roart.common.model.SimDataDTO;
import roart.common.model.StockDTO;
import roart.common.model.TimingBLDTO;
import roart.common.model.TimingDTO;
import roart.db.common.DbDS;
import roart.db.model.MLMetrics;
import roart.db.model.Stock;

public class DbHibernateDS extends DbDS {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    private static DbDS instance;
    
    @Override
    public List<StockDTO> getStocksByMarket(String market) throws Exception {
    	return DbHibernate.getAll(market);
    }

	@Override
	public MetaDTO getMetaByMarket(String market) throws Exception {
		return DbHibernate.getMarket(market);
	}

    public static DbDS instance() {
        if (instance == null) {
            instance = new DbHibernateDS();
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
    public List<MetaDTO> getAllMetas() {
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
    public List<MemoryDTO> getAllMemories() {
        return DbHibernate.getMemories();
    }

    @Override
    public List<MemoryDTO> getMemoriesByMarket(String market) {
        return DbHibernate.getMemoriesByMarket(market);
    }

    @Override
    public List<MemoryDTO> getMemories(String market, String action, String component, String subcomponent,
            String parameters, Date startDate, Date endDate) {
        return DbHibernate.getMemories(market, null, null, null, null, startDate, endDate);
    }

    @Override
    public List<TimingDTO> getAllTimings() {
        return DbHibernate.getTimings();
    }

    @Override
    public List<TimingDTO> getTimings(String market, String action, Date startDate, Date endDate) {
        return DbHibernate.getTiming(market, action, startDate, endDate);
    }

    @Override
    public List<RelationDTO> getAllRelations() {
        return DbHibernate.getRelations();
    }

    @Override
    public List<IncDecDTO> getAllIncDecs() {
        return DbHibernate.getIncDecs();
    }

    @Override
    public List<IncDecDTO> getIncDecs(String market, Date startDate, Date endDate, String parameters) {
        return DbHibernate.getIncDecs(market, startDate, endDate, parameters);
    }

    @Override
    public List<ConfigDTO> getConfigs(String market, String action, String component, String subcomponent,
            String parameters, Date startDate, Date endDate) {
        return DbHibernate.getConfigs(market, action, component, subcomponent, parameters, startDate, endDate);
    }

    @Override
    public List<ConfigDTO> getConfigsByMarket(String market) {
        try {
            return DbHibernate.getConfigsByMarket(market);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public List<MLMetricsDTO> getAllMLMetrics() {
        return DbHibernate.getMLMetrics();
    }

    @Override
    public List<MLMetricsDTO> getMLMetrics(String market, Date startDate, Date endDate) {
        return DbHibernate.getMLMetrics(market, startDate, endDate);
    }

    @Override
    public List<SimDataDTO> getAllSimData(String market, LocalDate startDate, LocalDate endDate) {
        return DbHibernate.getSimData(market, startDate, endDate);
    }

    @Override
    public List<AboveBelowDTO> getAllAboveBelow(String market, Date startDate, Date endDate) {
        return DbHibernate.getAllAboveBelow(market, startDate, endDate);
    }

    @Override
    public List<ActionComponentDTO> getAllActionComponent() {
        return DbHibernate.getAllActionComponent();
    }

    @Override
    public List<TimingBLDTO> getAllTimingBL() {
        return DbHibernate.getAllTimingBLDTO();
    }

    @Override
    public List<Date> getDates(String market) {
        return DbHibernate.getDates(market);
    }

    @Override
    public List<ContDTO> getAllConts() {
        return DbHibernate.getAllCont();
    }

    @Override
    public List<StockDTO> getAllStocks() throws Exception {
        return DbHibernate.getAllStocks();
    }

    @Override
    public List<ConfigDTO> getAllConfigs() {
        return DbHibernate.getAllConfigs();
    }

    @Override
    public List<SimDataDTO> getAllSimData(String market) {
        return DbHibernate.getAllSimData(market);
    }

    @Override
    public List<AboveBelowDTO> getAllAboveBelow() {
        return DbHibernate.getAllAboveBelow();
    }

    @Override
    public List<SimDataDTO> getAllSimData() {
        return DbHibernate.getAllSimData();
    }

    @Override
    public List<SimRunDataDTO> getAllSimRunData(String market, LocalDate startDate, LocalDate endDate) {
        return DbHibernate.getAllSimData2();
    }
}

