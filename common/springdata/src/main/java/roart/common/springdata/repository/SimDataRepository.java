package roart.common.springdata.repository;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import roart.common.model.SimDataDTO;
import roart.common.springdata.rowmapper.SimDataRowMapper;

@Repository
public class SimDataRepository {

    @Autowired
    NamedParameterJdbcTemplate jdbcTemplate;
        
    public SimDataDTO getById(String market, String dbid) throws Exception {
        String queryString = "select * from sim where ";
        if (market != null) {
            queryString += " market = :market";
        } else {
            queryString += " market like '%'";
        }
        if (dbid != null) {
            queryString += " and dbid = :dbid";
        }
        MapSqlParameterSource query = new MapSqlParameterSource();
        if (market != null) {
            query.addValue("market", market);
        }
        if (dbid != null) {
            query.addValue("dbid", dbid);
        }
        return jdbcTemplate.query(queryString, query, new SimDataRowMapper()).get(0);
    }

    public List<SimDataDTO> getAll(String market, LocalDate startDate, LocalDate endDate) throws Exception {
        String queryString = "select * from sim where ";
        if (market != null) {
            queryString += " market = :market";
        } else {
            queryString += " market like '%'";
        }
        if (startDate != null) {
            queryString += " and date >= :startdate";
        }
        if (endDate != null) {
            queryString += " and date <= :enddate";
        }
        MapSqlParameterSource query = new MapSqlParameterSource();
        if (market != null) {
            query.addValue("market", market);
        }
        if (startDate != null) {
            query.addValue("startdate", Date.from(startDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()));
        }
        if (endDate != null) {
            query.addValue("enddate", Date.from(endDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()));
        }
        return jdbcTemplate.query(queryString, query, new SimDataRowMapper());
    }
}
