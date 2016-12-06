package com.legacytojava.msgui.servlet;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.legacytojava.message.dao.inbox.AttachmentsDao;
import com.legacytojava.message.vo.inbox.AttachmentsVo;
import com.legacytojava.msgui.util.SpringUtil;

/**
 * The File Servlet that serves files from database.
 */
public class FileServlet extends HttpServlet {
	private static final long serialVersionUID = -8129545604805974235L;
	static final Logger logger = Logger.getLogger(FileServlet.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();
	
	private AttachmentsDao attachmentsDao = null;
	
	public void init() throws ServletException {
		ServletContext ctx = getServletContext();
		logger.info("init() - ServerInfo: " + ctx.getServerInfo() + ", Context Path: "
				+ ctx.getContextPath());
		attachmentsDao = (AttachmentsDao) SpringUtil.getWebAppContext(ctx)
				.getBean("attachmentsDao");
	}
	
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

        // Get ID from request
        String id = request.getParameter("id");
        String depth = request.getParameter("depth");
        String seq = request.getParameter("seq");
        logger.info("File Id/Depth/Seq: " + id + "/" + depth + "/" + seq);
        // Check if ID is supplied from the request
        if (id == null || depth == null || seq == null) {
        	logger.error("Missing file ID and/or Depth and/or Seq from request.");
            response.sendRedirect("/FileNotFoundError.jsp");
            return;
        }

        // Lookup AttachmentsVo by id/depth/seq in database.
        long msgId = 0;
        int attchmntDepth = 0;
        int attchmntSeq = 0;
        try {
        	msgId = Long.parseLong(id);
        	attchmntDepth = Integer.parseInt(depth);
        	attchmntSeq = Integer.parseInt(seq);
        }
        catch (NumberFormatException e) {
        	logger.error("Failed to convert file ID or Depth or Seq to numeric values.");
            response.sendRedirect("/FileNotFoundError.jsp");
            return;
        }
		AttachmentsVo fileData = null;
        try {
        	fileData = attachmentsDao.getByPrimaryKey(msgId, attchmntDepth,
					attchmntSeq); // returns null if no result is found.
        }
        catch (Throwable e) {
            logger.error("Throwable caught", e);
            response.sendRedirect("/FileNotFoundError.jsp");
            return;
        }

        // Check if file is actually retrieved from database.
        if (fileData == null) {
            logger.error("Failed to retrieve file from database.");
            response.sendRedirect("/FileNotFoundError.jsp");
            return;
        }
        if (fileData.getAttchmntValue() == null) {
        	logger.warn("Empty attachment, key = " + id +"/" + depth + "/" + seq);
        	return;
        }
        BufferedOutputStream output = null;
        try {
            // Get file content
            ByteArrayInputStream input = new ByteArrayInputStream(fileData.getAttchmntValue());
            int contentLength = input.available();
            // initialize servlet response.
            response.reset();
            response.setContentLength(contentLength);
            response.setContentType(fileData.getAttchmntType());
            response.setHeader("Content-disposition",
                "attachment; filename=\"" + fileData.getAttchmntName() + "\"");
            output = new BufferedOutputStream(response.getOutputStream());
            // Write file contents to response
            while (contentLength-- > 0) {
                output.write(input.read());
            }
            output.flush();
        } 
        catch (IOException e) {
        	logger.error("IOException caught", e);
        	throw e;
        }
        finally {
            // make sure to close stream
            if (output != null) {
                try {
                    output.close();
                }
                catch (IOException e) {
                	// This is a serious error
                	logger.error("IOException caught during output.close()", e);
                    throw e;
                }
            }
        }
    }
}