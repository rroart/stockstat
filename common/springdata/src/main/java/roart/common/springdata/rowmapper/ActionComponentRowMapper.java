package roart.common.springdata.rowmapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.Date;

import org.springframework.jdbc.core.RowMapper;

import roart.common.model.ActionComponentItem;
import roart.common.util.TimeUtil;

import org.springframework.jdbc.core.RowMapper;

public class ActionComponentRowMapper implements RowMapper<ActionComponentItem>{
    @Override
    public ActionComponentItem mapRow(ResultSet rs, int rowNum) throws SQLException {

        ActionComponentItem item = new ActionComponentItem();
        item.setDbid((Long) rs.getObject("dbid"));
        item.setAction(rs.getString("action"));
        item.setComponent(rs.getString("component"));
        item.setSubcomponent(rs.getString("subcomponent"));
        item.setMarket(rs.getString("market"));
        item.setTime((Double) rs.getObject("time"));
        item.setHaverun((Boolean) rs.getObject("haverun"));
        item.setPriority((Integer) rs.getObject("priority"));
        //List<TimingItem> timings;
        item.setBuy((Boolean) rs.getObject("buy"));
        item.setParameters(rs.getString("parameters"));
        item.setRecord(TimeUtil.convertDate(rs.getDate("record")));
        //item.setResult(rs.getBlockingQueue("result"));
        item.setDate(TimeUtil.convertDate(rs.getDate("date")));
        return item;
    }
}
