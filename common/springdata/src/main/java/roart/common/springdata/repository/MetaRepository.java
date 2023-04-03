package roart.common.springdata.repository;

import java.util.List;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import roart.common.springdata.model.Meta;

@Repository
public class MetaRepository {

    static NamedParameterJdbcTemplate jdbcTemplate;

    public Meta getById(String id) throws Exception {
        return null;
    }

    public List<Meta> getAll() throws Exception {
        return null;
    }

    public List<String> getMarkets() throws Exception {
        return null;
    }

    public List<Meta> getAll(String mymarket) throws Exception {
        return null;
    }
}
