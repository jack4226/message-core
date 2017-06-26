package ltj.message.dao.servers;

import java.util.List;

import ltj.message.vo.SocketServerVo;

public interface SocketServerDao {
	public SocketServerVo getByServerName(String serverName);
	public SocketServerVo getByPrimaryKey(long rowId);
	public List<SocketServerVo> getAll(boolean onlyActive);
	public int update(SocketServerVo socketServerVo);
	public int deleteByServerName(String serverName);
	public int deleteByPrimaryKey(long rowId);
	public int insert(SocketServerVo socketServerVo);
}
