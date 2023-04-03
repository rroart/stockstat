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
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

import roart.common.model.ContItem;
import roart.common.springdata.rowmapper.ContRowMapper;

@Repository
public class ContRepository {

    @Autowired
    NamedParameterJdbcTemplate jdbcTemplate;
    
    public List<ContItem> getAll() throws Exception {
        String sql = "select * from cont";
        MapSqlParameterSource namedParameters = new MapSqlParameterSource();
        return jdbcTemplate.query(sql, namedParameters, new ContRowMapper());
    }
    
    public List<ContItem> getAll(String mymarket) throws Exception {
        String sql = "select * from cont where marketid = :market";
        MapSqlParameterSource namedParameters = new MapSqlParameterSource();
        namedParameters.addValue("market", mymarket);
        return jdbcTemplate.query(sql, namedParameters, new ContRowMapper());
    }
}
