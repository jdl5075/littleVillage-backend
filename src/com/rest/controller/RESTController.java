package com.rest.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class RESTController {

	@RequestMapping(value = "/rest/name", method = RequestMethod.GET)
	public @ResponseBody String getJSON() {

		return "Hello World";

	}

}