package ltj.message.bo.customer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import ltj.message.bo.mailinglist.MailingListBo;
import ltj.message.dao.emailaddr.MailingListDao;
import ltj.message.exception.DataValidationException;
import ltj.message.vo.CustomerVo;
import ltj.message.vo.emailaddr.MailingListVo;

@Service
@Component("customerSignupBo")
@Scope(value="prototype")
public class CustomerSignupBoImpl implements CustomerSignupBo {
	static final Logger logger = LogManager.getLogger(CustomerSignupBoImpl.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();
	
	@Autowired
	private MailingListDao mailingListDao;
	@Autowired
	private MailingListBo mailingListBo;
	@Autowired
	private CustomerBo customerBo;
	
	public int signUpOnly(CustomerVo vo) throws DataValidationException {
		if (isDebugEnabled) {
			logger.debug("signUpOnly() - Entering...");
		}
		// add customer to database
		int rowsAffected = insertOrUpdate(vo);
		return rowsAffected;
	}

	private int insertOrUpdate(CustomerVo vo) throws DataValidationException {
		int rowsInserted = 0;
		CustomerVo cust = customerBo.getByEmailAddr(vo.getEmailAddr());
		if (cust == null) {
			rowsInserted = customerBo.insert(vo);
			// race condition may occur, it should be vary rare.
		}
		else {
			vo.setRowId(cust.getRowId());
			vo.setCustId(cust.getCustId());
			customerBo.update(vo);
		}
		return rowsInserted;
	}

	public int signUpAndSubscribe(CustomerVo vo, String listId) throws DataValidationException {
		if (isDebugEnabled) {
			logger.debug("signUpAndSubscribe() - Entering..., listId: " + listId);
		}
		// validate list id
		MailingListVo listVo = mailingListDao.getByListId(listId);
		if (listVo == null) {
			throw new DataValidationException("Mailing List does not exist: " + listId);
		}
		// add customer to database
		int rowsAffected = insertOrUpdate(vo);
		mailingListBo.subscribe(vo.getEmailAddr(), listId);
		return rowsAffected;
	}

	public int addToList(String emailAddr, String listId) throws DataValidationException {
		if (isDebugEnabled) {
			logger.debug("addToList() - Entering..., emailAddr/listId: " + emailAddr + "/" + listId);
		}
		// validate list address
		MailingListVo listVo = mailingListDao.getByListId(listId);
		if (listVo == null) {
			throw new DataValidationException("Mailing List does not exist: " + listId);
		}
		// get customer from database
		CustomerVo vo = customerBo.getByEmailAddr(emailAddr);
		if (vo == null) {
			throw new DataValidationException("Could not find Customer by email address: " + emailAddr);
		}
		int emailsAdded = mailingListBo.subscribe(vo.getEmailAddr(), listId);
		return emailsAdded;
	}
	
	public int removeFromList(String emailAddr, String listId) throws DataValidationException {
		if (isDebugEnabled) {
			logger.debug("removeFromList() - Entering..., emailAddr/listId: " + emailAddr + "/" + listId);
		}
		// validate list address
		MailingListVo listVo = mailingListDao.getByListId(listId);
		if (listVo == null) {
			throw new DataValidationException("Mailing List does not exist: " + listId);
		}
		// get customer from database
		CustomerVo vo = customerBo.getByEmailAddr(emailAddr);
		if (vo == null) {
			throw new DataValidationException("Could not find Customer by email address: " + emailAddr);
		}
		int emailsAdded = mailingListBo.unSubscribe(vo.getEmailAddr(), listId);
		return emailsAdded;
	}

}
