package roart.common.springdata.rowmapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.Date;

import org.springframework.jdbc.core.RowMapper;

import roart.common.model.MemoryItem;
import roart.common.util.TimeUtil;

import org.springframework.jdbc.core.RowMapper;

public class MemoryRowMapper implements RowMapper<MemoryItem>{
    @Override
    public MemoryItem mapRow(ResultSet rs, int rowNum) throws SQLException {
	MemoryItem item = new MemoryItem();
	// \([A-Za-z]+\) \([A-Za-z]+\) item.set\2(rs.get\1("\,(downcase \2)")
	// item.set\([a-z]\) → item.set\,(upcase \1))
	item.setAction(rs.getString("action"));
     item.setRecord(TimeUtil.convertDate(rs.getDate("record")));
     item.setDate(TimeUtil.convertDate(rs.getDate("date")));
     item.setUsedsec(rs.getInt("usedsec"));
     item.setMarket(rs.getString("market"));
     item.setTestaccuracy(rs.getDouble("testaccuracy"));
     item.setTestloss(rs.getDouble("testloss"));
     item.setConfidence(rs.getDouble("confidence"));
     item.setLearnConfidence(rs.getDouble("learnconfidence"));
     item.setCategory(rs.getString("category"));
     item.setType(rs.getString("type"));
     item.setComponent(rs.getString("component"));
     item.setSubcomponent(rs.getString("subcomponent"));
     item.setLocalcomponent(rs.getString("localcomponent"));
     item.setDescription(rs.getString("description"));
     item.setInfo(rs.getString("info"));
     item.setFuturedays(rs.getInt("futuredays"));
     item.setFuturedate(TimeUtil.convertDate(rs.getDate("futuredate")));
     item.setPositives(rs.getLong("positives"));
     item.setSize(rs.getLong("size"));
     item.setAbovepositives(rs.getLong("abovepositives"));
     item.setAbovesize(rs.getLong("abovesize"));
     item.setBelowpositives(rs.getLong("belowpositives"));
     item.setBelowsize(rs.getLong("belowsize"));
     item.setParameters(rs.getString("parameters"));
     item.setTp(rs.getLong("tp"));
     item.setTpSize(rs.getLong("tpsize"));
     item.setTpConf(rs.getDouble("tpconf"));
     item.setTpProb(rs.getDouble("tpprob"));
     item.setTpProbConf(rs.getDouble("tpprobconf"));
     item.setTn(rs.getLong("tn"));
     item.setTnSize(rs.getLong("tnsize"));
     item.setTnConf(rs.getDouble("tnconf"));
     item.setTnProb(rs.getDouble("tnprob"));
     item.setTnProbConf(rs.getDouble("tnprobconf"));
     item.setFp(rs.getLong("fp"));
     item.setFpSize(rs.getLong("fpsize"));
     item.setFpConf(rs.getDouble("fpconf"));
     item.setFpProb(rs.getDouble("fpprob"));
     item.setFpProbConf(rs.getDouble("fpprobconf"));
     item.setFn(rs.getLong("fn"));
     item.setFnSize(rs.getLong("fnsize"));
     item.setFnConf(rs.getDouble("fnconf"));
     item.setFnProb(rs.getDouble("fnprob"));
     item.setFnProbConf(rs.getDouble("fnprobconf"));
     item.setPosition(rs.getInt("position"));
     return item;    }
}
