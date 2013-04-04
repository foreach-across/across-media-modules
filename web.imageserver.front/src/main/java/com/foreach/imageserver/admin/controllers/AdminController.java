package com.foreach.imageserver.admin.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class AdminController
{
	@RequestMapping(value = "/", method = RequestMethod.GET)
	public final ModelAndView showHomePage()
	{
		return new ModelAndView( "index" );
	}

	@RequestMapping(value = "/login", method = RequestMethod.GET)
	public final ModelAndView showLoginPage()
	{
		return new ModelAndView( "login/login" );
	}

	@RequestMapping(value = "/noaccess", method = RequestMethod.GET)
	public final ModelAndView showNoAccessPage()
	{
		return new ModelAndView( "login/noaccess" );
	}
}
