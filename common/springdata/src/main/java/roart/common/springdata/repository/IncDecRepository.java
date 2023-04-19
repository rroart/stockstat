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
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import roart.common.model.IncDecItem;
import roart.common.springdata.model.IncDec;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import roart.common.springdata.rowmapper.IncDecRowMapper;

@Repository
public class IncDecRepository {

    @Autowired
    NamedParameterJdbcTemplate jdbcTemplate;
    
    public List<IncDecItem> getAll(String market, Date startDate, Date endDate, String parameters) throws Exception {
        String queryString = "select * from IncDec where market = :market";
        if (startDate != null) {
            queryString += " and date > :startdate";
        }
        if (endDate != null) {
            queryString += " and date <= :enddate";
        }
        MapSqlParameterSource query = new MapSqlParameterSource();
        query.addValue("market", market);
        //query.addValue("action", action);
        if (startDate != null) {
            query.addValue("startdate", startDate);
        }
        if (endDate != null) {
            query.addValue("enddate", endDate);
        }
        if (parameters != null) {
            query.addValue("parameters", parameters);
        }
        return jdbcTemplate.query(queryString, query, new IncDecRowMapper());
    }

    public void delete(String market, String action /*not used*/, String component, String subcomponent, Date startDate, Date endDate) throws Exception {
        String queryString = "delete from IncDec where market = :market";
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
    public List<IncDecItem> getAll(String mymarket) throws Exception {
        String sql = "select * from incdec where market = ?";
        MapSqlParameterSource query = new MapSqlParameterSource();
        query.addValue("market", mymarket);
        return jdbcTemplate.query(sql, query, new IncDecRowMapper());
    }

    public List<IncDecItem> getAll() throws Exception {
        String sql = "select * from incdec";
        MapSqlParameterSource query = new MapSqlParameterSource();
        return jdbcTemplate.query(sql, query, new IncDecRowMapper());
    }

}
