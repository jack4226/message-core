package ltj.msgui.servlet;

import javax.annotation.Resource;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;

import org.apache.log4j.Logger;

import ltj.message.constant.Constants;
import ltj.message.dao.client.ClientDao;
import ltj.message.main.CreateAllTables;
import ltj.message.util.EnvUtil;
import ltj.message.util.JpaUtil;
import ltj.message.util.PrintUtil;
import ltj.msgui.util.SpringUtil;

/**
 * Servlet implementation class DerbyInitServlet
 */
@WebServlet(name="DerbyInitServlet", urlPatterns="/DerbyInit/*", loadOnStartup=8)
public class DerbyInitServlet extends HttpServlet {
	private static final long serialVersionUID = 1810496150486989387L;
	static final Logger logger = Logger.getLogger(DerbyInitServlet.class);
	
	@Resource 
	private javax.sql.DataSource msgdb_pool;
	
	@Resource(name="msgdb_pool")
	private  javax.sql.DataSource myDS;
	
    @Override
	public void init() throws ServletException {
		ServletContext ctx = getServletContext();
		logger.info("init() - ServerInfo: " + ctx.getServerInfo() + ", Context Path: " + ctx.getContextPath());
		
		EnvUtil.displayAllThreads();
		
		// test
		logger.info("msgdb_pool DataSource 1: " + PrintUtil.prettyPrint(msgdb_pool));
		logger.info("msgdb_pool DataSource 2: " + PrintUtil.prettyPrint(myDS));
		
		if (Constants.isDerbyDatabase(JpaUtil.getDBProductName())) {
			ClientDao sender = SpringUtil.getWebAppContext().getBean(ClientDao.class);
			if (sender.getAll().isEmpty()) {
				logger.warn("Initializing Derby database and load all the tables...");
				// load initial data to tables
				CreateAllTables loader = new CreateAllTables();
				try {
					loader.createTablesAndLoadData();
				}
				catch (Exception e) {
					logger.error("Failed to load data to tables", e);
				}
			}
		}
	}
}
