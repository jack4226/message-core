package ltj.message.bo;

import static org.junit.Assert.*;

import java.text.ParseException;

import javax.mail.internet.AddressException;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import ltj.message.bean.MessageBean;
import ltj.message.bo.outbox.MsgOutboxBo;
import ltj.message.bo.template.RenderBo;
import ltj.message.bo.template.RenderRequest;
import ltj.message.bo.template.RenderResponse;
import ltj.message.bo.test.BoTestBase;
import ltj.message.dao.outbox.MsgRenderedDao;
import ltj.message.dao.template.BodyTemplateDao;
import ltj.message.dao.template.SubjTemplateDao;
import ltj.message.exception.DataValidationException;
import ltj.vo.outbox.MsgRenderedVo;
import ltj.vo.template.BodyTemplateVo;
import ltj.vo.template.SubjTemplateVo;

public class MsgOutboxBoTest extends BoTestBase {
	static final Logger logger = Logger.getLogger(MsgOutboxBoTest.class);
	@Autowired
	private MsgOutboxBo msgOutboxBo;
	@Autowired
	private MsgRenderedDao renderedDao;
	@Autowired
	private SubjTemplateDao subjTmpltDao;
	@Autowired
	private BodyTemplateDao bodyTmpltDao;
	@Autowired
	private RenderBo renderBo;
	
	@Test
	public void testMsgRendering() {
		MsgRenderedVo mrvo = renderedDao.getRandomRecord();
		assertNotNull(mrvo);
		logger.info("MsgRenderedVo: " + LF + mrvo);
		try {
			SubjTemplateVo subjvo = subjTmpltDao.getByBestMatch(mrvo.getSubjTemplateId(), mrvo.getClientId(),
					mrvo.getStartTime());
			assertNotNull(subjvo);
			logger.info("SubjTemplateVo:" + LF + subjvo);
			BodyTemplateVo bodyvo = bodyTmpltDao.getByBestMatch(mrvo.getBodyTemplateId(), mrvo.getClientId(),
					mrvo.getStartTime());
			assertNotNull(bodyvo);
			logger.info("BodyTemplateVo:" + LF + bodyvo);
			
			RenderRequest req = msgOutboxBo.getRenderRequestByPK(mrvo.getRenderId());
			assertNotNull(req);
			logger.info("RenderRequest:" + LF + req);
			
			RenderResponse subjrsp = renderBo.getRenderedSubj(req);
			assertNotNull(subjrsp);
			logger.info("Subject Render Response:" + LF + subjrsp.getMessageBean());
			RenderResponse bodyrsp = renderBo.getRenderedBody(req);
			assertNotNull(bodyrsp);
			logger.info("Body Render Response:" + LF + bodyrsp.getMessageBean());
			
			MessageBean msgBean = msgOutboxBo.getMessageByPK(mrvo.getRenderId());
			assertNotNull(msgBean);
			logger.debug("MessageBean returned:" + LF + msgBean);
			
			assertEquals(subjrsp.getMessageBean().getSubject(), msgBean.getSubject());
			assertEquals(bodyrsp.getMessageBean().getBody(), msgBean.getBody());
		} 
		catch (AddressException | DataValidationException | ParseException e) {
			e.printStackTrace();
			fail();
		}
	}
}
