package roart.common.springdata.rowmapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.Date;

import org.springframework.jdbc.core.RowMapper;

import roart.common.model.RelationItem;
import roart.common.util.TimeUtil;

import org.springframework.jdbc.core.RowMapper;

public class RelationRowMapper implements RowMapper<RelationItem>{
    @Override
    public RelationItem mapRow(ResultSet rs, int rowNum) throws SQLException {
        RelationItem item = new RelationItem();
        item.setRecord(TimeUtil.convertDate(rs.getDate("record")));
        item.setMarket(rs.getString("market"));
        item.setId(rs.getString("id"));
        item.setAltId(rs.getString("altid"));
        item.setType(rs.getString("type"));
        item.setOtherMarket(rs.getString("othermarket"));
        item.setOtherId(rs.getString("otherid"));
        item.setOtherAltId(rs.getString("otheraltid"));
        item.setValue((Double) rs.getObject("value"));
        return item;
    }
}
