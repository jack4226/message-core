package ltj.message.vo.inbox;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Date;

import ltj.data.preload.RuleNameEnum;
import ltj.message.vo.PagingVo;

public final class SearchFieldsVo extends PagingVo implements Serializable {
	private static final long serialVersionUID = 8888455019361283024L;
	
	public static enum MsgType {All, Received, Sent, Draft, Closed, Trash};
	public static enum RuleName {All};
	
	private MsgType msgType = null;
	private String ruleName = RuleName.All.toString();
	private Long fromAddrId = null;
	private Long toAddrId = null;
	private String fromAddr = null;
	private String toAddr = null;
	private String subject = null;
	private String body = null;
	
	private Boolean read = null;
	private Boolean flagged = null;
	private Date recent = null;
	
	// define paging context
	public static final int MSG_INBOX_PAGE_SIZE = 25;
	protected Timestamp receivedTimeFirst = null;
	protected Timestamp receivedTimeLast = null;
	// end of paging
	
	public static void main(String[] args) {
		SearchFieldsVo vo1 = new SearchFieldsVo();
		vo1.printMethodNames();
		System.out.println(vo1.toString());
		SearchFieldsVo vo2 = new SearchFieldsVo();
		vo2.setMsgType(MsgType.Closed);
		vo1.setSubject("auto-reply");
		vo2.setRuleName(RuleNameEnum.HARD_BOUNCE.name());
		vo1.setBody("test message");
		vo2.setFromAddrId(10L);
		vo1.setFromAddr("test@test.com");
		vo2.setToAddr("to@to.com");
		vo1.setToAddrId(20L);
		System.out.println(vo1.equalsLevel1(vo2));
		System.out.println(vo1.listChanges());
	}
	
	public SearchFieldsVo() {
		super();
		setPageSize(MSG_INBOX_PAGE_SIZE);
		init();
	}
	
	private void init() {
		msgType = MsgType.Received;
	}
	
	public void resetFlags() {
		read = null;
		flagged = null;
		recent = null;
	}
	
	public void resetAll() {
		init();
		resetFlags();
		resetPageContext();
	}


	public boolean equalsLevel1(SearchFieldsVo vo) {
		return equalsToSearch(vo);
	}
	
	public boolean equalsLevel1_deprecated(SearchFieldsVo vo) {
		getLogList().clear();
		if (this == vo) return true;
		if (vo == null) return false;
		if (this.msgType == null) {
			if (vo.msgType != null) {
				addChangeLog("MsgType", this.msgType, vo.msgType);
			}
		}
		else {
			if (!this.msgType.equals(vo.msgType)) {
				addChangeLog("MsgType", this.msgType, vo.msgType);
			}
		}
		if (this.ruleName == null) {
			if (vo.ruleName != null) {
				addChangeLog("RuleName", this.ruleName, vo.ruleName);
			}
		}
		else {
			if (!this.ruleName.equals(vo.ruleName)) {
				addChangeLog("RuleName", this.ruleName, vo.ruleName);
			}
		}
		if (this.toAddrId == null) {
			if (vo.toAddrId != null) {
				addChangeLog("ToAddrId", this.toAddrId, vo.toAddrId);
			}
		}
		else {
			if (!this.toAddrId.equals(vo.toAddrId)) {
				addChangeLog("ToAddrId", this.toAddrId, vo.toAddrId);
			}
		}
		if (this.fromAddrId == null) {
			if (vo.fromAddrId != null) {
				addChangeLog("FromAddrId", this.fromAddrId, vo.fromAddrId);
			}
		}
		else {
			if (!this.fromAddrId.equals(vo.fromAddrId)) {
				addChangeLog("FromAddrId", this.fromAddrId, vo.fromAddrId);
			}
		}
		if (this.toAddr == null) {
			if (vo.toAddr != null) {
				addChangeLog("ToAddr", this.toAddr, vo.toAddr);
			}
		}
		else {
			if (!this.toAddr.equals(vo.toAddr)) {
				addChangeLog("ToAddr", this.toAddr, vo.toAddr);
			}
		}
		if (this.fromAddr == null) {
			if (vo.fromAddr != null) {
				addChangeLog("FromAddr", this.fromAddr, vo.fromAddr);
			}
		}
		else {
			if (!this.fromAddr.equals(vo.fromAddr)) {
				addChangeLog("FromAddr", this.fromAddr, vo.fromAddr);
			}
		}
		if (this.subject == null) {
			if (vo.subject != null) {
				addChangeLog("Subject", this.subject, vo.subject);
			}
		}
		else {
			if (!this.subject.equals(vo.subject)) {
				addChangeLog("Subject", this.subject, vo.subject);
			}
		}
		if (this.body == null) {
			if (vo.body != null) {
				addChangeLog("Body", this.body, vo.body);
			}
		}
		else {
			if (!this.body.equals(vo.body)) {
				addChangeLog("Body", this.body, vo.body);
			}
		}
		if (getLogList().size() > 0) return false;
		else return true;
	}
	
	public void copyLevel1To(SearchFieldsVo vo) {
		if (vo == null) {
			return;
		}
		vo.setMsgType(this.msgType);
		vo.setRuleName(this.ruleName);
		vo.setToAddrId(this.toAddrId);
		vo.setFromAddrId(this.fromAddrId);
		vo.setToAddr(this.toAddr);
		vo.setFromAddr(this.fromAddr);
		vo.setSubject(this.subject);
		vo.setBody(this.body);
	}
	
	public MsgType getMsgType() {
		return msgType;
	}
	public void setMsgType(MsgType msgType) {
		this.msgType = msgType;
	}
	public Long getFromAddrId() {
		return fromAddrId;
	}
	public void setFromAddrId(Long fromAddr) {
		this.fromAddrId = fromAddr;
	}
	public Long getToAddrId() {
		return toAddrId;
	}
	public void setToAddrId(Long toAddr) {
		this.toAddrId = toAddr;
	}
	public Boolean getRead() {
		return read;
	}
	public void setRead(Boolean read) {
		this.read = read;
	}
	public Boolean getFlagged() {
		return flagged;
	}
	public void setFlagged(Boolean flagged) {
		this.flagged = flagged;
	}
	public String getRuleName() {
		return ruleName;
	}
	public void setRuleName(String ruleName) {
		this.ruleName = ruleName;
	}
	public Date getRecent() {
		return recent;
	}
	public void setRecent(Date recent) {
		this.recent = recent;
	}
	public String getFromAddr() {
		return fromAddr;
	}
	public void setFromAddr(String fromEmailAddr) {
		this.fromAddr = fromEmailAddr;
	}
	public String getToAddr() {
		return toAddr;
	}
	public void setToAddr(String toEmailAddr) {
		this.toAddr = toEmailAddr;
	}
	public String getSubject() {
		return subject;
	}
	public void setSubject(String subject) {
		this.subject = subject;
	}
	public String getBody() {
		return body;
	}
	public void setBody(String body) {
		this.body = body;
	}
	public Timestamp getReceivedTimeFirst() {
		return receivedTimeFirst;
	}
	public void setReceivedTimeFirst(Timestamp receivedTimeFirst) {
		this.receivedTimeFirst = receivedTimeFirst;
	}
	public Timestamp getReceivedTimeLast() {
		return receivedTimeLast;
	}
	public void setReceivedTimeLast(Timestamp receivedTimeLast) {
		this.receivedTimeLast = receivedTimeLast;
	}
	public long getMsgIdFirst() {
		return nbrIdFirst;
	}
	public void setMsgIdFirst(long msgIdFirst) {
		this.nbrIdFirst = msgIdFirst;
	}
	public long getMsgIdLast() {
		return nbrIdLast;
	}
	public void setMsgIdLast(long msgIdLast) {
		this.nbrIdLast = msgIdLast;
	}
}