package ltj.message.vo;

import java.io.Serializable;

public class PagingAddrVo extends PagingVo implements Serializable {
	private static final long serialVersionUID = -3937664465278379794L;
	
	private String emailAddr = null;

	public static void main(String[] args) {
		PagingAddrVo vo1 = new PagingAddrVo();
		vo1.printMethodNames();
		System.out.println(vo1.toString());
		PagingAddrVo vo2 = new PagingAddrVo();
		vo2.setStatusId("A");
		vo1.setEmailAddr("abcd");
		System.out.println(vo1.equalsToSearch(vo2));
		System.out.println(vo1.listChanges());
	}
	
	public String getEmailAddr() {
		return emailAddr;
	}

	public void setEmailAddr(String emailAddr) {
		this.emailAddr = emailAddr;
	}
	
}