package roart.common.springdata.repository;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import roart.common.model.MLMetricsDTO;
import roart.common.springdata.rowmapper.MLMetricsRowMapper;

@Repository
@Component
public class MLMetricsRepository {

    @Autowired
    NamedParameterJdbcTemplate jdbcTemplate;
    
    public List<MLMetricsDTO> getAll(String market, Date startDate, Date endDate) throws Exception {
        String queryString = "select * from MLMetrics where market = :market";
        if (startDate != null) {
            queryString += " and date > :startdate";
        }
        if (endDate != null) {
            queryString += " and date <= :enddate";
        }
        MapSqlParameterSource namedParameters = new MapSqlParameterSource();
        namedParameters.addValue("market", market);
        //query.addValue("action", action);
        if (startDate != null) {
            namedParameters.addValue("startdate", startDate);
        }
        if (endDate != null) {
            namedParameters.addValue("enddate", endDate);
        }
        return jdbcTemplate.query(queryString, namedParameters, new MLMetricsRowMapper());
    }

    public List<MLMetricsDTO> getAll(String mymarket) throws Exception {
        String sql = "select * from mlmetrics where market = :market";
        MapSqlParameterSource namedParameters = new MapSqlParameterSource();
        namedParameters.addValue("market", mymarket);
        return jdbcTemplate.query(sql, namedParameters, new MLMetricsRowMapper());
    }

    public List<MLMetricsDTO> getAll() throws Exception {
        String sql = "select * from mlmetrics";
        MapSqlParameterSource namedParameters = new MapSqlParameterSource();
        return jdbcTemplate.query(sql, namedParameters, new MLMetricsRowMapper());
    }
}
