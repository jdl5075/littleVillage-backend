package com.twilio;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;

import com.twilio.sdk.verbs.Dial;
import com.twilio.sdk.verbs.Message;
import com.twilio.sdk.verbs.TwiMLResponse;
import com.twilio.sdk.verbs.TwiMLException;
import com.twilio.sdk.verbs.Say;
 
public class TwilioServlet extends HttpServlet {
	Logger logger = Logger.getLogger(TwilioServlet.class);
	
	/**
	 *  MessageSid	A 34 character unique identifier for the message. May be used to later retrieve this message from the REST API.
		SmsSid	Same value as MessageSid. Deprecated and included for backward compatibility.
		AccountSid	The 34 character id of the Account this message is associated with.
		From	The phone number that sent this message.
		To	The phone number of the recipient.
		Body	The text body of the message. Up to 1600 characters long.
		NumMedia	The number of media items associated with your message
	 */
    public void service(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String messageSid = request.getParameter("MessageSid");
        String from = request.getParameter("from");
        String to = request.getParameter("to");
        String body = request.getParameter("body");
        JSONObject json = new JSONObject();
        json.put("body", body);
        logger.debug(json.toString());
    	TwiMLResponse twiml = new TwiMLResponse();
        Message message = new Message("Thank You!");
        try {
            twiml.append(message);
        } catch (TwiMLException e) {
            e.printStackTrace();
        }
 
        response.setContentType("application/xml");
        response.getWriter().print(twiml.toXML());
    }
}