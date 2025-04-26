package roart.common.springdata.repository;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

import roart.common.model.ContDTO;
import roart.common.springdata.rowmapper.ContRowMapper;

@Repository
public class ContRepository {

    @Autowired
    NamedParameterJdbcTemplate jdbcTemplate;
    
    public List<ContDTO> getAll() throws Exception {
        String sql = "select * from cont";
        MapSqlParameterSource namedParameters = new MapSqlParameterSource();
        return jdbcTemplate.query(sql, namedParameters, new ContRowMapper());
    }
    
    public List<ContDTO> getAll(String mymarket) throws Exception {
        String sql = "select * from cont where market = :market";
        MapSqlParameterSource namedParameters = new MapSqlParameterSource();
        namedParameters.addValue("market", mymarket);
        return jdbcTemplate.query(sql, namedParameters, new ContRowMapper());
    }
}
