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

import roart.common.model.ActionComponentItem;
import roart.common.springdata.rowmapper.ActionComponentRowMapper;

@Repository
public class ActionComponentRepository {

    @Autowired
    NamedParameterJdbcTemplate jdbcTemplate;

    public List<ActionComponentItem> getAll(String market, String action, String component, String subcomponent, String parameters/*, Date startDate, Date endDate*/) throws Exception {
        String queryString = "select * from ActionComponent where market = :market and action = :action and component = :component";
        if (subcomponent != null) {
            queryString += " and subcomponent = :subcomponent";
        }
        if (parameters != null) {
            queryString += " and parameters = :parameters";
        }
        /*
        if (startDate != null) {
            queryString += " and date > :startdate";
        }
        if (endDate != null) {
            queryString += " and date <= :enddate";
        }
        */
        MapSqlParameterSource namedParameters = new MapSqlParameterSource();
        namedParameters.addValue("market", market);
        namedParameters.addValue("action", action);
        namedParameters.addValue("component", component);
        if (subcomponent != null) {
            namedParameters.addValue("subcomponent", subcomponent);
        }
        if (parameters != null) {
            namedParameters.addValue("parameters", parameters);
        }
        /*
        if (startDate != null) {
            query.addValue("startdate", startDate);
        }
        if (endDate != null) {
            query.addValue("enddate", endDate);
        }
        */
        return jdbcTemplate.query(queryString, namedParameters, new ActionComponentRowMapper());
    }
}
