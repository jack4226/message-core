package ltj.message.dao.idtokens;

import java.sql.Timestamp;
import java.util.Hashtable;
import java.util.List;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Component;

import ltj.message.dao.abstrct.AbstractDao;
import ltj.message.dao.abstrct.MetaDataUtil;
import ltj.message.vo.IdTokensVo;

@Component("idTokensDao")
public class IdTokensJdbcDao extends AbstractDao implements IdTokensDao {
	
	private static final Hashtable<String, IdTokensVo> cache = new Hashtable<String, IdTokensVo>();

	@Override
	public IdTokensVo getByClientId(String clientId) {
		/*
		 * This method is not thread safe as the "cache" is not locked.
		 * Since this method is heavily used it is reasonable to keep the 
		 * performance impact at the minimal by sacrificing thread safety.
		 */
		if (!cache.containsKey(clientId)) {
			String sql = "select * from id_tokens where client_id=?";
			Object[] parms = new Object[] {clientId};
			try {
			IdTokensVo vo = getJdbcTemplate().queryForObject(sql, parms, 
					new BeanPropertyRowMapper<IdTokensVo>(IdTokensVo.class));
				cache.put(clientId, vo);
			}
			catch (EmptyResultDataAccessException e) {
				cache.put(clientId, null);
			}
		}
		return cache.get(clientId);
	}
	
	@Override
	public List<IdTokensVo> getAll() {
		String sql = "select * from id_tokens order by client_id";
		List<IdTokensVo> list = getJdbcTemplate().query(sql, 
				new BeanPropertyRowMapper<IdTokensVo>(IdTokensVo.class));
		return list;
	}
	
	@Override
	public int update(IdTokensVo idTokensVo) {
		idTokensVo.setUpdtTime(new Timestamp(System.currentTimeMillis()));
		idTokensVo.setOrigUpdtTime(idTokensVo.getUpdtTime());
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(idTokensVo);
		String sql = MetaDataUtil.buildUpdateStatement("id_tokens", idTokensVo);
		synchronized (cache) {
			int rowsUpadted = getNamedParameterJdbcTemplate().update(sql, namedParameters);
			removeFromCache(idTokensVo.getClientId());
			return rowsUpadted;
		}
	}
	
	@Override
	public int delete(String clientId) {
		String sql = "delete from id_tokens where client_id=?";
		Object[] parms = new Object[] {clientId};
		synchronized (cache) {
			int rowsDeleted = getJdbcTemplate().update(sql, parms);
			removeFromCache(clientId);
			return rowsDeleted;
		}
	}
	
	@Override
	public int insert(IdTokensVo idTokensVo) {
		idTokensVo.setUpdtTime(new Timestamp(System.currentTimeMillis()));
		idTokensVo.setOrigUpdtTime(idTokensVo.getUpdtTime());
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(idTokensVo);
		String sql = MetaDataUtil.buildInsertStatement("id_tokens", idTokensVo);
		synchronized (cache) {
			int rowsInserted = getNamedParameterJdbcTemplate().update(sql, namedParameters);
			idTokensVo.setRowId(retrieveRowId());
			removeFromCache(idTokensVo.getClientId());
			return rowsInserted;
		}
	}
	
	private void removeFromCache(String clientId) {
		if (cache.containsKey(clientId)) {
			cache.remove(clientId);
		}
	}
}
