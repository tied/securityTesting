package com.miniorange.oauth.bamboo;

import com.atlassian.bamboo.user.BambooUser;
import com.atlassian.bamboo.user.BambooUserManager;
import com.atlassian.json.jsonorg.JSONObject;
import com.atlassian.user.User;
import com.atlassian.user.impl.DefaultUser;
import com.miniorange.oauth.bamboo.action.MoOAuthAction;
import com.miniorange.oauth.bamboo.dto.TokenResponse;
import com.miniorange.oauth.bamboo.factory.IProtocolAction;
import com.miniorange.oauth.utils.MoOAuthHttpUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import java.security.interfaces.RSAPublicKey;

import com.miniorange.oauth.utils.MoOAuthUtils;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import com.google.gson.*;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.List;

public class MoOAuthUserManager {

	private static BambooUserManager bambooUserManager;
	private static Log LOGGER = LogFactory.getLog(MoOAuthUserManager.class);
	private static MoOAuthSettings settings;
	private static JsonParser parser = new JsonParser();
	private static Gson gson = new Gson();

	public MoOAuthUserManager(BambooUserManager bambooUserManager, MoOAuthSettings settings) {
		this.bambooUserManager = bambooUserManager;
		this.settings = settings;
	}

	public static BambooUser createAPICall(String actionType, String userIdentity, String email, String fullName,
			List<String> groupsToAssign) {

		BambooUser createdBambooUser = null;
		ArrayList<NameValuePair> postParameters = new ArrayList<NameValuePair>();

		postParameters.add(new BasicNameValuePair("action", actionType));
		postParameters.add(new BasicNameValuePair("username", userIdentity));
		postParameters.add(new BasicNameValuePair("email", email));
		postParameters.add(new BasicNameValuePair("fullname", fullName));
		postParameters.add(new BasicNameValuePair("groups", gson.toJson(groupsToAssign)));

		String username = MoOAuthHttpUtils.sendPostRequest(settings.getCreateUserUrl(), postParameters,
				"application/x-www-form-urlencoded", null);
		LOGGER.debug("user created " + username);

		if (StringUtils.isNotBlank(username)) {
			createdBambooUser = bambooUserManager.getBambooUser(username);
		}

		return createdBambooUser;
	}

	public static BambooUser saveAPICall(String actionType, String userIdentity, String email, String fullName) {

		BambooUser savedUser = null;

		User user = bambooUserManager.getUser(userIdentity);
		DefaultUser defaultUser = new DefaultUser(user);
		defaultUser.setFullName(fullName);
		defaultUser.setEmail(email);

		ArrayList<NameValuePair> postParameters = new ArrayList<NameValuePair>();
		postParameters.add(new BasicNameValuePair("action", actionType));
		postParameters.add(new BasicNameValuePair("username", userIdentity));
		postParameters.add(new BasicNameValuePair("email", email));
		postParameters.add(new BasicNameValuePair("fullname", fullName));

		String username = MoOAuthHttpUtils.sendPostRequest(settings.getCreateUserUrl(), postParameters,
				"application/x-www-form-urlencoded", null);
		LOGGER.debug("user saved " + username);

		if (StringUtils.isNotBlank(username)) {
			savedUser = bambooUserManager.getBambooUser(username);
		}

		return savedUser;
	}

	public static void groupUpdateAPICall(String actionType, String userIdentity, List<String> groupsToAssign) {
		LOGGER.debug("groupUpdateAPICall: Preparing post parameter for REST Call");

		ArrayList<NameValuePair> postParameters = new ArrayList<NameValuePair>();

		postParameters.add(new BasicNameValuePair("action", actionType));
		postParameters.add(new BasicNameValuePair("username", userIdentity));
		postParameters.add(new BasicNameValuePair("groups", gson.toJson(groupsToAssign)));

		LOGGER.debug("Post parameters : " + postParameters.toString());

		String username = MoOAuthHttpUtils.sendPostRequest(settings.getCreateUserUrl(), postParameters,
				"application/x-www-form-urlencoded", null);
		LOGGER.debug("user updated " + username);
	}

	public static void groupCreateAPICall(String actionType, String userIdentity,  String groupsToCreate) {
		LOGGER.debug("groupCreateAPICall");

		ArrayList<NameValuePair> postParameters = new ArrayList<NameValuePair>();

		postParameters.add(new BasicNameValuePair("action", actionType));
		postParameters.add(new BasicNameValuePair("username", userIdentity));
		postParameters.add(new BasicNameValuePair("group_to_create", groupsToCreate));

		String username = MoOAuthHttpUtils.sendPostRequest(settings.getCreateUserUrl(), postParameters,
				"application/x-www-form-urlencoded", null);

	}
	public static String fetchUserInfo(HttpServletRequest request, IProtocolAction protocolAction) {

		String accessTokenResponse = getAndSetAccessAndRefreshToken(request, protocolAction);
		if(StringUtils.equals(accessTokenResponse, "invalid_token")){
			return "invalid_token";
		}
		TokenResponse response = protocolAction.sendUserInfoRequest(accessTokenResponse, settings);
		return response.getResponse();
	}

	public static String fetchGroupInfo(JsonObject userInfodata, HttpServletRequest request, String email, String id,
			MoOAuthSettings settings, IProtocolAction protocolAction, MoOAuthUtils oauthUtils)
			throws JsonParseException {

		LOGGER.debug("Fetch Group Info");
		String refreshToken = getSessionAttribute("refresh_token", request);
		String accessToken = getSessionAttribute("access_token", request);

		LOGGER.debug("refresh token : " + refreshToken + "\n" + "access token : " + accessToken);

		if (protocolAction instanceof MoOAuthAction) {
			((MoOAuthAction) protocolAction).setRefreshToken(refreshToken);
			((MoOAuthAction) protocolAction).setAccessToken(accessToken);
		}

		TokenResponse tokenResponse = protocolAction.sendGroupInfoRequest(userInfodata, request, email, id, settings,
				oauthUtils);
		return tokenResponse.getResponse();

	}

	public static String getAndSetAccessAndRefreshToken(HttpServletRequest request, IProtocolAction protocolAction) {

		TokenResponse tokenResponse = protocolAction.sendTokenRequest(settings, request.getParameter("code"));
		LOGGER.debug("Access Token Response : ");
		String accessTokenResponse = tokenResponse.getResponse();
		JsonObject checkErrorObj = parser.parse(tokenResponse.getResponse()).getAsJsonObject();

		if (checkErrorObj.has("error"))
			return "error";

		String accessToken = "", refreshToken = "";

		if (StringUtils.isNotBlank(accessTokenResponse)) {
			if (checkErrorObj.get("access_token") != null) {
				accessToken = checkErrorObj.get("access_token").getAsString();
			}
			if (checkErrorObj.get("refresh_token") != null) {
				refreshToken = checkErrorObj.get("refresh_token").getAsString();
			}
			LOGGER.debug("Access Token : " + accessToken);
			LOGGER.debug("Refress Token : " + refreshToken);
		} else {
			if (StringUtils.isEmpty(accessTokenResponse) || StringUtils.isBlank(accessTokenResponse)
					|| accessTokenResponse.equals("Failed")) {
				LOGGER.error("An error occured while fetching the access token. Please check configured the access token enpoint");
				return "error";
			}
		}

		if (MoOAuthUtils.isOpenIdProtocol(settings.getAppName())) {
			if (StringUtils.isNoneEmpty(settings.getPublicKey())) {
				String id_token = (new JSONObject(tokenResponse.getResponse())).optString("id_token");
				if (id_token != null) {
					LOGGER.debug("ID Token = " + id_token);
					String[] id_token_parts = id_token.split("\\.");
					DecodedJWT jwt = JWT.decode(id_token);
					String algorithm = jwt.getAlgorithm();
					PublicKey publicKey = getPublicKeyObjectFromConfiguredKey(settings.getPublicKey());
					Boolean isValid = verifyTokenSignature(jwt, publicKey, algorithm);
					if (!BooleanUtils.toBoolean(isValid)) {
						LOGGER.error("Signature Validation Failed!");
						return "invalid_token";
					}
					LOGGER.debug("Signature Validation Successfully");
				}
			}
		}

		HttpSession session_refresh_token = request.getSession();
		session_refresh_token.setAttribute("refresh_token", refreshToken);
		LOGGER.debug("refresh token : " + getSessionAttribute("refresh_token", request));

		HttpSession session_access_token = request.getSession();
		session_access_token.setAttribute("access_token", accessToken);
		LOGGER.debug("access token : " + getSessionAttribute("access_token", request));

		return accessTokenResponse;
	}

	public static PublicKey getPublicKeyObjectFromConfiguredKey(String configuredKey) {

		LOGGER.debug("Getting public key object from configured key");
		PublicKey publicKey = null;

		try {
			configuredKey = MoOAuthUtils.deserializePublicKey(configuredKey);
			LOGGER.debug("Deserialize public Key : " + configuredKey);

			byte[] publicBytes = Base64.decodeBase64(configuredKey);

			X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicBytes);
			KeyFactory keyFactory = KeyFactory.getInstance("RSA");
			publicKey = keyFactory.generatePublic(keySpec);

			LOGGER.debug("Public Key : "+publicKey.toString());
		} catch (Exception e) {
			LOGGER.debug("An error occured while generating public key object from configured key ", e);
		}

		return publicKey;
	}

	public static Boolean verifyTokenSignature(DecodedJWT JWTToken, PublicKey publicKey, String signatureAlgorithm) {
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

	/*public static Boolean verifyToken(String[] id_token_parts) {
		byte[] ID_TOKEN_SIGNATURE = base64UrlDecodeToBytes(id_token_parts[2]);
		byte[] data = (id_token_parts[0] + "." + id_token_parts[1]).getBytes(StandardCharsets.UTF_8);
		String publicCertificate = settings.getPublicKey();
		publicCertificate = MoOAuthUtils.deserializePublicKey(publicCertificate);
		byte[] publicBytes = Base64.decodeBase64(publicCertificate);
		try {
			X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicBytes);
			KeyFactory keyFactory = KeyFactory.getInstance("RSA");
			PublicKey publicKey = keyFactory.generatePublic(keySpec);

			boolean isSignatureValid = verifyUsingPublicKey(data, ID_TOKEN_SIGNATURE, publicKey);
			return isSignatureValid;
		} catch (GeneralSecurityException e) {
			e.printStackTrace();
			return false;
		}
	}*/

	public static byte[] base64UrlDecodeToBytes(String input) {
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

	private static String getSessionAttribute(String attributeName, HttpServletRequest request) {
		HttpSession session = request.getSession();
		String token = (String) session.getAttribute(attributeName);
		return token;
	}

}
