 package com.rest;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.codehaus.jackson.node.ObjectNode;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.db.connect.DatabaseConnection;
import com.rest.routes.RESTRoutes;

@Controller
public class RESTRecords extends AbstractRESTController{
	Logger logger = Logger.getLogger(RESTRecords.class);
	
	@RequestMapping(value = "/", method = RequestMethod.GET)
	public @ResponseBody Map<String, String> getRoot(
			HttpServletRequest request,
			HttpServletResponse response
		){
		ObjectNode node = mapper.createObjectNode();
		try{
			Connection connection = DatabaseConnection.connect();
			connection.close();
			
			return new HashMap<String, String>();
		}catch(Exception e){
			logger.debug("Connection close failed: " + e.getMessage());
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			return new HashMap<String, String>();	
		}
	}
	
	@RequestMapping(value = RESTRoutes.USER, method = RequestMethod.GET)
	public @ResponseBody Map<String, String> getUser(
			HttpServletRequest request,
			HttpServletResponse response
		){
		ObjectNode node = mapper.createObjectNode();
		try{
			Connection connection = DatabaseConnection.connect();
			connection.close();
			
			return new HashMap<String, String>();
		}catch(Exception e){
			logger.debug("Connection close failed: " + e.getMessage());
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			return new HashMap<String, String>();	
		}
	}
}
