package ltj.message.bo.mailinglist;

import java.util.HashMap;
import java.util.Map;

import ltj.message.bo.template.RenderVariable;
import ltj.message.constant.Constants;
import ltj.message.constant.VariableName;
import ltj.message.constant.VariableType;
import ltj.message.vo.emailaddr.MailingListVo;

public final class MailingListUtil {

	private MailingListUtil() {
		// static only
	}
	
	public static Map<String, RenderVariable> renderListVariables(MailingListVo listVo, String subscriberAddress,
			long subscriberAddressId) {
		Map<String, RenderVariable> variables = new HashMap<String, RenderVariable>();
		String varName = null;
		RenderVariable var = null;
		
		varName = VariableName.MailingListVariableName.MailingListId.name();
		var = new RenderVariable(
				varName,
				listVo.getListId(),
				null,
				VariableType.TEXT,
				Constants.Y,
				false,
				null);
		variables.put(varName, var);
		
		varName = VariableName.MailingListVariableName.MailingListName.name();
		var = new RenderVariable(
				varName,
				listVo.getDisplayName(),
				null,
				VariableType.TEXT,
				Constants.Y,
				false,
				null);
		variables.put(varName, var);
		
		varName = VariableName.MailingListVariableName.MailingListAddress.name();
		var = new RenderVariable(
				varName,
				listVo.getEmailAddr(),
				null,
				VariableType.TEXT,
				Constants.Y,
				false,
				null);
		variables.put(varName, var);
		
		varName = VariableName.MailingListVariableName.SubscriberAddress.name();
		var = new RenderVariable(
				varName,
				subscriberAddress,
				null,
				VariableType.TEXT,
				Constants.Y,
				false,
				null);
		variables.put(varName, var);
		
		varName = VariableName.MailingListVariableName.SubscriberAddressId.name();
		var = new RenderVariable(
				varName,
				String.valueOf(subscriberAddressId),
				null,
				VariableType.TEXT,
				Constants.Y,
				false,
				null);
		variables.put(varName, var);
		
		return variables;
	}
}
