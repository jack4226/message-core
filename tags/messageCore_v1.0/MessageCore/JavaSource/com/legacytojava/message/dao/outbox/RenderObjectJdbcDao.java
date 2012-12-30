package com.legacytojava.message.dao.outbox;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import com.legacytojava.message.vo.outbox.RenderObjectVo;

public class RenderObjectJdbcDao implements RenderObjectDao {
	
	private DataSource dataSource;
	private JdbcTemplate jdbcTemplate;
	
	private static final class RenderObjectMapper implements RowMapper {
		
		public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
			RenderObjectVo renderVariableVo = new RenderObjectVo();

			renderVariableVo.setRenderId(rs.getLong("RenderId"));
			renderVariableVo.setVariableName(rs.getString("VariableName"));
			renderVariableVo.setVariableFormat(rs.getString("VariableFormat"));
			renderVariableVo.setVariableType(rs.getString("VariableType"));
			renderVariableVo.setVariableValue(rs.getBytes("VariableValue"));
			
			return renderVariableVo;
		}
	}

	public RenderObjectVo getByPrimaryKey(long renderId, String variableName) {
		String sql = 
			"select * " +
			"from " +
				"RenderObject where RenderId=? and variableName=? ";
		
		Object[] parms = new Object[] {renderId, variableName};
		List<?> list = (List<?>)jdbcTemplate.query(sql, parms, new RenderObjectMapper());
		if (list.size()>0)
			return (RenderObjectVo)list.get(0);
		else
			return null;
	}
	
	@SuppressWarnings("unchecked")
	public List<RenderObjectVo> getByRenderId(long renderId) {
		String sql = 
			"select * " +
			" from " +
				" RenderObject where RenderId=? " +
			" order by variableName";
		Object[] parms = new Object[] {renderId};
		List<RenderObjectVo> list = (List<RenderObjectVo>)jdbcTemplate.query(sql, parms, new RenderObjectMapper());
		return list;
	}
	
	public int update(RenderObjectVo renderVariableVo) {
		
		ArrayList<Object> fields = new ArrayList<Object>();
		fields.add(renderVariableVo.getVariableFormat());
		fields.add(renderVariableVo.getVariableType());
		fields.add(renderVariableVo.getVariableValue());
		fields.add(renderVariableVo.getRenderId());
		fields.add(renderVariableVo.getVariableName());
		
		String sql =
			"update RenderObject set " +
				"VariableFormat=?, " +
				"VariableType=?, " +
				"VariableValue=? " +
			" where " +
				" RenderId=? and VariableName=? ";
		
		int rowsUpadted = jdbcTemplate.update(sql, fields.toArray());
		return rowsUpadted;
	}
	
	public int deleteByPrimaryKey(long msgId, String variableName) {
		String sql = 
			"delete from RenderObject where renderId=? and variableName=? ";
		
		ArrayList<String> fields = new ArrayList<String>();
		fields.add(msgId+"");
		fields.add(variableName);
		
		int rowsDeleted = jdbcTemplate.update(sql, fields.toArray());
		return rowsDeleted;
	}
	
	public int deleteByRenderId(long msgId) {
		String sql = 
			"delete from RenderObject where renderId=? ";
		
		ArrayList<String> fields = new ArrayList<String>();
		fields.add(msgId+"");
		
		int rowsDeleted = jdbcTemplate.update(sql, fields.toArray());
		return rowsDeleted;
	}
	
	public int insert(RenderObjectVo renderVariableVo) {
		String sql = 
			"INSERT INTO RenderObject (" +
				"RenderId, " +
				"VariableName, " +
				"VariableFormat, " +
				"VariableType, " +
				"VariableValue " +
			") VALUES (" +
				" ?, ?, ?, ?, ? " +
				")";
		
		ArrayList<Object> fields = new ArrayList<Object>();
		fields.add(renderVariableVo.getRenderId());
		fields.add(renderVariableVo.getVariableName());
		fields.add(renderVariableVo.getVariableFormat());
		fields.add(renderVariableVo.getVariableType());
		fields.add(renderVariableVo.getVariableValue());
		
		int rowsInserted = jdbcTemplate.update(sql, fields.toArray());
		return rowsInserted;
	}
	
	public DataSource getDataSource() {
		return dataSource;
	}

	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
		this.jdbcTemplate = new JdbcTemplate(this.dataSource);
	}
	
	protected String getRowIdSql() {
		return "select last_insert_id()";
	}
}
