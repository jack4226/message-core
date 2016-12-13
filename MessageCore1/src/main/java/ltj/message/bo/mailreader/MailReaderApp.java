package ltj.message.bo.mailreader;

import org.apache.log4j.Logger;
import org.springframework.context.support.AbstractApplicationContext;

import ltj.jbatch.app.SpringUtil;
import ltj.message.dao.mailbox.MailBoxDao;
import ltj.message.vo.MailBoxVo;

public class MailReaderApp {
	static final Logger logger = Logger.getLogger(MailReaderApp.class);
	
	public static void main(String[] args) {
		AbstractApplicationContext factory = SpringUtil.getAppContext();
		MailBoxDao mailBoxDao = factory.getBean(MailBoxDao.class);
		MailBoxVo vo = mailBoxDao.getByPrimaryKey("jwang", "localhost");
		if (vo == null) return;
		vo.setFromTimer(true);
		MailReaderBoImpl reader = new MailReaderBoImpl(vo);
		try {
				//reader.start();
				//reader.join();
				reader.readMail(vo.isFromTimer());
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		System.exit(0);
	}

}
