package ltj.message.dao.inbox;

import java.util.List;

import ltj.vo.outbox.MsgStreamVo;


public interface MsgStreamDao {
	public MsgStreamVo getByPrimaryKey(long msgId);
	public List<MsgStreamVo> getByFromAddrId(long fromAddrId);
	public List<MsgStreamVo> getByToAddrId(long toAddrId);
	public List<MsgStreamVo> getByFromAddress(String address);
	public List<MsgStreamVo> getByToAddress(String address);
	public MsgStreamVo getLastRecord();
	public MsgStreamVo getRandomRecord();
	public int update(MsgStreamVo msgStreamVo);
	public int deleteByPrimaryKey(long msgId);
	public int insert(MsgStreamVo msgStreamVo);
}
