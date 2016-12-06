package ltj.message.dao.inbox;

import java.util.List;

import ltj.message.vo.inbox.AttachmentsVo;

public interface AttachmentsDao {
	public AttachmentsVo getByPrimaryKey(long msgId, int attchmntDepth, int attchmntSeq);
	public List<AttachmentsVo> getByMsgId(long msgId);
	public int update(AttachmentsVo attachmentsVo);
	public int deleteByPrimaryKey(long msgId, int attchmntDepth, int attchmntSeq);
	public int deleteByMsgId(long msgId);
	public int insert(AttachmentsVo attachmentsVo);
}
