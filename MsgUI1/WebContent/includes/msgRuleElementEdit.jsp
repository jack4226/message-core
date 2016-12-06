<%@ taglib uri="http://java.sun.com/jsf/core"  prefix="f" %>
<%@ taglib uri="http://java.sun.com/jsf/html"  prefix="h" %>

<h:panelGrid columns="1" styleClass="gettingStartedContent">
	<h:messages styleClass="errors" layout="list" rendered="#{debug.showMessages}"/>
	<h:outputFormat value="#{msgs.msgRuleElementEditLabel}" styleClass="gridHeader">
	   <f:param value="#{msgrules.ruleElement.ruleName}.#{msgrules.ruleElement.elementSeq}"/>
	</h:outputFormat>
	<h:panelGrid columns="3" styleClass="editPaneHeader"
		columnClasses="promptColumn, inputColumn, messageColumn">
		<h:outputText value="#{msgs.ruleNamePrompt}"/>
		<h:inputText id="rulename" value="#{msgrules.ruleElement.ruleName}"
			required="true" label="#{msgs.ruleNamePrompt}" maxlength="26" size="30"
			disabled="#{msgrules.ruleElement.markedForEdition}">
			<f:validateLength minimum="1" maximum="26"/>
		</h:inputText>
		<h:message for="rulename" styleClass="errorMessage"/>
		
		<h:outputText value="#{msgs.ruleElementSeqPrompt}"/>
		<h:inputText id="ruleseq" value="#{msgrules.ruleElement.elementSeq}"
			required="true" label="#{msgs.ruleElementSeqPrompt}" size="5"
			disabled="#{msgrules.ruleElement.markedForEdition}">
			<f:validateLongRange minimum="0" maximum="999"/>
		</h:inputText>
		<h:message for="ruleseq" styleClass="errorMessage"/>
		
		<h:outputText value="#{msgs.dataNamePrompt}"/>
		<h:selectOneMenu id="dataname" value="#{msgrules.ruleElement.dataName}"
			required="true" label="#{msgs.dataNamePrompt}" onchange="submit()">
			<f:selectItems value="#{codes.ruleDataNameItems}"/>
		</h:selectOneMenu>
		<h:message for="dataname" styleClass="errorMessage"/>
		
		<h:outputText value="#{msgs.headerNamePrompt}"
			rendered="#{msgrules.ruleElement.dataName == 'X-Header'}"/>
		<h:inputText id="headername" value="#{msgrules.ruleElement.headerName}"
			size="30" maxlength="50" label="#{msgs.headerNamePrompt}"
			rendered="#{msgrules.ruleElement.dataName == 'X-Header'}">
			<f:validateLength minimum="1" maximum="50"/>
		</h:inputText>
		<h:message for="headername" styleClass="errorMessage"
			rendered="#{msgrules.ruleElement.dataName == 'X-Header'}"/>
		
		<h:outputText value="#{msgs.criteriaPrompt}"/>
		<h:selectOneMenu id="criteria" value="#{msgrules.ruleElement.criteria}"
			required="true" label="#{msgs.criteriaPrompt}" onchange="submit()">
			<f:selectItems value="#{codes.ruleCriteriaItems}"/>
		</h:selectOneMenu>
		<h:message for="criteria" styleClass="errorMessage"/>
		
		<h:outputText value="#{msgs.targetTextPrompt}"/>
		<h:selectOneMenu value="#{msgrules.ruleElement.targetText}"
			rendered="#{msgrules.ruleElement.dataName == 'CarrierCode'}">
			<f:selectItems value="#{codes.mailCarrierCodeItems}"/>
		</h:selectOneMenu>
		<h:selectOneMenu value="#{msgrules.ruleElement.targetText}"
			rendered="#{msgrules.ruleElement.dataName == 'RuleName'}">
			<f:selectItems value="#{dynacodes.builtinRuleNameItems}"/>
		</h:selectOneMenu>
		<h:inputTextarea id="targettext1" value="#{msgrules.ruleElement.targetText}" 
			title="#{msgrules.ruleElement.targetText}"
			label="#{msgs.targetTextPrompt}" rows="5" cols="80"
			rendered="#{msgrules.ruleElement.dataName != 'CarrierCode' 
				&& msgrules.ruleElement.dataName != 'RuleName'
				&& msgrules.ruleElement.criteria != 'reg_ex'}">
			<f:validateLength maximum="2000"/>
		</h:inputTextarea>
		<h:inputTextarea id="targettext2" value="#{msgrules.ruleElement.targetText}" 
			title="#{msgrules.ruleElement.targetText}"
			label="#{msgs.targetTextPrompt}" rows="5" cols="80"
			rendered="#{msgrules.ruleElement.dataName != 'CarrierCode' 
				&& msgrules.ruleElement.dataName != 'RuleName'
				&& msgrules.ruleElement.criteria == 'reg_ex'}"
			validator="#{msgrules.validateRegex}">
			<f:validateLength maximum="2000"/>
		</h:inputTextarea>
		<h:message for="targettext2" styleClass="errorMessage"/>
		
		<h:outputText value="#{msgs.caseSensitivePrompt}"/>
		<h:selectBooleanCheckbox id="case" 
			value="#{msgrules.ruleElement.textCaseSensitive}"/>
		<h:message for="case" styleClass="errorMessage"/>
		
		<h:outputText value="#{msgs.targetProcPrompt}"/>
		<h:selectOneMenu id="targetproc" value="#{msgrules.ruleElement.targetProc}">
			<f:selectItem itemValue="" itemLabel="Not Selected"/>
			<f:selectItems value="#{codes.targetProcItems}"/>
			<f:converter converterId="NullableStringConverter"/>
		</h:selectOneMenu>
		<h:message for="targetproc" styleClass="errorMessage"/>
		
		<h:outputText value="#{msgs.exclusionsPrompt}"/>
		<h:inputTextarea id="exclusions" value="#{msgrules.ruleElement.exclusions}"
			label="#{msgs.exclusionsPrompt}" rows="3" cols="80">
			<f:validateLength maximum="65536"/>
			<f:converter converterId="NullableStringConverter"/>
		</h:inputTextarea>
		<h:message for="exclusions" styleClass="errorMessage"/>
		
		<h:outputText value="#{msgs.delimiterPrompt}"/>
		<h:inputText id="delimiter" value="#{msgrules.ruleElement.delimiter}"
			label="#{msgs.delimiterPrompt}" size="2" maxlength="5">
			<f:converter converterId="NullableStringConverter"/>
		</h:inputText>
		<h:message for="delimiter" styleClass="errorMessage"/>
	</h:panelGrid>
	<h:outputText value="#{msgs[msgrules.testResult]}"
		rendered="#{msgrules.testResult != null}" styleClass="errorMessage"
		id="testResult" />
	<f:verbatim><p/></f:verbatim>
	<h:panelGrid columns="2" styleClass="commandBar"
		columnClasses="alignLeft70, alignRight30">
		<h:panelGroup>
			<h:commandButton value="#{msgs.doneButtonText}" title="Done Edit"
				action="#{msgrules.doneRuleElementEdit}"/>
			<f:verbatim>&nbsp;</f:verbatim>
			<h:commandButton type="reset" value="#{msgs.resetButtonText}"
				title="Reset" />
			<f:verbatim>&nbsp;</f:verbatim>
			<h:commandButton value="#{msgs.cancelButtonText}" title="Cancel Changes"
				immediate="true" action="#{msgrules.cancelEdit}" />
		</h:panelGroup>
		<h:panelGroup>
			<f:verbatim>&nbsp;</f:verbatim>
		</h:panelGroup>
	</h:panelGrid>
</h:panelGrid>
