package roart.common.springdata.repository;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import roart.common.model.TimingItem;
import roart.common.springdata.model.Timing;
import roart.common.springdata.rowmapper.TimingRowMapper;

@Repository
@Component
public class TimingRepository {

    @Autowired
    NamedParameterJdbcTemplate jdbcTemplate;
    
    public List<TimingItem> getAll(String market, String action, Date startDate, Date endDate) throws Exception {
        String queryString = "select * from Timing where market = :market and action = :action";
        if (startDate != null) {
            queryString += " and date > :startdate";
        }
        if (endDate != null) {
            queryString += " and date <= :enddate";
        }
        MapSqlParameterSource namedParameters = new MapSqlParameterSource();
        namedParameters.addValue("market", market);
        namedParameters.addValue("action", action);
        //query.addValue("action", action);
        if (startDate != null) {
            namedParameters.addValue("startdate", startDate);
        }
        if (endDate != null) {
            namedParameters.addValue("enddate", endDate);
        }
        return jdbcTemplate.query(queryString, namedParameters, new TimingRowMapper());
    }
    public void delete(String market, String action, String component, String subcomponent, Date startDate, Date endDate) throws Exception {
        String queryString = "delete from Timing where market = :market";
        if (action != null) {
            queryString += " and action = :action";
        }
        if (component != null) {
            queryString += " and component = :component";
        }
        if (subcomponent != null) {
            queryString += " and subcomponent = :subcomponent";
        }
        if (startDate != null) {
            queryString += " and date > :startdate";
        }
        if (endDate != null) {
            queryString += " and date <= :enddate";
        }
        MapSqlParameterSource namedParameters = new MapSqlParameterSource();
        namedParameters.addValue("market", market);
        //query.addValue("action", action);
        if (action != null) {
            namedParameters.addValue("action", action);
        }
        if (component != null) {
            namedParameters.addValue("component", component);
        }
        if (subcomponent != null) {
            namedParameters.addValue("subcomponent", subcomponent);
        }
        if (startDate != null) {
            namedParameters.addValue("startdate", startDate);
        }
        if (endDate != null) {
            namedParameters.addValue("enddate", endDate);
        }
        jdbcTemplate.update(queryString, namedParameters);
    }

    public List<TimingItem> getAll(String mymarket) throws Exception {
        String sql = "select * from timing where marketid = :market";
        MapSqlParameterSource namedParameters = new MapSqlParameterSource();
        namedParameters.addValue("market", mymarket);
        return jdbcTemplate.query(sql, namedParameters, new TimingRowMapper());
    }

    public List<TimingItem> getAll() throws Exception {
        String sql = "select * from timing";
        MapSqlParameterSource namedParameters = new MapSqlParameterSource();
        return jdbcTemplate.query(sql, namedParameters, new TimingRowMapper());
    }

}
