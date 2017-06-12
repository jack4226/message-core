package ltj.msgui.bean;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;

import org.apache.log4j.Logger;

import ltj.jbatch.common.ProductKey;
import ltj.message.constant.Constants;
import ltj.message.dao.client.ClientDao;
import ltj.message.vo.ClientVo;
import ltj.msgui.util.SpringUtil;

@ManagedBean(name="enterProductKey")
@RequestScoped
public class EnterProductKeyBean implements java.io.Serializable {
	private static final long serialVersionUID = 5162094104987950893L;
	static final Logger logger = Logger.getLogger(EnterProductKeyBean.class);
	private String name = null;
	private String productKey = null;
	private String message = null;
	
	private transient ClientDao senderDataDao = null;
	
	public String enterProductKey() {
		message = null;
		if (!ProductKey.validateKey(productKey)) {
			message = "Invalid Product Key.";
			return "enterkey.failed";
		}
		ClientVo data = getClientDao().getByClientId(Constants.DEFAULT_CLIENTID);
		data.setSystemKey(productKey);
		getClientDao().update(data);
		logger.info("enterProductKey() - rows updated: " + 1);
		return "enterkey.saved";
	}
	
	private ClientDao getClientDao() {
		if (senderDataDao == null) {
			senderDataDao = SpringUtil.getWebAppContext().getBean(ClientDao.class);
		}
		return senderDataDao;
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
