package ltj.message.vo;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.validation.constraints.NotNull;

import ltj.message.constant.Constants;
import ltj.message.constant.RuleCriteria;

public class PagingVo extends BaseVo implements java.io.Serializable, Cloneable {
	private static final long serialVersionUID = -1139494427014770362L;
	
	// define paging context
	public static enum PageAction {FIRST, NEXT, PREVIOUS, CURRENT, LAST};
	
	public static final int PAGE_SIZE = 20;
	protected PageAction pageAction; // = PageAction.CURRENT;
	protected int pageSize = PAGE_SIZE;
	protected int pageNumber = 0;
	protected long rowCount = -1;
	// end of paging
	
	protected Object searchObjFirst;
	protected Object searchObjLast;
	
	private final OrderBy orderBy = new OrderBy();
	private final SearchBy searchBy = new SearchBy();
	
	protected final Set<String> searchFields = new LinkedHashSet<>();
	
	public PagingVo() {
		init();
	}
	
	private void init() {
		//setStatusId(null);
		pageAction = PageAction.CURRENT;
		
		orderBy.setOrderBy(Column.rowId, null);
		orderBy.setOrderBy(Column.updtTime, null);
		orderBy.setOrderBy(Column.emailAddr, null);
		
		for (Column c : Column.values()) { // initialize search criteria
			initColumn(c);
		}
	}
	
	public void resetPageContext() {
		logger.info("resetPageContext() - entering...");
		pageAction = PageAction.CURRENT;
		pageNumber = 0;
		rowCount = -1;
		searchObjFirst = searchObjLast = null;
		
//		Field[] fields = this.getClass().getDeclaredFields();
//		for (int i = 0; i < fields.length; i++) {
//			Field field = fields[i];
//			try {
//				if (searchFields.contains(field.getName())) {
//					if (field.getType().isAssignableFrom(Class.forName("java.lang.Object"))) {
//						field.set(field.getType(), null);
//					}
//				}
//			}
//			catch (Exception e) {e.printStackTrace();}
//		}
	}
	
//	protected final void setSearchFields() {
//		searchFields.add("statusId");
//		Field[] fields = this.getClass().getDeclaredFields();
//		for (int i = 0; i < fields.length; i++) {
//			Field field = fields[i];
//			int mod = field.getModifiers();
//			if (Modifier.isPrivate(mod) && !Modifier.isFinal(mod) && !Modifier.isStatic(mod) && !Modifier.isAbstract(mod)) {
//				searchFields.add(field.getName());
//			}
//		}
//	}
//	
//	public Set<String> getSearchFields() {
//		return searchFields;
//	}
//	
	private void initColumn(Column c) {
		if (!getSearchBy().containsKey(c)) {
			if (java.sql.Timestamp.class.isAssignableFrom(c.getDataType()) || java.sql.Date.class.isAssignableFrom(c.getDataType())) {
				searchBy.setCriteria(c, new Criteria(RuleCriteria.GE, null, c.getDataType(), MatchBy.ExactPhrase));
			}
			else if (Boolean.class.isAssignableFrom(c.getDataType())) {
				searchBy.setCriteria(c, new Criteria(RuleCriteria.EQUALS, null, c.getDataType(), MatchBy.ExactPhrase));
			}
			else if (Number.class.isAssignableFrom(c.getDataType())) {
				searchBy.setCriteria(c, new Criteria(RuleCriteria.EQUALS, null, c.getDataType(), MatchBy.ExactPhrase));
			}
			else if (String.class.isAssignableFrom(c.getDataType())) {
				searchBy.setCriteria(c, new Criteria(RuleCriteria.CONTAINS, null, c.getDataType(), MatchBy.AnyWords));
			}
		}
	}
	
	public OrderBy getOrderBy() {
		return orderBy;
	}
	
	public SearchBy getSearchBy() {
		return searchBy;
	}
	
	public void setOrderBy(Column column, Boolean isAscending) {
		getOrderBy().setOrderBy(column, isAscending);
	}
	
	public Boolean getOrderBy(Column column) {
		return getOrderBy().getIsAscending(column);
	}
	
	public Criteria getSearchCriteria(Column column) {
		initColumn(column);
		return getSearchBy().getCriteria(column);
	}
	
	public void setSearchCriteria(Column column, Criteria criteria) {
		getSearchBy().setCriteria(column, criteria);
	}
	
	public Object getSearchValue(Column column) {
		return getSearchCriteria(column).getValue();
	}
	
	public MatchBy getSearchMatchBy(Column column) {
		return getSearchCriteria(column).getMatchBy();
	}
	
	public void setSearchValue(Column column, Object value) {
		if (value instanceof Criteria) {
			getSearchBy().setCriteria(column, (Criteria) value);
		}
		else if (getSearchBy().containsKey(column)) {
			Criteria criteria = getSearchBy().getCriteria(column);
			criteria.setValue(value);
		}
		else {
			getSearchBy().setCriteria(column, new Criteria(RuleCriteria.EQUALS, value));
		}
	}

	public void setRowId(int rowId) {
		super.setPrimaryKey("" + rowId);
		getSearchBy().setCriteria(Column.rowId, new Criteria(RuleCriteria.EQUALS, rowId));
	}

	@Override
	public void setStatusId(String status) {
		super.setStatusId(status);
		getSearchBy().setCriteria(Column.statusId, new Criteria(RuleCriteria.EQUALS, status));
	}

	@Override
	public void setUpdtUserId(String updtUserId) {
		super.setUpdtUserId(updtUserId);
		getSearchBy().setCriteria(Column.updtUserId, new Criteria(RuleCriteria.EQUALS, updtUserId));
	}
	
	public final class OrderBy implements Serializable {
		private static final long serialVersionUID = -6331807454048883727L;
		private Map<Column, Boolean> map = new LinkedHashMap<>();
		private OrderBy() {}
		
		public Boolean getIsAscending(@NotNull Column column) {
			return map.get(column);
		}
		
		public void setOrderBy(@NotNull Column column, Boolean isAscending) {
			map.put(column, isAscending);
		}

		boolean containsKey(Column column) {
			return map.containsKey(column);
		}
		
		public Set<Column> getKeySet() {
			return map.keySet();
		}
		
		public List<String> getOrderByForPrint() {
			List<String> list = new ArrayList<>();
			for (Iterator<Column> it=getKeySet().iterator(); it.hasNext();) {
				Column col = it.next();
				if (map.get(col) != null) {
					list.add("Order by: " + col.name() + " -> " + (map.get(col) ? "Asc" : "Desc"));
				}
			}
			return list;
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			List<String> list = getOrderByForPrint();
			for (String order : list) {
				sb.append(order + LF);
			}
			return sb.toString();
		}
	}
	
	public final class SearchBy implements Serializable {
		private static final long serialVersionUID = 3784418185322440915L;
		private Map<Column, Criteria> map = new LinkedHashMap<>();
		
		public Criteria getCriteria(@NotNull Column column) {
			return map.get(column);
		}
		
		public void setCriteria(@NotNull Column column, @NotNull Criteria criteria) {
			map.put(column, criteria);
		}
		
		boolean containsKey(Column column) {
			return map.containsKey(column);
		}
		
		public Set<Column> getKeySet() {
			return map.keySet();
		}
		
		public Map<String, String> getSearchByForPrint() {
			return getSearchByForPrint(false);
		}
		
		public Map<String, String> getSearchByForPrint(boolean includeNullValue) {
			Map<String, String> map = new LinkedHashMap<>();
			if (getKeySet().size() > 0) {
				map.put(String.format("%-13s", "Header"), Criteria.header());
				map.put(String.format("%-13s", "----------"), Criteria.underLine());
				for (Iterator<Column> it=getKeySet().iterator(); it.hasNext();) {
					Column col = it.next();
					Criteria cri = getCriteria(col);
					if (cri.getValue() != null || includeNullValue) {
						map.put(String.format("%-13s", col.name()), cri.toString());
					}
				}
			}
			return map;
		}
		
		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			for (Iterator<String> it=getSearchByForPrint().keySet().iterator(); it.hasNext();) {
				String col = it.next();
				sb.append("Search by: " + col + " -> " + getSearchByForPrint().get(col) + LF);
			}
			return sb.toString();
		}
	}
	
	static final Class<?> getValueClass(Object value) {
		return (value instanceof Integer) ? Integer.class
				: ((value instanceof Boolean) ? Boolean.class
						: ((value instanceof Timestamp) ? Timestamp.class
								: ((value instanceof java.sql.Date) ? java.sql.Date.class : String.class)));
	}
	
	static final MatchBy getDefaultMatchBy(@NotNull RuleCriteria operation) {
		if (RuleCriteria.EQUALS.equals(operation) || RuleCriteria.NOT_EQUALS.equals(operation)) {
			return MatchBy.ExactPhrase;
		}
		else if (RuleCriteria.GE.equals(operation) || RuleCriteria.LE.equals(operation)
				|| RuleCriteria.GREATER_THAN.equals(operation) || RuleCriteria.LESS_THAN.equals(operation)) {
			return MatchBy.ExactPhrase;
		}
		else if (RuleCriteria.STARTS_WITH.equals(operation)) {
			return MatchBy.BeginWith;
		}
		else if (RuleCriteria.ENDS_WITH.equals(operation)) {
			return MatchBy.EndWith;
		}
		return MatchBy.AnyWords;
	}
	
	public final static class Criteria implements Serializable {
		private static final long serialVersionUID = 7547705264065371748L;
		RuleCriteria operation;
		Object value;
		final Class<?> dataType;
		MatchBy matchBy;
		
		public Criteria(@NotNull RuleCriteria operation, Object value) {
			this(operation, value, PagingVo.getValueClass(value));
		}

		public Criteria(@NotNull RuleCriteria operation, Object value, MatchBy matchBy) {
			this(operation, value, PagingVo.getValueClass(value), matchBy);
		}

		public Criteria(@NotNull RuleCriteria operation, Object value, Class<?> dataType) {
			this(operation, value, dataType, getDefaultMatchBy(operation));
		}

		public Criteria(@NotNull RuleCriteria operation, Object value, Class<?> dataType, MatchBy matchBy) {
			this.operation = operation;
			this.value = value;
			this.dataType = dataType;
			this.matchBy = matchBy;
		}

		public RuleCriteria getOperation() {
			return operation;
		}

		public void setOperation(RuleCriteria operation) {
			this.operation = operation;
		}

		public Object getValue() {
			return value;
		}
		
		public void setValue(Object value) {
			this.value = value;
		}

		public Class<?> getDataType() {
			return dataType;
		}
		
		public MatchBy getMatchBy() {
			return matchBy;
		}
		
		public void setMatchBy(MatchBy matchBy) {
			this.matchBy = matchBy;
		}
		
		static String header() {
			return String.format("%-12s  %-11s  %-10s  %s", "Operation", "MatchBy", "Data Type", "Value");
		}

		static String underLine() {
			return String.format("%-12s  %-11s  %-10s  %s", "-----------", "----------", "---------", "---------");
		}
		
		@Override
		public String toString() {
			String msg = String.format("%-12s  %-11s  %-10s  %s", operation.name(),  matchBy.name(), dataType.getSimpleName(), value);
			return msg;
		}
	}

	
	public boolean equalsToSearch(PagingVo vo) {
		getLogList().clear();
		if (vo == null) {
			return false;
		}
		if (vo instanceof PagingVo) {
			PagingVo pagingVo = (PagingVo) vo;
			List<Column> colList = Column.getColumnsByType(Type.msginbox, true);
			for (Column c : colList) { //Column.values()) {
				if (getSearchValue(c) == null) {
					if (pagingVo.getSearchValue(c) != null) {
						addChangeLog(c.name(), null, pagingVo.getSearchValue(c));
					}
				}
				else {
					if (!getSearchValue(c).equals(pagingVo.getSearchValue(c))) {
						addChangeLog(c.name(), getSearchValue(c), pagingVo.getSearchValue(c));
					}
				}
			}
		}
		if (getLogList().size() > 0) {
			return false;
		}
		else {
			return true;
		}
	}
	
	public void printMethodNames() {
		Method thisMethods[] = this.getClass().getDeclaredMethods();
		for (int i = 0; i < thisMethods.length; i++) {
			Method method = (Method) thisMethods[i];
			String name = method.getName();
			Class<?>[] params = method.getParameterTypes();
			if (name.startsWith("get") && params.length == 0) {
				System.out.println("Method Name: " + name + ", Return Type: " + method.getReturnType().getName());
			}
		}
		for (Iterator<String> it = searchFields.iterator(); it.hasNext();) {
			String searchField = it.next();
			System.out.println("Search Field: " + searchField);
		}
	}
	
	public static enum MatchBy {
		AllWords, AnyWords,ExactPhrase, BeginWith, EndWith;
	}
	
	public static enum Type {
		common, emailaddr, msgcount, subrdata, subscipt, msginbox;
	}
	
	public static enum Column {
		rowId(Integer.class, Type.common), updtTime(java.sql.Timestamp.class, Type.common), updtUserId(Type.common), statusId(Type.common),
		// Email Address
		emailAddrId(Integer.class, Type.emailaddr, Type.subscipt), origEmailAddr(Type.emailaddr), bounceCount(Integer.class, Type.emailaddr),
		emailAddr(true, Type.subrdata, Type.emailaddr, Type.msgcount),
		// Message Count
		sentCount(Integer.class, Type.msgcount), openCount(Integer.class, Type.msgcount), clickCount(Integer.class, Type.msgcount),
		// Subscription
		listId(true, Type.subscipt), subscribed(Boolean.class, true, Type.subscipt),
		// Subscriber Data
		clientId(true, Type.subrdata), ssnNumber(Type.subrdata), dayPhone(Type.subrdata), firstName(Type.subrdata), lastName(Type.subrdata),
		// Message in box
		msgId(Integer.class, Type.msginbox), receivedTime(java.sql.Timestamp.class, Type.msginbox),
		fromAddrId(Integer.class, true, Type.msginbox), toAddrId(Integer.class, true, Type.msginbox), ruleName(true, Type.msginbox),
		fromAddr(true, Type.msginbox), toAddr(true, Type.msginbox), messageFolder(true, Type.msginbox), msgSubject(Type.msginbox),
		msgBody(Type.msginbox), readCount(Integer.class, Type.msginbox), isFlagged(Boolean.class, Type.msginbox), msgDirection(Type.msginbox);
		
		final private Type[] types;
		final private Class<?> dataType;
		final private boolean isJoin; // column from joining another table
		
		private Column(Type... types) {
			this(String.class, false, types);
		}
		
		private Column(boolean isJoin, Type... types) {
			this(String.class, isJoin, types);
		}
		
		private Column(Class<?> dataType, Type... types) {
			this(dataType, false, types);
		}
		
		private Column(Class<?> dataType, boolean isJoin, Type... types) {
			this.types = new Type[types.length];
			int idx = 0;
			for (Type type : types) {
				this.types[idx++] = type;
			}
			this.dataType = dataType;
			this.isJoin = isJoin;
		}
		
		public Type[] getTypes() {
			return types;
		}
		
		public Class<?> getDataType() {
			return dataType;
		}
		
		public boolean isJoin() {
			return isJoin;
		}
		
		public static List<Column> getColumnsByType(Type type) {
			return getColumnsByType(type, false);
		}
		
		public static List<Column> getColumnsByType(Type type, boolean includeJoin) {
			List<Column> list = new ArrayList<>();
			for (Column c : Column.values()) {
				for (Type tp : c.getTypes()) {
					if (tp.equals(type) || tp.equals(Type.common)) {
						if (!c.isJoin || includeJoin) {
							list.add(c);
							break;
						}
					}
				}
			}
			return list;
		}
	}
	
	public static class PagingContext {
		public String whereSql = "";
		public String fetchOrder = "";
		public int pageSize = 0;
	}

	public static PagingContext getPagingWhereSql(PagingVo vo, String[] CRIT, List<Object> parms, int rows,
			String sqlField) {
		PagingContext ctx = new PagingContext();
		ctx.fetchOrder = "desc";
		ctx.pageSize = vo.getPageSize();
		if (vo.getPageAction().equals(PagingVo.PageAction.FIRST)) {
			// do nothing
		}
		else if (vo.getPageAction().equals(PagingVo.PageAction.NEXT)) {
			if (vo.getSearchObjLast() != null) {
				ctx.whereSql += CRIT[parms.size()] + " " + sqlField + " < ? ";
				parms.add(vo.getSearchObjLast());
			}
		}
		else if (vo.getPageAction().equals(PagingVo.PageAction.PREVIOUS)) {
			if (vo.getSearchObjFirst() != null) {
				ctx.whereSql += CRIT[parms.size()] + " " + sqlField + " > ? ";
				parms.add(vo.getSearchObjFirst());
				ctx.fetchOrder = "asc";
			}
		}
		else if (vo.getPageAction().equals(PagingVo.PageAction.LAST)) {
			ctx.pageSize = rows % vo.getPageSize();
			if (ctx.pageSize == 0) {
				ctx.pageSize = Math.min(rows, vo.getPageSize());
			}
			ctx.fetchOrder = "asc";
		}
		else if (vo.getPageAction().equals(PagingVo.PageAction.CURRENT)) {
			if (vo.getSearchObjFirst() != null) {
				ctx.whereSql += CRIT[parms.size()] + " " + sqlField + " <= ? ";
				parms.add(vo.getSearchObjFirst());
			}
		}
		return ctx;
	}
	
	public PageAction getPageAction() {
		return pageAction;
	}

	public void setPageAction(PageAction action) {
		this.pageAction = action;
		if (PageAction.FIRST.equals(action)) {
			pageNumber = 0;
		}
		else if (PageAction.NEXT.equals(action)) {
			pageNumber = pageNumber + 1;
		}
		else if (PageAction.PREVIOUS.equals(action)) {
			pageNumber = pageNumber > 0 ? pageNumber - 1 : pageNumber;
		}
		else if (PageAction.LAST.equals(action)) {
			pageNumber = (int) Math.floor(Math.abs((double)(rowCount - 1)) / (double)pageSize);
		}
	}
	
	
	public final static String[] CRIT = { " where ", " and ", " and ", " and ", " and ", " and ", " and ",
			" and ", " and ", " and ", " and ", " and ", " and ", " and ", " and ", " and ", " and ",
			" and ", " and ", " and ", " and ", " and ", " and ", " and ", " and ", " and ", " and ",
			" and ", " and ", " and ", " and ", " and ", " and ", " and ", " and ", " and ", " and ",
			" and ", " and ", " and ", " and ", " and ", " and ", " and ", " and ", " and ", " and ",
			" and ", " and ", " and ", " and ", " and ", " and ", " and ", " and ", " and ", " and "};

	public static String buildSearchField(PagingVo vo, PagingVo.Column col, String[] CRIT, List<Object> parms,
			String field) {
		return buildSearchField(vo, col, CRIT, parms, field, false);
	}
	
	public static String buildSearchField(PagingVo vo, PagingVo.Column col, String[] CRIT, List<Object> parms,
			String field, boolean toLowerCase) {
		String sql = "";
		PagingVo.Criteria criteria = vo.getSearchCriteria(col);
		if (criteria != null) {
			String operator = " =? ";
			if (RuleCriteria.NOT_EQUALS.equals(criteria.getOperation())) {
				operator = " !=? ";
			}
			else if (RuleCriteria.GE.equals(criteria.getOperation())) {
				operator = " >=? ";
			}
			else if (RuleCriteria.GREATER_THAN.equals(criteria.getOperation())) {
				operator = " >? ";
			}
			else if (RuleCriteria.LE.equals(criteria.getOperation())) {
				operator = " <=? ";
			}
			else if (RuleCriteria.LESS_THAN.equals(criteria.getOperation())) {
				operator = " <? ";
			}
			else if (RuleCriteria.IS_BLANK.equals(criteria.getOperation())) {
				operator = " is null ";
			}
			else if (RuleCriteria.IS_NOT_BLANK.equals(criteria.getOperation())) {
				operator = " is not null ";
			}
			
			if (RuleCriteria.IS_BLANK.equals(criteria.getOperation())
					|| RuleCriteria.IS_NOT_BLANK.equals(criteria.getOperation())) {
				sql = CRIT[parms.size()] + " " + field + operator;
			}
			else if (criteria.getValue() != null) {
				if (col.getDataType().isAssignableFrom(String.class)) {
					String value = ((String) criteria.getValue()).trim();
					
					if (toLowerCase || RuleCriteria.STARTS_WITH.equals(criteria.getOperation())
							|| RuleCriteria.ENDS_WITH.equals(criteria.getOperation())
							|| RuleCriteria.CONTAINS.equals(criteria.getOperation())
							|| RuleCriteria.REG_EX.equals(criteria.getOperation())) {
						// make it case insensitive
						field = "lower(" + field + ")";
						value = value.toLowerCase();
					}
					
					if (RuleCriteria.STARTS_WITH.equals(criteria.getOperation())) {
						operator = " like ? ";
						value = value + "%";
					}
					else if (RuleCriteria.ENDS_WITH.equals(criteria.getOperation())) {
						operator = " like ? ";
						value = "%" + value;
					}
					else if (RuleCriteria.CONTAINS.equals(criteria.getOperation())
							|| RuleCriteria.REG_EX.equals(criteria.getOperation())) {
						operator = " like ? ";
						if (value.indexOf(" ") < 0) { // only one word
							value = "%" + value + "%";
						}
						else { // two or more words
							if (PagingVo.MatchBy.AnyWords.equals(criteria.getMatchBy())) {
								operator = " REGEXP ? ";
								value = (value + "").replaceAll("[ ]+", "|"); // any word
							}
							else if (PagingVo.MatchBy.AllWords.equals(criteria.getMatchBy())) {
								String[] items = value.split("[ ]+");
								for (String item : items) {
									sql += CRIT[parms.size()] + " " + field + operator;
									parms.add("%" + item + "%");
								}
								return sql;
							}
							else {
								value = "%" + value + "%";
							}
						}
					}
					
					sql = CRIT[parms.size()] + " " + field + operator;
					parms.add(value);
				}
				else if (col.getDataType().isAssignableFrom(Boolean.class)) {
					sql = CRIT[parms.size()] + " " + field + operator;
					if (Boolean.TRUE.equals(criteria.getValue())) {
						parms.add(Constants.Y);
					}
					else {
						parms.add(Constants.N);
					}
				}
				else {
					Object value = criteria.getValue();
					sql = CRIT[parms.size()] + " " + field + operator;
					parms.add(value);
				}
			}
		}
		return sql;
	}
	
	public int getPageSize() {
		return pageSize;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

	public long getRowCount() {
		return rowCount;
	}

	public void setRowCount(long rowCount) {
		this.rowCount = rowCount;
	}

	public int getPageNumber() {
		return pageNumber;
	}

	public void setPageNumber(int pageNumber) {
		this.pageNumber = pageNumber;
	}

	public Object getSearchObjFirst() {
		return searchObjFirst;
	}

	public void setSearchObjFirst(Object searchObjFirst) {
		this.searchObjFirst = searchObjFirst;
	}

	public Object getSearchObjLast() {
		return searchObjLast;
	}

	public void setSearchObjLast(Object searchObjLast) {
		this.searchObjLast = searchObjLast;
	}

}
