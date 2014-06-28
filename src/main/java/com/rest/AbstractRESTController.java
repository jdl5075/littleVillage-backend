package com.rest;

import org.codehaus.jackson.map.ObjectMapper;

public abstract class AbstractRESTController {
	
	final static protected ObjectMapper mapper = new ObjectMapper();
}