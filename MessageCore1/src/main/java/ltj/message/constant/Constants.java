package ltj.message.constant;

import ltj.data.preload.MailingListEnum;

public final class Constants {
	
	public final static String DEFAULT_USER_ID = "MsgMaint";
	public final static String DEFAULT_DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
	public final static String DEFAULT_CLIENTID = "System";
	
	public final static String VENDER_DOMAIN_NAME = "Emailsphere.com";
	public final static String VENDER_SUPPORT_EMAIL = "support@" + VENDER_DOMAIN_NAME;
	public final static String POWERED_BY_HTML_TAG = "<div style='color: blue;'>Powered by "
			+ "<a style='color: darkblue;' href='http://www." + VENDER_DOMAIN_NAME + "' target='_blank'>"
			+ VENDER_DOMAIN_NAME + "</a></div>";
	public final static String POWERED_BY_TEXT = "Powered by " + VENDER_DOMAIN_NAME;
	public static final boolean EmbedPoweredByToFreeVersion = true;
	
	public final static String ADMIN_ROLE = "admin";
	public final static String USER_ROLE = "user";
	
	public final static String FreePremiumUpgradeTemplateId = "FreePremiumUpgradeReply";
	public final static String FreePremiumUpgradeRuleName = "FreePremiumUpgrade";

	//
	// define mail types for rule engine
	//
	public static final String SMTP_MAIL = "smtpmail";
	public static final String WEB_MAIL = "webmail";
	
	public final static String Y = CodeType.Y.value();
	public final static String N = CodeType.N.value();
	public final static String YES = CodeType.Yes.value();
	public final static String NO = CodeType.No.value();
	
	// suspend email address after 5 times of consecutive bounces
	public final static int BOUNCE_SUSPEND_THRESHOLD = 5;

	// demo mailing list
	public final static String DEMOLIST1_ADDR = MailingListEnum.SMPLLST1.getAcctName() + "@localhost";
	public final static String DEMOLIST2_ADDR = MailingListEnum.SMPLLST2.getAcctName() + "@localhost";	
	public final static String DEMOLIST1_NAME = MailingListEnum.SMPLLST1.name();
	public final static String DEMOLIST2_NAME = MailingListEnum.SMPLLST2.name();
	
	//
	// define VERP constants
	//
	public final static String VERP_BOUNCE_ADDR_XHEADER = "X-VERP_Bounce_Addr";
	public final static String VERP_BOUNCE_EMAILID_XHEADER = "X-VERP_Bounce_EmailId";
	public final static String VERP_REMOVE_ADDR_XHEADER = "X-VERP_Remove_Addr";
	public final static String VERP_REMOVE_LISTID_XHEADER = "X-VERP_Remove_ListId";
	
	// define message related constants 
	public static final String MESSAGE_TRUNCATED = "=== message truncated ===";
	public static final String MSG_DELIMITER_BEGIN = "--- ";
	public static final String MSG_DELIMITER_END = " wrote:";
	public static final String CRLF = "\r\n";
	public static final String DASHES_OF_33 = "---------------------------------"; 

}
