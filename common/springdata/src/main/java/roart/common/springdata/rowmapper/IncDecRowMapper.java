package roart.common.springdata.rowmapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.Date;

import org.springframework.jdbc.core.RowMapper;

import roart.common.model.IncDecItem;
import roart.common.util.TimeUtil;

import org.springframework.jdbc.core.RowMapper;

public class IncDecRowMapper implements RowMapper<IncDecItem>{
    @Override
    public IncDecItem mapRow(ResultSet rs, int rowNum) throws SQLException {
        IncDecItem item = new IncDecItem();
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
