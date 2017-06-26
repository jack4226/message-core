package ltj.message.dao.servers;

import java.util.List;

import ltj.message.vo.SmtpConnVo;

public interface SmtpServerDao {
	public SmtpConnVo getByServerName(String serverName);
	public SmtpConnVo getByPrimaryKey(long rowId);
	public List<SmtpConnVo> getAll(boolean onlyActive);
	public List<SmtpConnVo> getAllForTrial(boolean onlyActive);
	public List<SmtpConnVo> getByServerType(String serverType, boolean onlyActive);
	public List<SmtpConnVo> getBySslFlag(boolean useSSL, boolean onlyActive);
	public List<SmtpConnVo> getBySslFlagForTrial(boolean useSSL, boolean onlyActive);
	public int update(SmtpConnVo smtpConnVo);
	public int deleteByServerName(String serverName);
	public int deleteByPrimaryKey(long rowId);
	public int insert(SmtpConnVo smtpConnVo);
}
