package com.twilio;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;

import com.twilio.sdk.verbs.Message;
import com.twilio.sdk.verbs.TwiMLException;
import com.twilio.sdk.verbs.TwiMLResponse;
 
public class TwilioServlet extends HttpServlet {
	Logger logger = Logger.getLogger(TwilioServlet.class);
	
	@Value(value="${app.appRoot}")
	private static String appRoot;
	
	public static String getAppRoot() {
		return appRoot;
	}

	public static void setAppRoot(String urlRoot) {
		TwilioServlet.appRoot = urlRoot;
	}

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
        String fromCity = request.getParameter("FromCity");
        String phoneNumber = request.getParameter("From");
        String body = request.getParameter("Body");
        String zipCode = request.getParameter("FromZip");
        Enumeration<String> parameterNames = request.getParameterNames();
        JSONObject params = new JSONObject();
        while(parameterNames.hasMoreElements()){
        	String paramName = parameterNames.nextElement();
        	params.put("name", paramName);
        	params.put("value", request.getParameter(paramName));
        }
        logger.debug("twilio request: " + params.toString());
        
        //send this sms to the rest api
        HttpClient httpclient = new DefaultHttpClient();
        logger.debug("sending sms to " + appRoot + "rest/sms");
        HttpPost httpPost = new HttpPost(appRoot + "rest/sms");
                                                                                          
        ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
        nameValuePairs.add(new BasicNameValuePair("messageSid", messageSid)); 
        nameValuePairs.add(new BasicNameValuePair("phoneNumber", phoneNumber)); 
        nameValuePairs.add(new BasicNameValuePair("body", body)); 
        nameValuePairs.add(new BasicNameValuePair("zipCode", zipCode)); 
        nameValuePairs.add(new BasicNameValuePair("fromCity", fromCity)); 
        
        httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
        HttpResponse postResponse = httpclient.execute(httpPost);
        int code = postResponse.getStatusLine().getStatusCode();
        if(code >= 400){
        	throw new RuntimeException(code + ": Failed to ingest sms message: " + messageSid);
        }else{
        	logger.debug("sms ingested: " + messageSid);
        }
        TwiMLResponse twiml = new TwiMLResponse();
        String msg = "Got it! - See @ villagepulse.org";
        if(StringUtils.containsIgnoreCase(body, "help")){
        	msg = "Reply with event @ place ex: theft @ 312 Cermak";
        }
        try {
        	Message message = new Message(msg);
            twiml.append(message);
        } catch (TwiMLException e) {
            e.printStackTrace();
        }
 
        response.setContentType("application/xml");
        response.getWriter().print(twiml.toXML());
    }
}