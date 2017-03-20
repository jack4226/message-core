package ltj.msgui.bean;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.faces.component.html.HtmlDataTable;
import javax.faces.model.DataModel;

import org.apache.log4j.Logger;

import ltj.message.dao.emailaddr.EmailAddressDao;
import ltj.message.dao.inbox.MsgClickCountDao;
import ltj.message.dao.inbox.MsgInboxDao;
import ltj.message.util.StringUtil;
import ltj.message.vo.PagingCountVo;
import ltj.message.vo.PagingVo;
import ltj.message.vo.inbox.MsgClickCountVo;
import ltj.message.vo.inbox.MsgInboxVo;
import ltj.msgui.util.FacesUtil;
import ltj.msgui.util.SpringUtil;

public class BroadcastMsgListBean {
	static final Logger logger = Logger.getLogger(BroadcastMsgListBean.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();

	private MsgClickCountDao msgClickCountDao = null;
	private EmailAddressDao emailAddressDao = null;
	private MsgInboxDao msgInboxDao = null;
	private DataModel broadcasts = null;
	private MsgClickCountVo broadcast = null;
	private boolean editMode = true;
	private MsgInboxVo broadcastMsg = null;

	private HtmlDataTable dataTable;
	private final PagingCountVo pagingAddrVo =  new PagingCountVo();;
	
	private String testResult = null;
	private String actionFailure = null;
	
	static final String TO_VIEW = "broadcastlist.view";
	static final String TO_PAGING = "broadcastlist.paging";
	static final String TO_FAILED = "broadcastlist.failed";
	static final String TO_DELETED = "broadcastlist.deleted";
	static final String TO_SAVED = "broadcastlist.saved";
	static final String TO_CANCELED = "broadcastlist.canceled";

	public DataModel getBroadcasts() {
		String fromPage = FacesUtil.getRequestParameter("frompage");
		if (fromPage != null && fromPage.equals("main")) {
			resetPagingVo();
		}
		// retrieve total number of rows
		if (pagingAddrVo.getRowCount() < 0) {
			int rowCount = getMsgClickCountsDao().getMsgCountForWeb();
			pagingAddrVo.setRowCount(rowCount);
		}
		if (broadcasts == null || !pagingAddrVo.getPageAction().equals(PagingVo.PageAction.CURRENT)) {
			List<MsgClickCountVo> brdList = getMsgClickCountsDao().getBroadcastsWithPaging(pagingAddrVo);
			/* set keys for paging */
			if (!brdList.isEmpty()) {
				MsgClickCountVo firstRow = (MsgClickCountVo) brdList.get(0);
				pagingAddrVo.setNbrIdFirst(firstRow.getMsgId());
				MsgClickCountVo lastRow = (MsgClickCountVo) brdList.get(brdList.size() - 1);
				pagingAddrVo.setNbrIdLast(lastRow.getMsgId());
			}
			else {
				pagingAddrVo.setNbrIdFirst(-1);
				pagingAddrVo.setNbrIdLast(-1);
			}
			//logger.info("PagingAddrVo After: " + pagingAddrVo);
			pagingAddrVo.setPageAction(PagingVo.PageAction.CURRENT);
			broadcasts = new PagedListDataModel(brdList, pagingAddrVo.getRowCount(), pagingAddrVo.getPageSize());
		}
		return broadcasts;
	}
	
	public String pageFirst() {
		dataTable.setFirst(0);
		pagingAddrVo.setPageAction(PagingVo.PageAction.FIRST);
		return TO_PAGING;
	}

	public String pagePrevious() {
		dataTable.setFirst(dataTable.getFirst() - dataTable.getRows());
		pagingAddrVo.setPageAction(PagingVo.PageAction.PREVIOUS);
		return TO_PAGING;
	}

	public String pageNext() {
		dataTable.setFirst(dataTable.getFirst() + dataTable.getRows());
		pagingAddrVo.setPageAction(PagingVo.PageAction.NEXT);
		return TO_PAGING;
	}

	public String pageLast() {
		int count = dataTable.getRowCount();
		int rows = dataTable.getRows();
		dataTable.setFirst(count - ((count % rows != 0) ? count % rows : rows));
		pagingAddrVo.setPageAction(PagingVo.PageAction.LAST);
		return TO_PAGING;
	}
    
	public int getLastPageRow() {
		int lastRow = dataTable.getFirst() + dataTable.getRows();
		if (lastRow > dataTable.getRowCount()) {
			return dataTable.getRowCount();
		}
		else {
			return lastRow;
		}
	}
	
	public PagingCountVo getPagingVo() {
		return pagingAddrVo;
	}
	
	public void refresh() {
		broadcasts = null;
		if (dataTable.getFirst() <= 0) {
			// to display messages newly arrived
			pageFirst();
		}
	}

	public String refreshPage() {
		refresh();
		pagingAddrVo.setRowCount(-1);
		return "";
	}
	
	public void resetPagingVo() {
		pagingAddrVo.resetPageContext();
		if (dataTable != null) {
			dataTable.setFirst(0);
		}
		refresh();
	}

	public MsgClickCountDao getMsgClickCountsDao() {
		if (msgClickCountDao == null) {
			msgClickCountDao = (MsgClickCountDao) SpringUtil.getWebAppContext().getBean("msgClickCountDao");
		}
		return msgClickCountDao;
	}

	public EmailAddressDao getEmailAddrDao() {
		if (emailAddressDao == null) {
			emailAddressDao = (EmailAddressDao) SpringUtil.getWebAppContext().getBean("emailAddressDao");
		}
		return emailAddressDao;
	}

	public MsgInboxDao getMsgInboxDao() {
		if (msgInboxDao == null) {
			msgInboxDao = (MsgInboxDao) SpringUtil.getWebAppContext().getBean("msgInboxDao");
		}
		return msgInboxDao;
	}

	public String viewBroadcastMsg() {
		if (isDebugEnabled) {
			logger.debug("viewBroadcastMsg() - Entering...");
		}
		if (broadcasts == null) {
			logger.warn("viewBroadcastMsg() - Broadcast List is null.");
			return TO_FAILED;
		}
		if (!broadcasts.isRowAvailable()) {
			logger.warn("viewBroadcastMsg() - Broadcast Row not available.");
			return TO_FAILED;
		}
		reset();
		this.broadcast = (MsgClickCountVo) broadcasts.getRowData();
		logger.info("viewBroadcastMsg() - Broadcast to be viewed: " + broadcast.getMsgId());
		broadcast.setMarkedForEdition(true);
		editMode = true;
		broadcastMsg = getMsgInboxDao().getByPrimaryKey(broadcast.getMsgId());
		if (isDebugEnabled) {
			logger.debug("viewBroadcastMsg() - MsgClickCountVo to be passed to jsp: " + broadcast);
		}
		return TO_VIEW;
	}

	public String deleteBroadcasts() {
		if (isDebugEnabled) {
			logger.debug("deleteBroadcasts() - Entering...");
		}
		if (broadcasts == null) {
			logger.warn("deleteBroadcasts() - Broadcast List is null.");
			return TO_FAILED;
		}
		reset();
		List<MsgClickCountVo> subrList = getBroadcastList();
		for (int i=0; i<subrList.size(); i++) {
			MsgClickCountVo vo = subrList.get(i);
			if (vo.isMarkedForDeletion()) {
				int rowsDeleted = getMsgClickCountsDao().deleteByPrimaryKey(vo.getMsgId());
				if (rowsDeleted > 0) {
					logger.info("deleteBroadcasts() - Broadcast deleted: " + vo.getMsgId());
					pagingAddrVo.setRowCount(pagingAddrVo.getRowCount() - rowsDeleted);
				}
			}
		}
		refresh();
		return TO_DELETED;
	}

	public String saveBroadcasts() {
		if (isDebugEnabled) {
			logger.debug("saveBroadcasts() - Entering...");
		}
		if (broadcasts == null) {
			logger.warn("saveBroadcasts() - Broadcast List is null.");
			return TO_FAILED;
		}
		reset();
		List<MsgClickCountVo> subrList = getBroadcastList();
		for (int i=0; i<subrList.size(); i++) {
			MsgClickCountVo vo = subrList.get(i);
			if (vo.isMarkedForDeletion()) {
				if (!StringUtil.isEmpty(FacesUtil.getLoginUserId())) {
					vo.setUpdtUserId(FacesUtil.getLoginUserId());
				}
				int rowsUpdated = getMsgClickCountsDao().update(vo);
				if (rowsUpdated > 0) {
					logger.info("saveBroadcasts() - Broadcast updated: " + vo.getMsgId());
				}
			}
		}
		refresh();
		return TO_SAVED;
	}

	public String cancelEdit() {
		refresh();
		return TO_CANCELED;
	}

	public boolean getAnyBroadcastsMarkedForDeletion() {
		if (isDebugEnabled) {
			logger.debug("getAnyBroadcastsMarkedForDeletion() - Entering...");
		}
		if (broadcasts == null) {
			logger.warn("getAnyBroadcastsMarkedForDeletion() - Broadcast List is null.");
			return false;
		}
		List<MsgClickCountVo> subrList = getBroadcastList();
		for (Iterator<MsgClickCountVo> it=subrList.iterator(); it.hasNext();) {
			MsgClickCountVo vo = it.next();
			if (vo.isMarkedForDeletion()) {
				return true;
			}
		}
		return false;
	}

	void reset() {
		testResult = null;
		actionFailure = null;
	}

	@SuppressWarnings("unchecked")
	private List<MsgClickCountVo> getBroadcastList() {
		if (broadcasts == null) {
			return new ArrayList<MsgClickCountVo>();
		}
		else {
			return (List<MsgClickCountVo>)broadcasts.getWrappedData();
		}
	}

	public MsgClickCountVo getBroadcast() {
		return broadcast;
	}

	public void setBroadcast(MsgClickCountVo subscriber) {
		this.broadcast = subscriber;
	}

	public boolean isEditMode() {
		return editMode;
	}

	public void setEditMode(boolean editMode) {
		this.editMode = editMode;
	}

	public String getTestResult() {
		return testResult;
	}

	public void setTestResult(String testResult) {
		this.testResult = testResult;
	}

	public String getActionFailure() {
		return actionFailure;
	}

	public void setActionFailure(String actionFailure) {
		this.actionFailure = actionFailure;
	}

	public HtmlDataTable getDataTable() {
		return dataTable;
	}

	public void setDataTable(HtmlDataTable dataTable) {
		this.dataTable = dataTable;
	}

	public MsgInboxVo getBroadcastMsg() {
		return broadcastMsg;
	}

	public void setBroadcastMsg(MsgInboxVo broadcastMsg) {
		this.broadcastMsg = broadcastMsg;
	}
}
