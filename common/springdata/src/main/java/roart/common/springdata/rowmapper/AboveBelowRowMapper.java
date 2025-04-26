package roart.common.springdata.rowmapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import roart.common.model.AboveBelowDTO;
import roart.common.util.TimeUtil;

public class AboveBelowRowMapper implements RowMapper<AboveBelowDTO>{
    @Override
    public AboveBelowDTO mapRow(ResultSet rs, int rowNum) throws SQLException {
        AboveBelowDTO item = new AboveBelowDTO();
        item.setRecord(TimeUtil.convertDate(rs.getDate("record")));
        item.setDate(rs.getDate("date"));
        item.setMarket(rs.getString("market"));
        item.setComponents(rs.getString("components"));
        item.setSubcomponents(rs.getString("subcomponents"));
        item.setScore((Double) rs.getObject("score"));
        return item;
    }
}
