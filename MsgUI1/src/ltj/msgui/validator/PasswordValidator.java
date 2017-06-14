package ltj.msgui.validator;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;

import ltj.message.util.StringUtil;

@FacesValidator("passwordValidator")
public class PasswordValidator implements Validator {

	public void validate(FacesContext context, UIComponent component, Object value) throws ValidatorException {
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
		if (StringUtil.isNotEmpty(password) && !password.equals(confirm)) {
			FacesMessage message = ltj.msgui.util.MessageUtil.getMessage("ltj.msgui.messages", "passwordsNotEqual", null);
			message.setSeverity(FacesMessage.SEVERITY_WARN);
			throw new ValidatorException(message);
		}
	}
}
