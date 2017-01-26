package ltj.message.bean;

import java.io.Serializable;

public class MessageContext implements Serializable {
	private static final long serialVersionUID = 4987450798001951974L;
	
	private javax.mail.Message[] messages;
	private MessageBean messageBean;
	private String[] taskArguments;
	private Integer[] rowIds;
	
	public MessageContext() {}
	
	public MessageContext(MessageBean messageBean) {
		this.messageBean = messageBean;
	}

	public javax.mail.Message[] getMessages() {
		return messages;
	}

	public void setMessages(javax.mail.Message[] messages) {
		this.messages = messages;
	}

	public MessageBean getMessageBean() {
		return messageBean;
	}

	public void setMessageBean(MessageBean messageBean) {
		this.messageBean = messageBean;
	}

	public String[] getTaskArguments() {
		return taskArguments;
	}

	public void setTaskArguments(String... taskArguments) {
		this.taskArguments = taskArguments;
	}

	public Integer[] getRowIds() {
		return rowIds;
	}
}
