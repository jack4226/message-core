package ltj.message.dao.abstrct;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.sql.DataSource;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ltj.message.constant.Constants;
import ltj.message.util.PrintUtil;
import ltj.message.vo.BaseVo;
import ltj.message.vo.emailaddr.MailingListVo;
import ltj.message.vo.inbox.MsgAttachmentVo;
import ltj.message.vo.inbox.MsgInboxVo;
import ltj.message.vo.rule.RuleLogicVo;
import ltj.spring.util.SpringUtil;

public class MetaDataUtil {
	static final Logger logger = LogManager.getLogger(MetaDataUtil.class);

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
			String name = method.getName();
			if (method.getReturnType().isAssignableFrom(java.lang.Boolean.class)
					|| "boolean".equals(method.getReturnType().getName())) {
				name = name.replaceFirst("^is", "get");
			}
			if (name.length()<=3 || !name.startsWith("get") || parmTypes.length != 0) {
				// must be a getter and not have any input parameters
				continue;
			}
			//logger.info("Method name: " + method.getName());
			String columnName = name.substring(3).toLowerCase();
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
			String name = method.getName();
			if (method.getReturnType().isAssignableFrom(java.lang.Boolean.class)
					|| "boolean".equals(method.getReturnType().getName())) {
				name = name.replaceFirst("^is", "get");
			}
			if (name.length()<=3 || !name.startsWith("get") || parmTypes.length != 0) {
				// must be a getter and not have any input parameters
				continue;
			}
			//logger.info("Method name: " + method.getName());
			String columnName = name.substring(3).toLowerCase();
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
				// build primary key map
				java.sql.ResultSet pkey_rs = md.getPrimaryKeys(catalog, schema, tableName);
				while (pkey_rs.next()) {
					String column = pkey_rs.getString("COLUMN_NAME");
					short seq = pkey_rs.getShort("KEY_SEQ");
					String pkName = pkey_rs.getString("PK_NAME");
					PrimaryKey pkey = new PrimaryKey();
					pkey.setColumnName(column);
					pkey.setKeySeq(seq);
					pkey.setPkName(pkName);
					table.getPrimaryKeyMap().put(pkey.getColumnName().toLowerCase().replaceAll("_", ""), pkey);
				}
				// build index map
				java.sql.ResultSet uni_idx_rs = md.getIndexInfo(catalog, schema, tableName, false, false);
				while(uni_idx_rs.next()) {
					String indexName = uni_idx_rs.getString("INDEX_NAME");
					String column = uni_idx_rs.getString("COLUMN_NAME");
					short position = uni_idx_rs.getShort("ORDINAL_POSITION");
					boolean non_unique = uni_idx_rs.getBoolean("NON_UNIQUE");
					Index idx = new Index();
					idx.setColumnName(column);
					idx.setIndexName(indexName);
					idx.setPosition(position);
					idx.setUnique(!non_unique);
					String columnKey = column.toLowerCase().replaceAll("_", "");
					if (!table.getIndexMap().containsKey(columnKey)) {
						List<Index> idxList = new ArrayList<>();
						table.getIndexMap().put(columnKey, idxList);
					}
					table.getIndexMap().get(columnKey).add(idx);
				}
				// build column map
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
		Table rule_logic = MetaDataUtil.getTableMetaData("rule_logic");
		logger.info("Table Metadata:" + PrintUtil.prettyPrint(rule_logic, 3));
		logger.info(buildUpdateStatement("msg_inbox", new MsgInboxVo()));
		logger.info(buildInsertStatement("mailing_list", new MailingListVo()));
		logger.info(buildUpdateStatement("msg_attachment", new MsgAttachmentVo()));
		logger.info(buildInsertStatement("rule_logic", new RuleLogicVo()));
	}
}
