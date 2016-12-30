package ltj.message.bo;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import ltj.message.bean.MessageBean;
import ltj.message.dao.client.ClientDao;
import ltj.message.exception.DataValidationException;
import ltj.message.vo.ClientVo;

@Component("excludingPostmastersBo")
@Scope(value="singleton")
@Lazy(value=true)
public class ExcludingPostmastersBoImpl extends TaskBaseAdaptor {
	static final Logger logger = Logger.getLogger(ExcludingPostmastersBoImpl.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();
	
	@Autowired
	private ClientDao clientDao;

	/**
	 * retrieve all mailing list addresses, and construct a regular expression
	 * that matches any address on the list.
	 * 
	 * @return a regular expression
	 */
	public String process(MessageBean messageBean) throws DataValidationException {
		if (isDebugEnabled) {
			logger.debug("Entering process() method...");
		}
		StringBuffer sb = new StringBuffer();
		List<ClientVo> list = clientDao.getAll();
		for (int i = 0; i < list.size(); i++) {
			ClientVo item = list.get(i);
			if (StringUtils.isNotBlank(item.getDomainName())) {
				String emailAddr = "postmaster@" + item.getDomainName().trim();
				buildEmailList(emailAddr, i, sb);
				if (StringUtils.isNotBlank(item.getContactEmail())) {
					buildEmailList(item.getContactEmail(), i + 1, sb);
				}
			}
		}
		return sb.toString();
	}
	
	void buildEmailList(String emailAddr, int i, StringBuffer sb) {
		if (i > 0) {
			sb.append(",");
		}
		sb.append(emailAddr);
	}
}
