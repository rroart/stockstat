package roart.common.springdata.repository;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import roart.common.model.StockDTO;
import roart.common.springdata.rowmapper.StockRowMapper;

@Repository
public class StockRepository {

    @Autowired
    NamedParameterJdbcTemplate jdbcTemplate;
    
    public List<StockDTO> getAll(String mymarket) throws Exception {
        String sql = "select * from stock where marketid = :market";
        MapSqlParameterSource namedParameters = new MapSqlParameterSource();
        namedParameters.addValue("market", mymarket);
        return jdbcTemplate.query(sql, namedParameters, new StockRowMapper());
    }
    
    public List<String> getMarkets() throws Exception {
        MapSqlParameterSource namedParameters = new MapSqlParameterSource();
        return jdbcTemplate.queryForList("select distinct (marketid) from Stock", namedParameters, String.class);
    }

    public List<Date> getDates(String mymarket) throws Exception {
        MapSqlParameterSource namedParameters = new MapSqlParameterSource();
        namedParameters.addValue("market", mymarket);
        return jdbcTemplate.queryForList("select distinct (date) from Stock where marketid = :market", namedParameters, Date.class);
    }

}
