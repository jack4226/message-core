package ltj.message.vo.inbox;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Date;

import org.apache.log4j.Logger;

import ltj.data.preload.FolderEnum;
import ltj.data.preload.RuleNameEnum;
import ltj.message.vo.PagingVo;
import ltj.message.vo.SearchVo;

public final class SearchFieldsVo implements Serializable, SearchVo {
	private static final long serialVersionUID = 8888455019361283024L;
	static Logger logger = Logger.getLogger(SearchFieldsVo.class);
	
	public static enum RuleName {All};
	
	private final PagingVo pagingVo;
	
	private FolderEnum folderType = null;
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
//	protected Timestamp receivedTimeFirst = null;
//	protected Timestamp receivedTimeLast = null;
	// end of paging
	
	public static void main(String[] args) {
		SearchFieldsVo vo1 = new SearchFieldsVo(new PagingVo());
		vo1.getPagingVo().printMethodNames();
		System.out.println(vo1.toString());
		SearchFieldsVo vo2 = new SearchFieldsVo(new PagingVo());
		vo2.setFolderType(FolderEnum.Closed);
		vo1.setSubject("auto-reply");
		vo2.setRuleName(RuleNameEnum.HARD_BOUNCE.name());
		vo1.setBody("test message");
		vo2.setFromAddrId(10L);
		vo1.setFromAddr("test@test.com");
		vo2.setToAddr("to@to.com");
		vo1.setToAddrId(20L);
		System.out.println(vo1.equalsLevel1(vo2));
		System.out.println(vo1.getPagingVo().listChanges());
	}
	
	public SearchFieldsVo(PagingVo pagingVo) {
		this.pagingVo = pagingVo;
		pagingVo.setPageSize(MSG_INBOX_PAGE_SIZE);
		init();
	}
	
	private void init() {
		folderType = FolderEnum.Inbox;
	}
	
	public void resetFlags() {
		read = null;
		flagged = null;
		recent = null;
	}
	
	public void resetAll() {
		init();
		resetFlags();
		pagingVo.resetPageContext();
	}

	@Override
	public PagingVo getPagingVo() {
		return pagingVo;
	}


	public boolean equalsLevel1(SearchFieldsVo vo) {
		if (this == vo) {
			return true;
		}
		if (vo == null) {
			return false;
		}
		
		getPagingVo().equalsToSearch(vo.getPagingVo());

		String className = this.getClass().getName();
		Method thisMethods[] = this.getClass().getMethods();
		for (int i = 0; i < thisMethods.length; i++) {
			Method method = (Method) thisMethods[i];
			String methodName = method.getName();
			Class<?>[] params = method.getParameterTypes();
			if (methodName.length() > 3 && methodName.startsWith("get") && params.length == 0) {
				Method voMethod = null;
				try {
					voMethod = vo.getClass().getMethod(methodName, params);
				}
				catch (NoSuchMethodException e) {
					logger.warn(className + ".equalsToSearch(): " + e.getMessage());
					return false;
				}
				try {
					Class<?> returnType = method.getReturnType();
					String returnTypeName = returnType.getName();
					if ((returnTypeName.endsWith("java.lang.String"))
							|| (returnTypeName.endsWith("java.lang.Integer"))
							|| (returnTypeName.endsWith("java.lang.Long"))
							|| (returnTypeName.endsWith("java.sql.Timestamp"))
							|| (returnTypeName.endsWith("java.sql.Date"))
							|| (returnType.equals(java.lang.Integer.TYPE))
							|| (returnType.equals(java.lang.Long.TYPE))
							|| (returnType.equals(java.lang.Character.TYPE))
							|| (returnTypeName.endsWith("FolderEnum"))) {
						Object thisValue = method.invoke((Object)this, (Object[])params);
						Object voValue = voMethod.invoke((Object)vo, (Object[])params);
						if (thisValue == null) {
							if (voValue != null) {
								getPagingVo().addChangeLog(methodName.substring(3), thisValue, voValue);
							}
						}
						else {
							if (!thisValue.equals(voValue)) {
								getPagingVo().addChangeLog(methodName.substring(3), thisValue, voValue);
							}
						}
					}
				}
				catch (Exception e) {
					logger.warn(className + ".equalsToSearch(): " + e.getMessage());
					return false;
				}
			}
		}
		if (getPagingVo().getLogList().size() > 0) {
			return false;
		}
		else {
			return true;
		}
	}
	
	
	public void copyLevel1To(SearchFieldsVo vo) {
		if (vo == null) {
			return;
		}
		vo.setFolderType(this.folderType);
		vo.setRuleName(this.ruleName);
		vo.setToAddrId(this.toAddrId);
		vo.setFromAddrId(this.fromAddrId);
		vo.setToAddr(this.toAddr);
		vo.setFromAddr(this.fromAddr);
		vo.setSubject(this.subject);
		vo.setBody(this.body);
	}
	
	public FolderEnum getFolderType() {
		return folderType;
	}
	public void setFolderType(FolderEnum msgType) {
		this.folderType = msgType;
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
//	public Timestamp getReceivedTimeFirst() {
//		return receivedTimeFirst;
//	}
//	public void setReceivedTimeFirst(Timestamp receivedTimeFirst) {
//		this.receivedTimeFirst = receivedTimeFirst;
//	}
//	public Timestamp getReceivedTimeLast() {
//		return receivedTimeLast;
//	}
//	public void setReceivedTimeLast(Timestamp receivedTimeLast) {
//		this.receivedTimeLast = receivedTimeLast;
//	}
}