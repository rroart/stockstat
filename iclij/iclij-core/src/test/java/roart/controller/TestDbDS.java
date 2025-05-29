package roart.controller;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Component;

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
import roart.iclij.config.IclijConfig;

@Component
public class TestDbDS extends DbDS {

    @Override
    public List<StockDTO> getAllStocks() throws Exception {
        return null;
    }

    @Override
    public List<StockDTO> getStocksByMarket(String market) throws Exception {
        return null;
    }

    @Override
    public MetaDTO getMetaByMarket(String market) throws Exception {
        return null;
    }

    @Override
    public List<MetaDTO> getAllMetas() {
        return null;
    }

    @Override
    public List<MemoryDTO> getAllMemories() {
        return null;
    }

    @Override
    public List<MemoryDTO> getMemoriesByMarket(String market) {
        return null;
    }

    @Override
    public List<MemoryDTO> getMemories(String market, String action, String component, String subcomponent,
            String parameters, Date startDate, Date endDate) {
        return null;
    }

    @Override
    public List<TimingDTO> getAllTimings() {
        return null;
    }

    @Override
    public List<TimingDTO> getTimings(String market, String action, Date startDate, Date endDate) {
        return null;
    }

    @Override
    public List<RelationDTO> getAllRelations() {
        return null;
    }

    @Override
    public List<IncDecDTO> getAllIncDecs() {
        return null;
    }

    @Override
    public List<IncDecDTO> getIncDecs(String market, Date startDate, Date endDate, String parameters) {
        return null;
    }

    @Override
    public List<ConfigDTO> getAllConfigs() {
        return null;
    }

    @Override
    public List<ConfigDTO> getConfigs(String market, String action, String component, String subcomponent,
            String parameters, Date startDate, Date endDate) {
        return null;
    }

    @Override
    public List<ConfigDTO> getConfigsByMarket(String market) {
        return null;
    }

    @Override
    public List<MLMetricsDTO> getAllMLMetrics() {
        return null;
    }

    @Override
    public List<MLMetricsDTO> getMLMetrics(String market, Date startDate, Date endDate) {
        return null;
    }

    @Override
    public SimDataDTO getSimData(String market, String dbid) {
        return null;
    }

    @Override
    public List<SimDataDTO> getAllSimData() {
        return null;
    }

    @Override
    public List<SimDataDTO> getAllSimData(String market) {
        return null;
    }

    @Override
    public List<SimDataDTO> getAllSimData(String market, LocalDate startDate, LocalDate endDate) {
        return null;
    }

    @Override
    public List<AboveBelowDTO> getAllAboveBelow() {
        return null;
    }

    @Override
    public List<AboveBelowDTO> getAllAboveBelow(String market, Date startDate, Date endDate) {
        return null;
    }

    @Override
    public List<ActionComponentDTO> getAllActionComponent() {
        return null;
    }

    @Override
    public List<TimingBLDTO> getAllTimingBL() {
        return null;
    }

    @Override
    public List<ContDTO> getAllConts() {
        return null;
    }

    @Override
    public void save(Object object) {
        
    }

    @Override
    public void deleteById(Object object, String dbid) {
        
    }

    @Override
    public void delete(Object object, String market, String action, String component, String subcomponent,
            Date startDate, Date endDate) {
        
    }

    @Override
    public List<String> getMarkets() {
        return null;
    }

    @Override
    public List<Date> getDates(String market) {
        return null;
    }

    @Override
    public List<SimRunDataDTO> getAllSimRunData(String market, LocalDate startDate, LocalDate endDate) {
        return null;
    }

}
