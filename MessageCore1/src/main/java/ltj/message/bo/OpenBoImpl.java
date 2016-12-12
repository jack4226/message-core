package ltj.message.bo;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import ltj.message.bean.MessageBean;
import ltj.message.constant.MsgStatusCode;
import ltj.message.dao.inbox.MsgInboxDao;
import ltj.message.exception.DataValidationException;
import ltj.message.vo.inbox.MsgInboxVo;

@Component("openBo")
@Scope(value="prototype")
@Lazy(value=true)
public class OpenBoImpl extends TaskBaseAdaptor {
	static final Logger logger = Logger.getLogger(OpenBoImpl.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();
	
	@Autowired
	private MsgInboxDao msgInboxDao;

	/**
	 * Open the message by MsgId.
	 * @return a Long representing the msgId opened.
	 */
	public Long process(MessageBean messageBean) throws DataValidationException {
		if (isDebugEnabled)
			logger.debug("Entering process() method...");
		if (messageBean==null) {
			throw new DataValidationException("input MessageBean is null");
		}
		long msgId = -1L;
		if (messageBean.getMsgId()==null) {
			logger.warn("MessageBean.msgId is null, nothing to open");
			return Long.valueOf(msgId);
		}
		
		MsgInboxVo msgInboxVo = msgInboxDao.getByPrimaryKey(messageBean.getMsgId());
		if (msgInboxVo != null) {
			msgId = msgInboxVo.getMsgId();
			int rowsUpdated = 0;
			if (!MsgStatusCode.OPENED.equals(msgInboxVo.getStatusId())) {
				msgInboxVo.setStatusId(MsgStatusCode.OPENED);
				rowsUpdated = msgInboxDao.updateStatusId(msgInboxVo);
			}
			if (isDebugEnabled)
				logger.debug("Rows updated to Opened status: " + rowsUpdated);
		}
		return Long.valueOf(msgId);
	}	
}
