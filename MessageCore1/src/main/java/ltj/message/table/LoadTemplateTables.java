package ltj.message.table;

import java.sql.Timestamp;

import org.springframework.context.ApplicationContext;

import ltj.message.constant.CarrierCode;
import ltj.message.constant.Constants;
import ltj.message.constant.EmailAddressType;
import ltj.message.constant.StatusIdCode;
import ltj.message.constant.VariableName;
import ltj.message.constant.VariableStatus;
import ltj.message.constant.VariableType;
import ltj.message.constant.XHeaderName;
import ltj.message.dao.template.BodyTemplateDao;
import ltj.message.dao.template.ClientVariableDao;
import ltj.message.dao.template.GlobalVariableDao;
import ltj.message.dao.template.MsgSourceDao;
import ltj.message.dao.template.SubjTemplateDao;
import ltj.message.dao.template.TemplateVariableDao;
import ltj.spring.util.SpringUtil;
import ltj.vo.template.BodyTemplateVo;
import ltj.vo.template.ClientVariableVo;
import ltj.vo.template.GlobalVariableVo;
import ltj.vo.template.MsgSourceVo;
import ltj.vo.template.SubjTemplateVo;
import ltj.vo.template.TemplateVariableVo;

public class LoadTemplateTables {
	//AbstractApplicationContext factory = null;
	public static void main(String[] args) {
		LoadTemplateTables loadInboxTables = new LoadTemplateTables();
		try {
			loadInboxTables.loadData();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		System.exit(0);
	}
	
	public void loadData() {
		loadData(null);
	}
	
	public void loadData(ApplicationContext factory) {
		if (factory == null) {
			factory = SpringUtil.getDaoAppContext();
		}
		GlobalVariableDao globalVariableDao = factory.getBean(GlobalVariableDao.class);
		ClientVariableDao clientVariableDao = factory.getBean(ClientVariableDao.class);
		TemplateVariableDao templateVariableDao = factory.getBean(TemplateVariableDao.class);
		SubjTemplateDao subjTemplateDao = factory.getBean(SubjTemplateDao.class);
		BodyTemplateDao bodyTemplateDao = factory.getBean(BodyTemplateDao.class);
		MsgSourceDao msgSourceDao = factory.getBean(MsgSourceDao.class);

		load(globalVariableDao);
		load5(globalVariableDao);
		load(clientVariableDao);
		load3(clientVariableDao);
		load(templateVariableDao);
		load4(templateVariableDao);
		load(subjTemplateDao);
		load(bodyTemplateDao);
		load1(bodyTemplateDao);
		load(msgSourceDao);
		load2(msgSourceDao);

	}
		
	void load(GlobalVariableDao globalVariableDao) {
		Timestamp updtTime = new Timestamp(System.currentTimeMillis());
		GlobalVariableVo in = new GlobalVariableVo();
		
		in.setVariableName("CurrentDateTime");
		in.setStartTime(updtTime);
		in.setVariableValue(null);
		in.setVariableFormat("yyyy-MM-dd HH:mm:ss");
		in.setVariableType(VariableType.DATETIME);
		in.setStatusId(StatusIdCode.ACTIVE);
		in.setAllowOverride(Constants.YES_CODE);
		in.setRequired(Constants.NO_CODE);
		globalVariableDao.insert(in);

		in.setVariableName("CurrentDate");
		in.setStartTime(updtTime);
		in.setVariableValue(null);
		in.setVariableFormat("yyyy-MM-dd");
		in.setVariableType(VariableType.DATETIME);
		in.setStatusId(StatusIdCode.ACTIVE);
		in.setAllowOverride(Constants.YES_CODE);
		in.setRequired(Constants.NO_CODE);
		globalVariableDao.insert(in);

		in.setVariableName("CurrentTime");
		in.setStartTime(updtTime);
		in.setVariableValue(null);
		in.setVariableFormat("hh:mm:ss a");
		in.setVariableType(VariableType.DATETIME);
		in.setStatusId(StatusIdCode.ACTIVE);
		in.setAllowOverride(Constants.YES_CODE);
		in.setRequired(Constants.NO_CODE);
		globalVariableDao.insert(in);

		// load default client id
		in = new GlobalVariableVo();
		
		in.setVariableName(XHeaderName.XHEADER_CLIENT_ID);
		in.setStartTime(updtTime);
		in.setVariableValue(Constants.DEFAULT_CLIENTID);
		in.setVariableFormat(null);
		in.setVariableType(VariableType.X_HEADER);
		in.setStatusId(StatusIdCode.ACTIVE);
		in.setAllowOverride(Constants.YES_CODE);
		in.setRequired(Constants.NO_CODE);
		globalVariableDao.insert(in);
		
		in = new GlobalVariableVo();
		in.setVariableName("PoweredBySignature");
		in.setStartTime(updtTime);
		in.setVariableValue(Constants.POWERED_BY_HTML_TAG);
		in.setVariableFormat(null);
		in.setVariableType(VariableType.TEXT);
		in.setStatusId(StatusIdCode.ACTIVE);
		in.setAllowOverride(Constants.NO_CODE);
		in.setRequired(Constants.NO_CODE);
		globalVariableDao.insert(in);
		
		System.out.println("load() completed.\n"+in);
	}
	
	void load(ClientVariableDao clientVariableDao) {
		Timestamp updtTime = new Timestamp(System.currentTimeMillis());
		ClientVariableVo in = new ClientVariableVo();
		
		in.setClientId(Constants.DEFAULT_CLIENTID);
		in.setVariableName("CurrentDateTime");
		in.setStartTime(updtTime);
		in.setVariableValue(null);
		in.setVariableFormat(null);
		in.setVariableType(VariableType.DATETIME);
		in.setStatusId(StatusIdCode.ACTIVE);
		in.setAllowOverride(Constants.YES_CODE);
		in.setRequired(Constants.NO_CODE);

		clientVariableDao.insert(in);

		in.setClientId(Constants.DEFAULT_CLIENTID);
		in.setVariableName("CurrentDate");
		in.setStartTime(updtTime);
		in.setVariableValue(null);
		in.setVariableFormat("yyyy-MM-dd");
		in.setVariableType(VariableType.DATETIME);
		in.setStatusId(StatusIdCode.ACTIVE);
		in.setAllowOverride(Constants.YES_CODE);
		in.setRequired(Constants.NO_CODE);

		clientVariableDao.insert(in);

		in.setClientId(Constants.DEFAULT_CLIENTID);
		in.setVariableName("CurrentTime");
		in.setStartTime(updtTime);
		in.setVariableValue(null);
		in.setVariableFormat("hh:mm:ss a");
		in.setVariableType(VariableType.DATETIME);
		in.setStatusId(StatusIdCode.ACTIVE);
		in.setAllowOverride(Constants.YES_CODE);
		in.setRequired(Constants.NO_CODE);

		clientVariableDao.insert(in);

		System.out.println("load() completed.\n"+in);
	}
	
	void load(TemplateVariableDao templateVariableDao) {
		Timestamp updtTime = new Timestamp(System.currentTimeMillis());
		TemplateVariableVo in = new TemplateVariableVo();
		
		in.setTemplateId("WeekendDeals");
		in.setClientId(Constants.DEFAULT_CLIENTID);
		in.setVariableName("CurrentDateTime");
		in.setStartTime(updtTime);
		in.setVariableValue(null);
		in.setVariableFormat(null);
		in.setVariableType(VariableType.DATETIME);
		in.setStatusId(StatusIdCode.ACTIVE);
		in.setAllowOverride(Constants.YES_CODE);
		in.setRequired(Constants.NO_CODE);

		templateVariableDao.insert(in);

		in.setTemplateId("WeekendDeals");
		in.setClientId(Constants.DEFAULT_CLIENTID);
		in.setVariableName("CurrentDate");
		in.setStartTime(updtTime);
		in.setVariableValue(null);
		in.setVariableFormat("yyyy-MM-dd");
		in.setVariableType(VariableType.DATETIME);
		in.setStatusId(StatusIdCode.ACTIVE);
		in.setAllowOverride(Constants.YES_CODE);
		in.setRequired(Constants.NO_CODE);

		templateVariableDao.insert(in);

		System.out.println("load() completed.\n"+in);
	}
	
	void load(SubjTemplateDao subjTemplateDao) {
		Timestamp updtTime = new Timestamp(System.currentTimeMillis());
		SubjTemplateVo in = new SubjTemplateVo();
		
		in.setTemplateId("WeekendDeals");
		in.setClientId(Constants.DEFAULT_CLIENTID);
		in.setStartTime(updtTime);
		in.setDescription(null);
		in.setStatusId(StatusIdCode.ACTIVE);
		in.setTemplateValue("Weekend Deals at MyBesyDeals.com");

		subjTemplateDao.insert(in);

		System.out.println("load() completed.\n"+in);
	}
	
	void load(BodyTemplateDao bodyTemplateDao) {
		Timestamp updtTime = new Timestamp(System.currentTimeMillis());
		BodyTemplateVo in = new BodyTemplateVo();
		
		in.setTemplateId("WeekendDeals");
		in.setClientId(Constants.DEFAULT_CLIENTID);
		in.setStartTime(updtTime);
		in.setDescription(null);
		in.setStatusId(StatusIdCode.ACTIVE);
		in.setTemplateValue("Dear customer, here is a list of great deals on gardening tools provided to you by mydot.com.");
		in.setContentType("text/plain");

		bodyTemplateDao.insert(in);

		System.out.println("load() completed.\n"+in);
	}
	
	void load(MsgSourceDao msgSourceDao) {
		//Timestamp updtTime = new Timestamp(System.currentTimeMillis());
		MsgSourceVo in = new MsgSourceVo();
		
		in.setMsgSourceId("WeekendDeals");
		in.setDescription("Default Message Source");
		in.setStatusId(StatusIdCode.ACTIVE);
		in.setFromAddrId(Long.valueOf(1));
		in.setReplyToAddrId(null);
		in.setSubjTemplateId("WeekendDeals");
		in.setBodyTemplateId("WeekendDeals");
		in.setTemplateVariableId("WeekendDeals");
		in.setExcludingIdToken(Constants.NO_CODE);
		in.setCarrierCode(CarrierCode.SMTPMAIL);
		in.setAllowOverride(Constants.YES_CODE);
		in.setSaveMsgStream(Constants.YES_CODE);
		in.setArchiveInd(Constants.NO_CODE);
		in.setPurgeAfter(null);
		in.setUpdtUserId("testuser");

		msgSourceDao.insert(in);

		System.out.println("load() completed.\n"+in);
	}
	
	void load1(BodyTemplateDao bodyTemplateDao) {
		Timestamp updtTime = new Timestamp(System.currentTimeMillis());
		BodyTemplateVo in = new BodyTemplateVo();
		
		String template = "BeginTemplate\n"
			+ "Current DateTime: ${CurrentDate}<br>\n"
			+ "${name1}${name2} Some Text ${name3}More Text<br>\n"
			+ "${TABLE_SECTION_BEGIN}TableRowBegin &lt;${name2}&gt; TableRowEnd<br>\n" 
			+ "${TABLE_SECTION_END}text<br>\n"
			+ "${OPTIONAL_SECTION_BEGIN}Level 1-1 ${name1}<br>\n"
			+ "${OPTIONAL_SECTION_BEGIN}Level 2-1<br>\n${OPTIONAL_SECTION_END}<br>\n"
			+ "${OPTIONAL_SECTION_BEGIN}Level 2-2${dropped}<br>\n${OPTIONAL_SECTION_END}"
			+ "${OPTIONAL_SECTION_BEGIN}Level 2-3${name2}<br>\n${OPTIONAL_SECTION_END}"
			+ "${OPTIONAL_SECTION_END}"
			+ "${OPTIONAL_SECTION_BEGIN}Level 1-2<br>\n${OPTIONAL_SECTION_END}"
			+ "${name4}<br>\n"
			+ "EndTemplate<br>\n";
		
		in.setTemplateId("testTemplate");
		in.setClientId(Constants.DEFAULT_CLIENTID);
		in.setStartTime(updtTime);
		in.setDescription(null);
		in.setStatusId(StatusIdCode.ACTIVE);
		in.setTemplateValue(template);
		in.setContentType("text/html");

		bodyTemplateDao.insert(in);

		System.out.println("load() completed.\n"+in);
	}
	
	void load2(MsgSourceDao msgSourceDao) {
		//Timestamp updtTime = new Timestamp(System.currentTimeMillis());
		MsgSourceVo in = new MsgSourceVo();
		
		in.setMsgSourceId("testMsgSource");
		in.setDescription("Message Source");
		in.setStatusId(StatusIdCode.ACTIVE);
		in.setFromAddrId(Long.valueOf(1));
		in.setReplyToAddrId(null);
		in.setSubjTemplateId("WeekendDeals");
		in.setBodyTemplateId("testTemplate");
		in.setTemplateVariableId("WeekendDeals");
		in.setExcludingIdToken(Constants.NO_CODE);
		in.setCarrierCode(CarrierCode.SMTPMAIL);
		in.setAllowOverride(Constants.YES_CODE);
		in.setSaveMsgStream(Constants.YES_CODE);
		in.setArchiveInd(Constants.NO_CODE);
		in.setPurgeAfter(null);
		in.setUpdtUserId("testuser");

		msgSourceDao.insert(in);

		System.out.println("load() completed.\n"+in);
	}
	
	void load3(ClientVariableDao clientVariableDao) {
		Timestamp updtTime = new Timestamp(System.currentTimeMillis());
		ClientVariableVo in = new ClientVariableVo();
		
		in.setClientId(Constants.DEFAULT_CLIENTID);
		in.setVariableName(VariableName.CLIENT_ID);
		in.setStartTime(updtTime);
		in.setVariableValue(Constants.DEFAULT_CLIENTID);
		in.setVariableFormat(null);
		in.setVariableType(VariableType.TEXT);
		in.setStatusId(StatusIdCode.ACTIVE);
		in.setAllowOverride(Constants.YES_CODE);
		in.setRequired(Constants.NO_CODE);

		clientVariableDao.insert(in);

		System.out.println("load() completed.\n"+in);
	}
	
	void load4(TemplateVariableDao templateVariableDao) {
		Timestamp updtTime = new Timestamp(System.currentTimeMillis());
		TemplateVariableVo in = new TemplateVariableVo();
		
		in.setTemplateId("WeekendDeals");
		in.setClientId(Constants.DEFAULT_CLIENTID);
		in.setVariableName(VariableName.CUSTOMER_ID);
		in.setStartTime(updtTime);
		in.setVariableValue(null);
		in.setVariableFormat(null);
		in.setVariableType(VariableType.TEXT);
		in.setStatusId(StatusIdCode.ACTIVE);
		in.setAllowOverride(Constants.YES_CODE);
		in.setRequired(Constants.YES_CODE);

		templateVariableDao.insert(in);

		System.out.println("load() completed.\n"+in);
	}
	
	void load5(GlobalVariableDao globalVariableDao) {
		Timestamp updtTime = new Timestamp(System.currentTimeMillis());
		GlobalVariableVo in = new GlobalVariableVo();
		
		in.setVariableName(EmailAddressType.TO_ADDR);
		in.setStartTime(updtTime);
		in.setVariableValue(null);
		in.setVariableFormat(null);
		in.setVariableType(VariableType.ADDRESS);
		in.setStatusId(StatusIdCode.ACTIVE);
		in.setAllowOverride(VariableStatus.MANDATORY);
		in.setRequired(Constants.YES_CODE);

		globalVariableDao.insert(in);

		System.out.println("load() completed.\n"+in);
	}
}