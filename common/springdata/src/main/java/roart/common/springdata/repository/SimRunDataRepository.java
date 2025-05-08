package roart.common.springdata.repository;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import roart.common.model.SimRunDataDTO;
import roart.common.springdata.rowmapper.SimRunDataRowMapper;

@Repository
public class SimRunDataRepository {
    @Autowired
    NamedParameterJdbcTemplate jdbcTemplate;
    
    public List<SimRunDataDTO> getAll(String market, LocalDate startDate, LocalDate endDate) throws Exception {
        String queryString = "select * from sim2 where ";
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
        return jdbcTemplate.query(queryString, query, new SimRunDataRowMapper());
    }

}
