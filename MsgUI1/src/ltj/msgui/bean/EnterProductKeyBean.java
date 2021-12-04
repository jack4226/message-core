package ltj.msgui.bean;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ltj.jbatch.common.ProductKey;
import ltj.message.dao.client.ClientDao;
import ltj.msgui.util.SpringUtil;

public class EnterProductKeyBean {
	static final Logger logger = LogManager.getLogger(EnterProductKeyBean.class);
	private String name = null;
	private String productKey = null;
	private String message = null;
	
	private ClientDao clientDao = null;
	
	public String enterProductKey() {
		message = null;
		if (!ProductKey.validateKey(productKey)) {
			message = "Invalid Product Key.";
			return "enterkey.failed";
		}
		int rowsUpdated = getClientDao().updateSystemKey(productKey);
		logger.info("enterProductKey() - rows updated: " + rowsUpdated);
		return "enterkey.saved";
	}
	
	private ClientDao getClientDao() {
		if (clientDao == null) {
			clientDao = (ClientDao) SpringUtil.getWebAppContext().getBean("clientDao");
		}
		return clientDao;
	}
    
	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getProductKey() {
		return productKey;
	}

	public void setProductKey(String productKey) {
		this.productKey = productKey;
	}
}
