package ltj.message.dao;

import static org.junit.Assert.*;

import javax.annotation.Resource;

import org.junit.Test;

import ltj.message.dao.abstrct.DaoTestBase;
import ltj.message.dao.outbox.MsgRenderedDao;
import ltj.vo.outbox.MsgRenderedVo;

public class MsgRenderedTest extends DaoTestBase {

	@Resource
	private MsgRenderedDao renderedDao;
	
	@Test
	public void testRenderedDao() {
		MsgRenderedVo vo1 = renderedDao.getLastRecord();
		assertNotNull(vo1);
		
		MsgRenderedVo vo2 = renderedDao.getLastRecord();
		assertNotNull(vo2);
		
		MsgRenderedVo vo3 = selectByPrimaryKey(vo1.getRenderId());
		assertNotNull(vo3);
		
		int rowsUpdated = update(vo3);
		assertEquals(1, rowsUpdated);
		
		MsgRenderedVo vo4 = insert(vo3.getRenderId());
		assertNotEquals(vo4.getRenderId(), vo3.getRenderId());
		
		int rowsDeleted = deleteByPrimaryKey(vo4);
		assertEquals(1, rowsDeleted);
	}
	
	private MsgRenderedVo selectByPrimaryKey(long renderId) {
		MsgRenderedVo renderedVo = renderedDao.getByPrimaryKey(renderId);
		if (renderedVo!=null) {
			logger.info("MsgRenderedDao - selectByPrimaryKey: " + LF + renderedVo);
		}
		return renderedVo;
	}

	private int update(MsgRenderedVo renderedVo) {
		renderedVo.setUpdtUserId("unitTest");
		int rows = renderedDao.update(renderedVo);
		logger.info("MsgRenderedDao - update: rows updated " + rows);
		return rows;
	}

	private MsgRenderedVo insert(long renderedId) {
		MsgRenderedVo vo = renderedDao.getByPrimaryKey(renderedId);;
		if (vo!=null) {
			vo.setUpdtUserId("unitTest");;
			int rows = renderedDao.insert(vo);
			logger.info("MsgRenderedDao - insert: rows inserted " + rows);
			return selectByPrimaryKey(vo.getRenderId());
		}
		return null;
	}

	private int deleteByPrimaryKey(MsgRenderedVo vo) {
		int rowsDeleted = renderedDao.deleteByPrimaryKey(vo.getRenderId());
		logger.info("MsgRenderedDao - deleteByPrimaryKey: Rows Deleted: " + rowsDeleted);
		return rowsDeleted;
	}

}
