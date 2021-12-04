package ltj.message.bo;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;
import javax.mail.internet.InternetAddress;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

import ltj.message.bean.MessageBean;
import ltj.message.bean.MsgHeader;
import ltj.message.bo.inbox.MessageParser;
import ltj.message.bo.inbox.MsgInboxBo;
import ltj.message.bo.outbox.MsgOutboxBo;
import ltj.message.bo.test.BoTestBase;
import ltj.message.dao.idtokens.EmailIdParser;
import ltj.message.dao.outbox.MsgRenderedDao;
import ltj.message.vo.inbox.MsgHeaderVo;
import ltj.message.vo.inbox.MsgInboxVo;
import ltj.vo.outbox.MsgRenderedVo;

public class MsgInboxBoTest extends BoTestBase {
	static final Logger logger = LogManager.getLogger(MsgInboxBoTest.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();
	final static String LF = System.getProperty("line.separator","\n");
	@Resource
	private MsgInboxBo msgInboxBo;
	@Resource
	private MsgOutboxBo msgOutboxBo;
	@Resource
	private MessageParser parser;
	
	@Resource
	private MsgRenderedDao renderedDao;
	
	@Test
	public void testMsgInboxBo() {
		try {
			MsgRenderedVo renderedVo = renderedDao.getRandomRecord();
			assertNotNull(renderedVo);
			long msgId = renderedVo.getRenderId();
			MessageBean messageBean = msgOutboxBo.getMessageByPK(msgId);
			assertNotNull(messageBean);
			if (isDebugEnabled) {
				logger.debug("MessageBean returned:" + LF + messageBean);
			}
			String ruleName = parser.parse(messageBean);
			
			// build MsgHeader
			MsgHeader header = new MsgHeader();
			String hdrName = EmailIdParser.getDefaultParser().getEmailIdXHdrName();
			String hdrValue = EmailIdParser.getDefaultParser().wrapupEmailId4XHdr(msgId);
			if (StringUtils.isBlank(hdrName)) {
				hdrName = "MsgInboxBoTest";
			}
			if (StringUtils.isBlank(hdrValue)) {
				hdrValue = "MsgInboxBoTest Test Value";
			}
			header.setName(hdrName);
			header.setValue(hdrValue);
			List<MsgHeader> headers = new ArrayList<MsgHeader>();
			headers.add(header);
			messageBean.setHeaders(headers);
			
			if (isDebugEnabled) {
				logger.debug("MessageBean After:" + LF + messageBean);
			}
			messageBean.setRuleName(ruleName);
			if (messageBean.getFrom() == null) {
				messageBean.setFrom(InternetAddress.parse("testfrom@localhost"));
			}
			if (messageBean.getTo() == null) {
				messageBean.setTo(InternetAddress.parse("testto@localhost"));
			}
			msgId = msgInboxBo.saveMessage(messageBean);
			logger.info("msgInboxBo.saveMessage - MsgId returned: " + msgId);
			
			// verify results
			MsgInboxVo vo = msgInboxBo.getAllDataByMsgId(msgId);
			assertNotNull(vo);
			assertEquals(ruleName, vo.getRuleName());
			
			logger.info("MsgInboxVo.toString() - " + LF + vo);
			List<MsgHeaderVo> hdrVoList = vo.getMsgHeaders();
			boolean foundMatchingHeader = false;
			for (MsgHeaderVo hdrVo : hdrVoList) {
				if (StringUtils.equals(hdrName, hdrVo.getHeaderName())) {
					assertEquals(hdrValue, hdrVo.getHeaderValue());
					foundMatchingHeader = true;
				}
			}
			assertEquals(true, foundMatchingHeader);
		}
		catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}
}
