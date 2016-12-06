package ltj.message.dao.outbox;

import java.util.List;

import ltj.vo.outbox.RenderAttachmentVo;

public interface RenderAttachmentDao {
	public RenderAttachmentVo getByPrimaryKey(long renderId, int attchmntSeq);
	public List<RenderAttachmentVo> getByRenderId(long renderId);
	public int update(RenderAttachmentVo renderAttachmentVo);
	public int deleteByPrimaryKey(long renderId, int attchmntSeq);
	public int deleteByRenderId(long renderId);
	public int insert(RenderAttachmentVo renderAttachmentVo);
}
