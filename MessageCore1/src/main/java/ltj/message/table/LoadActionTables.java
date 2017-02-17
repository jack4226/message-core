package ltj.message.table;

import java.sql.Timestamp;

import org.springframework.context.ApplicationContext;

import ltj.data.preload.EmailTemplateEnum;
import ltj.data.preload.MailingListEnum;
import ltj.data.preload.QueueNameEnum;
import ltj.data.preload.RuleActionDetailEnum;
import ltj.data.preload.RuleActionEnum;
import ltj.data.preload.RuleDataTypeEnum;
import ltj.data.preload.RuleNameEnum;
import ltj.message.constant.AddressType;
import ltj.message.constant.StatusIdCode;
import ltj.message.constant.TableColumnName;
import ltj.message.dao.action.MsgActionDao;
import ltj.message.dao.action.MsgActionDetailDao;
import ltj.message.dao.action.MsgDataTypeDao;
import ltj.message.vo.action.MsgActionDetailVo;
import ltj.message.vo.action.MsgActionVo;
import ltj.message.vo.action.MsgDataTypeVo;
import ltj.spring.util.SpringUtil;

public class LoadActionTables {
	String LF = System.getProperty("line.separator", "\n");
	
	public static void main(String[] args) {
		LoadActionTables loadActionTables = new LoadActionTables();
		try {
			loadActionTables.loadData();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		System.exit(0);
	}
	
	public void loadData() {
		loadData(null);
	}
	
	public void loadData(ApplicationContext factory)  {
		if (factory == null) {
			factory = SpringUtil.getDaoAppContext();
		}
		MsgDataTypeDao msgDataTypeDao = factory.getBean(MsgDataTypeDao.class);
		MsgActionDao msgActionDao = factory.getBean(MsgActionDao.class);
		MsgActionDetailDao msgActionDetailDao = factory.getBean(MsgActionDetailDao.class);
		load(msgDataTypeDao);
		load(msgActionDetailDao);
		load(msgActionDao);
	}
	
	void load(MsgDataTypeDao msgDataTypeDao) {
		String jndiProperties = 
			"java.naming.factory.initial=org.jnp.interfaces.NamingContextFactory" + LF +
			"java.naming.provider.url=jnp:////localhost:2099" + LF +
			"java.naming.factory.url.pkgs=org.jboss.naming:org.jnp.interfaces";
		
		
		for (RuleDataTypeEnum type : RuleDataTypeEnum.values()) {
			MsgDataTypeVo vo = null;
			if (RuleDataTypeEnum.EMAIL_ADDRESS.equals(type)) {
				// insert email address values
				for (AddressType addrType : AddressType.values()) {
					vo = new MsgDataTypeVo(RuleDataTypeEnum.EMAIL_ADDRESS.name(), "$" + addrType.value(), "MessageBean");
					msgDataTypeDao.insert(vo);
				}
				// insert column names storing email address
				for (String columnName : TableColumnName.CLIENT_TABLE_EMAIL_COLUMNS) {
					vo = new MsgDataTypeVo(RuleDataTypeEnum.EMAIL_ADDRESS.name(), "$" + columnName, "clientDao");
					msgDataTypeDao.insert(vo);
				}
			}
			else if (RuleDataTypeEnum.QUEUE_NAME.equals(type)) {
				for (QueueNameEnum queue : QueueNameEnum.values()) {
					vo = new MsgDataTypeVo(RuleDataTypeEnum.QUEUE_NAME.name(), "$" + queue.name(), queue.getQueueName());
					msgDataTypeDao.insert(vo);
				}
			}
			else if (RuleDataTypeEnum.TEMPLATE_ID.equals(type)) {
				for (EmailTemplateEnum tmp : EmailTemplateEnum.values()) {
					if (EmailTemplateEnum.SubscribeByEmailReply.equals(tmp)) {
						vo = new MsgDataTypeVo(RuleDataTypeEnum.TEMPLATE_ID.name(), tmp.name(), jndiProperties);
					}
					else {
						vo = new MsgDataTypeVo(RuleDataTypeEnum.TEMPLATE_ID.name(), tmp.name(), null);
					}
					msgDataTypeDao.insert(vo);
				}
			}
			else if (RuleDataTypeEnum.RULE_NAME.equals(type)) {
				for (RuleNameEnum ruleName : RuleNameEnum.values()) {
					if (RuleNameEnum.GENERIC.equals(ruleName)) {
						continue; // skip GENERIC
					}
					vo = new MsgDataTypeVo(RuleDataTypeEnum.RULE_NAME.name(), ruleName.name(), ruleName.getValue());
					msgDataTypeDao.insert(vo);
				}
			}
			else if (RuleDataTypeEnum.MAILING_LIST.equals(type)) {
				for (MailingListEnum list : MailingListEnum.values()) {
					vo = new MsgDataTypeVo(RuleDataTypeEnum.MAILING_LIST.name(), "$" + list.name(),list.getAcctName());
					msgDataTypeDao.insert(vo);
				}
			}
		}

//		MsgDataTypeVo vo = null;
//		vo = new MsgDataTypeVo("EMAIL_ADDRESS", "$" + AddressType.FROM_ADDR.value(), "MessageBean");
//		msgDataTypeDao.insert(vo);
//		vo = new MsgDataTypeVo("EMAIL_ADDRESS", "$" + AddressType.TO_ADDR.value(), "MessageBean");
//		msgDataTypeDao.insert(vo);
//		vo = new MsgDataTypeVo("EMAIL_ADDRESS", "$" + AddressType.CC_ADDR.value(), "MessageBean");
//		msgDataTypeDao.insert(vo);
//		vo = new MsgDataTypeVo("EMAIL_ADDRESS", "$" + AddressType.BCC_ADDR.value(), "MessageBean");
//		msgDataTypeDao.insert(vo);
//		vo = new MsgDataTypeVo("EMAIL_ADDRESS", "$" + AddressType.FINAL_RCPT_ADDR.value(), "MessageBean");
//		msgDataTypeDao.insert(vo);
//		vo = new MsgDataTypeVo("EMAIL_ADDRESS", "$" + AddressType.ORIG_RCPT_ADDR.value(), "MessageBean");
//		msgDataTypeDao.insert(vo);
//		vo = new MsgDataTypeVo("EMAIL_ADDRESS", "$" + AddressType.FORWARD_ADDR.value(), "MessageBean");
//		msgDataTypeDao.insert(vo);
//		vo = new MsgDataTypeVo("EMAIL_ADDRESS", "$" + TableColumnName.SECURITY_DEPT_ADDR, "clientDao");
//		msgDataTypeDao.insert(vo);
//		vo = new MsgDataTypeVo("EMAIL_ADDRESS", "$" + TableColumnName.CUSTOMER_CARE_ADDR, "clientDao");
//		msgDataTypeDao.insert(vo);
//		vo = new MsgDataTypeVo("EMAIL_ADDRESS", "$" + TableColumnName.RMA_DEPT_ADDR, "clientDao");
//		msgDataTypeDao.insert(vo);
//		vo = new MsgDataTypeVo("EMAIL_ADDRESS", "$" + TableColumnName.VIRUS_CONTROL_ADDR, "clientDao");
//		msgDataTypeDao.insert(vo);
//		vo = new MsgDataTypeVo("EMAIL_ADDRESS", "$" + TableColumnName.SPAM_CONTROL_ADDR, "clientDao");
//		msgDataTypeDao.insert(vo);
//		vo = new MsgDataTypeVo("EMAIL_ADDRESS", "$" + TableColumnName.CHALLENGE_HANDLER_ADDR, "clientDao");
//		msgDataTypeDao.insert(vo);
//		vo = new MsgDataTypeVo("QUEUE_NAME", "$RMA_REQUEST_INPUT", "rmaRequestInput");
//		msgDataTypeDao.insert(vo);
//		vo = new MsgDataTypeVo("QUEUE_NAME", "$CUSTOMER_CARE_INPUT", "customerCareInput");
//		msgDataTypeDao.insert(vo);
//		vo = new MsgDataTypeVo(MsgDataType.TEMPLATE_ID, "SubscribeByEmailReply", jndiProperties);
//		msgDataTypeDao.insert(vo);
		
		// insert rule names
//		for (RuleNameType name : RuleNameType.values()) {
//			if (RuleNameType.GENERIC.equals(name)) {
//				continue; // skip GENERIC
//			}
//			String ruleName = name.toString();
//			vo = new MsgDataTypeVo("RULE_NAME", ruleName, null);
//			msgDataTypeDao.insert(vo);
//		}
		
		System.out.println("load() completed.");
	}
	
	void load(MsgActionDetailDao msgActionDetailDao) {
		Timestamp updtTime = new Timestamp(System.currentTimeMillis());
		
		for (RuleActionDetailEnum act : RuleActionDetailEnum.values()) {
			String dataType = act.getDataType() == null ? null : act.getDataType().name();
			MsgActionDetailVo vo = new MsgActionDetailVo(act.name(), act.getDescription(), act.getServiceName(),
					act.getClassName(), dataType, updtTime, "testuser");
			msgActionDetailDao.insert(vo);
		}
//		MsgActionDetailVo act = null;
//		act = new MsgActionDetailVo("ACTIVATE","activete email address","activateBo",null,"EMAIL_ADDRESS",updtTime,"testuser");
//		msgActionDetailDao.insert(act);
//		act = new MsgActionDetailVo("BOUNCE_UP","increase bounce count","bounceBo",null,"EMAIL_ADDRESS",updtTime,"testuser");
//		msgActionDetailDao.insert(act);
//		act = new MsgActionDetailVo("CLOSE","close the message","closeBo",null,null,updtTime,"testuser");
//		msgActionDetailDao.insert(act);
//		act = new MsgActionDetailVo("CSR_REPLY","send off the reply from csr","csrReplyBo",null,null,updtTime,"testuser");
//		msgActionDetailDao.insert(act);
//		act = new MsgActionDetailVo("AUTO_REPLY","reply to the message automatically","autoReplyBo",null,MsgDataType.TEMPLATE_ID,updtTime,"testuser");
//		msgActionDetailDao.insert(act);
//		act = new MsgActionDetailVo("MARK_DLVR_ERR","mark delivery error","deliveryErrorBo",null,null,updtTime,"testuser");
//		msgActionDetailDao.insert(act);
//		act = new MsgActionDetailVo("DROP","drop the message","dropBo","ltj.message.bo.DropBoImpl",null,updtTime,"testuser");
//		msgActionDetailDao.insert(act);
//		act = new MsgActionDetailVo("FORWARD","forward the message","forwardBo",null,"EMAIL_ADDRESS",updtTime,"testuser");
//		msgActionDetailDao.insert(act);
//		act = new MsgActionDetailVo("TO_CSR","redirect to message queue","toCsrBo",null,"QUEUE_NAME",updtTime,"testuser");
//		msgActionDetailDao.insert(act);
//		act = new MsgActionDetailVo("SAVE","save the message","saveBo",null,null,updtTime,"testuser");
//		msgActionDetailDao.insert(act);
//		act = new MsgActionDetailVo("SENDMAIL","simply send the mail off","sendMailBo",null,"EMAIL_ADDRESS",updtTime,"testuser");
//		msgActionDetailDao.insert(act);
//		act = new MsgActionDetailVo("SUSPEND","suspend email address","suspendBo",null,"EMAIL_ADDRESS",updtTime,"testuser");
//		msgActionDetailDao.insert(act);
//		act = new MsgActionDetailVo("UNSUBSCRIBE","remove from the mailing list","unsubscribeBo",null,"EMAIL_ADDRESS",updtTime,"testuser");
//		msgActionDetailDao.insert(act);
//		act = new MsgActionDetailVo("SUBSCRIBE","subscribe to the mailing list","subscribeBo",null,"EMAIL_ADDRESS",updtTime,"testuser");
//		msgActionDetailDao.insert(act);
//		act = new MsgActionDetailVo("BROADCAST","broadcast to a mailing list","broadcastBo",null,null,updtTime,"testuser");
//		msgActionDetailDao.insert(act);
//		act = new MsgActionDetailVo("ASSIGN_RULENAME","set a rule mame and re-queue","assignRuleNameBo",null,"RULE_NAME",updtTime,"testuser");
//		msgActionDetailDao.insert(act);
//		act = new MsgActionDetailVo("OPEN","open the message","openBo",null,null,updtTime,"testuser");
//		msgActionDetailDao.insert(act);

		System.out.println("load() completed.");
	}
	
	void load(MsgActionDao msgActionDao) {
		Timestamp now = new Timestamp(System.currentTimeMillis());
		
		for (RuleActionEnum act : RuleActionEnum.values()) {
			String ruleName = act.getRuleName().name();
			MsgActionVo vo = new MsgActionVo(ruleName, act.getSequence(), now, null,
					act.getActionDetail().name(), StatusIdCode.ACTIVE, act.getFieldValues());
			msgActionDao.insert(vo);
		}
		
//		MsgActionVo act = null;
//		// for build-in rules
//		act = new MsgActionVo(RuleNameType.HARD_BOUNCE.name(),1,now,null,"SAVE","A",null);
//		msgActionDao.insert(act);
//		act = new MsgActionVo(RuleNameType.HARD_BOUNCE.name(),2,now,null,"SUSPEND","A","$"+AddressType.FINAL_RCPT_ADDR.value()+","+"$"+AddressType.ORIG_RCPT_ADDR.value());
//		msgActionDao.insert(act);
//		act = new MsgActionVo(RuleNameType.HARD_BOUNCE.name(),3,now,null,"MARK_DLVR_ERR","A",null);
//		msgActionDao.insert(act);
//		act = new MsgActionVo(RuleNameType.HARD_BOUNCE.name(),4,now,null,"CLOSE","A",null);
//		msgActionDao.insert(act);
//		act = new MsgActionVo(RuleNameType.SOFT_BOUNCE.name(),1,now,null,"SAVE","A",null);
//		msgActionDao.insert(act);
//		act = new MsgActionVo(RuleNameType.SOFT_BOUNCE.name(),2,now,null,"BOUNCE_UP","A","$"+AddressType.FINAL_RCPT_ADDR.value()+","+"$"+AddressType.ORIG_RCPT_ADDR.value());
//		msgActionDao.insert(act);
//		act = new MsgActionVo(RuleNameType.SOFT_BOUNCE.name(),3,now,null,"CLOSE","A",null);
//		msgActionDao.insert(act);
//		act = new MsgActionVo(RuleNameType.MAILBOX_FULL.name(),1,now,null,"SAVE","A",null);
//		msgActionDao.insert(act);
//		act = new MsgActionVo(RuleNameType.MAILBOX_FULL.name(),2,now,null,"BOUNCE_UP","A","$"+AddressType.FINAL_RCPT_ADDR.value()+","+"$"+AddressType.ORIG_RCPT_ADDR.value());
//		msgActionDao.insert(act);
//		act = new MsgActionVo(RuleNameType.MAILBOX_FULL.name(),3,now,null,"CLOSE","A",null);
//		msgActionDao.insert(act);
//		act = new MsgActionVo(RuleNameType.MSGSIZE_TOO_BIG.toString(),1,now,null,"SAVE","A",null);
//		msgActionDao.insert(act);
//		act = new MsgActionVo(RuleNameType.MSGSIZE_TOO_BIG.toString(),2,now,null,"TO_CSR","A","$CUSTOMER_CARE_INPUT");
//		msgActionDao.insert(act);
//		act = new MsgActionVo(RuleNameType.MAIL_BLOCK.toString(),1,now,null,"SAVE","A",null);
//		msgActionDao.insert(act);
//		act = new MsgActionVo(RuleNameType.MAIL_BLOCK.toString(),2,now,null,"FORWARD","A","$" + TableColumnName.SPAM_CONTROL_ADDR);
//		msgActionDao.insert(act);
//		act = new MsgActionVo(RuleNameType.SPAM_BLOCK.toString(),1,now,null,"SAVE","A",null);
//		msgActionDao.insert(act);
//		act = new MsgActionVo(RuleNameType.SPAM_BLOCK.toString(),2,now,null,"FORWARD","A","$"+TableColumnName.SPAM_CONTROL_ADDR);
//		msgActionDao.insert(act);
//		act = new MsgActionVo(RuleNameType.VIRUS_BLOCK.name(),1,now,null,"SAVE","A",null);
//		msgActionDao.insert(act);
//		act = new MsgActionVo(RuleNameType.VIRUS_BLOCK.name(),2,now,null,"FORWARD","A","$"+TableColumnName.VIRUS_CONTROL_ADDR);
//		msgActionDao.insert(act);
//		act = new MsgActionVo(RuleNameType.CHALLENGE_RESPONSE.toString(),1,now,null,"FORWARD","A","$"+TableColumnName.CHALLENGE_HANDLER_ADDR);
//		msgActionDao.insert(act);
//		act = new MsgActionVo(RuleNameType.AUTO_REPLY.toString(),1,now,null,"SAVE","A",null);
//		msgActionDao.insert(act);
//		act = new MsgActionVo(RuleNameType.AUTO_REPLY.toString(),2,now,null,"CLOSE","A",null);
//		msgActionDao.insert(act);
//		act = new MsgActionVo(RuleNameType.CC_USER.name(),1,now,null,"DROP","A",null);
//		msgActionDao.insert(act);
//		act = new MsgActionVo(RuleNameType.MDN_RECEIPT.name(),1,now,null,"ACTIVATE","A","$"+AddressType.FINAL_RCPT_ADDR.value()+","+"$"+AddressType.ORIG_RCPT_ADDR.value());
//		msgActionDao.insert(act);
//		act = new MsgActionVo(RuleNameType.MDN_RECEIPT.name(),2,now,null,"DROP","A",null);
//		msgActionDao.insert(act);
//		act = new MsgActionVo(RuleNameType.CSR_REPLY.toString(),1,now,null,"CSR_REPLY","A",null);
//		msgActionDao.insert(act);
//		act = new MsgActionVo(RuleNameType.SEND_MAIL.name(),1,now,null,"SENDMAIL","A","$"+AddressType.TO_ADDR.value());
//		msgActionDao.insert(act);
//		act = new MsgActionVo(RuleNameType.RMA_REQUEST.toString(),1,now,null,"SAVE","A",null);
//		msgActionDao.insert(act);
//		act = new MsgActionVo(RuleNameType.RMA_REQUEST.toString(),2,now,null,"ACTIVATE","A","$"+AddressType.FROM_ADDR.value());
//		msgActionDao.insert(act);
//		act = new MsgActionVo(RuleNameType.RMA_REQUEST.toString(),3,now,null,"TO_CSR","A","$RMA_REQUEST_INPUT");
//		msgActionDao.insert(act);
//		act = new MsgActionVo(RuleNameType.UNSUBSCRIBE.toString(),1,now,null,"SAVE","A",null);
//		msgActionDao.insert(act);
//		act = new MsgActionVo(RuleNameType.UNSUBSCRIBE.toString(),2,now,null,"UNSUBSCRIBE","A","$"+AddressType.FROM_ADDR.value());
//		msgActionDao.insert(act);
//		act = new MsgActionVo(RuleNameType.UNSUBSCRIBE.toString(),3,now,null,"CLOSE","A",null);
//		msgActionDao.insert(act);
//		act = new MsgActionVo(RuleNameType.SUBSCRIBE.toString(),1,now,null,"SAVE","A",null);
//		msgActionDao.insert(act);
//		act = new MsgActionVo(RuleNameType.SUBSCRIBE.toString(),2,now,null,"SUBSCRIBE","A","$"+AddressType.FROM_ADDR.value());
//		msgActionDao.insert(act);
//		act = new MsgActionVo(RuleNameType.SUBSCRIBE.toString(),3,now,null,"ACTIVATE","A","$"+AddressType.FROM_ADDR.value());
//		msgActionDao.insert(act);
//		act = new MsgActionVo(RuleNameType.SUBSCRIBE.toString(),4,now,null,"AUTO_REPLY","A","SubscribeByEmailReply");
//		msgActionDao.insert(act);
//		act = new MsgActionVo(RuleNameType.SUBSCRIBE.toString(),5,now,null,"CLOSE","A",null);
//		msgActionDao.insert(act);
//		act = new MsgActionVo(RuleNameType.BROADCAST.name(),1,now,null,"SAVE","A",null);
//		msgActionDao.insert(act);
//		act = new MsgActionVo(RuleNameType.BROADCAST.name(),2,now,null,"BROADCAST","A",null);
//		msgActionDao.insert(act);
//		act = new MsgActionVo(RuleNameType.BROADCAST.name(),3,now,null,"CLOSE","A",null);
//		msgActionDao.insert(act);
//		act = new MsgActionVo(RuleNameType.GENERIC.name(),1,now,null,"SAVE","A",null);
//		msgActionDao.insert(act);
//		act = new MsgActionVo(RuleNameType.GENERIC.name(),2,now,null,"ACTIVATE","A","$"+AddressType.FROM_ADDR.value());
//		msgActionDao.insert(act);
//		act = new MsgActionVo(RuleNameType.GENERIC.name(),3,now,null,"TO_CSR","A","$CUSTOMER_CARE_INPUT");
//		msgActionDao.insert(act);
//		
//		// for custom rules
//		act = new MsgActionVo("Unattended_Mailbox",1,now,null,"DROP","A",null);
//		msgActionDao.insert(act);
//		act = new MsgActionVo("OutOfOffice_AutoReply",1,now,null,"SAVE","A",null);
//		msgActionDao.insert(act);
//		act = new MsgActionVo("OutOfOffice_AutoReply",2,now,null,"CLOSE","A",null);
//		msgActionDao.insert(act);
//		act = new MsgActionVo("Contact_Us",1,now,null,"SAVE","A",null);
//		msgActionDao.insert(act);
//		act = new MsgActionVo("XHeader_SpamScore",1,now,null,"SAVE","A",null);
//		msgActionDao.insert(act);
//		act = new MsgActionVo("XHeader_SpamScore",2,now,null,"CLOSE","A",null);
//		msgActionDao.insert(act);
//		act = new MsgActionVo("Executable_Attachment",1,now,null,"DROP","A",null);
//		msgActionDao.insert(act);
//		act = new MsgActionVo("HardBouce_WatchedMailbox",1,now,null,"SAVE","A",null);
//		msgActionDao.insert(act);
//		act = new MsgActionVo("HardBouce_WatchedMailbox",2,now,null,"OPEN","A",null);
//		msgActionDao.insert(act);
//		act = new MsgActionVo("HardBounce_NoFinalRcpt",1,now,null,"SAVE","A",null);
//		msgActionDao.insert(act);
//		act = new MsgActionVo("HardBounce_NoFinalRcpt",2,now,null,"OPEN","A",null);
//		msgActionDao.insert(act);

		System.out.println("load() completed.");
	}
}