package ltj.data.preload;

import ltj.message.constant.EmailVariableType;

/*
 * define sample email variables
 */
public enum EmailVariableEnum implements EnumInterface {

	CustomerName(EmailVariableType.Custom, "customer_tbl", "FirstName,LastName", false, "Valued Customer",
			"SELECT CONCAT(c.FirstName, ' ', c.LastName) as ResultStr FROM customer_tbl c, email_address e where e.emailaddrId=c.emailAddrId and e.emailAddrId=?;","ltj.message.external.CustomerNameResolver"),
	CustomerFirstName(EmailVariableType.Custom, "customer_tbl", "FirstName", false, "Valued Customer",
			"SELECT c.FirstName as ResultStr FROM customer_tbl c, email_address e where e.emailaddrId=c.emailAddrId and e.emailAddrId=?;","ltj.message.external.CustomerNameResolver"),
	CustomerLastName(EmailVariableType.Custom, "customer_tbl", "LastName", false, "Valued Customer",
			"SELECT c.LastName as ResultStr FROM customer_tbl c, email_address e where e.emailaddrId=c.emailAddrId and e.emailAddrId=?;","ltj.message.external.CustomerNameResolver"),
	CustomerAddress(EmailVariableType.Custom, "customer_tbl", "StreetAddress", false, "",
			"SELECT CONCAT_WS(',',c.StreetAddress2,c.StreetAddress) as ResultStr FROM customer_tbl c, email_address e where e.emailaddrId=c.emailAddrId and e.emailAddrId=?;","ltj.message.external.CustomerNameResolver"),
	CustomerCityName(EmailVariableType.Custom, "customer_tbl", "CityName", false, "",
			"SELECT c.CityName as ResultStr FROM customer_tbl c, email_address e where e.emailaddrId=c.emailAddrId and e.emailAddrId=?;","ltj.message.external.CustomerNameResolver"),
	CustomerStateCode(EmailVariableType.Custom, "customer_tbl", "StateCode", false, "",
			"SELECT CONCAT_WS(',',c.StateCode,c.ProvinceName) as ResultStr FROM customer_tbl c, email_address e where e.emailaddrId=c.emailAddrId and e.emailAddrId=?;","ltj.message.external.CustomerNameResolver"),
	CustomerZipCode(EmailVariableType.Custom, "customer_tbl", "ZipCode", false, "",
			"SELECT CONCAT_WS('-',c.ZipCode5,c.ZipCode4) as ResultStr FROM customer_tbl c, email_address e where e.emailaddrId=c.emailAddrId and e.emailAddrId=?;","ltj.message.external.CustomerNameResolver"),
	CustomerCountry(EmailVariableType.Custom, "customer_tbl", "Country", false, "",
			"SELECT c.Country as ResultStr FROM customer_tbl c, email_address e where e.emailaddrId=c.emailAddrId and e.emailAddrId=?;","ltj.message.external.CustomerNameResolver"),

	EmailOpenCountImgTag(EmailVariableType.System, "", "", true,
			"<img src='${WebSiteUrl}/msgopen.jsp?msgid=${BroadcastMsgId}&listid=${MailingListId}&sbsrid=${SubscriberAddressId}' alt='' height='1' width='1'>", null, null),
	EmailClickCountImgTag(EmailVariableType.System, "", "", true,
			"<img src='${WebSiteUrl}/msgclick.jsp?msgid=${BroadcastMsgId}&listid=${MailingListId}&sbsrid=${SubscriberAddressId}' alt='' height='1' width='1'>", null, null),
	EmailUnsubscribeImgTag(EmailVariableType.System, "", "", true,
			"<img src=='${WebSiteUrl}/msgunsub.jsp?msgid=${BroadcastMsgId}&listid=${MailingListId}&sbsrid=${SubscriberAddressId}' alt='' height='1' width='1'>", null, null),
	EmailTrackingTokens(EmailVariableType.System, "", "", true,
			"msgid=${BroadcastMsgId}&listid=${MailingListId}&sbsrid=${SubscriberAddressId}", null, null),
	FooterWithUnsubLink(EmailVariableType.System, "", "", true,
			"<p>To unsubscribe from this mailing list, " + LF +
			"<a target='_blank' href='${WebSiteUrl}/MsgUnsubPage.jsp?msgid=${BroadcastMsgId}&listid=${MailingListId}&sbsrid=${SubscriberAddressId}'>click here</a>.</p>", null, null),
	FooterWithUnsubAddr(EmailVariableType.System, "", "", true,
			"To unsubscribe from this mailing list, send an e-mail to: ${MailingListAddress}" + LF +
			"with \"unsubscribe\" (no quotation marks) in the subject.", null, null),
	SubscribeURL(EmailVariableType.System, "", "", true,
			"${WebSiteUrl}/subscribe.jsp?sbsrid=${SubscriberAddressId}", null, null),
	ConfirmationURL(EmailVariableType.System, "", "", true,
			"${WebSiteUrl}/confirmsub.jsp?sbsrid=${_EncodedSubcriberId}&listids=${_SubscribedListIds}&sbsraddr=${SubscriberAddress}", null, null),
	UnsubscribeURL(EmailVariableType.System, "", "", true,
			"${WebSiteUrl}/unsubscribe.jsp?sbsrid=${_EncodedSubcriberId}&listids=${_SubscribedListIds}&sbsraddr=${SubscriberAddress}", null, null),
	UserProfileURL(EmailVariableType.System, "", "", true,
			"${WebSiteUrl}/userprofile.jsp?sbsrid=${SubscriberAddressId}",null,null),
	TellAFriendURL(EmailVariableType.System, "", "", true,
			"${WebSiteUrl}/referral.jsp?msgid=${BroadcastMsgId}&listid=${MailingListId}&sbsrid=${SubscriberAddressId}", null, null),
	SiteLogoURL(EmailVariableType.System, "", "", true,
			"${WebSiteUrl}/images/logo.gif", null, null);

	private EmailVariableType variableType;
	private String tableName;
	private String columnName;
	private boolean isBuiltin;
	private String defaultValue;
	private String variableQuery;
	private String variableProcName;

	private EmailVariableEnum(EmailVariableType variableType, String tableName,
			String columnName, boolean isBuiltin, String defaultValue,
			String variableQuery, String variableProcName) {
		this.variableType = variableType;
		this.tableName = tableName;
		this.columnName = columnName;
		this.isBuiltin = isBuiltin;
		this.defaultValue = defaultValue;
		this.variableQuery = variableQuery;
		this.variableProcName = variableProcName;
	}

	public EmailVariableType getVariableType() {
		return variableType;
	}

	public void setVariableType(EmailVariableType variableType) {
		this.variableType = variableType;
	}

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public String getColumnName() {
		return columnName;
	}

	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}

	public boolean isBuiltin() {
		return isBuiltin;
	}

	public void setBuiltin(boolean isBuiltin) {
		this.isBuiltin = isBuiltin;
	}

	public String getDefaultValue() {
		return defaultValue;
	}

	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}

	public String getVariableQuery() {
		return variableQuery;
	}

	public void setVariableQuery(String variableQuery) {
		this.variableQuery = variableQuery;
	}

	public String getVariableProcName() {
		return variableProcName;
	}

	public void setVariableProcName(String variableProcName) {
		this.variableProcName = variableProcName;
	}
	
} 
