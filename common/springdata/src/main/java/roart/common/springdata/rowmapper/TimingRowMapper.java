package roart.common.springdata.rowmapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import roart.common.model.TimingDTO;
import roart.common.util.TimeUtil;

public class TimingRowMapper implements RowMapper<TimingDTO>{
    @Override
    public TimingDTO mapRow(ResultSet rs, int rowNum) throws SQLException {
        TimingDTO item = new TimingDTO();
        item.setRecord(TimeUtil.convertDate(rs.getDate("record")));
        item.setDate(TimeUtil.convertDate(rs.getDate("date")));
        item.setMarket(rs.getString("market"));
        item.setMlmarket(rs.getString("mlmarket"));
        item.setAction(rs.getString("action"));
        item.setEvolve((Boolean) rs.getObject("evolve"));
        item.setComponent(rs.getString("component"));
        item.setSubcomponent(rs.getString("subcomponent"));
        item.setParameters(rs.getString("parameters"));
        item.setMytime((Double) rs.getObject("time"));
        item.setScore((Double) rs.getObject("score"));
        item.setBuy((Boolean) rs.getObject("buy"));
        item.setDescription(rs.getString("description"));
        return item;

    }
}
