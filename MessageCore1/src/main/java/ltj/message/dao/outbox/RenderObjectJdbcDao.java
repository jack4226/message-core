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
import ltj.vo.outbox.RenderObjectVo;

@Component("renderObjectDao")
public class RenderObjectJdbcDao extends AbstractDao implements RenderObjectDao {
	
	@Override
	public RenderObjectVo getByPrimaryKey(long renderId, String variableName) {
		String sql = 
			"select * " +
			"from " +
				"RenderObject where RenderId=? and variableName=? ";
		
		Object[] parms = new Object[] {renderId, variableName};
		try {
			RenderObjectVo vo = getJdbcTemplate().queryForObject(sql, parms, 
					new BeanPropertyRowMapper<RenderObjectVo>(RenderObjectVo.class));
			return vo;
		}
		catch (EmptyResultDataAccessException e) {
			return null;
		}
	}
	
	@Override
	public List<RenderObjectVo> getByRenderId(long renderId) {
		String sql = 
			"select * " +
			" from " +
				" RenderObject where RenderId=? " +
			" order by variableName";
		Object[] parms = new Object[] {renderId};
		List<RenderObjectVo> list = getJdbcTemplate().query(sql, parms, 
				new BeanPropertyRowMapper<RenderObjectVo>(RenderObjectVo.class));
		return list;
	}
	
	@Override
	public int update(RenderObjectVo renderVariableVo) {
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(renderVariableVo);
		String sql = MetaDataUtil.buildUpdateStatement("RenderObject", renderVariableVo);

		int rowsUpadted = getNamedParameterJdbcTemplate().update(sql, namedParameters);
		return rowsUpadted;
	}
	
	@Override
	public int deleteByPrimaryKey(long msgId, String variableName) {
		String sql = 
			"delete from RenderObject where renderId=? and variableName=? ";
		
		List<Object> fields = new ArrayList<>();
		fields.add(msgId);
		fields.add(variableName);
		
		int rowsDeleted = getJdbcTemplate().update(sql, fields.toArray());
		return rowsDeleted;
	}
	
	@Override
	public int deleteByRenderId(long msgId) {
		String sql = 
			"delete from RenderObject where renderId=? ";
		
		List<Object> fields = new ArrayList<>();
		fields.add(msgId);
		
		int rowsDeleted = getJdbcTemplate().update(sql, fields.toArray());
		return rowsDeleted;
	}
	
	@Override
	public int insert(RenderObjectVo renderVariableVo) {
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(renderVariableVo);
		String sql = MetaDataUtil.buildInsertStatement("RenderObject", renderVariableVo);
		int rowsInserted = getNamedParameterJdbcTemplate().update(sql, namedParameters);
		return rowsInserted;
	}
}
