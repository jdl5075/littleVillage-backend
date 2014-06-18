package com.db.connect;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;

public class DatabaseConnection {
	
	static Logger logger = Logger.getLogger(DatabaseConnection.class);
	
	@Value(value="${jdbc.url}")
	private static String dbUrl;
	
	public String getDbUrl(){
		return dbUrl;
	}
	
	public void setDbUrl(String dbUrl){
		this.dbUrl = dbUrl;
	}
	 
	@Value(value="${jdbc.username}")
	private static String dbUsername;
	
	public String getDbUsername(){
		return dbUsername;
	}
	
	public void setDbUsername(String dbUsername){
		this.dbUsername = dbUsername;
	}
	 
	@Value(value="${jdbc.password}")
	private static String dbPassword;
	
	public String getDbPassword(){
		return dbPassword;
	}
	
	public void setDbPassword(String dbPassword){
		this.dbPassword = dbPassword;
	}
	
	public static Connection connect(){
		try {
			Class.forName("org.postgresql.Driver");
		} catch (ClassNotFoundException e) {
			logger.debug("Postgres JDBC driver not found: " + e.getMessage());
		}
		 
		Connection connection = null;
	
		try {
			connection = DriverManager.getConnection(dbUrl, dbUsername, dbPassword);
		} catch (SQLException e) {
			logger.debug("Connection failed: " + e.getMessage());
		}
	
		if (connection != null) {
			logger.debug("Connected to database");
		} else {
			logger.debug("Failed to make connection!");
		}
		return connection;
		
	}
}
