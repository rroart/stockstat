package roart.common.springdata.repository;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import roart.common.model.SimDataItem;
import roart.common.springdata.model.SimData;
import roart.common.springdata.rowmapper.SimDataRowMapper;

@Repository
public class SimDataRepository {

    @Autowired
    NamedParameterJdbcTemplate jdbcTemplate;
    
    public List<SimDataItem> getAll(String market, LocalDate startDate, LocalDate endDate) throws Exception {
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
