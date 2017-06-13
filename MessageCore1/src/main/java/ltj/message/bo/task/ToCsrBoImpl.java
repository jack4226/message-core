package ltj.message.bo.task;

import javax.jms.JMSException;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import ltj.data.preload.RuleDataTypeEnum;
import ltj.message.bean.MessageBean;
import ltj.message.dao.action.MsgDataTypeDao;
import ltj.message.exception.DataValidationException;
import ltj.message.util.StringUtil;
import ltj.message.vo.action.MsgDataTypeVo;

@Component("toCsrBo")
@Scope(value="prototype")
@Lazy(value=true)
public class ToCsrBoImpl extends TaskBaseAdaptor {
	static final Logger logger = Logger.getLogger(ToCsrBoImpl.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();
	
	@Autowired
	private MsgDataTypeDao msgDataTypeDao;

	/**
	 * Forward the message to CSR input queue. The queue name should be stored
	 * in "DataTypeValues" of MsgAction table. Queue name ruleEngineOutput
	 * will be used if the value from the table is not defined or not valid.
	 * 
	 * @return the JMS Message Id (a String) from the message that were sent to
	 *         the output queue.
	 * 
	 * @see ltj.message.bo.task.TaskBaseBo#process(ltj.message.bean.MessageBean)
	 */
	public Object process(MessageBean messageBean) throws DataValidationException, JMSException {
		if (isDebugEnabled) {
			logger.debug("Entering process() method...");
		}
		if (messageBean == null) {
			throw new DataValidationException("input MessageBean is null");
		}
		if (getArgumentList().size() == 0) {
			logger.warn("Arguments is not valued, use default queue: ruleEngineOutput");
		}
		else if (isDebugEnabled) {
			logger.debug("Arguments passed: " + taskArguments);
		}
		
		String queueName = null;
		if (getArgumentList(taskArguments).size() > 0) {
			if (taskArguments[0].startsWith("$")) { // variable name
				// retrieve the real queue name (Spring JMS template) from database:
				MsgDataTypeVo vo = msgDataTypeDao.getByTypeValuePair(RuleDataTypeEnum.QUEUE_NAME.name(), taskArguments[0]);
				if (vo != null) {
					if (StringUtil.isNotEmpty(vo.getMiscProperties())) {
						queueName = vo.getMiscProperties();
					}
				}
				else {
					throw new DataValidationException("Record not found by: " + RuleDataTypeEnum.QUEUE_NAME.name() + "/" + taskArguments[0]);
				}
			}
			else { // should be a JMS template name
				queueName = taskArguments[0];
			}
		}
		// Configure JmsProcessor to use provided JMS template
		if (StringUtils.isNotBlank(queueName)) {
			setTargetQueue(queueName);
		}
		else {
			setTargetToCsrWorkQueue(); // use default queue
		}
		String correlid = "ToCsrBo." + (messageBean.getMsgRefId() == null ? "-1" : messageBean.getMsgRefId());
			// set correlation id. To be used in the future.
		String jmsMsgId = jmsProcessor.writeMsg(messageBean, correlid, false);
		/*
		 * jmsMsgId returned from the 1st message could be used as correlation id
		 * by the subsequent messages in the same group
		 */
		if (isDebugEnabled) {
			logger.debug("Jms Message Id returned: " + jmsMsgId);
		}
		return jmsMsgId;
	}
}
