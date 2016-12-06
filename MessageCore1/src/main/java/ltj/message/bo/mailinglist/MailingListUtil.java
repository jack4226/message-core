package ltj.message.bo.mailinglist;

import java.util.HashMap;

import ltj.message.bo.template.RenderVariable;
import ltj.message.constant.Constants;
import ltj.message.constant.VariableName;
import ltj.message.constant.VariableType;
import ltj.message.vo.emailaddr.MailingListVo;

public final class MailingListUtil {

	private MailingListUtil() {
		// static only
	}
	
	public static HashMap<String, RenderVariable> renderListVariables(MailingListVo listVo,
			String subscriberAddress, long subscriberAddressId) {
		HashMap<String, RenderVariable> variables = new HashMap<String, RenderVariable>();
		String varName = null;
		RenderVariable var = null;
		
		varName = VariableName.LIST_VARIABLE_NAME.MailingListId.toString();
		var = new RenderVariable(
				varName,
				listVo.getListId(),
				null,
				VariableType.TEXT,
				Constants.YES_CODE,
				Constants.NO_CODE,
				null);
		variables.put(varName, var);
		
		varName = VariableName.LIST_VARIABLE_NAME.MailingListName.toString();
		var = new RenderVariable(
				varName,
				listVo.getDisplayName(),
				null,
				VariableType.TEXT,
				Constants.YES_CODE,
				Constants.NO_CODE,
				null);
		variables.put(varName, var);
		
		varName = VariableName.LIST_VARIABLE_NAME.MailingListAddress.toString();
		var = new RenderVariable(
				varName,
				listVo.getEmailAddr(),
				null,
				VariableType.TEXT,
				Constants.YES_CODE,
				Constants.NO_CODE,
				null);
		variables.put(varName, var);
		
		varName = VariableName.LIST_VARIABLE_NAME.SubscriberAddress.toString();
		var = new RenderVariable(
				varName,
				subscriberAddress,
				null,
				VariableType.TEXT,
				Constants.YES_CODE,
				Constants.NO_CODE,
				null);
		variables.put(varName, var);
		
		varName = VariableName.LIST_VARIABLE_NAME.SubscriberAddressId.toString();
		var = new RenderVariable(
				varName,
				String.valueOf(subscriberAddressId),
				null,
				VariableType.TEXT,
				Constants.YES_CODE,
				Constants.NO_CODE,
				null);
		variables.put(varName, var);
		
		return variables;
	}
}
