/*
 * Copyright (c) 2019 SAP SE or an SAP affiliate company. All rights reserved.
 */
package de.hybris.platform.acceleratorstorefrontcommons.interceptors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;

/**
 */
public interface BeforeViewHandler
{
	/**
	 * Called before the DispatcherServlet renders the view.
	 * Can expose additional model objects to the view via the given ModelAndView.
	 *
	 * @param request current HTTP request
	 * @param response current HTTP response
	 * @param modelAndView the <code>ModelAndView</code> that the handler returned
	 * @throws Exception in case of errors
	 */
	void beforeView(HttpServletRequest request, HttpServletResponse response, ModelAndView modelAndView) throws Exception;
}
