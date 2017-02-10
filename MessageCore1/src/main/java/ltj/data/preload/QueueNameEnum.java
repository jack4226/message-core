package ltj.data.preload;

public enum QueueNameEnum {
	RMA_REQUEST_INPUT("rmaRequestInput"),
	CUSTOMER_CARE_INPUT("customerCareInput");
	
	private String queueName;
	private QueueNameEnum(String queueName) {
		this.queueName = queueName;
	}
	public String getQueueName() {
		return queueName;
	}
}
