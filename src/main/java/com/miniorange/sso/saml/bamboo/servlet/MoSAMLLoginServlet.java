package com.miniorange.sso.saml.bamboo.servlet;

import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.servlet.ServletException;
import javax.servlet.http.*;
import javax.xml.parsers.DocumentBuilderFactory;

import com.atlassian.bamboo.user.LoginInformationImpl;
import com.atlassian.bamboo.ww2.interceptors.DelegatingSecurityExecuteAndWaitInterceptor;
import com.atlassian.crowd.embedded.api.*;
import com.atlassian.json.jsonorg.JSONObject;
import com.atlassian.sal.api.component.ComponentLocator;
import com.atlassian.seraph.service.rememberme.RememberMeService;
import com.miniorange.sso.saml.bamboo.MoPluginConstants;
import com.miniorange.sso.saml.dto.MoIDPConfig;
import com.miniorange.sso.saml.utils.MoHttpUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opensaml.xml.util.Base64;
import org.springframework.web.util.HtmlUtils;
import org.w3c.dom.Node;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;
import org.xml.sax.InputSource;

import com.atlassian.bamboo.security.BambooPermissionManager;

import com.atlassian.bamboo.user.BambooUser;
import com.atlassian.bamboo.user.BambooUserManager;
import com.atlassian.crowd.exception.GroupNotFoundException;
import com.atlassian.crowd.exception.OperationFailedException;
import com.atlassian.crowd.exception.OperationNotPermittedException;
import com.atlassian.crowd.exception.UserNotFoundException;
import com.atlassian.seraph.auth.Authenticator;
import com.atlassian.seraph.auth.DefaultAuthenticator;
import com.atlassian.seraph.config.SecurityConfigFactory;
import com.atlassian.templaterenderer.TemplateRenderer;
import com.atlassian.user.EntityException;
import com.atlassian.user.Group;
import com.atlassian.user.User;
import com.atlassian.user.impl.DefaultUser;
import com.atlassian.user.search.SearchResult;
import com.atlassian.user.search.page.Pager;
import com.miniorange.sso.saml.MoSAMLException;
import com.miniorange.sso.saml.MoSAMLResponse;
import com.miniorange.sso.saml.bamboo.MoPluginHandler;
import com.miniorange.sso.saml.bamboo.MoSAMLManager;
import com.miniorange.sso.saml.bamboo.MoSAMLSettings;
import com.miniorange.sso.saml.utils.MoEncryptionUtils;
import com.miniorange.sso.saml.utils.MoSAMLUtils;

public class MoSAMLLoginServlet extends HttpServlet {
	private static Log LOGGER = LogFactory.getLog(MoSAMLLoginServlet.class);
	private MoSAMLSettings settings;
	private MoSAMLManager samlManager;
	private BambooUserManager bambooUserManager;
	private BambooPermissionManager bambooPermissionManager;
	private final TemplateRenderer renderer;

	private String setup;

	private MoIDPConfig idpConfig;

	public MoSAMLLoginServlet(MoSAMLSettings settings, BambooUserManager bambooUserManager, MoSAMLManager samlManager,
			BambooPermissionManager bambooPermissionManager,
			TemplateRenderer renderer, MoIDPConfig idpConfig) {
		this.settings = settings;
		this.bambooUserManager = bambooUserManager;
		this.samlManager = samlManager;
		this.bambooPermissionManager = bambooPermissionManager;
		this.renderer = renderer;
		this.idpConfig = idpConfig;
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
			result = MoSAMLLoginServlet.getGroupnameFromRegexMethod(regexp, regexg, groupName);
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

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String username = StringUtils.EMPTY;
		String email = StringUtils.EMPTY;
		LOGGER.debug("SAMLLoginServlet doGet() called");
		String returnTo = request.getParameter("return_to");
		returnTo = MoSAMLUtils.sanitizeText(returnTo);

		String title = request.getParameter("title");
		String idpID = request.getParameter("idp");
		this.setup = request.getParameter("setup");
		// Select first IDP in case the IDP ID is not mentioned
		if (StringUtils.isBlank(idpID) && !settings.getIdPList().isEmpty()) {
			idpID = settings.getIdPList().get(0);
		}

		HttpSession session = request.getSession();
		session.setAttribute("idpId", idpID);
		Cookie idpIdCookie = MoHttpUtils.createCookie(request.getRequestURL().toString(), MoPluginConstants.IDP_ID_COOKIE, idpID, true);
		MoHttpUtils.addCookie(response, idpIdCookie);

		if (StringUtils.isNotBlank(idpID)) {
			idpConfig = MoPluginHandler.constructIdpConfigObject(idpID);
		}

		if (idpConfig == null || StringUtils.isBlank(idpConfig.getId())) {
			try {
				LOGGER.error("Error occurred while generating SAML Request. Error_CODE: IDP_ID_INVALID");
				redirectToLoginWithSAMLError(response, null, "cant_send_request");
				return;
			} catch (Exception e) {
				LOGGER.error("Error occurred while generating SAML Request. "+e);
			}
		}

		if (StringUtils.isBlank(returnTo)||StringUtils.endsWith(returnTo,"/plugins/servlet")) {
			LOGGER.debug("Session Created");
			returnTo = settings.getDashboardUrl();
		} else if (StringUtils.contains(returnTo, "testregex")) {
			StringBuilder output = new StringBuilder("<div style='display:none'>");
			String regexp = request.getParameter("regexp");
			regexp = StringEscapeUtils.escapeJavaScript(regexp);
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
			output.append("<script>"
					+ "var input = document.getElementById(\"attrvalue\");"
					+ "input.addEventListener(\"keyup\", function(event) {"
					+ "if (event.keyCode === 13) {"
					+ "document.getElementById(\"testregex-button\").click();"
					+ "}"
					+ "});"
					+ "document.querySelector(\"#testregex-button\").onclick = function() {"
					+ "var attrValue = document.querySelector(\"#attrvalue\").value;" + "var regexp ='" + MoSAMLUtils.sanitizeText(regexp) + "';"

					+ "console.log('original: '+regexp);" + "if(" + isRegexValid(regexp) + ")" + "{"
					+ "console.log('Escaped: '+regexp);" + "var regExp = new RegExp(regexp, 'g');"
					+ "var result = regExp.exec(attrValue);" + " var username = '';" + "if (result && result!=\"\") {"
					+ "if(result.length>1){" + "var length = result.length;"
					+ "for(var count = 1; count < length; count++){" + "console.log('Multiple Groups'); "
					+ "console.log(result[count]);" + "username += result[count];" + "}" + "}" + "else{"
					+ "console.log('No groups '+result);" + "username = result[0];" + "}"
					+ "console.log(\"Result...:\" + username);"
					+ "document.querySelector(\"#result\").innerHTML = username;}"

					+ "else { document.querySelector(\"#result\").innerHTML = \"Error : No Pattern Found.\"; }" + "}"
					+ " else { document.querySelector(\"#error\").innerHTML = \"Invalid Regex Expression.\";}"

					+ "}</script>");
			response.setContentType("text/html");
			response.getOutputStream().write(output.toString().getBytes("UTF-8"));
			return;
		}
		else if (StringUtils.contains(returnTo,"testgroupregex")){
			showTestRegexGroupResult(request,response);
			return;
		}
		else if(StringUtils.containsIgnoreCase(returnTo, "displaysamlrequest")) {
			displaySamlRequest(request, response, idpConfig, setup);
			return;
		} else {
			if (!StringUtils.isBlank(title) && title != null) {
				StringBuffer returnToUrl = new StringBuffer();
				returnToUrl.append(returnTo).append("&").append("title").append("=");
				returnToUrl.append(title);
				returnTo = URLEncoder.encode(returnToUrl.toString(), StandardCharsets.UTF_8.toString());
			}
		}

		ArrayList<String> enabledIdps = settings.getSsoEnabledForIdPList();
		if (!enabledIdps.contains(idpConfig.getId())&& ! returnTo.equals("testidpconfiguration")) {
			try {
				LOGGER.debug("SSO is not enabled for this IDP: " + idpConfig.getIdpName() + " : " + idpConfig.getId());
				redirectToLoginWithSAMLError(response, null, "sso_not_enabled_for_idp");
				return;
			}catch(Exception e){
				LOGGER.error("Error occurred while redirecting to error page.");
			}
		}

		if (!StringUtils.isBlank(idpConfig.getRelayState()) && idpConfig.getRelayStateRedirectionType().equals(MoPluginConstants.FORCE_REDIRECT)) {
			if (!returnTo.equals("displaysamlrequest") && !returnTo.equals("displaysamlresponse")
					&& !returnTo.equals("testidpconfiguration"))
				returnTo = idpConfig.getRelayState();
		}

		if (idpConfig.getSignedRequest())
			samlManager.createAuthnRequestAndRedirect(request, response, returnTo, idpConfig);
		else
			samlManager.createUnSignedAuthnRequestAndRedirect(request, response, returnTo, idpConfig);
	}

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		try {
			LOGGER.debug("SAML Login Servlet doPost() called...");
			String username = StringUtils.EMPTY;
			String email = StringUtils.EMPTY;
			MoSAMLResponse samlResponse = null;
			String relayState = request.getParameter(MoSAMLUtils.RELAY_STATE_PARAM);
			relayState = MoSAMLUtils.sanitizeText(relayState);

			String idpid = getIdpId(request, response);

			if(StringUtils.isNotBlank(idpid)){
				if (!settings.getIdPList().contains(idpid)) {
					redirectToLoginWithSAMLError(response, null, "cant_find_idp");
					return;
				}
				this.idpConfig = MoPluginHandler.constructIdpConfigObject(idpid);

			} else if(this.idpConfig==null || this.idpConfig.getId() == null) {
				if (settings.getIdPList().size() == 1) {
					idpid = settings.getIdPList().get(0);
				} else if (StringUtils.isNotBlank(settings.getSsoServiceUrl())) {
					idpid = MoPluginConstants.DEFAULT_IDP_ID;
				} else {
					redirectToLoginWithSAMLError(response, null, "cant_find_idp");
					return;
				}
				this.idpConfig = MoPluginHandler.constructIdpConfigObject(idpid);
			}

			if (StringUtils.contains(relayState, "displaysamlresponse")) {
				displaySamlResponse(request, response, idpConfig, setup);
				return;
			}

			if(!settings.getEnableSAMLSSO() && !relayState.equals("testidpconfiguration")){
				try {
					LOGGER.debug("SSO is not enabled  " );
					redirectToLoginWithSAMLError(response, null, "sso_not_enabled");
					return;
				}catch(Exception e){
					LOGGER.error("Error occurred while redirecting to error page.");
				}
			}

			ArrayList<String> enabledIdps = settings.getSsoEnabledForIdPList();
			if (!enabledIdps.contains(idpConfig.getId())&& !relayState.equals("testidpconfiguration")) {
				try {
					LOGGER.debug("SSO is not enabled for this IDP: " + idpConfig.getIdpName() + " : " + idpConfig.getId());
					redirectToLoginWithSAMLError(response, null, "sso_not_enabled_for_idp");
					return;
				}catch(Exception e){
					LOGGER.error("Error occurred while redirecting to error page.");
				}
			}

			try {
				samlResponse = samlManager.readSAMLResponse(request, response, idpConfig);
			} catch (MoSAMLException e) {
				if (StringUtils.contains(relayState, "testidpconfiguration")) {
					showTestConfigurationResult(null, request, response,setup, e);
					return;
				}
				redirectToLoginWithSAMLError(response, null, "cant_signin_check_configuration");
				return;
			}
			if (StringUtils.contains(samlResponse.getRelayStateURL(), "testidpconfiguration")) {
				showTestConfigurationResult(samlResponse, request, response, setup, null);
				return;
			}

			/* Check License */
			if (!settings.isLicenseDefine()) {
				LOGGER.debug("No Evaluation license installed");
				redirectToLoginWithSAMLError(response, null, "cant_signin_no_license");
				return;
			}

			if (samlResponse.getAttributes().get(idpConfig.getUsernameAttribute()) != null
					&& samlResponse.getAttributes().get(idpConfig.getUsernameAttribute()).length == 1) {
				username = samlResponse.getAttributes().get(idpConfig.getUsernameAttribute())[0];
			}

			if (samlResponse.getAttributes().get(idpConfig.getEmailAttribute()) != null
					&& samlResponse.getAttributes().get(idpConfig.getEmailAttribute()).length == 1) {
				email = samlResponse.getAttributes().get(idpConfig.getEmailAttribute())[0];
			}

			username = username.toLowerCase();
			email = email.toLowerCase();
			LOGGER.debug("Username received: " + username + ", Email received: " + email);

			/* check regex */
			if (idpConfig.getRegexPatternEnabled()) {
				try {
					Pattern pattern = Pattern.compile(StringUtils.trimToEmpty(idpConfig.getRegexPattern()));
					Matcher matcher = pattern.matcher(username);
					LOGGER.debug(matcher);
					if (matcher.find()) {
						username = StringUtils.EMPTY;
						if (matcher.groupCount() > 0) {
							for (int i = 1; i <= matcher.groupCount(); i++) {
								username += matcher.group(i);
							}
						} else {
							username = matcher.group();
						}
						LOGGER.debug("Username after regex operation: " + username);
					}
				} catch (Exception e) {
					e.printStackTrace();
					redirectToLoginWithSAMLError(response, null, "cant_signin_regex_pattern_exception");
				}
			}

			if (StringUtils.isNotBlank(username) && StringUtils.isNotBlank(email)) {
				BambooUser bambooUser = null;
				if (StringUtils.equalsIgnoreCase(idpConfig.getLoginUserAttribute(), "email")) {
					LOGGER.debug("Searching user by email attribute ");
//					LOGGER.debug("Insatnce of Default User : "+(bambooUserManager.getUsersByEmail(email).pager().iterator().next() instanceof DefaultUser));
//					LOGGER.debug("Insatnce of com.atlassian.crowd.embedded.atlassianuser.EmbeddedCrowdUser : "+(bambooUserManager.getUsersByEmail(email).pager().iterator().next() instanceof com.atlassian.crowd.embedded.atlassianuser.EmbeddedCrowdUser));

					SearchResult<Object> emailUserSearchResult= bambooUserManager.getUsersByEmail(email);
					Pager<Object> pager = emailUserSearchResult.pager();
					int countUser = 0;
					String userFoundByEmail = email;
					for(Object user : pager){
						countUser++;
						LOGGER.debug("Class Name : "+user.getClass());
						if (user instanceof com.atlassian.user.impl.DefaultUser) {
							userFoundByEmail = ((com.atlassian.user.impl.DefaultUser)user).getName();
						} else if (user instanceof com.atlassian.crowd.embedded.atlassianuser.EmbeddedCrowdUser) {
							userFoundByEmail = ((com.atlassian.crowd.embedded.atlassianuser.EmbeddedCrowdUser)user).getName();
						} else {
							userFoundByEmail = username;
						}
					}
					LOGGER.debug("userFoundByEmail : "+userFoundByEmail);
					
					if(countUser > 1) {
						LOGGER.error("More than one user found with the same email i.e "+email+" address.");
						redirectToLoginWithSAMLError(response, null, "cant_signin_check_configuration");
						return;
					}

					if(!StringUtils.isEmpty(userFoundByEmail)){
						bambooUser = bambooUserManager.getBambooUser(userFoundByEmail);
						LOGGER.debug("Bamboo user found by email  = "+bambooUser);
					}

				} else{
					LOGGER.debug("Searching user by username attribute ");
					bambooUser = bambooUserManager.getBambooUser(username);
				}
				
				Authenticator authenticator = SecurityConfigFactory.getInstance().getAuthenticator();
				if ((authenticator instanceof DefaultAuthenticator)) {
					Method getUserMethod = DefaultAuthenticator.class.getDeclaredMethod("getUser", String.class);
					getUserMethod.setAccessible(true);
					Object userObject = null;
					if (bambooUser == null) {
						LOGGER.debug("User doesn't exist. Creating New User." + idpConfig.getAllowUserCreation());
						if (BooleanUtils.toBoolean(idpConfig.getAllowUserCreation())) {
							userObject = tryCreateOrUpdateUser(username, email, samlResponse);
						} else {
							LOGGER.debug("New user creation is restricted, unable to login new user");
							redirectToLoginWithSAMLError(response, null, "cant_signin_check_configuration");
							return;
						}
					} else {
						LOGGER.debug("User exist. Updating User.");
						if (idpConfig.getRegexPatternEnabled()) {
							if (!email.equals(bambooUser.getEmail())) {
								LOGGER.debug("Different Email Found");
								redirectToLoginWithSAMLError(response, null, "cant_signin_check_configuration");
								return;
							}
						}
						userObject = updateUserProfile(bambooUser.getName(), email, samlResponse);
						LOGGER.debug("Is User with username "+bambooUser.getName()+" active :" + bambooUser.isEnabled());
						if (!BooleanUtils.toBoolean(bambooUser.isEnabled())) {
							LOGGER.debug("User is not allowed to login because user is inactive");
							redirectToLoginWithSAMLError(response, null, "cant_signin_user_inactive");
							return;
						}
					}
					if (userObject != null) {
						LOGGER.debug("Establishing session for: " + userObject);
						Boolean result = authoriseUserAndEstablishSession((DefaultAuthenticator) authenticator,
								userObject, request, response);
						if (result.booleanValue()) {
							LOGGER.debug("Session created. Redirecting user to " + samlResponse.getRelayStateURL());
							Cookie logoutCookie = MoHttpUtils.createCookie(request.getRequestURL().toString(), "LOGOUTCOOKIE",
									idpConfig.getId(), false);
							Cookie nameIDCookie = MoHttpUtils.createCookie(request.getRequestURL().toString(), "NAMEIDCOOKIE",
									samlResponse.getNameId(), false);
							Cookie sessionIndexCookie = MoHttpUtils.createCookie(request.getRequestURL().toString(), "SESSIONINDEXCOOKIE",
									samlResponse.getSessionIndex(), false);
							response.addCookie(logoutCookie);
							response.addCookie(nameIDCookie);
							response.addCookie(sessionIndexCookie);
							addLastAuthenticationTimestamp(((BambooUser)userObject).getName());
							redirectToSuccessfulAuthLandingPage(request, response, samlResponse.getRelayStateURL());
							return;
						}
						LOGGER.debug("Session could NOT be created. Redirecting user to login page.");
					}
				}
			}
			LOGGER.error("Username or email not received in the SAML Response. Please check your configuration.");

		} catch (Exception e) {
			LOGGER.error("An error occurred while verifying the SAML Response.", e);
		}
		redirectToLoginWithSAMLError(response, null, "cant_signin_check_configuration");
	}

	private void displaySamlRequest(HttpServletRequest request, HttpServletResponse response, MoIDPConfig idpConfig, String setup){
		try {
			StringBuilder output = new StringBuilder("<div style='display:none'>");
			String message;
			if (idpConfig.getSignedRequest())
				message = samlManager.getSignedTestAuthnRequest(idpConfig);
			else
				message = samlManager.getUnSignedTestAuthnRequest(idpConfig);

			message = XmlFormatter(message);

			JSONObject idpConfigObject = settings.getIdpConfig(idpConfig.getId());
			idpConfigObject.put("samlRequest", message);
			settings.setIdpConfig(idpConfigObject, idpConfig.getId());

			message = HtmlUtils.htmlEscape(message);

			output.append(message);
			output.append("</div>");
			output.append(
					"<br><div style='color: #3c763d;background-color: #dff0d8; padding:2%;margin-bottom:20px;text-align:center; border:1px solid #AEDB9A; font-size:18pt;'>Authentication(SAML) Request</div>");
			output.append(
					"<textarea rows='6' cols='100' word-wrap='break-word;' style='width:100%; margin:0px; height:350px;' id ='reqmessage' readonly>"
							+ message + "</textarea> ");
			if(!StringUtils.equals(setup,"quicktestconfig")) {
				output.append("<div style=\"margin:3%;display:block;text-align:center;\"><input style=\"padding:1%;"
						+ "width:100px;background: #0091CD none repeat scroll 0% 0%;cursor: pointer;font-size:15px;"
						+ "border-width: 1px;border-style: solid;border-radius: 3px;white-space: nowrap;"
						+ "box-sizing:border-box;border-color: #0073AA;box-shadow:0px 1px 0px rgba(120,200,230,0.6) inset;"
						+ "color: #FFF;\" type=\"button\" value=\"Done\" onClick=\"self.close();\"></div>");
			}
			output.append(
					"<div style=\"margin:3%;display:block;text-align:center;\"><input id =\"copy-req-button\" style=\"padding:1%;"
							+ "width:150px;background: #0091CD none repeat scroll 0% 0%;cursor: pointer;font-size:15px;"
							+ "border-width: 1px;border-style: solid;border-radius: 3px;white-space: nowrap;"
							+ "box-sizing:border-box;border-color: #0073AA;box-shadow:0px 1px 0px rgba(120,200,230,0.6) inset;"
							+ "color: #FFF;\" type=\"button\" value=\"Copy to Clipboard\"></div>");
			output.append(
					"<div style=\"margin:3%;display:block;text-align:center;\"><input id =\"download-button\" style=\"padding:1%;"
							+ "width:200px;background: #0091CD none repeat scroll 0% 0%;cursor: pointer;font-size:15px;"
							+ "border-width: 1px;border-style: solid;border-radius: 3px;white-space: nowrap;"
							+ "box-sizing:border-box;border-color: #0073AA;box-shadow:0px 1px 0px rgba(120,200,230,0.6) inset;"
							+ "color: #FFF;\" type=\"button\" value=\"Download SAMLRequest\" onclick=\"downloadRequest()\"></div>");
			output.append("<script>" + "document.querySelector(\"#copy-req-button\").onclick = function() {"
					+ "document.querySelector(\"#reqmessage\").select();" + "document.execCommand('copy');" + "};"
					+ "</script>");
			output.append("<script>" + "function downloadRequest() {"
					+ "var textToWrite = document.getElementById(\"reqmessage\").value;"
					+ "var blob = new Blob([textToWrite],{" + "type: \"text/xml;charset=utf-8;\"});"
					+ "var fileNameToSaveAs = \"SAMLRequest.xml\";"
					+ "var downloadLink = document.createElement(\"a\");" + "downloadLink.download = fileNameToSaveAs;"
					+ "downloadLink.innerHTML = \"Download File\";" + "if(window.navigator.msSaveOrOpenBlob){"
					+ "navigator.msSaveBlob(blob, \"SAMLRequest.xml\");}" + "if (window.webkitURL != null){"
					+ "downloadLink.href = window.webkitURL.createObjectURL(blob);}" + "else{"
					+ "downloadLink.href = window.URL.createObjectURL(blob);}"
					+ "downloadLink.onclick = destroyClickedElement;"
					+ "document.body.appendChild(downloadLink);"
					+ "downloadLink.style.display = \"none\";"
					+ "downloadLink.click();}"
					+ "function destroyClickedElement(event){" + "document.body.removeChild(event.target);" + "}"
					+ "</script>");

			if (!StringUtils.equalsIgnoreCase(setup, "quick")) {
				response.setContentType("text/html");
				response.setCharacterEncoding("iso-8859-1");
				response.getOutputStream().write(output.toString().getBytes(StandardCharsets.UTF_8));
			}
		}catch (Exception e){
			LOGGER.error("Error occurred while displaying SAML request.");
		}
	}

	private void displaySamlResponse(HttpServletRequest request, HttpServletResponse response, MoIDPConfig idpConfig, String setup){
		try {
			StringBuilder output = new StringBuilder("<div style='display:none'>");
			String encodedSAMLResponse = request.getParameter(MoSAMLUtils.SAML_RESPONSE_PARAM);
			String xml = new String(Base64.decode(encodedSAMLResponse), "UTF-8");
			xml = XmlFormatter(xml);
			output.append(xml);

			JSONObject idpConfigObject = settings.getIdpConfig(idpConfig.getId());
			idpConfigObject.put("samlResponse", xml);
			settings.setIdpConfig(idpConfigObject, idpConfig.getId());

			xml = HtmlUtils.htmlEscape(xml);
			output.append("</div>");
			output.append(
					"<div style='color: #3c763d;background-color: #dff0d8; padding:2%;margin-bottom:20px;text-align:center; border:1px solid #AEDB9A; font-size:18pt;'>SAML Response</div>");
			output.append(
					"<textarea rows='6' cols='100' word-wrap='break-word;' style='width:100%; margin:0px; height:350px;' id ='resmessage' readonly>"
							+ MoSAMLUtils.sanitizeText(xml) + "</textarea> ");

			if(!StringUtils.equals(setup,"quicktestconfig")) {
				output.append("<div style=\"margin:3%;display:block;text-align:center;\"><input style=\"padding:1%;"
						+ "width:100px;background: #0091CD none repeat scroll 0% 0%;cursor: pointer;font-size:15px;"
						+ "border-width: 1px;border-style: solid;border-radius: 3px;white-space: nowrap;"
						+ "box-sizing:border-box;border-color: #0073AA;box-shadow:0px 1px 0px rgba(120,200,230,0.6) inset;"
						+ "color: #FFF;\" type=\"button\" value=\"Done\" onClick=\"self.close();\"></div>");
			}
			output.append(
					"<div style=\"margin:3%;display:block;text-align:center;\"><input id =\"copy-res-button\" style=\"padding:1%;"
							+ "width:150px;background: #0091CD none repeat scroll 0% 0%;cursor: pointer;font-size:15px;"
							+ "border-width: 1px;border-style: solid;border-radius: 3px;white-space: nowrap;"
							+ "box-sizing:border-box;border-color: #0073AA;box-shadow:0px 1px 0px rgba(120,200,230,0.6) inset;"
							+ "color: #FFF;\" type=\"button\" value=\"Copy to Clipboard\"></div>");
			output.append(
					"<div style=\"margin:3%;display:block;text-align:center;\"><input id =\"download-button\" style=\"padding:1%;"
							+ "width:200px;background: #0091CD none repeat scroll 0% 0%;cursor: pointer;font-size:15px;"
							+ "border-width: 1px;border-style: solid;border-radius: 3px;white-space: nowrap;"
							+ "box-sizing:border-box;border-color: #0073AA;box-shadow:0px 1px 0px rgba(120,200,230,0.6) inset;"
							+ "color: #FFF;\" type=\"button\" value=\"Download SAMLResponse\" onclick=\"downloadResponse()\"></div>");
			output.append("<script>" + "document.querySelector(\"#copy-res-button\").onclick = function() {"
					+ "document.querySelector(\"#resmessage\").select();" + "document.execCommand('copy');" + "};"
					+ "</script>");
			output.append("<script>" + "function downloadResponse() {"
					+ "var textToWrite = document.getElementById(\"resmessage\").value;"
					+ "var blob = new Blob([textToWrite],{" + "type: \"text/xml;charset=utf-8;\"});"
					+ "var fileNameToSaveAs = \"SAMLResponse.xml\";"
					+ "var downloadLink = document.createElement(\"a\");"
					+ "downloadLink.download = fileNameToSaveAs;" + "downloadLink.innerHTML = \"Download File\";"
					+ "if(window.navigator.msSaveOrOpenBlob){"
					+ "navigator.msSaveBlob(blob, \"SAMLResponse.xml\");}" + "if (window.webkitURL != null){"
					+ "downloadLink.href = window.webkitURL.createObjectURL(blob);}" + "else{"
					+ "downloadLink.href = window.URL.createObjectURL(blob);}"
					+ "downloadLink.onclick = destroyClickedElement;"
					+ "document.body.appendChild(downloadLink);"
					+ "downloadLink.style.display = \"none\";"
					+ "downloadLink.click();}"
					+ "function destroyClickedElement(event){" + "document.body.removeChild(event.target);" + "}"
					+ "</script>");
			if (!StringUtils.equalsIgnoreCase(setup, "quick")) {
				response.setContentType("text/html");
				response.setCharacterEncoding("iso-8859-1");
				response.getOutputStream().write(output.toString().getBytes(StandardCharsets.UTF_8));
			}
		}catch (Exception e){
			LOGGER.error("error occurred while displaying SAML Resposne :" , e);
		}
	}

	private void addLastAuthenticationTimestamp(String username) throws OperationNotPermittedException {
		CrowdService crowdService = ComponentLocator.getComponent(CrowdService.class);
		com.atlassian.crowd.embedded.api.User user = crowdService.getUser(username);
		Calendar cal = Calendar.getInstance();
		crowdService.setUserAttribute(user,"lastAuthenticated",String.valueOf(cal.getTimeInMillis()));
	}

	private Boolean authoriseUserAndEstablishSession(DefaultAuthenticator authenticator, Object userObject,
			HttpServletRequest request, HttpServletResponse response)
			throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
		Principal principal = (Principal) userObject;
		Method authUserMethod = DefaultAuthenticator.class.getDeclaredMethod("authoriseUserAndEstablishSession",
				HttpServletRequest.class, HttpServletResponse.class, Principal.class);

		authUserMethod.setAccessible(true);
		Boolean result = (Boolean) authUserMethod.invoke(authenticator, new Object[]{request, response, principal});
		LOGGER.debug(
				"Authentication Result: " + result + " Is cookie Enabled?: " + settings.getRememberMeCookieEnabled());
		if (result && settings.getRememberMeCookieEnabled()) {
			RememberMeService rememberMeService = null;
			Method remembermeMethod = DefaultAuthenticator.class.getDeclaredMethod("getRememberMeService");
			remembermeMethod.setAccessible(true);
			Object returnValue = remembermeMethod.invoke(authenticator);
			if (!(returnValue instanceof RememberMeService)) {
				LOGGER.debug("Authenticator method {} did not return a RememberMeService, but a ");
			}
			rememberMeService = (RememberMeService) returnValue;
			String addCookieMethodName = "addRememberMeCookie";
			Class<?>[] parameterTypes = new Class[]{HttpServletRequest.class, HttpServletResponse.class,
					String.class};
			Object[] parameters = new Object[]{request, response, principal.getName()};

			Class<?> rememberMeServiceClass = rememberMeService.getClass();
			LOGGER.debug("Scanning methods for " + rememberMeServiceClass.getCanonicalName());
			Method methodToInvoke = rememberMeServiceClass.getMethod(addCookieMethodName, parameterTypes);
			methodToInvoke.setAccessible(true);
			methodToInvoke.invoke(rememberMeService, parameters);
			LOGGER.debug("Calling {} succeeded." + addCookieMethodName);
		}
		return result;
	}

	private void redirectToSuccessfulAuthLandingPage(HttpServletRequest request, HttpServletResponse response,
			String relayState) throws IOException {
		LOGGER.debug("RELAY STATE::::"+relayState);
		String redirectUrl = settings.getDashboardUrl();
		if (StringUtils.isNotBlank(relayState)) {
			if (StringUtils.contains(relayState, "://")){
				if(settings.isRelayStateDomainValid(idpConfig.getRelayState(),relayState)) {
					relayState = StringUtils.stripEnd(relayState, "/");
					if (StringUtils.isNotBlank(idpConfig.getRelayState()) &&
							(idpConfig.getRelayStateRedirectionType().equals(MoPluginConstants.FORCE_REDIRECT) || relayState.equals(settings.getSpBaseUrl())))
						redirectUrl = idpConfig.getRelayState();
					else
						redirectUrl = relayState;
				}else{
					redirectUrl = StringUtils.isNotBlank(idpConfig.getRelayState()) ? idpConfig.getRelayState() : settings.getBaseUrl();
				}
			} else {
				if(StringUtils.isNotBlank(idpConfig.getRelayState()) &&
						(idpConfig.getRelayStateRedirectionType().equals(MoPluginConstants.FORCE_REDIRECT) || relayState.equals("/start.action")))
					redirectUrl = idpConfig.getRelayState();
				else
					redirectUrl = settings.getBaseUrl().concat(relayState);
			}
		}

		if(StringUtils.contains(redirectUrl,"/logout")||StringUtils.contains(redirectUrl,"saml/logout")){
			redirectUrl=settings.getBaseUrl();
		}

		LOGGER.debug("Redirecting user to " + redirectUrl);
		redirect(response, redirectUrl);
	}

	private void redirectToLoginWithSAMLError(HttpServletResponse response, Exception exception, String string)
			throws ServletException {
		try {
			if (exception != null) {
				LOGGER.error("SAML Plugin error: " + exception.getMessage(), exception);
			}
			String redirectUrl = settings.getLoginPageUrl() + "?samlerror=" + string;
			//if (settings.getDefaultLoginDisabled() && settings.getBackdoorEnabled()) {
			//	redirectUrl += "&saml_sso=false";
			//}

			if (BooleanUtils.toBoolean(settings.getEnableErrorMsgTemplate()) || (settings.getDefaultLoginDisabled() && settings.getIdPList().size() == 1)) {
				String baseURL = settings.getLoginPageUrl();
				Map<String, Object> context = new HashMap();

				context.put("baseUrl", baseURL);
				response.setContentType("text/html;charset=utf-8");
				String result = renderer.renderFragment(settings.getErrorMsgTemplate(), context);
				response.getWriter().write(result);

			} else {
				redirect(response, redirectUrl);
			}
		} catch (IOException ioException) {
			throw new ServletException();
		}
	}

	private BambooUser tryCreateOrUpdateUser(String username, String email, MoSAMLResponse samlResponse)
			throws UserNotFoundException, OperationFailedException, GroupNotFoundException,
			OperationNotPermittedException, IllegalArgumentException, EntityException {
		LOGGER.debug("Creating New User.");
		String fullName = StringUtils.EMPTY;
		String firstName = StringUtils.EMPTY, lastName = StringUtils.EMPTY;

		if (idpConfig.getUseSeparateNameAttributes()) {
			if (samlResponse.getAttributes().get(idpConfig.getFirstNameAttribute()) != null
					&& samlResponse.getAttributes().get(idpConfig.getFirstNameAttribute()).length == 1) {
				firstName = samlResponse.getAttributes().get(idpConfig.getFirstNameAttribute())[0];
			}
			if (samlResponse.getAttributes().get(idpConfig.getLastNameAttribute()) != null
					&& samlResponse.getAttributes().get(idpConfig.getLastNameAttribute()).length == 1) {
				lastName = samlResponse.getAttributes().get(idpConfig.getLastNameAttribute())[0];
			}
			fullName = firstName + " " + lastName;
		} else if (samlResponse.getAttributes().get(idpConfig.getFullNameAttribute()) != null
				&& samlResponse.getAttributes().get(idpConfig.getFullNameAttribute()).length == 1) {
			fullName = samlResponse.getAttributes().get(idpConfig.getFullNameAttribute())[0];
		}

		String[] roleValues = samlResponse.getAttributes().get(idpConfig.getRoleAttribute());
		LOGGER.debug("roleValues = " + roleValues);
		Boolean userWithNOIDPGroups = (roleValues == null || roleValues.length == 0);
		LOGGER.debug("\n\n userWithNOIDPGroups = " + userWithNOIDPGroups + "\n\n");
		ArrayList<String> roleValuesList = serializeGroups(roleValues);
		List<String> groupsToAssign = getListOfMappedGroupsToAssign(roleValuesList);
		String randomPassword = getRandomPassword();

		if (StringUtils.isBlank(fullName)) {
			fullName = username;
		}
		BambooUser createdBombooUser = null;

		boolean addDefaultGroups = !StringUtils.equals(idpConfig.getEnableDefaultGroupsFor(),
				MoPluginConstants.ENABLE_DEFAULT_GROUPS_FOR_DO_NOT_ASSIGN_DEFAULT_GROUPS) && ((StringUtils.equals(idpConfig.getEnableDefaultGroupsFor(),
				MoPluginConstants.ENABLE_DEFAULT_GROUPS_FOR_USERS_WITH_NO_IDP_GROUPS) && userWithNOIDPGroups == true)
				|| StringUtils.equals(idpConfig.getEnableDefaultGroupsFor(),MoPluginConstants.ENABLE_DEFAULT_GROUPS_FOR_NEW_USERS)
				|| StringUtils.equals(idpConfig.getEnableDefaultGroupsFor(),MoPluginConstants.ENABLE_DEFAULT_GROUPS_FOR_ALL_USERS));
		List<String> defaultGroup = new ArrayList<String>();
		if (BooleanUtils.toBoolean(addDefaultGroups)) {
			LOGGER.debug("Assigning default group to new user");
			List<String> defaultGroups = (List<String>) idpConfig.getDefaultGroupsList();
			if (defaultGroups != null && defaultGroups.size() > 0) {
				defaultGroup.addAll(defaultGroups);
			} else {
				defaultGroup.add(idpConfig.getDefaultGroup());
			}
		}

		if (idpConfig.getOnTheFlyGroupCreation()) {
			groupsToAssign = new ArrayList<>();
			LOGGER.debug("On the Fly is enabled. Creating user with Username: " + username + ", Email: " + email + ", " + "Name:"
					+ fullName);
			if (roleValues != null && roleValues.length > 0) {
				LOGGER.debug("Assigning mapped groups to user");
				groupsToAssign = createAndAssignGroups(roleValuesList);
			}
			groupsToAssign.addAll(defaultGroup);
			createdBombooUser = bambooUserManager.addUser(username, randomPassword, email, fullName, "",
					groupsToAssign);
		} else {
			if ((idpConfig.getCreateUsersIfRoleMapped() && groupsToAssign.size() > 0)
					|| !idpConfig.getCreateUsersIfRoleMapped()) {
				LOGGER.debug("Roles found. Creating user with Username: " + username + ", Email: " + email + ", " + "Name:"
						+ fullName);

				if (groupsToAssign.isEmpty()) {
					LOGGER.debug("username = " + username + "email = " + email
							+ "fullName = " + fullName + "defaultGroup = " + defaultGroup);
					createdBombooUser = bambooUserManager.addUser(username, randomPassword, email, fullName, "",
							defaultGroup);
				} else {
					LOGGER.debug("username = " + username + "email = " + email
							+ "fullName = " + fullName + "defaultGroup = " + groupsToAssign);

					groupsToAssign.addAll(defaultGroup);
					createdBombooUser = bambooUserManager.addUser(username, randomPassword, email, fullName, "",
							groupsToAssign);
				}
			}
		}
		return createdBombooUser;
	}

	private List<String> createAndAssignGroups(ArrayList<String> roleValuesList) {
		List<String> groupsToAssign = new ArrayList<String>();
		Boolean canCreateNewGroups = idpConfig.getCreateNewGroups();
		try {
			for (String groupsName : roleValuesList) {
				if (bambooUserManager.getGroup(groupsName.trim()) == null && BooleanUtils.toBoolean(canCreateNewGroups)) {
					LOGGER.debug("Creating New Group : " + groupsName.trim());
					bambooUserManager.createGroup(groupsName.trim());
				}
				Group bambooGroup = bambooUserManager.getGroup(groupsName.trim());
				if (bambooGroup != null) {
					LOGGER.debug("Assigning Bamboo Group to user with name : " + bambooGroup.getName().trim());
					groupsToAssign.add(groupsName.trim());
				} else {
					LOGGER.debug("Group not present in Bamboo with name : " + groupsName.trim());
				}
			}
		} catch (Exception e) {
			LOGGER.error("An Error occurred while creating a list of group for assigning the user");
			e.printStackTrace();
		}
		return groupsToAssign;
	}

	/**
	 * serializeGroups() checks if any of the value has ';'. If it does, then
	 * these values are separated and stored back in the list. Then all the
	 * values in final list are trimmed. parameters : array of values received
	 * in saml assertion returns : List of groups/roles
	 */
	private ArrayList<String> serializeGroups(String[] roleValues) {
		ArrayList<String> trimmedList = new ArrayList<String>();
		if (roleValues != null && roleValues.length != 0) {
			ArrayList<String> roleValuesList = new ArrayList<String>(Arrays.asList(roleValues));
			for (int i = 0; i < roleValuesList.size(); i++) {
				if (StringUtils.contains(roleValuesList.get(i), ";")) {
					String roleValue = roleValuesList.remove(i);
					ArrayList<String> separatedValues = new ArrayList<String>(
							Arrays.asList(StringUtils.split(roleValue, ";")));
					roleValuesList.addAll(separatedValues);
				}
			}

			for (String roleValue : roleValuesList) {
				LOGGER.debug("Group Regex Pattern Enabled "+idpConfig.getGroupRegexPatternEnabled());
				if(idpConfig.getGroupRegexPatternEnabled()){
					LOGGER.debug("Transforming the rolevalue");
					roleValue = getGroupnameFromRegexMethod(idpConfig.getRegexPatternForGroup(),idpConfig.getRegexGroups(),roleValue);
				}
				trimmedList.add(roleValue.trim());
			}
		}
		return trimmedList;

	}

	private List<String> getListOfMappedGroupsToAssign(ArrayList<String> roleValues) {

		List<String> groupsToAssign = new ArrayList<>();
		if (roleValues != null && roleValues.size() > 0) {
			HashMap<String, String> roleMapping = idpConfig.getRoleMapping();
			Iterator<String> it = roleMapping.keySet().iterator();
			while (it.hasNext()) {
				String key = it.next();
				String value = roleMapping.get(key);
				String[] groupNamesConfigured = StringUtils.split(value, ";");
				for (int i = 0; i < groupNamesConfigured.length; i++) {
					String groupValue = groupNamesConfigured[i];

					if (roleValues.contains(groupValue)) {
						Group bambooUsersGroup = bambooUserManager.getGroup(key);
						if (bambooUsersGroup != null) {
							groupsToAssign.add(bambooUsersGroup.toString());
						}
					}
				}
			}
		}
		return groupsToAssign;
	}

	public String getRandomPassword() {
		String SALTCHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
		StringBuilder salt = new StringBuilder();
		Random rnd = new Random();
		while (salt.length() < 18) {
			int index = (int) (rnd.nextFloat() * SALTCHARS.length());
			salt.append(SALTCHARS.charAt(index));
		}
		String saltStr = salt.toString();
		return saltStr;

	}

	private BambooUser updateUserProfile(String username, String email, MoSAMLResponse samlResponse) throws Exception {

		LOGGER.debug("Update user profile called");
		String fullName = StringUtils.EMPTY;
		String firstName = StringUtils.EMPTY, lastName = StringUtils.EMPTY;

		if (idpConfig.getUseSeparateNameAttributes()) {
			if (samlResponse.getAttributes().get(idpConfig.getFirstNameAttribute()) != null
					&& samlResponse.getAttributes().get(idpConfig.getFirstNameAttribute()).length == 1) {
				firstName = samlResponse.getAttributes().get(idpConfig.getFirstNameAttribute())[0];
			}
			if (samlResponse.getAttributes().get(idpConfig.getLastNameAttribute()) != null
					&& samlResponse.getAttributes().get(idpConfig.getLastNameAttribute()).length == 1) {
				lastName = samlResponse.getAttributes().get(idpConfig.getLastNameAttribute())[0];
			}
			fullName = firstName + " " + lastName;
		} else if (samlResponse.getAttributes().get(idpConfig.getFullNameAttribute()) != null
				&& samlResponse.getAttributes().get(idpConfig.getFullNameAttribute()).length == 1) {
			fullName = samlResponse.getAttributes().get(idpConfig.getFullNameAttribute())[0];
		}

		//if true, user attributes will be updated
		if (idpConfig.getKeepExistingUserAttributes()) {
			try {
				LOGGER.debug("Updating user attributes.");
				User user = bambooUserManager.getUser(username);
				DefaultUser defaultUser = new DefaultUser(user);
				defaultUser.setFullName(fullName);
				defaultUser.setEmail(email);
				bambooUserManager.saveUser(defaultUser);

			} catch (Exception e) {
				LOGGER.debug("An exception occurs while updating user.");
			}
		}

		String[] roleValues = samlResponse.getAttributes().get(idpConfig.getRoleAttribute());
		LOGGER.debug("roleValues = " + roleValues);
		Boolean userWithNOIDPGroups = (roleValues == null || roleValues.length == 0);
		LOGGER.debug("\n\n userWithNOIDPGroups = " + userWithNOIDPGroups + "\n\n");
		ArrayList<String> roleValuesList = serializeGroups(roleValues);
		List<String> groupsToAssign = getListOfMappedGroupsToAssign(roleValuesList);

		Boolean canBeUpdated = canGroupBeUpdated(username);
		if (idpConfig.getKeepExistingUserRoles()) {

			LOGGER.debug("Can groups can be updated: "+canBeUpdated);

			List<String> existingGroupsOfUser = new ArrayList<>();
			existingGroupsOfUser = bambooUserManager.getGroupNamesAsList(bambooUserManager.getBambooUser(username));

			//if (bambooPermissionManager.isAdmin(username) || bambooPermissionManager.isSystemAdmin(username)) {
			//	LOGGER.debug("Member of administrator group");
			//	if (!groupsToAssign.isEmpty()) {
			//		for (String newGroups : groupsToAssign) {
			//			if (!existingGroupsOfUser.contains(newGroups)) {
			//				bambooUserManager.addMembership(newGroups, username);
			//			}
			//		}
			//	}
			//} else {
			//	LOGGER.debug("Not a Member of administrator group");

			if (idpConfig.getOnTheFlyGroupCreation()) {
//				if (roleValues != null && roleValues.length > 0) {
					List<String> onTheFlyGroupsToAssign = new ArrayList<>();
					List<String> doNotRemoveFromGroups = idpConfig.getOnTheFlyDoNotRemoveGroups();
					if (!BooleanUtils.toBoolean(idpConfig.getOnTheFlyAssignNewGroupsOnly())) {
						for (String group : existingGroupsOfUser) {
							if (!doNotRemoveFromGroups.contains(group)) {
								if (BooleanUtils.toBoolean(canBeUpdated)) {
									try {
										if (bambooPermissionManager.getAdminGroups().contains(group)) {
											LOGGER.debug(group + " has ADMINISTRATOR permission & checking for single admin ");
											LOGGER.debug("getAdminUsers().size() : "+getAdminUsers().size());
											if (getAdminUsers().size() > 1) {
												bambooUserManager.removeMembership(group, username);
												LOGGER.debug("User " + username + " is not a Single Admin so removed from group " + group);
											} else {
												LOGGER.debug("Unable to delete user with username "+username +" because he is the single admin present in the System.");
											}
										} else {
											bambooUserManager.removeMembership(group, username);
											LOGGER.debug("User " + username + " is removed from group " + group);
										}
									} catch (Exception e) {
										LOGGER.debug("Issue with removing user with name "+username+" from group "+group +" Reason:"+e);
									}
								}
							}
						}
					}
					LOGGER.debug("Assigning groups to user from SAML against attribute name.");
					onTheFlyGroupsToAssign = createAndAssignGroups(roleValuesList);
					for (String newGroups : onTheFlyGroupsToAssign) {
						if (BooleanUtils.toBoolean(canBeUpdated) && !isGroupAlreadyAssignedToUser(username, newGroups)) {
							try {
								bambooUserManager.addMembership(newGroups, username);
								LOGGER.debug("User "+username+" is added to group "+newGroups);
							} catch (Exception e) {
								LOGGER.debug("Issue with adding user with name "+username+" from group "+newGroups +" Reason:"+e);
							}
						}
					}
//				}
			} else {
				if (StringUtils.isNotBlank(idpConfig.getRoleAttribute())) {
					for (String newGroups : groupsToAssign) {
						if (!existingGroupsOfUser.contains(newGroups)) {
							if (BooleanUtils.toBoolean(canBeUpdated) && !isGroupAlreadyAssignedToUser(username, newGroups)) {
								try {
									bambooUserManager.addMembership(newGroups, username);
									LOGGER.debug("User "+username+" is added to group "+newGroups);
								} catch (Exception e) {
									LOGGER.debug("Issue with adding user with name "+username+" from group "+newGroups +" Reason:"+e);
								}
							}
						}
					}
					HashMap<String, String> roleMapping = idpConfig.getRoleMapping();

					for (String groups : existingGroupsOfUser) {
						if (!groupsToAssign.contains(groups)) {
							if (StringUtils.isNotEmpty(roleMapping.get(groups))) {
								if (BooleanUtils.toBoolean(canBeUpdated) && isGroupAlreadyAssignedToUser(username, groups)) {
									try {
										bambooUserManager.removeMembership(groups, username);
										LOGGER.debug("User "+username+" is removed to group "+groups);
									} catch (Exception e) {
										LOGGER.debug("Issue with removing user with name "+username+" from group "+groups +" Reason:"+e);
									}
								}
							}
						}
					}
				} else {
					LOGGER.debug("Group to Attribute is EMPTY");
				}
			}
		} else {
			LOGGER.debug("Update user groups is disabled.");
		}
		boolean addDefaultGroups = StringUtils.equals(idpConfig.getEnableDefaultGroupsFor(),
				MoPluginConstants.ENABLE_DEFAULT_GROUPS_FOR_ALL_USERS) || (StringUtils.equals(idpConfig.getEnableDefaultGroupsFor(),
				MoPluginConstants.ENABLE_DEFAULT_GROUPS_FOR_USERS_WITH_NO_IDP_GROUPS) && userWithNOIDPGroups == true);

		// Assigning default group to user
		if (BooleanUtils.toBoolean(addDefaultGroups)) {
			List<String> defaultGroup = new ArrayList<String>();
			List<String> defaultGroups = (List<String>) idpConfig.getDefaultGroupsList();
			if (defaultGroups != null && defaultGroups.size() > 0) {
				for (String group : defaultGroups) {
					if (BooleanUtils.toBoolean(canBeUpdated) && !isGroupAlreadyAssignedToUser(username, group)) {
						try {
							bambooUserManager.addMembership(group, username);
							LOGGER.debug("User "+username+" is added to group "+group);
						} catch (Exception e) {
							LOGGER.debug("Issue with adding user with name "+username+" from group "+group +" Reason:"+e);
						}
					}
				}
			} else {
				if (BooleanUtils.toBoolean(canBeUpdated) && !isGroupAlreadyAssignedToUser(username, idpConfig.getDefaultGroup())) {
					try {
						bambooUserManager.addMembership(idpConfig.getDefaultGroup(), username);
						LOGGER.debug("User "+username+" is added to group "+idpConfig.getDefaultGroup());
					} catch (Exception e) {
						LOGGER.debug("Issue with adding user with name "+username+" from group "+idpConfig.getDefaultGroup() +" Reason:"+e);
					}
				}
			}
		}

		return bambooUserManager.getBambooUser(username);
	}

	private Collection<String> getAdminUsers() {
		Set<String> adminUsers = new HashSet<>();
		adminUsers.addAll(bambooPermissionManager.getAdminUsers());
		adminUsers.addAll(bambooPermissionManager.getRestrictedAdminUsers());

		for (String groupName : bambooPermissionManager.getAdminGroups()) {
			List<String> users = bambooUserManager.getMemberNamesAsList(bambooUserManager.getGroup(groupName));
			LOGGER.debug("Users : "+users);
			adminUsers.addAll(users);
		}
		return adminUsers;
	}

	private boolean isGroupAlreadyAssignedToUser(String username, String groupName) {
		List<String> existingGroupsOfUser = new ArrayList<>();
		existingGroupsOfUser = bambooUserManager.getGroupNamesAsList(bambooUserManager.getBambooUser(username));
		return existingGroupsOfUser.contains(groupName);
	}

	private boolean canGroupBeUpdated(String username) {
		LOGGER.debug("canGroupBeUpdated called");
		if (settings.getCurrentBuildNumber() < 60604) {
			LOGGER.debug("Is Read Only : "+bambooUserManager.isReadOnly(bambooUserManager.getUser(username)));
			return !bambooUserManager.isReadOnly(bambooUserManager.getUser(username));
		} else {
			com.atlassian.crowd.embedded.api.CrowdService crowdService = ComponentLocator.getComponent(com.atlassian.crowd.embedded.api.CrowdService.class);
			com.atlassian.crowd.embedded.api.CrowdDirectoryService crowdDirectoryService = ComponentLocator.getComponent(com.atlassian.crowd.embedded.api.CrowdDirectoryService.class);
			com.atlassian.crowd.embedded.api.User user = crowdService.getUser(username);
			com.atlassian.crowd.embedded.api.Directory directory = crowdDirectoryService.findDirectoryById(user.getDirectoryId());
			return directory == null ? false : directory.getAllowedOperations().contains(com.atlassian.crowd.embedded.api.OperationType.UPDATE_GROUP);
		}
	}

	public BambooUserManager getBambooUserManager() {
		return bambooUserManager;
	}

	public void setBambooUserManager(BambooUserManager bambooUserManager) {
		this.bambooUserManager = bambooUserManager;
	}

	public void setSettings(MoSAMLSettings settings) {
		this.settings = settings;
	}

	public void setSamlManager(MoSAMLManager samlManager) {
		this.samlManager = samlManager;
	}

	public BambooPermissionManager getBambooPermissionManager() {
		return bambooPermissionManager;
	}

	public void setBambooPermissionManager(BambooPermissionManager bambooPermissionManager) {
		this.bambooPermissionManager = bambooPermissionManager;
	}


	private void redirect(HttpServletResponse response, String url) throws IOException {
		StringBuffer htmlStart = new StringBuffer(
				"<html><head><script>window.onload = " + "function() {window.location.href=\"" + url
						+ "\"};</script></head><body>Please " + "wait...</body></html>");
		response.setContentType("text/html");
		response.getOutputStream().write(htmlStart.toString().getBytes());
	}

	public String XmlFormatter(String xml) {

		try {
			final InputSource src = new InputSource(new StringReader(xml));
			final Node document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(src)
					.getDocumentElement();
			final Boolean keepDeclaration = Boolean.valueOf(xml.startsWith("<?xml"));

			// May need this:
			// System.setProperty(DOMImplementationRegistry.PROPERTY,"com.sun.org.apache.xerces.internal.dom.DOMImplementationSourceImpl");

			final DOMImplementationRegistry registry = DOMImplementationRegistry.newInstance();
			final DOMImplementationLS impl = (DOMImplementationLS) registry.getDOMImplementation("LS");
			final LSSerializer writer = impl.createLSSerializer();

			writer.getDomConfig().setParameter("format-pretty-print", Boolean.TRUE); // Set
																						// this
																						// to
																						// true
																						// if
																						// the
																						// output
																						// needs
																						// to
																						// be
																						// beautified.
			writer.getDomConfig().setParameter("xml-declaration", keepDeclaration); // Set
																					// this
																					// to
																					// true
																					// if
																					// the
																					// declaration
																					// is
																					// needed
																					// to
																					// be
																					// outputted.

			return writer.writeToString(document);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private void showTestConfigurationResult(MoSAMLResponse samlResponse, HttpServletRequest request, HttpServletResponse response, String setup,
			MoSAMLException e) throws IOException {
		response.setContentType("text/html");

		Boolean testConfigFailed = Boolean.FALSE;
		if (e == null) {
			String username = "";
			StringBuffer htmlStart = new StringBuffer("<div style=\"font-family:Calibri;padding:0 3%;\">");
			String[] usernameArray = samlResponse.getAttributes().get(idpConfig.getUsernameAttribute());
			if (usernameArray != null && usernameArray.length == 1) {
				username = usernameArray[0];
			}
			if (StringUtils.isBlank(username)) {
				htmlStart = htmlStart.append("<div style=\"color: #a94442;background-color: #f2dede;padding: 15px;"
						+ "margin-bottom: 20px;text-align:center;border:1px solid #E6B3B2;font-size:18pt;\">TEST "
						+ "FAILED</div><div style=\"color: #a94442;font-size:14pt; margin-bottom:20px;\">WARNING: Username "
						+ "attribute not found in the response. Users will not be able to login. [Please check Username attribute in User profile tab it should be similar to attribute Name in IDP.]</div>");
			} else {
				htmlStart = htmlStart.append("<div style=\"color: #3c763d;background-color: #dff0d8; padding:2%;"
						+ "margin-bottom:20px;text-align:center; border:1px solid #AEDB9A; font-size:18pt;\">TEST "
						+ "SUCCESSFUL</div>");
			}
			htmlStart = htmlStart.append("<span style=\"font-size:14pt;\"><b>Hello</b>, " + MoSAMLUtils.sanitizeText(username) + "</span><br/>"
					+ "<p style=\"font-weight:bold;font-size:14pt;margin-left:1%;\">ATTRIBUTES RECEIVED:</p>"
					+ "<table style=\"border-collapse:collapse;border-spacing:0; display:table;width:100%; "
					+ "font-size:14pt;background-color:#EDEDED;\"><tr style=\"text-align:center;\">"
					+ "<td style=\"font-weight:bold;border:2px solid #949090;padding:2%;\">ATTRIBUTE NAME</td>"
					+ "<td style=\"font-weight:bold;padding:2%;border:2px solid #949090; word-wrap:break-word;\">"
					+ "ATTRIBUTE VALUE</td></tr>");
			Iterator<String> it = samlResponse.getAttributes().keySet().iterator();
			while (it.hasNext()) {
				String key = it.next();
				htmlStart = htmlStart.append("<tr><td style=\"font-weight:bold;border:2px solid #949090;padding:2%;\">"
						+ MoSAMLUtils.sanitizeText(key) + "</td><td style=\"padding:2%;border:2px solid #949090;word-wrap:break-word;\">");

				String[] values = samlResponse.getAttributes().get(key);
				for (int i = 0; i < values.length; i++) {
					if (i == values.length - 1)
						htmlStart = htmlStart.append(StringUtils.join(MoSAMLUtils.sanitizeText(values[i]), "<br/>"));
					else
						htmlStart = htmlStart.append(StringUtils.join(MoSAMLUtils.sanitizeText(values[i]), "<hr/>"));
				}
				htmlStart = htmlStart.append("</td></tr>");
			}
			htmlStart = htmlStart.append("</table></div>");
			if(!StringUtils.equals(setup,"quicktestconfig")) {
				htmlStart = htmlStart
						.append("<div style=\"margin:3%;display:block;text-align:center;\"><input style=\"padding:1%;"
								+ "width:100px;background: #0091CD none repeat scroll 0% 0%;cursor: pointer;font-size:15px;"
								+ "border-width: 1px;border-style: solid;border-radius: 3px;white-space: nowrap;"
								+ "box-sizing:border-box;border-color: #0073AA;box-shadow:0px 1px 0px rgba(120,200,230,0.6) inset;"
								+ "color: #FFF;\" type=\"button\" value=\"Done\" onClick=\"self.close();\"></div>");
			}
			if (!StringUtils.equalsIgnoreCase(setup, "quick")) {
				response.setContentType("text/html");
				response.setCharacterEncoding("iso-8859-1");
				response.getOutputStream().write(htmlStart.toString().getBytes(StandardCharsets.UTF_8));
			}
		} else {
			testConfigFailed = Boolean.TRUE;
			StringBuffer htmlStart = new StringBuffer("<div style=\"font-family:Calibri;padding:0 3%;\">");
			htmlStart = htmlStart
					.append("<div style=\"color:#a94442;background-color:#f2dede;padding:15px;margin-bottom:20px;"
							+ "text-align:center;border:1px solid #E6B3B2;font-size:18pt;\">TEST FAILED</div>");
			htmlStart = htmlStart
					.append("<table style=\"border-collapse:collapse;border-spacing:0; display:table;width:100%;"
							+ "font-size:14pt;\"><tr style=\"padding-top:10px;padding-bottom:10px;\"><td style=\"font-weight:bold;"
							+ "padding:10px 5px 10px 5px;\">Error Code</td><td style=\"word-wrap:break-word;\">"
							+ e.getErrorCode()
							+ "</td></tr><tr><td style=\"font-weight:bold;padding:10px 5px 10px 5px;\">"
							+ "Error Message</td><td style=\"word-wrap:break-word;\">" + e.getMessage()
							+ "</td></tr><tr>"
							+ "<td style=\"font-weight:bold;padding:10px 5px 10px 5px;\">Resolution</td>"
							+ "<td style=\"word-wrap:break-word;\">" + e.getResolution() + "</tr></table></div>");
			if (!StringUtils.equalsIgnoreCase(setup, "quick")) {
				response.setContentType("text/html");
				response.setCharacterEncoding("iso-8859-1");
				response.getOutputStream().write(htmlStart.toString().getBytes(StandardCharsets.UTF_8));
			}
		}
		try {
			StringBuffer message = new StringBuffer();
			if (testConfigFailed){
				message = message.append("<div style=\"font-family:Calibri;padding:0 3%;\">");
				message = message.append("<div style=\"color:#a94442;background-color:#f2dede;padding:15px;margin-bottom:20px;"
						+ "text-align:center;border:1px solid #E6B3B2;font-size:18pt;\">TEST FAILED</div>");
				message = message.append("<table style=\"border-collapse:collapse;border-spacing:0; display:table;width:100%;"
						+ "font-size:14pt;\"><tr style=\"padding-top:10px;padding-bottom:10px;\"><td style=\"font-weight:bold;"
						+ "padding:10px 5px 10px 5px;\">Error Code</td><td style=\"word-wrap:break-word;\">"
						+ e.getErrorCode()
						+ "</td></tr><tr><td style=\"font-weight:bold;padding:10px 5px 10px 5px;\">"
						+ "Error Message</td><td style=\"word-wrap:break-word;\">" + e.getMessage()
						+ "</td></tr><tr>"
						+ "<td style=\"font-weight:bold;padding:10px 5px 10px 5px;\">Resolution</td>"
						+ "<td style=\"word-wrap:break-word;\">" + e.getResolution() + "</tr></table></div>");
			}else {
				message = message.append("<div style=\"color: #3c763d;background-color: #dff0d8; padding:2%;"
						+ "margin-bottom:20px;text-align:center; border:1px solid #AEDB9A; font-size:18pt;\">Test "
						+ "complete. You may close this window.</div>");

				displaySamlRequest(request, response, idpConfig, setup);
				displaySamlResponse(request, response, idpConfig,setup);

				JSONObject idpConfigObject = settings.getIdpConfig(idpConfig.getId());
				idpConfigObject.put("testConfig",samlResponse.getAttributes());
				settings.setIdpConfig(idpConfigObject,idpConfig.getId());
			}
			if (StringUtils.equalsIgnoreCase(setup,"quick")) {
				response.setCharacterEncoding("iso-8859-1");
				response.setContentType("text/html");
				response.getOutputStream().write(message.toString().getBytes(StandardCharsets.UTF_8));
			}
		}catch (Exception ie){
			LOGGER.error("Error while getting saml request or response");
		}
	}
	public static String getGroupnameFromRegexMethod(String groupRegexPattern,String regexGroups,String groupName){
		LOGGER.debug("Applying regex pattern on Groupname : "+groupName);
		Pattern pattern = Pattern.compile(StringUtils.trimToEmpty(groupRegexPattern));
		Matcher matcher = pattern.matcher(groupName);
		if (matcher.find()) {
			groupName = StringUtils.EMPTY;
			LOGGER.debug("Matched Groups "+matcher.groupCount());
			if (matcher.groupCount() > 0) {
				groupName=MoPluginHandler.getGroupNameFromRegex(matcher,StringUtils.trimToEmpty(regexGroups));
			} else {
				groupName=matcher.group();
			}
		}
		return groupName;
	}

	private String getIdpId(HttpServletRequest request, HttpServletResponse response) {
		String idpId = "";

		//Checking the Session for SP Initiated. Doesn't work if the Lax or Strict cookie is set
		HttpSession session = request.getSession();
		idpId = (String) session.getAttribute("idpId");
		if(StringUtils.isNotBlank(idpId)) {
			session.removeAttribute("idpId");
			return idpId;
		}

		//Checking the Cookie for SP Initiated. Works when Lax or Strict cookie is set but doesn't work if the URL is http
		Cookie idpIdCookie = MoHttpUtils.getCookie(request, MoPluginConstants.IDP_ID_COOKIE);
		if(idpIdCookie!=null){
			idpId = idpIdCookie.getValue();
			if(StringUtils.isNotBlank(idpId)){
				MoHttpUtils.clearCookie(request,response, MoPluginConstants.IDP_ID_COOKIE);
				return idpId;
			}
		}

		//Checking parameter in ACS URL for IDP Initiated SSO
		idpId = request.getParameter("idp");
		if(StringUtils.isNotBlank(idpId)) {
			return idpId;
		}

		// Checking Issuer if nothing else works. Doesn't work if there are multiple IDPs with same issuer like multiple Azure or ADFS
		String issuer = MoSAMLManager.getIssuerFromResponse(request);
		int issuerCount = 0; //To check whether multiple IDPs have the same issuer. In that case issuer can't be used to identify the IDP
		for (Map.Entry<String, String> issuerEntry : settings.getIssuerMap().entrySet()) {
			if (StringUtils.equals(issuer, issuerEntry.getValue())) {
				idpId = issuerEntry.getKey();
				issuerCount++;
			}
		}

		if (issuerCount > 1)
			return null;

		return idpId;
	}

	private Boolean isRegexValid(String regex) {
		try {
			Pattern.compile(regex);
		} catch (PatternSyntaxException e) {
			LOGGER.debug("Invalid Regex Pattern.");
			return Boolean.FALSE;
		}
		return Boolean.TRUE;
	}
}
