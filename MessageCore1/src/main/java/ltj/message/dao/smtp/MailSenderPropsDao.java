package ltj.message.dao.smtp;

import java.util.List;

import ltj.message.vo.MailSenderVo;

public interface MailSenderPropsDao {
	public MailSenderVo getByPrimaryKey(int rowId);
	public List<MailSenderVo> getAll();
	public int update(MailSenderVo mailSenderVo);
	public int deleteByPrimaryKey(int rowId);
	public int insert(MailSenderVo mailSenderVo);
}
