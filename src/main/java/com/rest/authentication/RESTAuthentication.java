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
	final ObjectMapper mapper = new ObjectMapper();
	Logger logger = Logger.getLogger(RESTAuthentication.class);
	
	@RequestMapping(value = RESTRoutes.LOGIN, method = RequestMethod.POST)
	public @ResponseBody ObjectNode loginUser(
			HttpServletRequest request,
			HttpServletResponse response) throws SQLException {
		Subject currentUser = SecurityUtils.getSubject();
		
		//both email and password are base64 encoded
		String emailAddress = request.getParameter("emailAddress");
		char[] password = request.getParameter("password").toCharArray();
		String rememberMe = request.getParameter("rememberMe");
		
		ObjectNode node = mapper.createObjectNode();
		
		Session session = currentUser.getSession();
		
		currentUser.logout(); //logout on a login attempt
		
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
		
		return node;
	}
	
	@RequestMapping(value = RESTRoutes.LOGOUT, method = RequestMethod.GET)
	public @ResponseBody ObjectNode logoutUser(
			HttpServletRequest request,
			HttpServletResponse response
		) {
		Subject currentUser = SecurityUtils.getSubject();
		String message = currentUser.getPrincipal() + " logged out";
		currentUser.logout();
		ObjectNode node = mapper.createObjectNode();
		node.put("message", message);
		return node;
	}
	
	@RequestMapping(value = RESTRoutes.CREATE_ACCOUNT, method = RequestMethod.POST)
	public @ResponseBody ObjectNode createAccount(
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
			
			String salt = EncryptionUtils.getSalt();
			
			byte[] encryptedPassword = EncryptionUtils.encrypt(password, salt);
			
			return AuthenticationDAO.createAccount(emailAddress, encryptedPassword, dateString, salt);
		}catch(Exception e){
			logger.debug("Failed to create user: " + e.getMessage());
		}finally{
			try {
				connection.close();
			} catch (SQLException e) {
				logger.debug("Failed to close connection: " + e.getMessage());
			}
		}
		return null;
	}
	
	@RequestMapping(value= RESTRoutes.GENERATE_PASSWORD_TOKEN, method= RequestMethod.GET)
	public @ResponseBody ObjectNode generatePasswordToken(
			HttpServletRequest request,
			HttpServletResponse response,
			@RequestParam(value="email", required = true) String emailAddress){
	    ObjectNode node = mapper.createObjectNode();
	    try {
			node.put("token", AuthenticationDAO.generatePasswordResetToken(emailAddress) );
			return node;
		} catch (NoSuchAlgorithmException e) {
			logger.debug("Failed to generate password token: " + e.getMessage());
		}
	    return null;
	}
	
	@RequestMapping(value= RESTRoutes.VALIDATE_PASSWORD_TOKEN, method= RequestMethod.GET)
	public @ResponseBody ObjectNode validatePasswordToken(
			HttpServletRequest request,
			HttpServletResponse response,
			@RequestParam(value="email", required = true) String email,
			@RequestParam(value="token", required = true) String token){
		
		response.setContentType("application/json");
	    
	    ObjectNode node = mapper.createObjectNode();
	    boolean valid = AuthenticationDAO.validatePasswordResetToken(email, token);
    	node.put("token", token );
		node.put("valid",  valid);
		if(!valid){
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		}
		return node;
	}
	
	@RequestMapping(value = RESTRoutes.RESET_PASSWORD, method = RequestMethod.POST)
	public @ResponseBody ObjectNode resetPassword(
		HttpServletRequest request,
		HttpServletResponse response){
		String emailAddress = request.getParameter("email");
		char[] password = request.getParameter("password").toCharArray();
		String token = request.getParameter("token");
		try {
			if(AuthenticationDAO.validatePasswordResetToken(emailAddress, token)){
				AuthenticationDAO.resetPassword(emailAddress, password);
			    
			    ObjectNode node = mapper.createObjectNode();
			    node.put("message", "Password for " + emailAddress + " has been reset");
				return node;
			}
		} catch (NoSuchAlgorithmException e) {
			logger.debug("Algoruthm not found: " + e.getMessage());
		} catch (SignatureException e) {
			logger.debug("Failed to genreate signature: " + e.getMessage());
		} catch (SQLException e) {
			logger.debug("Failed to reset password: " + e.getMessage());
		}
		return null;
	}
	
	@Override
	protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        return new SimpleAuthorizationInfo();
	}

	@Override
	protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
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
				if(!StringUtils.equals(userPassword, new String(EncryptionUtils.encrypt(credentials.getPassword(), userSalt)) )){
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
