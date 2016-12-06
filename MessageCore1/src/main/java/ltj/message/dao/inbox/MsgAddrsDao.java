package ltj.message.dao.inbox;

import java.util.List;

import ltj.message.vo.inbox.MsgAddrsVo;

public interface MsgAddrsDao {
	public MsgAddrsVo getByPrimaryKey(long msgId, String addrType, int addrSeq);
	public List<MsgAddrsVo> getByMsgId(long msgId);
	public List<MsgAddrsVo> getByMsgIdAndType(long msgId, String addrType);
	public int update(MsgAddrsVo msgAddrsVo);
	public int deleteByPrimaryKey(long msgId, String addrType, int addrSeq);
	public int deleteByMsgId(long msgId);
	public int insert(MsgAddrsVo msgAddrsVo);
}
