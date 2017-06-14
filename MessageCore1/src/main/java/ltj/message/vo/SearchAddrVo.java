package ltj.message.vo;

import java.io.Serializable;

import ltj.message.constant.RuleCriteria;

public class SearchAddrVo implements Serializable, SearchVo {
	private static final long serialVersionUID = 3511831652927641084L;

	private String emailAddr = null;

	private final PagingVo pagingVo;
	
	public SearchAddrVo(PagingVo pagingVo) {
		this.pagingVo = pagingVo;
	}
	
	@Override
	public PagingVo getPagingVo() {
		return pagingVo;
	}

	public String getEmailAddr() {
		return emailAddr;
	}

	public void setEmailAddr(String emailAddr) {
		this.emailAddr = emailAddr;
		pagingVo.setSearchCriteria(PagingVo.Column.emailAddr, new PagingVo.Criteria(RuleCriteria.REG_EX, emailAddr, PagingVo.MatchBy.AnyWords));
	}

}
