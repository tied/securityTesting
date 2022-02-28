package com.miniorange.oauth.confluence.servlet;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.servlet.http.*;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.client.utils.URIUtils;
import org.springframework.web.util.HtmlUtils;

import org.apache.commons.lang.StringEscapeUtils;
import com.miniorange.oauth.confluence.MoOAuthSettings;
import com.miniorange.oauth.confluence.MoOAuthManager;

public class MoOAuthAutoLoginServlet extends HttpServlet {

	private static Log LOGGER = LogFactory.getLog(MoOAuthAutoLoginServlet.class);

	private MoOAuthSettings settings;

	public MoOAuthAutoLoginServlet(MoOAuthSettings settings) {
		this.settings = settings;
	}

	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		LOGGER.info("OAuth Authentication Servlet doGet() called...");
		
		LOGGER.debug("fetching header");
		String currentUserIPAddress = request.getHeader("X-FORWARDED-FOR");  
		LOGGER.debug("currentUserIPAddress (header) : "+ currentUserIPAddress);
		if (currentUserIPAddress == null) {  
			currentUserIPAddress = request.getRemoteAddr();  
		} 

		LOGGER.debug("get current user IP Address: "+ currentUserIPAddress);
		
		for (String key : settings.getUserSessionTimeout().keySet()) {
			LOGGER.debug("key : "+ key);
			LOGGER.debug("IP Pattern : "+ Pattern.matches(key, currentUserIPAddress));
			if (Pattern.matches(key, currentUserIPAddress)) {
				LOGGER.debug("Key matched with user IP");
				int cookieValue = Integer.parseInt(settings.getUserSessionTimeout().get(key));
				Cookie sessionCookie = new Cookie("SESSIONCOOKIE", "SessionCookie"); 
				LOGGER.debug("creating SESSIONCOOKIE with cookie value : "+cookieValue);
				
				sessionCookie.setMaxAge(cookieValue);
				sessionCookie.setPath("/");
				response.addCookie(sessionCookie);
				
				Cookie validateSessionCookie = new Cookie("VALIDATESESSIONCOOKIE", "ValidateSessionCookie"); 
				LOGGER.debug("creating validateSessionCookie " + validateSessionCookie.getName());
				validateSessionCookie.setPath("/");
				response.addCookie(validateSessionCookie);

			} 
		}

		/* Parameters For the Authorization Request */
		String CLIENT_ID = settings.getClientID();
		String SCOPE = settings.getScope();
		String RESPONSE_TYPE = "code";
		String REDIRECT_URI = settings.getAutoCallBackUrl();
		LOGGER.debug("App Callback URL : "+ settings.getCustomCallbackParameter());		  
		LOGGER.debug("REDIRECT_URI : "+ REDIRECT_URI);  
		
		Boolean useStateParameter = settings.getUseStateParameter();
		Boolean acrValueCheck = settings.getAcrValue();
		Boolean nonceCheck = settings.getNonceCheck();
		Map<String, String> oauth_request_parameters=settings.getOauth_request_parameters();
		Boolean oauthParameterCheck=!oauth_request_parameters.isEmpty();

		StringBuilder AUTHORIZE_ENDPOINT = new StringBuilder(settings.getAuthorizeEndpoint());

		if(!Objects.equals(AUTHORIZE_ENDPOINT.charAt(AUTHORIZE_ENDPOINT.length() - 1),'&')) {
			if ((AUTHORIZE_ENDPOINT.indexOf("?") > -1)) {
				AUTHORIZE_ENDPOINT.append("&");
			} else {
				AUTHORIZE_ENDPOINT.append("?");
			}
		}

		AUTHORIZE_ENDPOINT.append("client_id=").append(CLIENT_ID).append("&scope=")
				.append(URLEncoder.encode(SCOPE, "UTF-8")).append("&redirect_uri=")
				.append(URLEncoder.encode(REDIRECT_URI, "UTF-8")).append("&response_type=").append(RESPONSE_TYPE).append("&prompt=none");
		
		LOGGER.debug("State Parameter : "+ useStateParameter);
		LOGGER.debug("Acr Value : "+ acrValueCheck);
		LOGGER.debug("Nonce Check : "+ nonceCheck); 

		if ((StringUtils.equalsIgnoreCase(settings.getAppName(),"Custom App")
				|| StringUtils.equalsIgnoreCase(settings.getAppName(), "miniOrange")
				|| StringUtils.equalsIgnoreCase(settings.getAppName(),"OpenID")
				|| StringUtils.equalsIgnoreCase(settings.getAppName(), "OKTA")) 
				|| acrValueCheck || nonceCheck || useStateParameter|| oauthParameterCheck) {
			if (nonceCheck) { 
				LOGGER.debug("send nonce in the request"); 
				passNonceInRequest(request, AUTHORIZE_ENDPOINT); 
			}			 
			if (useStateParameter || StringUtils.equalsIgnoreCase(settings.getAppName(), "OKTA")) { 
				LOGGER.debug("send state parameter in the request"); 
				passStateParameterValue(request, AUTHORIZE_ENDPOINT); 
			}			 
			if (acrValueCheck) { 
				LOGGER.debug("send acr_value in the request"); 
				passAcrValueInRequest(request, AUTHORIZE_ENDPOINT); 
			}
			if(oauthParameterCheck){
				LOGGER.debug("send Oauth request parameters in the request");
				passOauthParametersInRequest(request, AUTHORIZE_ENDPOINT,oauth_request_parameters);
			}
		}

		LOGGER.info("OAuth CODE request generated");
		MoOAuthManager.httpRedirect(response, AUTHORIZE_ENDPOINT.toString());
	}
	
	private void passNonceInRequest(HttpServletRequest request, StringBuilder AUTHORIZE_ENDPOINT) { 
		String nonce = randomAlphaNumeric(10); 
		HttpSession httpSession = request.getSession(); 
		httpSession.setAttribute("nonce", nonce); 
		AUTHORIZE_ENDPOINT.append("&nonce=").append(nonce); 
	} 
	
	private void passAcrValueInRequest(HttpServletRequest request, StringBuilder AUTHORIZE_ENDPOINT) {
		String acrValue = randomAlphaNumeric(10);
		HttpSession httpSession = request.getSession();
		httpSession.setAttribute("acr_values", acrValue);
		AUTHORIZE_ENDPOINT.append("&acr_values=").append(acrValue);
	}
	private void passOauthParametersInRequest(HttpServletRequest request, StringBuilder AUTHORIZE_ENDPOINT, Map<String,String> oauth_request_parameters) {
		HttpSession httpSession = request.getSession();
		for (Map.Entry<String,String> entry : oauth_request_parameters.entrySet())
		{
			httpSession.setAttribute(entry.getKey(), entry.getValue());
			AUTHORIZE_ENDPOINT.append("&"+entry.getKey()+"=").append(entry.getValue());
		}
	}
	private void passStateParameterValue(HttpServletRequest request, StringBuilder AUTHORIZE_ENDPOINT) {
		String stateParameterValue = randomAlphaNumeric(10);
		HttpSession httpSession = request.getSession();
		httpSession.setAttribute("state_attribute_parameter", stateParameterValue);
		AUTHORIZE_ENDPOINT.append("&state=").append(stateParameterValue);
	}

	public void setSettings(MoOAuthSettings settings) {
		this.settings = settings;
	}

	private String randomAlphaNumeric(int count) {
		String ALPHA_NUMERIC_STRING = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
		StringBuilder builder = new StringBuilder();
		while (count-- != 0) {
			int character = (int)(Math.random()*ALPHA_NUMERIC_STRING.length());
			builder.append(ALPHA_NUMERIC_STRING.charAt(character));
		}
		return builder.toString();
	}

}
