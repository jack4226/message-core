package ltj.message.dao.inbox;

import java.util.Date;
import java.util.List;

import ltj.message.vo.inbox.MsgInboxVo;
import ltj.message.vo.inbox.MsgInboxWebVo;
import ltj.message.vo.inbox.SearchFieldsVo;

public interface MsgInboxDao {
	public MsgInboxVo getByPrimaryKey(long msgId);
	public MsgInboxVo getLastRecord();
	public MsgInboxVo getRandomRecord();
	public MsgInboxVo getLastReceivedRecord();
	public MsgInboxVo getLastSentRecord();
	public List<MsgInboxWebVo> getByLeadMsgId(long leadMsgId);
	public List<MsgInboxWebVo> getByMsgRefId(long msgRefId);
	public List<MsgInboxVo> getByFromAddrId(long addrId);
	public List<MsgInboxVo> getByToAddrId(long addrId);
	public List<MsgInboxVo> getByFromAddress(String address);
	public List<MsgInboxVo> getByToAddress(String address);
	public List<MsgInboxVo> getRecent(int days);
	public List<MsgInboxVo> getRecent(Date date);
	public int getInboxUnreadCount();
	public int getSentUnreadCount();
	public int getAllUnreadCount();
	public int resetInboxUnreadCount();
	public int resetSentUnreadCount();
	public int getRowCountForWeb(SearchFieldsVo vo);
	public List<MsgInboxWebVo> getListForWeb(SearchFieldsVo vo);
	public int update(MsgInboxWebVo msgInboxVo);
	public int updateCounts(MsgInboxWebVo msgInboxVo);
	public int updateCounts(MsgInboxVo msgInboxVo);
	public int updateStatusId(MsgInboxVo msgInboxVo);
	public int updateStatusIdByLeadMsgId(MsgInboxVo msgInboxVo);
	public int update(MsgInboxVo msgInboxVo);
	public int deleteByPrimaryKey(long msgId);
	public int insert(MsgInboxVo msgInboxVo);
}
