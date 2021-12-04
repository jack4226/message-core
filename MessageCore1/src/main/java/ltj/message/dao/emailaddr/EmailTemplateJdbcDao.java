package ltj.message.dao.emailaddr;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import ltj.message.constant.MsgDataType;
import ltj.message.dao.abstrct.AbstractDao;
import ltj.message.dao.abstrct.MetaDataUtil;
import ltj.message.dao.action.MsgDataTypeDao;
import ltj.message.dao.client.ReloadFlagsDao;
import ltj.message.util.BlobUtil;
import ltj.message.vo.action.MsgDataTypeVo;
import ltj.message.vo.emailaddr.EmailTemplateVo;

@Component("emailTemplateDao")
public class EmailTemplateJdbcDao extends AbstractDao implements EmailTemplateDao {
	static final Logger logger = LogManager.getLogger(EmailTemplateJdbcDao.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();
	
	private static final class EmailTemplateMapper implements RowMapper<EmailTemplateVo> {
		
		public EmailTemplateVo mapRow(ResultSet rs, int rowNum) throws SQLException {
			EmailTemplateVo emailTemplateVo = new EmailTemplateVo();
			
			emailTemplateVo.setRowId(rs.getInt("row_id"));
			emailTemplateVo.setTemplateId(rs.getString("template_id"));
			emailTemplateVo.setListId(rs.getString("list_id"));
			emailTemplateVo.setSubject(rs.getString("subject"));
			emailTemplateVo.setBodyText(rs.getString("body_text"));
			emailTemplateVo.setIsHtml(rs.getBoolean("is_html"));
			emailTemplateVo.setListType(rs.getString("list_type"));
			emailTemplateVo.setDeliveryOption(rs.getString("delivery_option"));
			emailTemplateVo.setSelectCriteria(rs.getString("select_criteria"));
			Boolean embedEmailId = rs.getBoolean("embed_email_id");
			if (rs.wasNull()) {
				emailTemplateVo.setEmbedEmailId(null);
			}
			else {
				emailTemplateVo.setEmbedEmailId(embedEmailId);
			}
			emailTemplateVo.setIsBuiltIn(rs.getBoolean("is_built_in"));
			emailTemplateVo.setOrigTemplateId(emailTemplateVo.getTemplateId());
			emailTemplateVo.setClientId(rs.getString("client_id"));
			// retrieve SchedulesBlob class
			byte[] bytes = rs.getBytes("schedules");
			try {
				SchedulesBlob blob = (SchedulesBlob) BlobUtil.bytesToObject(bytes);
				emailTemplateVo.setSchedulesBlob(blob);
			}
			catch (Exception e) {
				e.printStackTrace();
				throw new RuntimeException("Exception caught - " + e.toString());
			}
			return emailTemplateVo;
		}
	}
	
	@Override
	public EmailTemplateVo getByTemplateId(String templateId) {
		String sql = "select a.*, b.client_id " +
				" from email_template a, mailing_list b " +
				" where a.list_id=b.list_id and a.template_id=?";
		Object[] parms = new Object[] {templateId};
		List<?> list = (List<?>) getJdbcTemplate().query(sql, parms, new EmailTemplateMapper());
		if (list.size()>0) {
			return (EmailTemplateVo)list.get(0);
		}
		else {
			return null;
		}
	}
	
	@Override
	public List<EmailTemplateVo> getByListId(String listId) {
		String sql = "select a.*, b.client_id " +
				" from email_template a, mailing_list b " +
				" where a.list_id=b.list_id and a.list_id=?" +
				" order by a.template_id";
		Object[] parms = new Object[] {listId};
		List<EmailTemplateVo> list = (List<EmailTemplateVo>) getJdbcTemplate().query(sql, parms,
				new EmailTemplateMapper());
		return list;
	}
	
	@Override
	public List<EmailTemplateVo> getAll() {
		String sql = "select a.*, b.client_id " +
				" from email_template a, mailing_list b " +
				" where a.list_id=b.list_id" +
				" order by a.row_id";
		List<EmailTemplateVo> list = (List<EmailTemplateVo>) getJdbcTemplate().query(sql,
				new EmailTemplateMapper());
		return list;
	}

	@Override
	public List<EmailTemplateVo> getAllForTrial() {
		String sql = "select a.*, b.client_id " +
				" from email_template a, mailing_list b " +
				" where a.list_id=b.list_id" +
				" order by a.row_id" +
				" limit 20";
		int fetchSize = getJdbcTemplate().getFetchSize();
		int maxRows = getJdbcTemplate().getMaxRows();
		getJdbcTemplate().setFetchSize(20);
		getJdbcTemplate().setMaxRows(20);
		List<EmailTemplateVo> list = (List<EmailTemplateVo>) getJdbcTemplate().query(sql, new EmailTemplateMapper());
		getJdbcTemplate().setFetchSize(fetchSize);
		getJdbcTemplate().setMaxRows(maxRows);
		return list;
	}

	@Override
	public synchronized int update(EmailTemplateVo emailTemplateVo) {
		List<Object> keys = new ArrayList<>();
		keys.add(emailTemplateVo.getTemplateId());
		keys.add(emailTemplateVo.getListId());
		keys.add(emailTemplateVo.getSubject());
		keys.add(emailTemplateVo.getBodyText());
		keys.add(emailTemplateVo.getIsHtml());
		keys.add(emailTemplateVo.getListType());
		keys.add(emailTemplateVo.getDeliveryOption());
		keys.add(emailTemplateVo.getSelectCriteria());
		keys.add(emailTemplateVo.getEmbedEmailId());
		keys.add(emailTemplateVo.getIsBuiltIn());
		SchedulesBlob blob = emailTemplateVo.getSchedulesBlob();
		try {
			keys.add(BlobUtil.objectToBytes(blob));
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Exception caught - " + e.toString());
		}
		keys.add(emailTemplateVo.getRowId());
		
		String sql = "update email_template set " +
			"template_id=?," +
			"list_id=?," +
			"subject=?," +
			"body_text=?," +
			"is_html=?," +
			"list_type=?," +
			"delivery_option=?," +
			"select_criteria=?," +
			"embed_email_id=?," +
			"is_built_in=?," +
			"schedules=?" +
			" where row_id=?";
		
		Object[] parms = keys.toArray();
		int rowsUpadted = getJdbcTemplate().update(sql, parms);
		updateMsgDataType(emailTemplateVo); // do it before set Original Template Id
		emailTemplateVo.setOrigTemplateId(emailTemplateVo.getTemplateId());
		updateReloadFlags();
		return rowsUpadted;
	}

	@Override
	public synchronized int deleteByTemplateId(String templateId) {
		String sql = "delete from email_template where template_id=?";
		Object[] parms = new Object[] {templateId};
		int rowsDeleted = getJdbcTemplate().update(sql, parms);
		deleteMsgDataType(templateId);
		updateReloadFlags();
		return rowsDeleted;
	}
	
	@Override
	public synchronized int insert(EmailTemplateVo emailTemplateVo) {
		SchedulesBlob blob = emailTemplateVo.getSchedulesBlob();
		byte[] bytes = null;
		try {
			bytes = BlobUtil.objectToBytes(blob);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Exception caught - " + e.toString());
		}
		Object[] parms = {
				emailTemplateVo.getTemplateId(),
				emailTemplateVo.getListId(),
				emailTemplateVo.getSubject(),
				emailTemplateVo.getBodyText(),
				emailTemplateVo.getIsHtml(),
				emailTemplateVo.getListType(),
				emailTemplateVo.getDeliveryOption(),
				emailTemplateVo.getSelectCriteria(),
				emailTemplateVo.getEmbedEmailId(),
				emailTemplateVo.getIsBuiltIn(),
				bytes
			};
		
		String sql = "INSERT INTO email_template (" +
			"template_id," +
			"list_id," +
			"subject," +
			"body_text," +
			"is_html," +
			"list_type," +
			"delivery_option," +
			"select_criteria," +
			"embed_email_id," +
			"is_built_in," +
			"schedules" +
			") VALUES (" +
				" ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, "+
				" ? )";
		
		int rowsInserted = getJdbcTemplate().update(sql, parms);
		emailTemplateVo.setRowId(retrieveRowId());
		emailTemplateVo.setOrigTemplateId(emailTemplateVo.getTemplateId());
		if (MetaDataUtil.getTableMetaData("msg_data_type") != null) {
			insertMsgDataType(emailTemplateVo);
		}
		updateReloadFlags();
		return rowsInserted;
	}
	
	/*
	 * define methods that sync up template id to MsgDataType table.
	 */
	private final String DataType = MsgDataType.TEMPLATE_ID.name();
	private int insertMsgDataType(EmailTemplateVo vo) {
		int rowsInserted = 0;
		// 1) retrieve the record
		MsgDataTypeVo typeVo = getMsgDataTypeDao().getByTypeValuePair(DataType, vo.getTemplateId());
		if (typeVo == null) { // not found, insert
			typeVo = new MsgDataTypeVo();
			typeVo.setDataType(DataType);
			typeVo.setDataTypeValue(vo.getTemplateId());
			rowsInserted = getMsgDataTypeDao().insert(typeVo);
		}
		return rowsInserted;
	}
	
	private int deleteMsgDataType(String templateId) {
		int rowsDeleted = 0;
		// 1) retrieve the record
		MsgDataTypeVo vo = getMsgDataTypeDao().getByTypeValuePair(DataType, templateId);
		if (vo != null) { // found the record, delete
			rowsDeleted = getMsgDataTypeDao().deleteByPrimaryKey(vo.getRowId());
		}
		return rowsDeleted;
	}
	
	private int updateMsgDataType(EmailTemplateVo vo) {
		int rowsUpadted = 0;
		MsgDataTypeVo typeVo = getMsgDataTypeDao().getByTypeValuePair(DataType,
				vo.getOrigTemplateId());
		if (typeVo != null) { // record found, update
			typeVo.setDataTypeValue(vo.getTemplateId());
			rowsUpadted = getMsgDataTypeDao().update(typeVo);
		}
		else { // original record not found, check the new record and
				// insert one if it does not exist
			rowsUpadted = insertMsgDataType(vo);
		}
		return rowsUpadted;
	}
	
	private void updateReloadFlags() {
		getReloadFlagsDao().updateTemplateReloadFlag();
	}

	@Autowired
	private ReloadFlagsDao reloadFlagsDao;
	private synchronized ReloadFlagsDao getReloadFlagsDao() {
		return reloadFlagsDao;
	}

	@Autowired
	private MsgDataTypeDao msgDataTypeDao;
	MsgDataTypeDao getMsgDataTypeDao() {
		return msgDataTypeDao;
	}
}
