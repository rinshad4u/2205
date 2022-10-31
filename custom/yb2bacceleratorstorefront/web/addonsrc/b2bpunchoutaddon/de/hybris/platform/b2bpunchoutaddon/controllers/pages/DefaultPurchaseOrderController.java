/*
 * Copyright (c) 2019 SAP SE or an SAP affiliate company. All rights reserved.
 */
package de.hybris.platform.b2bpunchoutaddon.controllers.pages;

import de.hybris.platform.b2b.punchout.PunchOutException;
import de.hybris.platform.b2b.punchout.PunchOutResponseCode;
import de.hybris.platform.b2b.punchout.PunchOutUtils;
import de.hybris.platform.b2b.punchout.security.PunchOutUserAuthenticationStrategy;
import de.hybris.platform.b2b.punchout.services.CXMLBuilder;
import de.hybris.platform.b2b.punchout.services.PunchOutService;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.cxml.CXML;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;


/**
 * Purchase order controller default implementation.
 * @deprecated Please use #de.hybris.platform.b2bpunchoutocc.controllers instead
 */
@Deprecated (since="2205", forRemoval=true)
@Component
public class DefaultPurchaseOrderController implements PunchOutController
{
	private static final Logger LOG = LoggerFactory.getLogger(DefaultPurchaseOrderController.class);

	@Resource(name = "punchOutService")
	private PunchOutService punchOutService;

	@Resource(name = "punchOutUserAuthenticationStrategy")
	private PunchOutUserAuthenticationStrategy punchOutUserAuthenticationStrategy;

	/**
	 * Handles a Order Request from the Punch Out Provider.
	 *
	 * @param requestBody
	 *           The cXML containing the order to be processed.
	 * @param request
	 *           The servlet request.
	 * @param response
	 *           The servlet response.
	 * @return A cXML with the Order Response, containing the status of the processing of the order.
	 */
	@PostMapping(value = "/punchout/cxml/order")
	@ResponseBody
	public CXML handlePunchOutPurchaseOrderRequest(@RequestBody final CXML requestBody, final HttpServletRequest request,
			final HttpServletResponse response)
	{
		CXML cxml = null;
		final String identity = punchOutService.retrieveIdentity(requestBody);
		if (LOG.isDebugEnabled()) {
			LOG.debug("CXML received:{}",PunchOutUtils.marshallFromBeanTree(requestBody));
		}
		LOG.info("Order Identity: {}", identity);

		// if user exists
		if (identity != null)
		{
			punchOutUserAuthenticationStrategy.authenticate(identity, request, response);

			cxml = punchOutService.processPurchaseOrderRequest(requestBody);
		}
		else
		{
			final String message = "Unable to find user " + identity;
			LOG.error(message);
			cxml = CXMLBuilder.newInstance().withResponseCode(PunchOutResponseCode.ERROR_CODE_AUTH_FAILED)
					.withResponseMessage(message).create();
		}
		if (LOG.isDebugEnabled()) {
			LOG.debug("CXML output:{}" , PunchOutUtils.marshallFromBeanTree(cxml));
		}
		return cxml;
	}

	@ExceptionHandler
	@ResponseBody
	public CXML handleException(final Exception exc)
	{
		LOG.error("Could not process PunchOut Order Request", exc);

		CXML response = null;

		if (exc instanceof PunchOutException)
		{
			final PunchOutException punchoutException = (PunchOutException) exc;
			response = CXMLBuilder.newInstance().withResponseCode(punchoutException.getErrorCode())
					.withResponseMessage(PunchOutException.PUNCHOUT_EXCEPTION_MESSAGE).create();
		}
		else
		{
			response = CXMLBuilder.newInstance().withResponseCode(PunchOutResponseCode.INTERNAL_SERVER_ERROR)
					.withResponseMessage(PunchOutException.PUNCHOUT_EXCEPTION_MESSAGE).create();
		}

		return response;
	}
}
