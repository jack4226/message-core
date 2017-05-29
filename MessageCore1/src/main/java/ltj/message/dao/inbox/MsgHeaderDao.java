package ltj.message.dao.inbox;

import java.util.List;

import ltj.message.vo.inbox.MsgHeaderVo;

public interface MsgHeaderDao {
	public MsgHeaderVo getByPrimaryKey(long msgId, int headerSeq);
	public List<MsgHeaderVo> getByMsgId(long msgId);
	public List<MsgHeaderVo> getRandomRecord();
	public int update(MsgHeaderVo msgHeaderVo);
	public int deleteByPrimaryKey(long msgId, int headerSeq);
	public int deleteByMsgId(long msgId);
	public int insert(MsgHeaderVo msgHeaderVo);
}
