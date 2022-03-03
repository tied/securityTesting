/*
 * Receives the callback response from the Authorize endpoint of the OAuth Provider.
 * URL: base url + /plugins/servlet/oauth/callback
 *
 * The Authorization response is received here - with the code from the OAuth Provider.
 *
 * The Access Token request + Code -> access token endpoint of the OAuth provider.
 * The AccessToken response is received with the Access Token.
 *
 * The Get User Info request + Access Token -> Get user info endpoint of the OAuth provider.
 * The Get User info response is received with the User details.
 *
 * The customer is redirected to the home page after this.
 *
 */
package com.miniorange.oauth.bamboo.servlet;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.*;

import com.atlassian.crowd.embedded.api.CrowdService;
import com.atlassian.crowd.embedded.api.CrowdDirectoryService;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.crowd.embedded.api.Directory;
import com.atlassian.crowd.embedded.api.OperationType;
import com.atlassian.json.jsonorg.JSONArray;
import com.atlassian.templaterenderer.TemplateRenderer;
import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.google.gson.*;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import com.atlassian.seraph.service.rememberme.RememberMeService;
import com.atlassian.bamboo.security.BambooPermissionManager;
import com.atlassian.bamboo.user.BambooUser;
import com.atlassian.bamboo.user.BambooUserManager;
import com.atlassian.sal.api.component.ComponentLocator;
import com.atlassian.seraph.auth.Authenticator;
import com.atlassian.seraph.auth.AuthenticatorException;
import com.atlassian.seraph.auth.DefaultAuthenticator;
import com.atlassian.seraph.config.SecurityConfigFactory;
import com.atlassian.user.Group;
import com.atlassian.user.search.SearchResult;
import com.atlassian.user.search.page.Pager;
import com.miniorange.oauth.bamboo.MoOAuthManager;
import com.miniorange.oauth.bamboo.MoOAuthPluginConstants;
import com.miniorange.oauth.bamboo.MoOAuthSettings;
import com.miniorange.oauth.bamboo.MoOAuthUserManager;
import com.miniorange.oauth.bamboo.dto.TokenResponse;
import com.miniorange.oauth.bamboo.factory.IProtocolAction;
import com.miniorange.oauth.bamboo.factory.OAuthOpenIdDecisionHandler;
import com.miniorange.oauth.utils.MoOAuthHttpUtils;
import com.miniorange.oauth.utils.MoOAuthUtils;

import static com.miniorange.oauth.bamboo.servlet.MoOAuthLoginServlet.getGroupnameFromRegexMethod;


public class MoOAuthCallbackServlet extends HttpServlet {

	private static Log LOGGER = LogFactory.getLog(MoOAuthCallbackServlet.class);

	private MoOAuthSettings settings;
	private BambooUserManager bambooUserManager;
	private MoOAuthManager oauthManager;
	private MoOAuthUserManager userManager;
	private MoOAuthUtils oauthUtils;
	private BambooPermissionManager bambooPermissionManager;
	private Gson gson = new Gson();
	private JsonParser parser = new JsonParser();
	private String accessToken = StringUtils.EMPTY;
	private HashMap<String, String> userInfoMap = new HashMap<>();
	private String refreshToken = StringUtils.EMPTY;
	private String googleRefreshToken = StringUtils.EMPTY;
	private IProtocolAction protocolAction;
	private TemplateRenderer renderer;
    static HttpServletRequest reqObjectForRememberMe;
    static HttpServletResponse respObjectForRememberMe;

	public MoOAuthCallbackServlet(MoOAuthSettings settings, BambooUserManager bambooUserManager,
								  BambooPermissionManager bambooPermissionManager,
								  MoOAuthManager oauthManager, MoOAuthUserManager userManager, MoOAuthUtils oauthUtils, TemplateRenderer renderer) {
		this.settings = settings;
		this.bambooUserManager = bambooUserManager;
		this.bambooPermissionManager = bambooPermissionManager;
		this.userManager = userManager;
		this.oauthManager = oauthManager;
		this.oauthUtils = oauthUtils;
		this.renderer = renderer;
	}

	/* Callback Servlet GET Call */
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) {
		try {
			LOGGER.debug("Executing OAuth Authentication Callback Servlet");
			LOGGER.debug("Code received : "+request.getParameter("code"));

			if (StringUtils.isBlank(refreshToken)) {
				refreshToken = settings.getRefreshToken();
			}

			LOGGER.debug("Refresh Token : "+refreshToken);

			protocolAction = OAuthOpenIdDecisionHandler.getProtocolHandler(settings.getAppName());
			/* Test Configuration */
			if (settings.isTestIDPConfigurationInUse(request)) {
				LOGGER.debug("TestIDPConfigurationInUse::"+settings.isTestIDPConfigurationInUse(request));
				MoOAuthHttpUtils.removeCookie("test_configuration_in_use", request, response);
				LOGGER.debug("Rendering Test Configuration");
				showTestConfigurationResult(request, response, null);
				return;
			}

			/* When Verify Credentials Button is Clicked (Only for Google) */
			if (settings.isVerifyCredentialsInUse(request)) {
				MoOAuthHttpUtils.removeCookie("verify_credentials_in_use", request, response);
				LOGGER.debug("Initiating credential verification for Google Apps");
				showVerifyCredentials(request, response);
				return;
			}

			if (BooleanUtils.toBoolean(settings.getUseStateParameter())) {
				String stateParameterToServer = getSessionAttribute("state_attribute_parameter", request);
				String stateParameterFromServer = request.getParameter("state");
				if (stateParameterFromServer == null || stateParameterFromServer.equalsIgnoreCase("")) {
					LOGGER.error("No State Parameter in the response. State parameter validation failed. redirecting to login page");
					redirectToLoginWithOAuthError(response, null, "cant_signin_no_state_parameter_found");
					return;
				}

				if (!StringUtils.equalsIgnoreCase(stateParameterToServer, stateParameterFromServer)) {
					LOGGER.error("State parameter validation failed. redirecting to login page");
					redirectToLoginWithOAuthError(response, null, "cant_signin_invalid_state_parameter");
					return;
				}
				LOGGER.debug("State Parameter is validated successfully");
			}

			/* Check License */
			if (!settings.isLicenseDefine()) {
				LOGGER.error("The plugin is not licensed. Redirecting user to login page");
				redirectToLoginWithOAuthError(response, null, "cant_signin_no_license");
				return;
			}

			TokenResponse tokenResponse;

			String accessTokenResponse = MoOAuthUserManager.getAndSetAccessAndRefreshToken(request, protocolAction);

			if(accessTokenResponse.equals("invalid_token")){
				LOGGER.error("Invalid Access Token. Redirecting to the login page ");
				redirectToLoginWithOAuthError(response, null, "invalid signature");
				return;
			}

			/* Access Token Response error */
			if (StringUtils.isEmpty(accessTokenResponse) || StringUtils.isBlank(accessTokenResponse)
					|| accessTokenResponse.equals("Failed") || accessTokenResponse.equals("error")) {
				LOGGER.debug("Error with the AccessTokenEndpoint");
				redirectToLoginWithOAuthError(response, null, "Error with the AccessTokenEndpoint");
				return;
			}

			/* Convert Access Token Response String to JSONObject */
			JsonObject accessTokenEndpointData = parser.parse(accessTokenResponse).getAsJsonObject();
			LOGGER.debug("Access Token response " + accessTokenEndpointData.toString());

			/* If the AccessToken Response has any error */
			if (accessTokenEndpointData.has("error")) {
				LOGGER.error("Invalid Access Token Response");
				redirectToLoginWithOAuthError(response, null, "invalid configuration parameters");
				return;
			}

			String accessToken = StringUtils.EMPTY;
			if (MoOAuthUtils.isOpenIdProtocol(settings.getAppName())) {
				if (StringUtils.isNoneEmpty(settings.getPublicKey())) {
					LOGGER.debug("Validating Signature in the JWT Token(ID Token)");
					accessToken = accessTokenEndpointData.get("id_token").getAsString();
					//String[] id_token_parts = accessToken.split("\\.");
					//Boolean isValid = MoOAuthUserManager.verifyToken(id_token_parts);
					LOGGER.debug("ID Token = " + accessToken);
					DecodedJWT jwt = JWT.decode(accessToken);
					String algorithm = jwt.getAlgorithm();
					PublicKey publicKey = userManager.getPublicKeyObjectFromConfiguredKey(settings.getPublicKey());
					Boolean isValid = userManager.verifyTokenSignature(jwt, publicKey, algorithm);
					if (!BooleanUtils.toBoolean(isValid)) {
						LOGGER.error("Signature Validation Failed!");
						redirectToLoginWithOAuthError(response, null, "invalid signature");
						return;
					}
				}
			}

			/* Retrieving access token */
			this.accessToken = accessTokenEndpointData.get("access_token").getAsString();

			// settings.setAccessToken(accessToken);
			tokenResponse = protocolAction.sendUserInfoRequest(accessTokenResponse, settings);
			String userDetailedInfoResponse = tokenResponse.getResponse();

			if (userDetailedInfoResponse == "Failed") {
				LOGGER.error("UserInfoResponse Failed!");
				redirectToLoginWithOAuthError(response, null, "invalid userDetailedInfo request URL");
				return;
			}

			/* If User Info is fetched successfully */
			JsonObject getUserInfoData = parser.parse(userDetailedInfoResponse).getAsJsonObject();
			HashMap<String, Object> userDetailsMap = new HashMap<>();

			this.userInfoMap = oauthUtils
					.copyToStringValueMap(oauthUtils.toMapObjects(getUserInfoData, userDetailsMap));
			LOGGER.debug("User Info Map: " + userInfoMap);
			// settings.setUserInfoMap(userInfoMap);
			
			if(settings.getNonceCheck() && MoOAuthUtils.isOpenIdProtocol(settings.getAppName())) {
				LOGGER.debug("Validating Nonce Parameter");
				String nonceValueFromServer= userInfoMap.get("nonce"); 
				String nonceValueToServer = getSessionAttribute("nonce", request);
				LOGGER.debug("Nonce parameter sent : "+nonceValueToServer+" Nonce received in the response : "+nonceValueFromServer);
				if (!nonceValueFromServer.equals(nonceValueToServer)||nonceValueFromServer==null) { 
					LOGGER.debug("Nonce Parameter validation failed.");
					redirectToLoginWithOAuthError(response, null, "cant_signin_invalid_nonce_found"); 
					return; 
				} 
			} 
 

			Set<String> emails = oauthUtils.findEmails(userInfoMap);
			String email = StringUtils.EMPTY;
			if (emails.size() > 0) {
				LOGGER.debug("Searching for email in the response");
				List<String> list = new ArrayList<String>(emails);
				email = list.get(0);
				LOGGER.debug("User email: " + email);
			}

			/*
			 * Because Meetup OAuth response does not return email in any of the attribute
			 * (id is considered as unique identifier)
			 */
			if (StringUtils.equals(settings.getAppName(), MoOAuthPluginConstants.MEETUP)) {
				String meetupid = oauthUtils.findKey(getUserInfoData, "id", settings.getAppName());
				email = (new StringBuilder()).append(meetupid).append("@meetup.com").toString();
			}

			LOGGER.debug("email: " + email);

			/* If no email is returned, Redirect the user to login page with an error */
			if (StringUtils.isEmpty(settings.getUsernameAttribute()) && StringUtils.isEmpty(settings.getEmailAttribute())&& email.isEmpty()) {
				LOGGER.error("No user identifier attributes is configured. Unable to identify SSO User. Please check the user profile configuration in the plugin !");
				redirectToLoginWithOAuthError(response, null, "cant_signin_check_configuration");
				return;
			}

			/* Authorise the user and redirect to dashboard page. */
			authoriseAndRedirect(getUserInfoData, request, response, email, settings.getRegexPatternEnabled(),
					this.accessToken);

			return;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		super.doPost(req, resp);
		String refreshToken = req.getParameter("refresh_token");

		if (refreshToken != null) {
			if (StringUtils.equals(settings.getAppName(), MoOAuthPluginConstants.GOOGLE)) {
				settings.setGoogleRefreshToken(refreshToken);
			} else {
				settings.setRefreshToken(refreshToken);
			}
		}
		return;
	}

	/* This is the function for showing Test Configuration Pop-up Window */
	private void showTestConfigurationResult(HttpServletRequest request, HttpServletResponse response, String error)
			throws IOException {

		StringBuffer htmlStart = new StringBuffer("<head><style>table{font-family: arial, sans-serif;border-collapse: "
				+ "collapse;width: 100%;}td, th {border: 1px solid #dddddd;text-align: left;padding: 8px;}"
				+ "tr:nth-child(even){background-color: #dddddd;}</style></head><div style=\"font-family:Calibri;padding:0 3%;\">");

		try {
			if (error == null) {
				JsonObject getUserInfoData;
				// LOGGER.debug("Inside showTestConfigurationResult");

				String email = StringUtils.EMPTY;
				String id = StringUtils.EMPTY;
				String userInfoResult = MoOAuthUserManager.fetchUserInfo(request, protocolAction);

				LOGGER.debug(userInfoResult);
				if (userInfoResult.equals("invalid_client")) {
					htmlStart = htmlStart.append("<div style=\"padding: 10px;"
							+ "margin-bottom: 30px;text-align:center;font-size:18pt;font-family=Courier New\">Oops! Something went wrong! "
							+ "</div><div style=\"padding: 15px;"
							+ "margin-bottom: 30px;text-align:left;font-family=Courier New\">"
							+ "<b style='font-size:17pt'>Possible Cause:</b><hr style=\"border: 1px solid #E6B3B2; \" class=\"header\" /> "
							+ "<p style='font-size:15pt;'> Error with the unauthorized invalid client."
					        + " Please check if the user is valid and the entered values are correct."
							+ " </br> </br> If you need any help, please <a href=\"https://miniorange.atlassian.net/servicedesk/customer/portal/2\"> "
							+ "click here</a> to reach out to us.</p></div>");
					response.setContentType("text/html");
					response.getOutputStream().write(htmlStart.toString().getBytes("UTF-8"));
					return;
				} else if (userInfoResult.equals("error")) {
					htmlStart = htmlStart.append("<div style=\"padding: 10px;"
							+ "margin-bottom: 30px;text-align:center;font-size:18pt;font-family=Courier New\">Oops! Something went wrong! "
							+ "</div><div style=\"padding: 15px;"
							+ "margin-bottom: 30px;text-align:left;font-family=Courier New\">"
							+ "<b style='font-size:17pt'>Possible Cause:</b><hr style=\"border: 1px solid #E6B3B2; \" class=\"header\" /> "
							+ "<p style='font-size:15pt;'> Error with the AccessTokenEndpoint / Client Secret."
							+ " Please check if the Access Token Endpoint is correct."
							+ " </br> </br> If you need any help, please <a href=\"https://miniorange.atlassian.net/servicedesk/customer/portal/2\"> "
							+ "click here</a> to reach out to us.</p></div>");
					response.setContentType("text/html");
					response.getOutputStream().write(htmlStart.toString().getBytes());
					return;
				} else if (userInfoResult.equals("Failed")) {
					htmlStart = htmlStart.append("<div style=\"padding: 10px;"
							+ "margin-bottom: 30px;text-align:center;font-size:18pt;font-family=Courier New\">Oops! Something went wrong! "
							+ "</div><div style=\"padding: 15px;"
							+ "margin-bottom: 30px;text-align:left;font-family=Courier New\">"
							+ "<b style='font-size:17pt'>Possible Cause:</b><hr style=\"border: 1px solid #E6B3B2; \" class=\"header\" /> "
							+ "<p style='font-size:15pt;'> An unexpected error occured in response."
							+ " Please check your configurations."
							+ " </br> If you need any help, please <a href=\"https://miniorange.atlassian.net/servicedesk/customer/portal/2\"> "
							+ "click here</a> to reach out to us.</p></div>");
					response.setContentType("text/html");
					response.getOutputStream().write(htmlStart.toString().getBytes());
					return;
				} else if (userInfoResult.equals("invalid_request")) {
					htmlStart = htmlStart.append("<div style=\"padding: 10px;"
							+ "margin-bottom: 30px;text-align:center;font-size:18pt;font-family=Courier New\">Oops! Something went wrong! "
							+ "</div><div style=\"padding: 15px;"
							+ "margin-bottom: 30px;text-align:left;font-family=Courier New\">"
							+ "<b style='font-size:17pt'>Possible Cause:</b><hr style=\"border: 1px solid #E6B3B2; \" class=\"header\" /> "
							+ "<p style='font-size:15pt;'> Error with the plugin configurations. "
							+ " Please check if the configuration done in plugin and scope entered  is correct."
							+ " </br> If you need any help, please <a href=\"https://miniorange.atlassian.net/servicedesk/customer/portal/2\"> "
							+ "click here</a> to reach out to us.</p></div>");
					response.setContentType("text/html");
					response.getOutputStream().write(htmlStart.toString().getBytes());
					return;
				} else if (userInfoResult.equals("invalid_token")) {
					htmlStart = htmlStart.append("<div style=\"padding: 10px;"
							+ "margin-bottom: 30px;text-align:center;font-size:18pt;font-family=Courier New\">Oops! Something went wrong! "
							+ "</div><div style=\"padding: 15px;"
							+ "margin-bottom: 30px;text-align:left;font-family=Courier New\">"
							+ "<b style='font-size:17pt'>Possible Cause:</b><hr style=\"border: 1px solid #E6B3B2; \" class=\"header\" /> "
							+ "<p style='font-size:15pt;'> Invalid Issuer or Public key found in response"
							+ " Please check if Issuer or Public Key is correct."
							+ " </br> If you need any help, please <a href=\"https://miniorange.atlassian.net/servicedesk/customer/portal/2\"> "
							+ "click here</a> to reach out to us.</p></div>");
					response.setContentType("text/html");
					response.getOutputStream().write(htmlStart.toString().getBytes("UTF-8"));
					return;
				} else {

					getUserInfoData = parser.parse(userInfoResult).getAsJsonObject();
					LOGGER.debug("userInfoData " + getUserInfoData);

					if (getUserInfoData.has("error")) {
						LOGGER.error("invalid userDetailedInfo request URL");
						htmlStart = htmlStart.append("<div style=\"padding: 15px;"
								+ "margin-bottom: 30px;text-align:center;font-size:18pt;font-family=Courier New\">Oops! Something went wrong! "
								+ "</div><div style=\"border:1px solid #E6B3B2;padding: 15px;"
								+ "margin-bottom: 30px;text-align:left;font-family=Courier New\">"
								+ "<b style='font-size:17pt'>Possible Cause:</b>"
								+ "<hr style=\"border: 1px solid #E6B3B2; \" class=\"header\" />"
								+ " <p style='font-size:15pt'> Error with the User Info response."
								+ " Please check if the User Info Endpoint is correct."
								+ " </br> If you need any help, please <a href=\"https://miniorange.atlassian.net/servicedesk/customer/portal/2\"> "
								+ "click here</a> to reach out to us.</p></div>");
						response.setContentType("text/html");
						response.getOutputStream().write(htmlStart.toString().getBytes());
					}

					HashMap<String, Object> userDetailsMap = new HashMap<String, Object>();
					HashMap<String, String> userInfoMap = new HashMap<String, String>();
					userInfoMap = oauthUtils
							.copyToStringValueMap(oauthUtils.toMapObjects(getUserInfoData, userDetailsMap));
					LOGGER.debug("User Info Map: " + userInfoMap);
					Set<String> emails = oauthUtils.findEmails(userInfoMap);
					LOGGER.debug("User emails are " + emails);

					if (emails.size() > 0) {
						List<String> list = new ArrayList<String>(emails);
						email = list.get(0);
						LOGGER.debug("User email is " + email);
					}

					LOGGER.debug("Test successfully completed");
					htmlStart = htmlStart.append("<div style=\"color: #3c763d;background-color: #dff0d8; padding:2%;"
							+ "margin-bottom:20px;text-align:center; border:1px solid #AEDB9A; font-size:18pt;\">TEST "
							+ "SUCCESSFUL</div>");

					htmlStart = htmlStart.append(
							"<div style=\"text-align:left;\"><h2><b>User Attributes</b></h2></div><div style=\"padding:20px;vertical-align:middle; text-align:center\"><table>"
									+ "<tr style=\"text-align:center;\">"
									+ "<td style=\"font-weight:bold;border:2px solid #949090;padding:2%;\">ATTRIBUTE NAME</td>"
									+ "<td style=\"font-weight:bold;padding:2%;border:2px solid #949090; word-wrap:break-word;\">"
									+ "ATTRIBUTE VALUE</td></tr>");

					/* Facebook User Info Endpoint requires id of the user to Fetch Info */
					if (userInfoMap.containsKey("id"))
						id = userInfoMap.get("id");

					Iterator it = userInfoMap.entrySet().iterator();
					while (it.hasNext()) {
						Map.Entry pair = (Map.Entry) it.next();
						htmlStart = htmlStart.append("<tr>");
						htmlStart = htmlStart
								.append("<td style=\"font-weight:bold;border:2px solid #949090;padding:2%;\">"
										+ (String) pair.getKey() + "</td>");
						htmlStart = htmlStart
								.append("<td style=\"padding:2%;border:2px solid #949090;word-wrap:break-word;\">"
										+ (String) pair.getValue() + "</td>");
						htmlStart = htmlStart.append("</tr>");
						it.remove();
					}

					htmlStart = htmlStart.append("</table></div>");
				}

				String groupInfoResult = StringUtils.EMPTY;
				if (!MoOAuthUtils.isOpenIdProtocol(settings.getAppName())) {
					groupInfoResult = MoOAuthUserManager.fetchGroupInfo(getUserInfoData, request, email, id, settings,
							protocolAction, oauthUtils);
				}

				if (groupInfoResult.equals("Failed")) {
					this.refreshToken = "";
					sendRequestToSaveToken(StringUtils.EMPTY);
					htmlStart = htmlStart
							.append("<div style=\"text-align:left;\"><h2>Unable to fetch Groups Info! </h2></div>");
					if (settings.getAppName().equals(MoOAuthPluginConstants.GOOGLE)) {
						htmlStart = htmlStart.append(
								"<div style=\"text-align:left;\"><h2>Please verify your administrator credentials "
										+ "for fetching Groups of users.</h2></div>");
					}
				} else if (StringUtils.isNotEmpty(groupInfoResult)) {
					htmlStart = htmlStart
							.append("<div style=\"text-align:left;\"><h2><b>User Group Attributes</b></h2></div>");
					htmlStart = htmlStart.append(
							"<div style=\"padding:20px;vertical-align:middle; text-align:center\"><table><tr style=\"text-align:center;\">"
									+ "<td style=\"font-weight:bold;border:2px solid #949090;padding:2%;\">ATTRIBUTE NAME</td>"
									+ "<td style=\"font-weight:bold;padding:2%;border:2px solid #949090; word-wrap:break-word;\">"
									+ "ATTRIBUTE VALUE</td></tr>");

					/* Convert String to JSONArray */
					if (!(groupInfoResult.startsWith("[")))
						groupInfoResult = "[" + groupInfoResult + "]";

					JsonArray jsonArray = parser.parse(groupInfoResult).getAsJsonArray();

					if (StringUtils.equals(settings.getAppName(), MoOAuthPluginConstants.MEETUP)) {
						htmlStart = htmlStart.append("<tr>");
						htmlStart = htmlStart.append(
								"<td style=\"font-weight:bold;border:2px solid #949090;padding:2%;\">name</td><td style=\"padding:2%;border:2px solid #949090;word-wrap:break-word;\">");
					}
					/* Iterate each JSONObject in an JSONArray */
					for (int groupIndex = 0; groupIndex < jsonArray.size(); groupIndex++) {

						/*JsonObject getGroupUserInfoResponse = jsonArray.get(groupIndex).getAsJsonObject();

						LOGGER.debug("Group Info Response: " + getGroupUserInfoResponse);
						Map<String, JsonElement> groupDetailsMap = oauthUtils.jsonToMap(getGroupUserInfoResponse);
						LOGGER.debug("Group Details Map: " + groupDetailsMap);*/

						Map<String, JsonElement> groupDetailsMap = new HashMap<>();
						try {
							JsonObject getGroupUserInfoResponse = jsonArray.get(groupIndex).getAsJsonObject();
							LOGGER.debug("Group Info Response: " + getGroupUserInfoResponse);
							groupDetailsMap = oauthUtils.jsonToMap(getGroupUserInfoResponse);
						} catch (JsonParseException e) {
							JsonElement group = jsonArray.get(groupIndex);
							groupDetailsMap.put(settings.getRoleAttribute(), group);
						}
						LOGGER.debug("Group Details Map: " + groupDetailsMap);

						if (StringUtils.equals(settings.getAppName(), MoOAuthPluginConstants.MEETUP)) {
							if (groupDetailsMap.containsKey("name")) {
								htmlStart = htmlStart.append(groupDetailsMap.get("name").getAsString() + "</br><hr/>");
							}
						} else {
							for (Map.Entry<String, JsonElement> entry : groupDetailsMap.entrySet()) {
								String key = entry.getKey();
								//JsonElement value = entry.getValue();
								Object value = null;
								if (entry.getValue().isJsonNull()) {
									value = entry.getValue();
								} else {
									try {
										value = entry.getValue().getAsString();
									} catch (UnsupportedOperationException e) {
										value = entry.getValue();
									} catch (IllegalStateException i) {
										value = entry.getValue();
									}
								}
								htmlStart = htmlStart.append("<tr>");
								htmlStart = htmlStart
										.append("<td style=\"font-weight:bold;border:2px solid #949090;padding:2%;\">"
												+ key + "</td>");
								if (value instanceof JsonArray) {
									LOGGER.debug("JSONArray Found!!!!!!");
									htmlStart = htmlStart.append(
											"<td style=\"padding:2%;border:2px solid #949090;word-wrap:break-word;\"></td></tr>");

									//JsonArray array = value.getAsJsonArray();
									JsonArray array = (JsonArray) value;
									for (int index = 0; index < array.size(); index++) {
										htmlStart = htmlStart.append("<tr style=\"height: 10px !important;\"></tr>");
										JsonObject object = ((JsonArray)value).get(index).getAsJsonObject();
										Map<String, JsonElement> nestedMap = oauthUtils.jsonToMap(object);
										for (Map.Entry<String, JsonElement> nestedEntry : nestedMap.entrySet()) {
											String nestedKey = nestedEntry.getKey();
											Object nestedValue = nestedEntry.getValue();
											/*String nestedValueStr = "";
											LOGGER.debug("nestedKey : "+nestedKey +" nestedValue :  "+nestedValue );
											if(!nestedValue.isJsonNull() && !nestedValue.isJsonArray()){
												nestedValueStr = nestedValue.getAsString();
											}*/
											htmlStart = htmlStart.append("<tr>");
											htmlStart = htmlStart.append(
													"<td style=\"font-weight:bold;border:2px solid #949090; padding:2%; text-align:right;\">"
															+ nestedKey + "</td>");
											htmlStart = htmlStart.append(
													"<td style=\"padding:2%;border:2px solid #949090;word-wrap:break-word;\">"
															+ nestedValue + "</td>");
											htmlStart = htmlStart.append("</tr>");
										}
									}

									htmlStart = htmlStart.append("<tr style=\"height: 10px !important;\"></tr>");

								} else {
									if (value != null) {
										htmlStart = htmlStart.append(
												"<td style=\"padding:2%;border:2px solid #949090;word-wrap:break-word;\">"
														+ value.toString().replaceAll("^\"|\"$", "") + "</td>");
									} else {
										htmlStart = htmlStart.append(
												"<td style=\"padding:2%;border:2px solid #949090;word-wrap:break-word;\">"
														+ value + "</td>");
									}
									htmlStart = htmlStart.append("</tr>");
								}
							}
						}
					}
					if (StringUtils.equals(settings.getAppName(), MoOAuthPluginConstants.MEETUP)) {
						htmlStart = htmlStart.append("</td></tr>");
					}
					htmlStart = htmlStart.append("</table>");
					htmlStart = htmlStart.append("</div>");
					htmlStart = htmlStart.append(
							"<div style=\"margin:3%;display:block;text-align:center;\"><input style=\"padding:1%;"
									+ "width:100px;background: #0091CD none repeat scroll 0% 0%;cursor: pointer;font-size:15px;"
									+ "border-width: 1px;border-style: solid;border-radius: 3px;white-space: nowrap;"
									+ "box-sizing:border-box;border-color: #0073AA;box-shadow:0px 1px 0px rgba(120,200,230,0.6) inset;"
									+ "color: #FFF;\" type=\"button\" value=\"Done\" onClick=\"self.close();\"></div>");
				}
				response.setContentType("text/html");
				response.getOutputStream().write(htmlStart.toString().getBytes());
			}
		} catch (Exception e) {
			e.printStackTrace();
			htmlStart = htmlStart.append("<div style=\"padding: 10px;"
					+ "margin-bottom: 30px;text-align:center;font-size:18pt;font-family=Courier New\">Oops! Something went wrong! "
					+ "</div><div style=\"padding: 15px;"
					+ "margin-bottom: 30px;text-align:left;font-family=Courier New\">"
					+ "<b style='font-size:17pt'>Possible Cause:</b><hr style=\"border: 1px solid #E6B3B2; \" class=\"header\" /> "
					+ "<p style='font-size:15pt;'> An unexpected error occured in response."
					+ " Please check your configurations."
					+ " </br> If you need any help, please <a href=\"https://miniorange.atlassian.net/servicedesk/customer/portal/2\"> "
					+ "click here</a> to reach out to us.</p></div>");
			response.setContentType("text/html");
			response.getOutputStream().write(htmlStart.toString().getBytes("UTF-8"));
			return;
		}
	}

	/*
	 * This is the function for showing Verify Credentials pop-up window (Only for
	 * Google)
	 */
	private void showVerifyCredentials(HttpServletRequest request, HttpServletResponse response) throws IOException {
		LOGGER.debug("showVerifyCredentials function!");

		StringBuffer htmlStart = new StringBuffer("<head><style>table{font-family: arial, sans-serif;border-collapse: "
				+ "collapse;width: 100%;}td, th {border: 1px solid #dddddd;text-align: left;padding: 8px;}"
				+ "tr:nth-child(even){background-color: #dddddd;}</style></head><div style=\"font-family:Calibri;padding:0 3%;\">");

		// String refreshToken = settings.getRefreshToken();

		if (StringUtils.isEmpty(this.googleRefreshToken)) {
			this.googleRefreshToken = settings.getGoogleRefreshToken();
		}

		if (StringUtils.isEmpty(googleRefreshToken)) {

			String refreshTokenResponse = endpointCallToGetRefreshToken(request.getParameter("code"));
			LOGGER.debug("refreshTokenResponse is " + refreshTokenResponse);

			JsonObject refreshTokenData = parser.parse(refreshTokenResponse).getAsJsonObject();
			if (refreshTokenData.has("refresh_token")) {
				this.refreshToken = refreshTokenData.get("refresh_token").getAsString();
				sendRequestToSaveToken(this.refreshToken);
				LOGGER.debug("Saved token after verify " + settings.getGoogleRefreshToken());
				// settings.setIsCredentialsVerified(true);
				htmlStart = htmlStart.append("<div style=\"color: black; padding:2%;"
						+ "margin-bottom:20px;text-align:center; border:1px solid #AEDB9A; font-size:18pt;\">Thanks for verifying "
						+ "your account with the Administrator Account. Groups will be fetched for your users now. "
						+ "</div>");
			} else {
				htmlStart = htmlStart.append("<div style=\"color: black; padding:2%;"
						+ "margin-bottom:20px;text-align:left; border:1px solid #AEDB9A; font-size:16pt;\">Unable to Verify Admin Credentials."
						+ ". Please make sure you are verifying the credentials with the Administrator Account."
						+ "<br/><br/>If you signed in with an Admin Account and are still seeing this, please follow the below steps and try again."
						+ "<ol>"
						+ "<li>Login to <a href='https://www.google.com/settings/u/1/security'>Google Admin Console</a></li>"
						+ "<li>Navigate to Sign In & security >> Apps with Account Access >> Manage Access.</li>"
						+ "<li>Remove access for the App you are using to grant Google Groups permissions.</li>"
						+ "<li>Close this popup and Verify Credentials again.</li>" + "</ol>" + "</div>");
			}
		} else {
			htmlStart = htmlStart.append("<div style=\"color: black; padding:2%;"
					+ "margin-bottom:20px;text-align:center; border:1px solid #AEDB9A; font-size:18pt;\">You "
					+ "have already verified the credentials of the administrator account. "
					+ "Groups of users will be fetched from Google.</div>");
		}

		htmlStart = htmlStart
				.append("<div style=\"margin:3%;display:block;text-align:center;\"><input style=\"padding:1%;"
						+ "width:100px;background: #0091CD none repeat scroll 0% 0%;cursor: pointer;font-size:15px;"
						+ "border-width: 1px;border-style: solid;border-radius: 3px;white-space: nowrap;"
						+ "box-sizing:border-box;border-color: #0073AA;box-shadow:0px 1px 0px rgba(120,200,230,0.6) inset;"
						+ "color: #FFF;\" type=\"button\" value=\"Done\" onClick=\"self.close();\"></div>");

		response.setContentType("text/html");
		response.getOutputStream().write(htmlStart.toString().getBytes());
	}

	private void sendRequestToSaveToken(String refreshToken) {
		List<NameValuePair> postParameters = new ArrayList<>();
		postParameters.add(new BasicNameValuePair("action", "saveRefreshToken"));
		postParameters.add(new BasicNameValuePair("refresh_token", refreshToken));
		String Response = MoOAuthHttpUtils.sendPostRequest(settings.getCallBackUrl()+ settings.getCustomizableCallbackURL(), postParameters,
				"application/x-www-form-urlencoded", null);
		LOGGER.debug("Response " + Response);
	}

	/*
	 * This is the function to Get Refresh Token from Access token Endpoint (Only
	 * for Google Admin)
	 */
	private String endpointCallToGetRefreshToken(String code) {

		String ACCESSTOKEN_ENDPOINT = settings.getAccessTokenEndpoint();
		String REDIRECT_URI = settings.getBaseUrl().concat("/plugins/servlet/oauth/callback") + settings.getCustomizableCallbackURL();

		ArrayList<NameValuePair> postParameters = new ArrayList<NameValuePair>();

		postParameters.add(new BasicNameValuePair("redirect_uri", REDIRECT_URI));
		postParameters.add(new BasicNameValuePair("grant_type", "authorization_code"));
		postParameters.add(new BasicNameValuePair("client_id", settings.getClientID()));
		postParameters.add(new BasicNameValuePair("client_secret", settings.getClientSecret()));
		postParameters.add(new BasicNameValuePair("code", code));

		String Response = MoOAuthHttpUtils.sendPostRequest(ACCESSTOKEN_ENDPOINT, postParameters,
				"application/x-www-form-urlencoded", null);
		LOGGER.debug("response " + Response);

		return Response;
	}

	/* This is the function to Create or Update User at the time of log in. */
	private BambooUser tryCreateOrUpdateUser(HttpServletRequest request, String email, String userName,
			JsonObject getUserInfoData, String accessToken) {

		try {
			LOGGER.debug("Creating/Updating user.");
			String fullName = StringUtils.EMPTY;
			String firstName = StringUtils.EMPTY;
			String lastName = StringUtils.EMPTY;
			//String emailAttrValue = StringUtils.EMPTY;

			// HashMap<String, String> userInfoMap = settings.getUserInfoMap();
			//Duplicate Code
			/*if (StringUtils.isEmpty(userName) && userInfoMap.containsKey(settings.getUsernameAttribute())) {
				LOGGER.debug("Fetching Username...");
				userName = userInfoMap.get(settings.getUsernameAttribute());
			}
			LOGGER.debug("Username is:" + userName);
			*/

			LOGGER.debug("Fetching FullName...");
			if (settings.getUseSeparateNameAttributes()) {
				if (StringUtils.isNotEmpty(settings.getFirstNameAttribute())) {
					if (userInfoMap.containsKey(settings.getFirstNameAttribute())) {
						firstName = userInfoMap.get(settings.getFirstNameAttribute());
						LOGGER.debug("First Name fetched for the attribute " + settings.getFirstNameAttribute()
								+ " is......." + firstName);
					}
				}
				if (StringUtils.isNotEmpty(settings.getLastNameAttribute())) {
					if (userInfoMap.containsKey(settings.getLastNameAttribute())) {
						lastName = userInfoMap.get(settings.getLastNameAttribute());
						LOGGER.debug("Last Name  fetched for the attribute " + settings.getLastNameAttribute()
								+ " is......." + lastName);
					}
				}

				fullName = firstName + " " + lastName;

			} else {
				if (StringUtils.isNotEmpty(settings.getFullNameAttribute())) {
					if (userInfoMap.containsKey(settings.getFullNameAttribute())) {
						fullName = userInfoMap.get(settings.getFullNameAttribute());
						LOGGER.debug("FullName fetched......." + fullName);
					}
				}
			}

			//Duplicate Code
			/*LOGGER.debug("Fetching Email.....");
			if (StringUtils.isNotEmpty(settings.getEmailAttribute())) {
				if (userInfoMap.containsKey(settings.getEmailAttribute())) {
					emailAttrValue = userInfoMap.get(settings.getEmailAttribute());
					LOGGER.debug("Email Attr Value fetched......." + emailAttrValue);
				}
			}*/

//			if(getUserInfoData.has(settings.getEmailAttribute())) {
//				emailAttrValue = oauthUtils.findKey(getUserInfoData, settings.getEmailAttribute(), settings.getAppName());
//			}

			/* Id is required for Getting User from Facebook */
			String id = StringUtils.EMPTY;
			if (StringUtils.equals(settings.getAppName(), MoOAuthPluginConstants.FACEBOOK))
				id = getUserInfoData.get("id").getAsString();
			LOGGER.debug("id: " + id);

			String groupsResponse = StringUtils.EMPTY;
			groupsResponse = MoOAuthUserManager.fetchGroupInfo(getUserInfoData, request, email, id, settings,
					protocolAction, oauthUtils);

			if (groupsResponse.isEmpty() || StringUtils.equals(groupsResponse, "Failed")) {
				if (StringUtils.isNotEmpty(settings.getRoleAttribute())) {
					if (userInfoMap.containsKey(settings.getRoleAttribute())) {
						groupsResponse = userInfoMap.get(settings.getRoleAttribute());
						LOGGER.debug("Group Values fetched from user map......." + groupsResponse);
					}
				}
			}

			String groupValues = StringUtils.EMPTY;

			if (!(groupsResponse.isEmpty()) && !StringUtils.equals(groupsResponse, "Failed")) {
				LOGGER.debug("Group Response is: " + groupsResponse);
				if (!(groupsResponse.startsWith("["))) {
					groupsResponse = "[" + groupsResponse + "]";
				}

				try {
					JsonArray jsonArray = parser.parse(groupsResponse).getAsJsonArray();
					LOGGER.debug("jsonArray :" + jsonArray);
					/* Iterate each JSONObject in an JSONArray */
					for (int groupIndex = 0; groupIndex < jsonArray.size(); groupIndex++) {
						if (jsonArray.get(groupIndex).isJsonObject()) {
							JsonObject getGroupUserInfoData = jsonArray.get(groupIndex).getAsJsonObject();
							String value = oauthUtils.findKey(getGroupUserInfoData, settings.getRoleAttribute(),
									settings.getAppName());
							if (StringUtils.isNotEmpty(value))
								groupValues = groupValues + ";" + value;
							LOGGER.debug("When object fetched Group Values are: " + groupValues);
						} else {
							String value = jsonArray.get(groupIndex).getAsString();
							if (StringUtils.isNotEmpty(value))
								groupValues = groupValues + ";" + value;
							LOGGER.debug("When String fetched Group Values are: " + groupValues);
						}
					}
				}catch (Exception e){
					LOGGER.debug("error :", e);
				}
			}


			ArrayList<String> groupValueList = null;
			List<String> groupsToAssign = new ArrayList<>();
			if (groupValues != null && StringUtils.isNotEmpty(groupValues)) {
				groupValueList = new ArrayList(Arrays.asList(groupValues.split(";")));
				groupValueList.remove("");
			}
			LOGGER.debug("groupregexpattern = " + settings.getGroupRegexPatternEnabled());
			if(settings.getGroupRegexPatternEnabled() && groupValueList != null){
				for(int i=0;i<groupValueList.size();i++){
					String groupname = getGroupnameFromRegexMethod(settings.getRegexPatternForGroup(),settings.getRegexGroups(),groupValueList.get(i));
					LOGGER.debug(groupname);
					groupValueList.set(i,groupname);
				}
			}


			/*
			 * Check whether Username mapping is done and assigns User Identity accordingly.
			 */
			String userIdentity = StringUtils.EMPTY;

			/* code for login user with email address */
			if (StringUtils.equalsIgnoreCase(settings.getLoginUserAttribute(), "email")) {
				LOGGER.debug("Email attribute is selected for login/creating user in Bamboo");
				SearchResult<Object> emailUserSearchResult = bambooUserManager.getUsersByEmail(email);
				Pager<Object> pager = emailUserSearchResult.pager();
				int countUser = 0;
				String userFoundByEmail = StringUtils.EMPTY;
				for (Object user : pager) {
					countUser++;
					if(user instanceof com.atlassian.user.impl.DefaultUser){
						userFoundByEmail = ((com.atlassian.user.impl.DefaultUser)user).getName();
					} else if(user instanceof com.atlassian.crowd.embedded.atlassianuser.EmbeddedCrowdUser){
						userFoundByEmail = ((com.atlassian.crowd.embedded.atlassianuser.EmbeddedCrowdUser)user).getName();
					}
				}

				if (countUser > 1) {
					LOGGER.error("More than one user found with the same email i.e " + email + " address.");
					return null;
				}
				if (!StringUtils.isEmpty(userFoundByEmail)) {
					BambooUser bambooUser = bambooUserManager.getBambooUser(userFoundByEmail);
					LOGGER.debug("Bamboo user found by email  = " + bambooUser);
					userIdentity = bambooUser.getName();
				}

				if (StringUtils.isEmpty(userIdentity)) {
					if (StringUtils.isNotEmpty(userName)) {
						userIdentity = userName;
					} else if (StringUtils.isNotEmpty(email) && !oauthUtils.isEmailId(email)) {
						userIdentity = email;
					} else {
						userIdentity = email;
					}
				}
			} else {
				if (StringUtils.isEmpty(userName)) {
					userIdentity = email;
				} else {
					userIdentity = userName;
				}
			}
			LOGGER.debug("User identity is " + userIdentity);

			BambooUser updateBambooUser = null;
			List<String> defaultGroupsList = (List<String>) settings.getDefaultGroupsList();

			if (bambooUserManager.getBambooUser(userIdentity) != null) {
				// Existing User
				LOGGER.debug("User exists: " + userIdentity);

				// check if attributes are changed ... update them ... save api call
				// check if groups are changed ... update them ... group update api call

				updateBambooUser = bambooUserManager.getBambooUser(userIdentity);

				if (!BooleanUtils.toBoolean(settings.getKeepExistingUserAttributes())) {
					try {
						LOGGER.debug("Updating user attributes.");
						if(fullName.isEmpty()){
							fullName = updateBambooUser.getFullName();
						}
						updateBambooUser = userManager.saveAPICall("saveUser", userIdentity, email, fullName);
					} catch (Exception e) {
						LOGGER.debug("An exception occurs while updating user. " + e);
					}
				}

				//checks if directory has write permission
				Boolean canBeUpdated = canGroupBeUpdated(userIdentity);
				List<String> existingGroupsOfUser = bambooUserManager
						.getGroupNamesAsList(bambooUserManager.getBambooUser(userIdentity));
				List<String> newGroupsToAssign = new ArrayList<>();
				newGroupsToAssign.addAll(existingGroupsOfUser);

				if (!BooleanUtils.toBoolean(settings.getKeepExistingUserRoles())) {
					LOGGER.debug("Existing user groups can be updated. Updating groups");
					if (BooleanUtils.toBoolean(settings.getOnTheFlyGroupMapping())) {
						LOGGER.debug("On the Fly Group mapping is enabled");

						if (!StringUtils.equals(settings.getOnTheFlyFilterIDPGroupsOption(), MoOAuthPluginConstants.ON_THE_FLY_NO_GROUP_FILTER)
								&& groupValueList != null) {
								groupValueList = onTheFlyFilterIDPGroups(groupValueList);
								LOGGER.debug("List of IDP Groups after applying filter :" + groupValueList.toString());
						}

						List<String> onTheFlyGroupsToAssign = new ArrayList<>();
						List<String> doNotRemoveFromGroups = (List<String>) settings.getOnTheFlyDoNotRemoveGroups();

						if (!BooleanUtils.toBoolean(settings.getOnTheFlyAssignNewGroupsOnly())) {
							for (String group : existingGroupsOfUser) {
								if (!doNotRemoveFromGroups.contains(group) && BooleanUtils.toBoolean(canBeUpdated) && !groupValueList.contains(group)) {
									if (bambooPermissionManager.getAdminGroups().contains(group)) {
										LOGGER.debug( group + " has ADMINISTRATOR permission & checking for single admin ");
										if (getAdminUsers().size() > 1) {
//											LOGGER.debug("removing user from group - "+group);
											newGroupsToAssign.remove(group);
										}
									} else {
//										LOGGER.debug("removing user from group - "+group);
										newGroupsToAssign.remove(group);
									}
								}
							}
						}
						if (groupValues != null && StringUtils.isNotEmpty(groupValues)) {
							LOGGER.debug("Assigning groups from response");
							onTheFlyGroupsToAssign = createAndAssignGroups(groupValueList, userIdentity);

							for (String newGroups : onTheFlyGroupsToAssign) {
								if (BooleanUtils.toBoolean(canBeUpdated) && !newGroupsToAssign.contains(newGroups)) {
									newGroupsToAssign.add(newGroups);
								}
							}
						}

					} else {
						LOGGER.debug("Manual Group mapping is enabled");

						// Add all mapped groups are in groupsToAssign
						if (groupValues != null && StringUtils.isNotEmpty(groupValues)) {
							groupsToAssign = getListOfMappedGroupsToAssign(groupValueList);
						}
						if (!settings.getRoleAttribute().isEmpty()) {
							for (String newGroups : groupsToAssign) {
								if (BooleanUtils.toBoolean(canBeUpdated) && !existingGroupsOfUser.contains(newGroups)) {
									newGroupsToAssign.add(newGroups);
								}
							}

							HashMap<String, String> roleMapping = settings.getRoleMapping();

							for (String groups : existingGroupsOfUser) {
								if (!groupsToAssign.contains(groups)) {
									if (StringUtils.isNotEmpty(roleMapping.get(groups))) {
										if (BooleanUtils.toBoolean(canBeUpdated)
												&& existingGroupsOfUser.contains(groups)) {
											newGroupsToAssign.remove(groups);
										}
									}
								}
							}
						} else {
							LOGGER.debug("RoleAttribute() is EMPTY.");
						}
					}
				} else {
					LOGGER.debug("Existing user group updating is set to FALSE. KeepExistingUserRoles is checked");
				}

				// default groups
				if (StringUtils.equalsIgnoreCase(settings.getEnableDefaultGroupsFor(),
						MoOAuthPluginConstants.ENABLE_DEFAULT_GROUPS_FOR_ALL_USERS)) {
					LOGGER.debug("Assigning default groups to existing user");
					if (defaultGroupsList != null && defaultGroupsList.size() > 0) {
						// Assign all default groups [if not already assigned]
						for (String group : defaultGroupsList) {
							if (BooleanUtils.toBoolean(canBeUpdated) && !newGroupsToAssign.contains(group)) {
								newGroupsToAssign.add(group);
							}
						}
					} else {
						// Assign a default group
						if (BooleanUtils.toBoolean(canBeUpdated)
								&& !newGroupsToAssign.contains(settings.getDefaultGroup())) {
							newGroupsToAssign.add(settings.getDefaultGroup());
						}
					}
				}

				if (!BooleanUtils.toBoolean(settings.getKeepExistingUserRoles())
						|| StringUtils.equalsIgnoreCase(settings.getEnableDefaultGroupsFor(),
								MoOAuthPluginConstants.ENABLE_DEFAULT_GROUPS_FOR_ALL_USERS)) {
					LOGGER.debug("New groups to be assgined to the SSO user : " + newGroupsToAssign.toString());
					LOGGER.debug("making call to group update API");
					userManager.groupUpdateAPICall("updateGroups", userIdentity, newGroupsToAssign);
				}

				return updateBambooUser;
			} else {
				// New User
				LOGGER.debug("User DOES NOT exist. Creating new user.");
				if (!BooleanUtils.toBoolean(settings.getRestrictUserCreation())) {
					if (!email.isEmpty()) { // User email in OAuth response
						if (StringUtils.isBlank(fullName)) {
							fullName = userIdentity;
						}

						if (BooleanUtils.toBoolean(settings.getOnTheFlyGroupMapping())) {
							LOGGER.debug("On the Fly Group mapping is enabled");

							if (!StringUtils.equals(settings.getOnTheFlyFilterIDPGroupsOption(), MoOAuthPluginConstants.ON_THE_FLY_NO_GROUP_FILTER)
									&& groupValueList != null) {
								groupValueList = onTheFlyFilterIDPGroups(groupValueList);
								LOGGER.debug("List of IDP Groups after applying filter :" + groupValueList.toString());
							}

							// Add all groups from test configuration to groupsToAssign [(if allowed)create
							// groups if not found]
							// Add default groups if any to groupsToAssign and create user

							if (groupValues != null && StringUtils.isNotEmpty(groupValues)) {
								groupsToAssign = createAndAssignGroups(groupValueList, userIdentity);
							}
						} else {
							LOGGER.debug("Manual Group mapping is enabled");

							// Add Groups mapped to user to groupsToAssign
							// Add default groups if any to groupsToAssign and create user [check if
							// 'restrict user creation on role mapping' condition is satisfied]

							if (groupValues != null && StringUtils.isNotEmpty(groupValues)) {
								groupsToAssign = getListOfMappedGroupsToAssign(groupValueList);
							}

							if ((!settings.getCreateUsersIfRoleMapped()) || (groupsToAssign.size() > 0)) {
								LOGGER.debug("Roles found. Creating user with Username: " + userIdentity + ", Email: "
										+ email + ", " + "Name:" + fullName);
							} else {
								LOGGER.debug("groups are not properly mapped for the email " + email);
								return null;
							}
						}
						if (!StringUtils.equalsIgnoreCase(settings.getEnableDefaultGroupsFor(),
								MoOAuthPluginConstants.ENABLE_DEFAULT_GROUPS_FOR_DO_NOT_ASSIGN_DEFAULT_GROUPS)) {
							LOGGER.debug("Assigning default groups to new user");
							if (defaultGroupsList != null && defaultGroupsList.size() > 0) {
								// Assign all default groups
								groupsToAssign.addAll(defaultGroupsList);
							} else {
								// Assign a default group
								groupsToAssign.add(settings.getDefaultGroup());
							}
						}
						LOGGER.debug("username = " + userIdentity + "email = " + email + "fullName = " + fullName
								+ "Groups = " + groupsToAssign);
						return userManager.createAPICall("createUser", userIdentity, email, fullName, groupsToAssign);
					} else {
						// New user creation failed due to empty email
						LOGGER.debug("User creation failed due to empty email");
						return null;
					}
				} else {
					// New user creation is disabled
					LOGGER.debug("User creation is disabled for all the users.");
					return null;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	
	private Boolean authoriseUserAndEstablishSession(DefaultAuthenticator authenticator, Object userObject,
			HttpServletRequest request, HttpServletResponse response)
			throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, AuthenticatorException {

		Principal principal = (Principal) userObject;
		Method authUserMethod = DefaultAuthenticator.class.getDeclaredMethod("authoriseUserAndEstablishSession",
				HttpServletRequest.class, HttpServletResponse.class, Principal.class);

		authUserMethod.setAccessible(true);

		Boolean result = (Boolean) authUserMethod.invoke(authenticator, request, response, principal);
		LOGGER.debug("Authentication Result: " + result + " Is cookie Enabled?: " + settings.getRememberMeCookieEnabled());

		if (result && settings.getRememberMeCookieEnabled()) {

			reqObjectForRememberMe=request;
			respObjectForRememberMe=response;

			List<NameValuePair> postParameters = new ArrayList<>();
			postParameters.add(new BasicNameValuePair("action", "rememberMeFeature"));
			postParameters.add(new BasicNameValuePair("name", principal.getName()));

			String Response = MoOAuthHttpUtils.sendPostRequest("http://localhost:8085/plugins/servlet/oauth/moapi", postParameters,
					"application/x-www-form-urlencoded", null);
			LOGGER.debug("response " + Response);
		}
		MoOAuthHttpUtils.setCookie(MoOAuthPluginConstants.LOGOUTCOOKIE, MoOAuthPluginConstants.LOGOUTCOOKIE, response);
		return result;
	}

	/* Redirects user to Dashboard on successful Log in */
	private void redirectToSuccessfulAuthLandingPage(HttpServletRequest request, HttpServletResponse response,
			String relayState) throws IOException {
		String redirectUrl = settings.getDashboardUrl();
		if (StringUtils.isNotBlank(relayState)) {
			if (StringUtils.contains(relayState, "://")) {
				redirectUrl = relayState;
			} else {
				redirectUrl = settings.getBaseUrl().concat(relayState);
			}
		}
		LOGGER.debug("Redirecting user to " + redirectUrl);
		MoOAuthManager.httpRedirect(response, redirectUrl);
	}

	/* Redirects user to Login page due to some error in login process */
	private void redirectToLoginWithOAuthError(HttpServletResponse response, Exception exception, String oauthError)
			throws IOException, ServletException {
		if (exception != null) {
			LOGGER.error("OAuth Plugin error: " + exception.getMessage(), exception);
		}

		String redirectUrl = settings.getLoginPageUrl() + "?oautherror=" + oauthError;
		try {
			if (settings.getEnableErrorMsgTemplate()) {
				Map<String, Object> context = new HashMap();
				context.put("baseURL", settings.getBaseUrl());
				StringBuffer result = new StringBuffer(renderer.renderFragment(settings.getErrorMsgTemplate(), context));
				response.setContentType("text/html");
				response.getOutputStream().write(result.toString().getBytes());
			} else {
				redirectUrl = redirectUrl + "&oauth_sso=false";
				MoOAuthManager.httpRedirect(response, redirectUrl);
			}
		} catch (Exception e) {
			MoOAuthManager.httpRedirect(response, redirectUrl);
		}
	}

	private void authoriseAndRedirect(JsonObject getUserInfoData, HttpServletRequest request,
			HttpServletResponse response, String email, Boolean regexEnabled, String accessToken)
			throws IOException, ServletException {
		LOGGER.debug("Initiating user authentication and authorization flow");
		try {
			Boolean isDomainAllowed = Boolean.TRUE;
			LOGGER.debug("email address : " + email);

			String username = "";
			LOGGER.debug("Fetching Username...");

			if (StringUtils.isNotEmpty(settings.getUsernameAttribute())) {
				LOGGER.debug("Fetching Username from configured username attribute" + settings.getUsernameAttribute());

				if (oauthUtils.checkIfKeyExist(userInfoMap, settings.getUsernameAttribute())) {
					username = oauthUtils.getValue(userInfoMap, settings.getUsernameAttribute());
					username = username.replace("\"", "");
					LOGGER.debug("Username from map :" + username);
				} else {
					username = oauthUtils.findKey(getUserInfoData, settings.getUsernameAttribute(),
							settings.getAppName());
					username = username.replace("\"", "");
					LOGGER.debug("Username from key :" + username);
				}
			}
			LOGGER.debug("SSO User's username : " + username);

			LOGGER.info("Fetching Email.....");
			String emailAttribute = settings.getEmailAttribute();
			if (StringUtils.isNotBlank(emailAttribute)) {
				String[] emailAttrValueArray = emailAttribute.split(";");
				for(String emailAttr:emailAttrValueArray) {
					emailAttr = StringUtils.trimToEmpty(emailAttr);
					if (oauthUtils.checkIfKeyExist(userInfoMap, emailAttr)) {
						email = oauthUtils.getValue(userInfoMap, emailAttr);
						if(oauthUtils.isEmailId(email))
							break;
					} else {
						email = oauthUtils.findKey(getUserInfoData, emailAttr,
							settings.getAppName());
					}
				}
			}
			LOGGER.debug("email : " + email);

			LOGGER.debug("SSO User's email : " + email);

			/* check regex */
			if (regexEnabled && StringUtils.isNotBlank(username)) {
				LOGGER.debug("regex enabled...");
				Pattern pattern = Pattern.compile(StringUtils.trimToEmpty(settings.getRegexPattern()));
				Matcher matcher = pattern.matcher(username);
				LOGGER.debug("matcher : " + matcher);
				try {
					if (matcher.find()) {
						username = StringUtils.EMPTY;
						if (matcher.groupCount() > 0) {
							for (int i = 1; i <= matcher.groupCount(); i++) {
								username += matcher.group(i);
							}
						} else {
							username = matcher.group();
						}
						// settings.setUsernameAttribute(username);
						LOGGER.debug("Username after appling regex: " + username);
					}
				} catch (Exception e) {
					LOGGER.error("Invalid regex pattern");
					redirectToLoginWithOAuthError(response, null, "cant_signin_regex_pattern_exception");
					return;
				}
			}
			LOGGER.debug("Customer email received: " + email);
			if (!email.isEmpty() && StringUtils.isNotEmpty(settings.getAllowedDomains()))
				isDomainAllowed = checkForAllowedDomain(email);

			if (BooleanUtils.toBoolean(isDomainAllowed)) {
				Authenticator authenticator = SecurityConfigFactory.getInstance().getAuthenticator();
				Method getUserMethod = DefaultAuthenticator.class.getDeclaredMethod("getUser", String.class);
				getUserMethod.setAccessible(true);
				if ((authenticator instanceof DefaultAuthenticator)) {
					Object userObject = tryCreateOrUpdateUser(request, email, username, getUserInfoData,
							this.accessToken);
					if (userObject != null) {
						BambooUser bambooUser = (BambooUser) userObject;
						if (bambooUser.isEnabled() == false) {
							LOGGER.error("User is deactivated. Can't create user session.");
							redirectToLoginWithOAuthError(response, null, "user_is_deactivated_contact_admin");
							return;
						}
						LOGGER.debug("Establishing session for: " + bambooUser.getName());
						boolean result = authoriseUserAndEstablishSession((DefaultAuthenticator) authenticator,
								userObject, request, response);
						if (result) {
							LOGGER.debug("Session created Successfully! redirect url = " + getSessionAttribute("return_to", request));
							String returnTo = getSessionAttribute("return_to", request);
							if (StringUtils.isNotEmpty(settings.getRelayState()))
								redirectToSuccessfulAuthLandingPage(request, response, settings.getRelayState());
							else if (StringUtils.isNotBlank(returnTo))
								redirectToSuccessfulAuthLandingPage(request, response, returnTo);
							else
								redirectToSuccessfulAuthLandingPage(request, response, settings.getBaseUrl());
							return;
						}
						LOGGER.error("Session could NOT be created. Redirecting user to login page.");
					}
				}
			} else {
				LOGGER.error("User is restricted to login");
				redirectToLoginWithOAuthError(response, null, "user_not_allowed");
				return;
			}
			redirectToLoginWithOAuthError(response, null, "cant_signin_check_configuration");
			return;
		} catch (Exception e) {
			LOGGER.error("An error occurred while signing in the user.", e);
			redirectToLoginWithOAuthError(response, e, "cant_signin_check_configuration");
			return;
		}
	}

	private String getSessionAttribute(String attributeName, HttpServletRequest request) {
		HttpSession session = request.getSession();
		String returnTo = (String) session.getAttribute(attributeName);
		return returnTo;
	}

	private List<String> createAndAssignGroups(ArrayList<String> roleValuesList, String userIdentity) {
		List<String> groupsToAssign = new ArrayList<String>();
		Boolean canCreateNewGroups = settings.getOnTheFlyCreateNewGroups();
		try {
			for (String groupsName : roleValuesList) {
				if (bambooUserManager.getGroup(groupsName.trim()) == null
						&& BooleanUtils.toBoolean(canCreateNewGroups)) {
					LOGGER.debug("Creating New Group : " + groupsName.trim());
					MoOAuthUserManager.groupCreateAPICall("createGroup", userIdentity, groupsName.trim());
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

	private List<String> getListOfMappedGroupsToAssign(ArrayList<String> roleValuesList) {
		List<String> groupsToAssign = new ArrayList<>();
		if (roleValuesList != null && roleValuesList.size() > 0) {
			HashMap<String, String> roleMapping = settings.getRoleMapping();
			Iterator<String> it = roleMapping.keySet().iterator();
			while (it.hasNext()) {
				String key = it.next();
				String value = roleMapping.get(key);
				String[] groupNamesConfigured = StringUtils.split(value, ";");
				for (int i = 0; i < groupNamesConfigured.length; i++) {
					String groupValue = groupNamesConfigured[i];

					if (roleValuesList.contains(groupValue)) {
						com.atlassian.user.Group bambooUsersGroup = bambooUserManager.getGroup(key);
						if (bambooUsersGroup != null) {
							groupsToAssign.add(bambooUsersGroup.toString());
						}
					}
				}
			}
		}
		return groupsToAssign;
	}

	private boolean checkForAllowedDomain(String email) {
		String domains[] = settings.getAllowedDomains().split(";");
		String userDomain = email.split("@")[1];
		for (int index = 0; index < domains.length; index++) {
			if (StringUtils.equals(StringUtils.trimToEmpty(domains[index]), userDomain))
				return Boolean.TRUE;
		}
		return Boolean.FALSE;
	}

	private boolean canGroupBeUpdated(String userIdentity) {
		LOGGER.debug("canGroupBeUpdated called");
		if (settings.getCurrentBuildNumber() < 60604) {
			return !bambooUserManager.isReadOnly(bambooUserManager.getUser(userIdentity));
		} else {
			CrowdService crowdService = ComponentLocator.getComponent(CrowdService.class);
			CrowdDirectoryService crowdDirectoryService = ComponentLocator.getComponent(CrowdDirectoryService.class);
			User user = crowdService.getUser(userIdentity);
			Directory directory = crowdDirectoryService.findDirectoryById(user.getDirectoryId());
			return directory == null ? false
					: directory.getAllowedOperations().contains(OperationType.UPDATE_GROUP);
		}
	}

	private Collection<String> getAdminUsers() {
		Set<String> adminUsers = new HashSet<>();
		adminUsers.addAll(bambooPermissionManager.getAdminUsers());
		adminUsers.addAll(bambooPermissionManager.getRestrictedAdminUsers());

		for (String groupName : bambooPermissionManager.getAdminGroups()) {
			List<String> users = bambooUserManager.getMemberNamesAsList(bambooUserManager.getGroup(groupName));
//			LOGGER.debug("Users : "+users);
			adminUsers.addAll(users);
		}
		return adminUsers;
	}

	private ArrayList<String> onTheFlyFilterIDPGroups(ArrayList<String> idpGroups){
		ArrayList<String> filteredGroups = new ArrayList<>();

		LOGGER.debug("Filtering IDP groups using filter type : " + settings.getOnTheFlyFilterIDPGroupsOption() + ", and filter key :" + settings.getOnTheFlyFilterIDPGroupsKey()) ;
		if(StringUtils.equals(settings.getOnTheFlyFilterIDPGroupsOption(), MoOAuthPluginConstants.ON_THE_FLY_FILTER_GROUPS_STARTS_WITH)){
			for (String group : idpGroups){
				if(StringUtils.startsWith(group,settings.getOnTheFlyFilterIDPGroupsKey())){
					filteredGroups.add(group);
				}
			}
		}else if(StringUtils.equals(settings.getOnTheFlyFilterIDPGroupsOption(), MoOAuthPluginConstants.ON_THE_FLY_FILTER_GROUPS_CONTAINS)){
			for (String group : idpGroups){
				if(StringUtils.contains(group, settings.getOnTheFlyFilterIDPGroupsKey())){
					filteredGroups.add(group);
				}
			}
		}else if(StringUtils.equals(settings.getOnTheFlyFilterIDPGroupsOption(), MoOAuthPluginConstants.ON_THE_FLY_FILTER_GROUPS_WITH_REGEX)){
			Pattern pattern = Pattern.compile(StringUtils.trimToEmpty(settings.getOnTheFlyFilterIDPGroupsKey()));
			for(String group : idpGroups){
				Matcher matcher = pattern.matcher(group);
				try{
					if(matcher.find()){
						filteredGroups.add(group);
					}
				}catch(Exception e){
					LOGGER.error("Error while filtering groups using regex pattern", e);
				}
			}
		}
		return filteredGroups;
	}

	public void setSettings(MoOAuthSettings settings) {
		this.settings = settings;
	}

	public void setOAuthManager(MoOAuthManager oauthManager) {
		this.oauthManager = oauthManager;
	}

	public MoOAuthUtils getOauthUtils() {
		return oauthUtils;
	}

	public void setOauthUtils(MoOAuthUtils oauthUtils) {
		this.oauthUtils = oauthUtils;
	}

	public String getAccessToken() {
		return accessToken;
	}

	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}
    
    /*public HttpServletRequest getReqObject() {
		return reqObjectForRememberMe;
	}

	public void setReqObject(HttpServletRequest request) {
		this.reqObjectForRememberMe = request;
	}
    
    public HttpServletResponse getRespObject() {
		return respObjectForRememberMe;
	}

	public void setRespObject(HttpServletResponse resp) {
		this.respObjectForRememberMe = respObjectForRememberMe;
	}*/

	public HashMap<String, String> getUserInfoMap() {
		return userInfoMap;
	}

	public void setUserInfoMap(HashMap<String, String> userInfoMap) {
		this.userInfoMap = userInfoMap;
	}

	public String getRefreshToken() {
		return refreshToken;
	}

	public void setRefreshToken(String refreshToken) {
		this.refreshToken = refreshToken;
	}
}