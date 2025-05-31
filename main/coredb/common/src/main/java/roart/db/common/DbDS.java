package roart.db.common;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

public abstract class DbDS {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    public abstract List<StockDTO> getAllStocks() throws Exception;

    public abstract List<StockDTO> getStocksByMarket(String market) throws Exception;

    public abstract MetaDTO getMetaByMarket(String market) throws Exception;

    public abstract List<MetaDTO> getAllMetas();

    public abstract List<MemoryDTO> getAllMemories();

    public abstract List<MemoryDTO> getMemoriesByMarket(String market);

    public abstract List<MemoryDTO> getMemories(String market, String action, String component, String subcomponent, String parameters, Date startDate, Date endDate);

    public abstract List<TimingDTO> getAllTimings();

    public abstract List<TimingDTO> getTimings(String market, String action, Date startDate, Date endDate);

    public abstract List<RelationDTO> getAllRelations();

    public abstract List<IncDecDTO> getAllIncDecs();

    public abstract List<IncDecDTO> getIncDecs(String market, Date startDate, Date endDate, String parameters);

    public abstract List<ConfigDTO> getAllConfigs();

    public abstract List<ConfigDTO> getConfigs(String market, String action, String component, String subcomponent, String parameters, Date startDate, Date endDate);

    public abstract List<ConfigDTO> getConfigsByMarket(String market);

    public abstract List<MLMetricsDTO> getAllMLMetrics();

    public abstract List<MLMetricsDTO> getMLMetrics(String market, Date startDate, Date endDate);
    
    public abstract SimDataDTO getSimData(String dbid);

    public abstract List<SimDataDTO> getAllSimData();

    public abstract List<SimDataDTO> getAllSimData(String market);

    public abstract List<SimDataDTO> getAllSimData(String market, LocalDate startDate, LocalDate endDate);

    public abstract List<AboveBelowDTO> getAllAboveBelow();

    public abstract List<AboveBelowDTO> getAllAboveBelow(String market, Date startDate, Date endDate);

    public abstract List<ActionComponentDTO> getAllActionComponent();

    public abstract List<TimingBLDTO> getAllTimingBL();

    public abstract List<ContDTO> getAllConts();

    public abstract void save(Object object);

    public abstract void deleteById(Object object, String dbid);

    public abstract void delete(Object object, String market, String action, String component, String subcomponent, Date startDate, Date endDate);

    public abstract List<String> getMarkets();

    public abstract List<Date> getDates(String market);

    public abstract List<SimRunDataDTO> getAllSimRunData(String market, LocalDate startDate, LocalDate endDate);

}

