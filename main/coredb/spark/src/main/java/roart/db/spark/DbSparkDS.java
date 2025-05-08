package roart.db.spark;

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

public class DbSparkDS extends DbDS {

	private Logger log = LoggerFactory.getLogger(this.getClass());

	private static DbDS instance;
	
	@Override
	public List<StockDTO> getStocksByMarket(String market) throws Exception {
		return DbSpark.getAll(market);
	}

	@Override
	public MetaDTO getMetaByMarket(String market) {
		return DbSpark.getMarket(market);
	}

    public static DbDS instance(IclijConfig conf) {
        if (instance == null) {
            instance = new DbSparkDS();
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
    public List<MetaDTO> getAllMetas() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<MemoryDTO> getAllMemories() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<MemoryDTO> getMemoriesByMarket(String market) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<MemoryDTO> getMemories(String market, String action, String component, String subcomponent,
            String parameters, Date startDate, Date endDate) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<TimingDTO> getAllTimings() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<TimingDTO> getTimings(String market, String action, Date startDate, Date endDate) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<RelationDTO> getAllRelations() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<IncDecDTO> getAllIncDecs() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<IncDecDTO> getIncDecs(String market, Date startDate, Date endDate, String parameters) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<ConfigDTO> getConfigs(String market, String action, String component, String subcomponent,
            String parameters, Date startDate, Date endDate) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<ConfigDTO> getConfigsByMarket(String market) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<MLMetricsDTO> getAllMLMetrics() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<MLMetricsDTO> getMLMetrics(String market, Date startDate, Date endDate) {
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
    public List<SimDataDTO> getAllSimData(String market, LocalDate startDate, LocalDate endDate) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<AboveBelowDTO> getAllAboveBelow(String market, Date startDate, Date endDate) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<ActionComponentDTO> getAllActionComponent() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<TimingBLDTO> getAllTimingBL() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<Date> getDates(String market) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<ContDTO> getAllConts() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<StockDTO> getAllStocks() throws Exception {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<ConfigDTO> getAllConfigs() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<SimDataDTO> getAllSimData() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<SimDataDTO> getAllSimData(String market) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<AboveBelowDTO> getAllAboveBelow() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<SimRunDataDTO> getAllSimRunData(String market, LocalDate startDate, LocalDate endDate) {
        // TODO Auto-generated method stub
        return null;
    }

}

