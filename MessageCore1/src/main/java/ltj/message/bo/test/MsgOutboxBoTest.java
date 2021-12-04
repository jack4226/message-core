package ltj.message.bo.test;

import static org.junit.Assert.*;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.springframework.test.annotation.Rollback;

import ltj.jbatch.queue.JmsProcessor;
import ltj.message.bean.MessageBean;
import ltj.message.bo.outbox.MsgOutboxBo;
import ltj.message.bo.template.RenderBo;
import ltj.message.bo.template.RenderRequest;
import ltj.message.bo.template.RenderResponse;
import ltj.message.bo.template.RenderVariable;
import ltj.message.bo.template.Renderer;
import ltj.message.constant.AddressType;
import ltj.message.constant.Constants;
import ltj.message.constant.VariableName;
import ltj.message.constant.VariableType;
import ltj.message.dao.inbox.MsgInboxDao;
import ltj.message.util.PrintUtil;
import ltj.message.vo.emailaddr.EmailAddressVo;
import ltj.message.vo.inbox.MsgInboxVo;

@FixMethodOrder
public class MsgOutboxBoTest extends BoTestBase {
	static final Logger logger = LogManager.getLogger(MsgOutboxBoTest.class);
	@Resource
	private MsgOutboxBo msgOutboxBo;
	@Resource
	private RenderBo renderBo;
	@Resource
	private JmsProcessor jmsProcessor;
	@Resource
	private MsgInboxDao inboxDao;
	
	private static MessageBean msgBean;
	
	@Test
	// must commit MsgRendered record for MailSender
	@Rollback(value=false)
	public void test1() { // testMailOutboxBo
		try {
			RenderRequest req = new RenderRequest(
					"testMsgSource",
					Constants.DEFAULT_CLIENTID,
					new Timestamp(new java.util.Date().getTime()),
					buildTestVariables()
					);
			RenderResponse rsp = renderBo.getRenderedEmail(req);
			assertNotNull(rsp);
			logger.info("Renderer Body: ####################" + LF + rsp.getMessageBean().getBody());
			
			long msgId = msgOutboxBo.saveRenderData(rsp);
			assertTrue(msgId>0);
			
			RenderRequest req2 = msgOutboxBo.getRenderRequestByPK(msgId);
			assertNotNull(req2);
			logger.info("RenderRequest2: ####################"+LF+req2);
			
			RenderResponse rsp2 = renderBo.getRenderedEmail(req2);
			assertNotNull(rsp2);
			logger.info("RenderResponse2: ####################"+LF+rsp2);
			
			Map<String, RenderVariable> req2Vars = req2.getVariableOverrides();
			
			Map<String, RenderVariable> rsp2Vars = rsp2.getVariableFinal();
			
			//assertTrue(rsp2.getVariableErrors().isEmpty());
			
			assertEquals(req2Vars.get("DomainName"), rsp2Vars.get("DomainName"));
			assertEquals(req2Vars.get("From"), rsp2Vars.get("From"));
			assertEquals(req2Vars.get("To"), rsp2Vars.get("To"));
			assertEquals(req2Vars.get("CustId"), rsp2Vars.get("CustId"));
			assertEquals(req2Vars.get("ClientId"), rsp2Vars.get("ClientId"));
			
			assertEquals(req2Vars.get("name1"), rsp2Vars.get("name1"));
			assertEquals(req2Vars.get("name2"), rsp2Vars.get("name2"));
			assertEquals(req2Vars.get("name3"), rsp2Vars.get("name3"));
			
			msgBean = rsp.getMessageBean();
			assertNotNull(msgBean);
			
			String body = msgBean.getBody();
			assertNotNull(body);
			String subj = msgBean.getSubject();
			assertNotNull(subj);
			msgBean.setSubject(subj + " - " + (new Random().nextInt(999) + 1));
			
			assertTrue(body.indexOf((String)rsp2Vars.get("name1").getVariableValue()) > 0 );
			assertTrue(body.indexOf((String)rsp2Vars.get("name2").getVariableValue()) > 0 );
			assertTrue(body.indexOf((String)rsp2Vars.get("name3").getVariableValue()) > 0 );
			
			jmsProcessor.setQueueName("mailSenderInput"); // TODO set queue name from property
			String msgWritten = jmsProcessor.writeMsg(rsp.getMessageBean());
			assertNotNull(msgWritten);
			logger.info("Message Written - JMS MessageId: " + msgWritten);
		}
		catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}
	
	@Test
	public void test2() { // wait for 5 seconds
		try {
			Thread.sleep(WaitTimeInMillis);
		} catch (InterruptedException e) {
			//
		}
	}

	@Test
	public void test3() { // verify results
		
		EmailAddressVo fromVo = selectEmailAddrByAddress(msgBean.getFromAsString());
		EmailAddressVo toVo = selectEmailAddrByAddress(msgBean.getToAsString());
		
		assertNotNull(fromVo);
		assertNotNull(toVo);
		
		List<MsgInboxVo> msgFromList = inboxDao.getByFromAddrId(fromVo.getEmailAddrId());
		assertTrue(msgFromList.size() > 0);
		
		List<MsgInboxVo> msgToList = inboxDao.getByToAddrId(toVo.getEmailAddrId());
		assertTrue(msgToList.size() > 0);
		
		boolean subjectFound = false;
		for (MsgInboxVo vo : msgFromList) {
			if (StringUtils.equals(msgBean.getSubject(), vo.getMsgSubject())) {
				subjectFound = true;
				logger.info("Subject matched: " + PrintUtil.prettyPrint(vo, 1));
				break;
			}
		}
		assertEquals(true, subjectFound);
	}
	
	
	private static Map<String, RenderVariable> buildTestVariables() {
		Map<String, RenderVariable> map=new HashMap<String, RenderVariable>();
		
		RenderVariable toAddr = new RenderVariable(
				AddressType.TO_ADDR.value(), 
				"testto@localhost",
				null, 
				VariableType.ADDRESS, 
				"Y",
				false, 
				null
			);
		map.put(toAddr.getVariableName(), toAddr);
		
		RenderVariable customer = new RenderVariable(
				VariableName.CUSTOMER_ID.value(), 
				"test",
				"maximum 16 characters", 
				VariableType.TEXT, 
				"Y",
				false, 
				null
			);
		map.put(customer.getVariableName(), customer);
		
		RenderVariable req1 = new RenderVariable(
				"name1", 
				"Jack Wang", 
				null, 
				VariableType.TEXT, 
				"Y",
				false, 
				null
			);
		RenderVariable req2 = new RenderVariable(
				"name2", 
				"Rendered User2", 
				null, 
				VariableType.TEXT, 
				"Y",
				false, 
				null
			);
		RenderVariable req3 = new RenderVariable(
				"name3", 
				"Rendered User3", 
				null, 
				VariableType.TEXT, 
				"Y",
				false, 
				null
			);
		RenderVariable req4 = new RenderVariable(
				"name4", 
				"Recursive VariableType ${name1} End", 
				null, 
				VariableType.TEXT, 
				"Y",
				false, 
				null
			);
		RenderVariable req5 = new RenderVariable(
				"name5", 
				"Rendered User5", 
				null, 
				VariableType.TEXT, 
				"Y",
				false, 
				null
			);
		
		RenderVariable req6_1 = new RenderVariable(
				"attachment1.txt", 
				"Attachment Text ============================================", 
				"text/plain; charset=\"iso-8859-1\"", 
				VariableType.LOB, 
				"Y",
				false, 
				null
			);
		
		ClassLoader loader = ClassLoader.getSystemClassLoader();
		java.net.URL url = loader.getResource("jndi.properties");
		try {
			Object content = url.getContent();
			byte[] buffer = null;
			if (content instanceof BufferedInputStream) {
				BufferedInputStream bis = (BufferedInputStream)content;
				int len = bis.available();
				buffer = new byte[len];
				bis.read(buffer);
				bis.close();
			}
			RenderVariable req6_2 = new RenderVariable(
					"jndi.bin",
					buffer,
					"application/octet-stream",
					VariableType.LOB, 
					"Y",
					false, 
					null
				);
			map.put("attachment2", req6_2);
		}
		catch (IOException e) {
			logger.error("IOException caught", e);
		}
		
		// build a Collection for Table
		RenderVariable req2_row1 = new RenderVariable(
				"name2", 
				"Rendered User2 - Row 1", 
				null, 
				VariableType.TEXT, 
				"Y",
				false, 
				null
			);
		RenderVariable req2_row2 = new RenderVariable(
				"name2", 
				"Rendered User2 - Row 2", 
				null, 
				VariableType.TEXT, 
				"Y",
				false, 
				null
			);
		List<Map<String, RenderVariable>> collection = new ArrayList<Map<String, RenderVariable>>();
		Map<String, RenderVariable> row1 = new HashMap<String, RenderVariable>();	// a row
		row1.put(req2.getVariableName(), req2_row1);
		row1.put(req3.getVariableName(), req3);
		collection.add(row1);
		Map<String, RenderVariable> row2 = new HashMap<String, RenderVariable>();	// a row
		row2.put(req2.getVariableName(), req2_row2);
		row2.put(req3.getVariableName(), req3);
		collection.add(row2);
		RenderVariable array = new RenderVariable(
				Renderer.TableVariableName, 
				collection, 
				null, 
				VariableType.COLLECTION, 
				"Y",
				false, 
				null
			);
		// end of Collection
		
		map.put("name1", req1);
		map.put("name2", req2);
		map.put("name3", req3);
		map.put("name4", req4);
		map.put("name5", req5);
		map.put("attachment1", req6_1);
		map.put(Renderer.TableVariableName, array);
		
		return map;
	}
}
