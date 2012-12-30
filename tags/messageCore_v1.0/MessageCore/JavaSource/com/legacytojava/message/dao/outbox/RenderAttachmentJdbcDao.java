package com.legacytojava.message.dao.outbox;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import com.legacytojava.message.vo.outbox.RenderAttachmentVo;

public class RenderAttachmentJdbcDao implements RenderAttachmentDao {
	
	private DataSource dataSource;
	private JdbcTemplate jdbcTemplate;
	
	private static final class RenderAttachmentMapper implements RowMapper {
		
		public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
			RenderAttachmentVo renderAttachmentVo = new RenderAttachmentVo();
			
			renderAttachmentVo.setRenderId(rs.getLong("RenderId"));
			renderAttachmentVo.setAttchmntSeq(rs.getInt("AttchmntSeq"));
			renderAttachmentVo.setAttchmntName(rs.getString("AttchmntName"));
			renderAttachmentVo.setAttchmntType(rs.getString("AttchmntType"));
			renderAttachmentVo.setAttchmntDisp(rs.getString("AttchmntDisp"));
			renderAttachmentVo.setAttchmntValue(rs.getBytes("AttchmntValue"));
			
			return renderAttachmentVo;
		}
	}

	public RenderAttachmentVo getByPrimaryKey(long renderId, int attchmntSeq) {
		String sql = 
			"select * " +
			"from " +
				"RenderAttachment where renderId=? and attchmntSeq=? ";
		
		Object[] parms = new Object[] {renderId, attchmntSeq};
		List<?> list = (List<?>)jdbcTemplate.query(sql, parms, new RenderAttachmentMapper());
		if (list.size()>0)
			return (RenderAttachmentVo)list.get(0);
		else
			return null;
	}
	
	@SuppressWarnings("unchecked")
	public List<RenderAttachmentVo> getByRenderId(long renderId) {
		String sql = 
			"select * " +
			" from " +
				" RenderAttachment where renderId=? " +
			" order by attchmntSeq";
		Object[] parms = new Object[] {renderId};
		List<RenderAttachmentVo> list = (List<RenderAttachmentVo>)jdbcTemplate.query(sql, parms, new RenderAttachmentMapper());
		return list;
	}
	
	public int update(RenderAttachmentVo renderAttachmentVo) {
		
		ArrayList<Object> fields = new ArrayList<Object>();
		fields.add(renderAttachmentVo.getAttchmntName());
		fields.add(renderAttachmentVo.getAttchmntType());
		fields.add(renderAttachmentVo.getAttchmntDisp());
		fields.add(renderAttachmentVo.getAttchmntValue());
		fields.add(renderAttachmentVo.getRenderId());
		fields.add(renderAttachmentVo.getAttchmntSeq());
		
		String sql =
			"update RenderAttachment set " +
				"AttchmntName=?, " +
				"AttchmntType=?, " +
				"AttchmntDisp=?, " +
				"AttchmntValue=? " +
			" where " +
				" renderId=? and attchmntSeq=? ";
		
		int rowsUpadted = jdbcTemplate.update(sql, fields.toArray());
		return rowsUpadted;
	}
	
	public int deleteByPrimaryKey(long renderId, int attchmntSeq) {
		String sql = 
			"delete from RenderAttachment where renderId=? and attchmntSeq=? ";
		
		ArrayList<Object> fields = new ArrayList<Object>();
		fields.add(renderId+"");
		fields.add(attchmntSeq+"");
		
		int rowsDeleted = jdbcTemplate.update(sql, fields.toArray());
		return rowsDeleted;
	}
	
	public int deleteByRenderId(long renderId) {
		String sql = 
			"delete from RenderAttachment where renderId=? ";
		
		ArrayList<Object> fields = new ArrayList<Object>();
		fields.add(renderId+"");
		
		int rowsDeleted = jdbcTemplate.update(sql, fields.toArray());
		return rowsDeleted;
	}
	
	public int insert(RenderAttachmentVo renderAttachmentVo) {
		String sql = 
			"INSERT INTO RenderAttachment (" +
			"RenderId, " +
			"AttchmntSeq, " +
			"AttchmntName, " +
			"AttchmntType, " +
			"AttchmntDisp, " +
			"AttchmntValue " +
			") VALUES (" +
				" ?, ?, ?, ?, ?, ? " +
				")";
		
		ArrayList<Object> fields = new ArrayList<Object>();
		fields.add(renderAttachmentVo.getRenderId());
		fields.add(renderAttachmentVo.getAttchmntSeq());
		fields.add(renderAttachmentVo.getAttchmntName());
		fields.add(renderAttachmentVo.getAttchmntType());
		fields.add(renderAttachmentVo.getAttchmntDisp());
		fields.add(renderAttachmentVo.getAttchmntValue());
		
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
