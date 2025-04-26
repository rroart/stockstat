package roart.common.springdata.rowmapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import roart.common.model.MemoryDTO;
import roart.common.util.TimeUtil;

public class MemoryRowMapper implements RowMapper<MemoryDTO>{
    @Override
    public MemoryDTO mapRow(ResultSet rs, int rowNum) throws SQLException {
        MemoryDTO item = new MemoryDTO();
        // \([A-Za-z]+\) \([A-Za-z]+\) item.set\2(rs.get\1("\,(downcase \2)")
        // item.set\([a-z]\) → item.set\,(upcase \1))
        item.setAction(rs.getString("action"));
        item.setRecord(TimeUtil.convertDate(rs.getDate("record")));
        item.setDate(TimeUtil.convertDate(rs.getDate("date")));
        item.setUsedsec((Integer) rs.getObject("usedsec"));
        item.setMarket(rs.getString("market"));
        item.setTestaccuracy((Double) rs.getObject("testaccuracy"));
        item.setTestloss((Double) rs.getObject("testloss"));
        item.setConfidence((Double) rs.getObject("confidence"));
        item.setLearnConfidence((Double) rs.getObject("learnconfidence"));
        item.setCategory(rs.getString("category"));
        item.setType(rs.getString("type"));
        item.setComponent(rs.getString("component"));
        item.setSubcomponent(rs.getString("subcomponent"));
        item.setLocalcomponent(rs.getString("localcomponent"));
        item.setDescription(rs.getString("description"));
        item.setInfo(rs.getString("info"));
        item.setFuturedays((Integer) rs.getObject("futuredays"));
        item.setFuturedate(TimeUtil.convertDate(rs.getDate("futuredate")));
        item.setPositives((Long) rs.getObject("positives"));
        item.setSize((Long) rs.getObject("size"));
        item.setAbovepositives((Long) rs.getObject("abovepositives"));
        item.setAbovesize((Long) rs.getObject("abovesize"));
        item.setBelowpositives((Long) rs.getObject("belowpositives"));
        item.setBelowsize((Long) rs.getObject("belowsize"));
        item.setParameters(rs.getString("parameters"));
        item.setTp((Long) rs.getObject("tp"));
        item.setTpSize((Long) rs.getObject("tpsize"));
        item.setTpConf((Double) rs.getObject("tpconf"));
        item.setTpProb((Double) rs.getObject("tpprob"));
        item.setTpProbConf((Double) rs.getObject("tpprobconf"));
        item.setTn((Long) rs.getObject("tn"));
        item.setTnSize((Long) rs.getObject("tnsize"));
        item.setTnConf((Double) rs.getObject("tnconf"));
        item.setTnProb((Double) rs.getObject("tnprob"));
        item.setTnProbConf((Double) rs.getObject("tnprobconf"));
        item.setFp((Long) rs.getObject("fp"));
        item.setFpSize((Long) rs.getObject("fpsize"));
        item.setFpConf((Double) rs.getObject("fpconf"));
        item.setFpProb((Double) rs.getObject("fpprob"));
        item.setFpProbConf((Double) rs.getObject("fpprobconf"));
        item.setFn((Long) rs.getObject("fn"));
        item.setFnSize((Long) rs.getObject("fnsize"));
        item.setFnConf((Double) rs.getObject("fnconf"));
        item.setFnProb((Double) rs.getObject("fnprob"));
        item.setFnProbConf((Double) rs.getObject("fnprobconf"));
        item.setPosition((Integer) rs.getObject("position"));
        return item;    }
}
