package com.legacytojava.message.bo.template;
	
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.*;

import com.legacytojava.message.bean.*;
import com.legacytojava.message.vo.template.MsgSourceVo;

public class RenderResponse implements Serializable {
	private static final long serialVersionUID = -735532735569912023L;
	MsgSourceVo msgSourceVo;
	String clientId;
	Timestamp startTime;
	HashMap<String, RenderVariable> variableFinal;
	HashMap<String, RenderVariable> variableErrors;
	MessageBean messageBean;

	RenderResponse(
			MsgSourceVo msgSourceVo,
			String clientId,
			Timestamp startTime,
			HashMap<String, RenderVariable> variableFinal,
			HashMap<String, RenderVariable> variableErrors,
			MessageBean messageBean)
		{
			this.msgSourceVo=msgSourceVo;
			this.clientId=clientId;
			this.startTime=startTime;
			this.variableFinal=variableFinal;
			this.variableErrors=variableErrors;
			this.messageBean=messageBean;
    }

	public String toString()
	{
		String LF = System.getProperty("line.separator","\n");
		StringBuffer sb = new StringBuffer();
		sb.append("========== Display RenderResponse Fields =========="+LF);
		if (msgSourceVo!=null) {
			sb.append(msgSourceVo.toString());
		}
		else {
			sb.append("MsgSourceReq:     "+"null"+LF);
		}
		sb.append("ClientId:        "+clientId+LF);
		sb.append("StartTime:       "+startTime+LF+LF);
		if (variableFinal!=null && !variableFinal.isEmpty()) {
			sb.append("Display Final Variables.........."+LF);
			Collection<RenderVariable> c = variableFinal.values();
			for (Iterator<RenderVariable> it=c.iterator(); it.hasNext();) {
				RenderVariable req = it.next();
				sb.append(req.toString());
			}
		}
		else {
			sb.append("VariableFinal:    "+"null"+LF);
		}
		if (variableErrors!=null && !variableErrors.isEmpty()) {
			sb.append("Display Error Variables.........."+LF);
			Collection<RenderVariable> c = variableErrors.values();
			for (Iterator<RenderVariable> it=c.iterator(); it.hasNext();) {
				RenderVariable req = it.next();
				sb.append(req.toString());
			}
		}
		else {
			sb.append("VariableErrors:   "+"null"+LF);
		}
		if (messageBean!=null)
			sb.append(LF+messageBean.toString());
		return sb.toString();
	}
	   
    public String getClientId() {
		return clientId;
	}

	public MessageBean getMessageBean() {
		return messageBean;
	}

	public MsgSourceVo getMsgSourceVo() {
		return msgSourceVo;
	}

	public HashMap<String, RenderVariable> getVariableErrors() {
		return variableErrors;
	}

	public HashMap<String, RenderVariable> getVariableFinal() {
		return variableFinal;
	}

	public Timestamp getStartTime() {
		return startTime;
	}
}
