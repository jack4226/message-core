package ltj.message.dao.inbox;

import java.util.List;

import ltj.vo.outbox.MsgStreamVo;


public interface MsgStreamDao {
	public MsgStreamVo getByPrimaryKey(long msgId);
	public List<MsgStreamVo> getByFromAddrId(long fromAddrId);
	public MsgStreamVo getLastRecord();
	public int update(MsgStreamVo msgStreamVo);
	public int deleteByPrimaryKey(long msgId);
	public int insert(MsgStreamVo msgStreamVo);
}
