package ltj.message.bo.template;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.mail.Address;
import javax.mail.Part;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ltj.message.bean.BodypartBean;
import ltj.message.bean.MessageBean;
import ltj.message.bean.MsgHeader;
import ltj.message.constant.AddressType;
import ltj.message.constant.CodeType;
import ltj.message.constant.Constants;
import ltj.message.constant.VariableName;
import ltj.message.constant.VariableType;
import ltj.message.constant.XHeaderName;
import ltj.message.dao.emailaddr.EmailAddrDao;
import ltj.message.dao.template.BodyTemplateDao;
import ltj.message.dao.template.ClientVariableDao;
import ltj.message.dao.template.GlobalVariableDao;
import ltj.message.dao.template.MsgSourceDao;
import ltj.message.dao.template.SubjTemplateDao;
import ltj.message.dao.template.TemplateVariableDao;
import ltj.message.exception.DataValidationException;
import ltj.message.util.StringUtil;
import ltj.vo.template.BodyTemplateVo;
import ltj.vo.template.ClientVariableVo;
import ltj.vo.template.GlobalVariableVo;
import ltj.vo.template.MsgSourceVo;
import ltj.vo.template.SubjTemplateVo;
import ltj.vo.template.TemplateVariableVo;

@Component("renderBo")
public class RenderBoImpl implements RenderBo {
	static final Logger logger = Logger.getLogger(RenderBoImpl.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();
	static final String LF = System.getProperty("line.separator","\n");
	
	private final Renderer render = Renderer.getInstance();
	
	@Autowired
	private MsgSourceDao msgSourceDao;
	@Autowired
	private BodyTemplateDao bodyTemplateDao;
	@Autowired
	private SubjTemplateDao subjTemplateDao;
	@Autowired
	private ClientVariableDao clientVariableDao;
	@Autowired
	private GlobalVariableDao globalVariableDao;
	@Autowired
	private TemplateVariableDao templateVariableDao;
	@Autowired
	private EmailAddrDao emailAddrDao;
	
	public RenderResponse getRenderedEmail(RenderRequest req)
			throws DataValidationException, ParseException, AddressException {
		logger.info("in getRenderedEmail(RenderRequest)...");
		if (req == null) {
			throw new IllegalArgumentException("RenderRequest is null");
		}
		if (req.startTime==null) {
			req.startTime = new Timestamp(System.currentTimeMillis());
		}
		RenderResponse rsp = initRenderResponse(req);
		buildRenderVariables(req, rsp);
		buildRenderedBody(req, rsp);
		buildRenderedSubj(req, rsp);
		buildRenderedAttachments(req, rsp);
		buildRenderedAddrs(req, rsp);
		buildRenderedMisc(req, rsp);
		buildRenderedXHdrs(req, rsp); // to be executed last

		return rsp;
	}
	
	private RenderResponse initRenderResponse(RenderRequest req) throws DataValidationException {
		MsgSourceVo vo = (MsgSourceVo) msgSourceDao.getByPrimaryKey(req.msgSourceId);
		if (vo == null) {
			throw new DataValidationException("MsgSource record not found for " + req.msgSourceId);
		}
		RenderResponse rsp = new RenderResponse(
				vo,
				req.clientId,
				req.startTime,
				new HashMap<String, RenderVariable>(),
				new HashMap<String, RenderVariable>(),
				new MessageBean()
				);
		return rsp;
	}
	
	public RenderResponse getRenderedBody(RenderRequest req) throws DataValidationException, ParseException {
		RenderResponse rsp = initRenderResponse(req);
		buildRenderVariables(req, rsp);
		buildRenderedBody(req, rsp);

		return rsp;
	}
	
	public RenderResponse getRenderedMisc(RenderRequest req) throws DataValidationException {
		RenderResponse rsp = initRenderResponse(req);
		buildRenderVariables(req, rsp);
		buildRenderedMisc(req, rsp);

		return rsp;
	}
	
	public RenderResponse getRenderedSubj(RenderRequest req) throws DataValidationException, ParseException {
		RenderResponse rsp = initRenderResponse(req);
		buildRenderVariables(req, rsp);
		buildRenderedSubj(req, rsp);

		return rsp;
	}

	public RenderResponse getRenderedAddrs(RenderRequest req) throws DataValidationException, AddressException {
		RenderResponse rsp = initRenderResponse(req);
		buildRenderVariables(req, rsp);
		buildRenderedAddrs(req, rsp);

		return rsp;
	}

	public RenderResponse getRenderedXHdrs(RenderRequest req) throws DataValidationException {
		RenderResponse rsp = initRenderResponse(req);
		buildRenderVariables(req, rsp);
		buildRenderedXHdrs(req, rsp);

		return rsp;
	}
	
	private void buildRenderedBody(RenderRequest req, RenderResponse rsp)
			throws DataValidationException, ParseException {
		if (isDebugEnabled) {
			logger.debug("in buildRenderedBody()...");
		}
		MsgSourceVo srcVo = rsp.msgSourceVo;
		
		String bodyTemplate = null;
		String contentType = null;
		// body template may come from variables
		if (rsp.variableFinal.containsKey(VariableName.BODY_TEMPLATE)
				&& Constants.Y.equalsIgnoreCase(srcVo.getAllowOverride())) {
			RenderVariable var = (RenderVariable) rsp.variableFinal.get(VariableName.BODY_TEMPLATE);
			if (VariableType.TEXT.equals(var.getVariableType())) {
				bodyTemplate = (String) var.getVariableValue();
				contentType = var.getVariableFormat() == null ? "text/plain" : var.getVariableFormat();
			}
		}
		
		if (bodyTemplate == null) {
			BodyTemplateVo tmpltVo = bodyTemplateDao.getByBestMatch(srcVo.getBodyTemplateId(), req.clientId,
					req.startTime);
			if (tmpltVo == null) {
				throw new DataValidationException("BodyTemplate not found for: " + srcVo.getBodyTemplateId() + "/"
						+ req.clientId + "/" + req.startTime);
			}
			bodyTemplate = tmpltVo.getTemplateValue();
			contentType = tmpltVo.getContentType();
		}
		
		String body = render(bodyTemplate, rsp.variableFinal, rsp.variableErrors);
		MessageBean mBean = rsp.messageBean;
		mBean.setContentType(contentType);
		mBean.setBody(body);
	}
	
	private void buildRenderedSubj(RenderRequest req, RenderResponse rsp)
			throws DataValidationException, ParseException {
		logger.info("in buildRenderedSubj()...");
		MsgSourceVo srcVo = rsp.msgSourceVo;

		String subjTemplate = null;
		// subject template may come from variables
		if (rsp.variableFinal.containsKey(VariableName.SUBJECT_TEMPLATE)
				&& Constants.Y.equalsIgnoreCase(srcVo.getAllowOverride())) {
			RenderVariable var = (RenderVariable) rsp.variableFinal.get(VariableName.SUBJECT_TEMPLATE);
			if (VariableType.TEXT.equals(var.getVariableType())) {
				subjTemplate = (String) var.getVariableValue();
			}
		}

		if (subjTemplate == null) {
			SubjTemplateVo tmpltVo = subjTemplateDao.getByBestMatch(srcVo.getSubjTemplateId(), req.clientId,
					req.startTime);
			if (tmpltVo == null) {
				throw new DataValidationException("SubjTemplate not found for: " + srcVo.getSubjTemplateId() + "/"
						+ req.clientId + "/" + req.startTime);
			}
			subjTemplate = tmpltVo.getTemplateValue();
		}
		
		String subj = render(subjTemplate, rsp.variableFinal, rsp.variableErrors);
		MessageBean mBean = rsp.messageBean;
		mBean.setSubject(subj);
	}

	private void buildRenderedAttachments(RenderRequest req, RenderResponse rsp) {
		logger.info("in buildRenderedAttachments()...");
		Map<String, RenderVariable> varbls = rsp.variableFinal;
		MessageBean mBean = rsp.messageBean;
		Collection<RenderVariable> c = varbls.values();
		for (Iterator<RenderVariable> it = c.iterator(); it.hasNext();) {
			RenderVariable r = it.next();
			if (VariableType.LOB.equals(r.getVariableType()) && r.getVariableValue() != null) {
				BodypartBean attNode = new BodypartBean();
				if (r.getVariableFormat() != null && r.getVariableFormat().indexOf(";") > 0
						&& r.getVariableFormat().indexOf("name=") > 0) {
					attNode.setContentType(r.getVariableFormat());
				}
				else {
					attNode.setContentType(r.getVariableFormat() + "; name=\"" + r.getVariableName() + "\"");
				}
				attNode.setDisposition(Part.ATTACHMENT);
				// not necessary, for consistency?
				attNode.setDescription(r.getVariableName());
				attNode.setValue(r.getVariableValue());
				mBean.put(attNode);
			}
		}
	}
	
	private String render(String templateText, Map<String, RenderVariable> varbls, Map<String, RenderVariable> errors)
			throws DataValidationException, ParseException {
		return render.render(templateText, varbls, errors);
	}
	
	private void buildRenderedAddrs(RenderRequest req, RenderResponse rsp) throws AddressException {
		logger.info("in buildRenderedAddrs()...");
		Map<String, RenderVariable> varbls = rsp.variableFinal;
		MessageBean mBean = rsp.messageBean;

		// variableValue could be type of: String/Address
		Collection<RenderVariable> c = varbls.values();
		for (Iterator<RenderVariable> it=c.iterator(); it.hasNext();) {
			RenderVariable r = it.next();
			if (VariableType.ADDRESS.equals(r.getVariableType()) && r.getVariableValue() != null) {
				if (AddressType.FROM_ADDR.value().equals(r.getVariableName())) {
					if (r.getVariableValue() instanceof String) {
						mBean.setFrom(InternetAddress.parse((String) r.getVariableValue()));
					}
					else if (r.getVariableValue() instanceof InternetAddress) {
						mBean.setFrom(InternetAddress.parse(((Address)r.getVariableValue()).toString()));
					}
				}
				else if (AddressType.REPLYTO_ADDR.value().equals(r.getVariableName())) {
					if (r.getVariableValue() instanceof String) {
						mBean.setReplyto(InternetAddress.parse((String) r.getVariableValue()));
					}
					else if (r.getVariableValue() instanceof Address) {
						mBean.setReplyto(InternetAddress.parse(((Address)r.getVariableValue()).toString()));
					}
				}
				else if (AddressType.TO_ADDR.value().equals(r.getVariableName())) {
					if (r.getVariableValue() instanceof String) {
						mBean.setTo(InternetAddress.parse((String) r.getVariableValue()));
					}
					else if (r.getVariableValue() instanceof Address) {
						mBean.setTo(InternetAddress.parse(((Address)r.getVariableValue()).toString()));
					}
				}
				else if (AddressType.CC_ADDR.value().equals(r.getVariableName())) {
					if (r.getVariableValue() instanceof String) {
						mBean.setCc(InternetAddress.parse((String) r.getVariableValue()));
					}
					else if (r.getVariableValue() instanceof Address) {
						mBean.setCc(InternetAddress.parse(((Address)r.getVariableValue()).toString()));
					}
				}
				else if (AddressType.BCC_ADDR.value().equals(r.getVariableName())) {
					if (r.getVariableValue() instanceof String) {
						mBean.setBcc(InternetAddress.parse((String) r.getVariableValue()));
					}
					else if (r.getVariableValue() instanceof Address) {
						mBean.setBcc(InternetAddress.parse(((Address)r.getVariableValue()).toString()));
					}
				}
			}
		}
	}
	
	private void buildRenderedMisc(RenderRequest req, RenderResponse rsp) {
		logger.info("in buildRenderedMisc()...");
		MsgSourceVo src = rsp.msgSourceVo;
		Map<String, RenderVariable> varbls = rsp.variableFinal;
		MessageBean mBean = rsp.messageBean;

		Collection<RenderVariable> c = varbls.values();
		for (Iterator<RenderVariable> it=c.iterator(); it.hasNext();) {
			RenderVariable r = it.next();
			if (r.getVariableValue() != null && VariableType.TEXT.equals(r.getVariableType())) {
				if (VariableName.PRIORITY.equals(r.getVariableName())) {
					String[] s = { (String) r.getVariableValue() };
					mBean.setPriority(s);
				}
				else if (VariableName.RULE_NAME.equals(r.getVariableName())) {
					mBean.setRuleName((String)r.getVariableValue());
				}
				else if (VariableName.CARRIER_CODE.equals(r.getVariableName())) {
					mBean.setCarrierCode((String)r.getVariableValue());
				}
				else if (VariableName.MAILBOX_HOST.equals(r.getVariableName())) {
					mBean.setMailboxHost((String)r.getVariableValue());
				}
				else if (VariableName.MAILBOX_HOST.equals(r.getVariableName())) {
					mBean.setMailboxHost((String)r.getVariableValue());
				}
				else if (VariableName.MAILBOX_NAME.equals(r.getVariableName())) {
					mBean.setMailboxName((String)r.getVariableValue());
				}
				else if (VariableName.MAILBOX_USER.equals(r.getVariableName())) {
					mBean.setMailboxUser((String)r.getVariableValue());
				}
				else if (VariableName.FOLDER_NAME.equals(r.getVariableName())) {
					mBean.setFolderName((String)r.getVariableValue());
				}
				else if (VariableName.CLIENT_ID.equals(r.getVariableName())) {
					mBean.setClientId((String)r.getVariableValue());
				}
				else if (VariableName.CUSTOMER_ID.equals(r.getVariableName())) {
					mBean.setCustId((String)r.getVariableValue());
				}
				else if (VariableName.TO_PLAIN_TEXT.equals(r.getVariableName())) {
					mBean.setToPlainText(Constants.Y.equals((String)r.getVariableValue()));
				}
			}
			else if (r.getVariableValue() != null && VariableType.NUMERIC.equals(r.getVariableType())) {
				if (VariableName.MSG_REF_ID.equals(r.getVariableName())) {
					if (r.getVariableValue() instanceof Long) {
						mBean.setMsgRefId((Long) r.getVariableValue());
					}
					else if (r.getVariableValue() instanceof String) {
						mBean.setMsgRefId(Long.valueOf((String) r.getVariableValue()));
					}
				}
			}
			else if (VariableType.DATETIME.equals(r.getVariableType())) {
				if (VariableName.SEND_DATE.equals(r.getVariableName())) {
					if (r.getVariableValue() == null) {
						mBean.setSendDate(new java.util.Date());
					}
					else {
						SimpleDateFormat fmt = new SimpleDateFormat(Constants.DEFAULT_DATETIME_FORMAT);
						if (r.getVariableFormat()!=null) {
							fmt.applyPattern(r.getVariableFormat());
						}
						if (r.getVariableValue() instanceof String) {
							try {
								java.util.Date date = fmt.parse((String) r.getVariableValue());
								mBean.setSendDate(date);
							}
							catch (ParseException e) {
								logger.error("ParseException caught", e);
								mBean.setSendDate(new java.util.Date());
							}
						}
						else if (r.getVariableValue() instanceof java.util.Date) {
							mBean.setSendDate((java.util.Date) r.getVariableValue());
						}
					}
				}
			}
		}
		// make sure CarrierCode is populated
		if (mBean.getCarrierCode() == null) {
			mBean.setCarrierCode(rsp.msgSourceVo.getCarrierCode());
		}

		if (Constants.Y.equalsIgnoreCase(src.getExcludingIdToken())) {
			mBean.setEmBedEmailId(Boolean.valueOf(false));
		}

		if (Constants.Y.equalsIgnoreCase(src.getSaveMsgStream())) {
			mBean.setSaveMsgStream(true);
		}
		else {
			mBean.setSaveMsgStream(false);
		}
	}
	
	/*
	 * If MessageBean's ClientId field is not valued, and X-Client_id header is
	 * found and valued, populate MessageBean's ClientId field with the value
	 * from X-Client_id header. <br> 
	 */
	private void buildRenderedXHdrs(RenderRequest req, RenderResponse rsp) {
		logger.info("in buildRenderedXHdrs()...");
		// MsgSourceVo src = rsp.msgSourceVo;
		Map<String, RenderVariable> varbls = rsp.variableFinal;
		MessageBean mBean = rsp.messageBean;

		List<MsgHeader> headers = new ArrayList<MsgHeader>();

		Collection<RenderVariable> c = varbls.values();
		for (Iterator<RenderVariable> it=c.iterator(); it.hasNext();) {
			RenderVariable r = it.next();
			if (VariableType.X_HEADER.equals(r.getVariableType()) && r.getVariableValue() != null) {
				MsgHeader msgHeader = new MsgHeader();
				msgHeader.setName(r.getVariableName());
				msgHeader.setValue((String) r.getVariableValue());
				headers.add(msgHeader);
				// set ClientId for MessageBean
				if (XHeaderName.CLIENT_ID.value().equals(r.getVariableName())) {
					if (StringUtil.isEmpty(mBean.getClientId())) {
						mBean.setClientId((String) r.getVariableValue());
					}
				}
				else if (XHeaderName.CUSTOMER_ID.value().equals(r.getVariableName())) {
					if (StringUtil.isEmpty(mBean.getCustId())) {
						mBean.setCustId((String) r.getVariableValue());
					}
				}
			}
		}
		mBean.setHeaders(headers);
	}
	
	private void buildRenderVariables(RenderRequest req, RenderResponse rsp) {
		logger.info("in buildRenderVariables()...");
		
		MsgSourceVo msgSourceVo = msgSourceDao.getByPrimaryKey(req.msgSourceId);
		rsp.msgSourceVo = msgSourceVo;
		
		// retrieve variables
		Collection<GlobalVariableVo> globalVariables = globalVariableDao.getCurrent();
		Collection<ClientVariableVo> clientVariables = clientVariableDao.getCurrentByClientId(req.clientId);
		Collection<TemplateVariableVo> templateVariables = templateVariableDao
				.getCurrentByTemplateId(msgSourceVo.getTemplateVariableId(), req.clientId);
		
		// convert variables into Map
		Map<String, RenderVariable> g_ht = GlobalVariablesToHashMap(globalVariables);
		Map<String, RenderVariable> c_ht = ClientVariablesToHashMap(clientVariables);
		Map<String, RenderVariable> t_ht = TemplateVariablesToHashMap(templateVariables);
		
		// variables from req and MsgSource table
		Map<String, RenderVariable> s_ht = new HashMap<String, RenderVariable>();
		RenderVariable vreq = new RenderVariable(
				VariableName.CLIENT_ID,
				req.clientId,
				null,
				VariableType.TEXT, 
				Constants.Y,
				Constants.Y,
				null);
		s_ht.put(vreq.getVariableName(), vreq);
		
		vreq = new RenderVariable(
			AddressType.FROM_ADDR.value(),
			emailAddrDao.getByAddrId(msgSourceVo.getFromAddrId().longValue()).getEmailAddr(),
			null,
			VariableType.ADDRESS, 
			Constants.Y,
			Constants.Y,
			null);
		s_ht.put(vreq.getVariableName(), vreq);
		
		if (msgSourceVo.getReplyToAddrId()!=null) {
			vreq = new RenderVariable(
				AddressType.REPLYTO_ADDR.value(),
				emailAddrDao.getByAddrId(msgSourceVo.getReplyToAddrId().longValue()).getEmailAddr(),
				null,
				VariableType.ADDRESS, 
				Constants.Y,
				Constants.N,
				null);
			s_ht.put(vreq.getVariableName(), vreq);
		}
		
		// get Runtime variables
		Map<String, RenderVariable> r_ht = req.variableOverrides;
		if (r_ht==null) {
			r_ht = new HashMap<String, RenderVariable>();
		}
		
		// error hash table
		Map<String, RenderVariable> err_ht = new HashMap<String, RenderVariable>();
		
		// merge variable hash tables
		mergeHashMaps(s_ht, g_ht, err_ht);
		mergeHashMaps(c_ht, g_ht, err_ht);
		mergeHashMaps(t_ht, g_ht, err_ht);
		mergeHashMaps(r_ht, g_ht, err_ht);
		
		verifyHashMap(g_ht, err_ht);
		
		rsp.variableFinal.putAll(g_ht);
		rsp.variableErrors.putAll(err_ht);
	}
	
	private void mergeHashMaps(Map<String, RenderVariable> from, Map<String, RenderVariable> to,
			Map<String, RenderVariable> error) {
		Set<String> keys = from.keySet();
		for (Iterator<String> it=keys.iterator(); it.hasNext();) {
			String name = it.next();
			if (to.get(name) != null) {
				RenderVariable req = (RenderVariable) to.get(name);
				if (Constants.Y.equalsIgnoreCase(req.getAllowOverride())
						|| CodeType.MANDATORY.value().equalsIgnoreCase(req.getAllowOverride())) {
					to.put(name, from.get(name));
				}
				else {
					RenderVariable r = (RenderVariable) from.get(name);
					r.setErrorMsg("Variable Override is not allowed.");
					error.put(name, r);
				}
			}
			else {
				to.put(name, from.get(name));
			}
		}
	}
	
	private void verifyHashMap(Map<String, RenderVariable> ht, Map<String, RenderVariable> error) {
		Set<String> keys = ht.keySet();
		for (Iterator<String> it=keys.iterator(); it.hasNext();) {
			String name = it.next();
			RenderVariable req = (RenderVariable) ht.get(name);
			if (CodeType.MANDATORY.value().equalsIgnoreCase(req.getAllowOverride())) {
				req.setErrorMsg("Variable Override is mandatory.");
				error.put(name, req);
			}
		}
	}
	
	private Map<String, RenderVariable> GlobalVariablesToHashMap(Collection<GlobalVariableVo> c) {
		Map<String, RenderVariable> ht = new HashMap<String, RenderVariable>();
		for (Iterator<GlobalVariableVo> it = c.iterator(); it.hasNext();) {
			GlobalVariableVo req = it.next();
			RenderVariable r = new RenderVariable(
				req.getVariableName(),
				req.getVariableValue(), 
				req.getVariableFormat(), 
				req.getVariableType(), 
				req.getAllowOverride(), 
				req.getRequired(),
				""
				);
			ht.put(req.getVariableName(), r);
		}
		return ht;
	}
	
	private Map<String, RenderVariable> ClientVariablesToHashMap(Collection<ClientVariableVo> c) {
		Map<String, RenderVariable> ht = new HashMap<String, RenderVariable>();
		for (Iterator<ClientVariableVo> it = c.iterator(); it.hasNext();) {
			ClientVariableVo req = it.next();
			RenderVariable r = new RenderVariable(
				req.getVariableName(),
				req.getVariableValue(), 
				req.getVariableFormat(), 
				req.getVariableType(), 
				req.getAllowOverride(), 
				req.getRequired(),
				""
				);
			ht.put(req.getVariableName(), r);
		}
		return ht;
	}
	
	private Map<String, RenderVariable> TemplateVariablesToHashMap(Collection<TemplateVariableVo> c) {
		Map<String, RenderVariable> ht = new HashMap<String, RenderVariable>();
		for (Iterator<TemplateVariableVo> it = c.iterator(); it.hasNext();) {
			TemplateVariableVo req = it.next();
			RenderVariable r = new RenderVariable(
				req.getVariableName(),
				req.getVariableValue(), 
				req.getVariableFormat(), 
				req.getVariableType(), 
				req.getAllowOverride(), 
				req.getRequired(),
				""
				);
			ht.put(req.getVariableName(), r);
		}
		return ht;
	}

}
