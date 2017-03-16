package ltj.message.dao.abstrct;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import ltj.message.constant.Constants;
import ltj.message.util.PrintUtil;
import ltj.message.vo.BaseVo;
import ltj.message.vo.emailaddr.MailingListVo;
import ltj.message.vo.inbox.AttachmentsVo;
import ltj.message.vo.inbox.MsgInboxVo;
import ltj.spring.util.SpringUtil;

public class MetaDataUtil {
	static final Logger logger = Logger.getLogger(MetaDataUtil.class);

	private static Map<String, Table> tableMetaData = new LinkedHashMap<>();

	public static Table getTableMetaData(String tableName) {
		if (tableMetaData.isEmpty()) {
			buildTableMetaData();
		}
		Table table = tableMetaData.get(StringUtils.lowerCase(tableName));
		if (table != null) {
			return table;
		}
		else { // reload meta data
			tableMetaData.clear();
			buildTableMetaData();
			return tableMetaData.get(StringUtils.lowerCase(tableName));
		}
	}
	
	/*
	 * Primary Key must be defined for the table for this method to work correctly.
	 */
	public static String buildUpdateStatement(String tableName, BaseVo vo) {
		Table table = getTableMetaData(tableName);
		if (table == null) {
			throw new IllegalStateException("Table name (" + tableName + ") does not exist in database!");
		}
		if (table.getPrimaryKeyList().isEmpty()) {
			throw new IllegalStateException("Primary Key must be defined for Table name (" + tableName + ")!");
		}
		StringBuffer columnSetter = new StringBuffer(); 
		String updateClause = "Update " + tableName + " set ";
		String whereClause = "";
		Method[] methods = vo.getClass().getMethods();
		for (Method method : methods) {
			int mod = method.getModifiers();
			if (!Modifier.isPublic(mod) || Modifier.isAbstract(mod) || Modifier.isStatic(mod)) {
				// must be public and non-static
				continue;
			}
			Class<?> parmTypes[] = method.getParameterTypes();
			if (method.getName().length()<=3 || !method.getName().startsWith("get") || parmTypes.length != 0) {
				// must be a getter and not have any input parameters
				continue;
			}
			//logger.info("Method name: " + method.getName());
			String columnName = method.getName().substring(3).toLowerCase();
			if (table.getColumnMap().containsKey(columnName)) {
				Column column = table.getColumnMap().get(columnName);
				String uncapName = StringUtils.uncapitalize(column.getColumnName());
				if (table.getPrimaryKeyMap().containsKey(columnName)) {
					if (whereClause.indexOf("where") >= 0) {
						whereClause += " and " + column.getColumnName() + "=:" + uncapName;
					}
					else {
						whereClause += " where " + column.getColumnName() + "=:" + uncapName;
					}
				}
				else {
					if (columnSetter.length() > 0) {
						columnSetter.append(", ");
					}
					columnSetter.append(column.getColumnName() + "=:" + uncapName);
				}
			}
		}
		return (updateClause + columnSetter.toString() + whereClause);
	}
	
	public static String buildInsertStatement(String tableName, BaseVo vo) {
		Table table = getTableMetaData(tableName);
		if (table == null) {
			throw new IllegalStateException("Table name (" + tableName + ") does not exist in database!");
		}
		StringBuffer columnList = new StringBuffer();
		StringBuffer valueList = new StringBuffer();
		String insertClause = "Insert INTO " + tableName + " (";
		String valuesClause = ") VALUES (";
		Method[] methods = vo.getClass().getMethods();
		for (Method method : methods) {
			int mod = method.getModifiers();
			if (!Modifier.isPublic(mod) || Modifier.isAbstract(mod) || Modifier.isStatic(mod)) {
				// must be public and non-static
				continue;
			}
			Class<?> parmTypes[] = method.getParameterTypes();
			if (method.getName().length()<=3 || !method.getName().startsWith("get") || parmTypes.length != 0) {
				// must be a getter and not have any input parameters
				continue;
			}
			//logger.info("Method name: " + method.getName());
			String columnName = method.getName().substring(3).toLowerCase();
			if (table.getColumnMap().containsKey(columnName)) {
				Column column = table.getColumnMap().get(columnName);
				String uncapName = StringUtils.uncapitalize(column.getColumnName());
				if (table.getPrimaryKeyMap().containsKey(columnName)) {
					if (Constants.YES.equalsIgnoreCase(column.getIsAutoIncrement())) {
						continue;
					}
				}
				if (columnList.length() > 0) {
					columnList.append(", ");
				}
				columnList.append(column.getColumnName());
				if (valueList.length() > 0) {
					valueList.append(", ");
				}
				valueList.append(":" + uncapName);
			}
		}
		return (insertClause + columnList.toString() + valuesClause + valueList.toString() + ")");
	}
	
	/**
	 * Reload MetaData from database.
	 */
	public synchronized static void reloadMetaData() {
		tableMetaData.clear();
	}

	private synchronized static void buildTableMetaData() {
		DataSource ds = (DataSource) SpringUtil.getDaoAppContext().getBean("mysqlDataSource");
		try {
			java.sql.Connection con = ds.getConnection();
			java.sql.DatabaseMetaData md = con.getMetaData();
			java.sql.ResultSet rs = md.getTables(null, null, "%", new String[] {"TABLE"});
			while (rs.next()) {
				String catalog = rs.getString("TABLE_CAT");
				String tableName = rs.getString("TABLE_NAME");
				String schema = rs.getString("TABLE_SCHEM");
				Table table = new Table();
				table.setCatalog(catalog);
				table.setSchema(schema);
				table.setTableName(tableName);
				java.sql.ResultSet pkey_rs = md.getPrimaryKeys(catalog, schema, tableName);
				while (pkey_rs.next()) {
					String column = pkey_rs.getString("COLUMN_NAME");
					int seq = pkey_rs.getInt("KEY_SEQ");
					String pkName = pkey_rs.getString("PK_NAME");
					PrimaryKey pkey = new PrimaryKey();
					pkey.setColumnName(column);
					pkey.setKeySeq(seq);
					pkey.setPkName(pkName);
					table.getPrimaryKeyMap().put(pkey.getColumnName().toLowerCase(), pkey);
				}
				java.sql.ResultSet column_rs = md.getColumns(catalog, schema, tableName, "%");
				while (column_rs.next()) {
					//String tableName = column_rs.getString("TABLE_NAME");
					String columnName = column_rs.getString("COLUMN_NAME");
					Column column = new Column();
					column.setColumnName(columnName);
					column.setColumnSize(column_rs.getInt("COLUMN_SIZE"));
					column.setDataType(column_rs.getInt("DATA_TYPE"));
					column.setDecimalDigits(column_rs.getInt("DECIMAL_DIGITS"));
					column.setIsAutoIncrement(column_rs.getString("IS_AUTOINCREMENT"));
					column.setIsNullable(column_rs.getString("IS_NULLABLE"));
					column.setNullable(column_rs.getInt("NULLABLE"));
					column.setTypeName(column_rs.getString("TYPE_NAME"));
					table.getColumnMap().put(column.getColumnName().toLowerCase(), column);
				}
				//logger.info(StringUtil.prettyPrint(table));
				tableMetaData.put(tableName.toLowerCase(), table);
			}
		} catch (SQLException e) {
			logger.error("SQLException caught", e);
			throw new RuntimeException("SQLException caught", e);
		}
	}

	public static void main(String[] args) {
		Table msg_inbox = MetaDataUtil.getTableMetaData("MsgInbox");
		logger.info("Table Metadata:" + PrintUtil.prettyPrint(msg_inbox));
		logger.info(buildUpdateStatement("MsgInbox", new MsgInboxVo()));
		logger.info(buildInsertStatement("MailingList", new MailingListVo()));
		logger.info(buildUpdateStatement("Attachments", new AttachmentsVo()));
	}
}
