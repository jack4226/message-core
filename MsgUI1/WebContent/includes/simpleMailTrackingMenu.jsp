<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>

<h:panelGrid columns="1" styleClass="gettingStartedMenu" 
		columnClasses="gettingStartedMenuColumn">
	
	<h:panelGroup>
		<h:commandLink value="#{msgs.receivedLinkText}" immediate="false"
			title="Received Messages" action="#{mailtracking.selectReceived}"
			styleClass="#{mailtracking.functionKey=='Received'?'menuLinkTextSelected':'menuLinkText'}"
		/>
		<h:outputText value=" (#{mailtracking.inboxUnreadCount})" styleClass="menuLinkText"/>
	</h:panelGroup>
	<h:panelGroup>
		<h:commandLink value="#{msgs.sentMailLinkText}" immediate="false"
			title="Sent Messages" action="#{mailtracking.selectSent}"
			styleClass="#{mailtracking.functionKey=='Sent'?'menuLinkTextSelected':'menuLinkText'}"
		/>
		<h:outputText value=" (#{mailtracking.sentUnreadCount})" styleClass="menuLinkText"/>
	</h:panelGroup>
	<h:panelGroup>
		<h:commandLink value="#{msgs.allMailLinkText}" immediate="false"
			title="All Messages" action="#{mailtracking.selectAll}"
			styleClass="#{mailtracking.functionKey=='All'?'menuLinkTextSelected':'menuLinkText'}"
		/>
	</h:panelGroup>
	<h:panelGroup>
	<h:commandLink value="#{msgs.closedLinkText}" immediate="false"
		title="Closed Messages" action="#{mailtracking.selectClosed}"
		styleClass="#{mailtracking.functionKey=='Closed'?'menuLinkTextSelected':'menuLinkText'}"
		/>
	<h:inputHidden value="#{mailtracking.functionKey}"/>
	</h:panelGroup>
	
	<f:verbatim><hr width="90%" align="center"></f:verbatim>
	<h:outputText value="#{msgs.listByPrompt}" styleClass="menuLinkLables"/>
	
	<h:outputText value="#{msgs.ruleNamePrompt}" style="font-weight: bold;"/>
	<%-- h:selectOneMenu value="#{mailtracking.ruleName}" onchange="submit()"
		valueChangeListener="#{mailtracking.ruleNameChanged}"/ --%>
	<h:selectOneMenu value="#{mailtracking.ruleName}">
		<f:selectItem itemValue="All" itemLabel="All Rule Names"/>
		<f:selectItems value="#{dynacodes.builtinRuleNameItems}"/>
		<f:selectItems value="#{dynacodes.customRuleNameItems}"/>
	</h:selectOneMenu>
	
	<h:outputText value="#{msgs.subjectPrompt}" style="font-weight: bold;"/>
	<h:inputText value="#{mailtracking.subject}" size="22" maxlength="255" id="subject">
		<f:converter converterId="NullableStringConverter" />
	</h:inputText>
	
	<h:outputText value="#{msgs.bodyTextPrompt}" style="font-weight: bold;"/>
	<h:inputText value="#{mailtracking.body}" size="22" maxlength="255" id="body">
		<f:converter converterId="NullableStringConverter" />
	</h:inputText>
	
	<h:outputText value="#{msgs.fromAddressPrompt}" style="font-weight: bold;"/>
	<h:panelGroup>
	<h:inputText value="#{mailtracking.fromAddress}" size="22" maxlength="255"
		id="fromaddr">
		<f:converter converterId="NullableStringConverter" />
	</h:inputText>
	<h:message for="fromaddr" styleClass="errorMessage"/>
	</h:panelGroup>
	
	<h:outputText value="#{msgs.toEmailAddressPrompt}" style="font-weight: bold;"/>
	<h:panelGroup>
	<h:inputText value="#{mailtracking.toAddress}" size="22" maxlength="255"
		id="toaddr" validator="#{mailtracking.checkEmailAddress}">
		<f:converter converterId="NullableStringConverter" />
	</h:inputText>
	<h:message for="toaddr" styleClass="errorMessage"/>
	</h:panelGroup>
	
	<h:panelGroup>
	<h:commandButton value="#{msgs.searchButtonText}" title="Submit search request"
		action="#{mailtracking.searchBySearchVo}"/>
	<f:verbatim>&nbsp;</f:verbatim>
	<h:commandButton value="#{msgs.resetButtonText}" title="Reset search fields"
		action="#{mailtracking.resetSearchFields}" immediate="true"/>
	</h:panelGroup>
</h:panelGrid>
