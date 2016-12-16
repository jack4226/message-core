package ltj.message.bo.test;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.junit.Test;

import ltj.message.bo.mailinglist.MailingListBo;
import ltj.message.exception.DataValidationException;
import ltj.message.exception.OutOfServiceException;
import ltj.message.exception.TemplateNotFoundException;

public class MailingListBoTest extends BoTestBase {
	static final Logger logger = Logger.getLogger(MailingListBoTest.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();
	@Resource
	private MailingListBo mlServiceBo;
	
	@Test
	public void testMailingListBo() {
		String mailingListId = "SMPLLST1";
		String testEmailAddr = "test@test.com";
		try {
			String templateId = "SampleNewsletter2";
			int rows = broadcast(templateId);
			assertTrue(rows==1);
			rows = sendMail(templateId);
			assertTrue(rows==1);
			rows = unSubscribe(testEmailAddr, mailingListId);
			assertTrue(rows==1);
			rows = subscribe(testEmailAddr, mailingListId);
			assertTrue(rows==1);
		}
		catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}
	
	// TODO verify results
	
	int broadcast(String templateId) throws OutOfServiceException, TemplateNotFoundException,
			DataValidationException {
		int mailsSent = mlServiceBo.broadcast(templateId);
		logger.info("Number of emails sent: " + mailsSent);
		return mailsSent;
	}

	int sendMail(String templateId) throws OutOfServiceException, TemplateNotFoundException,
			DataValidationException {
		String toAddr = "testto@localhost";
		Map<String, String> vars = new HashMap<String, String>();
		vars.put("CustomerName", "List Subscriber");
		int mailsSent = mlServiceBo.send(toAddr, vars, templateId);
		logger.info("Number of emails sent: " + mailsSent);
		return mailsSent;
	}

	int subscribe(String emailAddr, String listId) throws DataValidationException {
		int rows = mlServiceBo.subscribe(emailAddr, listId);
		logger.info("Number of rows added: " + rows);
		return rows;
	}

	int unSubscribe(String emailAddr, String listId) throws DataValidationException {
		int rows = mlServiceBo.unSubscribe(emailAddr, listId);
		logger.info("Number of rows removed: " + rows);
		return rows;
	}
}
