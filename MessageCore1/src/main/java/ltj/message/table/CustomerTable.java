package ltj.message.table;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.GregorianCalendar;

import ltj.jbatch.common.PasswordUtil;
import ltj.jbatch.common.PasswordUtil.PasswordTuple;
import ltj.message.constant.Constants;
import ltj.message.constant.MobileCarrier;
import ltj.message.constant.StatusId;
import ltj.message.dao.customer.CustomerDao;
import ltj.message.dao.emailaddr.EmailAddressDao;
import ltj.message.main.CreateTableBase;
import ltj.message.vo.CustomerVo;
import ltj.message.vo.emailaddr.EmailAddressVo;
import ltj.spring.util.SpringUtil;

public class CustomerTable extends CreateTableBase {
	/**
	 * Creates a new instance of CustomerTables
	 * 
	 * @throws SQLException
	 * @throws ClassNotFoundException
	 */
	public CustomerTable() throws ClassNotFoundException, SQLException {
		init();
	}
	
	public void dropTables() {
		try {
			stm.execute("DROP TABLE customer_tbl");
			System.out.println("Dropped customer_tbl Table...");
		}
		catch (SQLException e) {
		}
		try {
			stm.execute("DROP TABLE cust_sequence");
			System.out.println("Dropped cust_sequence Table...");
		}
		catch (SQLException e) {
		}
	}
	
	public void createTables() throws SQLException {
		createCustSequenceTable();
		createCustomerTable();
	}

	void createCustomerTable() throws SQLException {
		try {
			stm.execute("CREATE TABLE customer_tbl ( "
					+ "row_id int AUTO_INCREMENT not null, "
					+ "cust_id varchar(16) NOT NULL, "	//1
					+ "client_id varchar(16) NOT NULL, "
					+ "ssn_number varchar(11), "
					+ "tax_id varchar(10), "
					+ "profession varchar(40), "	//5
					+ "first_name varchar(32), "
					+ "middle_name varchar(32), "
					+ "last_name varchar(32) NOT NULL, "
					+ "alias varchar(50), " // also known as Company Name
					+ "street_address varchar(60), "	//10
					+ "street_address2 varchar(40), "
					+ "city_name varchar(32), "
					+ "state_code char(2), "
					+ "zip_code5 char(5), "
					+ "zip_code4 varchar(4), "	//15
					+ "province_name varchar(30), "
					+ "postal_code varchar(11), "
					+ "country varchar(5), "
					+ "day_phone varchar(18), "
					+ "evening_phone varchar(18), "	//20
					+ "mobile_phone varchar(18), "
					+ "birth_date Date, "
					+ "start_date Date NOT NULL, "
					+ "end_date Date, "
					+ "mobile_carrier varchar(26), " // 25
					+ "msg_header varchar(100), "
					+ "msg_detail varchar(255), "
					+ "msg_optional varchar(100), "
					+ "msg_footer varchar(100), "
					+ "time_zone_code char(1), " // 30
					+ "memo_text varchar(255), "
					+ "status_id char(1) NOT NULL DEFAULT '" + StatusId.ACTIVE.value() + "', " 
					+ "security_question varchar(100), "
					+ "security_answer varchar(26), "
					+ "email_addr varchar(255) NOT NULL, " // 35
					+ "email_addr_id bigint NOT NULL, "
					+ "prev_email_addr varchar(255), "
					+ "password_change_time datetime(3), "
					+ "user_password varchar(40), "
					+ "password_salt varchar(16), "
					+ "updt_time datetime(3) NOT NULL, "  //40
					+ "updt_user_id varchar(10) NOT NULL, "
					+ "CONSTRAINT customer_pkey PRIMARY KEY (row_id), "
					+ "FOREIGN KEY customer_fk_client_id (client_id) REFERENCES client_tbl(client_id) ON DELETE CASCADE ON UPDATE CASCADE, "
					+ "INDEX customer_ix_client_id (client_id), "
					+ "FOREIGN KEY customer_fk_email_addr_id (email_addr_id) REFERENCES email_address(email_addr_id) ON DELETE CASCADE ON UPDATE CASCADE, "
					+ "UNIQUE INDEX customer_ix_email_addr_id (email_addr_id), "
					+ "UNIQUE INDEX customer_ix_cust_id (cust_id), "
					+ "INDEX customer_ix_ssn_number (ssn_number) "
					+ ") ENGINE=InnoDB");
			System.out.println("Created customer_tbl Table...");
		}
		catch (SQLException e) {
			System.err.println("SQL Error: " + e.getMessage());
			throw e;
		}
	}

	void createCustSequenceTable() throws SQLException {
		try {
			stm.execute("CREATE TABLE cust_sequence ( " +
			"seq_id bigint NOT NULL " +
			") ENGINE=MyISAM"); // table-level locking ?
			System.out.println("Created cust_sequence Table...");
			stm.execute("INSERT INTO cust_sequence (seq_id) VALUES(0)");
		}
		catch (SQLException e) {
			System.err.println("SQL Error: " + e.getMessage());
			throw e;
		}
	}
	
	public void loadTestData() throws SQLException {
		EmailAddressDao addrDao = SpringUtil.getDaoAppContext().getBean(EmailAddressDao.class);
		String emailAddr = "jsmith@test.com";
		EmailAddressVo addrvo = addrDao.findByAddress(emailAddr);
		PasswordTuple tuple = PasswordUtil.getEncryptedPassword("test@pswd");
		
		CustomerDao dao = SpringUtil.getDaoAppContext().getBean(CustomerDao.class);
		
		try {
			CustomerVo vo = new CustomerVo();
			vo.setCustId("test");
			vo.setClientId(Constants.DEFAULT_CLIENTID);
			vo.setSsnNumber("123-45-6789");
			vo.setTaxId(null);
			vo.setProfession("Software Consultant");
			vo.setFirstName("Joe");
			vo.setMiddleName(null);
			vo.setLastName("Smith");
			vo.setAlias(null);
			vo.setStreetAddress("123 Main st");
			vo.setStreetAddress2(null);
			vo.setCityName("Dublin");
			vo.setStateCode("OH");
			vo.setZipCode5("43017");
			vo.setZipCode4(null);
			vo.setProvinceName(null);
			vo.setPostalCode("43017");
			vo.setCountry("US");
			vo.setDayPhone("614-234-5678");
			vo.setEveningPhone("614-987-6543");
			vo.setMobilePhone("614-JOE-CELL");
			GregorianCalendar cal = new GregorianCalendar(1980, 01, 01);
			vo.setBirthDate(new java.sql.Date(cal.getTimeInMillis()));
			cal = new GregorianCalendar(2010, 05, 10);
			vo.setStartDate(new java.sql.Date(cal.getTimeInMillis()));
			cal = new GregorianCalendar(2019, 05, 10);
			vo.setEndDate(new java.sql.Date(cal.getTimeInMillis()));
			vo.setMobileCarrier(MobileCarrier.TMobile.getValue());
			vo.setMsgHeader("Joe's Message Header");
			vo.setMsgDetail("Dear Joe,");
			vo.setMsgOptional(null);
			vo.setMsgFooter("Have a Nice Day.");
			vo.setTimeZoneCode("E");
			vo.setMemoText("I-Care Pilot customer");
			vo.setStatusId(StatusId.ACTIVE.value());
			vo.setSecurityQuestion("What is your favorite movie?");
			vo.setSecurityAnswer("Rambo");
			vo.setEmailAddr(emailAddr);
			vo.setEmailAddrId(addrvo.getEmailAddrId());
			vo.setPrevEmailAddr(null);
			vo.setPasswordChangeTime(null);
			vo.setUserPassword(tuple.getPassword());
			vo.setPasswordSalt(tuple.getSalt());
			vo.setUpdtTime(new Timestamp(System.currentTimeMillis()));
			vo.setUpdtUserId(Constants.DEFAULT_USER_ID);
			
			int rows = dao.insert(vo);
			
			System.out.println("Number of rows inserted to customer_tbl: " + rows);
		}
		catch (Exception e) {
			System.err.println("SQL Error: " + e.getMessage());
			throw e;
		}
		
	}

	/**
	 * @param args
	 *            the command line arguments
	 */
	public static void main(String[] args) {
		try {
			CustomerTable ct = new CustomerTable();
			ct.dropTables();
			ct.createTables();
			ct.loadTestData();
			ct.wrapup();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}