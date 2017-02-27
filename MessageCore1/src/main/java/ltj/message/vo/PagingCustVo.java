package ltj.message.vo;

import ltj.message.util.StringUtil;


public final class PagingCustVo extends PagingAddrVo implements java.io.Serializable, Cloneable {
	private static final long serialVersionUID = 2702501767172625606L;
	private String clientId = null;
	private String ssnNumber = null;
	private String dayPhone = null;
	private String firstName = null;
	private String lastName = null;
	
	public static void main(String[] args) {
		PagingCustVo vo1 = new PagingCustVo();
		vo1.printMethodNames();
		PagingCustVo vo2 = new PagingCustVo();
		vo2.setClientId("System");
		vo1.setSsnNumber(" 123-45-6789 ");
		vo2.setStatusId("A");
		StringUtil.stripAll(vo1);
		System.out.println(vo1.toString());
		System.out.println(vo1.equalsToSearch(vo2));
		System.out.println(vo1.listChanges());
	}

	public String getClientId() {
		return clientId;
	}
	public void setClientId(String clientId) {
		this.clientId = clientId;
	}
	public String getSsnNumber() {
		return ssnNumber;
	}
	public void setSsnNumber(String ssnNumber) {
		this.ssnNumber = ssnNumber;
	}
	public String getDayPhone() {
		return dayPhone;
	}
	public void setDayPhone(String dayPhone) {
		this.dayPhone = dayPhone;
	}
	public String getFirstName() {
		return firstName;
	}
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}
	public String getLastName() {
		return lastName;
	}
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
}
