package ltj.message.dao.user;

import java.util.List;

import ltj.message.vo.UserVo;

public interface UserDao {
	public UserVo getByPrimaryKey(String userId);
	public UserVo getForLogin(String userId, String password);
	public List<UserVo> getFirst100(boolean onlyActive);
	public int update(UserVo userVo);
	public int update4Web(UserVo userVo);
	public int deleteByPrimaryKey(String userId);
	public int insert(UserVo userVo);
}
