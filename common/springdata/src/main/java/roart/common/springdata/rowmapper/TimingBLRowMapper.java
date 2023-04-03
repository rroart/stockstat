package roart.common.springdata.rowmapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.Date;

import org.springframework.jdbc.core.RowMapper;

import roart.common.model.TimingBLItem;
import roart.common.util.TimeUtil;

import org.springframework.jdbc.core.RowMapper;

public class TimingBLRowMapper implements RowMapper<TimingBLItem>{
    @Override
    public TimingBLItem mapRow(ResultSet rs, int rowNum) throws SQLException {
	TimingBLItem item = new TimingBLItem();
 item.setDbid(rs.getLong("dbid"));
item.setRecord(TimeUtil.convertDate(rs.getDate("record")));
    item.setId(rs.getString("id"));
    item.setCount(rs.getInt("count"));
    return item;
    }
}
