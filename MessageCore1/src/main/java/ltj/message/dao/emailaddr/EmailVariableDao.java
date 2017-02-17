package ltj.message.dao.emailaddr;

import java.util.List;

import ltj.message.constant.EmailVariableType;
import ltj.message.vo.emailaddr.EmailVariableVo;

public interface EmailVariableDao {
	public static final String SYSTEM_VARIABLE = EmailVariableType.System.value();
	public static final String CUSTOMER_VARIABLE = EmailVariableType.Custom.value();
	
	public EmailVariableVo getByName(String variableName);
	public List<EmailVariableVo> getAll();
	public List<EmailVariableVo> getAllForTrial();
	public List<EmailVariableVo> getAllCustomVariables();
	public List<EmailVariableVo> getAllBuiltinVariables();
	public String getByQuery(String query, long addrId);
	public int update(EmailVariableVo emailVariableVo);
	public int deleteByName(String variableName);
	public int insert(EmailVariableVo emailVariableVo);
}
