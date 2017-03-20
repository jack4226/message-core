package ltj.message.dao.inbox;

import java.util.List;

import ltj.message.vo.inbox.MsgAddressVo;

public interface MsgAddressDao {
	public MsgAddressVo getByPrimaryKey(long msgId, String addrType, int addrSeq);
	public List<MsgAddressVo> getByMsgId(long msgId);
	public List<MsgAddressVo> getByMsgIdAndType(long msgId, String addrType);
	public List<MsgAddressVo> getRandomRecord();
	public int update(MsgAddressVo msgAddressVo);
	public int deleteByPrimaryKey(long msgId, String addrType, int addrSeq);
	public int deleteByMsgId(long msgId);
	public int insert(MsgAddressVo msgAddressVo);
}
