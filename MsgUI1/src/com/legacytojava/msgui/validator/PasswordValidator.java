package com.legacytojava.msgui.validator;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;

import com.legacytojava.message.util.StringUtil;

public class PasswordValidator implements Validator {

	public void validate(FacesContext context, UIComponent component, Object value)
			throws ValidatorException {
		// Obtain the first password field from f:attribute.
		String password1 = (String) component.getAttributes().get("password1");
		// Find the actual JSF component for the password.
		UIInput passwordInput = (UIInput) context.getViewRoot().findComponent(password1);
		// Get entered value of the first password.
		String password = (String) passwordInput.getValue();
		// get the entered value of the second password
		String confirm = (String) value;
		// Check if the first password is actually entered and compare it with
		// second password.
		if (!StringUtil.isEmpty(password) && !password.equals(confirm)) {
	        FacesMessage message = com.legacytojava.msgui.util.Messages.getMessage(
					"com.legacytojava.msgui.messages", "passwordsNotEqual", null);
			message.setSeverity(FacesMessage.SEVERITY_WARN);
			throw new ValidatorException(message);
		}
	}
}
