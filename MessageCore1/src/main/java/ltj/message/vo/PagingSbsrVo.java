package ltj.message.vo;

import ltj.message.util.StringUtil;

public final class PagingSbsrVo extends PagingAddrVo implements java.io.Serializable, Cloneable {
	private static final long serialVersionUID = -525908919519049524L;
	private String listId = null;
	private Boolean subscribed = null;
	
	public static void main(String[] args) {
		PagingSbsrVo vo1 = new PagingSbsrVo();
		vo1.printMethodNames();
		PagingSbsrVo vo2 = new PagingSbsrVo();
		vo2.setListId("SMPLLST1");
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

}
