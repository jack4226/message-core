package ltj.message.dao.inbox;

import java.util.List;

import ltj.message.vo.SearchVo;
import ltj.message.vo.inbox.MsgClickCountVo;

public interface MsgClickCountDao {
	public MsgClickCountVo getRandomRecord();
	public int getMsgCountForWeb();
	public MsgClickCountVo getByPrimaryKey(long msgId);
	public int getBroadcastsCount(SearchVo vo);
	public List<MsgClickCountVo> getBroadcastsWithPaging(SearchVo vo);
	public int update(MsgClickCountVo msgClickCountVo);
	public int deleteByPrimaryKey(long msgId);
	public int insert(MsgClickCountVo msgClickCountVo);
	public int updateSentCount(long msgId, int count);
	public int updateOpenCount(long msgId, int count);
	public int updateClickCount(long msgId, int count);
	public int updateOpenCount(long msgId);
	public int updateClickCount(long msgId);
	public int updateReferalCount(long msgId, int count);
	public int updateReferalCount(long msgId);
	public int updateStartTime(long msgId);
	public int updateUnsubscribeCount(long msgId, int count);
	public int updateComplaintCount(long msgId, int count);
}
