package ltj.message.bo.customer;

import ltj.message.exception.DataValidationException;
import ltj.message.vo.CustomerVo;

public interface CustomerSignupBo {

	public int signUpOnly(CustomerVo vo) throws DataValidationException;
	
	public int signUpAndSubscribe(CustomerVo vo, String listId) throws DataValidationException;
	
	public int addToList(String emailAddr, String listId) throws DataValidationException;
	
	public int removeFromList(String emailAddr, String listId) throws DataValidationException;
}
