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
import roart.common.springdata.repository.SpringStockRepository;
import roart.common.springdata.repository.StockRepository;
import roart.db.common.DbDS;

@Component
public class DbSpringDS extends DbDS {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    DbSpring service;
    
    @Override
    public List<StockDTO> getStocksByMarket(String market) throws Exception {
    	return service.getStocksByMarket(market);
    }

	@Override
	public MetaDTO getMetaByMarket(String market) throws Exception {
		return service.getMetaByMarket(market);
	}

    @Override
    public List<String> getMarkets() {
        return service.getMarkets();
    }

    @Override
    public List<MetaDTO> getAllMetas() {
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
    public List<MemoryDTO> getAllMemories() {
        return service.getMemories();
    }

    @Override
    public List<MemoryDTO> getMemoriesByMarket(String market) {
        return service.getMemoriesByMarket(market);
    }

    @Override
    public List<MemoryDTO> getMemories(String market, String action, String component, String subcomponent,
            String parameters, Date startDate, Date endDate) {
        return service.getMemories(market, action, component, subcomponent, parameters, startDate, endDate);
    }

    @Override
    public List<TimingDTO> getAllTimings() {
        return service.getTimings();
    }

    @Override
    public List<TimingDTO> getTimings(String market, String action, Date startDate, Date endDate) {
        return service.getTimings(market, action, startDate, endDate);
    }

    @Override
    public List<RelationDTO> getAllRelations() {
        return service.getRelations();
    }

    @Override
    public List<IncDecDTO> getAllIncDecs() {
        return service.getIncDecs();
    }

    @Override
    public List<IncDecDTO> getIncDecs(String market, Date startDate, Date endDate, String parameters) {
        return service.getIncDecs(market, startDate, endDate, parameters);
    }

    @Override
    public List<ConfigDTO> getConfigs(String market, String action, String component, String subcomponent,
            String parameters, Date startDate, Date endDate) {
        return service.getConfigs(market, action, component, subcomponent, parameters, startDate, endDate);
    }

    @Override
    public List<ConfigDTO> getConfigsByMarket(String market) {
        return service.getConfigsByMarket(market);
    }

    @Override
    public List<MLMetricsDTO> getAllMLMetrics() {
        return service.getMLMetrics();
    }

    @Override
    public List<MLMetricsDTO> getMLMetrics(String market, Date startDate, Date endDate) {
        return service.getMLMetrics(market, startDate, endDate);
    }

    @Override
    public SimDataDTO getSimData(String dbid) {
        try {
            return service.getSimData(dbid);
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
            return null;
        }
    }

    @Override
    public List<SimDataDTO> getAllSimData(String market, LocalDate startDate, LocalDate endDate) {
        try {
            return service.getSimData(market, startDate, endDate);
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
            return null;
        }
    }

    @Override
    public List<AboveBelowDTO> getAllAboveBelow(String market, Date startDate, Date endDate) {
       try {
        return service.getAboveBelow(market, startDate, endDate);
    } catch (Exception e) {
        log.error(Constants.EXCEPTION, e);
        return null;
    }
    }

    @Override
    public List<ActionComponentDTO> getAllActionComponent() {
        return service.getActionComponent();
    }

    @Override
    public List<TimingBLDTO> getAllTimingBL() {
        return service.getTimingBL();
    }

    @Override
    public List<Date> getDates(String market) {
        return service.getDates(market);
    }

    @Override
    public List<ContDTO> getAllConts() {
        return service.getCont();
    }

    @Override
    public List<StockDTO> getAllStocks() throws Exception {
        return service.getAllStocks();
    }

    @Override
    public List<ConfigDTO> getAllConfigs() {
        return service.getAllConfigs();
    }

    @Override
    public List<SimDataDTO> getAllSimData(String market) {
        return service.getAllSimData();
    }

    @Override
    public List<AboveBelowDTO> getAllAboveBelow() {
        return service.getAllAboveBelow();
    }

    @Override
    public List<SimDataDTO> getAllSimData() {
        return service.getAllSimData();
    }

    @Override
    public List<SimRunDataDTO> getAllSimRunData(String market, LocalDate startDate, LocalDate endDate) {
        return service.getAllSimRunData();
    }

}

