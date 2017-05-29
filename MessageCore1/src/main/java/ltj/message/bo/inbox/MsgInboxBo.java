package ltj.message.bo.inbox;

import ltj.message.bean.MessageBean;
import ltj.message.exception.DataValidationException;
import ltj.message.vo.inbox.MsgInboxVo;

public interface MsgInboxBo {

	public long saveMessage(MessageBean messageBean) throws DataValidationException;

	public MsgInboxVo getMessageByPK(long msgId);
	
	public MsgInboxVo getAllDataByMsgId(long msgId);
}
