package ltj.spring.util;

import java.util.Properties;

import javax.mail.MessagingException;

import org.apache.log4j.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.core.task.TaskExecutor;
import org.springframework.jdbc.support.lob.DefaultLobHandler;
import org.springframework.jdbc.support.lob.LobHandler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import ltj.message.bean.SimpleEmailSender;

@Configuration
@ComponentScan(basePackages = {"ltj.message.dao"})
//@org.springframework.context.annotation.Import(SpringJmsConfig.class)
@ImportResource({"classpath:/properties-config.xml", "classpath:/spring-mysql-config.xml"})
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
	
	@Bean
	public TaskExecutor taskExecuter() {
		ThreadPoolTaskExecutor task = new ThreadPoolTaskExecutor();
		task.setCorePoolSize(5);
		task.setMaxPoolSize(10);
		task.setQueueCapacity(100);
		return task;
	}
	
	@Bean
	public LobHandler lobHandler() {
		return new DefaultLobHandler();
	}
}
