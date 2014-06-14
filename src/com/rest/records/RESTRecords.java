 package com.rest.records;

import java.io.IOException;
import java.sql.Connection;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.subject.Subject;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RequestParam;

import com.db.connect.DatabaseConnection;
import com.rest.authentication.AbstractRESTController;
import com.rest.routes.RESTRoutes;

@Controller
public class RESTRecords extends AbstractRESTController{
	Logger logger = Logger.getLogger(RESTRecords.class);
	
	@RequestMapping(value = RESTRoutes.USER, method = RequestMethod.GET)
	public @ResponseBody void getUser(
			HttpServletRequest request,
			HttpServletResponse response,
			@RequestParam(value="ticket", required = false) String ticket,
			@CookieValue("JSESSIONID") String cookieTicket
		){
		JsonNode node = mapper.createObjectNode();
		try{
			response.setContentType("application/json");
			ticket = this.resolveTicket(ticket, cookieTicket);
			Connection connection = DatabaseConnection.connect();
			connection.close();
			
			try {
				response.getWriter().print(node);
			} catch (IOException e) {
				logger.debug("Failed to create JSON response: " + e.getMessage());
			}
		}catch(Exception e){
			logger.debug("Connection close failed: " + e.getMessage());
			try {
				JsonNode error = mapper.readTree(e.getMessage());
				response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
				response.getWriter().print(error);
			} catch (IOException ex) {
				logger.debug("Failed to create JSON response: " + ex.getMessage());
			}
			
		}
	}
}
