package roart.common.springdata.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class RelationRepository {

    @Autowired
    NamedParameterJdbcTemplate jdbcTemplate;
    
}
