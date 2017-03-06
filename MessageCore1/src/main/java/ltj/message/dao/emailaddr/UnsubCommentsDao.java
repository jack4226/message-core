package ltj.message.dao.emailaddr;

import java.util.List;

import ltj.message.vo.emailaddr.UnsubCommentsVo;

public interface UnsubCommentsDao {
	public UnsubCommentsVo getByPrimaryKey(int rowId);
	public List<UnsubCommentsVo> getFirst100();
	public List<UnsubCommentsVo> getByEmailAddrId(long emailAddrId);
	public List<UnsubCommentsVo> getByListId(String listId);
	public int update(UnsubCommentsVo unsubCommentsVo);
	public int deleteByPrimaryKey(int rowId);
	public int deleteByEmailAddrId(long emailAddrId);
	public int insert(UnsubCommentsVo unsubCommentsVo);
}
