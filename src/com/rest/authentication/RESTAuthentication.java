package com.rest.authentication;

import java.io.IOException;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.SignatureException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
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
        SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
        return new BigInteger(130, random).toString(32);
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
		    	node.put("status", HttpServletResponse.SC_ACCEPTED);
		    }else{
		    	response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		    	node.put("status", HttpServletResponse.SC_UNAUTHORIZED);
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
	
	@RequestMapping(value = RESTRoutes.LOGOUT, method = RequestMethod.POST)
	public @ResponseBody void logoutUser() {
		Subject currentUser = SecurityUtils.getSubject();
		currentUser.logout();
	}
	
	@RequestMapping(value = RESTRoutes.CREATE_ACCOUNT, method = RequestMethod.POST)
	public @ResponseBody void createAccount(
			HttpServletRequest request,
			HttpServletResponse response){
		try{
			Connection connection = DatabaseConnection.connect();
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
			
			String encryptedPassword = encrypt(password, salt);
			
			Statement statement = connection.createStatement();
			
		    String sql = "INSERT INTO users (email, password, creation_date, last_accessed, salt) " +
		     "VALUES ('" + emailAddress + "', '" + encryptedPassword + "', '" + dateString + "', '" + dateString + "', '" + salt + "')";
		    statement.executeUpdate(sql);
		}catch(Exception e){
			logger.debug("Failed to create user: " + e.getMessage());
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
	public static String encrypt(char[] data, String key)
			throws java.security.SignatureException {
		String result;
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
			byte[] encodedBytes = Base64.encodeBase64(rawHmac);
			result = new String(encodedBytes);
		} catch (Exception e) {
			throw new SignatureException("Failed to generate HMAC : "
					+ e.getMessage());
		}
		return result;
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
				if(!StringUtils.equals(userPassword, encrypt(credentials.getPassword(), userSalt)) ){
					throw new IncorrectCredentialsException("Incorrect Password");
				}else{
					connection.close();
					return new SimpleAuthenticationInfo(token.getPrincipal(), token.getCredentials(), this.getClass().getSimpleName());
				}
			}else{
				throw new UnknownAccountException("User not found");
			}
		}catch(Exception e){
			logger.debug("Query failed " + e.getMessage());
		}finally{
			try {
				connection.close();
			} catch (SQLException e) {
				logger.debug("Connection close failed: " + e.getMessage());
			}
		}
		return null; //failed to authenticate
	}
}
