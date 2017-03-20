package ltj.message.dao.action;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Component;

import ltj.message.dao.abstrct.AbstractDao;
import ltj.message.dao.abstrct.MetaDataUtil;
import ltj.message.dao.client.ReloadFlagsDao;
import ltj.message.vo.action.MsgActionDetailVo;

@Component("msgActionDetailDao")
public class MsgActionDetailJdbcDao extends AbstractDao implements MsgActionDetailDao {
	
	@Override
	public MsgActionDetailVo getByActionId(String actionId) {
		String sql = 
			"select *, UpdtTime as OrigUpdtTime " +
			"from " +
				"msg_action_detail where actionId=? ";
		
		Object[] parms = new Object[] {actionId};
		try {
			MsgActionDetailVo vo = getJdbcTemplate().queryForObject(sql, parms,
					new BeanPropertyRowMapper<MsgActionDetailVo>(MsgActionDetailVo.class));
			return vo;
		}
		catch (EmptyResultDataAccessException e) {
			return null;
		}
	}
	
	@Override
	public MsgActionDetailVo getByPrimaryKey(int rowId) {
		String sql = 
			"select *, UpdtTime as OrigUpdtTime " +
			"from " +
				"msg_action_detail where RowId=? ";
		
		Object[] parms = new Object[] {rowId};
		
		try {
			MsgActionDetailVo vo = getJdbcTemplate().queryForObject(sql, parms,
					new BeanPropertyRowMapper<MsgActionDetailVo>(MsgActionDetailVo.class));
			return vo;
		}
		catch (EmptyResultDataAccessException e) {
			return null;
		}
	}
	
	@Override
	public List<MsgActionDetailVo> getAll() {
		String sql = 
			"select *, UpdtTime as OrigUpdtTime " +
			" from " +
				" msg_action_detail " +
			" order by actionId asc ";
		List<MsgActionDetailVo> list = getJdbcTemplate().query(sql, 
				new BeanPropertyRowMapper<MsgActionDetailVo>(MsgActionDetailVo.class));
		return list;
	}
	
	@Override
	public List<String> getActionIds() {
		String sql = 
			"select distinct(ActionId) from msg_action_detail " +
			" order by ActionId";
		
		List<String> list = (List<String>)getJdbcTemplate().queryForList(sql, String.class);
		return list;
	}
	
	@Override
	public synchronized int update(MsgActionDetailVo msgActionDetailVo) {
		msgActionDetailVo.setUpdtTime(new Timestamp(System.currentTimeMillis()));
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(msgActionDetailVo);
		String sql = MetaDataUtil.buildUpdateStatement("msg_action_detail", msgActionDetailVo);
		if (msgActionDetailVo.getOrigUpdtTime() != null) {
			sql += " and UpdtTime=:origUpdtTime ";
		}
		int rowsUpadted = getNamedParameterJdbcTemplate().update(sql, namedParameters);
		msgActionDetailVo.setOrigUpdtTime(msgActionDetailVo.getUpdtTime());
		updateReloadFlags();
		return rowsUpadted;
	}
	
	@Override
	public synchronized int deleteByActionId(String actionId) {
		String sql = 
			"delete from msg_action_detail where actionId=? ";
		
		List<String> fields = new ArrayList<>();
		fields.add(actionId);
		
		int rowsDeleted = getJdbcTemplate().update(sql, fields.toArray());
		updateReloadFlags();
		return rowsDeleted;
	}
	
	@Override
	public synchronized int deleteByPrimaryKey(int rowId) {
		String sql = 
			"delete from msg_action_detail where RowId=? ";
		
		List<Object> fields = new ArrayList<>();
		fields.add(rowId);
		
		int rowsDeleted = getJdbcTemplate().update(sql, fields.toArray());
		updateReloadFlags();
		return rowsDeleted;
	}
	
	@Override
	public synchronized int insert(MsgActionDetailVo msgActionDetailVo) {
		msgActionDetailVo.setUpdtTime(new Timestamp(System.currentTimeMillis()));
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(msgActionDetailVo);
		String sql = MetaDataUtil.buildInsertStatement("msg_action_detail", msgActionDetailVo);
		int rowsInserted = getNamedParameterJdbcTemplate().update(sql, namedParameters);
		msgActionDetailVo.setRowId(retrieveRowId());
		msgActionDetailVo.setOrigUpdtTime(msgActionDetailVo.getUpdtTime());
		updateReloadFlags();
		return rowsInserted;
	}

	private void updateReloadFlags() {
		getReloadFlagsDao().updateActionReloadFlag();
	}

	@Autowired
	private ReloadFlagsDao reloadFlagsDao;
	private synchronized ReloadFlagsDao getReloadFlagsDao() {
		return reloadFlagsDao;
	}
}
