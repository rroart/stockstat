package roart.common.springdata.rowmapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import roart.common.model.SimRunDataDTO;

public class SimRunDataRowMapper implements RowMapper<SimRunDataDTO>{
    @Override
    public SimRunDataDTO mapRow(ResultSet rs, int rowNum) throws SQLException {
        SimRunDataDTO item = new SimRunDataDTO();
        item.setCorrelation(item.getCorrelation());
        item.setDbid(item.getDbid());
        item.setEnddate(item.getEnddate());
        item.setMarket(item.getMarket());
        item.setRecorddate(item.getRecorddate());
        item.setScore(item.getScore());
        item.setSimdatadbid(item.getSimdatadbid());
        item.setStartdate(item.getStartdate());
        return item;
    }

}
