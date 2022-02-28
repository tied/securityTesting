package com.miniorange.oauth.confluence.servlet;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.servlet.http.*;

import com.miniorange.oauth.confluence.MoOAuthPluginHandler;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.client.utils.URIUtils;
import org.springframework.web.util.HtmlUtils;

import org.apache.commons.lang.StringEscapeUtils;
import com.miniorange.oauth.confluence.MoOAuthSettings;
import com.miniorange.oauth.confluence.MoOAuthManager;

public class MoOAuthLoginServlet extends HttpServlet {

	private static Log LOGGER = LogFactory.getLog(MoOAuthLoginServlet.class);

	private MoOAuthSettings settings;

	public MoOAuthLoginServlet(MoOAuthSettings settings) {
		this.settings = settings;
	}

	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		LOGGER.info("OAuth Authentication Servlet doGet() called...");
		String return_to = request.getParameter("return_to");
		
		if (StringUtils.equals(return_to, "testoauthconfiguration")) {
			settings.setIsTestIDPConfigurationClicked(true);
		} else {
			settings.setIsTestIDPConfigurationClicked(false);
		}

		if (StringUtils.equals(return_to, "verifycredentials")) {
			settings.setIsVerifyCredentialsClicked(true);
		} else {
			settings.setIsVerifyCredentialsClicked(false);
		}
		
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
		
		/* Test Regex */

		return_to = sanitizeReturnTo(return_to);

		LOGGER.debug("returnTo : "+ return_to);
		if (StringUtils.contains(return_to, "testregex")) {
			LOGGER.info("testing regex pattern...");
			StringBuilder output = new StringBuilder("<div style='display:none'>");
			String regexp = request.getParameter("regexp");
			regexp = StringEscapeUtils.escapeJavaScript(regexp);
			LOGGER.debug("regex pattern = " + regexp);
			LOGGER.debug("Check if regex is valid : "+ isRegexValid(regexp));
			String message = "TEST REGEX....!";
			message = HtmlUtils.htmlEscape(message);

			output.append(message);
			output.append("</div>");
			output.append(
					"<div style='color: #3c763d;background-color: #dff0d8; padding:2%;margin-bottom:20px;text-align:center; border:1px solid #AEDB9A; font-size:18pt;'>Test Regex Pattern</div>");
			output.append(
					"<div id=\"testregex\"> <input type=\"text\"  id=\"attrvalue\" name=\"attrvalue\" placeholder=\"Enter Attribute Value\" class=\"text long-field\"  style=\"width:250px;padding:10px; margin-left:20px\"/>&nbsp;&nbsp;&nbsp;"
							+ "<label id=\"result\"/><label id=\"error\"/></div><br><br>");
			output.append("<div style=\"margin:3%;display:block;\"><input id =\"testregex-button\" style=\"padding:1%;"
					+ "width:150px;background: #0091CD none repeat scroll 0% 0%;cursor: pointer;font-size:15px;"
					+ "border-width: 1px;border-style: solid;border-radius: 3px;white-space: nowrap;"
					+ "box-sizing:border-box;border-color: #0073AA;box-shadow:0px 1px 0px rgba(120,200,230,0.6) inset;"
					+ "color: #FFF;\" type=\"button\" value=\"Test Regex\"></div>");
			output.append("<script>" + "document.querySelector(\"#attrvalue\").addEventListener(\"keyup\",function(event) {"
					+ "if(event.keyCode === 13) {"
					+ "document.querySelector(\"#testregex-button\").click();"
					+ "}"
					+ "});"
					+ "document.querySelector(\"#testregex-button\").onclick = function() {"
					+ "var attrValue = document.querySelector(\"#attrvalue\").value;" + "var regexp ='" + regexp + "';"
					+ "console.log('original: '+regexp);" + "if(" + isRegexValid(regexp) + ")" + "{"
					+ "console.log('Escaped: '+regexp);" + "var regExp = new RegExp(regexp, 'g');"
					+ "var result = regExp.exec(attrValue);" + " var username = '';" + "if (result && result!=\"\") {"
					+ "if(result.length>1){" + "var length = result.length;"
					+ "for(var count = 1; count < length; count++){" + "console.log('Multiple Groups'); "
					+ "console.log(result[count]);" + "username += result[count];" + "}" + "}" + "else{"
					+ "console.log('No groups '+result);" + "username = result[0];" + "}"
					+ "console.log(\"Result...:\" + username);"
					+ "document.querySelector(\"#result\").innerHTML = 'Success : Pattern Found';}"

					+ "else { document.querySelector(\"#result\").innerHTML = \"Error : No Pattern Found.\"; }" + "}"
					+ " else { document.querySelector(\"#error\").innerHTML = \"Invalid Regex Expression.\";}"

					+ "}</script>");
			response.setContentType("text/html");
			response.getOutputStream().write(output.toString().getBytes(StandardCharsets.UTF_8));
			return;
		}

		if(StringUtils.contains(return_to, "testgroupregex")){
			try {
				showTestGroupRegexWindow(request, response);
			}catch (Exception e){
				LOGGER.error(e);
			}
			return;
		}

		if (!(settings.getIsTestIDPConfigurationClicked() && !(settings.getIsVerifyCredentialsClicked())
				&& StringUtils.isNotEmpty(request.getParameter("return_to")))){
			// Setting cookies to identity the RelayState URL.
			//settings.setReturnUrl(request.getParameter("return_to"));
			Cookie relayStateCookie = new Cookie("mo.confluence-oauth.RELAY_STATE", return_to);
			relayStateCookie.setPath("/");
			response.addCookie(relayStateCookie);
		}	

		/* Parameters For the Authorization Request */
		String CLIENT_ID = settings.getClientID();
		String SCOPE = settings.getScope();
		String RESPONSE_TYPE = "code";
		String REDIRECT_URI = settings.getCallBackUrl() + settings.getCustomCallbackParameter();
		LOGGER.debug("App Callback URL : "+ settings.getCustomCallbackParameter());		  
		LOGGER.debug("REDIRECT_URI : "+ REDIRECT_URI);  
		
		Boolean useStateParameter = settings.getUseStateParameter();
		Boolean acrValueCheck = settings.getAcrValue();
		Boolean nonceCheck = settings.getNonceCheck();
		Map<String, String> oauth_request_parameters=settings.getOauth_request_parameters();
		Boolean oauthParameterCheck=!oauth_request_parameters.isEmpty();

		StringBuilder AUTHORIZE_ENDPOINT = new StringBuilder(settings.getAuthorizeEndpoint());

		/*
		 * if(StringUtils.equals(settings.getAppName(),
		 * MoOAuthPluginConstants.AZURE))
		 * AUTHORIZE_ENDPOINT.append(settings.getDirectoryId()).append(
		 * "/oauth2/authorize");
		 */

		if(!Objects.equals(AUTHORIZE_ENDPOINT.charAt(AUTHORIZE_ENDPOINT.length() - 1),'&')) {
			if ((AUTHORIZE_ENDPOINT.indexOf("?") > -1)) {
				AUTHORIZE_ENDPOINT.append("&");
			} else {
				AUTHORIZE_ENDPOINT.append("?");
			}
		}

		AUTHORIZE_ENDPOINT.append("client_id=").append(CLIENT_ID).append("&scope=")
				.append(URLEncoder.encode(SCOPE, "UTF-8")).append("&redirect_uri=")
				.append(URLEncoder.encode(REDIRECT_URI, "UTF-8")).append("&response_type=").append(RESPONSE_TYPE);
		
		LOGGER.debug("State Parameter : "+ useStateParameter);
		LOGGER.debug("Acr Value : "+ acrValueCheck);
		LOGGER.debug("Nonce Check : "+ nonceCheck); 

		if ((StringUtils.equalsIgnoreCase(settings.getAppName(),"Custom App")
				|| StringUtils.equalsIgnoreCase(settings.getAppName(), "miniOrange")
				|| StringUtils.equalsIgnoreCase(settings.getAppName(),"OpenID")
				|| StringUtils.equalsIgnoreCase(settings.getAppName(),"miniOrange")
				|| StringUtils.equalsIgnoreCase(settings.getAppName(), "OKTA")) 
				|| acrValueCheck || nonceCheck || useStateParameter|| oauthParameterCheck) {
			if (nonceCheck) { 
				LOGGER.debug("send nonce in the request"); 
				passNonceInRequest(request, AUTHORIZE_ENDPOINT); 
			}			 
			if (useStateParameter || (StringUtils.equalsIgnoreCase(settings.getAppName(), "OKTA")
					|| StringUtils.equalsIgnoreCase(settings.getAppName(), "miniOrange"))) {
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

//		if (StringUtils.equalsIgnoreCase(settings.getAppName(), "ADFS")) {
//			AUTHORIZE_ENDPOINT.append("&resource=" + this.settings.getBaseUrl());
//		}

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

	private void showTestGroupRegexWindow(HttpServletRequest request, HttpServletResponse response) throws IOException{
		StringBuilder output = new StringBuilder("<div style='display:none'>");
		String regexp = request.getParameter("regexp");
		String regexg = request.getParameter("regexg");
		String groupName = request.getParameter("groupName");
		String result=StringUtils.EMPTY;
		regexp = org.apache.commons.lang.StringEscapeUtils.escapeJavaScript(regexp);
		regexg= org.apache.commons.lang.StringEscapeUtils.escapeJavaScript(regexg);
		groupName = org.apache.commons.lang.StringEscapeUtils.escapeJavaScript(groupName);

		if (StringUtils.isNotEmpty(regexg)&&StringUtils.isNotEmpty(regexp)&&StringUtils.isNotEmpty(groupName)) {
			result = getGroupnameFromRegexMethod(regexp, regexg, groupName);
		}
		output.append("</div>");
		if (StringUtils.isBlank(regexp)||StringUtils.isBlank(regexg)||StringUtils.isBlank(groupName)){
			output.append("<div style=\"color: #a94442;background-color: #f2dede;padding: 15px;"
					+ "margin-bottom: 20px;text-align:center;border:1px solid #E6B3B2;font-size:18pt;\">TEST "
					+ "FAILED</div><div style=\"color: #a94442;font-size:14pt; margin-bottom:20px;\">Error message: <br> ");
			if (StringUtils.isBlank(regexp)) {
				output.append("<li>Regular Expression field can't left blank</li><br>");
			}
			if (StringUtils.isBlank(regexg)){
				output.append("<li>Replace with field can't left blank</li><br>");
			}
			if (StringUtils.isBlank(groupName)){
				output.append("<li>Group Name field can't left blank</li><br>");
			}
			output.append("</div>");
		}
		else  if (StringUtils.isNotEmpty(result)&&StringUtils.isNotBlank(groupName)) {
			output.append("<div style='color: #3c763d;background-color: #dff0d8; padding:2%;margin-bottom:20px;text-align:center; border:1px solid #AEDB9A; font-size:18pt;'>" +
					"Test Regex Result</div>");
			output.append("<table border=\"1\" style=\"width: 100%;\">\n" +
					"     <tbody><tr>\n" +
					"        <th>Actual Value</th>\n" +
					"        <th>Transformed Value</th>\n" +
					"    </tr>\n" +
					"    <tr>\n" +
					"        <td align=\"center\">"+groupName+"</td>\n" +
					"        <td align=\"center\">"+result+"</td>\n" +
					"    </tr>    \n" +
					"    </tbody></table>");
		}
		else if (StringUtils.isEmpty(result)&&StringUtils.isNotBlank(groupName))
		{
			output.append("<div style=\"color: #a94442;background-color: #f2dede;padding: 15px;"
					+ "margin-bottom: 20px;text-align:center;border:1px solid #E6B3B2;font-size:18pt;\">TEST "
					+ "FAILED</div><div style=\"color: #a94442;font-size:14pt; margin-bottom:20px;\">Error message: <br> ");
			output.append("<li>Regex not valid for the group Name.</li><br>");
			output.append("</div>");
		}

		output.append("<div style=\"margin:3%;display:block;text-align:center;\"><input style=\"padding:1%;"
				+ "width:100px;background: #0091CD none repeat scroll 0% 0%;cursor: pointer;font-size:15px;"
				+ "border-width: 1px;border-style: solid;border-radius: 3px;white-space: nowrap;"
				+ "box-sizing:border-box;border-color: #0073AA;box-shadow:0px 1px 0px rgba(120,200,230,0.6) inset;"
				+ "color: #FFF;\" type=\"button\" value=\"Done\" onClick=\"self.close();\"></div>");
		response.setContentType("text/html");
		response.getOutputStream().write(output.toString().getBytes("UTF-8"));

	}

	public static String getGroupnameFromRegexMethod(String groupRegexPattern,String regexGroups,String groupName){
		LOGGER.debug("Applying regex pattern on Groupname : "+groupName);
		Pattern pattern = Pattern.compile(StringUtils.trimToEmpty(groupRegexPattern));
		Matcher matcher = pattern.matcher(groupName);
		if (matcher.find()) {
			groupName = StringUtils.EMPTY;
			LOGGER.debug("Matched Groups "+matcher.groupCount());
			if (matcher.groupCount() > 0) {
				groupName=MoOAuthPluginHandler.getGroupNameFromRegex(matcher,StringUtils.trimToEmpty(regexGroups));
			} else {
				groupName=matcher.group();
			}
		}
		return groupName;
	}


	private Boolean isRegexValid(String regex) {
		try {
			Pattern.compile(regex);
		} catch (PatternSyntaxException e) {
			LOGGER.error("Invalid Regex Pattern.");
			return Boolean.FALSE;
		}
		return Boolean.TRUE;
	}

	private String sanitizeReturnTo(String returnTo){
		LOGGER.debug("Inside sanitizeReturnTo");
		returnTo=StringUtils.trimToEmpty(returnTo);
		String sanitizedUrl = returnTo;
		LOGGER.debug("Original returnTo URL : " + sanitizedUrl);
		//String baseUrl = settings.getBaseUrl();
		String contextPath = MoOAuthPluginHandler.getContextPath();
		if (returnTo.contains(contextPath) && !contextPath.equalsIgnoreCase("/")){
			LOGGER.debug("Found contextPath in returnTo");
			sanitizedUrl = returnTo.replace(contextPath,"");
			LOGGER.debug("returnTo after sanitization : " + returnTo);
		}
		return sanitizedUrl;
	}

}
