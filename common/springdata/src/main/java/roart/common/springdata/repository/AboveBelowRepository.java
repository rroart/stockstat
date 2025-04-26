package roart.common.springdata.repository;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import roart.common.model.AboveBelowDTO;
import roart.common.springdata.rowmapper.AboveBelowRowMapper;

@Repository
public class AboveBelowRepository {

    @Autowired
    NamedParameterJdbcTemplate jdbcTemplate;
    
    public List<AboveBelowDTO> getAll(String market, Date startDate, Date endDate) throws Exception {
        String queryString = "select * from AboveBelow where ";
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
            query.addValue("startdate", startDate);
        }
        if (endDate != null) {
            query.addValue("enddate", endDate);
        }
        return jdbcTemplate.query(queryString, query, new AboveBelowRowMapper());
    }
    
    public void delete(String market, Date startDate, Date endDate) throws Exception {
        String queryString = "delete from AboveBelow where market = :market";
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
        jdbcTemplate.update(queryString, query);
    }

}
