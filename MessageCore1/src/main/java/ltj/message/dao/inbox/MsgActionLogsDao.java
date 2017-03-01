package ltj.message.dao.inbox;

import java.util.List;

import ltj.message.vo.inbox.MsgActionLogsVo;

public interface MsgActionLogsDao {
	public MsgActionLogsVo getByPrimaryKey(long msgId, Long msgRefId);
	public List<MsgActionLogsVo> getByMsgId(long msgId);
	public List<MsgActionLogsVo> getByLeadMsgId(long leadMsgId);
	public List<MsgActionLogsVo> getRandomRecord();
	public int update(MsgActionLogsVo msgActionLogsVo);
	public int deleteByPrimaryKey(long msgId, Long msgRefId);
	public int deleteByMsgId(long msgId);
	public int deleteByLeadMsgId(long leadMsgId);
	public int insert(MsgActionLogsVo msgActionLogsVo);
}
