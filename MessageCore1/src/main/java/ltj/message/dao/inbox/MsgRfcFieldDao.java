package ltj.message.dao.inbox;

import java.util.List;

import ltj.message.vo.inbox.MsgRfcFieldVo;

public interface MsgRfcFieldDao {
	public MsgRfcFieldVo getByPrimaryKey(long msgId, String rfcType);
	public List<MsgRfcFieldVo> getByMsgId(long msgId);
	public List<MsgRfcFieldVo> getRandomRecord();
	public int update(MsgRfcFieldVo msgRfcFieldVo);
	public int deleteByPrimaryKey(long msgId, String rfcType);
	public int deleteByMsgId(long msgId);
	public int insert(MsgRfcFieldVo msgRfcFieldVo);
}
