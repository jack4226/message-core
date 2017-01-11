package ltj.message.bo.outbox;

import java.text.ParseException;

import javax.mail.internet.AddressException;

import ltj.message.bean.MessageBean;
import ltj.message.bo.template.RenderRequest;
import ltj.message.bo.template.RenderResponse;
import ltj.message.exception.DataValidationException;

public interface MsgOutboxBo {

	public long saveRenderData(RenderResponse rsp) throws DataValidationException;

	public MessageBean getMessageByPK(long renderId) throws AddressException, DataValidationException, ParseException;

	public RenderRequest getRenderRequestByPK(long renderId) throws DataValidationException;
}
