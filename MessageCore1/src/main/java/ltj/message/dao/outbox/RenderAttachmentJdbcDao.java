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
import ltj.vo.outbox.RenderAttachmentVo;

@Component("renderAttachmentDao")
public class RenderAttachmentJdbcDao extends AbstractDao implements RenderAttachmentDao {
	
	@Override
	public RenderAttachmentVo getByPrimaryKey(long renderId, int attchmntSeq) {
		String sql = 
			"select * " +
			"from " +
				"RenderAttachment where renderId=? and attchmntSeq=? ";
		
		Object[] parms = new Object[] {renderId, attchmntSeq};
		try {
			RenderAttachmentVo vo = getJdbcTemplate().queryForObject(sql, parms, 
					new BeanPropertyRowMapper<RenderAttachmentVo>(RenderAttachmentVo.class));
			return vo;
		}
		catch (EmptyResultDataAccessException e) {
			return null;
		}
	}
	
	@Override
	public List<RenderAttachmentVo> getByRenderId(long renderId) {
		String sql = 
			"select * " +
			" from " +
				" RenderAttachment where renderId=? " +
			" order by attchmntSeq";
		Object[] parms = new Object[] {renderId};
		List<RenderAttachmentVo> list = getJdbcTemplate().query(sql, parms, 
				new BeanPropertyRowMapper<RenderAttachmentVo>(RenderAttachmentVo.class));
		return list;
	}
	
	@Override
	public int update(RenderAttachmentVo renderAttachmentVo) {
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(renderAttachmentVo);
		String sql = MetaDataUtil.buildUpdateStatement("RenderAttachment", renderAttachmentVo);
		int rowsUpadted = getNamedParameterJdbcTemplate().update(sql, namedParameters);
		return rowsUpadted;
	}
	
	@Override
	public int deleteByPrimaryKey(long renderId, int attchmntSeq) {
		String sql = 
			"delete from RenderAttachment where renderId=? and attchmntSeq=? ";
		
		List<Object> fields = new ArrayList<>();
		fields.add(renderId);
		fields.add(attchmntSeq);
		
		int rowsDeleted = getJdbcTemplate().update(sql, fields.toArray());
		return rowsDeleted;
	}
	
	@Override
	public int deleteByRenderId(long renderId) {
		String sql = 
			"delete from RenderAttachment where renderId=? ";
		
		List<Object> fields = new ArrayList<>();
		fields.add(renderId);
		
		int rowsDeleted = getJdbcTemplate().update(sql, fields.toArray());
		return rowsDeleted;
	}
	
	@Override
	public int insert(RenderAttachmentVo renderAttachmentVo) {
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(renderAttachmentVo);
		String sql = MetaDataUtil.buildInsertStatement("RenderAttachment", renderAttachmentVo);
		int rowsInserted = getNamedParameterJdbcTemplate().update(sql, namedParameters);
		return rowsInserted;
	}
}
