package ltj.message.dao.inbox;

import java.util.List;

import ltj.message.vo.inbox.MsgAttachmentVo;

public interface MsgAttachmentDao {
	public MsgAttachmentVo getByPrimaryKey(long msgId, int attchmntDepth, int attchmntSeq);
	public List<MsgAttachmentVo> getByMsgId(long msgId);
	public List<MsgAttachmentVo> getRandomRecord();
	public int update(MsgAttachmentVo msgAttachmentVo);
	public int deleteByPrimaryKey(long msgId, int attchmntDepth, int attchmntSeq);
	public int deleteByMsgId(long msgId);
	public int insert(MsgAttachmentVo msgAttachmentVo);
}
