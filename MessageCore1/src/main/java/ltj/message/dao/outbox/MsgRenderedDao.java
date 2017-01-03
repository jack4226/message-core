package ltj.message.dao.outbox;

import java.util.List;

import ltj.vo.outbox.MsgRenderedVo;

public interface MsgRenderedDao {
	public MsgRenderedVo getByPrimaryKey(long renderId);
	public MsgRenderedVo getLastRecord();
	public MsgRenderedVo getRandomRecord();
	public List<MsgRenderedVo> getByMsgSourceId(String msgSourceId);
	public int update(MsgRenderedVo msgRenderedVo);
	public int deleteByPrimaryKey(long renderId);
	public int insert(MsgRenderedVo msgRenderedVo);
}
