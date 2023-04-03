package roart.common.springdata.rowmapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.Date;

import org.springframework.jdbc.core.RowMapper;

import roart.common.model.AboveBelowItem;
import roart.common.util.TimeUtil;

import org.springframework.jdbc.core.RowMapper;

public class AboveBelowRowMapper implements RowMapper<AboveBelowItem>{
    @Override
    public AboveBelowItem mapRow(ResultSet rs, int rowNum) throws SQLException {
	AboveBelowItem item = new AboveBelowItem();
item.setRecord(TimeUtil.convertDate(rs.getDate("record")));
    item.setDate(rs.getDate("date"));
    item.setMarket(rs.getString("market"));
item.setComponents(rs.getString("components"));
    item.setSubcomponents(rs.getString("subcomponents"));
item.setScore(rs.getDouble("score"));
return item;
    }
}
