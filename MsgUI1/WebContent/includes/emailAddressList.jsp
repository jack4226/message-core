<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>

<h:messages styleClass="errors" layout="list" rendered="#{debug.showMessages}"/>
<h:outputFormat value="#{msgs.manageEmailAddresses}" styleClass="gridHeader">
   <f:param value=""/>
</h:outputFormat>
<h:dataTable binding="#{emailAddrsBean.dataTable}" 
   value="#{emailAddrsBean.emailAddrs}" var="list" 
   rows="#{emailAddrsBean.pagingVo.pageSize}"
   styleClass="jsfDataTable" rowClasses="oddRows, evenRows"
   headerClass="dataTableHeader" footerClass="dataTableFooter"
   columnClasses="twoPercent,fourtyPercent,fivePercent,fivePercent,tenPercent,twentyPercent,
   	tenPercent,fivePercent,fivePercent,fivePercent">
   
   <h:column>
      <f:facet name="header">
         <h:outputText value=""/>
      </f:facet>
      <h:selectBooleanCheckbox value="#{list.markedForDeletion}" title="#{list.emailAddr}_checkBox"
         disabled="#{not list.editable}" onchange="submit()"/>
   </h:column>
   <h:column>
      <f:facet name="header">
         <h:outputText value="#{msgs.emailAddrHeader}"/>
      </f:facet>
      <div class="cellHeight">
      <h:commandLink action="#{emailAddrsBean.viewEmailAddr}" title="#{list.emailAddr}">
      	<h:outputText value="#{list.emailAddr}" title="Click to Edit"/>
      	<f:param name="emailaddr" value="#{list.emailAddr}"/>
      </h:commandLink>
      </div>
   </h:column>
   <h:column>
      <f:facet name="header">
         <h:outputText value="#{msgs.bounceCountHeader}"/>
      </f:facet>
      <h:outputText value="#{list.bounceCount}" style="text-align: center;" title="#{list.emailAddr}_bounceCount"/>
   </h:column>
   <h:column>
      <f:facet name="header">
         <h:outputText value="#{msgs.acceptHtmlHeader}"/>
      </f:facet>
		<h:outputText value="#{list.acceptHtmlDesc}" title="#{list.emailAddr}_acceptHtml"/>
   </h:column>
   <h:column>
      <f:facet name="header">
         <h:outputText value="#{msgs.lastReceivedTimeHeader}"/>
      </f:facet>
      <div class="cellHeight">
	  <h:outputText value="#{list.lastRcptTime}" title="#{list.emailAddr}_lastRcptTime">
	  	<f:convertDateTime pattern="yyyy-MM-dd HH:mm"/>
	  </h:outputText>
	  </div>
   </h:column>
   <h:column>
      <f:facet name="header">
         <h:outputText value="#{msgs.customerNameHeader}"/>
      </f:facet>
      <div class="cellHeight">
	  <h:outputText value="#{list.customerName}" title="#{list.emailAddr}_customerName"/>
	  </div>
   </h:column>
   <h:column>
      <f:facet name="header">
         <h:outputText value="#{msgs.statusIdHeader}"/>
      </f:facet>
		<h:outputText value="#{list.statusIdDesc}" title="#{list.emailAddr}_statusId"/>
   </h:column>
   <h:column>
      <f:facet name="header">
         <h:outputText value="#{msgs.sentCountHeader}"/>
      </f:facet>
		<h:outputText value="#{list.sentCount}" title="#{list.emailAddr}_sentCount"/>
   </h:column>
   <h:column>
      <f:facet name="header">
         <h:outputText value="#{msgs.openCountHeader}"/>
      </f:facet>
		<h:outputText value="#{list.openCount}" title="#{list.emailAddr}_openCount"/>
   </h:column>
   <h:column>
      <f:facet name="header">
         <h:outputText value="#{msgs.clickCountHeader}"/>
      </f:facet>
		<h:outputText value="#{list.clickCount}" title="#{list.emailAddr}_clickCount"/>
   </h:column>
	<f:facet name="footer">
    <h:panelGroup>
	<h:panelGrid columns="2" styleClass="fullWidth"
		columnClasses="alignLeft50, alignRight50">
		<h:panelGroup>
			<h:inputText value="#{emailAddrsBean.searchString}" size="40"
				maxlength="100" title="Address to Search"/>
			<f:verbatim>&nbsp;</f:verbatim>
			<h:commandButton value="#{msgs.searchByEmailButtonText}" title="Submit Search"
				action="#{emailAddrsBean.searchByAddress}"/>
			<f:verbatim>&nbsp;</f:verbatim>
			<h:commandButton value="#{msgs.resetButtonText}" title="Reset Search"
				action="#{emailAddrsBean.resetSearch}"/>
		</h:panelGroup>
		<h:panelGroup>
          	<h:outputText value="#{emailAddrsBean.dataTable.first + 1}"
          		style="font-weight: bold;" title="First Address Number"/>
          	<h:outputText value=" - "/>
			<h:outputText value="#{emailAddrsBean.lastPageRow}" title="Last Address Number"
				style="font-weight: bold;" />
			<h:outputText value=" of " style="font-weight: bold;"/>
			<h:outputText value="#{emailAddrsBean.dataTable.rowCount}" 
          		style="font-weight: bold;" title="Total Address Count"/>
          	<f:verbatim>&nbsp;&nbsp;</f:verbatim>
            <h:commandLink value="#{msgs.firstLinkText}" action="#{emailAddrsBean.pageFirst}"
                disabled="#{emailAddrsBean.dataTable.first == 0}" title="Page First"/>
            <f:verbatim>&nbsp;|&nbsp;</f:verbatim>
            <h:commandLink value="#{msgs.prevLinkText}" action="#{emailAddrsBean.pagePrevious}"
                disabled="#{emailAddrsBean.dataTable.first == 0}" title="Page Previous"/>
            <f:verbatim>&nbsp;|&nbsp;</f:verbatim>
            <h:commandLink value="#{msgs.nextLinkText}" action="#{emailAddrsBean.pageNext}"
                disabled="#{emailAddrsBean.dataTable.first + emailAddrsBean.dataTable.rows
                    >= emailAddrsBean.dataTable.rowCount}" title="Page Next"/>
            <f:verbatim>&nbsp;|&nbsp;</f:verbatim>
            <h:commandLink value="#{msgs.lastLinkText}" action="#{emailAddrsBean.pageLast}"
                disabled="#{emailAddrsBean.dataTable.first + emailAddrsBean.dataTable.rows
                    >= emailAddrsBean.dataTable.rowCount}" title="Page Last"/>
        </h:panelGroup>
    </h:panelGrid>
    </h:panelGroup>
	</f:facet>
</h:dataTable>
<h:outputText value="#{emailAddrsBean.actionFailure}"
	rendered="#{emailAddrsBean.actionFailure != null}" styleClass="errorMessage"
	id="actionFailure" />
<f:verbatim><p/></f:verbatim>
<h:panelGrid columns="2" styleClass="commandBar"
	columnClasses="alignLeft70, alignRight30">
	<h:panelGroup>
		<h:commandButton value="#{msgs.deleteButtonText}"
			title="Delete selected rows" action="#{emailAddrsBean.deleteEmailAddrs}"
			disabled="#{not emailAddrsBean.anyEmailAddrsMarkedForDeletion}"
			onclick="javascript:return confirmDelete();" />
		<f:verbatim>&nbsp;</f:verbatim>
		<%-- h:commandButton value="#{msgs.saveSelectedButtonText}"
			title="Delete selected rows" action="#{emailAddrsBean.saveEmailAddrs}"
			disabled="#{not emailAddrsBean.anyEmailAddrsMarkedForDeletion}"
			onclick="javascript:return confirmSubmit();" />
		<f:verbatim>&nbsp;</f:verbatim --%>
		<h:commandButton value="#{msgs.addNewButtonText}"
			title="Add a new row" action="#{emailAddrsBean.addEmailAddr}" />
		<f:verbatim>&nbsp;</f:verbatim>
		<h:commandButton value="#{msgs.refreshLinkText}"
			title="Refresh from database" action="#{emailAddrsBean.refreshPage}" />
		<f:verbatim>&nbsp;</f:verbatim>
		<h:commandButton value="#{msgs.backButtonText}" action="#{emailAddrsBean.cancelEdit}"/>
	</h:panelGroup>
	<h:panelGroup>
		<f:verbatim>&nbsp;</f:verbatim>
	</h:panelGroup>
</h:panelGrid>