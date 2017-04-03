package ltj.message.dao.template;

import java.sql.Timestamp;
import java.util.List;

import ltj.vo.template.GlobalVariableVo;

public interface GlobalVariableDao {
	public GlobalVariableVo getByPrimaryKey(String variableName, Timestamp startTime);
	public GlobalVariableVo getByBestMatch(String variableName, Timestamp startTime);
	public GlobalVariableVo getByRowId(long rowId);
	public List<GlobalVariableVo> getByVariableName(String variableName);
	public List<GlobalVariableVo> getCurrent();
	public int update(GlobalVariableVo globalVariableVo);
	public int deleteByPrimaryKey(String variableName, Timestamp startTime);
	public int deleteByVariableName(String variableName);
	public int insert(GlobalVariableVo globalVariableVo);
}
