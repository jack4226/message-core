package ltj.message.dao.user;

import java.util.List;

import ltj.message.vo.UserVo;

public interface UserDao {
	public UserVo getByUserId(String userId);
	public UserVo getByPrimaryKey(long rowId);
	public UserVo getForLogin(String userId, String password);
	public List<UserVo> getFirst100(boolean onlyActive);
	public int update(UserVo userVo);
	public int update4Web(UserVo userVo);
	public int deleteByUserId(String userId);
	public int deleteByPrimaryKey(long rowId);
	public int insert(UserVo userVo);
}
