package ltj.message.dao.abstrct;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.sql.DataSource;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import ltj.message.constant.Constants;
import ltj.message.util.PrintUtil;
import ltj.message.vo.BaseVo;
import ltj.message.vo.emailaddr.MailingListVo;
import ltj.message.vo.inbox.MsgAttachmentVo;
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
				uncapName = convertToCamelCase(uncapName);
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
				valueList.append(":" + convertToCamelCase(uncapName));
			}
		}
		return (insertClause + columnList.toString() + valuesClause + valueList.toString() + ")");
	}
	
	static String convertToCamelCase(String str) {
		Pattern p = Pattern.compile("_(.)");
		Matcher m = p.matcher(str);
		StringBuffer sb = new StringBuffer();
		while (m.find()) {
			m.appendReplacement(sb, m.group(1).toUpperCase());
		}
		m.appendTail(sb);
		return sb.toString();
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
					table.getPrimaryKeyMap().put(pkey.getColumnName().toLowerCase().replaceAll("_", ""), pkey);
				}
				java.sql.ResultSet uni_idx_rs = md.getIndexInfo(catalog, schema, tableName, true, false);
				while(uni_idx_rs.next()) {
				/*
				TABLE_CAT String => table catalog (may be null)
				TABLE_SCHEM String => table schema (may be null)
				TABLE_NAME String => table name
				NON_UNIQUE boolean => Can index values be non-unique. false when TYPE is tableIndexStatistic
				INDEX_QUALIFIER String => index catalog (may be null); null when TYPE is tableIndexStatistic
				INDEX_NAME String => index name; null when TYPE is tableIndexStatistic
				TYPE short => index type:
					tableIndexStatistic - this identifies table statistics that are returned in conjuction with a table's index descriptions
					tableIndexClustered - this is a clustered index
					tableIndexHashed - this is a hashed index
					tableIndexOther - this is some other style of index
				ORDINAL_POSITION short => column sequence number within index; zero when TYPE is tableIndexStatistic
				COLUMN_NAME String => column name; null when TYPE is tableIndexStatistic
				ASC_OR_DESC String => column sort sequence, "A" => ascending, "D" => descending, may be null if sort sequence is not supported; null when TYPE is tableIndexStatistic
				CARDINALITY long => When TYPE is tableIndexStatistic, then this is the number of rows in the table; otherwise, it is the number of unique values in the index.
				PAGES long => When TYPE is tableIndexStatisic then this is the number of pages used for the table, otherwise it is the number of pages used for the current index.
				FILTER_CONDITION String => Filter condition, if any. (may be null)
				 */
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
					table.getColumnMap().put(column.getColumnName().toLowerCase().replaceAll("_", ""), column);
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
		Table msg_inbox = MetaDataUtil.getTableMetaData("msg_inbox");
		logger.info("Table Metadata:" + PrintUtil.prettyPrint(msg_inbox));
		Table timer_server = MetaDataUtil.getTableMetaData("timer_server");
		logger.info("Table Metadata:" + PrintUtil.prettyPrint(timer_server, 2));
		logger.info(buildUpdateStatement("msg_inbox", new MsgInboxVo()));
		logger.info(buildInsertStatement("mailing_list", new MailingListVo()));
		logger.info(buildUpdateStatement("msg_attachment", new MsgAttachmentVo()));
	}
}
