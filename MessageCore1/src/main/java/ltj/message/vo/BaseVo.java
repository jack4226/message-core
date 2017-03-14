package ltj.message.vo;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.commons.lang.StringUtils;

import ltj.message.constant.StatusId;
import ltj.message.util.Printf;

@XmlAccessorType (XmlAccessType.NONE)
@XmlRootElement (name = "baseVo")
public class BaseVo implements java.io.Serializable, Cloneable {
	private static final long serialVersionUID = 2438423000525545863L;
	
	final static String LF = System.getProperty("line.separator", "\n");
	@XmlElement
	protected String primaryKey = null;
	@XmlElement
	private String statusId = StatusId.ACTIVE.value();
	@XmlElement
    @XmlJavaTypeAdapter(TimestampAdapter.class)
	protected Timestamp updtTime = null;
    @XmlElement
	protected String updtUserId = null;
    @XmlElement
	@XmlJavaTypeAdapter(TimestampAdapter.class)
	private Timestamp origUpdtTime = null;

    @XmlTransient
	private List<ChangeLog> changeLogs = null;

	/**
	 * Define fields and methods used by UI components
	 */
	@XmlTransient
	protected boolean editable = true;
	@XmlTransient
	protected boolean markedForDeletion = false;
	@XmlTransient
	protected boolean markedForEdition = false;
	
	public BaseVo() {
		primaryKey = null;	
		statusId = StatusId.ACTIVE.value();
		updtTime = null;
		updtUserId = null;
		
		origUpdtTime = null;
		changeLogs = null;

		editable = true;
		markedForDeletion = false;
		markedForEdition = false;
	}

	public boolean isEditable() {
		return editable;
	}
	public void setEditable(boolean editable) {
		this.editable = editable;
	}
	public boolean isMarkedForDeletion() {
		return markedForDeletion;
	}
	public void setMarkedForDeletion(boolean markedForDeletion) {
		this.markedForDeletion = markedForDeletion;
	}
	public boolean isMarkedForEdition() {
		return markedForEdition;
	}
	public void setMarkedForEdition(boolean markedForEdition) {
		this.markedForEdition = markedForEdition;
	}
	
	public String getStatusIdDesc() {
		try {
			Method method = this.getClass().getMethod("getStatusId", (Class[])null);
			String desc = (String) method.invoke(this, (Object[])null);
			if (StatusId.ACTIVE.value().equals(desc)) {
				desc = "Active";
			}
			else if (StatusId.INACTIVE.value().equals(desc)) {
				desc = "Inactive";
			}
			else if (StatusId.SUSPENDED.value().equals(desc)) {
				desc = "Suspended";
			}
			return desc;
		}
		catch (Exception e) {
			// ignored
			return null;
		}
	}
	/** end of UI components */
	
	public boolean equalsTo(BaseVo vo) {
		getLogList().clear();
		if (this == vo) {
			return true;
		}
		if (vo == null) {
			return false;
		}
		Method thisMethods[] = this.getClass().getMethods();
		for (int i = 0; i < thisMethods.length; i++) {
			Method method = thisMethods[i];
			String methodName = method.getName();
			Class<?>[] params = method.getParameterTypes();
			if (methodName.length() > 3 && methodName.startsWith("get") && params.length == 0) {
				Method voMethod = null;
				try {
					voMethod = vo.getClass().getMethod(methodName, params);
				}
				catch (NoSuchMethodException e) {
					System.err.println("in equalsTo(): " + e.getMessage());
					return false;
				}
				try {
					Class<?> returnType = method.getReturnType();
					String returnTypeName = returnType.getName();
					if (isValidReturnType(returnType) || (returnTypeName.endsWith("PageAction"))) {
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
					System.err.println("in equalsTo(): " + e.getMessage());
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
	
	public String toString() {
		return toString(this);
	}

	public String toString(Object vo) {
		return toString(vo, 0);
	}

	String toString(int level) {
		return toString(this, level);
	}

	String toString(Object vo, int level) {
		if (vo == null) {
			return ("-null");
		}
		if (level > 5) {
			System.err.println("toString() has reached a deep level, a possible loop condition?");
			return "...... (deep level)";
		}
		
		Map<String, Method> methodMap = new HashMap<>();
		List<String> methodList = new ArrayList<>();
		Method methods[] = vo.getClass().getMethods();

		// sort the attributes by name
		for (int i = 0; i < methods.length; i++) {
			methodMap.put(methods[i].getName(), methods[i]);
			methodList.add(methods[i].getName());
		}
		Collections.sort(methodList);
		
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < methodList.size(); i++) {
			String methodName = methodList.get(i);
			Method method = methodMap.get(methodName);
			Object[] params = method.getParameterTypes();
			String paramClassName = vo.getClass().getSimpleName();

			if (methodName.startsWith("get") && params.length == 0) {
				try {
					sb.append("     " + dots(level) + paramClassName + "." + StringUtils.uncapitalize(methodName));
					sb.append("=");
					Class<?> returnType = method.getReturnType();
					String returnTypeName = returnType.getName();
					if (isValidReturnType(returnType)) {
						Object rtnObj = method.invoke(vo, params);
						if (rtnObj == null) {
							sb.append("null");
						}
						else {
							sb.append(StringUtils.trim(rtnObj.toString()));
						}
					}
					else if (returnTypeName.endsWith("java.lang.Class")) {
						sb.append(((Class<?>) method.invoke(vo, params)).getName());
					}
					else if (method.getName().indexOf("Vo") > 0) {
						Object subVo = method.invoke(vo, params);
						if (subVo == null) {
							sb.append("null");
						}
						else {
							// the call to vo.toString lets objects override
							// this default toString method....
							if (subVo instanceof BaseVo) {
								sb.append(LF + ((BaseVo) subVo).toString(level + 1));
							}
							else {
								sb.append(subVo.toString());
							}
						}
					}
					else if (returnType.equals(Class.forName("java.util.ArrayList"))
							|| returnType.equals(Class.forName("java.util.List"))) {
						sb.append("a List");
						List<?> lst = (List<?>) method.invoke(vo, params);
						for (int j = 0; lst != null && j < lst.size(); j++) {
							Object item = lst.get(j);
							if (item instanceof BaseVo) {
								sb.append(LF + ((BaseVo) item).toString(level + 1));
							}
							else if (item != null) {
								sb.append(LF + "     " + dots(level+1) + item.getClass().getCanonicalName());
								sb.append("=" + item.toString());
							}
						}
					}
					else {
						Object subVo = method.invoke(vo, params);
						if (subVo != null) {
							if (!(subVo instanceof BaseVo)) {
								sb.append(subVo.toString());
							}
						}
						else {
							sb.append("null");
						}
					}
					sb.append(LF);
				}
				catch (Exception e) {
					System.err.println("error getting values in toString " + methods[i].getName() + " " + e.getMessage());
				}
			}
		}
		return sb.toString();
	}

	protected boolean isValidReturnType(Class<?> returnType) {
		String returnTypeName = returnType.getName();
		if ((returnTypeName.endsWith("java.lang.String"))
				|| (returnTypeName.endsWith("java.lang.Integer"))
				|| (returnTypeName.endsWith("java.lang.Long"))
				|| (returnTypeName.endsWith("java.sql.Timestamp"))
				|| (returnTypeName.endsWith("java.sql.Date"))
				|| (returnType.equals(java.lang.Integer.TYPE))
				|| (returnType.equals(java.lang.Long.TYPE))
				|| (returnType.equals(java.lang.Character.TYPE))) {
			return true;
		}
		else {
			return false;
		}
	}

	private String dots(int level) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < level; i++) {
			sb.append(".");
		}
		return sb.toString();
	}
	
	public Object getClone() throws CloneNotSupportedException {
		return this.clone();
	}
	
	public List<ChangeLog> getLogList() {
		if (changeLogs == null) {
			changeLogs = new ArrayList<ChangeLog>();
		}
		return changeLogs;
	}
	
	protected void addChangeLog(String fieldName, Object left, Object right) {
		getLogList().add(new ChangeLog(fieldName, left, right));
	}
	
	public String listChanges() {
		StringBuffer sb = new StringBuffer();
		for (ChangeLog item : getLogList()) {
			sb.append(item.printf() + LF);
		}
		return sb.toString();
	}
	
	public static class ChangeLog implements Serializable {
		private static final long serialVersionUID = -5732262969319705777L;
		String fieldName;
		Object leftValue;
		Object rightValue;
		ChangeLog(String fieldName, Object leftValue, Object rightValue) {
			this.fieldName = fieldName;
			this.leftValue = leftValue;
			this.rightValue = rightValue;
		}
		
		String printf() {
			String str = Printf.sprintf("%14s", fieldName) + " : ";
			if (leftValue instanceof Long) {
				str += Printf.sprintf("%14d", leftValue) + "  <->  ";
			}
			else {
				str += Printf.sprintf("%14s", leftValue) + "  <->  ";
			}
			if (rightValue instanceof Long) {
				str += Printf.sprintf("%-14d", rightValue);
			}
			else {
				str += Printf.sprintf("%-14s", rightValue);
			}
			return str;
		}
		
		public String getFieldName() {
			return fieldName;
		}
		public Object getLeftValue() {
			return leftValue;
		}
		public Object getRightValue() {
			return rightValue;
		}
	}
	
	public String getPrimaryKey() {
		return primaryKey;
	}
	public void setPrimaryKey(String primaryKey) {
		this.primaryKey = primaryKey;
	}
	public final String getStatusId() {
		return statusId;
	}
	public final void setStatusId(String status) {
		this.statusId = status;
	}
	public Timestamp getUpdtTime() {
		return updtTime;
	}
	public void setUpdtTime(Timestamp updtTime) {
		this.updtTime = updtTime;
		StackTraceElement[] traces = new Throwable().getStackTrace();
		for (int i = 0; i < traces.length; i++) {
			String className = traces[i].getClassName();
			String methodName = traces[i].getMethodName();
			if (className.endsWith("Mapper") && methodName.equals("mapRow")) {
				// called from DAO JdbcTemplate BeanPropertyRowMapper.mapRow()...
				setOrigUpdtTime(updtTime);
				break;
			}
			else if (i > 3) {
				break;
			}
		}
	}
	public String getUpdtUserId() {
		return updtUserId;
	}
	public void setUpdtUserId(String updtUserId) {
		this.updtUserId = updtUserId;
	}
	public Timestamp getOrigUpdtTime() {
		return origUpdtTime;
	}
	public void setOrigUpdtTime(Timestamp origUpdtTime) {
		this.origUpdtTime = origUpdtTime;
	}
}
