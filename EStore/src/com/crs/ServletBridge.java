package com.crs;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import atg.nucleus.servlet.ServletService;
import atg.servlet.DynamoHttpServletRequest;
import atg.servlet.DynamoHttpServletResponse;
import atg.servlet.GenericServletService;
import atg.servlet.ServletUtil;

// TODO: Auto-generated Javadoc
/**
 * The Class ServletBridge.
 */
public class ServletBridge extends GenericServletService {

	/**
	 * The destination ATG Servlet Component which should get the request passed
	 * in to it.
	 */
	private ServletService mDestinationServlet;

	/**
	 * This Servlet's service method gets the ATG Dynamo Request and Response
	 * objects and pass through to the ATG Servlet component this Servlet is
	 * configured to point to.
	 * 
	 * @param pServletRequest
	 *            the servlet request
	 * @param pServletResponse
	 *            the servlet response
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @throws ServletException
	 *             the servlet exception
	 * @see atg.servlet.GenericServletService#service(javax.servlet.ServletRequest,
	 *      javax.servlet.ServletResponse)
	 */
	@Override
	public void service(final ServletRequest pServletRequest,
			final ServletResponse pServletResponse) throws IOException,
			ServletException {
		if (isLoggingDebug()) {
			logDebug("ServletBridge.service:" + "starting method...");
		}
		if (isLoggingDebug() && (pServletRequest instanceof HttpServletRequest)) {
			final HttpServletRequest httpRequest = (HttpServletRequest) pServletRequest;
			if (isLoggingDebug()) {
				logDebug("ServletBridge.service:" + "Request data:: URI:"
						+ httpRequest.getRequestURI() + " URL:"
						+ httpRequest.getRequestURL() + " queryString:"
						+ httpRequest.getQueryString());
			}
		}
		final DynamoHttpServletRequest dynRequest = ServletUtil
				.getDynamoRequest(getServletContext(), pServletRequest,
						pServletResponse);
		final DynamoHttpServletResponse dynResponse = ServletUtil
				.getDynamoResponse(dynRequest, pServletResponse);
		if (isLoggingDebug()) {
			logDebug("ServletBridge.service:"
					+ "passing request to destination servlet: "
					+ getDestinationServlet().getAbsoluteName());
		}
		getDestinationServlet().service(dynRequest, dynResponse);
		if (isLoggingDebug()) {
			logDebug("ServletBridge.service:" + "ending method.");
		}
	}

	/**
	 * Gets the destination ATG Servlet Component.
	 * 
	 * @return the destination servlet
	 */
	public ServletService getDestinationServlet() {
		return this.mDestinationServlet;
	}

	/**
	 * Sets the destination servlet.
	 * 
	 * @param pDestinationServlet
	 *            the new destination servlet
	 */
	public void setDestinationServlet(final ServletService pDestinationServlet) {
		this.mDestinationServlet = pDestinationServlet;
	}
}
