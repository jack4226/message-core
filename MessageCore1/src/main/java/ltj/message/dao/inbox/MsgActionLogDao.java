package ltj.message.dao.inbox;

import java.util.List;

import ltj.message.vo.inbox.MsgActionLogVo;

public interface MsgActionLogDao {
	public MsgActionLogVo getByPrimaryKey(long msgId, Long msgRefId);
	public List<MsgActionLogVo> getByMsgId(long msgId);
	public List<MsgActionLogVo> getByLeadMsgId(long leadMsgId);
	public List<MsgActionLogVo> getRandomRecord();
	public int update(MsgActionLogVo msgActionLogVo);
	public int deleteByPrimaryKey(long msgId, Long msgRefId);
	public int deleteByMsgId(long msgId);
	public int deleteByLeadMsgId(long leadMsgId);
	public int insert(MsgActionLogVo msgActionLogVo);
}
