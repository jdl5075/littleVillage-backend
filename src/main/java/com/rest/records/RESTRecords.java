 package com.rest.records;

import java.sql.Connection;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.codehaus.jackson.node.ObjectNode;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.db.connect.DatabaseConnection;
import com.rest.authentication.AbstractRESTController;
import com.rest.routes.RESTRoutes;

@Controller
public class RESTRecords extends AbstractRESTController{
	Logger logger = Logger.getLogger(RESTRecords.class);
	
	@RequestMapping(value = RESTRoutes.USER, method = RequestMethod.GET)
	public @ResponseBody ObjectNode getUser(
			HttpServletRequest request,
			HttpServletResponse response,
			@RequestParam(value="ticket", required = false) String ticket,
			@CookieValue("JSESSIONID") String cookieTicket
		){
		ObjectNode node = mapper.createObjectNode();
		try{
			ticket = this.resolveTicket(ticket, cookieTicket);
			Connection connection = DatabaseConnection.connect();
			connection.close();
			
			return node;
		}catch(Exception e){
			logger.debug("Connection close failed: " + e.getMessage());
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			return node;	
		}
	}
}
