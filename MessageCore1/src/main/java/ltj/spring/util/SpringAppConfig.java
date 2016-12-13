package ltj.spring.util;

import java.util.Properties;

import javax.mail.MessagingException;

import org.apache.log4j.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.scheduling.annotation.EnableScheduling;

import ltj.message.bean.SimpleEmailSender;

@Configuration
@ComponentScan(basePackages = {"ltj.message.dao", "ltj.message.bo", "ltj.jbatch"})
//@org.springframework.context.annotation.Import(SpringJmsConfig.class)
@ImportResource({"classpath:/properties-config.xml", "classpath:/spring-mysql-config.xml"})
@EnableJms
@EnableScheduling
public class SpringAppConfig {
	protected final static Logger logger = Logger.getLogger(SpringAppConfig.class);

	@Bean
	public SimpleEmailSender simpleEmailSender() {
		Properties smtpProps = new Properties();
		smtpProps.setProperty("smtphost","localhost");
		smtpProps.setProperty("smtpport", "-1");
		smtpProps.setProperty("userid", "jwang");
		smtpProps.setProperty("password", "jwang");
		smtpProps.setProperty("persistent", "no");
		try {
			return new SimpleEmailSender(smtpProps);
		} catch (NumberFormatException e) {
			throw new java.lang.IllegalStateException(e);
		} catch (MessagingException e) {
			throw new java.lang.IllegalStateException(e);
		}
	}
}
