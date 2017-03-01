package ltj.message.dao.inbox;

import java.util.List;

import ltj.message.vo.inbox.MsgUnsubCommentsVo;

public interface MsgUnsubCommentsDao {
	public MsgUnsubCommentsVo getByPrimaryKey(int rowId);
	public List<MsgUnsubCommentsVo> getFirst100();
	public List<MsgUnsubCommentsVo> getByMsgId(long msgId);
	public List<MsgUnsubCommentsVo> getByEmailAddrId(long emailAddrId);
	public List<MsgUnsubCommentsVo> getByListId(String listId);
	public int update(MsgUnsubCommentsVo msgUnsubCommentsVo);
	public int deleteByPrimaryKey(int rowId);
	public int deleteByMsgId(long msgId);
	public int deleteByEmailAddrId(long emailAddrId);
	public int insert(MsgUnsubCommentsVo msgUnsubCommentsVo);
}
