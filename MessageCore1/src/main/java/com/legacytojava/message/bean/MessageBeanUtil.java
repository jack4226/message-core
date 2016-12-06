package com.legacytojava.message.bean;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.activation.DataHandler;
import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;

import org.apache.log4j.Logger;

import com.legacytojava.message.bo.mailsender.MessageBodyBuilder;
import com.legacytojava.message.constant.Constants;
import com.legacytojava.message.constant.XHeaderName;
import com.legacytojava.message.dao.idtokens.EmailIdParser;
import com.legacytojava.message.util.EmailAddrUtil;
import com.legacytojava.message.util.StringUtil;

public final class MessageBeanUtil {
	static final Logger logger = Logger.getLogger(MessageBeanUtil.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();

	final static String LF = System.getProperty("line.separator", "\n");
	private static String MAILER = "MailSender";
	static boolean debugSession = false;

	private MessageBeanUtil() {
		// make it static only
	}

	/**
	 * convert MessageBean to JavaMail MimeMessage
	 * 
	 * @param msgBean -
	 *            a MessageBean object
	 * @return JavaMail Message
	 * @throws MessagingException
	 * @throws IOException 
	 */
	public static Message createMimeMessage(MessageBean msgBean) throws MessagingException,
			IOException {
		javax.mail.Session session = Session.getDefaultInstance(System.getProperties());
		if (debugSession)
			session.setDebug(true);
		Message msg = new MimeMessage(session);

		// First Set All Headers from a header List
		List<MsgHeader> headers = msgBean.getHeaders();
		if (headers != null) {
			for (int i = 0; i < headers.size(); i++) {
				MsgHeader header = headers.get(i);
				if (!getReservedHeaders().contains(header.getName())) {
					msg.setHeader(header.getName(), header.getValue());
				}
				if (isDebugEnabled) {
					logger.debug("createMimeMessage() - Header Line - " + header.getName() + ": "
							+ header.getValue());
				}
			}
		}
		
		// override certain headers with the data from MesssageBean
		if (msgBean.getFrom() != null) {
			for (int i = 0; i < msgBean.getFrom().length; i++) {
				// just for safety
				if (msgBean.getFrom()[i] != null) {
					msg.setFrom(msgBean.getFrom()[i]);
					break;
				}
			}
		}
		else {
			logger.warn("createMimeMessage() - MessageBean.getFrom() returns a null");
			msg.setFrom();
		}
		if (msgBean.getTo() != null) {
			msg.setRecipients(Message.RecipientType.TO, msgBean.getTo());
		}
		else {
			logger.warn("createMimeMessage() - MessageBean.getTo() returns a null");
		}
		if (msgBean.getCc() != null) {
			msg.setRecipients(Message.RecipientType.CC, msgBean.getCc());
		}
		if (msgBean.getBcc() != null) {
			msg.setRecipients(Message.RecipientType.BCC, msgBean.getBcc());
		}
		if (msgBean.getReplyto() != null) {
			msg.setReplyTo(msgBean.getReplyto());
		}
		
		// Add some bean fields to MimeMessage header
		addBeanFieldsToHeader(msgBean, msg);
		
		if (msgBean.getReturnPath() != null && msgBean.getReturnPath().trim().length() > 0) {
			msg.setHeader(XHeaderName.RETURN_PATH, msgBean.getReturnPath());
		}
		msg.setHeader(XHeaderName.XHEADER_PRIORITY, getMsgPriority(msgBean.getPriority()));
		msg.setHeader(XHeaderName.XHEADER_MAILER, MAILER);
		msg.setSentDate(new Date());

		msg.setSubject(msgBean.getSubject() == null ? "" : msgBean.getSubject());

		// Add encoded MsgId to X-Header
		if (msgBean.getMsgId() != null) {
			EmailIdParser parser = EmailIdParser.getDefaultParser();
			String xHeaderText = parser.wrapupEmailId4XHdr(msgBean.getMsgId());
			String xHeaderName = parser.getEmailIdXHdrName();
			msg.setHeader(xHeaderName, xHeaderText);
			if (isDebugEnabled) {
				String[] values = msg.getHeader(xHeaderName);
				String valueStr = null;
				for (int i = 0; values != null && i < values.length; i++) {
					if (i == 0)
						valueStr = values[i];
					else
						valueStr = valueStr + "," + values[i];
				}
				logger.debug("createMimeMessage() - X-Header Line - " + xHeaderName + ": "
						+ valueStr);
			}
		}

		// construct message body part
		List<BodypartBean> aNodes = msgBean.getNodes();
		if (msgBean.getMimeType().startsWith("multipart")) {
			Multipart mp = new MimeMultipart(msgBean.getMimeSubType());
			msg.setContent(mp);
			constructMultiPart(mp, (BodypartBean) msgBean, 0);
		}
		else if (aNodes != null && aNodes.size() > 0) {
			Multipart mp = new MimeMultipart("mixed"); // make up a default
			msg.setContent(mp);
			if (msgBean.getValue()!=null) {
				BodyPart bp = new MimeBodyPart();
				mp.addBodyPart(bp);
				constructSinglePart(bp, (BodypartBean) msgBean, 0);
			}
			constructMultiPart(mp, (BodypartBean) msgBean, 0);
		}
		else {
			constructSinglePart(msg, (BodypartBean) msgBean, 0);
		}
		msg.saveChanges(); // please remember to save the message
		
		return msg;
	}
	
	/**
	 * Add some MessageBean's properties as X-Headers to JavaMail message.
	 * @param messageBean
	 * @param msg
	 * @throws MessagingException
	 */
	public static void addBeanFieldsToHeader(MessageBean messageBean, Message msg)
			throws MessagingException {
		if (messageBean.getClientId() != null) {
			msg.setHeader(XHeaderName.XHEADER_CLIENT_ID, messageBean.getClientId());
		}
		if (messageBean.getCustId() != null) {
			msg.setHeader(XHeaderName.XHEADER_CUSTOMER_ID, messageBean.getCustId());
		}
		if (messageBean.getMsgId() != null) {
			msg.setHeader(XHeaderName.XHEADER_MSG_ID, messageBean.getMsgId().toString());
		}
		if (messageBean.getMsgRefId() != null) {
			msg.setHeader(XHeaderName.XHEADER_MSG_REF_ID, messageBean.getMsgRefId().toString());
		}
		if (messageBean.getRenderId() != null) {
			msg.setHeader(XHeaderName.XHEADER_RENDER_ID, messageBean.getRenderId().toString());
		}
		if (messageBean.getRuleName() != null) {
			msg.setHeader(XHeaderName.XHEADER_RULE_NAME, messageBean.getRuleName());
		}
		
		if (messageBean.isUseSecureServer()) {
			msg.setHeader(XHeaderName.XHEADER_USE_SECURE_SMTP, Constants.YES);
		}
		else {
			msg.setHeader(XHeaderName.XHEADER_USE_SECURE_SMTP, Constants.NO);
		}
		if (messageBean.getSaveMsgStream() == true) {
			msg.setHeader(XHeaderName.XHEADER_SAVE_RAW_STREAM, Constants.YES);
		}
		else {
			msg.setHeader(XHeaderName.XHEADER_SAVE_RAW_STREAM, Constants.NO);
		}
		if (messageBean.getEmBedEmailId() != null) {
			if (messageBean.getEmBedEmailId().booleanValue()) {
				msg.setHeader(XHeaderName.XHEADER_EMBED_EMAILID, Constants.YES);
			}
			else {
				msg.setHeader(XHeaderName.XHEADER_EMBED_EMAILID, Constants.NO);
			}
		}
		if (messageBean.getOverrideTestAddr() == true) {
			msg.setHeader(XHeaderName.XHEADER_OVERRIDE_TEST_ADDR, Constants.YES);
		}
		else {
			msg.setHeader(XHeaderName.XHEADER_OVERRIDE_TEST_ADDR, Constants.NO);
		}
	}
	
	/**
	 * create MessageBean from SMTP raw stream
	 * @param mailStream
	 * @return a MessageBean
	 * @throws MessagingException
	 */
	public static MessageBean createBeanFromStream(byte[] mailStream) throws MessagingException {
		Message msg = createMimeMessage(mailStream);
		try {
			MessageBean msgBean = MessageBeanBuilder.processPart(msg, null);
			return msgBean;
		}
		catch (IOException e) {
			logger.error("IOException caught", e);
			throw new MessagingException(e.toString());
		}
	}
	
	/**
	 * create JavaMail Message from SMTP raw stream
	 * @param mailStream
	 * @return a JavaMail Message
	 * @throws MessagingException
	 */
	public static Message createMimeMessage(byte[] mailStream) throws MessagingException {
		javax.mail.Session session = Session.getDefaultInstance(System.getProperties());
		session.setDebug(true);
		ByteArrayInputStream bais = new ByteArrayInputStream(mailStream);
		Message msg = new MimeMessage(session, bais);
		msg.saveChanges();
		session.setDebug(debugSession);
		return msg;
	}
	
	/**
	 * create JavaMail Message from mailbox message file.
	 * @param filePath
	 * @return a JavaMail Message
	 * @throws MessagingException
	 * @throws FileNotFoundException
	 */
	public static Message createMimeMessage(String filePath) throws MessagingException,
			FileNotFoundException {
		javax.mail.Session session = Session.getDefaultInstance(System.getProperties());
		session.setDebug(true);
		FileInputStream fis = new FileInputStream(filePath);
		Message msg = new MimeMessage(session, new BufferedInputStream(fis));
		msg.saveChanges();
		session.setDebug(debugSession);
		return msg;
	}
	
	private static void constructMultiPart(Multipart mp, BodypartBean aNode, int level)
			throws MessagingException, IOException {
		
		if (isDebugEnabled) {
			logger.debug("constructMultiPart() - MultipartHL - " + StringUtil.getDots(level)
					+ "Content Type: " + mp.getContentType());
		}
		List<BodypartBean> aNodes = aNode.getNodes();
		for (int i = 0; aNodes != null && i < aNodes.size(); i++) {
			BodypartBean subNode = aNodes.get(i);
			if (subNode.getMimeType().startsWith("multipart")) {
				Multipart subMp = new MimeMultipart(subNode.getMimeSubType());
				BodyPart multiBody = new MimeBodyPart();
				multiBody.setContent(subMp);
				mp.addBodyPart(multiBody);
				constructMultiPart(subMp, subNode, level + 1);
			}
			else {
				BodyPart bodyPart = new MimeBodyPart();
				mp.addBodyPart(bodyPart);
				constructSinglePart(bodyPart, subNode, level + 1);
			}
		}
	}

	private static final Set<String> reservedHeaders = new HashSet<String>();
	private static Set<String> getReservedHeaders() {
		if (reservedHeaders.isEmpty()) {
			reservedHeaders.add("Delivered-To");
			reservedHeaders.add("Received");
			reservedHeaders.add("Message-ID");
			reservedHeaders.add("Subject");
			reservedHeaders.add("Return-Path");
			reservedHeaders.add(EmailIdParser.getDefaultParser().getEmailIdXHdrName());
			//reservedHeaders.add("User-Agent");
		}
		return reservedHeaders;
	}
	
	private static void constructSinglePart(Part part, BodypartBean aNode, int level)
			throws MessagingException, IOException {
		// Set All Headers
		List<MsgHeader> headers = aNode.getHeaders();
		if (headers != null) {
			for (int i = 0; i < headers.size(); i++) {
				MsgHeader header = headers.get(i);
				if (!getReservedHeaders().contains(header.getName())) {
					part.setHeader(header.getName(), header.getValue());
				}
				if (isDebugEnabled) {
					logger.debug("constructSinglePart() - Header Line - "
							+ StringUtil.getDots(level) + header.getName() + ": "
							+ header.getValue());
				}
			}
		}

		part.setDisposition(aNode.getDisposition());
		part.setDescription(aNode.getDescription());

		if (aNode.getMimeType().startsWith("text")) {
			part.setContent(new String(aNode.getValue()), aNode.getContentType());
			if (aNode.getMimeType().startsWith("text/html")) {
				if (aNode.getDisposition() == null) {
					//part.setDisposition(Part.INLINE);
					/* 
					 Do not uncomment above line, as Dyndns Mail Relay server will insert 
					 "-----Inline Attachment Follows-----" at the beginning of the message.
					 */ 
				}
			}
		}
		else {
			if (aNode.getDescription() == null) {
				// not sure why do this, consistency?
				part.setDescription(MessageBeanBuilder.getFileName(aNode.getContentType()));
			}
			ByteArrayDataSource bads = new ByteArrayDataSource(aNode.getValue(), aNode
					.getContentType());
			part.setDataHandler(new DataHandler(bads));
		}
	}
	
	/**
	 * This method is used to build a JavaMail message that loops back to
	 * MailReader and must comply with rfc1893 standard
	 * 
	 * @param msgBean -
	 *            MessageBean
	 * @param loopbackText -
	 *            loop-back message text
	 * @throws MessagingException
	 * @throws IOException
	 *             from ByteArrayDataSource
	 */
	public static Message createMimeMessage(MessageBean msgBean, Address failedAddr,
			String loopbackText) throws MessagingException, IOException {
		if (isDebugEnabled)
			logger.debug("Entering createMimeMessage() for email loopback");
		javax.mail.Session session = Session.getDefaultInstance(System.getProperties());
		if (debugSession)
			session.setDebug(true);
		Message msg = new MimeMessage(session);

		Address[] fromAddr = InternetAddress.parse("postmaster@localhost");
		if (fromAddr != null && fromAddr.length > 0) {
			msg.setFrom(fromAddr[0]);
		}
		else {
			logger.fatal("createMimeMessage() - internal error, contact programming.");
			msg.setFrom();
		}
		if (msgBean.getTo() != null) {
			msg.setRecipients(Message.RecipientType.TO, msgBean.getTo());
		}
		else {
			logger.warn("createMimeMessage() - MessageBean.getTo() returns a null");
		}
		//if (msgBean.getReplyto() != null) {
		//	msg.setReplyTo(msgBean.getReplyto());
		//}
		if (msgBean.getMsgId() != null) {
			EmailIdParser parser = EmailIdParser.getDefaultParser();
			String xHeaderText = parser.wrapupEmailId4XHdr(msgBean.getMsgId());
			msg.setHeader(parser.getEmailIdXHdrName(), xHeaderText);
		}

		msg.setHeader(XHeaderName.XHEADER_PRIORITY, getMsgPriority(msgBean.getPriority()));
		msg.setHeader(XHeaderName.XHEADER_MAILER, MAILER);
		msg.setHeader(XHeaderName.XHEADER_SAVE_RAW_STREAM, Constants.YES);
		msg.setSentDate(new Date());

		String xMailer = ((MimeMessage) msg).getHeader(XHeaderName.XHEADER_MAILER, LF);
		String mimeVer = ((MimeMessage) msg).getHeader("MIME-Version", LF);

		// if (msgBean.getCc() != null)
		// msg.setRecipients(Message.RecipientType.CC, msgBean.getCc());
		// if (msgBean.getBcc() != null)
		// msg.setRecipients(Message.RecipientType.BCC, msgBean.getBcc());

		// construct message/rfc822 content
		String rfc822Str = LF; // don't remove this
		if (msgBean.getSmtpMessageId() != null) {
			rfc822Str += "Message-ID: " + msgBean.getSmtpMessageId() + LF ;
		}
		rfc822Str += "From: " + msgBean.getFromAsString() + LF +
				"To: " + failedAddr.toString() + LF +
				"Subject: " + msgBean.getSubject() + LF +
				"Date: " + msg.getSentDate() + " -0400" + LF +
				"MIME-Version: " + mimeVer + LF +
				"X-Mailer: " + xMailer + LF +
				"Content-Type: text/plain; charset=\"iso-8859-1\"" + LF + LF;

		String body = msgBean.getBody();
		String contentType = msgBean.getBodyContentType();
		if (contentType != null && contentType.toLowerCase().startsWith("text/html")) {
			body = MessageBodyBuilder.removeHtmlBodyTags(body);
		}
		if (body.length() > 10 * 1024) { // allow body text size up to 10k
			body = body.substring(0, 10 * 1024) + LF + Constants.MESSAGE_TRUNCATED;
		}
		msg.setSubject("MailSender Delivery Failure: See Description for Details.");

		Multipart mp = new MimeMultipart("mixed");
		msg.setContent(mp);

		MimeBodyPart mbp1 = new MimeBodyPart();
		mbp1.setDisposition(Part.INLINE);
		mbp1.setText(loopbackText, "us-ascii");
		mp.addBodyPart(mbp1);

		MimeBodyPart mbp2 = new MimeBodyPart();
		mbp2.setDisposition(Part.INLINE);
		ByteArrayDataSource bads = new ByteArrayDataSource(rfc822Str + body, "message/rfc822");
		mbp2.setDataHandler(new DataHandler(bads));
		mp.addBodyPart(mbp2);
		msg.saveChanges();

		return msg;
	}

	/*
	 * a code sample for constructing a multipart/related message
	 */
	static void constructMultiPartRelated(Message msg, MessageBean msgBean, List<MessageNode> mNodes)
			throws MessagingException, IOException {
		// create wrapper multipart/mixed part
		Multipart mp = new MimeMultipart("alternative");
		msg.setContent(mp);

		// create message body
		BodyPart msgBody = new MimeBodyPart();
		String body = msgBean.getValue() == null ? "" : new String(msgBean.getValue());
		String contentType = msgBean.getContentType();
		if (contentType.toLowerCase().startsWith("text/plain")) {
			msgBody.setContent(body, contentType);
		}
		else {
			ByteArrayDataSource bads = new ByteArrayDataSource(body, contentType);
			msgBody.setDataHandler(new DataHandler(bads));
		}
		mp.addBodyPart(msgBody);

		BodyPart related = new MimeBodyPart();
		MimeMultipart mr = new MimeMultipart("related");
		related.setContent(mr);
		mp.addBodyPart(related);

		if (mNodes != null && mNodes.size() > 0) {
			// MessageNode mNode = (MessageNode) mNodes.get(0);
			// BodypartBean aNode = mNode.getAttachmentNode();
			BodyPart bp = new MimeBodyPart();
			String html = "<html><head></head><body><h1>This is the HMTL version of the mail."
					+ "</h1><img src=\"cid:0001\"></body></html>";
			bp.setContent(html, "text/html");
			mr.addBodyPart(bp);
		}
		if (mNodes != null && mNodes.size() > 1) {
			MessageNode mNode = (MessageNode) mNodes.get(1);
			BodypartBean aNode = mNode.getBodypartNode();
			BodyPart img = new MimeBodyPart();
			img.setHeader("Content-ID", "0001");
			img.setDisposition(Part.INLINE);
			ByteArrayDataSource bads = new ByteArrayDataSource(aNode.getValue(), aNode
					.getContentType());
			img.setDataHandler(new DataHandler(bads));
			mr.addBodyPart(img);
		}
		msg.saveChanges();
	}

	public static String getMsgPriority(String[] priority) {
		String outPriority = "2 (Normal)";
		if (priority != null && priority[0] != null) {
			String in_p = priority[0].trim();
			if (in_p.equalsIgnoreCase("HIGH"))
				outPriority = "1 (High)";
			else if (in_p.equalsIgnoreCase("NORM"))
				outPriority = "2 (Normal)";
			else if (in_p.equalsIgnoreCase("LOW"))
				outPriority = "3 (Low)";
		}
		return (outPriority);
	}
	

	public static List<String> getMessageBeanMethodNames() {
		Method methods[] = MessageBean.class.getMethods();
		List<String> methodNameList = new ArrayList<String>();
		
		for (int i = 0; i < methods.length; i++) {
			Method method = (Method) methods[i];
			Class<?> parmTypes[] = method.getParameterTypes();
			int mod = method.getModifiers();
			if (Modifier.isPublic(mod) && !Modifier.isAbstract(mod) && !Modifier.isStatic(mod)) {
				if (method.getName().length() > 3 && method.getName().startsWith("get")
						&& parmTypes.length == 0) {
					String name = method.getName().substring(3);
					
					if (method.getReturnType().getName().equals("java.lang.String")
							|| method.getReturnType().getName().equals("java.lang.Long")) {
						
						// ignore following methods
						if ("ActionId".equals(name)) continue;
						if ("RfcMessageId".equals(name)) continue;
						if ("FolderName".equals(name)) continue;
						if ("BodyContentType".equals(name)) continue;
						if ("CarrierCode".equals(name)) continue;
						if ("MsgSourceId".equals(name)) continue;
						if ("RenderId".equals(name)) continue;
						if (name.startsWith("Dsn")) continue;
						if (name.startsWith("Des")) continue;
						if (name.startsWith("Dia")) continue;
						if (name.startsWith("Dis")) continue;
						if (name.endsWith("AsString")) continue;
						// end of ignore
						
						methodNameList.add(name);
					}
					else if (method.getReturnType().getCanonicalName().equals("javax.mail.Address[]")) {
						/* add a prefix so e-mail address fields are listed together */
						//methodNameList.add("Email_" + name); // problem with existing data
						methodNameList.add(name);
					}
				}
			}
		}
		Collections.sort(methodNameList);
		return methodNameList;
	}
	
	public static String invokeMethod(MessageBean msgBean, String name) {
		if (msgBean == null || name == null) {
			logger.warn("invokeMethod() - Either msgBean or name is null.");
			return null;
		}
		
		if (name.startsWith("Email_")) {
			// strip off prefix
			name = name.substring(6); 
		}
		name = "get" + name;
		
		try {
			if (isDebugEnabled)
				logger.debug("invoking method: " + name + "()");
			Method method = msgBean.getClass().getMethod(name, (Class[])null);
			Object obj =  method.invoke(msgBean, (Object[])null);
			if (obj instanceof String) {
				return (String) obj;
			}
			else if (obj instanceof Long) {
				return ((Long)obj).toString();
			}
			else if (obj instanceof Address[]) {
				return EmailAddrUtil.emailAddrToString((Address[])obj);
			}
			else if (obj != null) {
				logger.warn("invokeMethod() - invalid return type: " + obj.getClass().getName());
			}
		}
		catch (Exception e) {
			logger.error("invokeMethod() - Exception caught", e);
		}
		return null;
	}
	
	public static void main(String[] args) {
		List<String> methodNameList = getMessageBeanMethodNames();
		StringBuffer sb = new StringBuffer();
		for (int i=0; i<methodNameList.size(); i++) {
			sb.append(methodNameList.get(i) + LF);
		}
		System.out.println(sb.toString());
		
		MessageBean msgBean = new MessageBean();
		msgBean.setBody("test body text");
		System.out.println("Invoke getBody(): " + invokeMethod(msgBean, "Body"));
		msgBean.setMsgId(Long.valueOf(100));
		System.out.println("Invoke getMsgId(): " + invokeMethod(msgBean, "MsgId"));
		
		try {
			Address addr = new InternetAddress("bad.address@localhost");
			Message msg = createMimeMessage(msgBean, addr, "5.1.1" + LF + LF);
			MessageBeanBuilder.processPart(msg, null);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

}
