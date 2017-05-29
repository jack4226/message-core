package ltj.message.vo;

import java.io.Serializable;

public class PagingCountVo extends PagingVo implements Serializable {
	private static final long serialVersionUID = -3937664465278379794L;
	
	private Integer sentCount = null;
	private Integer openCount = null;
	private Integer clickCount = null;
	private String fromEmailAddr = null;

	public static void main(String[] args) {
		PagingCountVo vo1 = new PagingCountVo();
		vo1.printMethodNames();
		System.out.println(vo1.toString());
		PagingCountVo vo2 = new PagingCountVo();
		vo2.setStatusId("A");
		vo1.setFromEmailAddr("abcd");
		vo2.setSentCount(10);
		System.out.println(vo1.equalsToSearch(vo2));
		System.out.println(vo1.listChanges());
	}
	
	public Integer getSentCount() {
		return sentCount;
	}

	public void setSentCount(Integer sentCount) {
		this.sentCount = sentCount;
	}

	public Integer getOpenCount() {
		return openCount;
	}

	public void setOpenCount(Integer openCount) {
		this.openCount = openCount;
	}

	public Integer getClickCount() {
		return clickCount;
	}

	public void setClickCount(Integer clickCount) {
		this.clickCount = clickCount;
	}

	public String getFromEmailAddr() {
		return fromEmailAddr;
	}

	public void setFromEmailAddr(String emailAddr) {
		this.fromEmailAddr = emailAddr;
	}
	
}