package ltj.message.dao.inbox;

import java.util.List;

import ltj.message.vo.inbox.RfcFieldsVo;

public interface RfcFieldsDao {
	public RfcFieldsVo getByPrimaryKey(long msgId, String rfcType);
	public List<RfcFieldsVo> getByMsgId(long msgId);
	public List<RfcFieldsVo> getRandomRecord();
	public int update(RfcFieldsVo rfcFieldsVo);
	public int deleteByPrimaryKey(long msgId, String rfcType);
	public int deleteByMsgId(long msgId);
	public int insert(RfcFieldsVo rfcFieldsVo);
}
