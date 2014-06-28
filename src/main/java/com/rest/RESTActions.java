 package com.rest;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.message.BasicNameValuePair;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.db.connect.DatabaseConnection;
import com.rest.routes.RESTRoutes;

@Controller
public class RESTActions extends AbstractRESTController{
	Logger logger = Logger.getLogger(RESTActions.class);
	
	@RequestMapping(value = RESTRoutes.GET_CATEGORIES, method = RequestMethod.GET)
	public @ResponseBody Map<String, ArrayList<String>> getCategories(
			@RequestParam(value="type", required=false) String type,
			HttpServletRequest request,
			HttpServletResponse response
		){
		try{
			Connection connection = DatabaseConnection.connect();
			Statement statement = connection.createStatement();
			String query = "SELECT * FROM types;";
			//return every category if type is blank
			if(!StringUtils.isBlank(type)){
				query = "SELECT * FROM types WHERE type = '" + type + "';";
			}
			Map<String, ArrayList<String>> map = new HashMap<String, ArrayList<String>>();
			ResultSet results = statement.executeQuery(query);
			results.next();
			while(!results.isAfterLast()){
				String resultType = results.getString("type");
				String resultCategory = results.getString("category");
				if(map.get(resultType) == null){
					map.put(resultType, new ArrayList<String>());
				}
				map.get(resultType).add(resultCategory);
				results.next();
			}
			connection.close();
			
			return map;
		}catch(Exception e){
			logger.debug("Connection close failed: " + e.getMessage());
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			return new HashMap<String, ArrayList<String>>();	
		}
	}
	
	@RequestMapping(value = RESTRoutes.POST_SMS, method = RequestMethod.POST)
	public @ResponseBody Map<String, String> postSMS(
			HttpServletRequest request,
			HttpServletResponse response
		){
		logger.debug(request.getParameterMap().toString());
		String messageSid = request.getParameter("messageSid");
        String fromCity = request.getParameter("fromCity");
        String phoneNumber = request.getParameter("phoneNumber");
        String body = request.getParameter("body");
        String zipCode = request.getParameter("zipCode");
        
		Map<String, String> map = new HashMap<String, String>();
		map.put("fromCity", fromCity);
		map.put("phoneNumber", phoneNumber);
		map.put("zipCode", zipCode);
		map.put("body", body);
		map.put("messageSid", messageSid);
		try{
			Connection connection = DatabaseConnection.connect();
			Statement statement = connection.createStatement();
			String query = "INSERT INTO texts (messageSid, phoneNumber, fromCity, body, zipCode) VALUES('" + messageSid + "', '" + phoneNumber + "', '" + fromCity + "', '" + body + "', " + zipCode + ");";
			int results = statement.executeUpdate(query);
			if(results == 0){
				throw new IOException("Failed to insert sms: " + map.toString());
			}
			connection.close();
			return map;
		}catch(Exception e){
			logger.debug("Connection close failed: " + e.getMessage());
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			return new HashMap<String, String>();	
		}
	}
}
