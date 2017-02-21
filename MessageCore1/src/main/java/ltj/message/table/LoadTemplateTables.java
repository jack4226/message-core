package ltj.message.table;

import java.sql.Timestamp;

import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationContext;

import ltj.data.preload.ClientVariableEnum;
import ltj.data.preload.GlobalVariableEnum;
import ltj.message.constant.AddressType;
import ltj.message.constant.CodeType;
import ltj.message.constant.Constants;
import ltj.message.constant.CarrierCode;
import ltj.message.constant.StatusId;
import ltj.message.constant.VariableType;
import ltj.message.constant.VariableName;
import ltj.message.dao.emailaddr.EmailAddrDao;
import ltj.message.dao.template.BodyTemplateDao;
import ltj.message.dao.template.ClientVariableDao;
import ltj.message.dao.template.GlobalVariableDao;
import ltj.message.dao.template.MsgSourceDao;
import ltj.message.dao.template.SubjTemplateDao;
import ltj.message.dao.template.TemplateVariableDao;
import ltj.message.vo.emailaddr.EmailAddrVo;
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
		
		for (GlobalVariableEnum var : GlobalVariableEnum.values()) {
			GlobalVariableVo in = new GlobalVariableVo();
			if (StringUtils.isNotBlank(var.getVariableName())) {
				in.setVariableName(var.getVariableName());
			}
			else {
				in.setVariableName(var.name());
			}
			in.setStartTime(updtTime);
			in.setVariableValue(var.getDefaultValue());
			in.setVariableFormat(var.getVariableFormat());
			in.setVariableType(var.getVariableType().value());
			in.setStatusId(StatusId.ACTIVE.value());
			in.setAllowOverride(var.getAllowOverride().value());
			in.setRequired(Constants.N);
			globalVariableDao.insert(in);
		}
		
//		GlobalVariableVo in = new GlobalVariableVo();
//
//		in.setVariableName("CurrentDateTime");
//		in.setStartTime(updtTime);
//		in.setVariableValue(null);
//		in.setVariableFormat("yyyy-MM-dd HH:mm:ss");
//		in.setVariableType(VariableType.DATETIME.value());
//		in.setStatusId(StatusId.ACTIVE.value());
//		in.setAllowOverride(Constants.Y);
//		in.setRequired(Constants.N);
//		globalVariableDao.insert(in);
//
//		in.setVariableName("CurrentDate");
//		in.setStartTime(updtTime);
//		in.setVariableValue(null);
//		in.setVariableFormat("yyyy-MM-dd");
//		in.setVariableType(VariableType.DATETIME.value());
//		in.setStatusId(StatusId.ACTIVE.value());
//		in.setAllowOverride(Constants.Y);
//		in.setRequired(Constants.N);
//		globalVariableDao.insert(in);
//
//		in.setVariableName("CurrentTime");
//		in.setStartTime(updtTime);
//		in.setVariableValue(null);
//		in.setVariableFormat("hh:mm:ss a");
//		in.setVariableType(VariableType.DATETIME.value());
//		in.setStatusId(StatusId.ACTIVE.value());
//		in.setAllowOverride(Constants.Y);
//		in.setRequired(Constants.N);
//		globalVariableDao.insert(in);
//
//		// load default client id
//		in = new GlobalVariableVo();
//		
//		in.setVariableName(XHeaderName.CLIENT_ID.value());
//		in.setStartTime(updtTime);
//		in.setVariableValue(Constants.DEFAULT_CLIENTID);
//		in.setVariableFormat(null);
//		in.setVariableType(VariableType.X_HEADER.value());
//		in.setStatusId(StatusId.ACTIVE.value());
//		in.setAllowOverride(Constants.Y);
//		in.setRequired(Constants.N);
//		globalVariableDao.insert(in);
//		
//		in = new GlobalVariableVo();
//		in.setVariableName("PoweredBySignature");
//		in.setStartTime(updtTime);
//		in.setVariableValue(Constants.POWERED_BY_HTML_TAG);
//		in.setVariableFormat(null);
//		in.setVariableType(VariableType.TEXT.value());
//		in.setStatusId(StatusId.ACTIVE.value());
//		in.setAllowOverride(Constants.N);
//		in.setRequired(Constants.N);
//		globalVariableDao.insert(in);
		
		System.out.println("load() completed.");
	}
	
	void load(ClientVariableDao clientVariableDao) {
		Timestamp updtTime = new Timestamp(System.currentTimeMillis());
		
		for (ClientVariableEnum var : ClientVariableEnum.values()) {
			ClientVariableVo in = new ClientVariableVo();
			
			in.setClientId(Constants.DEFAULT_CLIENTID);
			in.setVariableName(var.name());
			in.setStartTime(updtTime);
			in.setVariableValue(var.getDefaultValue());
			in.setVariableFormat(var.getVariableFormat());
			in.setVariableType(var.getVariableType().value());
			in.setStatusId(StatusId.ACTIVE.value());
			in.setAllowOverride(var.getAllowOverride().value());
			in.setRequired(Constants.N);

			clientVariableDao.insert(in);
		}
		
//		ClientVariableVo in = new ClientVariableVo();
//		
//		in.setClientId(Constants.DEFAULT_CLIENTID);
//		in.setVariableName("CurrentDateTime");
//		in.setStartTime(updtTime);
//		in.setVariableValue(null);
//		in.setVariableFormat(null);
//		in.setVariableType(VariableType.DATETIME.value());
//		in.setStatusId(StatusId.ACTIVE.value());
//		in.setAllowOverride(Constants.Y);
//		in.setRequired(Constants.N);
//
//		clientVariableDao.insert(in);
//
//		in.setClientId(Constants.DEFAULT_CLIENTID);
//		in.setVariableName("CurrentDate");
//		in.setStartTime(updtTime);
//		in.setVariableValue(null);
//		in.setVariableFormat("yyyy-MM-dd");
//		in.setVariableType(VariableType.DATETIME.value());
//		in.setStatusId(StatusId.ACTIVE.value());
//		in.setAllowOverride(Constants.Y);
//		in.setRequired(Constants.N);
//
//		clientVariableDao.insert(in);
//
//		in.setClientId(Constants.DEFAULT_CLIENTID);
//		in.setVariableName("CurrentTime");
//		in.setStartTime(updtTime);
//		in.setVariableValue(null);
//		in.setVariableFormat("hh:mm:ss a");
//		in.setVariableType(VariableType.DATETIME.value());
//		in.setStatusId(StatusId.ACTIVE.value());
//		in.setAllowOverride(Constants.Y);
//		in.setRequired(Constants.N);
//
//		clientVariableDao.insert(in);

		System.out.println("load() completed.");
	}
	
	private static final String TestTemplateId_1 = "WeekendDeals";
	private static final String TestTemplateId_2 = "testTemplate";
	
	private static final String TestMsgSourceId_1 = "WeekendDeals";
	private static final String TestMsgSourceId_2 = "testMsgSource";
	
	void load(TemplateVariableDao templateVariableDao) {
		Timestamp updtTime = new Timestamp(System.currentTimeMillis());
		TemplateVariableVo in = new TemplateVariableVo();
		
		in.setTemplateId(TestTemplateId_1);
		in.setClientId(Constants.DEFAULT_CLIENTID);
		in.setVariableName(GlobalVariableEnum.CurrentDateTime.name());
		in.setStartTime(updtTime);
		in.setVariableValue(null);
		in.setVariableFormat(null);
		in.setVariableType(VariableType.DATETIME.value());
		in.setStatusId(StatusId.ACTIVE.value());
		in.setAllowOverride(Constants.Y);
		in.setRequired(Constants.N);

		templateVariableDao.insert(in);

		in.setTemplateId(TestTemplateId_1);
		in.setClientId(Constants.DEFAULT_CLIENTID);
		in.setVariableName(GlobalVariableEnum.CurrentDate.name());
		in.setStartTime(updtTime);
		in.setVariableValue(null);
		in.setVariableFormat("yyyy-MM-dd");
		in.setVariableType(VariableType.DATETIME.value());
		in.setStatusId(StatusId.ACTIVE.value());
		in.setAllowOverride(Constants.Y);
		in.setRequired(Constants.N);

		templateVariableDao.insert(in);

		System.out.println("load() completed.\n"+in);
	}
	
	void load(SubjTemplateDao subjTemplateDao) {
		Timestamp updtTime = new Timestamp(System.currentTimeMillis());
		SubjTemplateVo in = new SubjTemplateVo();
		
		in.setTemplateId(TestTemplateId_1);
		in.setClientId(Constants.DEFAULT_CLIENTID);
		in.setStartTime(updtTime);
		in.setDescription(null);
		in.setStatusId(StatusId.ACTIVE.value());
		in.setTemplateValue("Weekend Deals at MyBesyDeals.com");

		subjTemplateDao.insert(in);

		System.out.println("load() completed.\n"+in);
	}
	
	void load(BodyTemplateDao bodyTemplateDao) {
		Timestamp updtTime = new Timestamp(System.currentTimeMillis());
		BodyTemplateVo in = new BodyTemplateVo();
		
		in.setTemplateId(TestTemplateId_1);
		in.setClientId(Constants.DEFAULT_CLIENTID);
		in.setStartTime(updtTime);
		in.setDescription(null);
		in.setStatusId(StatusId.ACTIVE.value());
		in.setTemplateValue("Dear customer, here is a list of great deals on gardening tools provided to you by mydot.com.");
		in.setContentType("text/plain");

		bodyTemplateDao.insert(in);

		System.out.println("load() completed.\n"+in);
	}
	
	void load(MsgSourceDao msgSourceDao) {

		EmailAddrVo emailVo = getEmailAddrdao().findByAddress("jsmith@test.com");
		
		MsgSourceVo in = new MsgSourceVo();
		
		in.setMsgSourceId(TestMsgSourceId_1);
		in.setDescription("Default Message Source");
		in.setStatusId(StatusId.ACTIVE.value());
		in.setFromAddrId(emailVo.getEmailAddrId());
		in.setReplyToAddrId(null);
		in.setSubjTemplateId(TestTemplateId_1);
		in.setBodyTemplateId(TestTemplateId_1);
		in.setTemplateVariableId(TestTemplateId_1);
		in.setExcludingIdToken(Constants.N);
		in.setCarrierCode(CarrierCode.SMTPMAIL.value());
		in.setAllowOverride(Constants.Y);
		in.setSaveMsgStream(Constants.Y);
		in.setArchiveInd(Constants.N);
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
		
		in.setTemplateId(TestTemplateId_2);
		in.setClientId(Constants.DEFAULT_CLIENTID);
		in.setStartTime(updtTime);
		in.setDescription(null);
		in.setStatusId(StatusId.ACTIVE.value());
		in.setTemplateValue(template);
		in.setContentType("text/html");

		bodyTemplateDao.insert(in);

		System.out.println("load() completed.\n"+in);
	}
	
	void load2(MsgSourceDao msgSourceDao) {

		EmailAddrVo emailVo = getEmailAddrdao().findByAddress("jsmith@test.com");
		
		MsgSourceVo in = new MsgSourceVo();
		
		in.setMsgSourceId(TestMsgSourceId_2);
		in.setDescription("Test Message Source");
		in.setStatusId(StatusId.ACTIVE.value());
		in.setFromAddrId(emailVo.getEmailAddrId());
		in.setReplyToAddrId(null);
		in.setSubjTemplateId(TestTemplateId_1);
		in.setBodyTemplateId(TestTemplateId_2);
		in.setTemplateVariableId(TestTemplateId_1);
		in.setExcludingIdToken(Constants.N);
		in.setCarrierCode(CarrierCode.SMTPMAIL.value());
		in.setAllowOverride(Constants.Y);
		in.setSaveMsgStream(Constants.Y);
		in.setArchiveInd(Constants.N);
		in.setPurgeAfter(null);
		in.setUpdtUserId("testuser");

		msgSourceDao.insert(in);

		System.out.println("load() completed.\n"+in);
	}
	
	void load3(ClientVariableDao clientVariableDao) {
		Timestamp updtTime = new Timestamp(System.currentTimeMillis());
		ClientVariableVo in = new ClientVariableVo();
		
		in.setClientId(Constants.DEFAULT_CLIENTID);
		in.setVariableName(VariableName.CLIENT_ID.value());
		in.setStartTime(updtTime);
		in.setVariableValue(Constants.DEFAULT_CLIENTID);
		in.setVariableFormat(null);
		in.setVariableType(VariableType.TEXT.value());
		in.setStatusId(StatusId.ACTIVE.value());
		in.setAllowOverride(Constants.Y);
		in.setRequired(Constants.N);

		clientVariableDao.insert(in);

		System.out.println("load() completed.\n"+in);
	}
	
	void load4(TemplateVariableDao templateVariableDao) {
		Timestamp updtTime = new Timestamp(System.currentTimeMillis());
		TemplateVariableVo in = new TemplateVariableVo();
		
		in.setTemplateId(TestTemplateId_1);
		in.setClientId(Constants.DEFAULT_CLIENTID);
		in.setVariableName(VariableName.CUSTOMER_ID.value());
		in.setStartTime(updtTime);
		in.setVariableValue(null);
		in.setVariableFormat(null);
		in.setVariableType(VariableType.TEXT.value());
		in.setStatusId(StatusId.ACTIVE.value());
		in.setAllowOverride(Constants.Y);
		in.setRequired(Constants.Y);

		templateVariableDao.insert(in);

		System.out.println("load() completed.\n"+in);
	}
	
	void load5(GlobalVariableDao globalVariableDao) {
		Timestamp updtTime = new Timestamp(System.currentTimeMillis());
		GlobalVariableVo in = new GlobalVariableVo();
		
		in.setVariableName(AddressType.TO_ADDR.value());
		in.setStartTime(updtTime);
		in.setVariableValue(null);
		in.setVariableFormat(null);
		in.setVariableType(VariableType.ADDRESS.value());
		in.setStatusId(StatusId.ACTIVE.value());
		in.setAllowOverride(CodeType.MANDATORY.value());
		in.setRequired(Constants.Y);

		globalVariableDao.insert(in);

		System.out.println("load() completed.\n"+in);
	}
	
	private EmailAddrDao emailAddrDao = null;
	private EmailAddrDao getEmailAddrdao() {
		if (emailAddrDao == null) {
			emailAddrDao = SpringUtil.getDaoAppContext().getBean(EmailAddrDao.class);
		}
		return emailAddrDao;
	}
}