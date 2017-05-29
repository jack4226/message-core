package ltj.message.bo.customer;

import ltj.message.exception.DataValidationException;
import ltj.message.vo.CustomerVo;

public interface CustomerBo {
	public static int CUSTOMER_ID_MAX_LEN = 16;
	
	public int insert(CustomerVo vo) throws DataValidationException;
	
	public int update(CustomerVo vo) throws DataValidationException;
	
	public int delete(String custId) throws DataValidationException;
	
	public int deleteByEmailAddr(String emailAddr) throws DataValidationException;
	
	public CustomerVo getByCustId(String custId) throws DataValidationException;
	
	public CustomerVo getByEmailAddr(String emailAddr) throws DataValidationException;
}
