package com.rest.routes;

public class RESTRoutes {
	public static final String REST_PREFIX = "/rest";
	
	public static final String LOGIN = "/login";
	public static final String LOGOUT = "/logout";
	public static final String CREATE_ACCOUNT = "/create";
	
	//password resetting
	public static final String RESET_PASSWORD = "/reset";
	public static final String VALIDATE_PASSWORD_TOKEN = "/validate";
	public static final String GENERATE_PASSWORD_TOKEN = "/forgot";
	
	public static final String USER = REST_PREFIX + "/user";
}
