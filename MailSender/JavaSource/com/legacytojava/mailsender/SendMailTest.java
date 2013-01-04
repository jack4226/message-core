package com.legacytojava.mailsender;

import java.io.File;
import java.util.Date;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.apache.log4j.Logger;

import com.legacytojava.jbatch.SpringUtil;
import com.legacytojava.message.bean.MessageBean;
import com.legacytojava.message.bean.SimpleEmailSender;
import com.legacytojava.message.constant.MailProtocol;

public class SendMailTest {
	static final Logger logger = Logger.getLogger(SendMailTest.class);
	
	public static void main(String[] args) {
		int loops = 5; //100
		SendMailTest test = new SendMailTest();
		try {
			for (int i=0; i<loops; i++) {
				test.sendNotify("Test Subject " + i, "Test Body Message " + i);
				if (i<2) {
					test.sendMultiPart();
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	void sendNotify(String subject, String body) {
		try {
			MessageBean mBean = new MessageBean();
			try {
				mBean.setFrom(InternetAddress.parse("twang@localhost", false));
				mBean.setTo(InternetAddress.parse("testto@localhost", false));
				mBean.setTo(InternetAddress.parse("support@localhost", false));
				//mBean.setCc(InternetAddress.parse("jwang,twang", false));
			}
			catch (AddressException e) {
				logger.error("AddressException caught", e);
			}
			mBean.setSubject(subject); // + " " + new Date());
			mBean.setValue(body);
			SimpleEmailSender mSend = (SimpleEmailSender) SpringUtil.getAppContext().getBean("simpleEmailSender");
			if (mSend != null)
				mSend.sendMessage(mBean);
			else
				logger.info("JbMain.sendNotify(): message not sent, mSend not initialized.");
		}
		catch (Exception e) {
			logger.error("Exception caught during sendNotify()", e);
		}
	}
	
	/*
	 * Please refer to RFC 2111 for embedded references to other parts of the
	 * same message.
	 */
	void sendMultiPart() throws MessagingException {
		Properties smtpProps = new Properties();
		smtpProps.setProperty("smtphost","localhost");
		smtpProps.setProperty("smtpport","25");
		smtpProps.setProperty("protocol",MailProtocol.POP3);
		smtpProps.setProperty("user","jwang");
		smtpProps.setProperty("password","jwang");
		smtpProps.setProperty("host",smtpProps.getProperty("smtphost"));
		smtpProps.putAll(System.getProperties());

		Session session = Session.getDefaultInstance(smtpProps, null);
		session.setDebug(true);

		// construct the message 
		Message msg = new MimeMessage(session);

		msg.addFrom(InternetAddress.parse("twang",false));
		//msg.addRecipients(Message.RecipientType.TO, InternetAddress.parse("jwang",false));
		msg.addRecipients(Message.RecipientType.TO, InternetAddress.parse("testto@localhost",false));
		msg.setSubject("Test Multipart message");		
		msg.setSentDate(new Date());
		
		// create wrapper for multipart/mixed part
		Multipart mp = new MimeMultipart("alternative");
		msg.setContent(mp);

		// create the plain text
		BodyPart plainText = new MimeBodyPart();
		String bodyText = "This is the plain text version of the mail";
		//plainText.setText(bodyText);
		plainText.setContent(bodyText, "text/plain");
		mp.addBodyPart(plainText);

		BodyPart related = new MimeBodyPart();
		MimeMultipart mr = new MimeMultipart("related");
		related.setContent(mr);
		mp.addBodyPart(related);

		BodyPart html = new MimeBodyPart();
		String htmlText = "<html><head></head><body><h1>This is the HMTL version of the mail."
				+ "</h1><img src=\"cid:0001\"></body></html>";
		html.setContent(htmlText, "text/html");
		mr.addBodyPart(html);
		
		BodyPart img = new MimeBodyPart();
		img.setHeader("Content-ID", "0001");
		//img.setDisposition(Part.INLINE);
		img.setDisposition(Part.INLINE + "; filename=one.gif");
		ClassLoader loader = ClassLoader.getSystemClassLoader();
		java.net.URL url = loader.getResource("com/legacytojava/mailsender/htmldocs/images/one.gif");
		File file = new File(url.getPath());
		FileDataSource fds = new FileDataSource(file);
		img.setDataHandler(new DataHandler(fds));
		mr.addBodyPart(img);
		
		// send the thing off
		Transport.send(msg);
	}
}
