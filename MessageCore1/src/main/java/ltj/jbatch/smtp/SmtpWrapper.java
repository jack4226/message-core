package ltj.jbatch.smtp;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ltj.jbatch.pool.ObjectPool;

/**
 * Provide call backs for ObjectPool to create SMTP Connections
 */
public class SmtpWrapper implements java.io.Serializable {
	private static final long serialVersionUID = 1993933036805249603L;
	static final Logger logger = LogManager.getLogger(SmtpWrapper.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();
	
	final ObjectPool objectPool;

	/**
	 * create a SmtpWrapper instance
	 * @param smtpPool - a SMTP pool
	 */
	public SmtpWrapper(ObjectPool smtpPool) {
		this.objectPool = smtpPool;
	}

	/**
	 * get a SmtpConnection from pool
	 * 
	 * @return SmtpConnection instance
	 */
	public SmtpConnection getConnection() {
		return (SmtpConnection) objectPool.getItem();
	}

	/**
	 * put a SmtpConnection back to pool
	 * 
	 * @param conn -
	 *            SmtpConnection
	 */
	public void returnConnection(SmtpConnection conn) {
		objectPool.returnItem(conn);
	}

	public ObjectPool getObjectPool() {
		return objectPool;
	}
}