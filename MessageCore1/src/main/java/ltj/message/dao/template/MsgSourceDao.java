package ltj.message.dao.template;

import java.util.List;

import ltj.vo.template.MsgSourceVo;

public interface MsgSourceDao {
	public MsgSourceVo getByPrimaryKey(String msgSourceId);
	public List<MsgSourceVo> getByFromAddrId(long fromAddrId);
	public List<MsgSourceVo> getAll();
	public int update(MsgSourceVo msgSourceVo);
	public int deleteByPrimaryKey(String msgSourceId);
	public int deleteByFromAddrId(long fromAddrId);
	public int insert(MsgSourceVo msgSourceVo);
}
