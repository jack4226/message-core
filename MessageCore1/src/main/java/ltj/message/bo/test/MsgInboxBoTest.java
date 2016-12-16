package ltj.message.bo.test;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.junit.Test;

import ltj.message.bean.MessageBean;
import ltj.message.bean.MsgHeader;
import ltj.message.bo.inbox.MessageParser;
import ltj.message.bo.inbox.MsgInboxBo;
import ltj.message.bo.outbox.MsgOutboxBo;
import ltj.message.dao.idtokens.EmailIdParser;
import ltj.message.vo.inbox.MsgInboxVo;

public class MsgInboxBoTest extends BoTestBase {
	static final Logger logger = Logger.getLogger(MsgInboxBoTest.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();
	final static String LF = System.getProperty("line.separator","\n");
	@Resource
	private MsgInboxBo msgInboxBo;
	@Resource
	private MsgOutboxBo msgOutboxBo;
	@Resource
	private MessageParser parser;
	
	@Test
	public void testMsgInboxBo() {
		long msgId = 1L;
		try {
			MessageBean messageBean = msgOutboxBo.getMessageByPK(msgId);
			assertNotNull(messageBean);
			if (isDebugEnabled) {
				logger.debug("MessageBean returned:" + LF + messageBean);
			}
			String ruleName = parser.parse(messageBean);
			
			// build MsgHeader
			MsgHeader header = new MsgHeader();
			header.setName(EmailIdParser.getDefaultParser().getEmailIdXHdrName());
			header.setValue(EmailIdParser.getDefaultParser().wrapupEmailId4XHdr(msgId));
			List<MsgHeader> headers = new ArrayList<MsgHeader>();
			headers.add(header);
			messageBean.setHeaders(headers);
			
			if (isDebugEnabled) {
				logger.debug("MessageBean After:" + LF + messageBean);
			}
			messageBean.setRuleName(ruleName);
			msgId = msgInboxBo.saveMessage(messageBean);
			logger.info("msgInboxBo.saveMessage - MsgId returned: " + msgId);
			MsgInboxVo vo = msgInboxBo.getAllDataByMsgId(msgId);
			assertNotNull(vo);
			assertEquals(ruleName, vo.getRuleName());
		}
		catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}
}
