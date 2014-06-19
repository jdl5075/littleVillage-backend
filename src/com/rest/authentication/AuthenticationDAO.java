package com.rest.authentication;

import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.shiro.authc.AuthenticationException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import com.db.connect.DatabaseConnection;

public class AuthenticationDAO {
	
	private static final String HMAC_SHA1_ALGORITHM = "HmacSHA1";
	
	static Logger logger = Logger.getLogger(AuthenticationDAO.class);
	
	public static ObjectNode createAccount(String emailAddress, byte[] encryptedPassword, String dateString, String salt) throws SQLException{
		final ObjectMapper mapper = new ObjectMapper();
		Connection connection = DatabaseConnection.connect();
			if(connection == null){
				throw new AuthenticationException("Database connection error");
			}
		Statement statement = connection.createStatement();
		
	    ObjectNode node = mapper.createObjectNode();
	    
		if(!userExists(emailAddress)){
		
		    String sql = "INSERT INTO users (email, password, creation_date, last_accessed, salt) " +
		     "VALUES ('" + emailAddress + "', '" + new String(encryptedPassword) + "', '" + dateString + "', '" + dateString + "', '" + salt + "')";
		    statement.executeUpdate(sql);

		    node.put("response", "Successfully created an account for " + emailAddress);
		}else{
			node.put("response", emailAddress + " already exists");
		}
	    return node;
	}
	
	public static boolean userExists(String emailAddress) throws SQLException{
		Connection connection = DatabaseConnection.connect();
		if(connection == null){
			throw new AuthenticationException("Database connection error");
		}
		
		Statement statement = connection.createStatement();
		
		if(StringUtils.isBlank(emailAddress) ){
			throw new AuthenticationException("Username is null - Authentication failed.");
		}
		
		ResultSet userCredentials = statement.executeQuery("select password, salt from users where lower(email) like lower('" + emailAddress + "')");
		
		boolean userExists = userCredentials.isBeforeFirst();
		connection.close();
		return userExists;
	}
	
	public static String generatePasswordResetToken(String emailAddress) throws NoSuchAlgorithmException {
		Connection connection = DatabaseConnection.connect();
		if(connection == null){
			throw new AuthenticationException("Database connection error");
		}
		
		//create a timestamp that expires in 24 hours
		DateTime expiratonDate = new DateTime(DateTimeZone.UTC).plusDays(1);
		
		//get a fresh salt for this authentication token
		String expiry_salt = EncryptionUtils.getSalt();
		
		Timestamp timestamp = new Timestamp(expiratonDate.getMillis());
		try{
			Statement statement = connection.createStatement();
			
			String sql = "SELECT expiry_date, expiry_salt FROM password_tokens WHERE email='" + emailAddress + "';";
			
			ResultSet tokenCredentials = statement.executeQuery(sql);
			
			boolean tokenExists = tokenCredentials.isBeforeFirst();
			
			if(tokenExists){
				sql = "UPDATE password_tokens SET expiry_salt='" + expiry_salt + "', expiry_date='" + timestamp + "' WHERE email='" + emailAddress + "';";
			}else{
				sql = "INSERT INTO password_tokens (email, expiry_salt, expiry_date) VALUES('" + emailAddress + "', '" + expiry_salt + "', '" + timestamp + "');";
			}
			statement.executeUpdate(sql);
		}catch (Exception e) {
			logger.debug("Failed to generate password reset token: " + e.getMessage());
		}finally{
			try {
				connection.close();
			} catch (SQLException e) {
				logger.debug("Failed to close db connection: " + e.getMessage());
			}
		}
		return expiry_salt;
	}
	
	public static boolean validatePasswordResetToken(String emailAddress, String token){
		Connection connection = DatabaseConnection.connect();
		if(connection == null){
			throw new AuthenticationException("Database connection error");
		}
		
		try{
			Statement statement = connection.createStatement();
			String sql = "SELECT expiry_date, expiry_salt FROM password_tokens WHERE email='" + emailAddress + "';";
			
			ResultSet tokenCredentials = statement.executeQuery(sql);
			
			boolean tokenExists = tokenCredentials.isBeforeFirst();
			
			if(tokenExists){
				tokenCredentials.next();
				Timestamp dt = tokenCredentials.getTimestamp("expiry_date");
				
				DateTime now = new DateTime(DateTimeZone.UTC);
				
				//convert to joda datetime
				DateTime expiry_date = new DateTime(dt.getTime(), DateTimeZone.UTC);
				
				String expiry_salt = tokenCredentials.getString("expiry_salt");
				
				//verify the hash and ensure its inside the 24-hr window
				return StringUtils.equals(expiry_salt, token) &&
						now.isBefore(expiry_date);
			}
		}catch (SQLException e) {
			logger.debug("Failed to generate password reset token: " + e.getMessage());
		}finally{
			try {
				connection.close();
			} catch (SQLException e) {
				logger.debug("Failed to close db connection: " + e.getMessage());
			}
		}
		return false;
	}
	
	public static void resetPassword(String emailAddress, char[] password) 
			throws NoSuchAlgorithmException, SignatureException, SQLException{
		Connection connection = DatabaseConnection.connect();
		if(connection == null){
			throw new AuthenticationException("Database connection error");
		}
		
		try{
			Statement statement = connection.createStatement();
			
			String salt = EncryptionUtils.getSalt();
			
			byte[] encryptedPassword = EncryptionUtils.encrypt(password, salt);
			
			String sql = "UPDATE users SET password='" + new String(encryptedPassword) + "', salt='" + salt + "' WHERE email='" + emailAddress + "';";
			statement.executeUpdate(sql);
			
			sql = "DELETE FROM password_tokens WHERE email = '" + emailAddress + "';";
			statement.executeUpdate(sql);
		}catch (SQLException e) {
			logger.debug("Failed to reset password: " + e.getMessage());
		}finally{
			connection.close();
		}
	}
}
