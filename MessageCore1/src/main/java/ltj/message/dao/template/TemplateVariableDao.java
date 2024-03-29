package ltj.message.dao.template;

import java.sql.Timestamp;
import java.util.List;

import ltj.vo.template.TemplateVariableVo;

public interface TemplateVariableDao {
	public TemplateVariableVo getByPrimaryKey(String templateId, String clientId,
			String variableName, Timestamp startTime);
	
	public TemplateVariableVo getByBestMatch(String templateId, String clientId,
			String variableName, Timestamp startTime);

	public TemplateVariableVo getByRowId(long rowId);
	
	public List<TemplateVariableVo> getByVariableName(String variableName);

	public List<TemplateVariableVo> getByClientId(String clientId);

	public List<TemplateVariableVo> getCurrentByTemplateId(String templateId, String clientId);
	
	public List<TemplateVariableVo> getByTemplateId(String templateId);

	public int update(TemplateVariableVo templateVariableVo);

	public int deleteByPrimaryKey(String templateId, String clientId, String variableName,
			Timestamp startTime);

	public int deleteByVariableName(String variableName);

	public int deleteByClientId(String clientId);

	public int deleteByTemplateId(String templateId);

	public int insert(TemplateVariableVo templateVariableVo);
}
