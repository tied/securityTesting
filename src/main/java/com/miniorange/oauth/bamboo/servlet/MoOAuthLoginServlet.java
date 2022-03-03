/*
 * Handles the OAuth flow when 'Login with OAuth Provider" button is clicked.
 * URL: base url + /plugins/servlet/oauth/auth
 * 
 * The OAuth flow starts here. An authorization request is created and sent to the 
 * authorize endpoint of the OAuth provider.
 * 
 */

package com.miniorange.oauth.bamboo.servlet;

import com.atlassian.bamboo.util.HtmlUtils;
import com.miniorange.oauth.bamboo.MoOAuthManager;
import com.miniorange.oauth.bamboo.MoOAuthPluginConstants;
import com.miniorange.oauth.bamboo.MoOAuthPluginHandler;
import com.miniorange.oauth.bamboo.MoOAuthSettings;
import com.miniorange.oauth.utils.MoOAuthHttpUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class MoOAuthLoginServlet extends HttpServlet {

	private static Log LOGGER = LogFactory.getLog(MoOAuthLoginServlet.class);

	private MoOAuthSettings settings;

	public MoOAuthLoginServlet(MoOAuthSettings settings) {
		this.settings = settings;
	}

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		LOGGER.debug("OAuth Authentication Servlet doGet() called...");
		
		// if Test configuration is clicked
		String relayState = request.getParameter("return_to");
		LOGGER.debug("relayState is " + relayState);
		String referer = request.getHeader("referer");
		if(settings.getPluginApiAccessRestriction()
				&& !StringUtils.startsWith(referer, this.settings.getBaseUrl())){
			LOGGER.error(
					"Access Denied. API Restriction is enabled and request is not originated from the Bamboo.");
			response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied. You are not allowed to access this page.");
			return;
		}
		if (StringUtils.equals(relayState, "testoauthconfiguration")) {
			MoOAuthHttpUtils.setCookie("test_configuration_in_use","mo_test_configurations",response);
		}else{
			MoOAuthHttpUtils.removeCookie("test_configuration_in_use",request,response);
		}
			
		if(StringUtils.equals(relayState, "verifycredentials")) {
			MoOAuthHttpUtils.setCookie("verify_credentials_in_use","mo_verify_credentials",response);
		} else {
			MoOAuthHttpUtils.removeCookie("verify_credentials_in_use",request,response);
		}
		
		if(!(settings.isTestIDPConfigurationInUse(request) && !(settings.isVerifyCredentialsInUse(request)) && StringUtils.isNotEmpty(request.getParameter("return_to"))))
		{
			HttpSession session = request.getSession();
			session.setAttribute("return_to",request.getParameter("return_to"));
		}

		/* Test Regex */

		LOGGER.debug("returnTo : "+ relayState);
		if (StringUtils.contains(relayState, "testregex")) {
			LOGGER.debug("testing regex pattern...");
			StringBuilder output = new StringBuilder("<div style='display:none'>");
			String regexp = request.getParameter("regexp");
			regexp = StringEscapeUtils.escapeJava(regexp);
			LOGGER.debug("Check if regex is valid : "+ isRegexValid(regexp));
			String message = "TEST REGEX....!";
			message = HtmlUtils.getTextAsHtml(message);

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
			output.append("<script>"
					+ "var input = document.getElementById(\"attrvalue\");"
					+ "input.addEventListener(\"keyup\", function(event) {"
					+ "if (event.keyCode === 13) {"
					+ "document.getElementById(\"testregex-button\").click();"
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
					+ "console.log(\"Result : \" + username);"
					+ "document.querySelector(\"#result\").innerHTML = username;}"

					+ "else { document.querySelector(\"#result\").innerHTML = \"Error : No Pattern Found.\"; }" + "}"
					+ " else { document.querySelector(\"#error\").innerHTML = \"Invalid Regex Expression.\";}"

					+ "}</script>");
			response.setContentType("text/html");
			response.getOutputStream().write(output.toString().getBytes("UTF-8"));
			return;
		}
		if (StringUtils.contains(relayState, "testgroupregexcreate")) {
			LOGGER.debug("test group regex create call...");
			try {
				showTestRegexGroupResult(request,response);
			}catch (IOException e){
				LOGGER.error(e);
			}
			return;
		}

		if (StringUtils.contains(relayState, "testgroupregex")) {
			LOGGER.debug("test group regex call...");
			try {
				showTestGroupRegexWindow(response, request);
			}catch (IOException e){
				LOGGER.error(e);
			}
			return;
		}
		/* Parameters For the Authorization Request */
		String CLIENT_ID = settings.getClientID();
		String SCOPE = settings.getScope();
		String RESPONSE_TYPE = "code";
		String REDIRECT_URI = settings.getBaseUrl().concat("/plugins/servlet/oauth/callback") + settings.getCustomizableCallbackURL();
		Boolean nonceCheck = settings.getNonceCheck();
		Boolean acrValueCheck = settings.getACRValueCheck();
		Boolean useStateParameter = settings.getUseStateParameter();
		Map<String, String> oauth_request_parameters=settings.getOauth_request_parameters();
		Boolean oauthParameterCheck=!oauth_request_parameters.isEmpty();

		StringBuilder AUTHORIZE_ENDPOINT = new StringBuilder(settings.getAuthorizeEndpoint());

		//For keycloak, the scope parameter is not visible for configuration
		//This code is to handle the migration of Keycloak from OAuth Provider to OpenID provider
		if(StringUtils.equals(settings.getAppName(), MoOAuthPluginConstants.KEYCLOAK)){
			SCOPE = "openid";
		}

		if (AUTHORIZE_ENDPOINT.indexOf("?") > -1) {
			AUTHORIZE_ENDPOINT.append("&");
		}else {
			AUTHORIZE_ENDPOINT.append("?");
		}

		AUTHORIZE_ENDPOINT.append("client_id=").append(CLIENT_ID).append("&scope=")
				.append(URLEncoder.encode(SCOPE, "UTF-8")).append("&redirect_uri=").append(URLEncoder.encode(REDIRECT_URI,"UTF-8"))
				.append("&response_type=").append(RESPONSE_TYPE).append("&access_type=offline").append("&include_granted_scopes=true");
		LOGGER.debug("Nonce Check : "+ nonceCheck);
		LOGGER.debug("ACR Value Check : " + acrValueCheck);
		if ((StringUtils.equalsIgnoreCase(settings.getAppName(), "Custom OAuth")
				|| StringUtils.equalsIgnoreCase(settings.getAppName(), "Custom OpenID")
				|| StringUtils.equalsIgnoreCase(settings.getAppName(),"Okta")
				|| StringUtils.equalsIgnoreCase(settings.getAppName(), "miniOrange"))
				|| useStateParameter || nonceCheck || acrValueCheck || oauthParameterCheck) {
			if (nonceCheck) {
				LOGGER.debug("send nonce in the request");
				passNonceInRequest(request, AUTHORIZE_ENDPOINT);
			}
			if(acrValueCheck){
				LOGGER.debug("send acr_values parameter in the request");
				passACRValueParameterInRequest(request, AUTHORIZE_ENDPOINT);
			}
			if (useStateParameter || StringUtils.equalsIgnoreCase(settings.getAppName(), "OKTA")) {
				LOGGER.debug("send state parameter in the request");
				passStateParameterValue(request, AUTHORIZE_ENDPOINT);
			}
			if(oauthParameterCheck){
				LOGGER.debug("send Oauth request parameters in the request");
				passOauthParametersInRequest(request, AUTHORIZE_ENDPOINT,oauth_request_parameters);
			}
		}
		LOGGER.debug("OAuth CODE request generated");

		MoOAuthManager.httpRedirect(response, AUTHORIZE_ENDPOINT.toString());
	
	}



	public void showTestRegexGroupResult(HttpServletRequest request,HttpServletResponse response) throws IOException {
		StringBuilder output = new StringBuilder("<div style='display:none'>");
		String regexp = request.getParameter("regexp");
		String regexg = request.getParameter("regexg");
		String groupName = request.getParameter("groupName");
		String result=StringUtils.EMPTY;
		regexp = org.apache.commons.lang.StringEscapeUtils.escapeJavaScript(regexp);
		regexg= org.apache.commons.lang.StringEscapeUtils.escapeJavaScript(regexg);
		groupName = org.apache.commons.lang.StringEscapeUtils.escapeJavaScript(groupName);

		if (StringUtils.isNotEmpty(regexg)&&StringUtils.isNotEmpty(regexp)&&StringUtils.isNotEmpty(groupName)) {
			result = MoOAuthLoginServlet.getGroupnameFromRegexMethod(regexp, regexg, groupName);
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

	private void passNonceInRequest(HttpServletRequest request, StringBuilder AUTHORIZE_ENDPOINT) {
		String nonce = randomAlphaNumeric(10); 
		HttpSession httpSession = request.getSession(); 
		httpSession.setAttribute("nonce", nonce); 
		AUTHORIZE_ENDPOINT.append("&nonce=").append(nonce); 
	}

	private void passACRValueParameterInRequest(HttpServletRequest request, StringBuilder AUTHORIZE_ENDPOINT) {
		String acrValueParameter = randomAlphaNumeric(10);
		HttpSession httpSession = request.getSession();
		httpSession.setAttribute("acr_values", acrValueParameter);
		AUTHORIZE_ENDPOINT.append("&acr_values=").append(acrValueParameter);
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
	private String randomAlphaNumeric(int count) {
		String ALPHA_NUMERIC_STRING = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
		StringBuilder builder = new StringBuilder();
		while (count-- != 0) {
			int character = (int) (Math.random() * ALPHA_NUMERIC_STRING.length());
			builder.append(ALPHA_NUMERIC_STRING.charAt(character));
		}
		return builder.toString();
	}

	private void showTestGroupRegexWindow(HttpServletResponse response, HttpServletRequest request) throws IOException{
		LOGGER.debug("testing group regex pattern...");
		StringBuilder output = new StringBuilder("<div style='display:none'>");
		String regexp = request.getParameter("regexp");
		regexp = StringEscapeUtils.escapeJava(regexp);
		LOGGER.debug("Check if regex is valid : "+ isRegexValid(regexp));
		String message = "TEST REGEX....!";
		message = HtmlUtils.getTextAsHtml(message);

		output.append(message);
		output.append("</div>");
		output.append(
				"<div style='color: #3c763d;background-color: #dff0d8; padding:2%;margin-bottom:20px;text-align:center; border:1px solid #AEDB9A; font-size:18pt;'>Test Group Regex Pattern</div>");
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
				+ "document.querySelector(\"#result\").innerHTML = username; }"

				+ "else { document.querySelector(\"#result\").innerHTML = \"Error : No Pattern Found.\"; }" + "}"
				+ " else { document.querySelector(\"#error\").innerHTML = \"Invalid Regex Expression.\";}"

				+ "}</script>");
		response.setContentType("text/html");
		response.getOutputStream().write(output.toString().getBytes("UTF-8"));

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


	public void setSettings(MoOAuthSettings settings) {
		this.settings = settings;
	}

	public static String getGroupnameFromRegexMethod(String groupRegexPattern,String regexGroups,String groupName){
		LOGGER.debug("Applying regex pattern on Groupname : "+groupName);
		Pattern pattern = Pattern.compile(StringUtils.trimToEmpty(groupRegexPattern));
		Matcher matcher = pattern.matcher(groupName);
		if (matcher.find()) {
			groupName = StringUtils.EMPTY;
			LOGGER.debug("Matched Groups "+matcher.groupCount());
			if (matcher.groupCount() > 0) {
				groupName= MoOAuthPluginHandler.getGroupNameFromRegex(matcher,StringUtils.trimToEmpty(regexGroups));
			} else {
				groupName=matcher.group();
			}
		}
		return groupName;
	}

}
