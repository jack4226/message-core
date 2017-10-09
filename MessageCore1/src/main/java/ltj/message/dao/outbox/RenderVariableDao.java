package ltj.message.dao.outbox;

import java.util.ArrayList;
import java.util.List;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Component;

import ltj.message.dao.abstrct.AbstractDao;
import ltj.message.dao.abstrct.MetaDataUtil;
import ltj.vo.outbox.RenderVariableVo;

@Component("renderVariableDao")
public class RenderVariableDao extends AbstractDao {
	
	public RenderVariableVo getByPrimaryKey(long renderId, String variableName) {
		String sql = 
			"select * " +
			"from " +
				"render_variable where render_id=? and variable_name=? ";
		
		Object[] parms = new Object[] {renderId, variableName};
		try {
			RenderVariableVo vo = getJdbcTemplate().queryForObject(sql, parms, 
					new BeanPropertyRowMapper<RenderVariableVo>(RenderVariableVo.class));
			return vo;
		}
		catch (EmptyResultDataAccessException e) {
			return null;
		}
	}
	
	public List<RenderVariableVo> getByRenderId(long renderId) {
		String sql = 
			"select * " +
			" from " +
				" render_variable where render_id=? " +
			" order by variable_name";
		Object[] parms = new Object[] {renderId};
		List<RenderVariableVo> list = getJdbcTemplate().query(sql, parms, 
				new BeanPropertyRowMapper<RenderVariableVo>(RenderVariableVo.class));
		return list;
	}
	
	public int update(RenderVariableVo renderVariableVo) {
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(renderVariableVo);
		String sql = MetaDataUtil.buildUpdateStatement("render_variable", renderVariableVo);

		int rowsUpadted = getNamedParameterJdbcTemplate().update(sql, namedParameters);
		return rowsUpadted;
	}
	
	public int deleteByPrimaryKey(long msgId, String variableName) {
		String sql = 
			"delete from render_variable where render_id=? and variable_name=? ";
		
		List<Object> fields = new ArrayList<>();
		fields.add(msgId);
		fields.add(variableName);
		
		int rowsDeleted = getJdbcTemplate().update(sql, fields.toArray());
		return rowsDeleted;
	}
	
	public int deleteByRenderId(long msgId) {
		String sql = 
			"delete from render_variable where render_id=? ";
		
		List<Object> fields = new ArrayList<>();
		fields.add(msgId);
		
		int rowsDeleted = getJdbcTemplate().update(sql, fields.toArray());
		return rowsDeleted;
	}
	
	public int insert(RenderVariableVo renderVariableVo) {
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(renderVariableVo);
		String sql = MetaDataUtil.buildInsertStatement("render_variable", renderVariableVo);
		int rowsInserted = getNamedParameterJdbcTemplate().update(sql, namedParameters);
		return rowsInserted;
	}
	
}
