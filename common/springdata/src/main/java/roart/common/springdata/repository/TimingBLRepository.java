package roart.common.springdata.repository;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import roart.common.model.TimingBLDTO;
import roart.common.springdata.rowmapper.TimingBLRowMapper;

@Repository
@Component
public class TimingBLRepository {

    @Autowired
    NamedParameterJdbcTemplate jdbcTemplate;

    public List<TimingBLDTO> getAll(String market, String action, Date startDate, Date endDate) throws Exception {
        String queryString = "select * from TimingBL";
        if (startDate != null) {
            queryString += " and record > :startdate";
        }
        if (endDate != null) {
            queryString += " and record <= :enddate";
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
        return jdbcTemplate.query(queryString, namedParameters, new TimingBLRowMapper());
    }

    public List<TimingBLDTO> getAll(String mymarket) throws Exception {
        String sql = "select * from timingbl where marketid = :market";
        MapSqlParameterSource namedParameters = new MapSqlParameterSource();
        namedParameters.addValue("market", mymarket);
        return jdbcTemplate.query(sql, namedParameters, new TimingBLRowMapper());
    }

    public void deleteByDbid(String id) {
        String sql = "delete from timingbl where dbid = :id";
        MapSqlParameterSource namedParameters = new MapSqlParameterSource();
        namedParameters.addValue("dbid", id);
        jdbcTemplate.update(sql, namedParameters);        
    }

    public void deleteById(String id) {
        String sql = "delete from timingbl where id = :id";
        MapSqlParameterSource namedParameters = new MapSqlParameterSource();
        namedParameters.addValue("id", id);
        jdbcTemplate.update(sql, namedParameters);        
    }
}
