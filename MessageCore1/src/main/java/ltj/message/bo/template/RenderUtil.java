package ltj.message.bo.template;

import static ltj.message.constant.Constants.DEFAULT_CLIENTID;

import java.sql.Timestamp;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ltj.message.bo.mailinglist.MailingListUtil;
import ltj.message.constant.AddressType;
import ltj.message.constant.Constants;
import ltj.message.constant.VariableDelimiter;
import ltj.message.constant.VariableName;
import ltj.message.constant.VariableType;
import ltj.message.dao.emailaddr.EmailAddressDao;
import ltj.message.dao.emailaddr.EmailTemplateDao;
import ltj.message.dao.emailaddr.EmailVariableDao;
import ltj.message.dao.emailaddr.MailingListDao;
import ltj.message.dao.template.BodyTemplateDao;
import ltj.message.dao.template.ClientVariableDao;
import ltj.message.dao.template.GlobalVariableDao;
import ltj.message.dao.template.TemplateVariableDao;
import ltj.message.exception.DataValidationException;
import ltj.message.exception.TemplateNotFoundException;
import ltj.message.external.VariableResolver;
import ltj.message.util.EmailAddrUtil;
import ltj.message.util.StringUtil;
import ltj.message.vo.emailaddr.EmailAddressVo;
import ltj.message.vo.emailaddr.EmailTemplateVo;
import ltj.message.vo.emailaddr.EmailVariableVo;
import ltj.message.vo.emailaddr.MailingListVo;
import ltj.message.vo.emailaddr.TemplateRenderVo;
import ltj.spring.util.SpringUtil;
import ltj.vo.template.BodyTemplateVo;
import ltj.vo.template.ClientVariableVo;
import ltj.vo.template.GlobalVariableVo;
import ltj.vo.template.TemplateVariableVo;

public final class RenderUtil {
	static final Logger logger = LogManager.getLogger(RenderUtil.class);
	public static final boolean isDebugEnabled = logger.isDebugEnabled();
	static final String LF = System.getProperty("line.separator", "\n");
	
	private static GlobalVariableDao globalVariableDao = null;
	private static ClientVariableDao clientVariableDao = null;
	private static TemplateVariableDao templateVariableDao = null;
	private static BodyTemplateDao bodyTemplateDao = null;
	private static EmailTemplateDao emailTemplateDao = null;
	private static MailingListDao mailingListDao = null;
	private static EmailAddressDao emailAddressDao = null;

	private RenderUtil() {
		// static methods only
	}
	
	static GlobalVariableDao getGlobalVariableDao() {
		if (globalVariableDao == null) {
			globalVariableDao = SpringUtil.getDaoAppContext().getBean(GlobalVariableDao.class);
		}
		return globalVariableDao;
	}

	static ClientVariableDao getClientVariableDao() {
		if (clientVariableDao == null) {
			clientVariableDao = SpringUtil.getDaoAppContext().getBean(ClientVariableDao.class);
		}
		return clientVariableDao;
	}

	static TemplateVariableDao getTemplateVariableDao() {
		if (templateVariableDao == null) {
			templateVariableDao = SpringUtil.getDaoAppContext().getBean(TemplateVariableDao.class);
		}
		return templateVariableDao;
	}

	static BodyTemplateDao getBodyTemplateDao() {
		if (bodyTemplateDao == null) {
			bodyTemplateDao = SpringUtil.getDaoAppContext().getBean(BodyTemplateDao.class);
		}
		return bodyTemplateDao;
	}

	static EmailTemplateDao getEmailTemplateDao() {
		if (emailTemplateDao == null) {
			emailTemplateDao = SpringUtil.getDaoAppContext().getBean(EmailTemplateDao.class);
		}
		return emailTemplateDao;
	}

	static MailingListDao getMailingListDao() {
		if (mailingListDao == null) {
			mailingListDao = SpringUtil.getDaoAppContext().getBean(MailingListDao.class);
		}
		return mailingListDao;
	}

	static EmailAddressDao getEmailAddressDao() {
		if (emailAddressDao == null) {
			emailAddressDao = SpringUtil.getDaoAppContext().getBean(EmailAddressDao.class);
		}
		return emailAddressDao;
	}

	/**
	 * render a template by templateId and clientId.
	 * 
	 * @param templateId -
	 *            template id
	 * @param clientId -
	 *            client id
	 * @param variables -
	 *            variables
	 * @return rendered text
	 * @throws DataValidationException
	 * @throws ParseException
	 */
	public static String renderTemplateId(String templateId, String clientId, Map<String, RenderVariable> variables)
			throws DataValidationException, ParseException {
		if (StringUtil.isEmpty(clientId)) {
			clientId = DEFAULT_CLIENTID;
		}
		Timestamp startTime = new Timestamp(System.currentTimeMillis());
		BodyTemplateVo bodyVo = getBodyTemplateDao().getByBestMatch(templateId, clientId, startTime);
		if (bodyVo == null) {
			throw new DataValidationException(
					"BodyTemplate not found for: " + templateId + "/" + clientId + "/" + startTime);
		}
		else if (isDebugEnabled) {
			logger.debug("Template to render:" + LF + bodyVo.getTemplateValue());
		}
		
		Map<String, RenderVariable> map = new HashMap<String, RenderVariable>();

		List<TemplateVariableVo> tmpltList = getTemplateVariableDao().getByTemplateId(templateId);
		for (Iterator<TemplateVariableVo> it = tmpltList.iterator(); it.hasNext();) {
			TemplateVariableVo vo = it.next();
			RenderVariable var = new RenderVariable(
					vo.getVariableName(),
					vo.getVariableValue(),
					vo.getVariableFormat(),
					VariableType.getByValue(vo.getVariableType()),
					vo.getAllowOverride(),
					vo.getRequired(),
					null);
			if (map.containsKey(vo.getVariableName())) {
				RenderVariable v2 = map.get(vo.getVariableName());
				if (Constants.Y.equalsIgnoreCase(v2.getAllowOverride())) {
					map.put(vo.getVariableName(), var);
				}
			}
			else {
				map.put(vo.getVariableName(), var);
			}
		}
		
		if (variables != null) {
			Set<String> keys = variables.keySet();
			for (Iterator<String> it=keys.iterator(); it.hasNext(); ) {
				String key = it.next();
				if (map.containsKey(key)) {
					RenderVariable v2 = map.get(key);
					if (Constants.Y.equalsIgnoreCase(v2.getAllowOverride())) {
						map.put(key, variables.get(key));
					}
				}
				else {
					map.put(key, variables.get(key));
				}
			}
		}
		
		String text = renderTemplateText(bodyVo.getTemplateValue(), clientId, map);
		return text;
	}
	
	/**
	 * render a template by template text and client id.
	 * 
	 * @param templateText -
	 *            template text
	 * @param clientId -
	 *            client id
	 * @param variables -
	 *            variables
	 * @return rendered text
	 * @throws DataValidationException
	 * @throws ParseException
	 */
	public static String renderTemplateText(String templateText, String clientId, Map<String, RenderVariable> variables)
			throws DataValidationException, ParseException {
		if (templateText == null || templateText.trim().length() == 0) {
			return templateText;
		}
		if (StringUtil.isEmpty(clientId)) {
			clientId = DEFAULT_CLIENTID;
		}
		
		Map<String, RenderVariable> map = new HashMap<String, RenderVariable>();

		List<GlobalVariableVo> globalList = getGlobalVariableDao().getCurrent();
		for (Iterator<GlobalVariableVo> it = globalList.iterator(); it.hasNext();) {
			GlobalVariableVo vo = it.next();
			RenderVariable var = new RenderVariable(
					vo.getVariableName(),
					vo.getVariableValue(),
					vo.getVariableFormat(),
					VariableType.getByValue(vo.getVariableType()),
					vo.getAllowOverride(),
					vo.getRequired(),
					null);
			map.put(vo.getVariableName(), var);
		}

		List<ClientVariableVo> clientList = null;
		if (clientId != null) {
			clientList = getClientVariableDao().getCurrentByClientId(clientId);
			for (Iterator<ClientVariableVo> it = clientList.iterator(); it.hasNext();) {
				ClientVariableVo vo = it.next();
				RenderVariable var = new RenderVariable(
						vo.getVariableName(),
						vo.getVariableValue(),
						vo.getVariableFormat(),
						VariableType.getByValue(vo.getVariableType()),
						vo.getAllowOverride(),
						vo.getRequired(),
						null);
				if (map.containsKey(vo.getVariableName())) {
					RenderVariable v2 = map.get(vo.getVariableName());
					if (Constants.Y.equalsIgnoreCase(v2.getAllowOverride())) {
						map.put(vo.getVariableName(), var);
					}
				}
				else {
					map.put(vo.getVariableName(), var);
				}
			}
		}

		if (variables != null) {
			Set<String> keys = variables.keySet();
			for (Iterator<String> it=keys.iterator(); it.hasNext(); ) {
				String key = it.next();
				if (map.containsKey(key)) {
					RenderVariable v2 = map.get(key);
					if (Constants.Y.equalsIgnoreCase(v2.getAllowOverride())) {
						map.put(key, variables.get(key));
					}
				}
				else {
					map.put(key, variables.get(key));
				}
			}
		}
		
		Map<String, RenderVariable> errors = new HashMap<String, RenderVariable>();
		String text = Renderer.getInstance().render(templateText, map, errors);
		return text;
	}
	
	/**
	 * Retrieve variable names from a input text string. It throws a Data
	 * Validation exception if the length of any variable name exceeds 26
	 * characters, or a closing variable delimiter is missing.
	 * 
	 * @param text -
	 *            template text
	 * @return list of variable names
	 * @throws DataValidationException
	 */
	public static List<String> retrieveVariableNames(String text) throws DataValidationException {
		List<String> varNames = new ArrayList<String>();
		if (text == null || text.trim().length() == 0)
			return varNames;
		text = Renderer.convertUrlBraces(text);
		int bgnPos = 0;
		int nextPos;
		while ((bgnPos = text.indexOf(VariableDelimiter.OPEN_DELIMITER, bgnPos)) >= 0) {
			if ((nextPos = text.indexOf(VariableDelimiter.CLOSE_DELIMITER, bgnPos + VariableDelimiter.OPEN_DELIMITER.length())) > 0) {
				String name = text.substring(bgnPos + VariableDelimiter.OPEN_DELIMITER.length(), nextPos);
				if (name.length() > VariableDelimiter.VARIABLE_NAME_LENGTH) {
					int _openPos = text.indexOf(VariableDelimiter.OPEN_DELIMITER, bgnPos + VariableDelimiter.OPEN_DELIMITER.length());
					if (_openPos > 0 && _openPos < nextPos) {
						int _endPos = Math.min(text.length(), bgnPos + 26);
						throw new DataValidationException("Missing " + VariableDelimiter.CLOSE_DELIMITER
								+ " from position " + bgnPos + ", around: " + text.substring(bgnPos, _endPos));
					}
					else {
						throw new DataValidationException("VariableType name: ${" + name
								+ "} exceeded maximum length: " + VariableDelimiter.VARIABLE_NAME_LENGTH);
					}
				}
				bgnPos = nextPos + VariableDelimiter.CLOSE_DELIMITER.length();
				if (!varNames.contains(name)) {
					varNames.add(name);
				}
			}
			else {
				int _endPos = Math.min(text.length(), bgnPos + 26);
				throw new DataValidationException("Missing " + VariableDelimiter.CLOSE_DELIMITER + " from position "
						+ bgnPos + ", around: " + text.substring(bgnPos, _endPos));
			}
		}
		return varNames;
	}

	/**
	 * This method first retrieves variable names from the input text and save
	 * them into a list. It then loop through the list and for each name in the
	 * list, it checks the variable's value to make sure there is no loops.<br/>
	 * 
	 * This method should be called before an email template is saved to the
	 * database.
	 * 
	 * @param text -
	 *            template text
	 * @throws DataValidationException
	 */
	public static void checkVariableLoop(String text) throws DataValidationException {
		checkVariableLoop(text, (Map<String, RenderVariable>)null);
	}
	
	public static void checkVariableLoop(String text, Map<String, RenderVariable> vars) throws DataValidationException {
		List<String> varNames = retrieveVariableNames(text);
		if (vars != null) {
			for (String var : vars.keySet()) {
				varNames.add(var);
			}
		}
		for (String loopName : varNames) {
			EmailVariableVo vo = getEmailVariableDao().getByName(loopName);
			if (vo != null) {
				checkVariableLoop(vo.getDefaultValue(), loopName, vars);
			}
		}
	}
	
	/**
	 * Check the variables in the input text against the provided variable name
	 * (loopName) for possible loop. It throws a Data Validation exception if a
	 * variable name from the input text matches the "loopName", or the length
	 * of a variable name exceeds 26 characters, or a closing variable delimiter
	 * is missing.<br/>
	 * 
	 * This method should be called before an email variable is saved to the
	 * database.
	 * 
	 * @param text -
	 *            email variable value
	 * @param loopName -
	 *            variable name to match
	 * @throws DataValidationException
	 */
	public static void checkVariableLoop(String text, String loopName) throws DataValidationException {
		checkVariableLoop(text, loopName, null);
	}
	
	public static void checkVariableLoop(String text, String loopName, Map<String, RenderVariable> vars)
			throws DataValidationException {
		List<String> varNames = new ArrayList<String>();
		checkVariableLoop(text, loopName, varNames, 0, vars);
	}
	
	/**
	 * Check the variables in the input text against the provided variable name
	 * (loopName) for possible loop. It throws a Data Validation exception if a
	 * variable name from the input text matches the "loopName", or the length
	 * of a variable name exceeds 26 characters, or a closing variable delimiter
	 * is missing.
	 * 
	 * @param text -
	 *            template text
	 * @param loopName -
	 *            variable name to match
	 * @param varNames -
	 *            variable names found from template so far
	 * @param loops -
	 *            number of recursive loops
	 * @throws DataValidationException
	 */
	static void checkVariableLoop(String text, String loopName, List<String> varNames, int loops, Map<String, RenderVariable> vars)
			throws DataValidationException {
		if (text == null || text.trim().length() == 0) {
			return;
		}
		text = Renderer.convertUrlBraces(text);
		int bgnPos = 0;
		int nextPos;
		while ((bgnPos = text.indexOf(VariableDelimiter.OPEN_DELIMITER, bgnPos)) >= 0) {
			if ((nextPos = text.indexOf(VariableDelimiter.CLOSE_DELIMITER, bgnPos + VariableDelimiter.OPEN_DELIMITER.length())) > 0) {
				String name = text.substring(bgnPos + VariableDelimiter.OPEN_DELIMITER.length(), nextPos);
				if (name.length() > VariableDelimiter.VARIABLE_NAME_LENGTH) {
					int _openPos = text.indexOf(VariableDelimiter.OPEN_DELIMITER, bgnPos + VariableDelimiter.OPEN_DELIMITER.length());
					if (_openPos > 0 && _openPos < nextPos) {
						int _endPos = Math.min(text.length(), bgnPos + 26);
						throw new DataValidationException(
								"Missing " + VariableDelimiter.CLOSE_DELIMITER + " in ${" + loopName
										+ "} from position " + bgnPos + ", around: " + text.substring(bgnPos, _endPos));
					}
					else {
						throw new DataValidationException("VariableType name: ${" + name + "} exceeded maximum length: "
								+ VariableDelimiter.VARIABLE_NAME_LENGTH + " in ${" + loopName + "}");
					}
				}
				bgnPos = nextPos + VariableDelimiter.CLOSE_DELIMITER.length();
				logger.info("Loop " + loops + " - " + loopName + " -> " + name);
				if (!varNames.contains(name)) {
					varNames.add(name);
					if (varNames.contains(loopName)) {
						throw new DataValidationException(
								"Loop found, please check ${" + loopName + "} and its contents.");
					}
					if (isListVariable(name)) {
						continue;
					}
					RenderVariable var = vars == null ? null : vars.get(name);
					if (var != null && VariableType.TEXT.equals(var.getVariableType())) {
						String newText = (String) vars.get(name).getVariableValue();
						checkVariableLoop(newText, loopName, varNames, ++loops, vars);
					}
					else {
						EmailVariableVo vo = getEmailVariableDao().getByName(name);
						if (vo != null) {
							String newText = vo.getDefaultValue();
							checkVariableLoop(newText, loopName, varNames, ++loops, vars);
						}
					}
				}
			}
			else {
				int _endPos = Math.min(text.length(), bgnPos + 26);
				throw new DataValidationException("Missing " + VariableDelimiter.CLOSE_DELIMITER + " in ${" + loopName
						+ "} from position " + bgnPos + ", around: " + text.substring(bgnPos, _endPos));
			}
		}
	}

	private static EmailVariableDao emailVariableDao = null;
	static EmailVariableDao getEmailVariableDao() {
		if (emailVariableDao == null) {
			emailVariableDao = SpringUtil.getDaoAppContext().getBean(EmailVariableDao.class);
		}
		return emailVariableDao;
	}
	
	/**
	 * For each variable name on the input list, retrieve its value by executing
	 * its SQL query or process class if one is defined, return the default
	 * value otherwise.
	 * 
	 * @param variables -
	 *            list of variable names
	 * @param addrId -
	 *            email address id
	 * @return a map of rendered variables.
	 */
	static Map<String, RenderVariable> renderEmailVariables(List<String> variables, long addrId) {
		Map<String, RenderVariable> vars = new HashMap<String, RenderVariable>();
		for (String name : variables) {
			if (isListVariable(name)) {
				continue;
			}
			EmailVariableVo vo = getEmailVariableDao().getByName(name);
			if (vo == null) {
				logger.info("renderEmailVariables() - EmailVariable record not found, " + "variable name: " + name);
				continue;
			}
			String query = vo.getVariableQuery();
			String proc = vo.getVariableProc();
			String value = null;
			if (!StringUtil.isEmpty(query)) {
				try {
					value = getEmailVariableDao().getByQuery(query, addrId);
				}
				catch (Exception e) {
					logger.error("Exception caught for: " + query, e);
				}
			}
			else if (!StringUtil.isEmpty(proc)) {
				try {
					Object obj = Class.forName(proc).getDeclaredConstructor().newInstance();
					if (obj instanceof VariableResolver) {
						value = ((VariableResolver)obj).process(addrId);
					}
					else {
						logger.error("VariableType class is not a VariableResolver.");
					}
				}
				catch (Exception e) {
					logger.error("Exception caught for: " + proc, e);
				}
			}
			// use default if the query or procedure returned no value 
			if (value == null) {
				value = vo.getDefaultValue();
			}
			logger.info("renderEmailVariables() - name=" + name + ", value=" + value);
			RenderVariable var = new RenderVariable(name, value, null, VariableType.TEXT, Constants.Y,
					false, null);
			vars.put(name, var);
		}
		return vars;
	}
	
	private static boolean isListVariable(String name) {
		VariableName.MailingListVariableName[] listNames = VariableName.MailingListVariableName.values();
		for (VariableName.MailingListVariableName listName : listNames) {
			if (listName.name().equals(name)) {
				return true;
			}
		}
		return false;
 	}
	
	/* experimental */
	public static String renderEmailVariable(String emailVariableName, Long sbsrId) throws DataValidationException {
		String renderedValue = "";
		EmailVariableVo vo = getEmailVariableDao().getByName(emailVariableName);
		Map<String, RenderVariable> vars = new HashMap<String, RenderVariable>();
		if (sbsrId != null) {
			RenderVariable var = new RenderVariable(
					"SubscriberAddressId",
					sbsrId.toString(),
					null,
					VariableType.TEXT,
					Constants.Y,
					false,
					null);
			vars.put("SubscriberAddressId", var);
		}
		if (vo != null) {
			try {
				renderedValue = RenderUtil.renderTemplateText(vo.getDefaultValue(), null, vars);
			}
			catch (Exception e) {
				System.out.println("loadSbsrDaos.jsp - renderEmailVariable: " + e.toString());
			}
		}
		return renderedValue;
	}
	
	/**
	 * This method renders an email template using provided inputs. It retrieves
	 * a template text using provided template id, and renders the template
	 * using provided variables. It renders customer variables using the
	 * provided TO email address, and uses the list address from the template as
	 * its FROM address.
	 * 
	 * @param toAddr -
	 *            TO address
	 * @param variables -
	 *            list of variables with rendered values
	 * @param templateId -
	 *            template id
	 * @return A TemplateRenderVo instance
	 * @throws DataValidationException
	 * @throws TemplateNotFoundException
	 */
	public static TemplateRenderVo renderEmailTemplate(String toAddr, Map<String, String> variables, String templateId)
			throws DataValidationException, TemplateNotFoundException {
		return renderEmailTemplate(toAddr, variables, templateId, null);
	}
	
	/**
	 * This method renders an email template using provided inputs. It retrieves
	 * a template text using provided template id, and renders the template
	 * using provided variables. It renders customer variables using the
	 * provided TO email address, and uses the list address from the template as
	 * its FROM address. If the listIdOverride is provided, it'll use its list
	 * address as FROM address instead.
	 * 
	 * @param toAddr -
	 *            TO address
	 * @param variables -
	 *            list of variables with rendered values
	 * @param templateId -
	 *            template id
	 * @param listIdOverride -
	 *            use this list address as FROM address if provided.
	 * @return A TemplateRenderVo instance
	 * @throws DataValidationException
	 * @throws TemplateNotFoundException
	 */
	public static TemplateRenderVo renderEmailTemplate(String toAddr, Map<String, String> variables, String templateId,
			String listIdOverride) throws DataValidationException, TemplateNotFoundException {
		if (templateId == null) {
			throw new DataValidationException("Input templateId is null.");
		}
		validateToAddress(toAddr);
		EmailTemplateVo tmpltVo = getEmailTemplateDao().getByTemplateId(templateId);
		if (tmpltVo == null) {
			throw new TemplateNotFoundException("Could not find Template by Id: " + templateId);
		}
		MailingListVo listVo = null;
		if (!StringUtil.isEmpty(listIdOverride)) {
			// try the list id from input parameters first
			listVo = getMailingListDao().getByListId(listIdOverride);
			if (listVo == null) {
				logger.warn("renderEmailTemplate() - Failed to find List by override list Id: " + listIdOverride);
			}
		}
		if (listVo == null) {
			// use the list id from template
			listVo = getMailingListDao().getByListId(tmpltVo.getListId());
		}
		if (listVo == null) {
			throw new DataValidationException("Could not find Mailing List by Id: " + tmpltVo.getListId());
		}
		TemplateRenderVo renderVo = new TemplateRenderVo();
		renderVo.setToAddr(toAddr);
		renderVo.setClientId(listVo.getClientId());
		renderVo.setEmailTemplateVo(tmpltVo);
		renderVo.setMailingListVo(listVo);
		// retrieve variable names from body template
		List<String> varNames = RenderUtil.retrieveVariableNames(tmpltVo.getBodyText());
		if (isDebugEnabled) {
			logger.debug("renderEmailTemplate() - Body VariableType names: " + varNames);
		}
		// retrieve variable names from subject template
		String subjText = tmpltVo.getSubject() == null ? "" : tmpltVo.getSubject();
		List<String> subjVarNames = RenderUtil.retrieveVariableNames(subjText);
		if (!subjVarNames.isEmpty()) {
			varNames.addAll(subjVarNames);
			if (isDebugEnabled) {
				logger.debug("renderEmailTemplate() - Subject VariableType names: " + subjVarNames);
			}
		}
		// TODO fix deadlock issue when running from JUnit test
		EmailAddressVo addrVo = getEmailAddressDao().findByAddress(toAddr);
		// render email variables using TO emailAddrId
		Map<String, RenderVariable> vars = RenderUtil.renderEmailVariables(varNames, addrVo.getEmailAddrId());
		// include render variables from input data
		if (variables != null) {
			Set<String> keys = variables.keySet();
			for (String key : keys) {
				RenderVariable var = new RenderVariable(key, variables.get(key), null, VariableType.TEXT,
						Constants.Y, false, null);
				vars.put(key, var);
			}
		}
		// include mailing list variables
		vars.putAll(MailingListUtil.renderListVariables(listVo, toAddr, addrVo.getEmailAddrId()));
		try {
			// now render the templates
			String clientId = listVo.getClientId();
			String body = RenderUtil.renderTemplateText(tmpltVo.getBodyText(), clientId, vars);
			String subj = RenderUtil.renderTemplateText(tmpltVo.getSubject(), clientId, vars);
			renderVo.setSubject(subj);
			renderVo.setBody(body);
		}
		catch (ParseException e) {
			throw new DataValidationException("ParseException caught", e);
		}
		if (vars.containsKey(AddressType.CC_ADDR.value())) {
			// set CC if it was passed as an input variable
			RenderVariable cc = vars.get(AddressType.CC_ADDR.value());
			if (cc != null && VariableType.TEXT.equals(cc.getVariableType()) && cc.getVariableValue() != null) {
				try {
					validateFromAddress((String) cc.getVariableValue());
					renderVo.setCcAddr((String) cc.getVariableValue());
				}
				catch (Exception e) {
					logger.error("renderEmailTemplate() - Failed to parse CC address: " + cc.getVariableValue() + LF
							+ e.getMessage());
				}
			}
		}
		if (vars.containsKey(AddressType.BCC_ADDR.value())) {
			// set BCC if it was passed as an input variable
			RenderVariable bcc = vars.get(AddressType.BCC_ADDR.value());
			if (bcc != null && VariableType.TEXT.equals(bcc.getVariableType()) && bcc.getVariableValue() != null) {
				try {
					validateFromAddress((String) bcc.getVariableValue());
					renderVo.setBccAddr((String) bcc.getVariableValue());
				}
				catch (Exception e) {
					logger.error("renderEmailTemplate() - Failed to parse BCC address: " + bcc.getVariableValue() + LF
							+ e.getMessage());
				}
			}
		}
		validateFromAddress(listVo.getEmailAddr());
		renderVo.setFromAddr(listVo.getEmailAddr());
		return renderVo;
	}
	
	/**
	 * This method renders an email template using provided inputs. It renders
	 * the message subject and message body using provided variables. It renders
	 * customer variables using the provided TO email address. The FROM address
	 * is retrieved from the mailing list that is retrieved using the provided
	 * list id.
	 * 
	 * @param toAddr -
	 *            TO address
	 * @param variables -
	 *            list of variables with rendered values
	 * @param subj -
	 *            message subject
	 * @param body -
	 *            message body
	 * @param listId -
	 *            mailing list id this email associated to
	 * @return A TemplateRenderVo instance
	 * @throws DataValidationException
	 */
	public static TemplateRenderVo renderEmailText(String toAddr, Map<String, String> variables, String subj,
			String body, String listId) throws DataValidationException {
		return renderEmailText(toAddr, variables, subj, body, listId, null);
	}
	
	/**
	 * This method renders an email template using provided inputs. It renders
	 * the message subject and message body using provided variables. It renders
	 * customer variables using the provided TO email address. The FROM address
	 * is retrieved from the mailing list that is retrieved using the provided
	 * list id.<br/>
	 * 
	 * This method is intended for BroadcastBo where a same message body and
	 * subject are used again and again. The BroadcastBo could scan the message
	 * body and subject once for variable names, and pass them as one of the
	 * inputs.
	 * 
	 * @param toAddr -
	 *            TO address
	 * @param variables -
	 *            list of variables with rendered values
	 * @param subj -
	 *            message subject
	 * @param body -
	 *            message body
	 * @param listId -
	 *            mailing list id this email associated to
	 * @param variableNames -
	 *            list of variable names retrieved from subject and body
	 * @return A TemplateRenderVo instance
	 * @throws DataValidationException
	 */
	public static TemplateRenderVo renderEmailText(String toAddr, Map<String, String> variables, String subj,
			String body, String listId, List<String> variableNames) throws DataValidationException {
		// first check input TO address
		validateToAddress(toAddr);
		MailingListVo listVo = getMailingListDao().getByListId(listId);
		if (listVo == null) {
			throw new DataValidationException("Mailing List " + listId + " not found.");
		}
		String _from = listVo.getEmailAddr();
		String dispName = listVo.getDisplayName();
		if (!StringUtil.isEmpty(dispName)) {
			_from = dispName + "<" + _from + ">";
		}
		validateFromAddress(_from); // us list address as FROM
		TemplateRenderVo renderVo = new TemplateRenderVo();
		renderVo.setToAddr(toAddr);
		renderVo.setFromAddr(_from);
		renderVo.setClientId(listVo.getClientId());
		renderVo.setMailingListVo(listVo);
		List<String> varNames = null;
		if (variableNames == null) {
			// retrieve variable names from message body
			varNames = RenderUtil.retrieveVariableNames(body);
			if (isDebugEnabled) {
				logger.debug("Body VariableType names: " + varNames);
			}
			// retrieve variable names from message subject
			String subject = subj == null ? "" : subj;
			List<String> subjVarNames = RenderUtil.retrieveVariableNames(subject);
			if (!subjVarNames.isEmpty()) {
				varNames.addAll(subjVarNames);
				if (isDebugEnabled) {
					logger.debug("Subject VariableType names: " + subjVarNames);
				}
			}
		}
		else { // use variable names from input
			varNames = variableNames;
		}
		EmailAddressVo addrVo = getEmailAddressDao().findByAddress(toAddr);
		// retrieve variable values by variable name and email address id
		Map<String, RenderVariable> vars = RenderUtil.renderEmailVariables(varNames, addrVo.getEmailAddrId());
		// include render variables from input data
		if (variables != null) {
			Set<String> keys = variables.keySet();
			for (String key : keys) {
				RenderVariable var = new RenderVariable(key, variables.get(key), null,
						VariableType.TEXT, Constants.Y, false, null);
				vars.put(key, var);
			}
		}
		// include mailing list variables
		vars.putAll(MailingListUtil.renderListVariables(listVo, addrVo.getEmailAddr(), addrVo.getEmailAddrId()));
		try {
			String bodyText = RenderUtil.renderTemplateText(body, listVo.getClientId(), vars);
			String subjText = RenderUtil.renderTemplateText(subj, listVo.getClientId(), vars);
			renderVo.setSubject(subjText);
			renderVo.setBody(bodyText);
		}
		catch (ParseException e) {
			logger.error("Failed to render message body", e);
			throw new DataValidationException("ParseException caught: " + e.toString());
		}
		return renderVo;
	}
	
	private static void validateToAddress(String toAddr) throws DataValidationException {
		if (toAddr == null) {
			throw new DataValidationException("Input toAddr is null.");
		}
		if (!EmailAddrUtil.isRemoteEmailAddress(toAddr)) {
			throw new DataValidationException("Input toAddr is invalid: " + toAddr);
		}
		try {
			InternetAddress.parse(toAddr);
		}
		catch (AddressException e) {
			throw new DataValidationException("Input toAddr is invalid: " + toAddr, e);
		}
	}
	
	private static void validateFromAddress(String fromAddr) throws DataValidationException {
		try {
			InternetAddress.parse(fromAddr);
		}
		catch (AddressException e) {
			throw new DataValidationException("Invalid FROM address found from list: " + fromAddr);
		}
	}
	
	public static void main(String[] args) {
		try {
			String text = renderTemplateId("testTemplate", null, null);
			System.out.println(text);
			
			BodyTemplateVo bodyVo = getBodyTemplateDao().getByBestMatch("testTemplate", DEFAULT_CLIENTID, null);
			if (bodyVo == null) {
				throw new DataValidationException("BodyTemplate not found for testTemplate");
			}
			else if (isDebugEnabled) {
				logger.debug("Template to render:" + LF + bodyVo.getTemplateValue());
			}
			List<String> variables = retrieveVariableNames(bodyVo.getTemplateValue());
			System.out.println("Variables: " + variables);
			
			Map<String, String> vars = new HashMap<String, String>();
			vars.put("BroadcastMsgId","3");
			TemplateRenderVo renderVo = renderEmailTemplate("jsmith@test.com",vars, "SampleNewsletter1");
			System.out.println(renderVo);
			
			System.out.println(renderEmailVariable("UserProfileURL", Long.valueOf(1)));
			
			String checkText = "Dear ${SubscriberAddress}," + LF + LF + 
			"This is a sample text newsletter message for a traditional mailing list." + LF +
			"With a traditional mailing list, people who want to subscribe to the list " + LF +
			"must send an email from their account to the mailing list address with " + LF +
			"\"subscribe\" in the email subject." + LF + LF + 
			"Unsubscribing from a traditional mailing list is just as easy; simply send " + LF +
			"an email to the mailing list address with \"unsubscribe\" in subject." + LF + LF +
			"Date sent: ${CurrentDate}" + LF + LF +
			"BroadcastMsgId: ${BroadcastMsgId}, ListId: ${MailingListId}" + LF + LF +
			"Contact Email: ${ContactEmailAddress}" + LF + LF +
			"To see our promotions, copy and paste the following link in your browser:" + LF +
			"${WebSiteUrl}/SamplePromoPage.jsp?msgid=${BroadcastMsgId}&listid=${MailingListId}&sbsrid=${SubscriberAddressId}" + LF +
			"${FooterWithUnsubAddr}";
			checkVariableLoop(checkText);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
