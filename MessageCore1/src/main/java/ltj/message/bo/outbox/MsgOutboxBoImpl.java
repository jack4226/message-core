package ltj.message.bo.outbox;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.mail.Address;
import javax.mail.Part;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ltj.data.preload.RuleNameEnum;
import ltj.message.bean.MessageBean;
import ltj.message.bo.template.RenderBo;
import ltj.message.bo.template.RenderRequest;
import ltj.message.bo.template.RenderResponse;
import ltj.message.bo.template.RenderVariable;
import ltj.message.constant.Constants;
import ltj.message.constant.VariableType;
import ltj.message.dao.emailaddr.EmailAddrDao;
import ltj.message.dao.outbox.MsgRenderedDao;
import ltj.message.dao.outbox.RenderAttachmentDao;
import ltj.message.dao.outbox.RenderObjectDao;
import ltj.message.dao.outbox.RenderVariableDao;
import ltj.message.exception.DataValidationException;
import ltj.message.vo.emailaddr.EmailAddrVo;
import ltj.spring.util.SpringUtil;
import ltj.vo.outbox.MsgRenderedVo;
import ltj.vo.outbox.RenderAttachmentVo;
import ltj.vo.outbox.RenderObjectVo;
import ltj.vo.outbox.RenderVariableVo;

@Component("msgOutboxBo")
public class MsgOutboxBoImpl implements MsgOutboxBo {
	static final Logger logger = Logger.getLogger(MsgOutboxBoImpl.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();

	static final String LF = System.getProperty("line.separator","\n");
	
	@Autowired
	private RenderBo renderBo;
	@Autowired
	private MsgRenderedDao msgRenderedDao;
	@Autowired
	private RenderAttachmentDao renderAttachmentDao;
	@Autowired
	private RenderVariableDao renderVariableDao;
	@Autowired
	private RenderObjectDao renderObjectDao;
	@Autowired
	private EmailAddrDao emailAddrDao;
	
	public MsgOutboxBoImpl() {
	}
	
	public static void main(String[] args) {
		MsgOutboxBo msgOutboxBo = (MsgOutboxBo) SpringUtil.getAppContext().getBean("msgOutboxBo");
		long renderId = 2L;
		try {
			MessageBean bean = msgOutboxBo.getMessageByPK(renderId);
			System.out.println("MessageBean retrieved:\n" + bean);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * save Message Source Id's and RenderVariables into MsgRendered tables
	 * 
	 * @param rsp -
	 *            RenderResponse
	 * @return renderId of the record inserted
	 * @throws DataValidationException 
	 * @throws IOException 
	 */
	public long saveRenderData(RenderResponse rsp) throws DataValidationException {
		if (isDebugEnabled) {
			logger.debug("Entering saveRenderData()...");
		}
		if (rsp == null) {
			throw new DataValidationException("Input object is null");
		}
		if (rsp.getMessageBean() == null) {
			throw new DataValidationException("Input MessageBean object is null");
		}
		if (rsp.getMsgSourceVo() == null) {
			throw new DataValidationException("Input MsgSourceVo object is null");
		}
		
		MessageBean msgBean = rsp.getMessageBean();
		Timestamp updtTime = new Timestamp(new java.util.Date().getTime());
		
		//
		// save MsgRendered record
		//
		MsgRenderedVo msgVo = new MsgRenderedVo();
		
		msgVo.setMsgSourceId(rsp.getMsgSourceVo().getMsgSourceId());
		msgVo.setSubjTemplateId(rsp.getMsgSourceVo().getSubjTemplateId());
		msgVo.setBodyTemplateId(rsp.getMsgSourceVo().getBodyTemplateId());
		msgVo.setStartTime(rsp.getStartTime());
		msgVo.setClientId(msgBean.getClientId());
		msgVo.setCustId(msgBean.getCustId());
		msgVo.setPurgeAfter(rsp.getMsgSourceVo().getPurgeAfter());
		
		msgBean.setPurgeAfter(rsp.getMsgSourceVo().getPurgeAfter());
		msgBean.setRuleName(RuleNameEnum.SEND_MAIL.name());
		
		if (msgBean.getCarrierCode() == null) {
			msgBean.setCarrierCode(rsp.getMsgSourceVo().getCarrierCode());
		}
		if (msgBean.getFrom() == null && rsp.getMsgSourceVo().getFromAddrId() != null) {
			EmailAddrVo addrVo = emailAddrDao.getByAddrId(rsp.getMsgSourceVo().getFromAddrId());
			if (addrVo !=null) {
				try {
					Address[] from = InternetAddress.parse(addrVo.getEmailAddr());
					msgBean.setFrom(from);
				}
				catch (AddressException e) {
					logger.error("saveRenderData() - AddressException caught for address: " + addrVo.getEmailAddr());
				}
			}
		}
		if (msgBean.getReplyto() == null && rsp.getMsgSourceVo().getReplyToAddrId() != null) {
			EmailAddrVo addrVo = emailAddrDao.getByAddrId(rsp.getMsgSourceVo().getReplyToAddrId());
			if (addrVo != null) {
				try {
					Address[] replyto = InternetAddress.parse(addrVo.getEmailAddr());
					msgBean.setReplyto(replyto);
				}
				catch (AddressException e) {
					logger.error("saveRenderData() - AddressException caught for address: " + addrVo.getEmailAddr());
				}
			}
			
		}
		
		if (Constants.Y.equalsIgnoreCase(rsp.getMsgSourceVo().getExcludingIdToken())) {
			// operation moved to MailSender after message is written to database
			msgBean.setEmBedEmailId(Boolean.valueOf(false));
		}
		
		if (Constants.N.equalsIgnoreCase(rsp.getMsgSourceVo().getSaveMsgStream())) {
			// operation moved to MailSender after mail is sent
			msgBean.setSaveMsgStream(false);
		}
		
		msgVo.setUpdtTime(updtTime);
		msgVo.setUpdtUserId(Constants.DEFAULT_USER_ID);
		
		msgRenderedDao.insert(msgVo);
		msgBean.setRenderId(Long.valueOf(msgVo.getRenderId()));
		
		// save Render Attachments & Render Variables
		Map<?, ?> varbls = rsp.getVariableFinal();
		if (varbls!=null && !varbls.isEmpty()) {
			Collection<?> c = varbls.values();
			int i=0;
			for (Iterator<?> it=c.iterator(); it.hasNext(); ) {
				RenderVariable req = (RenderVariable)it.next();
				if (VariableType.LOB.equalsIgnoreCase(req.getVariableType())) {
					// save to RenderAttachment
					RenderAttachmentVo renderAttachmentVo = new RenderAttachmentVo();
					renderAttachmentVo.setRenderId(msgVo.getRenderId());
					renderAttachmentVo.setAttchmntName(req.getVariableName());
					renderAttachmentVo.setAttchmntSeq(i++);
					if (req.getVariableFormat() != null && req.getVariableFormat().indexOf(";") > 0
							&& req.getVariableFormat().indexOf("name=") > 0) {
						renderAttachmentVo.setAttchmntType(req.getVariableFormat());
					}
					else {
						renderAttachmentVo
								.setAttchmntType(req.getVariableFormat() + "; name=\"" + req.getVariableName() + "\"");
					}
					renderAttachmentVo.setAttchmntDisp(Part.ATTACHMENT);
					Object value = req.getVariableValue();
					if (req.getVariableValue() instanceof String) {
						renderAttachmentVo.setAttchmntValue(((String)value).getBytes());
					}
					else if (value instanceof byte[]) {
						renderAttachmentVo.setAttchmntValue((byte[])value);
					}
					else {
						throw new DataValidationException("Invalid Attachment Type: " + value.getClass().getName());
					}
					// create a record
					renderAttachmentDao.insert(renderAttachmentVo);
				}
				else if (VariableType.COLLECTION.equalsIgnoreCase(req.getVariableType())) {
					// save to RenderObject
					RenderObjectVo renderObjectVo = new RenderObjectVo();
					renderObjectVo.setRenderId(msgVo.getRenderId());
					renderObjectVo.setVariableName(req.getVariableName());
					renderObjectVo.setVariableFormat(req.getVariableFormat());
					renderObjectVo.setVariableType(req.getVariableType());
				    try {
					    ByteArrayOutputStream baos = new ByteArrayOutputStream();
					    ObjectOutputStream oos = new ObjectOutputStream(baos);
					    oos.writeObject(req.getVariableValue());
					    oos.flush();
					    byte[] bytes = baos.toByteArray();
					    oos.close();
					    renderObjectVo.setVariableValue(bytes);
				    }
				    catch (IOException e) {
				    	logger.error("saveRenderData() - IOException caught", e);
				    	throw new DataValidationException(e.toString());
				    }
					// create a record
					renderObjectDao.insert(renderObjectVo);
				}
				else {
					// save to RenderVariable
					RenderVariableVo renderVariableVo = new RenderVariableVo();
					renderVariableVo.setRenderId(msgVo.getRenderId());
					renderVariableVo.setVariableName(req.getVariableName());
					renderVariableVo.setVariableFormat(req.getVariableFormat());
					renderVariableVo.setVariableType(req.getVariableType());
					if (VariableType.TEXT.equalsIgnoreCase(req.getVariableType())
							|| VariableType.X_HEADER.equalsIgnoreCase(req.getVariableType())) {
						renderVariableVo.setVariableValue((String)req.getVariableValue());
					}
					else if (VariableType.ADDRESS.equalsIgnoreCase(req.getVariableType())) {
						if (req.getVariableValue() instanceof Address) {
							renderVariableVo.setVariableValue(((Address)req.getVariableValue()).toString());
						}
						else if (req.getVariableValue() instanceof String) {
							renderVariableVo.setVariableValue((String)req.getVariableValue());
						}
					}
					else if (VariableType.NUMERIC.equalsIgnoreCase(req.getVariableType())) {
						if (req.getVariableValue() instanceof Long) {
							renderVariableVo.setVariableValue(((Long)req.getVariableValue()).toString());
						}
						else if (req.getVariableValue() instanceof String) {
							renderVariableVo.setVariableValue((String)req.getVariableValue());
						}
					}
					else if (VariableType.DATETIME.equalsIgnoreCase(req.getVariableType())) {
						SimpleDateFormat fmt = new SimpleDateFormat(Constants.DEFAULT_DATETIME_FORMAT);
						if (req.getVariableFormat()!=null) {
							fmt = new SimpleDateFormat(req.getVariableFormat());
						}
						if (req.getVariableValue()!=null) {
							if (req.getVariableValue() instanceof String) {
								try {
									java.util.Date date = fmt.parse((String)req.getVariableValue());
									renderVariableVo.setVariableValue(fmt.format(date));
								}
								catch (ParseException e) {
									logger.error("saveRenderData() - Invalid Date Value: " + req.getVariableValue(), e);
									renderVariableVo.setVariableValue((String)req.getVariableValue());
								}
							}
							else if (req.getVariableValue() instanceof java.util.Date) {
								renderVariableVo.setVariableValue(fmt.format((java.util.Date) req.getVariableValue()));
							}
						}
					}
					else {
						logger.warn("saveRenderData() - Unrecognized render name/type: " + req.getVariableName() + "/"
								+ req.getVariableType() + ", ignored");
					}
					// create a record
					renderVariableDao.insert(renderVariableVo);
				}
			}
		}
		
		return msgVo.getRenderId();
	}
	
	/**
	 * retrieve a MessageBean by primary key from MsgRendered tables
	 * 
	 * @param renderId -
	 *            render id
	 * @return a MessageBean object
	 * @throws AddressException
	 * @throws DataValidationException
	 * @throws ParseException
	 */
	public MessageBean getMessageByPK(long renderId) throws AddressException, DataValidationException, ParseException {
		RenderRequest renderRequest = getRenderRequestByPK(renderId);
		if (renderRequest == null) { // should never happen
			throw new DataValidationException("RenderRequest is null for RenderId: " + renderId);
		}
		RenderResponse rsp = renderBo.getRenderedEmail(renderRequest);
		rsp.getMessageBean().setRenderId(Long.valueOf(renderId));
		return rsp.getMessageBean();
	}
	
	/**
	 * retrieve a RenderRequest by primary key from MsgRendered tables
	 * 
	 * @param renderId -
	 *            render id
	 * @return a RenderRequest object
	 * @throws DataValidationException 
	 */
	public RenderRequest getRenderRequestByPK(long renderId) throws DataValidationException {
		// get the messageOB
		MsgRenderedVo msgRenderedVo = msgRenderedDao.getByPrimaryKey(renderId);
		if (msgRenderedVo == null) {
			throw new DataValidationException("MsgRendered record not found for renderId: " + renderId);
		}
		String msgSourceId = msgRenderedVo.getMsgSourceId();
		//MsgSourceVo src = msgSourceDao.getByPrimaryKey(msgSourceId);
		if (msgSourceId == null) {
			throw new DataValidationException("MsgSourceId is null for RenderId: " + renderId);
		}
		
		// populate variableFinal
		Map<String, RenderVariable> varblFinal = new HashMap<String, RenderVariable>();
		List<RenderVariableVo> renderVariables = renderVariableDao.getByRenderId(renderId);
		if (renderVariables != null && !renderVariables.isEmpty()) {
			Iterator<RenderVariableVo> it = renderVariables.iterator();
			while (it.hasNext()) {
				RenderVariableVo varVo = it.next();
				RenderVariable r = new RenderVariable(
						varVo.getVariableName(),
						varVo.getVariableValue(),
						varVo.getVariableFormat(),
						varVo.getVariableType(), 
						Constants.Y, // allow override
						Constants.N, // required
						null // error message
						);
				
				varblFinal.put(r.getVariableName(), r);
			}
		}

		// populate renderObjects into variableFinal
		List<RenderObjectVo> renderObjects = renderObjectDao.getByRenderId(renderId);
		if (renderObjects != null && !renderObjects.isEmpty()) {
			Iterator<RenderObjectVo> it = renderObjects.iterator();
			while (it.hasNext()) {
				RenderObjectVo varVo = it.next();
				List<?> value = null;
				try {
					byte[] bytes = varVo.getVariableValue();
					ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
					ObjectInputStream ois = new ObjectInputStream(bais);
					value = (List<?>) ois.readObject();
				}
				catch (Exception e) {
					logger.error("Exception caught", e);
					throw new DataValidationException(e.toString());
				}
				RenderVariable r = new RenderVariable(
						varVo.getVariableName(),
						value,
						varVo.getVariableFormat(),
						varVo.getVariableType(),
						Constants.Y, // allow override
						Constants.N, // required
						null // error message
						);
				
				varblFinal.put(r.getVariableName(), r);
			}
		}

		// populate renderAttachments into variableFinal
		List<RenderAttachmentVo> renderAttachments = renderAttachmentDao.getByRenderId(renderId);
		if (renderAttachments != null && !renderAttachments.isEmpty()) {
			for (Iterator<RenderAttachmentVo> it = renderAttachments.iterator(); it.hasNext();) {
				RenderAttachmentVo attVo = it.next();
				Object value = null;
				if (attVo.getAttchmntType().indexOf("text")>=0) {
					value = new String(attVo.getAttchmntValue());
				}
				else {
					value = attVo.getAttchmntValue();
				}
				RenderVariable r = new RenderVariable(
					attVo.getAttchmntName(), 
					value, 
					attVo.getAttchmntType(), // content type as format
					VariableType.LOB, 
					Constants.Y, 
					Constants.N, 
					null
					);
				varblFinal.put(r.getVariableName(), r);
			}
		}
		
		RenderRequest renderRequest = new RenderRequest(
			msgSourceId,
			msgRenderedVo.getClientId(),
			msgRenderedVo.getStartTime(),
			varblFinal
			);
		
		return renderRequest;
	}
}
