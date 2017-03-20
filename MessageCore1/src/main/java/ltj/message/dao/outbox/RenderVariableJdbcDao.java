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
public class RenderVariableJdbcDao extends AbstractDao implements RenderVariableDao {
	
	@Override
	public RenderVariableVo getByPrimaryKey(long renderId, String variableName) {
		String sql = 
			"select * " +
			"from " +
				"render_variable where RenderId=? and variableName=? ";
		
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
	
	@Override
	public List<RenderVariableVo> getByRenderId(long renderId) {
		String sql = 
			"select * " +
			" from " +
				" render_variable where RenderId=? " +
			" order by variableName";
		Object[] parms = new Object[] {renderId};
		List<RenderVariableVo> list = getJdbcTemplate().query(sql, parms, 
				new BeanPropertyRowMapper<RenderVariableVo>(RenderVariableVo.class));
		return list;
	}
	
	@Override
	public int update(RenderVariableVo renderVariableVo) {
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(renderVariableVo);
		String sql = MetaDataUtil.buildUpdateStatement("render_variable", renderVariableVo);

		int rowsUpadted = getNamedParameterJdbcTemplate().update(sql, namedParameters);
		return rowsUpadted;
	}
	
	@Override
	public int deleteByPrimaryKey(long msgId, String variableName) {
		String sql = 
			"delete from render_variable where renderId=? and variableName=? ";
		
		List<Object> fields = new ArrayList<>();
		fields.add(msgId);
		fields.add(variableName);
		
		int rowsDeleted = getJdbcTemplate().update(sql, fields.toArray());
		return rowsDeleted;
	}
	
	@Override
	public int deleteByRenderId(long msgId) {
		String sql = 
			"delete from render_variable where renderId=? ";
		
		List<Object> fields = new ArrayList<>();
		fields.add(msgId);
		
		int rowsDeleted = getJdbcTemplate().update(sql, fields.toArray());
		return rowsDeleted;
	}
	
	@Override
	public int insert(RenderVariableVo renderVariableVo) {
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(renderVariableVo);
		String sql = MetaDataUtil.buildInsertStatement("render_variable", renderVariableVo);
		int rowsInserted = getNamedParameterJdbcTemplate().update(sql, namedParameters);
		return rowsInserted;
	}
	
}
