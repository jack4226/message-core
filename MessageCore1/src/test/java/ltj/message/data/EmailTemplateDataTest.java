package ltj.message.data;

import static org.junit.Assert.*;

import java.util.List;

import javax.annotation.Resource;

import org.junit.Test;

import ltj.message.bo.template.RenderUtil;
import ltj.message.dao.abstrct.DaoTestBase;
import ltj.message.dao.emailaddr.EmailTemplateDao;
import ltj.message.exception.DataValidationException;
import ltj.message.vo.emailaddr.EmailTemplateVo;

public class EmailTemplateDataTest extends DaoTestBase {

	@Resource
	private EmailTemplateDao emailTemplateDao;
	
	@Test
	public void checkVariableLoop() {
		List<EmailTemplateVo> list = emailTemplateDao.getAll();
		assertTrue(list.size() > 0);
		for (EmailTemplateVo vo : list) {
			try {
				RenderUtil.checkVariableLoop(vo.getBodyText());
			}
			catch (DataValidationException e) {
				fail("Variable loop found in body text");
			}
			try {
				RenderUtil.checkVariableLoop(vo.getSubject());
			}
			catch (DataValidationException e) {
				fail("Variable loop found in subject");
			}
		}
	}
	
}
