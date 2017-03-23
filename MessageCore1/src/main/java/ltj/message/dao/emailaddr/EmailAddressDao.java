package ltj.message.dao.emailaddr;

import java.util.List;

import ltj.message.vo.PagingAddrVo;
import ltj.message.vo.emailaddr.EmailAddressVo;

public interface EmailAddressDao {
	public EmailAddressVo getByAddrId(long addrId);
	public EmailAddressVo getByAddress(String address);
	public EmailAddressVo getRandomRecord();
	public EmailAddressVo getFromByMsgId(Long msgId);
	public EmailAddressVo getToByMsgId(Long msgId);
	public int getEmailAddressCount(PagingAddrVo vo);
	public List<EmailAddressVo> getEmailAddrsWithPaging(PagingAddrVo vo);
	public long getEmailAddrIdForPreview();
	/**
	 * find by email address. if it does not exist, add it to database.
	 */
	public EmailAddressVo findByAddress(String address);
	public EmailAddressVo findByAddressSP(String address);
	public int updateLastRcptTime(long addrId);
	public int updateLastSentTime(long addrId);
	public int updateAcceptHtml(long addrId, boolean acceptHtml);
	public int updateBounceCount(long emailAddrId, int count);
	public int updateBounceCount(EmailAddressVo emailAddressVo);
	public EmailAddressVo saveEmailAddress(String address);
	public int update(EmailAddressVo emailAddressVo);
	public int deleteByAddrId(long addrId);
	public int deleteByAddress(String address);
	public int insert(EmailAddressVo emailAddressVo);
}
