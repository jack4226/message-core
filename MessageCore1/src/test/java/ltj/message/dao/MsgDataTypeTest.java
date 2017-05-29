package ltj.message.dao;

import static org.junit.Assert.*;

import java.util.List;

import javax.annotation.Resource;

import org.junit.Test;

import ltj.message.dao.abstrct.DaoTestBase;
import ltj.message.dao.action.MsgDataTypeDao;
import ltj.message.vo.action.MsgDataTypeVo;

public class MsgDataTypeTest extends DaoTestBase {
	@Resource
	private MsgDataTypeDao dataTypeDao;

	@Test
	public void testMsgdataType() {
		try {
			List<String> dataTypeList = dataTypeDao.getDataTypes();
			assertFalse(dataTypeList.isEmpty());
			
			MsgDataTypeVo vo1 = null;
			for (String typeStr : dataTypeList) {
				List<MsgDataTypeVo> typeList = dataTypeDao.getByDataType(typeStr);
				assertFalse(typeList.isEmpty());
				
				vo1 = typeList.get(0);
				MsgDataTypeVo vo2 = dataTypeDao.getByPrimaryKey(vo1.getRowId());
				assertNotNull(vo2);
				assertTrue(vo1.equalsTo(vo2));
				
				MsgDataTypeVo vo3 = dataTypeDao.getByTypeValuePair(vo1.getDataType(), vo1.getDataTypeValue());
				assertNotNull(vo3);
				assertTrue(vo1.equalsTo(vo3));
			}
			
			assertNotNull(vo1);
			MsgDataTypeVo vo4 = new MsgDataTypeVo();
			vo4.setDataType(vo1.getDataType());
			vo4.setDataTypeValue(vo1.getDataTypeValue() + "_v2");
			int rowsInserted = dataTypeDao.insert(vo4);
			assertEquals(1, rowsInserted);
			
			vo4.setMiscProperties("test properties");
			int rowsUpdated = dataTypeDao.update(vo4);
			assertEquals(1, rowsUpdated);
			
			int rowsDeleted = dataTypeDao.deleteByPrimaryKey(vo4.getRowId());
			assertEquals(1, rowsDeleted);
		}
		catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}
}
