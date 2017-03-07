package ltj.message.vo;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

public class PagingVo extends BaseVo implements java.io.Serializable, Cloneable {
	private static final long serialVersionUID = -1139494427014770362L;
	
	// define paging context
	public static final int PAGE_SIZE = 20;
	protected long nbrIdFirst = -1;
	protected long nbrIdLast = -1;
	protected String strIdFirst = null;
	protected String strIdLast = null;
	public static enum PageAction {FIRST, NEXT, PREVIOUS, CURRENT, LAST};
	protected PageAction pageAction = PageAction.CURRENT;
	protected int pageSize = PAGE_SIZE;
	protected int rowCount = -1;
	// end of paging
	
	protected final Set<String> searchFields = new LinkedHashSet<>();
	
	public PagingVo() {
		setSearchFields();
		setStatusId(null);
	}
	
	public void resetPageContext() {
		nbrIdFirst = -1;
		nbrIdLast = -1;
		strIdFirst = null;
		strIdLast = null;
		pageAction = PageAction.CURRENT;
		rowCount = -1;
		
		Field[] fields = this.getClass().getDeclaredFields();
		for (int i = 0; i < fields.length; i++) {
			Field field = fields[i];
			try {
				if (searchFields.contains(field.getName())) {
					if (field.getType().isAssignableFrom(Class.forName("java.lang.Object"))) {
						field.set(field.getType(), null);
					}
				}
			}
			catch (Exception e) {e.printStackTrace();}
		}
	}
	
	protected final void setSearchFields() {
		searchFields.add("statusId");
		Field[] fields = this.getClass().getDeclaredFields();
		for (int i = 0; i < fields.length; i++) {
			Field field = fields[i];
			int mod = field.getModifiers();
			if (Modifier.isPrivate(mod) && !Modifier.isFinal(mod) && !Modifier.isStatic(mod) && !Modifier.isAbstract(mod)) {
				searchFields.add(field.getName());
			}
		}
	}
	
	public boolean equalsToSearch(PagingVo vo) {
		getLogList().clear();
		if (this == vo) {
			return true;
		}
		if (vo == null) {
			return false;
		}
		String className = this.getClass().getName();
		Method thisMethods[] = this.getClass().getMethods();
		for (int i = 0; i < thisMethods.length; i++) {
			Method method = thisMethods[i];
			String methodName = method.getName();
			Class<?>[] params = method.getParameterTypes();
			if (methodName.length() > 3 && methodName.startsWith("get") && params.length == 0) {
				String key = StringUtils.uncapitalize(methodName.substring(3));
				if (!searchFields.contains(key)) {
					continue;
				}
				Method voMethod = null;
				try {
					voMethod = vo.getClass().getMethod(methodName, params);
				}
				catch (NoSuchMethodException e) {
					System.err.println(className + ".equalsToSearch(): " + e.getMessage());
					return false;
				}
				try {
					Class<?> returnType = method.getReturnType();
					String returnTypeName = returnType.getName();
					if (isValidReturnType(returnType) || (returnTypeName.endsWith("MsgType"))) {
						Object thisValue = method.invoke((Object)this, (Object[])params);
						Object voValue = voMethod.invoke((Object)vo, (Object[])params);
						if (thisValue == null) {
							if (voValue != null) {
								addChangeLog(methodName.substring(3), thisValue, voValue);
							}
						}
						else {
							if (!thisValue.equals(voValue)) {
								addChangeLog(methodName.substring(3), thisValue, voValue);
							}
						}
					}
				}
				catch (Exception e) {
					System.err.println(className + ".equalsToSearch(): " + e.getMessage());
					return false;
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
		for (Iterator<String> it=searchFields.iterator(); it.hasNext();) {
			String searchField = it.next();
			System.out.println("Search Field: " + searchField);
		}
	}
	
	public PageAction getPageAction() {
		return pageAction;
	}

	public void setPageAction(PageAction action) {
		this.pageAction = action;
	}

	public int getPageSize() {
		return pageSize;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

	public int getRowCount() {
		return rowCount;
	}

	public void setRowCount(int rowCount) {
		this.rowCount = rowCount;
	}

	public long getNbrIdFirst() {
		return nbrIdFirst;
	}

	public void setNbrIdFirst(long nbrIdFirst) {
		this.nbrIdFirst = nbrIdFirst;
	}

	public long getNbrIdLast() {
		return nbrIdLast;
	}

	public void setNbrIdLast(long nbrIdLast) {
		this.nbrIdLast = nbrIdLast;
	}

	public String getStrIdFirst() {
		return strIdFirst;
	}

	public void setStrIdFirst(String strIdFirst) {
		this.strIdFirst = strIdFirst;
	}

	public String getStrIdLast() {
		return strIdLast;
	}

	public void setStrIdLast(String strIdLast) {
		this.strIdLast = strIdLast;
	}

}
