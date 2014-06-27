package com.rest.authentication;

import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.UnauthorizedException;
import org.apache.shiro.subject.Subject;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;

public abstract class AbstractRESTController {
	
	final static protected ObjectMapper mapper = new ObjectMapper();
	
	public String resolveTicket(String ticket, String cookieTicket) throws UnauthorizedException{
		Subject currentUser = SecurityUtils.getSubject();
		String sessionTicket = (String) currentUser.getSession().getId();
		
		if(!StringUtils.isBlank(sessionTicket) && 
				(StringUtils.equalsIgnoreCase(ticket, sessionTicket) ||
				StringUtils.equalsIgnoreCase(cookieTicket, sessionTicket)) ){
			return sessionTicket;
		}else{
			ObjectNode node = mapper.createObjectNode();
			node.put("message", "Error resolving ticket");
			node.put("ticket", ticket);
			node.put("cookie", cookieTicket);
			node.put("session", sessionTicket);
			throw new UnauthorizedException(node.toString());
		}
	}
}
