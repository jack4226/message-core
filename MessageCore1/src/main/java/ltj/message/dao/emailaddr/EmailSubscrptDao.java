package ltj.message.dao.emailaddr;

import java.util.List;

import ltj.message.vo.SearchVo;
import ltj.message.vo.emailaddr.EmailSubscrptVo;

public interface EmailSubscrptDao {
	public int subscribe(long addrId, String listId);
	public int subscribe(String addr, String listId);
	public int unsubscribe(long addrId, String listId);
	public int unsubscribe(String addr, String listId);
	public int optInRequest(String addr, String listId);
	public int optInRequest(long addrId, String listId);
	public int optInConfirm(long addrId, String listId);
	public int optInConfirm(String addr, String listId);
	public int getSubscriberCount(SearchVo vo);
	public List<EmailSubscrptVo> getSubscribersWithPaging(SearchVo vo);
	public List<EmailSubscrptVo> getSubscribers(String listId);
	public List<EmailSubscrptVo> getSubscribersWithCustomerRecord(String listId);
	public List<EmailSubscrptVo> getSubscribersWithoutCustomerRecord(String listId);
	public EmailSubscrptVo getByPrimaryKey(long addrId, String listId);
	public EmailSubscrptVo getByAddrAndListId(String addr, String listId);
	public List<EmailSubscrptVo> getByAddrId(long addrId);
	public List<EmailSubscrptVo> getByListId(String listId);
	public EmailSubscrptVo getRandomRecord();
	public int update(EmailSubscrptVo emailSubscrptVo);
	public int updateSentCount(long emailAddrId, String listId);
	public int updateOpenCount(long emailAddrId, String listId);
	public int updateClickCount(long emailAddrId, String listId);
	public int deleteByPrimaryKey(long addrid, String listId);
	public int deleteByAddrId(long addrId);
	public int deleteByListId(String listId);
	public int insert(EmailSubscrptVo emailSubscrptVo);
}
