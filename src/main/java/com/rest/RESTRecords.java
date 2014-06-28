 package com.rest;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.db.connect.DatabaseConnection;
import com.rest.routes.RESTRoutes;

@Controller
public class RESTRecords extends AbstractRESTController{
	Logger logger = Logger.getLogger(RESTRecords.class);
	
	@RequestMapping(value = RESTRoutes.GET_CATEGORIES, method = RequestMethod.GET)
	public @ResponseBody Map<String, ArrayList<String>> getUser(
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
}
