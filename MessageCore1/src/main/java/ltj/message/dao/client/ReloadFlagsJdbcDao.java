package ltj.message.dao.client;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.stereotype.Component;

import ltj.message.dao.abstrct.AbstractDao;
import ltj.message.vo.ReloadFlagsVo;

@Component("reloadFlagsDao")
public class ReloadFlagsJdbcDao extends AbstractDao implements ReloadFlagsDao {
	protected static final Logger logger = Logger.getLogger(ReloadFlagsJdbcDao.class);
	
	@Override
	public ReloadFlagsVo select() {
		return selectWithRepair(0);
	}
	
	private ReloadFlagsVo selectWithRepair(int retry) {
		String sql = "select * from reload_flags ";
		List<ReloadFlagsVo> list = getJdbcTemplate().query(sql, 
				new BeanPropertyRowMapper<ReloadFlagsVo>(ReloadFlagsVo.class));
		if (list.size()>0) {
			return list.get(0);
		}
		else if (retry < 1) {
			repair();
			return selectWithRepair(++retry);
		}
		else {
			throw new RuntimeException("Internal error, contact programming.");
		}
	}
	
	@Override
	public int update(ReloadFlagsVo vo) {
		String sql = "update reload_flags set " +
			"Clients=?," +
			"Rules=?," +
			"Actions=?," +
			"Templates=?," +
			"Schedules=?";
		List<Object> fields = new ArrayList<>();
		fields.add(vo.getClients());
		fields.add(vo.getRules());
		fields.add(vo.getActions());
		fields.add(vo.getTemplates());
		fields.add(vo.getSchedules());

		int rowsUpdated = getJdbcTemplate().update(sql, fields.toArray());
		return rowsUpdated;
	}
	
	@Override
	public int updateClientReloadFlag() {
		String sql = "update reload_flags set " +
			"Clients=Clients + 1";
		int rows = getJdbcTemplate().update(sql);
		return rows;
	}

	@Override
	public int updateRuleReloadFlag() {
		String sql = "update reload_flags set " +
			"Rules=Rules + 1";
		int rows = getJdbcTemplate().update(sql);
		return rows;
	}

	@Override
	public int updateActionReloadFlag() {
		String sql = "update reload_flags set " +
			"Actions=Actions + 1";
		int rows = getJdbcTemplate().update(sql);
		return rows;
	}

	@Override
	public int updateTemplateReloadFlag() {
		String sql = "update reload_flags set " +
			"Templates=Templates + 1";
		int rows = getJdbcTemplate().update(sql);
		return rows;
	}

	@Override
	public int updateScheduleReloadFlag() {
		String sql = "update reload_flags set " +
			"Schedules=Schedules + 1";
		int rows = getJdbcTemplate().update(sql);
		return rows;
	}

	private int repair() {
		String sql = "insert into reload_flags (" +
				"Clients," +
				"Rules," +
				"Actions," +
				"Templates," +
				"Schedules) " +
				" values (" +
				"0,0,0,0,0)";
		int rowsInserted = getJdbcTemplate().update(sql);
		return rowsInserted;
	}
}
