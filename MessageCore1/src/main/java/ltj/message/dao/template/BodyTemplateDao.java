package ltj.message.dao.template;

import java.sql.Timestamp;
import java.util.List;

import ltj.vo.template.BodyTemplateVo;

public interface BodyTemplateDao {
	public BodyTemplateVo getByPrimaryKey(String templateId, String clientId, Timestamp startTime);
	public BodyTemplateVo getByBestMatch(String templateId, String clientId, Timestamp startTime);
	public BodyTemplateVo getByRowId(long rowId);
	public List<BodyTemplateVo> getByTemplateId(String templateId);
	public List<BodyTemplateVo> getByClientId(String clientId);
	public int update(BodyTemplateVo bodyTemplateVo);
	public int deleteByPrimaryKey(String templateId, String clientId, Timestamp startTime);
	public int deleteByTemplateId(String templateId);
	public int deleteByClientId(String clientId);
	public int insert(BodyTemplateVo bodyTemplateVo);
}
