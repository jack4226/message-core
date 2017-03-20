package ltj.message.dao.inbox;

import java.util.List;

import ltj.message.vo.inbox.MsgUnsubCmntVo;

public interface MsgUnsubCmntDao {
	public MsgUnsubCmntVo getByPrimaryKey(int rowId);
	public List<MsgUnsubCmntVo> getFirst100();
	public List<MsgUnsubCmntVo> getByMsgId(long msgId);
	public List<MsgUnsubCmntVo> getByEmailAddrId(long emailAddrId);
	public List<MsgUnsubCmntVo> getByListId(String listId);
	public int update(MsgUnsubCmntVo msgUnsubCmntVo);
	public int deleteByPrimaryKey(int rowId);
	public int deleteByMsgId(long msgId);
	public int deleteByEmailAddrId(long emailAddrId);
	public int insert(MsgUnsubCmntVo msgUnsubCmntVo);
}
