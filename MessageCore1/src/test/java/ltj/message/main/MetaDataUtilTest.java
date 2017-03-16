package ltj.message.main;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import javax.annotation.Resource;import javax.sql.DataSource;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.test.annotation.Rollback;

import ltj.message.dao.abstrct.Column;
import ltj.message.dao.abstrct.DaoTestBase;
import ltj.message.dao.abstrct.MetaDataUtil;
import ltj.message.dao.abstrct.Table;
import ltj.message.dao.inbox.AttachmentsDao;
import ltj.message.dao.inbox.MsgHeadersDao;
import ltj.message.dao.inbox.MsgInboxDao;
import ltj.message.util.PrintUtil;
import ltj.message.vo.emailaddr.MailingListVo;
import ltj.message.vo.inbox.AttachmentsVo;
import ltj.message.vo.inbox.MsgHeadersVo;
import ltj.message.vo.inbox.MsgInboxVo;

public class MetaDataUtilTest  extends DaoTestBase {
	
	@Autowired
	protected DataSource mysqlDataSource;
	@Resource
	private MsgInboxDao msgInboxDao;
	@Resource
	private MsgHeadersDao msgHdrsDao;
	@Resource
	private AttachmentsDao attachDao;

	@Test
	public void testMetaData1() {
		Table msg_inbox = MetaDataUtil.getTableMetaData("MsgInbox");
		logger.info("Table Metadata1:" + PrintUtil.prettyPrint(msg_inbox, 2));
		assertEquals("message", msg_inbox.getCatalog());
		
		Set<String> columnNames = msg_inbox.getColumnMap().keySet();
		logger.info("Column Names: " + columnNames);
		assertTrue(columnNames.contains("msgid"));
		assertTrue(columnNames.contains("msgrefid"));
		assertTrue(columnNames.contains("leadmsgid"));
		assertTrue(columnNames.contains("rulename"));
		assertTrue(columnNames.contains("msgsubject"));
		assertTrue(columnNames.contains("msgbody"));
		
		Set<String> keyNames = msg_inbox.getPrimaryKeyMap().keySet();
		logger.info("Primary Key Names: " + keyNames);
		assertTrue(keyNames.contains("msgid"));
		
		List<String> colNameList = new ArrayList<>();
		for (Column col : msg_inbox.getColumnList()) {
			colNameList.add(col.getColumnName());
		}
		assertTrue(colNameList.contains("MsgId"));
		assertTrue(colNameList.contains("MsgRefId"));
		assertTrue(colNameList.contains("LeadMsgId"));
		assertTrue(colNameList.contains("RuleName"));
		assertTrue(colNameList.contains("MsgSubject"));
		assertTrue(colNameList.contains("MsgBody"));
	}
	
	@Test
	public void testMetaData2() {
		Table msg_addrs = MetaDataUtil.getTableMetaData("MsgAddrs");
		logger.info("Table Metadata2:" + PrintUtil.prettyPrint(msg_addrs, 2));
		assertEquals("message", msg_addrs.getCatalog());
		
		Set<String> columnNames = msg_addrs.getColumnMap().keySet();
		logger.info("Column Names: " + columnNames);
		assertTrue(columnNames.contains("msgid"));
		assertTrue(columnNames.contains("addrtype"));
		assertTrue(columnNames.contains("addrseq"));
		assertTrue(columnNames.contains("addrvalue"));
		
		Set<String> keyNames = msg_addrs.getPrimaryKeyMap().keySet();
		logger.info("Primary Key Names: " + keyNames);
		assertTrue(keyNames.contains("msgid"));
		assertTrue(keyNames.contains("addrtype"));
		assertTrue(keyNames.contains("addrseq"));
	}
	
	@Test
	@Rollback(value=true)
	public void testUpdate1() {
		MsgInboxVo vo1 = msgInboxDao.getRandomRecord();
		assertNotNull(vo1);
		String suffix = StringUtils.leftPad(new Random().nextInt(10000) + "", 4, ' ');
		vo1.setUpdtUserId("Test_" + suffix);
		
		String updateSQL = MetaDataUtil.buildUpdateStatement("MsgInbox", vo1);
		logger.info(updateSQL);
		assertTrue(updateSQL.matches("Update MsgInbox set .* where MsgId\\=\\:msgId"));
		
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(vo1);
		int rowsUpadted = new NamedParameterJdbcTemplate(mysqlDataSource).update(updateSQL, namedParameters);
		assertEquals(1, rowsUpadted);
		
		MsgInboxVo vo2 = msgInboxDao.getByPrimaryKey(vo1.getMsgId());
		assertEquals("Test_" + suffix, vo2.getUpdtUserId());
	}
	
	@Test
	public void testUpdate2() {
		List<AttachmentsVo> list1 = attachDao.getRandomRecord();
		if (list1.isEmpty()) {
			return;
		}
		AttachmentsVo vo1 = list1.get(list1.size() - 1);
		String updtSQL1 = MetaDataUtil.buildUpdateStatement("Attachments", vo1);
		logger.info(updtSQL1);
		assertTrue(updtSQL1.matches("Update Attachments set .* MsgId\\=\\:msgId.*"));
		assertTrue(updtSQL1.matches("Update Attachments set .* AttchmntSeq\\=\\:attchmntSeq.*"));
		assertTrue(updtSQL1.matches("Update Attachments set .* AttchmntDepth\\=\\:attchmntDepth.*"));
		
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(vo1);
		String suffix = StringUtils.leftPad(new Random().nextInt(10000) + "", 4, ' ');
		String value = "Test MetaDataUtil.update to attachments. Ref# " + suffix;
		vo1.setAttchmntValue(value.getBytes());
		vo1.setAttachmentSize(vo1.getAttchmntValue().length);
		int rowsUpadted = new NamedParameterJdbcTemplate(mysqlDataSource).update(updtSQL1, namedParameters);
		assertEquals(1, rowsUpadted);
		
		List<AttachmentsVo> list2 = attachDao.getByMsgId(vo1.getMsgId());
		assertEquals(list1.size(), list2.size());
		AttachmentsVo vo2 = list2.get(list2.size() - 1);
		assertEquals(value, new String(vo2.getAttchmntValue()));
	}

	@Test
	public void testInsert1() {
		String insertSQL1 = MetaDataUtil.buildInsertStatement("MailingList", new MailingListVo());
		logger.info(insertSQL1);
		assertTrue(insertSQL1.matches("Insert INTO MailingList \\(.*\\) VALUES \\(.*\\)"));
		
		List<MsgHeadersVo> hdrList1 = msgHdrsDao.getRandomRecord();
		assertFalse(hdrList1.isEmpty());
		MsgHeadersVo vo1 = hdrList1.get(hdrList1.size() - 1);
		String insertSQL2 = MetaDataUtil.buildInsertStatement("MsgHeaders", vo1);
		logger.info("MsgHeaders Insert: " + insertSQL2);
		
		String suffix = StringUtils.leftPad(new Random().nextInt(10000) + "", 4, ' ');
		String headerName = "Test_Header_" + suffix;
		vo1.setHeaderName(headerName);
		vo1.setHeaderValue("Test header value " + suffix);
		int hdrSeqBefore = vo1.getHeaderSeq();
		vo1.setHeaderSeq(hdrSeqBefore + 1);
		
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(vo1);
		int rowsInserted = new NamedParameterJdbcTemplate(mysqlDataSource).update(insertSQL2, namedParameters);
		assertEquals(1, rowsInserted);
		List<MsgHeadersVo> hdrList2 = msgHdrsDao.getByMsgId(vo1.getMsgId());
		assertEquals(hdrList1.size() + 1, hdrList2.size());
		MsgHeadersVo vo2 = hdrList2.get(hdrList2.size() - 1);
		assertEquals(headerName, vo2.getHeaderName());
		assertEquals(hdrSeqBefore + 1, vo2.getHeaderSeq());
	}
	
	@Test
	public void testInsert2() {
		List<AttachmentsVo> list1 = attachDao.getRandomRecord();
		if (list1.isEmpty()) {
			return;
		}
		AttachmentsVo vo1 = list1.get(list1.size() - 1);
		String isrtSQL1 = MetaDataUtil.buildInsertStatement("Attachments", vo1);
		
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(vo1);
		int seqBefore = vo1.getAttchmntSeq();
		String suffix = StringUtils.leftPad(new Random().nextInt(10000) + "", 4, ' ');
		String value = "Test MetaDataUtil.insert to attachments. Ref# " + suffix;
		vo1.setAttchmntValue(value.getBytes());
		vo1.setAttachmentSize(vo1.getAttchmntValue().length);
		vo1.setAttchmntSeq(seqBefore + 1);
		int rowsInserted = new NamedParameterJdbcTemplate(mysqlDataSource).update(isrtSQL1, namedParameters);
		assertEquals(1, rowsInserted);
		
		List<AttachmentsVo> list2 = attachDao.getByMsgId(vo1.getMsgId());
		assertEquals(list1.size() + 1, list2.size());
		AttachmentsVo vo2 = list2.get(list2.size() - 1);
		assertEquals(seqBefore + 1, vo2.getAttchmntSeq());
		assertEquals(value, new String(vo2.getAttchmntValue()));
	}

}
