package com.rest.authentication;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.apache.shiro.authz.UnauthorizedException;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.client.RestTemplate;

public class AbstractRESTControllerTest extends AbstractShiroTest{
	@Autowired
    private RestTemplate restTemplate;
	
	private class mockAbstractRESTController extends AbstractRESTController{}
    
    @Test public void returnsSessionTicket(){
    	//1.  Create a mock authenticated Subject instance for the test to run:
    	Subject mockSubject = mock(Subject.class);
        when(mockSubject.isAuthenticated()).thenReturn(true);
        
        Session mockSession = mock(Session.class);
        when(mockSubject.getSession()).thenReturn(mockSession);
        when(mockSession.getId()).thenReturn("mockSessionId");
        
        //2. Bind the subject to the current thread:
        setSubject(mockSubject);
        
    	//ticket, cookie ticket
    	String ticket = new mockAbstractRESTController().resolveTicket("mockSessionId", "test");
    	
    	assertEquals(ticket, "mockSessionId");
    }
    
    @Test public void returnsCookieTicket(){
    	//1.  Create a mock authenticated Subject instance for the test to run:
    	Subject mockSubject = mock(Subject.class);
        when(mockSubject.isAuthenticated()).thenReturn(true);
        
        Session mockSession = mock(Session.class);
        when(mockSubject.getSession()).thenReturn(mockSession);
        when(mockSession.getId()).thenReturn("mockSessionId");
        
        //2. Bind the subject to the current thread:
        setSubject(mockSubject);
        
    	//ticket, cookie ticket
    	String ticket = new mockAbstractRESTController().resolveTicket("test", "mockSessionId");
    	
    	assertEquals(ticket, "mockSessionId");
    }
    
    @Test public void throwsAnErrorIfNoSession(){
    	//1.  Create a mock authenticated Subject instance for the test to run:
    	Subject mockSubject = mock(Subject.class);
        when(mockSubject.isAuthenticated()).thenReturn(false);
        
        Session mockSession = mock(Session.class);
        when(mockSubject.getSession()).thenReturn(mockSession);
        when(mockSession.getId()).thenReturn(""); //session is destroyed
        
        //2. Bind the subject to the current thread:
        setSubject(mockSubject);
        
        try {
        	//ticket, cookie ticket
        	String ticket = new mockAbstractRESTController().resolveTicket("badTicket", "");
            fail("Fail! Method was expected to throw an exception because a session id is required.");
        } catch (UnauthorizedException e) {
            // expected
        }
    }
}
