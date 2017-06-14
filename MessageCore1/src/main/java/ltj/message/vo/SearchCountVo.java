package ltj.message.vo;

import java.io.Serializable;

import ltj.message.constant.RuleCriteria;

public class SearchCountVo implements Serializable, SearchVo {
	private static final long serialVersionUID = 3157556685285797284L;

	private Integer sentCount = null;
	private Integer openCount = null;
	private Integer clickCount = null;
	private String fromEmailAddr = null;

	private final PagingVo pagingVo;
	
	public SearchCountVo(PagingVo pagingVo) {
		this.pagingVo = pagingVo;
	}
	
	@Override
	public PagingVo getPagingVo() {
		return pagingVo;
	}

	public Integer getSentCount() {
		return sentCount;
	}

	public void setSentCount(Integer sentCount) {
		this.sentCount = sentCount;
		pagingVo.setSearchCriteria(PagingVo.Column.sentCount, new PagingVo.Criteria(RuleCriteria.GE, sentCount));
	}

	public Integer getOpenCount() {
		return openCount;
	}

	public void setOpenCount(Integer openCount) {
		this.openCount = openCount;
		pagingVo.setSearchCriteria(PagingVo.Column.openCount, new PagingVo.Criteria(RuleCriteria.GE, openCount));
	}

	public Integer getClickCount() {
		return clickCount;
	}

	public void setClickCount(Integer clickCount) {
		this.clickCount = clickCount;
		pagingVo.setSearchCriteria(PagingVo.Column.clickCount, new PagingVo.Criteria(RuleCriteria.GE, clickCount));
	}

	public String getFromEmailAddr() {
		return fromEmailAddr;
	}

	public void setFromEmailAddr(String fromEmailAddr) {
		this.fromEmailAddr = fromEmailAddr;
		pagingVo.setSearchCriteria(PagingVo.Column.emailAddr, new PagingVo.Criteria(RuleCriteria.REG_EX, fromEmailAddr, PagingVo.MatchBy.AnyWords));
	}

}
