package com.miniorange.oauth.confluence.servlet;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.Principal;
import java.security.PublicKey;
import java.security.Signature;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.bind.DatatypeConverter;

import com.atlassian.confluence.renderer.radeox.macros.MacroUtils;
import com.atlassian.confluence.user.UserDetailsManager;
import com.atlassian.confluence.util.velocity.VelocityUtils;
import com.atlassian.crowd.directory.ssl.LdapHostnameVerificationSSLSocketFactory;
import com.atlassian.json.jsonorg.JSONArray;
import com.atlassian.json.jsonorg.JSONException;
import com.atlassian.json.jsonorg.JSONObject;
import com.atlassian.seraph.auth.Authenticator;
import com.atlassian.seraph.auth.DefaultAuthenticator;
import com.atlassian.seraph.config.SecurityConfigFactory;
import com.atlassian.seraph.service.rememberme.RememberMeService;
import com.atlassian.user.GroupManager;
import com.google.gson.stream.MalformedJsonException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import com.google.gson.*;
import com.google.gson.JsonParseException;
import com.atlassian.confluence.security.Permission;
import com.atlassian.confluence.security.PermissionManager;
import com.atlassian.confluence.security.SpacePermission;
import com.atlassian.confluence.security.SpacePermissionManager;
import com.atlassian.confluence.security.login.LoginManager;
import com.atlassian.confluence.security.persistence.dao.hibernate.UserLoginInfo;
import com.atlassian.confluence.security.websudo.DefaultWebSudoManager;
import com.atlassian.confluence.spaces.Space;
import com.atlassian.confluence.spaces.SpaceManager;
import com.atlassian.confluence.user.AuthenticatedUserThreadLocal;
import com.atlassian.confluence.user.ConfluenceUser;
import com.atlassian.confluence.user.UserAccessor;
import com.atlassian.crowd.exception.GroupNotFoundException;
import com.atlassian.crowd.exception.OperationFailedException;
import com.atlassian.crowd.exception.OperationNotPermittedException;
import com.atlassian.crowd.exception.UserNotFoundException;
import com.atlassian.seraph.auth.AuthenticatorException;
import com.atlassian.spring.container.ContainerManager;
import com.atlassian.user.Group;
import com.atlassian.user.User;
import com.atlassian.user.UserManager;
import com.atlassian.user.impl.DefaultUser;
import com.atlassian.user.search.SearchResult;
import com.atlassian.user.search.page.Pager;
import com.atlassian.user.security.password.Credential;
import com.atlassian.templaterenderer.velocity.one.six.VelocityTemplateRenderer;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.miniorange.oauth.confluence.MoOAuthAuthenticator;
import com.miniorange.oauth.confluence.MoOAuthManager;
import com.miniorange.oauth.confluence.MoOAuthPluginConstants;
import com.miniorange.oauth.confluence.MoOAuthPluginHandler;
import com.miniorange.oauth.confluence.MoOAuthSettings;
import com.miniorange.oauth.confluence.action.MoOAuthAction;
import com.miniorange.oauth.confluence.dto.TokenResponse;
import com.miniorange.oauth.confluence.factory.IProtocolAction;
import com.miniorange.oauth.confluence.factory.OAuthOpenIdDecisionHandler;
import com.miniorange.oauth.utils.MoOAuthEncryptionUtils;
import com.miniorange.oauth.utils.MoOAuthHttpUtils;
import com.miniorange.oauth.utils.MoOAuthUtils;
import org.apache.lucene.util.fst.ListOfOutputs;

class MoOAuthCallbackServlet extends HttpServlet {

	private static Log LOGGER = LogFactory.getLog(MoOAuthCallbackServlet.class);
	private static final String DEFAULT_ERROR_MESSAGE = "template/com/miniorange/oauth/confluence/failedLoginTemplate.vm";

	private MoOAuthSettings settings;
	private UserManager userManager;
	private UserAccessor userAccessor;
	private MoOAuthUtils oauthUtils;
	private JsonParser parser = new JsonParser();
	private MoOAuthPluginHandler pluginHandler;
	private MoOAuthManager oauthManager;
	private DefaultWebSudoManager defaultWebSudoSessionManager;
	private MoOAuthAuthenticator oAuthAuthenticator;
	private PermissionManager permissionManager;
	private VelocityTemplateRenderer renderer;
	//private String accessToken = StringUtils.EMPTY;
	//private HashMap<String, String> userInfoMap = new HashMap<>();
	//private String refreshToken = StringUtils.EMPTY;
	//private String googleRefreshToken = StringUtils.EMPTY;
	//private String userDetailedInfoResponse = StringUtils.EMPTY;
	// Array containing a set of apps that support OIDC .Converted to ArrayList
	// listOfOpenIdApps before being passed to
	// OAuthOpenIdDecisionHandler.getProtocolHandler()
	private String[] openIdApps = { "OpenID", "ADFS", "AWS Cognito", "Azure B2C", "OKTA", "Keycloak" };
	private UserDetailsManager userDetailsManager;

	public MoOAuthCallbackServlet(MoOAuthSettings settings, UserManager userManager, UserAccessor userAccessor,
								  MoOAuthPluginHandler pluginHandler, MoOAuthManager oauthManager,
								  DefaultWebSudoManager defaultWebSudoSessionManager, MoOAuthAuthenticator oAuthAuthenticator,
								  MoOAuthUtils oauthUtils, PermissionManager permissionManager, UserDetailsManager userDetailsManager, VelocityTemplateRenderer renderer) {
		this.settings = settings;
		this.userManager = userManager;
		this.userAccessor = userAccessor;
		this.oauthManager = oauthManager;
		this.pluginHandler = pluginHandler;
		this.oAuthAuthenticator = oAuthAuthenticator;
		this.defaultWebSudoSessionManager = defaultWebSudoSessionManager;
		this.oauthUtils = oauthUtils;
		this.permissionManager = permissionManager;
		this.userDetailsManager = userDetailsManager;
		this.renderer = renderer;
	}

	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try {

			LOGGER.info("OAuth Authentication Callback Servlet doGet() called...");
			LOGGER.debug("settings.getIsTestsettingsurationClicked(): " + settings.getIsTestIDPConfigurationClicked());
			LOGGER.debug("Request");
			LOGGER.debug("Code: " + request.getParameter("code"));

			/* Passing both current appname and list of apps supporting OIDC */
			ArrayList<String> listOfOpenIdApps = new ArrayList<String>(Arrays.asList(openIdApps));
			IProtocolAction protocolAction = OAuthOpenIdDecisionHandler.getProtocolHandler(settings.getScope(),
					settings.getAppName(), listOfOpenIdApps);

			String accessToken = StringUtils.EMPTY;
			String googleRefreshToken = StringUtils.EMPTY;
			String refreshToken = StringUtils.EMPTY;
			HashMap<String, String> userInfoMap = new HashMap<>();
			String userDetailedInfoResponse = StringUtils.EMPTY;



			// if Test configuration is clicked, display the popup
			if (settings.getIsTestIDPConfigurationClicked()) {
				settings.setIsTestIDPConfigurationClicked(false);
				LOGGER.info("calling showTestConfigurationResult... ");
				showTestConfigurationResult(request, response, null, protocolAction, accessToken, refreshToken);
				return;
			}

			/* When Verify Credentials Button is Clicked (Only for Google) */
			if (settings.getIsVerifyCredentialsClicked()) {
				settings.setIsVerifyCredentialsClicked(false);
				LOGGER.info("Calling showVerifyCredentials... ");
				showVerifyCredentials(request, response, protocolAction,refreshToken);
				return;
			}

			if (settings.getMaxUsers() == 0) {
				LOGGER.error("No Evaluation license installed");
				redirectToLoginWithOAuthError( response, null , settings.getErrorMappingMap().get("SSO_AUTHENTICATION_FAILED"));
				return;
			}

			if (BooleanUtils.toBoolean(settings.getUseStateParameter())) {

				String stateParameterToServer = getSessionAttribute("state_attribute_parameter", request);
				String stateParameterFromServer = request.getParameter("state");
				if (stateParameterFromServer == null || stateParameterFromServer.equalsIgnoreCase("")) {
					LOGGER.error("In Valid (null) State Parameter return from Server");
					redirectToLoginWithOAuthError( response, null , settings.getErrorMappingMap().get("SSO_AUTHENTICATION_FAILED"));
					return;
				}

				if (!StringUtils.equalsIgnoreCase(stateParameterToServer, stateParameterFromServer)) {
					LOGGER.error("state parameter is Not Valid");
					redirectToLoginWithOAuthError( response, null , settings.getErrorMappingMap().get("SSO_AUTHENTICATION_FAILED"));
					return;
				}
				LOGGER.info("state parameter is Valid");
			}

			TokenResponse tokenResponse = endpointCallToGetAcessToken(request.getParameter("code"), protocolAction);
			String accessTokenResponse = tokenResponse.getResponse();

			/* Access Token Response error */
			if (StringUtils.isEmpty(accessTokenResponse) || StringUtils.isBlank(accessTokenResponse)
					|| accessTokenResponse.equals("Failed")) {
				LOGGER.error("Error with the AccessTokenEndpoint");
				redirectToLoginWithOAuthError( response, null , settings.getErrorMappingMap().get("SSO_AUTHENTICATION_FAILED"));
				return;
			}

			/* Convert Access Token Response String to JSONObject */
			JsonObject accessTokenEndpointData = parser.parse(accessTokenResponse).getAsJsonObject();
			LOGGER.debug("accessTokenEndpointData " + accessTokenEndpointData);
			if (accessTokenEndpointData.has("refresh_token")) {
				LOGGER.debug("HTTP Request : " + request.toString());
				HttpSession session_refresh_token = request.getSession();
				LOGGER.debug("HTTP Sdession  : " + session_refresh_token.toString());
				session_refresh_token.setAttribute("refresh_token",
						accessTokenEndpointData.get("refresh_token").getAsString());
				LOGGER.debug("refresh_token : " + getSessionAttribute("refresh_token", request));
			}
			/* If the AccessToken Response has any error */
			if (accessTokenEndpointData.has("error")) {
				LOGGER.error("accessTokenEndpointData has an error");
				redirectToLoginWithOAuthError( response, null , settings.getErrorMappingMap().get("SSO_AUTHENTICATION_FAILED"));
				return;
			}

			/* Retrieving access token */
			 accessToken = StringUtils.EMPTY;
			if (settings.getScope().toLowerCase().contains("openid")) {
				String id_token = accessTokenEndpointData.get("id_token").getAsString();

				LOGGER.debug("ID Token = " + id_token);
				DecodedJWT jwt = JWT.decode(id_token);
				String algorithm = jwt.getAlgorithm();
				Boolean isValid = Boolean.FALSE;

				if (id_token != null && StringUtils.equalsIgnoreCase(settings.getValidateSignatureMethod(), "publicKeySelect")
						&& StringUtils.isNotEmpty(settings.getPublicKey())) {
					LOGGER.debug("Validating JWT Signature using configured public key");
					isValid = verifyTokenSignature(jwt, oauthUtils.getPublicKeyObjectFromConfiguredKey(settings.getPublicKey()), algorithm);

					if (!BooleanUtils.toBoolean(isValid)) {
						LOGGER.error("Signature validation failed. Please check configured public key");
						redirectToLoginWithOAuthError( response, null , settings.getErrorMappingMap().get("SSO_AUTHENTICATION_FAILED"));
						return;
					}
					LOGGER.debug("Signature validation successful");

				} else if (id_token != null
						& StringUtils.equalsIgnoreCase(settings.getValidateSignatureMethod(), "JWKSEndPointURLSelect")
						&& StringUtils.isNotEmpty(settings.getjWKSEndpointURL())) {
					LOGGER.debug("Validating JWT Signature using public key received from JWKS endpoint");
					PublicKey publicKey = oauthUtils.getPublicKeyFromJWKSEndpoint(jwt, settings.getjWKSEndpointURL());
					isValid = verifyTokenSignature(jwt, publicKey, algorithm);

					if (!BooleanUtils.toBoolean(isValid)) {
						LOGGER.error("Signature validation failed. Please check configured JWKS endpoint");
						redirectToLoginWithOAuthError( response, null , settings.getErrorMappingMap().get("SSO_AUTHENTICATION_FAILED"));
						return;
					}
					LOGGER.debug("Signature validation successful");
				}
			}
			if (StringUtils.isEmpty(accessToken)){
				accessToken = accessTokenEndpointData.get("access_token").getAsString();}
			LOGGER.info("setting access token...");
			HttpSession session_access_token = request.getSession();
			session_access_token.setAttribute("access_token", accessToken);
			LOGGER.debug("access_token : " + getSessionAttribute("access_token", request));

			userDetailedInfoResponse = endpointCallToGetUserInfo(accessTokenResponse, protocolAction);
			if (StringUtils.equalsIgnoreCase(userDetailedInfoResponse, "Failed")
					|| StringUtils.equalsIgnoreCase(userDetailedInfoResponse, "error")
					|| StringUtils.equalsIgnoreCase(userDetailedInfoResponse, "invalid_client")
					|| StringUtils.equalsIgnoreCase(userDetailedInfoResponse, "invalid_token")) {
				LOGGER.error("UserInfoResponse Failed!. Reason : " + userDetailedInfoResponse);
				redirectToLoginWithOAuthError( response, null , settings.getErrorMappingMap().get("SSO_AUTHENTICATION_FAILED"));
				return;
			}

			LOGGER.debug("userDetailedInfoResponse = " + userDetailedInfoResponse);
			/* If User Info is fetched successfully */
			JsonObject getUserInfoData = parser.parse(userDetailedInfoResponse).getAsJsonObject();
			LOGGER.debug("getUserInfoData : " + getUserInfoData);
			HashMap<String, Object> userDetailsMap = new HashMap<String, Object>();

			userInfoMap = oauthUtils.copyToStringValueMap(oauthUtils.toMapObjects(getUserInfoData, userDetailsMap));
			LOGGER.debug("User Info Map: " + userInfoMap);
			if(settings.getNonceCheck() && listOfOpenIdApps.contains(settings.getAppName())) {
				LOGGER.debug("validating Nonce Parameter");
				String nonceValueInResponse= userInfoMap.get("nonce");
				String nonceValueInRequest = getSessionAttribute("nonce", request);
				LOGGER.debug("nonceValueInResponse : "+ nonceValueInResponse);
				LOGGER.debug("nonceValueInRequest : "+ nonceValueInRequest);
				if (!nonceValueInResponse.equals(nonceValueInRequest) || StringUtils.isBlank(nonceValueInResponse)) {
					LOGGER.debug("Nonce validation failed...");
					redirectToLoginWithOAuthError( response, null , settings.getErrorMappingMap().get("SSO_AUTHENTICATION_FAILED"));
					return;
				}
			}

			Set<String> emails = oauthUtils.findEmails(userInfoMap);
			String email = StringUtils.EMPTY;
			if (emails.size() > 0) {
				List<String> list = new ArrayList<String>(emails);
				email = list.get(0);
				LOGGER.debug("User email: " + email);
			}

			if (StringUtils.isNotEmpty(settings.getUsernameAttribute()) && (settings.getRegexPatternEnabled()
					|| StringUtils.equalsIgnoreCase(settings.getLoginUserAttribute(), "username"))) {
				if (!userInfoMap.containsKey(settings.getUsernameAttribute())) {
					LOGGER.error("An error occurred while signing in the user. Please check username mappings", null);
					redirectToLoginWithOAuthError( response, null , settings.getErrorMappingMap().get("SSO_AUTHENTICATION_FAILED"));
					return;
				}
			}
			if (StringUtils.isNotEmpty(settings.getUsernameAttribute()) && settings.getRegexPatternEnabled()) {
				if (!userInfoMap.containsKey(settings.getUsernameAttribute())) {
					LOGGER.error("An error occurred while signing in the user. Please check username mappings", null);
					redirectToLoginWithOAuthError( response, null , settings.getErrorMappingMap().get("SSO_AUTHENTICATION_FAILED"));
					return;
				}
			}
			if (StringUtils.equals(settings.getLoginUserAttribute(), "email")) {
				if (StringUtils.isNotEmpty(settings.getEmailAttribute())) {
					if (!userInfoMap.containsKey(settings.getEmailAttribute())) {
						LOGGER.error(
								"An error occurred while signing in the user. Login attribute set as email, please check email mappings.",
								null);
						redirectToLoginWithOAuthError( response, null , settings.getErrorMappingMap().get("SSO_AUTHENTICATION_FAILED"));
						return;
					}
				}
			}

			/*
			 * Because Meetup OAuth response does not return email in any of the attribute
			 * (id is considered as unique identifier)
			 */
			if (StringUtils.equals(settings.getAppName(), MoOAuthPluginConstants.MEETUP)) {
				String meetupid = oauthUtils.getValue(userInfoMap, "id");
				email = (new StringBuilder()).append(meetupid).append("@meetup.com").toString();
				LOGGER.debug("email: " + email);
			}

			/* Authorise the user and redirect to dashboard page. */
			LOGGER.debug("check if regex is enabled: " + settings.getRegexPatternEnabled());
			authoriseAndRedirect(getUserInfoData, request, response, email, settings.getRegexPatternEnabled(),
					accessToken, userInfoMap, protocolAction, userDetailedInfoResponse);

			return;
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
		}
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		super.doPost(req, resp);
		String refreshToken = req.getParameter("refresh_token");
		if (refreshToken != null)
			settings.setRefreshToken(refreshToken);
		return;
	}

	private void showTestConfigurationResult(HttpServletRequest request, HttpServletResponse response, String error,
			IProtocolAction protocolAction,String accessToken, String refreshToken) throws IOException {
		StringBuffer htmlStart = new StringBuffer("<head><style>table{font-family: arial, sans-serif;border-collapse: "
				+ "collapse;width: 100%;}td, th {border: 1px solid #dddddd;text-align: left;padding: 8px;}"
				+ "tr:nth-child(even){background-color: #dddddd;}</style></head><div style=\"font-family:Calibri;padding:0 3%;\">");

		try {
			if (error == null) {
				LOGGER.info("Inside showTestConfigurationResult");
				JsonObject getUserInfoData;
				String email = StringUtils.EMPTY;
				String id = StringUtils.EMPTY;
				LOGGER.info("Calling fetchUserInfo...");
				String userInfoResult = fetchUserInfo(request, protocolAction, accessToken, refreshToken);
				LOGGER.debug("UsesrInfoResult : " + userInfoResult);               //giving valid result
				String groupInfoResult = StringUtils.EMPTY;
				if (userInfoResult.contains("invalid_client")) {
					htmlStart = htmlStart.append("<div style=\"padding: 10px;"
							+ "margin-bottom: 30px;text-align:center;font-size:18pt;font-family=Courier New\">Oops! Something went wrong! "
							+ "</div><div style=\"padding: 15px;"
							+ "margin-bottom: 30px;text-align:left;font-family=Courier New\">"
							+ "<b style='font-size:17pt'>Possible Cause:</b><hr style=\"border: 1px solid #E6B3B2; \" class=\"header\" /> "
							+ "<p style='font-size:15pt;'> Error with the unauthorized invalid client."
							+ " Please check if the user is valid and the entered values are correct."
							+ " </br> If you need any help, please <a href=\"https://miniorange.atlassian.net/servicedesk/customer/portal/2\"> "
							+ "click here</a> to reach out to us.</p></div>");
					response.getOutputStream().write(htmlStart.toString().getBytes(StandardCharsets.UTF_8));
					return;
				} else if (userInfoResult.contains("error")) {
					// redirectToLoginWithOAuthError(response, null, "Error with the
					// AccessTokenEndpoint");

					htmlStart = htmlStart.append("<div style=\"padding: 10px;"
							+ "margin-bottom: 30px;text-align:center;font-size:18pt;font-family=Courier New\">Oops! Something went wrong! "
							+ "</div><div style=\"padding: 15px;"
							+ "margin-bottom: 30px;text-align:left;font-family=Courier New\">"
							+ "<b style='font-size:17pt'>Possible Cause:</b><hr style=\"border: 1px solid #E6B3B2; \" class=\"header\" /> "
							+ "<p style='font-size:15pt;'> Error with the AccessTokenEndpoint."
							+ " Please check if the Access Token Endpoint is correct."
							+ " </br> If you need any help, please <a href=\"https://miniorange.atlassian.net/servicedesk/customer/portal/2\"> "
							+ "click here</a> to reach out to us.</p></div>");
					response.getOutputStream().write(htmlStart.toString().getBytes(StandardCharsets.UTF_8));
					return;
				} else if (userInfoResult.contains("invalid_token")) {
					htmlStart = htmlStart.append("<div style=\"padding: 10px;"
							+ "margin-bottom: 30px;text-align:center;font-size:18pt;font-family=Courier New\">Oops! Something went wrong! "
							+ "</div><div style=\"padding: 15px;"
							+ "margin-bottom: 30px;text-align:left;font-family=Courier New\">"
							+ "<b style='font-size:17pt'>Possible Cause:</b><hr style=\"border: 1px solid #E6B3B2; \" class=\"header\" /> "
							+ "<p style='font-size:15pt;'> Error validating the Signature or Issuer in the Response."
							+ " </br> If you need any help, please <a href=\"https://miniorange.atlassian.net/servicedesk/customer/portal/2\"> "
							+ "click here</a> to reach out to us.</p></div>");
					response.getOutputStream().write(htmlStart.toString().getBytes(StandardCharsets.UTF_8));
					return;
				} else if (userInfoResult.contains("invalid_scope")) {
					htmlStart = htmlStart.append("<div style=\"padding: 10px;"
							+ "margin-bottom: 30px;text-align:center;font-size:18pt;font-family=Courier New\">Oops! Something went wrong! "
							+ "</div><div style=\"padding: 15px;"
							+ "margin-bottom: 30px;text-align:left;font-family=Courier New\">"
							+ "<b style='font-size:17pt'>Possible Cause:</b><hr style=\"border: 1px solid #E6B3B2; \" class=\"header\" /> "
							+ "<p style='font-size:15pt;'> Invalid scope configured."
							+ " </br> If you need any help, please <a href=\"https://miniorange.atlassian.net/servicedesk/customer/portal/2\"> "
							+ "click here</a> to reach out to us.</p></div>");
					response.getOutputStream().write(htmlStart.toString().getBytes(StandardCharsets.UTF_8));
					return;
				} else {
					try {
						 getUserInfoData = parser.parse(userInfoResult).getAsJsonObject();
						LOGGER.debug("getUserInfoData : " + getUserInfoData);               //giving valid result
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
							response.getOutputStream().write(htmlStart.toString().getBytes(StandardCharsets.UTF_8));
							return;
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

						LOGGER.info("Test successfully completed");

						htmlStart = htmlStart
								.append("<div style=\"color: #3c763d;background-color: #dff0d8; padding:2%;"
										+ "margin-bottom:20px;text-align:center; border:1px solid #AEDB9A; font-size:18pt;\">TEST "
										+ "SUCCESSFUL</div>");

						if (StringUtils.equals(settings.getAppName(), MoOAuthPluginConstants.KEYCLOAK)) {
							String checkPathString = oauthUtils.checkGroupPath(userInfoMap);
							if (checkPathString != null) {
								htmlStart = htmlStart.append("<div style='border:1px solid #FFDF00;padding: 15px;"
										+ "margin-bottom: 15px;text-align:left;font-family=Courier New'><p style='font-size:15pt'><strong>Warning: </strong>"
										+ "You are getting group path in <b>" + checkPathString
										+ "</b>. Group mapping will fail for this attribute.</p>"
										+ "<p>If you are using this attribute for group mapping then turn off full group path for the client in keycloak group mapper."
										+ " </br> If you need any help, please <a href=\"https://miniorange.atlassian.net/servicedesk/customer/portal/2\"> "
										+ "click here</a> to reach out to us.</p></div>");
							}
						}

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

						groupInfoResult = fetchGroupInfo(getUserInfoData, request, email, id, settings, protocolAction,
								oauthUtils, accessToken);
					} catch (IllegalStateException i) {
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
						response.getOutputStream().write(htmlStart.toString().getBytes(StandardCharsets.UTF_8));
						return;
					}

				}
				if (groupInfoResult.equals("Failed")) {
					refreshToken = "";
					sendRequestToSaveToken(StringUtils.EMPTY);
					htmlStart = htmlStart
							.append("<div style=\"text-align:left;\"><h2>Unable to fetch Groups Info! </h2></div>");
					if (settings.getAppName().equals(MoOAuthPluginConstants.GOOGLE)) {
						htmlStart = htmlStart.append(
								"<di" + "v style=\"text-align:left;\"><h2>Please verify your credentials!</h2></div>");
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
									LOGGER.info("JSONArray Found!!!!!!");
									htmlStart = htmlStart.append(
											"<td style=\"padding:2%;border:2px solid #949090;word-wrap:break-word;\"></td></tr>");
									// htmlStart = htmlStart.append("<td>");
									JsonArray array = (JsonArray) value;
									for (int index = 0; index < array.size(); index++) {
										// htmlStart = htmlStart.append("<table>");
										htmlStart = htmlStart.append("<tr style=\"height: 10px !important;\"></tr>");
										JsonObject object = ((JsonArray) value).get(index).getAsJsonObject();
										Map<String, JsonElement> nestedMap = oauthUtils.jsonToMap(object);
										for (Map.Entry<String, JsonElement> nestedEntry : nestedMap.entrySet()) {
											String nestedKey = nestedEntry.getKey();
											//////////////////// code need to be refactor////////////////////
											try {
												Object nestedValue = nestedEntry.getValue().getAsString();
												htmlStart = htmlStart.append("<tr>");
												htmlStart = htmlStart.append(
														"<td style=\"font-weight:bold;border:2px solid #949090; padding:2%; text-align:right;\">"
																+ nestedKey + "</td>");
												htmlStart = htmlStart.append(
														"<td style=\"padding:2%;border:2px solid #949090;word-wrap:break-word;\">"
																+ nestedValue + "</td>");
												htmlStart = htmlStart.append("</tr>");
											} catch (UnsupportedOperationException e) {
												Object nestedValue = nestedEntry.getValue();
												htmlStart = htmlStart.append("<tr>");
												htmlStart = htmlStart.append(
														"<td style=\"font-weight:bold;border:2px solid #949090; padding:2%; text-align:right;\">"
																+ nestedKey + "</td>");
												htmlStart = htmlStart.append(
														"<td style=\"padding:2%;border:2px solid #949090;word-wrap:break-word;\">"
																+ nestedValue + "</td>");
												htmlStart = htmlStart.append("</tr>");
											} catch (IllegalStateException i) {
												Object nestedValue = nestedEntry.getValue();
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
									}
									// htmlStart = htmlStart.append("</table></td>");
									htmlStart = htmlStart.append("<tr style=\"height: 10px !important;\"></tr>");

								} else {
									htmlStart = htmlStart.append(
											"<td style=\"padding:2%;border:2px solid #949090;word-wrap:break-word;\">"
													+ value + "</td>");
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
				response.getOutputStream().write(htmlStart.toString().getBytes(StandardCharsets.UTF_8));
			}
		} catch (JsonParseException e) {
			LOGGER.error(e.getMessage());
			htmlStart = htmlStart.append("<div style=\"padding: 10px;"
					+ "margin-bottom: 30px;text-align:center;font-size:18pt;font-family=Courier New\">Oops! Something went wrong! "
					+ "</div><div style=\"padding: 15px;"
					+ "margin-bottom: 30px;text-align:left;font-family=Courier New\">"
					+ "<b style='font-size:17pt'>Possible Cause:</b><hr style=\"border: 1px solid #E6B3B2; \" class=\"header\" /> "
					+ "<p style='font-size:15pt;'> An unexpected error occurred in response."
					+ " Seems like configuration issue.<br/>" + "Please check your provider settings.</p></div>");
			response.setContentType("text/html");
			response.getOutputStream().write(htmlStart.toString().getBytes(StandardCharsets.UTF_8));
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
			htmlStart = htmlStart.append("<div style=\"padding: 10px;"
					+ "margin-bottom: 30px;text-align:center;font-size:18pt;font-family=Courier New\">Oops! Something went wrong! "
					+ "</div><div style=\"padding: 15px;"
					+ "margin-bottom: 30px;text-align:left;font-family=Courier New\">"
					+ "<b style='font-size:17pt'>Possible Cause:</b><hr style=\"border: 1px solid #E6B3B2; \" class=\"header\" /> "
					+ "<p style='font-size:15pt;'> An unexpected error occurred in response."
					+ " Seems like configuration issue.<br/>" + "Please check your provider settings.</p></div>");
			response.setContentType("text/html");
			response.getOutputStream().write(htmlStart.toString().getBytes(StandardCharsets.UTF_8));
		}
	}

	private void sendRequestToSaveToken(String refreshToken) {
		List<NameValuePair> postParameters = new ArrayList<>();
		postParameters.add(new BasicNameValuePair("action", "saveRefreshToken"));
		postParameters.add(new BasicNameValuePair("refresh_token", refreshToken));
		String Response = MoOAuthHttpUtils.sendPostRequest(settings.getCallBackUrl() + settings.getCustomCallbackParameter(), postParameters,  
				"application/x-www-form-urlencoded", null); 
		LOGGER.debug("check app specific callback URL : "+ settings.getCustomCallbackParameter());  
		LOGGER.debug("Response " + Response); 
	}

	/*
	 * This is the function for showing Verify Credentials pop-up window (Only for
	 * Google)
	 */
	private void showVerifyCredentials(HttpServletRequest request, HttpServletResponse response,
			IProtocolAction protocolAction, String refreshToken) throws IOException, JsonParseException {
		LOGGER.info("showVerifyCredentials function!");

		StringBuffer htmlStart = new StringBuffer("<head><style>table{font-family: arial, sans-serif;border-collapse: "
				+ "collapse;width: 100%;}td, th {border: 1px solid #dddddd;text-align: left;padding: 8px;}"
				+ "tr:nth-child(even){background-color: #dddddd;}</style></head><div style=\"font-family:Calibri;padding:0 3%;\">");

		// if (StringUtils.isEmpty(this.googleRefreshToken)) {
		String googleRefreshToken = settings.getGoogleRefreshToken();
		// }

		// if (StringUtils.isEmpty(this.googleRefreshToken)) {

		TokenResponse tokenResponse = protocolAction.endpointCallToGetRefreshToken(request.getParameter("code"),
				settings);
		String refreshTokenResponse = tokenResponse.getResponse();
		LOGGER.debug("refreshTokenResponse is " + refreshTokenResponse);
		JsonObject refreshTokenData = parser.parse(refreshTokenResponse).getAsJsonObject();
		if (refreshTokenData.has("refresh_token")) {
			refreshToken = refreshTokenData.get("refresh_token").getAsString();
			settings.setGoogleRefreshToken(refreshTokenData.get("refresh_token").getAsString());
			sendRequestToSaveToken(refreshToken);
			settings.setIsCredentialsVerified(true);
			htmlStart = htmlStart.append("<div style=\"color: black; padding:2%;"
					+ "margin-bottom:20px;text-align:center; border:1px solid #AEDB9A; font-size:18pt;\">Thanks for verifying "
					+ "your account with the Administrator Account. Groups will be fetched for your users now. "
					+ "</div>");
		} else {
			htmlStart = htmlStart.append("<div style=\"color: black; padding:2%;"
					+ "margin-bottom:20px;text-align:left; border:1px solid #AEDB9A; font-size:16pt;\">Unable to Verify Admin Credentials."
					+ ". Please make sure you are verifying the credentials with the Administrator Account."
					+ "</div>");
		}
		/*
		 * } else { htmlStart =
		 * htmlStart.append("<div style=\"color: black; padding:2%;" +
		 * "margin-bottom:20px;text-align:center; border:1px solid #AEDB9A; font-size:18pt;\">You "
		 * + "have already verified the credentials of the administrator account. " +
		 * "Groups of users will be fetched from Google.</div>"); }
		 */

		htmlStart = htmlStart
				.append("<div style=\"margin:3%;display:block;text-align:center;\"><input style=\"padding:1%;"
						+ "width:100px;background: #0091CD none repeat scroll 0% 0%;cursor: pointer;font-size:15px;"
						+ "border-width: 1px;border-style: solid;border-radius: 3px;white-space: nowrap;"
						+ "box-sizing:border-box;border-color: #0073AA;box-shadow:0px 1px 0px rgba(120,200,230,0.6) inset;"
						+ "color: #FFF;\" type=\"button\" value=\"Done\" onClick=\"self.close();\"></div>");

		response.setContentType("text/html");
		response.getOutputStream().write(htmlStart.toString().getBytes(StandardCharsets.UTF_8));
	}

	/*
	 * This is the function for fetching User Information from Configured User Info
	 * Endpoint w.r.t App
	 */
	private String fetchUserInfo(HttpServletRequest request, IProtocolAction protocolAction, String accessToken, String refreshToken) throws JsonParseException {

		LOGGER.info("Inside fetchUserInfo");
		TokenResponse tokenResponse = endpointCallToGetAcessToken(request.getParameter("code"), protocolAction);
		LOGGER.debug("Token Response................." + tokenResponse);
		JsonObject tokenResponseObj = parser.parse(tokenResponse.getResponse()).getAsJsonObject();

		LOGGER.debug("tokenResponse : " + tokenResponse);
		if (tokenResponseObj != null) {

			if (tokenResponseObj.get("error") != null
					&& StringUtils.equals(tokenResponseObj.get("error").getAsString(), "invalid_grant")) {
				LOGGER.error("Invalid scope");
				return "invalid_scope";
			}
			if (tokenResponseObj.get("access_token") != null) {
				accessToken = tokenResponseObj.get("access_token").getAsString();
				LOGGER.debug("access token received:" + accessToken);
			}
			if (tokenResponseObj.get("refresh_token") != null) {
				refreshToken = tokenResponseObj.get("refresh_token").getAsString();
				LOGGER.debug("refresh token received:" + refreshToken);
			}
			LOGGER.debug("refreshToken : " + refreshToken);

		} else {
			if (StringUtils.isEmpty(accessToken) || StringUtils.isBlank(accessToken) || accessToken.equals("Failed")) {
				LOGGER.error("Error with the AccessTokenEndpoint");
				return "error";
			}
		}
		if (settings.getScope().toLowerCase().contains("openid")) {
			
			String id_token = tokenResponseObj.get("id_token").getAsString();
			LOGGER.debug("ID Token = " + id_token);
			DecodedJWT jwt = JWT.decode(id_token);
			LOGGER.debug("DecodedJWT Token = " + jwt);
			String algorithm = jwt.getAlgorithm();
			Boolean isValid = Boolean.FALSE;

			if (id_token != null && StringUtils.equalsIgnoreCase(settings.getValidateSignatureMethod(), "publicKeySelect")
					&& StringUtils.isNotEmpty(settings.getPublicKey())) {
				LOGGER.debug("Validating JWT Signature using configured public key");
				isValid = verifyTokenSignature(jwt, oauthUtils.getPublicKeyObjectFromConfiguredKey(settings.getPublicKey()), algorithm);
				
				if (!BooleanUtils.toBoolean(isValid)) {
					LOGGER.error("Signature validation failed. Please check configured public key");
					return "invalid_token";
				}
				LOGGER.debug("Signature Validation successful");

			
			} else if (id_token != null
					& StringUtils.equalsIgnoreCase(settings.getValidateSignatureMethod(), "JWKSEndPointURLSelect")
					&& StringUtils.isNotEmpty(settings.getjWKSEndpointURL())) {
				LOGGER.debug("Validating JWT Signature using public key received from JWKS endpoint");
				PublicKey publicKey = oauthUtils.getPublicKeyFromJWKSEndpoint(jwt, settings.getjWKSEndpointURL());
				isValid = verifyTokenSignature(jwt, publicKey, algorithm);
				
				if (!BooleanUtils.toBoolean(isValid)) {
					LOGGER.error("Signature validation failed. Please check configured JWKS endpoint");
					return "invalid_token";
				}
				LOGGER.debug("Signature Validation successful");

			}
		}
		
		HttpSession session_refresh_token = request.getSession();
		session_refresh_token.setAttribute("refresh_token", refreshToken);
		LOGGER.debug("refresh token : " + getSessionAttribute("refresh_token", request));

		HttpSession session_access_token = request.getSession();
		session_access_token.setAttribute("access_token", accessToken);
		LOGGER.debug("access token : " + getSessionAttribute("access_token", request));

		String accessTokenResponse = tokenResponse.getResponse();
		if (StringUtils.isEmpty(accessTokenResponse) || StringUtils.isBlank(accessTokenResponse)
				|| accessTokenResponse.equals("Failed")) {
			LOGGER.error("Error with the AccessTokenEndpoint");
			return "error";
		}

		LOGGER.info("Calling SendUserInfoRequest");
		TokenResponse response = protocolAction.sendUserInfoRequest(accessTokenResponse, settings);
		return response.getResponse();

	}

	/*
	 * This is the function to fetch Group Information from Group Info Endpoint
	 * w.r.t App
	 */
	private String fetchGroupInfo(JsonObject userInfodata, HttpServletRequest request, String email, String id,
			MoOAuthSettings settings, IProtocolAction protocolAction, MoOAuthUtils oauthUtils, String accessToken)
			throws JsonParseException {

		String refreshToken = getSessionAttribute("refresh_token", request);
		String accessatr = getSessionAttribute("access_token", request);
		accessToken = accessatr;

		if (protocolAction instanceof MoOAuthAction) {
			((MoOAuthAction) protocolAction).setRefreshToken(refreshToken);
			((MoOAuthAction) protocolAction).setAccessToken(accessToken);

		}
		TokenResponse tokenResponse = protocolAction.sendGroupInfoRequest(userInfodata, request, email, id, settings,
				oauthUtils);

		if (protocolAction instanceof MoOAuthAction) {
			accessToken = ((MoOAuthAction) protocolAction).getAccessToken();
			refreshToken = ((MoOAuthAction) protocolAction).getRefreshToken();
		}
		return tokenResponse.getResponse();

	}

	/*
	 * This is the function to get Access Token from Access Token Endpoint w.r.t App
	 */
	private TokenResponse endpointCallToGetAcessToken(String code, IProtocolAction protocolAction)
			throws JsonParseException {
		LOGGER.info("inside endpointCallToGetAcessToken...");
		TokenResponse response = protocolAction.sendTokenRequest(settings, code);
		LOGGER.debug("response : " + response);
		return response;
	}

	/* This is the function to call User Info Endpoint w.r.t App */
	private String endpointCallToGetUserInfo(String accessToken, IProtocolAction protocolAction)
			throws JsonParseException {
		LOGGER.info("inside endpointCallToGetUserInfo...");
		TokenResponse res = protocolAction.sendUserInfoRequest(accessToken, settings);
		return res.getResponse();
	}

	/* This is the function to Create or Update User at the time of log in. */
	private ConfluenceUser tryCreateOrUpdateUser(HttpServletRequest request, HttpServletResponse response, String email, String userName,
												 JsonObject userInfoData, String accessToken, HashMap<String, String> userInfoMap,
												 IProtocolAction protocolAction, String userDetailedInfoResponse) throws UserNotFoundException, OperationFailedException,
			GroupNotFoundException, OperationNotPermittedException ,MalformedJsonException, IOException{

		try {
			LOGGER.info("Creating/Updating user.");
			String fullName = StringUtils.EMPTY;
			String firstName = StringUtils.EMPTY, lastName = StringUtils.EMPTY;
			String emailAttrValue = StringUtils.EMPTY;
			DefaultUser userTemplate;

			LOGGER.info("Fetching FullName...");
			if (settings.getUseSeparateNameAttributes()) {
				if (StringUtils.isNotEmpty(settings.getFirstNameAttribute())) {
					if (oauthUtils.checkIfKeyExist(userInfoMap, settings.getFirstNameAttribute())) 
						firstName = oauthUtils.getValue(userInfoMap, settings.getFirstNameAttribute());
				
				}
				if (StringUtils.isNotEmpty(settings.getLastNameAttribute())) {
					if (oauthUtils.checkIfKeyExist(userInfoMap, settings.getLastNameAttribute())) 
						lastName = oauthUtils.getValue(userInfoMap, settings.getLastNameAttribute());
					
				}
				fullName = firstName + " " + lastName;

			} else {
				if (StringUtils.isNotEmpty(settings.getFullNameAttribute())) {
					if (oauthUtils.checkIfKeyExist(userInfoMap, settings.getFullNameAttribute())) 
						fullName = oauthUtils.getValue(userInfoMap, settings.getFullNameAttribute());
					
					LOGGER.debug("FullName fetched......." + fullName);
				}
			}

			LOGGER.info("Fetching Email.....");
			String emailAttribute = settings.getEmailAttribute();
			if (StringUtils.isNotBlank(emailAttribute)) {
				String[] emailAttrValueArray = emailAttribute.split(";");
				for(String emailAttr:emailAttrValueArray) {
					emailAttr = StringUtils.trimToEmpty(emailAttr);
					if (oauthUtils.checkIfKeyExist(userInfoMap, emailAttr)) {
						emailAttrValue = oauthUtils.getValue(userInfoMap, emailAttr);
						break;
					} else {
						emailAttrValue = oauthUtils.findKey(userInfoData, emailAttr,
								settings.getAppName());
					}
				}
				LOGGER.debug("emailAttrValue "+emailAttrValue);
				if (StringUtils.isNotBlank(emailAttrValue)) {
					email = emailAttrValue;
				}
			}
			LOGGER.debug("email : " + email);

			/* Id is required for Getting User from Facebook */
			String id = StringUtils.EMPTY;
			if (StringUtils.equals(settings.getAppName(), MoOAuthPluginConstants.FACEBOOK))
				id = userInfoData.get("id").getAsString();
			LOGGER.debug("id: " + id);
			String groupValues = StringUtils.EMPTY;
			String groupsResponse = StringUtils.EMPTY;

			LOGGER.debug("Group Info Endpoint : " + settings.getFetchGroupsEndpoint());
			if (StringUtils.isNotBlank(settings.getFetchGroupsEndpoint())) {
				groupsResponse = fetchGroupInfo(userInfoData, request, email, id, settings, protocolAction, oauthUtils, accessToken);
			} else {
				if (StringUtils.isEmpty(groupsResponse)) {	
					if (settings.getRoleAttribute().contains("*")) {						
						String groupKey = StringUtils.substringBefore(settings.getRoleAttribute(), "[");
						String subGroupKey = StringUtils.substringAfter(settings.getRoleAttribute(), "].");
						
						LOGGER.debug("groupKey : "+ groupKey + " subGroupKey : "+ subGroupKey);
						
						JSONObject groupRes = new JSONObject(userDetailedInfoResponse);
						LOGGER.debug("groupRes: "+ groupRes);
						JSONArray groupList = new JSONArray(groupRes.get(groupKey).toString());		
						LOGGER.debug("groupList : "+groupList);
						
						for (int i = 0; i < groupList.length(); i++) {  
						     JSONObject childJSONObject = groupList.getJSONObject(i);
						     String group = childJSONObject.getString(subGroupKey);
						     LOGGER.debug("Groups : "+ group);
						     groupsResponse += group.concat(", ");
						}	
					} else if (oauthUtils.checkIfKeyExist(userInfoMap, settings.getRoleAttribute())){
						groupsResponse = oauthUtils.getValue(userInfoMap, settings.getRoleAttribute());
					}
				}	
			}
		
			LOGGER.debug("groupsResponse : "+ groupsResponse);

			String groupsResponse_temp = StringUtils.EMPTY;
			if (!(groupsResponse.isEmpty()) && !StringUtils.equals(groupsResponse, "Failed")) {

				LOGGER.debug("Group Response is: " + groupsResponse);
				if (!(groupsResponse.startsWith("["))) {
						groupsResponse_temp = "[" + groupsResponse + "]";
				}
				JsonArray jsonArray = new JsonArray();
				try {
					 jsonArray = parser.parse(groupsResponse_temp).getAsJsonArray();

				}catch (Exception e)
				{
					LOGGER.error("Error occured due to double quotes");
//					groupsResponse_temp = "["+ "\"" + groupsResponse + "\""+ "]";
//					jsonArray = parser.parse(groupsResponse_temp).getAsJsonArray();
				}
				groupsResponse = groupsResponse_temp;
				LOGGER.debug("Group value after adding quotes : "+ groupsResponse);

				try {
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
					LOGGER.error("error :", e);
				}
			}


			ArrayList<String> groupValueList = null;
			List<Group> groupsToAssign = new ArrayList<Group>();
			List<Group> allMappedGroups = new ArrayList<Group>();
			List<String> groupsToBeRemoved = new ArrayList<String>();

			if (groupValues != null && StringUtils.isNotEmpty(groupValues)) {
//				groupValueList = new ArrayList(Arrays.asList(groupValues.split(";")));
				LOGGER.debug("this is the group values : "+ Arrays.asList(groupValues));

				ArrayList<String> roleValueList = new ArrayList(Arrays.asList(groupValues));
				groupValueList = serializeGroups(roleValueList);

				LOGGER.debug("groupValueList are " + groupValueList);
				List<List<Group>> returnedMappedGroups = new ArrayList<>();
				returnedMappedGroups = getListOfMappedGroupsToAssign(groupValueList);
				groupsToAssign = returnedMappedGroups.get(0);
				allMappedGroups = returnedMappedGroups.get(1);

				for (int mappedGroupIndex = 0; mappedGroupIndex < allMappedGroups.size(); mappedGroupIndex++) {
					boolean assignedGroupNotInMapped = true;
					for (int assignedGroupIndex = 0; assignedGroupIndex < groupsToAssign.size(); assignedGroupIndex++) {
						if (allMappedGroups.get(mappedGroupIndex).toString()
								.equals(groupsToAssign.get(assignedGroupIndex).toString())) {
							assignedGroupNotInMapped = false;
							break;
						}
					}
					if (assignedGroupNotInMapped == true) {
						groupsToBeRemoved.add(allMappedGroups.get(mappedGroupIndex).toString());
					}
					LOGGER.debug("groupsToBeRemoved: " + groupsToBeRemoved);
					LOGGER.debug("groupsToAssign: " + groupsToAssign);
				}
			} else {
				LOGGER.debug("No groups received in the response");
				// No groups received in the response -> remove user from all the mapped groups
				Map<String, String> roleMapping = settings.getRoleMapping();
				Iterator<String> it = roleMapping.keySet().iterator();
				while (it.hasNext()) {
					// role mapping is present for confluenceGroupName
					String confluenceGroupName = it.next();
					if (userAccessor.getGroup(confluenceGroupName) != null) {
						groupsToBeRemoved.add(confluenceGroupName);
					}
				}
				LOGGER.debug("groupsToBeRemoved: " + groupsToBeRemoved);
			}
			LOGGER.debug("EnableGroupsFor: " + settings.getEnableDefaultGroupsFor());

			/*
			 * Check whether Username mapping is done and assigns User Identity accordingly.
			 */
			String userIdentity = StringUtils.EMPTY;

			/* code for login user with email address */
			if (StringUtils.equalsIgnoreCase(settings.getLoginUserAttribute(), "email")) {
				LOGGER.info("searching confluence user by email attribute value");
				String tempEmail = emailAttrValue;
				if (StringUtils.isEmpty(tempEmail) || !oauthUtils.isEmailId(tempEmail)) {
					tempEmail = email;
				}
				SearchResult searchResult = userAccessor.getUsersByEmail(tempEmail);
				Pager<User> pager = searchResult.pager();
				int count = 0;
				for (User user : pager) {
					if (count == 0) {
						LOGGER.debug("found user by email = " + user.getEmail());
						userName = user.getName();
						userIdentity = user.getName();

					} else if (count > 1) {
						LOGGER.debug("email = " + email);
						LOGGER.error("more than one user found with this email address.");
						redirectToLoginWithOAuthError( response, null , settings.getErrorMappingMap().get("SSO_AUTHENTICATION_FAILED"));
						return null;
					}
					count++;
				}
				if (count > 1) {
					LOGGER.debug("email = " + email);
					LOGGER.error("more than one user found with this email address.");
					redirectToLoginWithOAuthError( response, null , settings.getErrorMappingMap().get("SSO_AUTHENTICATION_FAILED"));
					return null;
				}

				if (StringUtils.isEmpty(userIdentity)) {
					// we need to put emailAttrValue has user identity only when
					// it is not empty plus and it is valid email address
					if (StringUtils.isNotEmpty(emailAttrValue)) {
						if(!oauthUtils.isEmailId(emailAttrValue))
							userIdentity = email;
						else
							userIdentity = emailAttrValue;
					}
					else if (StringUtils.isNotEmpty(userName))
						userIdentity = userName;
				}

			} else {
				LOGGER.info("Login using UserName...");
				LOGGER.debug("email : " + email);
				LOGGER.debug("userName : " + userName);

				if (StringUtils.isEmpty(userName)) {
					LOGGER.debug("username is empty, assigning email");
					userIdentity = email;
				} else {
					userIdentity = userName;
				}
			}

			LOGGER.debug("userIdentity : " + userIdentity);
			UserAccessor userAccessor = (UserAccessor) ContainerManager.getComponent("userAccessor");

			LOGGER.debug("User email is " + email);
			if (userAccessor.exists(userIdentity)) {
				LOGGER.debug("User exists." + userIdentity);
				if (StringUtils.isEmpty(userName)) {
					userName = email;
				}

				ConfluenceUser confluenceUser =userAccessor.getUserByName(userName);
				if (confluenceUser !=null) {
					Boolean isDeactivated = BooleanUtils.toBoolean(userAccessor.isDeactivated(userName));
					if (isDeactivated) {
						if (!settings.getAutoActivateUser()) {
							LOGGER.error("User is deactivated. Can't create user session, redirecting to the login page");
							redirectToLoginWithOAuthError( response, null , settings.getErrorMappingMap().get("SSO_AUTHENTICATION_FAILED"));
							return null;
						}
						try {
							reactivateUser(confluenceUser);
						} catch (Exception e) {
							LOGGER.error("An error occurred while reactivating the user.", e);
							redirectToLoginWithOAuthError( response, null , settings.getErrorMappingMap().get("SSO_AUTHENTICATION_FAILED"));
							return null;
						}
					}
				}
				
				if (!settings.getKeepExistingUserAttributes()) {
					LOGGER.info("keep existing user attributes unchecked ");
					LOGGER.debug(" Updating user attributes with Username: " + userName + ", " + "Name:" + fullName);
					if (StringUtils.isEmpty(userName))
						userName = email;
					userTemplate = new DefaultUser(userAccessor.getUserByName(userName));
					if (StringUtils.isBlank(fullName)) {
						fullName = userTemplate.getFullName();
					}
					userTemplate.setFullName(fullName);
					userTemplate.setEmail(email);
					userAccessor.saveUser(userTemplate);
					HashMap<String, String> customAttributeMapping = settings.getCustomAttributeMapping();
					updateAdditionUserProfileAttributes(userName,customAttributeMapping, userInfoMap);
				}

				/*
				 * if (!settings.getKeepExistingUserRoles() && groupsToAssign.size() != 0 &&
				 * StringUtils.isNotBlank(settings.getRoleAttribute())) {
				 * LOGGER.info("keep existing roles unchecked "); if
				 * (userAccessor.hasMembership("confluence-administrators", userName)) {
				 * LOGGER.info("Member of administrator group"); } else {
				 * LOGGER.info("Not a Member of administrator group");
				 * removeUserFromGroups(userName, groupsToBeRemoved); }
				 * addUserToGroups(userAccessor.getUserByName(userName), groupsToAssign,
				 * Boolean.FALSE); } else LOGGER.info("KeepExistingUserRoles checked."); return
				 * userAccessor.getUserByName(userName);
				 */

				List<String> administratorNames = new ArrayList<>();
				try {
					administratorNames = userAccessor
							.getMemberNamesAsList(userAccessor.getGroup(UserAccessor.GROUP_CONFLUENCE_ADMINS));
				}catch(Exception e){
					LOGGER.error("Exception while getting admin users from confluence-administrator group :", e);
				}
				if (!settings.getKeepExistingUserRoles()) {
					LOGGER.debug("administratorNames : "+ administratorNames);
					LOGGER.info("Keep existing user roles is not checked.");
					if(administratorNames != null) {
						if (administratorNames.contains(userName) && administratorNames.size() == 1) {
							LOGGER.info("Only member of the admin group,cannot remove the user ");
						} else {
							LOGGER.info("Not a Member of administrator group");
							if (groupsToBeRemoved.isEmpty()) {
								LOGGER.debug("No groups to be removed from");
							} else {
								removeUserFromGroups(userName, groupsToBeRemoved);
							}
						}
					}else{
						LOGGER.debug("Failed to obtain admin user/group, User roles cannot updated");
					}
				} else {
					LOGGER.info("Keep existing user roles is checked.");
				}
				try {
					addUserToGroups(userAccessor.getUserByName(userName), groupsToAssign, groupValueList, Boolean.FALSE);
				}catch(Exception e){
					LOGGER.error("Exception Occurred While Mapping Groups", e);
				}

				return userAccessor.getUserByName(userName);
			} else if (!settings.getRestrictUserCreation()) {
				LOGGER.info("User DOES NOT exist. Creating new user.");
				LOGGER.debug("FullName: " + fullName + " userIdentity: " + userIdentity + " email: " + email);
				LOGGER.debug("groupsToAssign.size() :  " + groupsToAssign.size());

				// Instead of directly creating user with FIRST EMAIL found we need to check
				// if emailAttrValue is empty or if is not a valid email address
				String userEmail = StringUtils.EMPTY;
				if (StringUtils.isEmpty(emailAttrValue) || !oauthUtils.isEmailId(emailAttrValue))
					userEmail = email;
				else
					userEmail = emailAttrValue;
				LOGGER.debug("userEmail : " + userEmail);

				LOGGER.debug("!settings.getCreateUsersIfRoleMapped() : " + !settings.getCreateUsersIfRoleMapped());
				if ((!settings.getCreateUsersIfRoleMapped() || groupsToAssign.size() > 0)
						&& (StringUtils.isNotBlank(email))) {
					LOGGER.debug("User Details saved...!");
					ConfluenceUser createdUser = userAccessor.createUser(new DefaultUser(userIdentity, fullName, email),
							Credential.NONE);
					HashMap<String, String> customAttributeMapping = settings.getCustomAttributeMapping();
					updateAdditionUserProfileAttributes(userName ,customAttributeMapping, userInfoMap);
					try{
						addUserToGroups(createdUser, groupsToAssign, groupValueList, Boolean.TRUE);
					}catch(Exception e){
						LOGGER.error("Exception Occurred While Mapping Groups", e);
					}
					LOGGER.info("User Created...!");
					return createdUser;
				} else {
					if (StringUtils.isBlank(email)) {
						LOGGER.info("User creation failed, email not found in the response.");
					} else {
						LOGGER.debug(
								"User creation disabled for this user. Either create user is disabled or groups are not "
										+ "mapped for the email " + email);
					}
					redirectToLoginWithOAuthError(response, null, settings.getErrorMappingMap().get("UNAUTHORIZED_USER_LOGIN"));
					return null;
				}
			} else {
				LOGGER.error("User creation is disabled for all the users.");
				redirectToLoginWithOAuthError( response, null, settings.getErrorMappingMap().get("USER_NOT_FOUND"));
				return null;
			}

		} catch (Exception e) {
			LOGGER.error(e.getMessage());
		}
		redirectToLoginWithOAuthError( response, null, settings.getErrorMappingMap().get("SSO_AUTHENTICATION_FAILED"));
		return null;
	}

	private ArrayList<String> serializeGroups(ArrayList<String> roleValues) {
		LOGGER.debug("serializeGroups enterted");
		ArrayList<String> trimmedList = new ArrayList<String>();
		if (roleValues != null && roleValues.size() != 0) {

			for (int i = 0; i < roleValues.size(); i++) {
				if (StringUtils.contains(roleValues.get(i), ";")) {
					String roleValue = roleValues.remove(i);
					ArrayList<String> separatedValues = new ArrayList<String>(
							Arrays.asList(StringUtils.split(roleValue, ";")));
					roleValues.addAll(separatedValues);
				}
			}
			for (String roleValue : roleValues) {
				LOGGER.debug("Group Regex Pattern Enabled "+settings.getGroupRegexPatternEnabled());
				if(settings.getGroupRegexPatternEnabled()){
					LOGGER.debug("Transforming the rolevalue");
					roleValue = MoOAuthLoginServlet.getGroupnameFromRegexMethod(settings.getRegexPatternForGroup(),settings.getRegexGroups(),roleValue);
				}
				LOGGER.debug("TrimmedList :: "+trimmedList);
				trimmedList.add(roleValue.trim());
			}
		}
//		LOGGER.debug("this is the final list + " + trimmedList + "size :" +trimmedList.size() + "final value: "+ trimmedList.get(trimmedList.size()-1));
		return trimmedList;
	}

	private void addUserToGroups(ConfluenceUser newUser, List<Group> groupsToAssign, List<String> groupValueList, Boolean isNewUser)
			throws GroupNotFoundException, UserNotFoundException, OperationNotPermittedException,
			OperationFailedException {
		LOGGER.info("Adding user to groups");
		ConfluenceUser administratorUser = getAdministratorUser();
		UserAccessor userAccessor = (UserAccessor) ContainerManager.getComponent("userAccessor");
		String defaultGroup = settings.getDefaultGroup();
		List<String> defaultGroups = (List<String>) settings.getDefaultGroups();
		LOGGER.debug("defaultGroups : " + defaultGroups);
		if (!settings.getOnTheFlyGroupMapping()) {
			if (!settings.getKeepExistingUserRoles() || isNewUser) {
				LOGGER.info("keep existing user roles is unchecked");
				for (Group group : groupsToAssign) {
					LOGGER.debug("Assigning user " + newUser.getName() + " to " + group.getName());
					AuthenticatedUserThreadLocal.set(administratorUser);
					userAccessor.addMembership(group, newUser);
				}
			} else {
				LOGGER.info("keep existing user roles is checked.");
			}
			LOGGER.debug("Enable default groups for : " + settings.getEnableDefaultGroupsFor());
			AuthenticatedUserThreadLocal.set(administratorUser);
			if (!settings.getEnableDefaultGroupsFor().equals(MoOAuthPluginConstants.ENABLE_DEFAULT_GROUPS_FOR_NO_USERS)) {
				if (defaultGroups != null && defaultGroups.size() > 0) {
					for (String group : defaultGroups) {
						Group defaultConfluenceGroup = userAccessor.getGroup(group);
						if (administratorUser != null && defaultConfluenceGroup != null) {
							LOGGER.debug("Assigning user " + newUser.getName() + " to default group "
									+ defaultConfluenceGroup.getName());
							// userAccessor.addMembership(defaultConfluenceGroup, newUser);
							assignDefaultGroupsTo(isNewUser, newUser, defaultConfluenceGroup);
						}
					}
				} else {
					Group defaultConfluenceGroup = userAccessor.getGroup(defaultGroup);
					if (administratorUser != null && defaultConfluenceGroup != null) {
						LOGGER.debug("Assigning user " + newUser.getName() + " to default group "
								+ defaultConfluenceGroup.getName());
						assignDefaultGroupsTo(isNewUser, newUser, defaultConfluenceGroup);
					}
				}
			}
		}else {
			List<String> roleValuesList = new ArrayList<>();
			if (groupValueList != null && groupValueList.size() > 0) {
				for (String group : groupValueList) {
					if (StringUtils.isNotBlank(group) || StringUtils.isNotEmpty(group)) {
						roleValuesList.add(group);
					}
				}
			}

			if(!StringUtils.equals(settings.getOnTheFlyFilterIDPGroupsOption(), MoOAuthPluginConstants.ON_THE_FLY_NO_GROUP_FILTER)){
				roleValuesList = onTheFlyFilterIDPGroups(roleValuesList);
				LOGGER.debug("Filtered IDP groups :" + roleValuesList.toString());
			}

			LOGGER.debug("OAuth groups : " + roleValuesList);
			LOGGER.debug("On the Fly Group mapping is enabled");
			if (!BooleanUtils.toBoolean(isNewUser) && roleValuesList.size() > 0) {
				List<String> currentGroups = userAccessor.getGroupNames(newUser);
				LOGGER.debug("Current Groups of the user " + currentGroups.toString());
				List<String> groupsToRemove = new ArrayList<>();
				List<String> groupsKeepAsItIs = (List<String>) settings.getOnTheFlyDoNotRemoveGroups();
				LOGGER.debug("Groups to Keep As It is: " + groupsKeepAsItIs.toString());
				for (String groupName : currentGroups) {
					if (!groupsKeepAsItIs.contains(groupName)) {
						LOGGER.debug("Group to remove : " + groupName);
						groupsToRemove.add(groupName);
					}
				}
				LOGGER.debug("Groups to Remove the user from " + groupsToRemove.toString());
				if (!BooleanUtils.toBoolean(settings.getOnTheFlyAssignNewGroupsOnly())) {
					removeUserFromGroups(newUser.getName(), groupsToRemove);
				}
				List<String> currentGroupsDup = userAccessor.getGroupNames(newUser);
				LOGGER.debug("Current Groups of the user " + currentGroupsDup.toString());
			}
			List<String> groupsToAssignString = new ArrayList<>();
			LOGGER.debug("Role Value List : " + roleValuesList.size());
			if (roleValuesList.size() > 0) {
				groupsToAssignString = createAndAssignNewGroupsUser(roleValuesList);
			}
			LOGGER.debug("Enable default groups for : " + settings.getEnableDefaultGroupsFor());
			//Boolean assignDefaultGroupNone = !StringUtils.equalsIgnoreCase(idpConfig.getEnableDefaultGroupsFor(), MoOAuthPluginConstants.ENABLE_DEFAULT_GROUPS_FOR_NO_USERS);
			AuthenticatedUserThreadLocal.set(administratorUser);
			if (!settings.getEnableDefaultGroupsFor().equals(MoOAuthPluginConstants.ENABLE_DEFAULT_GROUPS_FOR_NO_USERS)) {
				LOGGER.debug("Assigning Default group to User");
				if (defaultGroups != null && defaultGroups.size() > 0) {
					for (String group : defaultGroups) {
						Group confluenceGroup = userAccessor.getGroup(group);
						if (administratorUser != null && confluenceGroup != null) {
							assignDefaultGroupsTo(isNewUser, newUser, confluenceGroup);
						}
					}
				}
			}
			if (!settings.getKeepExistingUserRoles() || isNewUser) {
				LOGGER.debug("OTF groups : " + groupsToAssignString);
				for (String group : groupsToAssignString) {
					Group confluenceGroup = userAccessor.getGroup(group);
					userAccessor.addMembership(confluenceGroup, newUser);
				}
			} else {
				LOGGER.debug("keep existing user roles is checked");
			}
		}

	}

	public void assignDefaultGroupsTo(Boolean isNewUser, ConfluenceUser newUser, Group defaultConfluenceGroup) {
		if (BooleanUtils.toBoolean(isNewUser)) {
			LOGGER.debug(
					"Assigning user " + newUser.getName() + " to default group " + defaultConfluenceGroup.getName());
			userAccessor.addMembership(defaultConfluenceGroup, newUser);
		} else if (settings.getEnableDefaultGroupsFor()
				.equals(MoOAuthPluginConstants.ENABLE_DEFAULT_GROUPS_FOR_ALL_USERS)) {
			LOGGER.debug(
					"Assigning user " + newUser.getName() + " to default group " + defaultConfluenceGroup.getName());
			userAccessor.addMembership(defaultConfluenceGroup, newUser);
		}
	}

	private void removeUserFromGroups(String username, List<String> groupsToRemove)
			throws OperationNotPermittedException, OperationFailedException {
		LOGGER.info("Removing user from groups");
		try {
			ConfluenceUser administratorUser = getAdministratorUser();
			UserAccessor userAccessor = (UserAccessor) ContainerManager.getComponent("userAccessor");
			AuthenticatedUserThreadLocal.set(administratorUser);
			for (String group : groupsToRemove) {
				if (userAccessor.hasMembership(group, username)) {
					LOGGER.debug("Removing user " + username + " from " + group);
					userAccessor.removeMembership(group, username);
				}
			}
		}catch (Exception e){
			LOGGER.error("Exception while removing user from groups :", e);
		}
	}
		private List<String> createAndAssignNewGroupsUser (List<String> roleValuesList) {
			List<String> groupsToAssign = new ArrayList<>();
			try {
				GroupManager groupManager = (GroupManager) ContainerManager.getInstance().getContainerContext().getComponent("groupManager");
				Boolean canCreateNewGroups = settings.getCreateNewGroups();
				for (String groupsName : roleValuesList) {
					if (groupManager.getGroup(groupsName.trim()) == null && BooleanUtils.toBoolean(canCreateNewGroups)) {
						LOGGER.debug("Creating new Group with name : " + groupsName);
						try {
							Group confGroup = groupManager.createGroup(groupsName.trim());
							LOGGER.debug("confGroup = " + confGroup.getName());
							groupsToAssign.add(confGroup.getName());
						} catch (Exception e) {
							LOGGER.debug("Issue with creating new Group with name : " + groupsName);
						}
					} else if (groupManager.getGroup(groupsName.trim()) != null) {
						Group confGroup = groupManager.getGroup(groupsName.trim());
						LOGGER.debug("confGroup = " + confGroup.getName());
						groupsToAssign.add(confGroup.getName());
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			return groupsToAssign;
		}

	/*
	 * private Boolean authoriseUserAndEstablishSession(Principal principal,
	 * HttpServletRequest request, HttpServletResponse response) throws
	 * NoSuchMethodException, InvocationTargetException, IllegalAccessException,
	 * AuthenticatorException {
	 * 
	 * LOGGER.debug("authoriseUserAndEstablishSession Creating user session for: " +
	 * principal.getName()); Boolean result =
	 * oAuthAuthenticator.createUserSession(request, response, principal);
	 * LOGGER.debug("Creating user session result: " + result); if
	 * (result.booleanValue()) { UserAccessor userAccessor = (UserAccessor)
	 * ContainerManager.getComponent("userAccessor"); LOGGER.debug("user name: " +
	 * principal.getName()); ConfluenceUser confluenceUser =
	 * userAccessor.getUserByName(principal.getName());
	 * LOGGER.debug("confluenceUser : "+ confluenceUser);
	 * LOGGER.debug(userAccessor.hasMembership("confluence-administrators",
	 * principal.getName())); LOGGER.debug("Is Confluence Administrator: " +
	 * permissionManager.isConfluenceAdministrator(userAccessor.getUserByName(
	 * principal.getName()))); LOGGER.debug("Is System Administrator: " +
	 * permissionManager.isSystemAdministrator(userAccessor.getUserByName(principal.
	 * getName()))); if
	 * (permissionManager.isConfluenceAdministrator(userAccessor.getUserByName(
	 * principal.getName()))) { LOGGER.debug("User has admin privileges"); if
	 * (StringUtils.isEmpty(settings.getAdminSessionOption())) {
	 * LOGGER.debug("session is starting.....");
	 * defaultWebSudoSessionManager.startSession(request, response); } else { Cookie
	 * cookie = MoOAuthHttpUtils.getCookie("mo.confluence-oauth.LOGOUTCOOKIE",
	 * request); Boolean isLogoutCookieSet = cookie!=null &&
	 * StringUtils.equalsIgnoreCase(cookie.getValue(), "LogoutCookie"); if
	 * (BooleanUtils.toBoolean(isLogoutCookieSet)) {
	 * LOGGER.debug("User has admin access & he is already logged in");
	 * defaultWebSudoSessionManager.startSession(request, response); } else {
	 * LOGGER.debug("Creation of admin session disabled"); } } }
	 * 
	 * LOGGER.debug("Create Logout Cookie...");
	 * 
	 * Cookie logoutCookie =
	 * MoOAuthHttpUtils.createCookie(request.getRequestURL().toString(),
	 * "mo.confluence-oauth.LOGOUTCOOKIE", "LogoutCookie");
	 * response.addCookie(logoutCookie);
	 * 
	 * }
	 * 
	 * LOGGER.debug("result : "+ result); return result; }
	 */

	private Boolean authoriseUserAndEstablishSession(DefaultAuthenticator authenticator, ConfluenceUser userObject,
			HttpServletRequest request, HttpServletResponse response, Boolean shouldCreateAdminSession)
			throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {

		LOGGER.info("Authorising user and establishing session");

		Principal principal = userObject;
		Boolean result = setRememberMeCookie(authenticator, userObject, request, response);

		//Boolean result = oAuthAuthenticator.createUserSession(request, response, principal);
		LOGGER.debug("result : " + result);

//        PermissionManager permissionManager = (PermissionManager) ContainerManager.getComponent("permissionManager");
//        Boolean isAdminUser = permissionManager.isConfluenceAdministrator(userObject) || permissionManager.isSystemAdministrator(userObject);

		if (result) {
			// UserAccessor userAccessor = (UserAccessor)
			// ContainerManager.getComponent("userAccessor");

			LOGGER.debug("user name: " + principal.getName());
			if (shouldCreateAdminSession) {
				LOGGER.info("Creating Admin Session");
				defaultWebSudoSessionManager.startSession(request, response);
				} else {
					LOGGER.debug("Creation of admin session disabled");
			}
			/*
			 * else { LOGGER.info("Admin session is not created"); Cookie cookie =
			 * MoOAuthHttpUtils.getCookie("mo.confluence-oauth.LOGOUTCOOKIE", request);
			 * Boolean isLogoutCookieSet = cookie!=null &&
			 * StringUtils.equalsIgnoreCase(cookie.getValue(), "LogoutCookie"); if
			 * (BooleanUtils.toBoolean(isLogoutCookieSet) &&
			 * BooleanUtils.toBoolean(isAdminUser)) {
			 * LOGGER.debug("User has admin access & he is already logged in");
			 * defaultWebSudoSessionManager.startSession(request, response); } else {
			 * LOGGER.error("Creation of admin session disabled"); } }
			 */

		}

		return result;
	}

	private Boolean shouldCreateAdminSession(ConfluenceUser confluenceUser, HttpServletRequest request) {
		LOGGER.info("Checking if admin session should be created");
		String relayStateUrl = getRelayStateURL(request);
		LOGGER.debug("relayStateUrl : " + relayStateUrl);
		PermissionManager permissionManager = (PermissionManager) ContainerManager.getComponent("permissionManager");
		LOGGER.debug("AdminSessionOption: " + settings.getAdminSessionOption());
		LOGGER.debug("permissionManager: " + permissionManager);
		Cookie cookie = MoOAuthHttpUtils.getCookie("mo.confluence-oauth.LOGOUTCOOKIE", request);
		Cookie formCookie = MoOAuthHttpUtils.getCookie("mo.confluence-oauth.FORM_COOKIE", request);
		Boolean isLogoutCookieSet = cookie != null && StringUtils.equalsIgnoreCase(cookie.getValue(), "LogoutCookie");
		LOGGER.debug("AdminSessionOption: " + settings.getAdminSessionOption());
		if (permissionManager != null) {
			if (permissionManager.isConfluenceAdministrator(confluenceUser) && isLogoutCookieSet) {
				LOGGER.debug("User " + confluenceUser.getName() + " is Confluence Administrator.");
				return true;
			} else if (permissionManager.isConfluenceAdministrator(confluenceUser)
					&& !BooleanUtils.toBoolean(settings.getAdminSessionOption())) {
				return Boolean.TRUE;
			} else {
			    String formCookieValue = StringUtils.EMPTY;
			    if(formCookie != null){
					formCookieValue = formCookie.getValue();
				}
				LOGGER.info("User does not have admin or system permissions..");
				if ((StringUtils.containsIgnoreCase(relayStateUrl, "/spaces") && isLogoutCookieSet)
						|| StringUtils.equals(formCookieValue, "authenticateform")) {
					LOGGER.info("Checking for space administration");
					/** Splitting the relaystate to get the space key */
					Map<String, String> queryMap = getQueryMap(relayStateUrl);
					if (queryMap.containsKey("key")) {
						String spaceKey = queryMap.get("key");
						LOGGER.debug("Spacekey: " + spaceKey + " | Username: " + confluenceUser.getName());
						SpaceManager spaceManager = (SpaceManager) ContainerManager.getComponent("spaceManager");
						Space accessedSpace = spaceManager.getSpace(spaceKey);
						LOGGER.debug("Space Info: " + accessedSpace.getName());
						if (accessedSpace != null) {
							LOGGER.debug("Checking space permission");
							SpacePermissionManager spacePermissionManager = (SpacePermissionManager) ContainerManager
									.getComponent("spacePermissionManager");
							LOGGER.debug("Has Space Administer Permission: " + spacePermissionManager.hasPermission(
									SpacePermission.ADMINISTER_SPACE_PERMISSION, accessedSpace, confluenceUser));

							LOGGER.debug("returning : " + spacePermissionManager.hasPermission(
									SpacePermission.ADMINISTER_SPACE_PERMISSION, accessedSpace, confluenceUser));

							return spacePermissionManager.hasPermission(SpacePermission.ADMINISTER_SPACE_PERMISSION,
									accessedSpace, confluenceUser);
						}
					}
				}
			}
		}

		return false;
	}

	private Map<String, String> getQueryMap(String relayStateUrl) {
		Map<String, String> queryMap = new HashMap<>();
		String[] splitStrings = relayStateUrl.split("\\?|\\&");
		for (String splitString : splitStrings) {
			if (StringUtils.contains(splitString, "=")) {
				String[] keyValue = splitString.split("=");
				queryMap.put(keyValue[0], keyValue[1]);
			}
		}

		return queryMap;
	}

	/* Redirects user to Dashboard on successful Log in */
	private void redirectToSuccessfulAuthLandingPage(HttpServletRequest request, HttpServletResponse response)
			throws IOException {

		String redirectUrl = null;

		if (!StringUtils.isBlank(settings.getRelayState())) {
			redirectUrl = settings.getRelayState();
			LOGGER.info("Default Relay State URL ");
		} else if (StringUtils.isNoneEmpty(getRelayStateURL(request))) {
			LOGGER.info("Last Access Relay State URL ");
			redirectUrl = getRelayStateURL(request);
		} else {
			redirectUrl = settings.getBaseUrl();
		}

		LOGGER.debug("Relay State URL = " + redirectUrl);

		if (StringUtils.startsWith(redirectUrl, "/") && !StringUtils.contains(redirectUrl, this.settings.getBaseUrl())) {
			redirectUrl = settings.getBaseUrl().concat(redirectUrl);
		}

		if (StringUtils.startsWithIgnoreCase(redirectUrl, "/")) {
			redirectUrl = redirectUrl.startsWith("/") ? redirectUrl.substring(1) : redirectUrl;
		}
		/*
		 * if ((StringUtils.isNotBlank(relayState))) { if
		 * (StringUtils.contains(relayState, "://")) { redirectUrl = relayState; } else
		 * { redirectUrl = settings.getBaseUrl().concat(relayState); } } else {
		 * redirectUrl = settings.getBaseUrl(); }
		 */

		clearRelayStateURLCookie(response);

		LOGGER.debug("Redirecting user to: " + redirectUrl);

		if(StringUtils.startsWithIgnoreCase(redirectUrl, "http") && !StringUtils.contains(redirectUrl, settings.getBaseUrl())){
			redirectUrl = settings.getBaseUrl();
		}

		MoOAuthManager.httpRedirect(response, redirectUrl);
	}

	/* Redirects user to Login page due to some error in login process */
	private void redirectToLoginWithOAuthError(HttpServletResponse response, Exception exception, String errorMsg)
			throws IOException {
		LOGGER.error("Authentication failed with error: " + errorMsg);

		String redirectUrl = settings.getLoginPageUrl() + "?oautherror=" + errorMsg;
		try {
			if (BooleanUtils.toBoolean(settings.getEnableErrorMsgTemplate() || (settings.getDefaultLoginDisabled()))) {
				LOGGER.info("Redirecting user to Error Message Template");
				Map context = MacroUtils.defaultVelocityContext();
				context.put("baseUrl", settings.getBaseUrl()+"/login.action");
				if(StringUtils.isBlank(errorMsg))
					errorMsg = "SSO Authentication Failed. Please contact your administrator for more details.";
				context.put("errorMsg",errorMsg);
				String result = VelocityUtils.getRenderedContent((CharSequence) settings.getErrorMsgTemplate(),
						context);
				LOGGER.debug("result = " + result);
				response.setContentType("text/html;charset=utf-8");
				response.getWriter().write(result);
			} else {
				LOGGER.debug("Showing default error message");
				Map<String, Object> context = new HashMap();
				String baseURL = "";
				baseURL = settings.getBaseUrl();
				if(StringUtils.isBlank(errorMsg))
					errorMsg = "SSO Authentication Failed. Please contact your administrator for more details.";
				context.put("baseUrl",baseURL);
				context.put("errorMsg",errorMsg);
				response.setContentType("text/html;charset=utf-8");
				this.renderer.render(DEFAULT_ERROR_MESSAGE, context, response.getWriter());
				response.setContentType("text/html;charset=utf-8");
			}
		} catch (Exception e) {
			LOGGER.error("Exception occured while overwritting file.");
			MoOAuthManager.httpRedirect(response, redirectUrl);
		}

	}

	private void authoriseAndRedirect(JsonObject userInfoData, HttpServletRequest request, HttpServletResponse response,
			String email, Boolean regexEnabled, String accessToken, HashMap<String, String> userInfoMap,
			IProtocolAction protocolAction, String userDetailedInfoResponse ) throws IOException {
		LOGGER.info("Called authoriseAndRedirect...");
		try {
			Boolean isAllowed = Boolean.TRUE;

			LOGGER.info("Fetching Email.....");
			String emailAttribute = settings.getEmailAttribute();
			String emailAttrValue = StringUtils.EMPTY;
			if (StringUtils.isNotBlank(emailAttribute)) {
				String[] emailAttrValueArray = emailAttribute.split(";");
				for(String emailAttr:emailAttrValueArray) {
					emailAttr = StringUtils.trimToEmpty(emailAttr);
					if (oauthUtils.checkIfKeyExist(userInfoMap, emailAttr)) {
						emailAttrValue = oauthUtils.getValue(userInfoMap, emailAttr);
						break;
					} else {
						emailAttrValue = oauthUtils.findKey(userInfoData, emailAttr,
								settings.getAppName());
					}
				}
				LOGGER.debug("emailAttrValue "+emailAttrValue);
				if (StringUtils.isNotBlank(emailAttrValue)) {
					email = emailAttrValue;
				}
			}
			LOGGER.debug("email address : " + email);

			String username = "";

			LOGGER.info("Fetching Username...");
			if (StringUtils.isNotEmpty(settings.getUsernameAttribute())) {
				LOGGER.info("Fetching Username from configured username attribute.");
				if (oauthUtils.checkIfKeyExist(userInfoMap, settings.getUsernameAttribute())) {
					username = oauthUtils.getValue(userInfoMap, settings.getUsernameAttribute());
					username = username.replace("\"","");
				}
				else {
					username = oauthUtils.findKey(userInfoData, settings.getUsernameAttribute(), settings.getAppName());
					username = username.replace("\"","");
				}
			}

			LOGGER.debug("username : " + username);

		 Authenticator authenticator = SecurityConfigFactory.getInstance().getAuthenticator();

			if (StringUtils.isNotBlank(email) || StringUtils.isNotBlank(username)) {

				/* check regex */
				if (regexEnabled && StringUtils.isNotBlank(username)) {
					LOGGER.info("regex enabled...");
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
						redirectToLoginWithOAuthError( response, null , settings.getErrorMappingMap().get("SSO_AUTHENTICATION_FAILED"));
						return;
					}
				}

				String userIdentity = StringUtils.EMPTY;

				if (StringUtils.equalsIgnoreCase(settings.getLoginUserAttribute(), "email")) {
					LOGGER.info("searching confluence user by email attribute value");
					SearchResult searchResult = userAccessor.getUsersByEmail(email);
					Pager<User> pager = searchResult.pager();
					int count = 0;
					for (User user : pager) {
						if (count == 0) {
							LOGGER.debug("found user by email = " + user.getEmail());
							username = user.getName();
							userIdentity = user.getName();

						} else if (count > 1) {
							LOGGER.error("more than one user found with this email address: "+email);
							redirectToLoginWithOAuthError( response, null , settings.getErrorMappingMap().get("SSO_AUTHENTICATION_FAILED"));
						}
						count++;
					}
					if (count > 1) {
						LOGGER.error("more than one user found with this email address: "+email);
						redirectToLoginWithOAuthError( response, null , settings.getErrorMappingMap().get("SSO_AUTHENTICATION_FAILED"));
					}

					if (StringUtils.isEmpty(userIdentity)) {
						// we need to put emailAttrValue has user identity only when it is not empty plus and it is valid email address
						if (StringUtils.isEmpty(email)) {
							userIdentity = username;
						} else {
							userIdentity = email;
						}
					}

				} else {
					LOGGER.info("Login using UserName...");

					if (StringUtils.isEmpty(username)) {
						LOGGER.debug("username is empty, assigning email");
						userIdentity = email;
					} else {
						userIdentity = username;
					}
				}

				Boolean userexist=userAccessor.exists(userIdentity);
				if (userexist) {
					LOGGER.debug("User exists." + userIdentity);
					if (StringUtils.isEmpty(username)) {
						username = email;
					}
				}
				ConfluenceUser confluenceUser =userAccessor.getUserByName(username);
				Boolean hasViewAccess=true;
				String spaceURL;
				if (StringUtils.isNoneEmpty(getRelayStateURL(request))) {
					spaceURL = getRelayStateURL(request);
				} else {
					spaceURL = settings.getBaseUrl();
				}
                List<String> spaceKey= Arrays.asList(spaceURL.split("/"));
                int spaceKeyIndex = spaceKey.indexOf("display")+1;
                if(spaceKeyIndex>0){
                    LOGGER.debug("CONTAINS KEY: "+spaceKey.get(spaceKeyIndex));
                    if (spaceKey.get(spaceKeyIndex)!=null) {
                        SpaceManager spaceManager = (SpaceManager) ContainerManager.getComponent("spaceManager");
                        Space accessedSpace = spaceManager.getSpace(spaceKey.get(spaceKeyIndex));
                        if (accessedSpace != null) {
                            SpacePermissionManager spacePermissionManager = (SpacePermissionManager) ContainerManager
                                .getComponent("spacePermissionManager");
                            hasViewAccess=spacePermissionManager.hasPermission(SpacePermission.VIEWSPACE_PERMISSION, accessedSpace, confluenceUser);
                            LOGGER.debug("HAS VIEW ACCESS: "+hasViewAccess);
                        }
                    }
                }else{
                    try{
                    	PermissionManager permissionManager = (PermissionManager) ContainerManager.getComponent("permissionManager");
                    	hasViewAccess= permissionManager.hasPermission(confluenceUser,Permission.VIEW,PermissionManager.TARGET_APPLICATION);
                    }catch(Exception e){
                       LOGGER.error("Error occured while checking user permission: "+e); 
                    }
				}
				Boolean isDeactivated = BooleanUtils.toBoolean(userAccessor.isDeactivated(username));
				if ((!userexist && settings.getRestrictUserCreation())||(userexist && !isDeactivated && !hasViewAccess)) {
					if(settings.getAllowGuestLogin() && MoOAuthPluginHandler.isGlobalAnonymousAccessEnabled() && settings.getDisableAnonymousAccess()){
						Cookie guestCookie = MoOAuthHttpUtils.createCookie(
							request.getRequestURL().toString(),
							MoOAuthPluginConstants.GUEST_COOKIE,
							MoOAuthEncryptionUtils.encrypt(
								settings.getCustomerTokenKey(), "GUEST_COOKIE"),settings.getRememberMeCookieEnabled());
						guestCookie.setMaxAge(Integer.parseInt(settings.getGuestSessionTimeout())*60);
						response.addCookie(guestCookie);
						LOGGER.debug("Guest Cookie added"+guestCookie);
						redirectToSuccessfulAuthLandingPage(request,response);
						return;
					}
				}
				else if (userexist) {
					if (isDeactivated) {
						if (!settings.getAutoActivateUser()) {
							LOGGER.debug("User is deactivated. Can't create user session, redirecting to the login page");
							redirectToLoginWithOAuthError( response, null , settings.getErrorMappingMap().get("SSO_AUTHENTICATION_FAILED"));
							return;
						}
					}
				}
				else{
					LOGGER.debug("User creation is enabled...Creating new user");
					confluenceUser=userAccessor.getUserByName(tryCreateOrUpdateUser(request,response, email, username, userInfoData, accessToken,
							userInfoMap, protocolAction, userDetailedInfoResponse).getName());
					userexist=true;
				}
				
				if(userexist){
					if (StringUtils.isNotEmpty(settings.getAllowedDomains()) && StringUtils.isNotEmpty(email))
						isAllowed = checkForAllowedDomain(email);
					if (BooleanUtils.toBoolean(isAllowed)) {
						LOGGER.debug("email before calling trycreateorupdateuser : " + email);
						Principal principal = tryCreateOrUpdateUser(request, response, email, username, userInfoData, accessToken,
								userInfoMap, protocolAction, userDetailedInfoResponse);
						if (principal != null) {
							LOGGER.debug("Establishing session for: " + principal.getName());
							confluenceUser = userAccessor.getUserByName(principal.getName());
							Boolean shouldCreateAdminSession = shouldCreateAdminSession(confluenceUser, request);
							LOGGER.debug("shouldCreateAdminSession : " + shouldCreateAdminSession);
							Boolean result = authoriseUserAndEstablishSession((DefaultAuthenticator) authenticator, confluenceUser, request, response,
									shouldCreateAdminSession);
							LOGGER.debug("result : " + result);
							LoginManager loginManager = (LoginManager) ContainerManager.getComponent("loginManager");
							UserLoginInfo userLoginInfo = new UserLoginInfo(confluenceUser);
							UserAccessor userAccessor = (UserAccessor) ContainerManager.getComponent("userAccessor");

							if (result.booleanValue()) {
								LOGGER.debug("Session created. Redirecting user to : " + getRelayStateURL(request));

								LOGGER.info("Create Logout Cookie...");
								Cookie logoutCookie = MoOAuthHttpUtils.createCookie(request.getRequestURL().toString(),
										"mo.confluence-oauth.LOGOUTCOOKIE", "LogoutCookie",settings.getRememberMeCookieEnabled());
								response.addCookie(logoutCookie);

								userLoginInfo.successfulLogin(new Date());
								loginManager.onSuccessfulLoginAttempt(confluenceUser.getName(), request);
								redirectToSuccessfulAuthLandingPage(request, response);
								return;
							}
							LOGGER.error("Session could NOT be created. Redirecting user to login page.");
							userLoginInfo.failedLogin(new Date());
							loginManager.onFailedLoginAttempt(confluenceUser.getName(), request);
						}
					} else {
						redirectToLoginWithOAuthError( response, null, settings.getErrorMappingMap().get("USER_DOMAIN_NOT_ALLOWED"));
						return;
					}
					return;
				}
				redirectToLoginWithOAuthError( response, null, settings.getErrorMappingMap().get("USER_NOT_FOUND"));
				return;
			}
			if (StringUtils.isBlank(username) && StringUtils.isBlank(email)) {
				LOGGER.error("Username and Email not received in the OAuth Response. Please check your configuration.");
			} else if (StringUtils.isBlank(username)) {
				LOGGER.error("Username not received in the OAuth Response. Please check your configuration.");
			}
			redirectToLoginWithOAuthError( response, null , settings.getErrorMappingMap().get("SSO_AUTHENTICATION_FAILED"));
		} catch (Exception e) {
			LOGGER.error("An error occurred while signing in the user.", e);
			redirectToLoginWithOAuthError( response, null , settings.getErrorMappingMap().get("SSO_AUTHENTICATION_FAILED"));
			return;
		}

	}

	/*
	 * private List<Group> getListOfMappedGroupsToAssign(ArrayList<String>
	 * roleValuesList) { List<Group> groupsToAssign = new ArrayList<>();
	 * UserAccessor userAccessor = (UserAccessor)
	 * ContainerManager.getComponent("userAccessor"); if (roleValuesList != null &&
	 * roleValuesList.size() > 0) { HashMap<String, String> roleMapping =
	 * settings.getRoleMapping(); Iterator<String> it =
	 * roleMapping.keySet().iterator(); while (it.hasNext()) { String key =
	 * it.next(); String value = roleMapping.get(key); String[] groupNamesConfigured
	 * = StringUtils.split(value, ";"); for (int i = 0; i <
	 * groupNamesConfigured.length; i++) { String groupValue =
	 * groupNamesConfigured[i]; if (roleValuesList.contains(groupValue)) { Group
	 * confluenceUsersGroup = (Group) userAccessor.getGroup(key); if
	 * (confluenceUsersGroup != null) { groupsToAssign.add(confluenceUsersGroup); }
	 * } } } } return groupsToAssign; }
	 */

	private List<List<Group>> getListOfMappedGroupsToAssign(ArrayList<String> roleValuesList) {
		List<Group> groupsToAssign = new ArrayList<>();
		List<Group> groupsMapped = new ArrayList<>();
		List<List<Group>> returnedMappedGroups = new ArrayList<>();
		UserAccessor userAccessor = (UserAccessor) ContainerManager.getComponent("userAccessor");
		if (roleValuesList != null && roleValuesList.size() > 0) {
			Map<String, String> roleMapping = settings.getRoleMapping();
			Iterator<String> it = roleMapping.keySet().iterator();
			LOGGER.debug("GET LIST: " + roleMapping.size());
			while (it.hasNext()) {
				String key = it.next();
				String value = roleMapping.get(key);
				String[] groupNamesConfigured = StringUtils.split(value, ";");
				for (int i = 0; i < groupNamesConfigured.length; i++) {
					Group confluenceUsersGroup = (Group) userAccessor.getGroup(key);
					if (confluenceUsersGroup != null) {
						groupsMapped.add(confluenceUsersGroup);
					}
					String groupValue = groupNamesConfigured[i];
					if (roleValuesList.contains(groupValue)) {
						// Group confluenceUsersGroup = (Group) userAccessor.getGroup(key);
						if (confluenceUsersGroup != null) {
							groupsToAssign.add(confluenceUsersGroup);
						}
					}
				}
			}
		}
		returnedMappedGroups.add(groupsToAssign);
		returnedMappedGroups.add(groupsMapped);
		return returnedMappedGroups;
	}

	/*public Boolean verifyToken(String[] id_token_parts) {
		boolean isSignatureValid = false;
		try {

			byte[] ID_TOKEN_SIGNATURE = base64UrlDecodeToBytes(id_token_parts[2]);
			byte[] data = (id_token_parts[0] + "." + id_token_parts[1]).getBytes(StandardCharsets.UTF_8);
			Base64 base64Url = new Base64(true);
			String header = new String(base64Url.decode(id_token_parts[0]));
			LOGGER.debug("header_Obtained" + header);
			JSONObject jsonHeader = new JSONObject(header);
			String kidFromHeader = jsonHeader.optString("kid");
			LOGGER.debug("kidFromHeader" + kidFromHeader);
			String publicCertificate = StringUtils.EMPTY;
			if (StringUtils.isNoneEmpty(settings.getPublicKey())
					&& settings.getValidateSignatureMethod().equals("publicKeySelect")) {
				LOGGER.info("validating using public Key");
				publicCertificate = settings.getPublicKey();
				publicCertificate = MoOAuthUtils.deserializePublicKey(publicCertificate);
				byte[] publicBytes = Base64.decodeBase64(publicCertificate);
				X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicBytes);
				KeyFactory keyFactory = KeyFactory.getInstance("RSA");
				PublicKey publicKey = keyFactory.generatePublic(keySpec);
				LOGGER.debug("public Key:" + publicKey);
				isSignatureValid = verifyUsingPublicKey(data, ID_TOKEN_SIGNATURE, publicKey);
				LOGGER.debug("isSignatureValid : " + isSignatureValid);
				return isSignatureValid;
			} else if (StringUtils.isNotBlank(settings.getjWKSEndpointURL())
					&& settings.getValidateSignatureMethod().equals("JWKSEndPointURLSelect")) {
				LOGGER.debug("validating using jwks");
				String JWKS = MoOAuthHttpUtils.sendGetRequest(settings.getjWKSEndpointURL());
				JSONObject JSONWebKeySet = new JSONObject(JWKS);
				PublicKey publicKey;
				JSONArray keys = JSONWebKeySet.optJSONArray("keys");
				HashMap<String, ArrayList<String>> keyTable = new HashMap<>();
				if (keys == null) {
					LOGGER.error("Keys Are Empty");
					return isSignatureValid;
				} else {
					for (int i = 0; i < keys.length(); i++) {
						JSONObject key = keys.getJSONObject(i);
						String kid = key.optString("kid");
						String n = key.optString("n");
						String e = key.optString("e");
						String x5c = key.optString("x5c");
						keyTable.computeIfAbsent(kid, k -> new ArrayList<>()).add(n);
						keyTable.computeIfAbsent(kid, k -> new ArrayList<>()).add(e);
						keyTable.computeIfAbsent(kid, k -> new ArrayList<>()).add(x5c);
					}
					String n_obtained = keyTable.get(kidFromHeader).get(0);
					LOGGER.debug("n_obtained" + n_obtained);
					String e_obtained = keyTable.get(kidFromHeader).get(1);
					LOGGER.debug("e_obtained" + e_obtained);
					String x5c_obtained = keyTable.get(kidFromHeader).get(2);
					LOGGER.debug("x5c_obtained" + x5c_obtained);
					if (StringUtils.isEmpty(x5c_obtained)) {
						LOGGER.debug("X5C VALUE IS EMPTY");
						LOGGER.debug("generating public key through modulus and exponent");
						byte[] n_obtained_byte = Base64.decodeBase64(n_obtained);
						byte[] e_obtained_byte = Base64.decodeBase64(e_obtained);
						BigInteger modulus = new BigInteger(1, n_obtained_byte);
						BigInteger exponent = new BigInteger(1, e_obtained_byte);
						RSAPublicKeySpec spec = new RSAPublicKeySpec(modulus, exponent);
						KeyFactory factory = KeyFactory.getInstance("RSA");
						publicKey = factory.generatePublic(spec);
					} else {
						LOGGER.debug("validating jwt using x.509 chain.");
						publicCertificate = x5c_obtained;
						CertificateFactory factory = CertificateFactory.getInstance("X.509");
						X509Certificate cert = (X509Certificate) factory.generateCertificate(
								new ByteArrayInputStream(DatatypeConverter.parseBase64Binary(publicCertificate)));
						publicKey = (RSAPublicKey) cert.getPublicKey();
					}
					isSignatureValid = verifyUsingPublicKey(data, ID_TOKEN_SIGNATURE, publicKey);
					LOGGER.debug("isSignatureValid : " + isSignatureValid);
					return isSignatureValid;
				}
			}

		} catch (GeneralSecurityException | JSONException e) {
			LOGGER.error(e.getMessage());
			return isSignatureValid;
		}
		return isSignatureValid;
	}*/
	
	public void updateAdditionUserProfileAttributes(String username, HashMap<String, String> addionalProfileAttribute, HashMap<String, String> userInfoMap) {
		LOGGER.debug("Updating Additional User Profile Information : " + username);
		try {
			Thread currentThread = Thread.currentThread();
			currentThread.setContextClassLoader(LdapHostnameVerificationSSLSocketFactory.class.getClassLoader());
			AuthenticatedUserThreadLocal.set(getAdministratorUser());
			ConfluenceUser confluenceUser = userAccessor.getUserByName(username);
			String userProperty;
			for (String userPropertyKey : addionalProfileAttribute.keySet()) {
				try {
					userProperty = oauthUtils.getValue(userInfoMap, addionalProfileAttribute.get(userPropertyKey));
				} catch (Exception e) {
					userProperty = StringUtils.EMPTY;
				}
				userDetailsManager.setStringProperty(confluenceUser, userPropertyKey.toLowerCase(), StringUtils.defaultString(userProperty));
			}
		} catch (Exception e) {
			LOGGER.error("An error occured while updating additional profile of a user.");
		}
	}
	private Boolean verifyTokenSignature(DecodedJWT JWTToken, PublicKey publicKey, String signatureAlgorithm) {
		try {

			LOGGER.debug("Public Key's Algorithm :  " + publicKey.getAlgorithm());
			
			LOGGER.debug("Algorithm used to sign JWT : " + signatureAlgorithm);

			// Default Algorithm
			Algorithm algorithm = Algorithm.RSA256((RSAPublicKey) publicKey);

			// Check for Algorithm used in received JWT token and update Algorithm
			if (StringUtils.equalsIgnoreCase(signatureAlgorithm, "RS512")) {
				algorithm = Algorithm.RSA512((RSAPublicKey) publicKey);
			} else if (StringUtils.equalsIgnoreCase(signatureAlgorithm, "RSA384")) {
				algorithm = Algorithm.RSA384((RSAPublicKey) publicKey);
			}

			LOGGER.debug("Algorithm object : " + algorithm.getName());

			LOGGER.debug("Public Cert : " + publicKey.toString());
			JWTVerifier verifier = JWT.require(algorithm).build();
			DecodedJWT verifiedJWT = verifier.verify(JWTToken.getToken());
			LOGGER.debug("decoded JWT Token : " + verifiedJWT.getToken());
			LOGGER.debug("Signature Verified");
			return true;
		} catch (JWTVerificationException e) {
			// TODO: handle exception
			LOGGER.error("Singnature verification failed ",e);
			return false;
		} catch (Exception e) {
			LOGGER.error("Unknown exception occurs while verifying Singnature ", e);
			return false;
		}
	}

	public byte[] base64UrlDecodeToBytes(String input) {
		Base64 decoder = new Base64(-1, null, true);
		byte[] decodedBytes = decoder.decode(input);

		return decodedBytes;
	}

	private static boolean verifyUsingPublicKey(byte[] data, byte[] signature, PublicKey pubKey)
			throws GeneralSecurityException {
		Signature sig = Signature.getInstance("SHA256withRSA");
		sig.initVerify(pubKey);
		sig.update(data);

		return sig.verify(signature);
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

	private ConfluenceUser getAdministratorUser() {
		try {
			UserAccessor userAccessor = (UserAccessor) ContainerManager.getComponent("userAccessor");
			List<String> administratorNames = userAccessor
					.getMemberNamesAsList(userAccessor.getGroup(UserAccessor.GROUP_CONFLUENCE_ADMINS));
			if (administratorNames != null && administratorNames.size() > 0) {
				for (String user : administratorNames) {
					ConfluenceUser confluenceAdminUser = userAccessor.getUserByName(user);
					LOGGER.debug("Confluence Admin User = " + confluenceAdminUser);
					if (permissionManager.isSystemAdministrator(confluenceAdminUser)
							&& !userAccessor.isDeactivated(confluenceAdminUser)) {
						return confluenceAdminUser;
					}
					LOGGER.error("Confluence Admin User = " + confluenceAdminUser
							+ " is not a system admin or user is deactivated");
				}
			}
		}catch(Exception e){
			LOGGER.error("Exception occurred while getting Administrator user :", e);
		}
		return null;
	}
	public void reactivateUser(ConfluenceUser confluenceUser) throws Exception {
		try {
			Thread currentThread = Thread.currentThread();
			currentThread.setContextClassLoader(LdapHostnameVerificationSSLSocketFactory.class.getClassLoader());
			AuthenticatedUserThreadLocal.set(getAdministratorUser());
			userAccessor.reactivateUser(confluenceUser);
		} catch (Exception e) {
			throw new Exception("An error occurred while reactivating the user.",e);
		}
	}

	private String getRelayStateURL(HttpServletRequest request) {
		Cookie[] cookies = request.getCookies();
		if (cookies != null) {
			for (Cookie cookie : cookies) {
				if (cookie.getName().equals("mo.confluence-oauth.RELAY_STATE") && cookie.getValue() != null) {
					return cookie.getValue();
				}
			}
		}
		return StringUtils.EMPTY;
	}

	private void clearRelayStateURLCookie(HttpServletResponse response) {
		Cookie cookie = new Cookie("mo.confluence-oauth.RELAY_STATE", "");
		cookie.setMaxAge(0);
		cookie.setPath("/");
		response.addCookie(cookie);
	}

	private String getSessionAttribute(String attributeName, HttpServletRequest request) {
		HttpSession session = request.getSession();
		String token = (String) session.getAttribute(attributeName);
		return token;
	}

	private Boolean setRememberMeCookie(DefaultAuthenticator authenticator, ConfluenceUser userObject, HttpServletRequest request,
										HttpServletResponse response)
			throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
		Principal principal = userObject;
		Method authUserMethod = DefaultAuthenticator.class.getDeclaredMethod("authoriseUserAndEstablishSession",
				HttpServletRequest.class, HttpServletResponse.class, Principal.class);

		authUserMethod.setAccessible(true);
		Boolean result = (Boolean) authUserMethod.invoke(authenticator, new Object[]{request, response, principal});
		LOGGER.debug("Authentication Result: " + result + " Is cookie Enabled?: " + settings.getRememberMeCookieEnabled());
		if (result && settings.getRememberMeCookieEnabled()) {
			RememberMeService rememberMeService;
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

	private List<String> onTheFlyFilterIDPGroups(List<String> idpGroups){
		List<String> filteredGroups = new ArrayList<>();

		LOGGER.debug("IDP groups received :" + idpGroups.toString());
		LOGGER.debug("filtering IDP groups using filter type : " + settings.getOnTheFlyFilterIDPGroupsOption() + ", and filter key :" + settings.getOnTheFlyFilterIDPGroupsKey()) ;
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

	public UserManager getUserManager() {
		return userManager;
	}

	public void setUserManager(UserManager userManager) {
		this.userManager = userManager;
	}

	public UserAccessor getUserAccessor() {
		return userAccessor;
	}

	public void setUserAccessor(UserAccessor userAccessor) {
		this.userAccessor = userAccessor;
	}

	public MoOAuthUtils getOauthUtils() {
		return oauthUtils;
	}

	public void setOauthUtils(MoOAuthUtils oauthUtils) {
		this.oauthUtils = oauthUtils;
	}

	public MoOAuthPluginHandler getPluginHandler() {
		return pluginHandler;
	}

	public void setPluginHandler(MoOAuthPluginHandler pluginHandler) {
		this.pluginHandler = pluginHandler;
	}

	public MoOAuthManager getOauthManager() {
		return oauthManager;
	}

	public void setOauthManager(MoOAuthManager oauthManager) {
		this.oauthManager = oauthManager;
	}

	public MoOAuthAuthenticator getoAuthAuthenticator() {
		return oAuthAuthenticator;
	}

	public void setoAuthAuthenticator(MoOAuthAuthenticator oAuthAuthenticator) {
		this.oAuthAuthenticator = oAuthAuthenticator;
	}

	public DefaultWebSudoManager getDefaultWebSudoSessionManager() {
		return defaultWebSudoSessionManager;
	}

	public void setDefaultWebSudoSessionManager(DefaultWebSudoManager defaultWebSudoSessionManager) {
		this.defaultWebSudoSessionManager = defaultWebSudoSessionManager;
	}

	public MoOAuthSettings getSettings() {
		return settings;
	}

	public VelocityTemplateRenderer getRenderer() {
		return renderer;
	}

	public void setRenderer(VelocityTemplateRenderer renderer) {
		this.renderer = renderer;
	}
}
