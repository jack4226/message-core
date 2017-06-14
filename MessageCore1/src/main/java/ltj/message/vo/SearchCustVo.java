package ltj.message.vo;

import java.io.Serializable;

import ltj.message.constant.RuleCriteria;

public class SearchCustVo implements Serializable, SearchVo {
	private static final long serialVersionUID = -6416261203185063683L;
	
	private String clientId = null;
	private String ssnNumber = null;
	private String dayPhone = null;
	private String firstName = null;
	private String lastName = null;
	private String emailAddr = null;

	private final PagingVo pagingVo;
	
	public SearchCustVo(PagingVo pagingVo) {
		this.pagingVo = pagingVo;
	}
	
	@Override
	public PagingVo getPagingVo() {
		return pagingVo;
	}

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
		pagingVo.setSearchCriteria(PagingVo.Column.clientId, new PagingVo.Criteria(RuleCriteria.EQUALS, clientId));
	}

	public String getSsnNumber() {
		return ssnNumber;
	}

	public void setSsnNumber(String ssnNumber) {
		this.ssnNumber = ssnNumber;
		pagingVo.setSearchCriteria(PagingVo.Column.ssnNumber, new PagingVo.Criteria(RuleCriteria.EQUALS, ssnNumber));
	}

	public String getDayPhone() {
		return dayPhone;
	}

	public void setDayPhone(String dayPhone) {
		this.dayPhone = dayPhone;
		pagingVo.setSearchCriteria(PagingVo.Column.dayPhone, new PagingVo.Criteria(RuleCriteria.EQUALS, dayPhone));
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
		pagingVo.setSearchCriteria(PagingVo.Column.firstName, new PagingVo.Criteria(RuleCriteria.EQUALS, firstName));
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
		pagingVo.setSearchCriteria(PagingVo.Column.lastName, new PagingVo.Criteria(RuleCriteria.EQUALS, lastName));
	}

	public String getEmailAddr() {
		return emailAddr;
	}

	public void setEmailAddr(String emailAddr) {
		this.emailAddr = emailAddr;
		pagingVo.setSearchCriteria(PagingVo.Column.emailAddr, new PagingVo.Criteria(RuleCriteria.REG_EX, emailAddr, PagingVo.MatchBy.AnyWords));
	}

}
