package ltj.message.vo;

import ltj.message.constant.Constants;
import ltj.message.util.StringUtil;

public final class PagingSbsrVo extends PagingVo implements java.io.Serializable, Cloneable {
	private static final long serialVersionUID = -525908919519049524L;
	private String listId = null;
	private Boolean subscribed = null;
	private String emailAddr = null;
	
	public static void main(String[] args) {
		PagingSbsrVo vo1 = new PagingSbsrVo();
		vo1.printMethodNames();
		PagingSbsrVo vo2 = new PagingSbsrVo();
		vo2.setListId(Constants.DEMOLIST1_NAME);
		vo2.setStatusId("A");
		StringUtil.stripAll(vo1);
		System.out.println(vo1.toString());
		System.out.println(vo1.equalsToSearch(vo2));
		System.out.println(vo1.listChanges());
	}

	public String getListId() {
		return listId;
	}
	public void setListId(String listId) {
		this.listId = listId;
	}
	public Boolean getSubscribed() {
		return subscribed;
	}
	public void setSubscribed(Boolean subscribed) {
		this.subscribed = subscribed;
	}
	public String getEmailAddr() {
		return emailAddr;
	}
	public void setEmailAddr(String emailAddr) {
		this.emailAddr = emailAddr;
	}

}
