package ltj.message.dao.client;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Component;

import ltj.message.constant.Constants;
import ltj.message.constant.StatusId;
import ltj.message.constant.VariableType;
import ltj.message.dao.abstrct.AbstractDao;
import ltj.message.dao.abstrct.MetaDataUtil;
import ltj.message.dao.template.ClientVariableDao;
import ltj.message.util.BlobUtil;
import ltj.message.util.StringUtil;
import ltj.message.util.TimestampUtil;
import ltj.message.vo.ClientVo;
import ltj.vo.template.ClientVariableVo;

@Component("clientDao")
public class ClientJdbcDao extends AbstractDao implements ClientDao {
	
	final static Map<String, ClientVo> clientCache = new HashMap<String, ClientVo>();
	private java.util.Date lastFetchTime = new java.util.Date();

	@Override
	public ClientVo getByClientId(String clientId) {
		java.util.Date currTime = new java.util.Date();
		if (currTime.getTime() - lastFetchTime.getTime() > (15*60*1000)) {
			// reload every 15 minutes
			synchronized (clientCache) {
				clientCache.clear();
			}
			lastFetchTime = currTime;
		}
		if (!clientCache.containsKey(clientId)) {
			String sql = "select *, client_id as OrigClientId, updt_time as OrigUpdtTime " +
					"from client_tbl where client_id=?";
			Object[] parms = new Object[] { clientId };
			try {
				ClientVo vo = getJdbcTemplate().queryForObject(sql, parms,
						new BeanPropertyRowMapper<ClientVo>(ClientVo.class));
				synchronized (clientCache) {
					clientCache.put(clientId, vo);
				}
			}
			catch (EmptyResultDataAccessException e) {
			}
		}
		return (ClientVo) BlobUtil.deepCopy(clientCache.get(clientId));
	}

	@Override
	public ClientVo getByDomainName(String domainName) {
		String sql = "select *, client_id as OrigClientId, updt_time as OrigUpdtTime " +
				"from client_tbl where domain_name=?";
		Object[] parms = new Object[] { domainName };
		List<ClientVo> list = getJdbcTemplate().query(sql, parms, new BeanPropertyRowMapper<ClientVo>(ClientVo.class));
		if (list.size() > 0) {
			return list.get(0);
		}
		else {
			return null;
		}
	}

	@Override
	public List<ClientVo> getAll() {
		String sql = 
			"select *, client_id as OrigClientId, updt_time as OrigUpdtTime " +
				"from client_tbl order by client_id";
		
		List<ClientVo> list = getJdbcTemplate().query(sql, new BeanPropertyRowMapper<ClientVo>(ClientVo.class));
		return list;
	}
	
	@Override
	public List<ClientVo> getAllForTrial() {
		String sql = 
			"select *, client_id as OrigClientId, updt_time as OrigUpdtTime " +
				"from client_tbl " +
			" order by row_id " +
			" limit 1 ";
		int fetchSize = getJdbcTemplate().getFetchSize();
		int maxRows = getJdbcTemplate().getMaxRows();
		getJdbcTemplate().setFetchSize(1);
		getJdbcTemplate().setMaxRows(1);
		List<ClientVo> list = getJdbcTemplate().query(sql, new BeanPropertyRowMapper<ClientVo>(ClientVo.class));
		getJdbcTemplate().setFetchSize(fetchSize);
		getJdbcTemplate().setMaxRows(maxRows);
		return list;
	}
	
	@Override
	public String getSystemId() {
		String sql = 
			"select system_id " +
				"from client_tbl where client_id='" + Constants.DEFAULT_CLIENTID + "'";
		return (String) getJdbcTemplate().queryForObject(sql, String.class);
	}

	@Override
	public String getSystemKey() {
		String sql = 
			"select system_key " +
				"from client_tbl where client_id='" + Constants.DEFAULT_CLIENTID + "'";
		return (String) getJdbcTemplate().queryForObject(sql, String.class);
	}

	@Override
	public synchronized int updateSystemKey(String key) {
		List<Object> keys = new ArrayList<>();
		keys.add(key);
		String sql = "update client_tbl set " +
			"system_key=? " +
			" where client_id= '" + Constants.DEFAULT_CLIENTID + "'";
		int rowsUpdated = getJdbcTemplate().update(sql, keys.toArray());
		return rowsUpdated;
	}

	@Override
	public synchronized int update(ClientVo clientVo) {
		clientVo.setUpdtTime(new Timestamp(System.currentTimeMillis()));
		validateClientVo(clientVo);
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(clientVo);
		String sql = MetaDataUtil.buildUpdateStatement("client_tbl", clientVo);
		if (clientVo.getOrigUpdtTime() != null) {
			// optimistic locking
			sql += " and updt_time=:origUpdtTime ";
		}
		int rowsUpadted = getNamedParameterJdbcTemplate().update(sql, namedParameters);
		synchronized (clientCache) {
			clientCache.remove(clientVo.getClientId()); // remove from cache
		}
		updateClientVariables(clientVo);
		clientVo.setOrigUpdtTime(clientVo.getUpdtTime());
		clientVo.setOrigClientId(clientVo.getClientId());
		updateReloadFlags();
		return rowsUpadted;
	}

	@Override
	public synchronized int delete(String clientId) {
		if (Constants.DEFAULT_CLIENTID.equals(clientId)) {
			throw new IllegalArgumentException("Can't delete System Default Client.");
		}
		String sql = "delete from client_tbl where client_id=?";
		Object[] parms = new Object[] {clientId};
		int rowsDeleted = getJdbcTemplate().update(sql, parms);
		synchronized (clientCache) {
			clientCache.remove(clientId); // remove from cache			
		}
		deleteClientVariables(clientId);
		updateReloadFlags();
		return rowsDeleted;
	}
	
	@Override
	public synchronized int insert(ClientVo clientVo) {
		clientVo.setUpdtTime(new Timestamp(System.currentTimeMillis()));
		validateClientVo(clientVo);
		String systemId = TimestampUtil.db2ToDecStr(TimestampUtil.getDb2Timestamp());
		clientVo.setSystemId(systemId);
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(clientVo);
		String sql = MetaDataUtil.buildInsertStatement("client_tbl", clientVo);
		int rowsInserted = getNamedParameterJdbcTemplate().update(sql, namedParameters);
		clientVo.setRowId(retrieveRowId());
		clientVo.setOrigUpdtTime(clientVo.getUpdtTime());
		clientVo.setOrigClientId(clientVo.getClientId());
		if (MetaDataUtil.getTableMetaData("client_variable") != null) {
			updateClientVariables(clientVo);
		}
		if (MetaDataUtil.getTableMetaData("reload_flags") != null) {
			updateReloadFlags();
		}
		return rowsInserted;
	}
	
	public static void validateClientVo(ClientVo clientVo) {
		if (clientVo.isUseTestAddr()) {
			if (StringUtil.isEmpty(clientVo.getTestToAddr())) {
				throw new IllegalStateException("Test TO Address was null");
			}
		}
		if (clientVo.isIsVerpEnabled()) {
			if (StringUtil.isEmpty(clientVo.getVerpInboxName())) {
				throw new IllegalStateException("VERP bounce inbox name was null");
			}
			if (StringUtil.isEmpty(clientVo.getVerpRemoveInbox())) {
				throw new IllegalStateException("VERP remove inbox name was null");
			}
		}
	}
	
	/**
	 * A synchronization method that updates client variables based on the new
	 * Client record.
	 * 
	 * @return number of rows inserted
	 */
	private int updateClientVariables(ClientVo clientVo) {
		getClientVariableDao().deleteByClientId(clientVo.getClientId());
		if (StringUtil.isEmpty(clientVo.getOrigClientId())) {
			getClientVariableDao().deleteByClientId(clientVo.getOrigClientId());
		}
		int rowsInserted = 0;
		ClientVariableVo vo = new ClientVariableVo();
		vo.setClientId(clientVo.getClientId());
		vo.setStatusId(StatusId.ACTIVE.value());
		vo.setStartTime(new Timestamp(new java.util.Date().getTime()));
		
		vo.setVariableName("DomainName");
		vo.setVariableValue(clientVo.getDomainName());
		vo.setVariableFormat(null);
		vo.setVariableType(VariableType.TEXT.value());
		vo.setAllowOverride(Constants.Y);
		vo.setRequired(Constants.N);
		getClientVariableDao().insert(vo);
		rowsInserted++;
		
		vo.setVariableName("SiteName");
		vo.setVariableValue(clientVo.getClientName());
		vo.setVariableFormat(null);
		vo.setVariableType(VariableType.TEXT.value());
		vo.setAllowOverride(Constants.Y);
		vo.setRequired(Constants.N);
		getClientVariableDao().insert(vo);
		rowsInserted++;
		
		if (!StringUtil.isEmpty(clientVo.getWebSiteUrl())) {
			vo.setVariableName("WebSiteUrl");
			vo.setVariableValue(clientVo.getWebSiteUrl());
			vo.setVariableFormat(null);
			vo.setVariableType(VariableType.TEXT.value());
			vo.setAllowOverride(Constants.Y);
			vo.setRequired(Constants.N);
			getClientVariableDao().insert(vo);
			rowsInserted++;
		}
		
		vo.setVariableName("ContactEmailAddress");
		vo.setVariableValue(clientVo.getContactEmail());
		vo.setVariableFormat(null);
		vo.setVariableType(VariableType.TEXT.value());
		vo.setAllowOverride(Constants.Y);
		vo.setRequired(Constants.N);
		getClientVariableDao().insert(vo);
		rowsInserted++;
		
		if (!StringUtil.isEmpty(clientVo.getContactPhone())) {
			vo.setVariableName("ContactPhoneNumber");
			vo.setVariableValue(clientVo.getContactPhone());
			vo.setVariableFormat(null);
			vo.setVariableType(VariableType.TEXT.value());
			vo.setAllowOverride(Constants.Y);
			vo.setRequired(Constants.N);
			getClientVariableDao().insert(vo);
			rowsInserted++;
		}
		
		vo.setVariableName("ClientId");
		vo.setVariableValue(clientVo.getClientId());
		vo.setVariableFormat(null);
		vo.setVariableType(VariableType.TEXT.value());
		vo.setAllowOverride(Constants.Y);
		vo.setRequired(Constants.N);
		getClientVariableDao().insert(vo);
		rowsInserted++;
		
		vo.setVariableName("CurrentDateTime");
		vo.setVariableValue(null);
		vo.setVariableFormat(null);
		vo.setVariableType(VariableType.DATETIME.value());
		vo.setAllowOverride(Constants.Y);
		vo.setRequired(Constants.N);
		getClientVariableDao().insert(vo);
		rowsInserted++;
		
		vo.setVariableName("CurrentDate");
		vo.setVariableValue(null);
		vo.setVariableFormat("yyyy-MM-dd");
		vo.setVariableType(VariableType.DATETIME.value());
		vo.setAllowOverride(Constants.Y);
		vo.setRequired(Constants.N);
		getClientVariableDao().insert(vo);
		rowsInserted++;
		
		vo.setVariableName("CurrentTime");
		vo.setVariableValue(null);
		vo.setVariableFormat("hh:mm:ss a");
		vo.setVariableType(VariableType.DATETIME.value());
		vo.setAllowOverride(Constants.Y);
		vo.setRequired(Constants.N);
		getClientVariableDao().insert(vo);
		rowsInserted++;
		
		return rowsInserted;
	}

	private int deleteClientVariables(String clientId) {
		int rowsDeleted = getClientVariableDao().deleteByClientId(clientId);
		return rowsDeleted;
	}
	
	private void updateReloadFlags() {
		getReloadFlagsDao().updateClientReloadFlag();
	}
	
	@Autowired
	private ClientVariableDao clientVariableDao;
	private synchronized ClientVariableDao getClientVariableDao() {
		return clientVariableDao;
	}
	
	@Autowired
	private ReloadFlagsDao reloadFlagsDao;
	private synchronized ReloadFlagsDao getReloadFlagsDao() {
		return reloadFlagsDao;
	}
}
