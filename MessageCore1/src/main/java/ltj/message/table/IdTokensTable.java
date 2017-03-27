package ltj.message.table;
import java.sql.SQLException;
import java.sql.Timestamp;

import ltj.message.constant.Constants;
import ltj.message.constant.EmailIdToken;
import ltj.message.dao.idtokens.IdTokensDao;
import ltj.message.main.CreateTableBase;
import ltj.message.vo.IdTokensVo;
import ltj.spring.util.SpringUtil;
public class IdTokensTable extends CreateTableBase {
	/**
	 * Creates a new instance of IdTokenTables
	 * 
	 * @throws SQLException
	 * @throws ClassNotFoundException
	 */
	public IdTokensTable() throws ClassNotFoundException, SQLException {
		init();
	}
	
	public void dropTables() {
		try {
			stm.execute("DROP TABLE id_tokens");
			System.out.println("Dropped id_tokens Table...");
		} catch (SQLException e) {}
	}
	
	public void createTables() throws SQLException {
		try {
			stm.execute("CREATE TABLE id_tokens ( " +
			"row_id int AUTO_INCREMENT not null, " +
			"client_id varchar(16) NOT NULL, " + 
			"description varchar(100), " +
			"body_begin_token varchar(16) NOT NULL, " +
			"body_end_token varchar(4) NOT NULL, " +
			"x_header_name varchar(20), " +
			"xhdr_begin_token varchar(16), " +
			"xhdr_end_token varchar(4), " +
			"max_length integer NOT NULL, " +
			"updt_time datetime(3) NOT NULL, " +
			"updt_user_id char(10) NOT NULL, " +
			"FOREIGN KEY id_tokens_client_id_fkey (client_id) REFERENCES client_tbl(client_id) ON DELETE CASCADE ON UPDATE CASCADE, " +
			"CONSTRAINT id_tokens_pkey PRIMARY KEY (row_id), " +
			"UNIQUE INDEX id_tokens_ix_client_id (client_id) " +
			") ENGINE=InnoDB");
			System.out.println("Created id_tokens Table...");
		} catch (SQLException e) {
			System.err.println("SQL Error: " + e.getMessage());
			throw e;
		}
	}
	
	public void loadTestData() throws SQLException {
		IdTokensDao dao = SpringUtil.getDaoAppContext().getBean(IdTokensDao.class);
		
		try {
			IdTokensVo vo = new IdTokensVo();
			vo.setClientId(Constants.DEFAULT_CLIENTID);
			vo.setDescription("Default SenderId");
			vo.setBodyBeginToken(EmailIdToken.BODY_BEGIN);
			vo.setBodyEndToken(EmailIdToken.BODY_END);
			vo.setXHeaderName(EmailIdToken.NAME);
			vo.setXhdrBeginToken(EmailIdToken.XHDR_BEGIN);
			vo.setXhdrEndToken(EmailIdToken.XHDR_END);
			vo.setMaxLength(EmailIdToken.MAXIMUM_LENGTH);
			vo.setUpdtTime(new Timestamp(System.currentTimeMillis()));
			vo.setUpdtUserId("SysAdmin");
			
			int rows = dao.insert(vo);
			
			System.out.println("Number of rows inserted to id_tokens: " + rows);
		} catch (Exception e) {
			System.err.println("SQL Error: " + e.getMessage());
			throw e;
		}
	}

	/**
	 * @param args the command line arguments
	 */
	public static void main(String[] args) {
		try {
			IdTokensTable ct = new IdTokensTable();
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