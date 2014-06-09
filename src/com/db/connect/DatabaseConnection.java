package com.db.connect;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.apache.log4j.Logger;

public class DatabaseConnection {
	
	static Logger logger = Logger.getLogger(DatabaseConnection.class);
	
	public static Connection connect(){
		try {
			Class.forName("org.postgresql.Driver");
		} catch (ClassNotFoundException e) {
			logger.debug("Postgres JDBC driver not found: " + e.getMessage());
		}
		 
		Connection connection = null;
	
		try {
			connection = DriverManager.getConnection(
					"jdbc:postgresql://127.0.0.1:5432/postgres", "mblum",
					"");
		} catch (SQLException e) {
			logger.debug("Connection failed: " + e.getMessage());
		}
	
		if (connection != null) {
			logger.debug("Connected to database");
		} else {
			System.out.println("Failed to make connection!");
		}
		return connection;
		
	}
}
