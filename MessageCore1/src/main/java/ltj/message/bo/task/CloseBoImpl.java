package ltj.message.bo.task;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import ltj.message.bean.MessageBean;
import ltj.message.constant.StatusId;
import ltj.message.dao.inbox.MsgInboxDao;
import ltj.message.exception.DataValidationException;
import ltj.message.vo.inbox.MsgInboxVo;

@Component("closeBo")
@Scope(value="prototype")
@Lazy(value=true)
public class CloseBoImpl extends TaskBaseAdaptor {
	static final Logger logger = LogManager.getLogger(CloseBoImpl.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();
	
	@Autowired
	private MsgInboxDao msgInboxDao;

	/**
	 * Close the message by MsgId.
	 * @return a Long representing the msgId closed.
	 */
	public Long process(MessageBean messageBean) throws DataValidationException {
		if (isDebugEnabled) {
			logger.debug("Entering process() method...");
		}
		if (messageBean==null) {
			throw new DataValidationException("input MessageBean is null");
		}
		long msgId = -1L;
		if (messageBean.getMsgId() == null) {
			logger.warn("MessageBean.msgId is null, nothing to close");
			return Long.valueOf(msgId);
		}
		
		MsgInboxVo msgInboxVo = msgInboxDao.getByPrimaryKey(messageBean.getMsgId());
		if (msgInboxVo != null) {
			msgId = msgInboxVo.getMsgId();
			msgInboxVo.setStatusId(StatusId.CLOSED.value());
			int rowsUpdated = msgInboxDao.updateStatusId(msgInboxVo);
			if (isDebugEnabled) {
				logger.debug("Rows updated to Closed status: " + rowsUpdated);
			}
		}
		return Long.valueOf(msgId);
	}
}
