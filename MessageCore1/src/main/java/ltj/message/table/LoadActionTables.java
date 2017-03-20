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
import ltj.message.constant.StatusId;
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

		System.out.println("load() completed.");
	}
	
	void load(MsgActionDao msgActionDao) {
		Timestamp now = new Timestamp(System.currentTimeMillis());
		
		for (RuleActionEnum act : RuleActionEnum.values()) {
			String ruleName = act.getRuleName().name();
			MsgActionVo vo = new MsgActionVo(ruleName, act.getSequence(), now, null,
					act.getActionDetail().name(), StatusId.ACTIVE.value(), act.getFieldValues());
			msgActionDao.insert(vo);
		}
		
		System.out.println("load() completed.");
	}
}