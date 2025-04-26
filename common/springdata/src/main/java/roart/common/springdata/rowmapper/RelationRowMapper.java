package roart.common.springdata.rowmapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import roart.common.model.RelationDTO;
import roart.common.util.TimeUtil;

public class RelationRowMapper implements RowMapper<RelationDTO>{
    @Override
    public RelationDTO mapRow(ResultSet rs, int rowNum) throws SQLException {
        RelationDTO item = new RelationDTO();
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
