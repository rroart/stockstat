package roart.common.springdata.rowmapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import roart.common.model.IncDecDTO;
import roart.common.util.TimeUtil;

public class IncDecRowMapper implements RowMapper<IncDecDTO>{
    @Override
    public IncDecDTO mapRow(ResultSet rs, int rowNum) throws SQLException {
        IncDecDTO item = new IncDecDTO();
        item.setRecord(TimeUtil.convertDate(rs.getDate("record")));
        item.setDate(TimeUtil.convertDate(rs.getDate("date")));
        item.setMarket(rs.getString("market"));
        item.setIncrease((Boolean) rs.getObject("increase"));
        item.setId(rs.getString("id"));
        item.setName(rs.getString("name"));
        item.setDescription(rs.getString("description"));
        item.setScore((Double) rs.getObject("score"));
        item.setParameters(rs.getString("parameters"));
        item.setComponent(rs.getString("component"));
        item.setSubcomponent(rs.getString("subcomponent"));
        item.setLocalcomponent(rs.getString("localcomponent"));
        return item;
    }


}
