package ltj.message.dao.abstrct;

public class Index {
	private String columnName;
	private int position;
	private boolean unique;
	private String indexName;

	public String getColumnName() {
		return columnName;
	}
	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}
	public int getPosition() {
		return position;
	}
	public void setPosition(int position) {
		this.position = position;
	}
	public boolean isUnique() {
		return unique;
	}
	public void setUnique(boolean unique) {
		this.unique = unique;
	}
	public String getIndexName() {
		return indexName;
	}
	public void setIndexName(String pkName) {
		this.indexName = pkName;
	}
}
