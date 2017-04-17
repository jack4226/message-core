<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>

<h:messages styleClass="errors" layout="list" rendered="#{debug.showMessages}"/>
<h:outputFormat value="#{msgs.viewBroadcastMessages}" styleClass="gridHeader">
   <f:param value=""/>
</h:outputFormat>
<h:dataTable binding="#{broadcastsListBean.dataTable}" 
   value="#{broadcastsListBean.broadcasts}" var="list" 
   rows="#{broadcastsListBean.pagingVo.pageSize}"
   styleClass="jsfDataTable" rowClasses="oddRows, evenRows"
   headerClass="dataTableHeader" footerClass="dataTableFooter">
   
   <h:column>
      <f:facet name="header">
         <h:outputText value=""/>
      </f:facet>
      <h:selectBooleanCheckbox value="#{list.markedForDeletion}" title="#{list.msgId}_checkBox"
         disabled="#{not list.editable}" onchange="submit()"/>
   </h:column>
   <h:column>
      <f:facet name="header">
         <h:outputText value="#{msgs.timeStartedHeader}"/>
      </f:facet>
      <h:commandLink action="#{broadcastsListBean.viewBroadcastMsg}" title="#{list.msgId}">
	      <h:outputText value="#{list.startTime}" title="Click to view broadcast message">
	      	<f:convertDateTime pattern="yyyy-MM-dd HH:mm:ss"/>
	      </h:outputText>
	      <f:param name="msgId" value="#{list.msgId}"/>
      </h:commandLink>
   </h:column>
   <h:column>
      <f:facet name="header">
         <h:outputText value="#{msgs.timeEndedHeader}"/>
      </f:facet>
		<h:outputText value="#{list.endTime}" title="#{list.msgId}_endTime">
		  <f:convertDateTime pattern="yyyy-MM-dd HH:mm:ss"/>
		</h:outputText>
   </h:column>
   <h:column>
      <f:facet name="header">
         <h:outputText value="#{msgs.listIdHeader}"/>
      </f:facet>
      <h:outputText value="#{list.listId}" title="#{list.msgId}_listId"/>
   </h:column>
   <h:column>
      <f:facet name="header">
         <h:outputText value="#{msgs.deliveryOptionHeader}"/>
      </f:facet>
		<h:outputText value="#{list.deliveryOptionDesc}" title="#{list.msgId}_dlvrOpt"/>
   </h:column>
   <h:column>
      <f:facet name="header">
         <h:outputText value="#{msgs.sentCountHeader}"/>
      </f:facet>
      <h:outputText value="#{list.sentCount}" title="#{list.msgId}_sentCount"/>
   </h:column>
   <h:column>
      <f:facet name="header">
         <h:outputText value="#{msgs.openCountHeader}"/>
      </f:facet>
      <h:outputText value="#{list.openCount}" title="#{list.msgId}_openCount"/>
   </h:column>
   <h:column>
      <f:facet name="header">
         <h:outputText value="#{msgs.clickCountHeader}"/>
      </f:facet>
      <h:outputText value="#{list.clickCount}" title="#{list.msgId}_clickCount"/>
   </h:column>
   <h:column>
      <f:facet name="header">
         <h:outputText value="#{msgs.lastOpenedHeader}"/>
      </f:facet>
      <h:outputText value="#{list.lastOpenTime}" title="#{list.msgId}_lastOpenTime">
      	<f:convertDateTime pattern="yyyy-MM-dd"/>
      </h:outputText>
   </h:column>
   <h:column>
      <f:facet name="header">
         <h:outputText value="#{msgs.lastClickedHeader}"/>
      </f:facet>
      <h:outputText value="#{list.lastClickTime}" title="#{list.msgId}_lastClickTime">
      	<f:convertDateTime pattern="yyyy-MM-dd"/>
      </h:outputText>
   </h:column>
   <h:column>
      <f:facet name="header">
         <h:outputText value="#{msgs.unsubscribeCountHeader}"/>
      </f:facet>
	  <h:outputText value="#{list.unsubscribeCount}" title="#{list.msgId}_unsubCount"/>
   </h:column>
	<f:facet name="footer">
    <h:panelGroup>
	<h:panelGrid columns="2" styleClass="fullWidth"
		columnClasses="alignLeft50, alignRight50">
		<h:panelGroup>
			<f:verbatim>&nbsp;</f:verbatim>
		</h:panelGroup>
		<h:panelGroup>
          	<h:outputText value="#{broadcastsListBean.dataTable.first + 1}" title="First Number"
          		style="font-weight: bold;"/>
          	<h:outputText value=" - "/>
			<h:outputText value="#{broadcastsListBean.lastPageRow}" title="Last Number"
				style="font-weight: bold;" />
			<h:outputText value=" of #{broadcastsListBean.dataTable.rowCount}" title="Total Row Count"
          		style="font-weight: bold;"/>
          	<f:verbatim>&nbsp;&nbsp;</f:verbatim>
            <h:commandLink value="#{msgs.firstLinkText}" action="#{broadcastsListBean.pageFirst}"
                disabled="#{broadcastsListBean.dataTable.first == 0}" title="Page First"/>
            <f:verbatim>&nbsp;|&nbsp;</f:verbatim>
            <h:commandLink value="#{msgs.prevLinkText}" action="#{broadcastsListBean.pagePrevious}"
                disabled="#{broadcastsListBean.dataTable.first == 0}" title="Page Previous"/>
            <f:verbatim>&nbsp;|&nbsp;</f:verbatim>
            <h:commandLink value="#{msgs.nextLinkText}" action="#{broadcastsListBean.pageNext}"
                disabled="#{broadcastsListBean.dataTable.first + broadcastsListBean.dataTable.rows
                    >= broadcastsListBean.dataTable.rowCount}" title="Page Next"/>
            <f:verbatim>&nbsp;|&nbsp;</f:verbatim>
            <h:commandLink value="#{msgs.lastLinkText}" action="#{broadcastsListBean.pageLast}"
                disabled="#{broadcastsListBean.dataTable.first + broadcastsListBean.dataTable.rows
                    >= broadcastsListBean.dataTable.rowCount}" title="Page Last"/>
        </h:panelGroup>
    </h:panelGrid>
    </h:panelGroup>
	</f:facet>
</h:dataTable>
<f:verbatim><p/></f:verbatim>
<h:panelGrid columns="2" styleClass="commandBar"
	columnClasses="alignLeft70, alignRight30">
	<h:panelGroup>
		<h:commandButton value="#{msgs.deleteButtonText}"
			title="Delete selected rows" action="#{broadcastsListBean.deleteBroadcasts}"
			disabled="#{not broadcastsListBean.anyBroadcastsMarkedForDeletion}"
			onclick="javascript:return confirmDelete();" />
		<f:verbatim>&nbsp;</f:verbatim>
		<h:commandButton value="#{msgs.saveSelectedButtonText}"
			title="Delete selected rows" action="#{broadcastsListBean.saveBroadcasts}"
			disabled="#{not broadcastsListBean.anyBroadcastsMarkedForDeletion}"
			onclick="javascript:return confirmSubmit();" />
		<f:verbatim>&nbsp;</f:verbatim>
		<h:commandButton value="#{msgs.refreshLinkText}"
			title="Refresh from database" action="#{broadcastsListBean.refreshPage}" />
		<f:verbatim>&nbsp;</f:verbatim>
		<h:commandButton value="#{msgs.backButtonText}" action="#{broadcastsListBean.cancelEdit}"/>
	</h:panelGroup>
	<h:panelGroup>
		<f:verbatim>&nbsp;</f:verbatim>
	</h:panelGroup>
</h:panelGrid>