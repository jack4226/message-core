package ltj.message.dao.emailaddr;

import java.util.List;

import ltj.message.vo.PagingAddrVo;
import ltj.message.vo.emailaddr.EmailAddrVo;

public interface EmailAddrDao {
	public EmailAddrVo getByAddrId(long addrId);
	public EmailAddrVo getByAddress(String address);
	public EmailAddrVo getRandomRecord();
	public EmailAddrVo getFromByMsgId(Long msgId);
	public EmailAddrVo getToByMsgId(Long msgId);
	public int getEmailAddressCount(PagingAddrVo vo);
	public List<EmailAddrVo> getEmailAddrsWithPaging(PagingAddrVo vo);
	public long getEmailAddrIdForPreview();
	/**
	 * find by email address. if it does not exist, add it to database.
	 */
	public EmailAddrVo findByAddress(String address);
	public int updateLastRcptTime(long addrId);
	public int updateLastSentTime(long addrId);
	public int updateAcceptHtml(long addrId, boolean acceptHtml);
	public int updateBounceCount(long emailAddrId, int count);
	public int updateBounceCount(EmailAddrVo emailAddrVo);
	public EmailAddrVo saveEmailAddress(String address);
	public int update(EmailAddrVo emailAddrVo);
	public int deleteByAddrId(long addrId);
	public int deleteByAddress(String address);
	public int insert(EmailAddrVo emailAddrVo);
}
