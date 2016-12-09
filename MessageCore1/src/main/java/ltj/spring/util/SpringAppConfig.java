package ltj.spring.util;

import org.apache.log4j.Logger;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.jms.annotation.EnableJms;

@Configuration
@ComponentScan(basePackages = {"ltj.message.dao", "ltj.message.bo", "ltj.jbatch"})
//@org.springframework.context.annotation.Import(SpringJmsConfig.class)
@ImportResource({"classpath:/properties-config.xml", "classpath:/spring-mysql-config.xml"})
@EnableJms
public class SpringAppConfig {
	protected final static Logger logger = Logger.getLogger(SpringAppConfig.class);

}
