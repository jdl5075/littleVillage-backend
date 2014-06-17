package com.rest.authentication;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.SignatureException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.ExcessiveAttemptsException;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.LockedAccountException;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.jdbc.JdbcRealm;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.db.connect.DatabaseConnection;
import com.rest.routes.RESTRoutes;

/**
 * This class defines common routines for generating authentication signatures
 * for AWS requests.
 */
@Controller
public class RESTAuthentication extends JdbcRealm{
	private static final String HMAC_SHA1_ALGORITHM = "HmacSHA1";
	
	Logger logger = Logger.getLogger(RESTAuthentication.class);
	
	final ObjectMapper mapper = new ObjectMapper();
	
	//Add salt
    private static String getSalt() throws NoSuchAlgorithmException
    {
    	byte[] salt = new byte[32];
        SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
        random.nextBytes(salt);
        return Base64.encodeBase64URLSafeString(salt);
    }
    
	@RequestMapping(value = RESTRoutes.LOGIN, method = RequestMethod.POST)
	public @ResponseBody void loginUser(
			HttpServletRequest request,
			HttpServletResponse response) throws SQLException {
		Subject currentUser = SecurityUtils.getSubject();
		
		//both email and password are base64 encoded
		String emailAddress = request.getParameter("emailAddress");
		char[] password = request.getParameter("password").toCharArray();
		String rememberMe = request.getParameter("rememberMe");
		
		ObjectNode node = mapper.createObjectNode();
		
		Session session = currentUser.getSession();
		
		Connection connection = DatabaseConnection.connect();
		if(connection == null){
			throw new AuthenticationException("Database connection error");
		}
		
		response.setContentType("application/json");
		
		//only authenticate against actual users
		if ( !currentUser.isAuthenticated()) { //only do authentication if necessary
		    UsernamePasswordToken token = new UsernamePasswordToken(emailAddress, password);
		    if(StringUtils.equalsIgnoreCase(rememberMe, "true")){
		    	token.setRememberMe(true);
		    }else{
		    	token.setRememberMe(false);
		    }

		    try {
		        currentUser.login(token);
		    }catch ( IncorrectCredentialsException ice ) {
		    	node.put("error", "Credentials Error: " + ice.getMessage());
		    } catch ( LockedAccountException lae ) {
		    	node.put("error", "Locked Account Error: " + lae.getMessage());
		    } catch ( ExcessiveAttemptsException eae ) {
		    	node.put("error", "Excessive Attempts Error: " + eae.getMessage());
		    } catch ( AuthenticationException ae ) {
		    	node.put("error", "Authentication Error: " + ae.getMessage());
		    }
		    //no errors, send back session ticket
		    if(currentUser.isAuthenticated()){
		    	if(session == null){ //get a new session
		    		session = currentUser.getSession();
		    	}
		    	node.put("ticket", session.getId().toString());
		    }else{
		    	response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		    }
		}else{
			node.put("ticket", session.getId().toString());
		}
		try {
			response.getWriter().print(node);
		} catch (IOException e) {
			logger.debug("Failed to create JSON response: " + e.getMessage());
		}finally{
			connection.close();
		}
		
	}
	
	@RequestMapping(value = RESTRoutes.LOGOUT, method = RequestMethod.GET)
	public @ResponseBody void logoutUser(
			HttpServletRequest request,
			HttpServletResponse response
		) {
		Subject currentUser = SecurityUtils.getSubject();
		String message = currentUser.getPrincipal() + " logged out";
		currentUser.logout();
		ObjectNode node = mapper.createObjectNode();
		node.put("status", message);
		try {
			response.getWriter().print(node);
		} catch (IOException e) {
			logger.debug("Failed to create JSON response: " + e.getMessage());
		}
	}
	
	@RequestMapping(value = RESTRoutes.CREATE_ACCOUNT, method = RequestMethod.POST)
	public @ResponseBody void createAccount(
			HttpServletRequest request,
			HttpServletResponse response){
		Connection connection = DatabaseConnection.connect();
		try{
			if(connection == null){
				throw new AuthenticationException("Database connection error");
			}
			
			//both email and password are base64 encoded
			String emailAddress = request.getParameter("emailAddress");
			char[] password = request.getParameter("password").toCharArray();
					
			SimpleDateFormat dateFormatGmt = new SimpleDateFormat("yyyy-MMM-dd HH:mm:ss");
			dateFormatGmt.setTimeZone(TimeZone.getTimeZone("UTC"));
			String dateString = dateFormatGmt.format(new Date());
			
			String salt = getSalt();
			
			byte[] encryptedPassword = encrypt(password, salt);
			
			Statement statement = connection.createStatement();
			
		    response.setContentType("application/json");
		    
		    ObjectNode node = mapper.createObjectNode();
		    
			if(!userExists(emailAddress)){
			
			    String sql = "INSERT INTO users (email, password, creation_date, last_accessed, salt) " +
			     "VALUES ('" + emailAddress + "', '" + new String(encryptedPassword) + "', '" + dateString + "', '" + dateString + "', '" + salt + "')";
			    statement.executeUpdate(sql);
			    
			    node.put("response", "Successfully created an account for " + emailAddress);
			}else{
				node.put("response", emailAddress + " already exists");
			}
		    try {
				response.getWriter().print(node);
			} catch (IOException e) {
				logger.debug("Failed to create JSON response: " + e.getMessage());
			}
		}catch(Exception e){
			logger.debug("Failed to create user: " + e.getMessage());
		}finally{
			try {
				connection.close();
			} catch (SQLException e) {
				logger.debug("Failed to close connection: " + e.getMessage());
			}
		}
	}
	
	@RequestMapping(value= RESTRoutes.GENERATE_PASSWORD_TOKEN, method= RequestMethod.GET)
	public @ResponseBody void generatePasswordToken(
			HttpServletRequest request,
			HttpServletResponse response,
			@RequestParam(value="email", required = true) String emailAddress){
		response.setContentType("application/json");
	    
	    ObjectNode node = mapper.createObjectNode();
	    try {
			node.put("token", generatePasswordResetToken(emailAddress) );
			response.getWriter().print(node);
		} catch (NoSuchAlgorithmException e) {
			logger.debug("Failed to generate password token: " + e.getMessage());
		}catch (IOException e) {
			logger.debug("Failed to create JSON response: " + e.getMessage());
		}	
	}
	
	@RequestMapping(value= RESTRoutes.VALIDATE_PASSWORD_TOKEN, method= RequestMethod.GET)
	public @ResponseBody void validatePasswordToken(
			HttpServletRequest request,
			HttpServletResponse response,
			@RequestParam(value="email", required = true) String email,
			@RequestParam(value="token", required = true) String token){
		
		response.setContentType("application/json");
	    
	    ObjectNode node = mapper.createObjectNode();
	    try {
	    	boolean valid = validatePasswordResetToken(email, token);
	    	node.put("token", token );
			node.put("valid",  valid);
			if(!valid){
				response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			}
			response.getWriter().print(node);
		}catch (IOException e) {
			logger.debug("Failed to create JSON response: " + e.getMessage());
		}	
	}
	
	@RequestMapping(value = RESTRoutes.RESET_PASSWORD, method = RequestMethod.POST)
	public @ResponseBody void resetPassword(
		HttpServletRequest request,
		HttpServletResponse response){
		String emailAddress = request.getParameter("email");
		char[] password = request.getParameter("password").toCharArray();
		String token = request.getParameter("token");
		try {
			if(validatePasswordResetToken(emailAddress, token)){
				resetPassword(emailAddress, password);
				
				response.setContentType("application/json");
			    
			    ObjectNode node = mapper.createObjectNode();
			    try {
			    	node.put("message", "Password for " + emailAddress + " has been reset");
					response.getWriter().print(node);
				}catch (IOException e) {
					logger.debug("Failed to create JSON response: " + e.getMessage());
				}	
			}
		} catch (NoSuchAlgorithmException e) {
			logger.debug("Algoruthm not found: " + e.getMessage());
		} catch (SignatureException e) {
			logger.debug("Failed to genreate signature: " + e.getMessage());
		} catch (SQLException e) {
			logger.debug("Failed to reset password: " + e.getMessage());
		}
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
	
	/**
	 * Computes RFC 2104-compliant HMAC signature. * @param data The data to be
	 * signed.
	 * 
	 * @param key
	 *            The signing key.
	 * @return The Base64-encoded RFC 2104-compliant HMAC signature.
	 * @throws java.security.SignatureException
	 *             when signature generation fails
	 */
	public static byte[] encrypt(char[] data, String key)
			throws java.security.SignatureException {
		String result;
		byte[] encodedBytes;
		try {

			// get an hmac_sha1 key from the raw key bytes
			SecretKeySpec signingKey = new SecretKeySpec(key.getBytes(),
					HMAC_SHA1_ALGORITHM);

			// get an hmac_sha1 Mac instance and initialize with the signing key
			Mac mac = Mac.getInstance(HMAC_SHA1_ALGORITHM);
			mac.init(signingKey);

			// compute the hmac on input data bytes
			byte[] rawHmac = mac.doFinal(new String(data).getBytes("UTF-8"));

			// base64-encode the hmac
			encodedBytes = Base64.encodeBase64(rawHmac);
		} catch (Exception e) {
			throw new SignatureException("Failed to generate HMAC : "
					+ e.getMessage());
		}
		return encodedBytes;
	}

	public String generatePasswordResetToken(String emailAddress) throws NoSuchAlgorithmException {
		Connection connection = DatabaseConnection.connect();
		if(connection == null){
			throw new AuthenticationException("Database connection error");
		}
		
		//create a timestamp that expires in 24 hours
		DateTime expiratonDate = new DateTime(DateTimeZone.UTC).plusDays(1);
		
		//get a fresh salt for this authentication token
		String expiry_salt = getSalt();
		
		Timestamp timestamp = new Timestamp(expiratonDate.getMillis());
		try{
			Statement statement = connection.createStatement();
			String sql = "INSERT INTO password_tokens (email, expiry_salt, expiry_date) VALUES('" + emailAddress + "', '" + expiry_salt + "', '" + timestamp + "');";
//			String sql = "UPDATE password_tokens SET expiry_date='" + timestamp.toString() + "', expiry_salt='" + guid + "' WHERE email='" + emailAddress + "';";
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
	
	public boolean validatePasswordResetToken(String emailAddress, String token){
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
	
	public void resetPassword(String emailAddress, char[] password) 
			throws NoSuchAlgorithmException, SignatureException, SQLException{
		Connection connection = DatabaseConnection.connect();
		if(connection == null){
			throw new AuthenticationException("Database connection error");
		}
		
		try{
			Statement statement = connection.createStatement();
			
			String salt = getSalt();
			
			byte[] encryptedPassword = encrypt(password, salt);
			
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
	
	@Override
	protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
		logger.debug("Authorizing...");
        return new SimpleAuthorizationInfo();
	}

	@Override
	protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
		logger.debug("Authenticating...");
		
		Connection connection = DatabaseConnection.connect();
		if(connection == null){
			throw new AuthenticationException("Database connection error");
		}
		
		try{
			Statement statement = connection.createStatement();
			
			UsernamePasswordToken credentials = (UsernamePasswordToken) token;
			
			if(StringUtils.isBlank( credentials.getUsername()) ){
				throw new AuthenticationException("Username is null - Authentication failed.");
			}
			
			ResultSet userCredentials = statement.executeQuery("select password, salt from users where lower(email) like lower('" + credentials.getUsername() + "')");
			
			boolean userExists = userCredentials.isBeforeFirst();
			
			if(userExists){
				logger.debug("User exists - checking credentials");
				userCredentials.next();
				String userPassword = userCredentials.getString("password");
				String userSalt = userCredentials.getString("salt");
				//encrypt the user's input against those stored in the db
				if(!StringUtils.equals(userPassword, new String(encrypt(credentials.getPassword(), userSalt)) )){
					throw new IncorrectCredentialsException("Incorrect Password");
				}else{
					connection.close();
					return new SimpleAuthenticationInfo(token.getPrincipal(), token.getCredentials(), this.getClass().getSimpleName());
				}
			}else{
				throw new UnknownAccountException("User not found");
			}
		} catch (SQLException e) {
			logger.debug("SQL exception: " + e.getMessage());
		} catch (SignatureException e) {
			logger.debug("Signature exception: " + e.getMessage());
		}finally{
			try {
				connection.close();
			} catch (SQLException e) {
				logger.debug("Connection close failed: " + e.getMessage());
			}
		}
		return null;
	}
}
