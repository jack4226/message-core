package ltj.message.dao.emailaddr;

import java.util.List;

import ltj.message.vo.emailaddr.EmailUnsubCmntVo;

public interface EmailUnsubCmntDao {
	public EmailUnsubCmntVo getByPrimaryKey(int rowId);
	public List<EmailUnsubCmntVo> getFirst100();
	public List<EmailUnsubCmntVo> getByEmailAddrId(long emailAddrId);
	public List<EmailUnsubCmntVo> getByListId(String listId);
	public int update(EmailUnsubCmntVo emailUnsubCmntVo);
	public int deleteByPrimaryKey(int rowId);
	public int deleteByEmailAddrId(long emailAddrId);
	public int insert(EmailUnsubCmntVo emailUnsubCmntVo);
}
