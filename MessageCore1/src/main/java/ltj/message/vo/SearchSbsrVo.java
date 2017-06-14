package ltj.message.vo;

import java.io.Serializable;

import ltj.message.constant.RuleCriteria;

public class SearchSbsrVo implements Serializable, SearchVo {
	private static final long serialVersionUID = 2790759348012157600L;
	
	private String listId = null;
	private Boolean subscribed = null;
	private String emailAddr = null;

	private final PagingVo pagingVo;
	
	public SearchSbsrVo(PagingVo pagingVo) {
		this.pagingVo = pagingVo;
	}
	
	@Override
	public PagingVo getPagingVo() {
		return pagingVo;
	}

	public String getListId() {
		return listId;
	}

	public void setListId(String listId) {
		this.listId = listId;
		pagingVo.setSearchCriteria(PagingVo.Column.listId, new PagingVo.Criteria(RuleCriteria.EQUALS, listId));
	}

	public Boolean getSubscribed() {
		return subscribed;
	}

	public void setSubscribed(Boolean subscribed) {
		this.subscribed = subscribed;
		pagingVo.setSearchCriteria(PagingVo.Column.subscribed, new PagingVo.Criteria(RuleCriteria.EQUALS, subscribed, Boolean.class));
	}

	public String getEmailAddr() {
		return emailAddr;
	}

	public void setEmailAddr(String emailAddr) {
		this.emailAddr = emailAddr;
		pagingVo.setSearchCriteria(PagingVo.Column.emailAddr, new PagingVo.Criteria(RuleCriteria.REG_EX, emailAddr, PagingVo.MatchBy.AnyWords));
	}

}
