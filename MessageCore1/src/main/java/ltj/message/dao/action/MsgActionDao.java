package ltj.message.dao.action;

import java.sql.Timestamp;
import java.util.List;

import ltj.message.vo.action.MsgActionVo;

public interface MsgActionDao {
	public List<MsgActionVo> getByRuleName(String ruleName);
	public boolean getHasActions(String ruleName);
	public List<MsgActionVo> getByBestMatch(String ruleName, Timestamp startTime, String clientId);
	public MsgActionVo getByPrimaryKey(int rowId);
	public List<MsgActionVo> getAll();
	public MsgActionVo getByUniqueKey(String ruleName, int actionSeq, Timestamp startTime,
			String clientId);
	public MsgActionVo getMostCurrent(String ruleName, int actionSeq, String clientId);
	public int update(MsgActionVo msgActionVo);
	public int deleteByRuleName(String ruleName);
	public int deleteByPrimaryKey(int rowId);
	public int deleteByUniqueKey(String ruleName, int actionSeq, Timestamp startTime,
			String clientId);
	public int insert(MsgActionVo msgActionVo);
}
