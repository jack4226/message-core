package ltj.message.dao.customer;

import java.util.List;

import ltj.message.vo.CustomerVo;
import ltj.message.vo.PagingCustVo;

public interface CustomerDao {
	public CustomerVo getByCustId(String custId);
	public List<CustomerVo> getByClientId(String clientId);
	public CustomerVo getByEmailAddrId(long emailAddrId);
	public CustomerVo getByEmailAddress(String emailAddr);
	public List<CustomerVo> getFirst100();
	public int getCustomerCount(PagingCustVo vo);
	public List<CustomerVo> getCustomersWithPaging(PagingCustVo vo);
	public int update(CustomerVo customerVo);
	public int delete(String custId);
	public int deleteByEmailAddr(String emailAddr);
	public int insert(CustomerVo customerVo);
}
