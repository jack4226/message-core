package ltj.message.dao.servers;

import java.util.List;

import ltj.message.vo.TimerServerVo;

public interface TimerServerDao {
	public TimerServerVo getByServerName(String serverName);
	public TimerServerVo getByPrimaryKey(long rowId);
	public List<TimerServerVo> getAll(boolean onlyActive);
	public int update(TimerServerVo timerServerVo);
	public int deleteByServerName(String serverName);
	public int deleteByPrimaryKey(long rowId);
	public int insert(TimerServerVo timerServerVo);
}
