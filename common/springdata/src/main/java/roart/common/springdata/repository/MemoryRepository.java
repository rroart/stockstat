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
import org.springframework.jdbc.core.namedparam.SqlParameterSource;

import roart.common.springdata.rowmapper.MemoryRowMapper;
import roart.common.model.MemoryItem;
import roart.common.springdata.model.Memory;

@Repository
public class MemoryRepository {

    @Autowired
    NamedParameterJdbcTemplate jdbcTemplate;
    
    public List<MemoryItem> getAll(String market, String action, String component, String subcomponent, String parameters, Date startDate, Date endDate) throws Exception {
        String queryString = "select * from Memory where market = :market";
        if (action != null) {
            queryString += " and action = :action";
        }
        if (component != null) {
            queryString += " and component = :component";
        }
        if (subcomponent != null) {
            queryString += " and subcomponent = :subcomponent";
        }
        if (parameters != null) {
            queryString += " and parameters = :parameters";
        }
        if (startDate != null) {
            queryString += " and date > :startdate";
        }
        if (endDate != null) {
            queryString += " and date <= :enddate";
        }
        MapSqlParameterSource namedParameters = new MapSqlParameterSource();
        namedParameters.addValue("market", market);
        if (action != null) {
            namedParameters.addValue("action", action);
        }
        if (component != null) {
            namedParameters.addValue("component", component);
        }
        if (subcomponent != null) {
            namedParameters.addValue("subcomponent", subcomponent);
        }
        if (parameters != null) {
            namedParameters.addValue("parameters", parameters);
        }
        if (startDate != null) {
            namedParameters.addValue("startdate", startDate);
        }
        if (endDate != null) {
            namedParameters.addValue("enddate", endDate);
        }
        return jdbcTemplate.query(queryString, namedParameters, new MemoryRowMapper());
    }

    public void delete(String market, String component, String subcomponent, Date startDate, Date endDate) throws Exception {
        String queryString = "delete from Memory where market = :market";
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
        MapSqlParameterSource query = new MapSqlParameterSource();
        query.addValue("market", market);
        //query.addValue("action", action);
        if (component != null) {
            query.addValue("component", component);
        }
        if (subcomponent != null) {
            query.addValue("subcomponent", subcomponent);
        }
        if (startDate != null) {
            query.addValue("startdate", startDate);
        }
        if (endDate != null) {
            query.addValue("enddate", endDate);
        }
        jdbcTemplate.update(queryString, query);

    }

    public List<MemoryItem> getAll(String mymarket) throws Exception {
        String sql = "select * from memory where marketid = :market";
        MapSqlParameterSource namedParameters = new MapSqlParameterSource();
        namedParameters.addValue("market", mymarket);
        return jdbcTemplate.query(sql, namedParameters, new MemoryRowMapper());
    }
}
