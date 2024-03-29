package ltj.message.dao.action;

import java.util.ArrayList;
import java.util.List;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Component;

import ltj.message.dao.abstrct.AbstractDao;
import ltj.message.dao.abstrct.MetaDataUtil;
import ltj.message.vo.action.MsgDataTypeVo;

@Component("msgDataTypeDao")
public class MsgDataTypeJdbcDao extends AbstractDao implements MsgDataTypeDao {
	
	@Override
	public MsgDataTypeVo getByTypeValuePair(String type, String value) {
		String sql = 
			"select * " +
			"from " +
				"msg_data_type where data_type=? and data_type_value=? ";
		
		Object[] parms = new Object[] {type, value};
		try {
			MsgDataTypeVo vo = getJdbcTemplate().queryForObject(sql, parms, 
					new BeanPropertyRowMapper<MsgDataTypeVo>(MsgDataTypeVo.class));
			return vo;
		}
		catch (EmptyResultDataAccessException e) {
			return null;
		}
	}
	
	@Override
	public MsgDataTypeVo getByPrimaryKey(int rowId) {
		String sql = 
			"select * " +
			"from " +
				"msg_data_type where row_id=? ";
		
		Object[] parms = new Object[] {rowId};
		try {
			MsgDataTypeVo vo = getJdbcTemplate().queryForObject(sql, parms, 
					new BeanPropertyRowMapper<MsgDataTypeVo>(MsgDataTypeVo.class));
			return vo;
		}
		catch (EmptyResultDataAccessException e) {
			return null;
		}
	}
	
	@Override
	public List<MsgDataTypeVo> getByDataType(String dataType) {
		String sql = 
			"select * " +
			"from " +
				"msg_data_type where data_type=? " +
			" order by data_type_value asc ";
		
		Object[] parms = new Object[] {dataType};
		List<MsgDataTypeVo> list = getJdbcTemplate().query(sql, parms, 
				new BeanPropertyRowMapper<MsgDataTypeVo>(MsgDataTypeVo.class));
		return list;
	}
	
	@Override
	public List<String> getDataTypes() {
		String sql = 
			"select distinct(data_type) " +
			"from " +
				"msg_data_type " +
			" order by data_type asc ";
		
		List<String> list = (List<String>)getJdbcTemplate().queryForList(sql, String.class);
		return list;
	}
	
	@Override
	public int update(MsgDataTypeVo msgDataTypeVo) {
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(msgDataTypeVo);
		String sql = MetaDataUtil.buildUpdateStatement("msg_data_type", msgDataTypeVo);
		int rowsUpadted = getNamedParameterJdbcTemplate().update(sql, namedParameters);
		return rowsUpadted;
	}
	
	@Override
	public int deleteByPrimaryKey(int rowId) {
		String sql = 
			"delete from msg_data_type where row_id=? ";
		
		List<Object> fields = new ArrayList<>();
		fields.add(rowId);
		
		int rowsDeleted = getJdbcTemplate().update(sql, fields.toArray());
		return rowsDeleted;
	}
	
	@Override
	public int deleteByDataType(String dataType) {
		String sql = 
			"delete from msg_data_type where data_type=? ";
		
		List<String> fields = new ArrayList<>();
		fields.add(dataType);
		
		int rowsDeleted = getJdbcTemplate().update(sql, fields.toArray());
		return rowsDeleted;
	}
	
	@Override
	public int insert(MsgDataTypeVo msgDataTypeVo) {
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(msgDataTypeVo);
		String sql = MetaDataUtil.buildInsertStatement("msg_data_type", msgDataTypeVo);
		int rowsInserted = getNamedParameterJdbcTemplate().update(sql, namedParameters);
		msgDataTypeVo.setRowId(retrieveRowId());
		return rowsInserted;
	}
}
