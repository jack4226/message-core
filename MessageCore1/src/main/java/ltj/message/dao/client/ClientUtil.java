package ltj.message.dao.client;

import static ltj.message.constant.Constants.DEFAULT_CLIENTID;

import java.util.Calendar;
import java.util.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ltj.jbatch.common.ProductKey;
import ltj.jbatch.obsolete.ProductUtil;
import ltj.message.util.StringUtil;
import ltj.message.util.TimestampUtil;
import ltj.message.vo.ClientVo;
import ltj.spring.util.SpringUtil;

public final class ClientUtil {
	static final Logger logger = LogManager.getLogger(ClientUtil.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();
	
	private static ClientDao clientDao = null;
	private static final int TRIAL_DAYS = 30;
	
	private ClientUtil() {
		// static only
	}
	
	public static void main(String[] args){
		try {
			System.out.println("Trial Ended? " + isTrialPeriodEnded());
			System.out.println("ProductKey Valid? " + isProductKeyValid());
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		System.exit(0);
	}

	public static boolean isTrialPeriodEnded() {
		String systemId = getClientDao().getSystemId();
		if (systemId != null) {
			String db2ts = null;
			try {
				db2ts = TimestampUtil.decStrToDb2(systemId);
			}
			catch (NumberFormatException e) {
				logger.error("Failed to convert the SystemId: " + systemId, e);
				return true;
			}
			Date date = null;
			try {
				date = TimestampUtil.db2ToDate(db2ts);
			}
			catch (NumberFormatException e) {
				logger.error("Failed to parse the timestamp: " + db2ts, e);
				return true;
			}
			Calendar now = Calendar.getInstance();
			Calendar exp = Calendar.getInstance();
			exp.setTime(date);
			exp.add(Calendar.DAY_OF_YEAR, TRIAL_DAYS);
			if (now.before(exp)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Check product key
	 * @return true if either productkey.txt or clients.SystemKey is valid.
	 */
	public static boolean isProductKeyValid() {
		String key = getClientDao().getSystemKey();
		return (ProductKey.validateKey(key) | ProductUtil.isProductKeyValid());
	}

	public static ClientVo getDefaultClientVo() {
		return getSiteProfile(DEFAULT_CLIENTID);
	}
	
	public static ClientVo getClientVo(String clientId) {
		return getSiteProfile(clientId);
	}
	
	public static ClientVo getSiteProfile(String clientId) {
		if (StringUtil.isEmpty(clientId)) {
			clientId = DEFAULT_CLIENTID;
		}
		ClientVo vo = getClientDao().getByClientId(clientId);
		if (vo != null) {
			return vo;
		}
		else {
			vo = getClientDao().getByClientId(DEFAULT_CLIENTID);
			if (vo != null) {
				return vo;
			}
			else {
				throw new RuntimeException(DEFAULT_CLIENTID + " missing in client_tbl table.");
			}
		}
	}
	
	private static ClientDao getClientDao() {
		if (clientDao == null) {
			clientDao = SpringUtil.getDaoAppContext().getBean(ClientDao.class);
		}
		return clientDao;
	}
}
