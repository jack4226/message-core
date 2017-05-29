package ltj.message.dao.abstrct;

public class PrimaryKey {
	private String columnName;
	private short keySeq;
	private String pkName;

	public String getColumnName() {
		return columnName;
	}
	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}
	public short getKeySeq() {
		return keySeq;
	}
	public void setKeySeq(short keySeq) {
		this.keySeq = keySeq;
	}
	public String getPkName() {
		return pkName;
	}
	public void setPkName(String pkName) {
		this.pkName = pkName;
	}
}
