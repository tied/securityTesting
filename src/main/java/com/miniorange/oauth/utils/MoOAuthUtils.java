package com.miniorange.oauth.utils;

import java.io.ByteArrayInputStream;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.DatatypeConverter;

import com.miniorange.oauth.confluence.MoOAuthPluginConstants;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.atlassian.json.jsonorg.JSONArray;
import com.atlassian.json.jsonorg.JSONObject;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.google.gson.*;

public class MoOAuthUtils {

	private static Log LOGGER = LogFactory.getLog(MoOAuthUtils.class);

	public static final Pattern VALID_EMAIL_ADDRESS_REGEX = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$",
			Pattern.CASE_INSENSITIVE);
	private ArrayList<String> listOfOpenIdApps;

	public Map<String, JsonElement> jsonToMap(JsonObject getGroupUserInfoResponse) throws JsonParseException {
		Set<Map.Entry<String, JsonElement>> keys = getGroupUserInfoResponse.entrySet();
		Map<String, JsonElement> map = new HashMap<>();
		for (Map.Entry<String, JsonElement> entry : keys) {
			String key = entry.getKey();
			if (getGroupUserInfoResponse.get(key) instanceof JsonObject) {
				jsonToMap((JsonObject) getGroupUserInfoResponse.get(key));
			} else {
				map.put(key, getGroupUserInfoResponse.get(key));
			}
		}
		return map;
	}

	public HashMap<String, Object> toMap(JsonObject object, HashMap<String, Object> map) {
		Set<Map.Entry<String, JsonElement>> keysItr = object.entrySet();
		for (Map.Entry<String, JsonElement> entry : keysItr) {
			String key = entry.getKey();
			JsonElement value = object.get(key);
			Object valueToPutInMap;
			if (value.isJsonArray()) {
				valueToPutInMap = toList(value.getAsJsonArray(), map);
			} else if (value.isJsonObject()) {
				valueToPutInMap = toMap(value.getAsJsonObject(), map);
			} else {
				if (!value.isJsonNull()) {
					valueToPutInMap = value.getAsString();
					map.put(key, valueToPutInMap);
				}
			}
		}
		return map;
	}

	public Map<String, Object> toMap(JsonObject object, Map<String, Object> map) throws JsonParseException {
		Set<Map.Entry<String, JsonElement>> keysItr = object.entrySet();
		for (Map.Entry<String, JsonElement> entry : keysItr) {
			String key = entry.getKey();
			JsonElement value = object.get(key);
			Object valueToPutInMap;
			if (value.isJsonArray()) {
				valueToPutInMap = toList(value.getAsJsonArray(), map);
			} else if (value.isJsonArray()) {
				valueToPutInMap = toMap(value.getAsJsonObject(), map);
			} else {
				if (!value.isJsonNull()) {
					valueToPutInMap = value.getAsString();
					map.put(key, value.getAsString());
				}
			}
		}
		return map;
	}

	public List<Object> toList(JsonArray array, HashMap<String, Object> map) throws JsonParseException {
		List<Object> list = new ArrayList<Object>();
		for (int i = 0; i < array.size(); i++) {
			JsonElement value = array.get(i);
			Object valueToPutInMap = value;
			if (value.isJsonArray()) {
				valueToPutInMap = toList(value.getAsJsonArray(), map);
			} else if (value.isJsonArray()) {
				valueToPutInMap = toMap(value.getAsJsonObject(), map);
			}
			list.add(valueToPutInMap);
		}

		return list;
	}

	public List<Object> toList(JsonArray array, Map<String, Object> map) {
		List<Object> list = new ArrayList<Object>();
		for (int i = 0; i < array.size(); i++) {
			JsonElement value = array.get(i);
			Object valueToPutInMap = value;
			if (value.isJsonArray()) {
				valueToPutInMap = toList(value.getAsJsonArray(), map);
			} else if (value.isJsonArray()) {
				valueToPutInMap = toMap(value.getAsJsonObject(), map);
			}
			list.add(valueToPutInMap);
		}
		return list;
	}

	public HashMap<String, String> copyToStringValueMap(HashMap<String, Object> input) {
		HashMap<String, String> ret = new HashMap<>();
		String currValue="";
		for (Map.Entry<String, Object> entry : input.entrySet()) {
			if(entry.getValue()!=null) {
				currValue = ((String) entry.getValue());
				ret.put(entry.getKey(), currValue);
			}
		}
		return ret;
	}

	public Map<String, String> copyToStringValueMap(
			Map<String, Object> input) {
		HashMap<String, String> ret = new HashMap<>();
		for (Map.Entry<String, Object> entry : input.entrySet()) {
			ret.put(entry.getKey(), (String) entry.getValue());
		}
		return ret;
	}

	public PublicKey getPublicKeyObjectFromConfiguredKey(String configuredKey) {

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

	public PublicKey getPublicKeyFromJWKSEndpoint(DecodedJWT JWTToken, String JWKSEndpoint) {

		LOGGER.debug("Getting public key from JWKS Endpoint");

		PublicKey publicKey = null;

		try {
			String JWKSResponse = MoOAuthHttpUtils.sendGetRequest(JWKSEndpoint);

			JSONObject JSONWebKeySet = new JSONObject(JWKSResponse);

			LOGGER.debug("JWKS JSON Response " + JSONWebKeySet.toString());

			JSONArray keys = JSONWebKeySet.optJSONArray("keys");

			HashMap<String, ArrayList<String>> keyTable = new HashMap<>();

			Base64 base64Url = new Base64(true);

			String header = new String(base64Url.decode(JWTToken.getHeader()));

			LOGGER.debug("Header from JWT Token" + header);

			JSONObject jsonHeader = new JSONObject(header);

			String kidFromHeader = jsonHeader.optString("kid");

			LOGGER.debug("kidFromHeader" + kidFromHeader);
			String publicCertificate = StringUtils.EMPTY;

			if (keys == null) {
				LOGGER.error("Keys Are Empty");
				return null;
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
					LOGGER.debug("Getting public key through modulus and exponent");
					byte[] n_obtained_byte = Base64.decodeBase64(n_obtained);
					byte[] e_obtained_byte = Base64.decodeBase64(e_obtained);
					BigInteger modulus = new BigInteger(1, n_obtained_byte);
					BigInteger exponent = new BigInteger(1, e_obtained_byte);
					RSAPublicKeySpec spec = new RSAPublicKeySpec(modulus, exponent);
					KeyFactory factory = KeyFactory.getInstance("RSA");
					publicKey = factory.generatePublic(spec);
					LOGGER.debug("Public Key : " + publicKey);
				} else {
					LOGGER.debug("Getting public key using x.509 chain.");
					publicCertificate = x5c_obtained;
					CertificateFactory factory = CertificateFactory.getInstance("X.509");
					X509Certificate cert = (X509Certificate) factory.generateCertificate(
							new ByteArrayInputStream(DatatypeConverter.parseBase64Binary(publicCertificate)));
					publicKey = (RSAPublicKey) cert.getPublicKey();
					LOGGER.debug("Public Key : " + publicKey);
				}
			}
		} catch (Exception e) {
			LOGGER.error("An error occured while fetching the public key from JWKS endpoint ", e);
		}

		return publicKey;
	}

	public String findKey(JsonObject getUserInfoData, String key, String appName) {

		LOGGER.debug("JsonObject to find String: " + getUserInfoData.toString());
		try {

			Set<Map.Entry<String, JsonElement>> x = getUserInfoData.entrySet();
			String multipleValues = StringUtils.EMPTY;
			LOGGER.debug("Fetching Value for Attribute: " + key);

			for (Map.Entry<String, JsonElement> keySet : x) {
				String currentKey = keySet.getKey();
				if ((getUserInfoData.get(currentKey).isJsonObject()) && (getUserInfoData != null)
						&& !(StringUtils.equals(key, "id"))) {
					// LOGGER.debug("JsonObject: " + getUserInfoData.getAsJsonObject(currentKey));
					String value = findKey(getUserInfoData.getAsJsonObject(currentKey), key, appName);
					if (value != null)
						return value;
				} else if ((getUserInfoData.get(currentKey).isJsonArray()) && (getUserInfoData != null)
						&& !(StringUtils.equals(key, "id"))) {
					// LOGGER.debug("JsonArray found.. " +
					// getUserInfoData.getAsJsonArray(currentKey));
					JsonArray array = getUserInfoData.getAsJsonArray(currentKey);
					// LOGGER.debug("Size of this array: "+array.size());
					if (StringUtils.equals(appName, MoOAuthPluginConstants.MEETUP))
						continue;
					for (int index = 0; index < array.size(); index++) {
						JsonObject object = (getUserInfoData.getAsJsonArray(currentKey)).get(index).getAsJsonObject();
						// LOGGER.debug("JsonObject at index "+index+" : "+object);
						if (index > 0) {
							multipleValues = multipleValues.concat(";");
						}

						if (object.has(key)) {
							// LOGGER.debug("Found the key: "+key);
							// LOGGER.debug("value: "+object.get(key).getAsString());

							multipleValues = multipleValues.concat(object.get(key).getAsString());
						}
					}
					// LOGGER.debug("Multiple Values: " + multipleValues);
					return multipleValues;

				} else if (getUserInfoData.get(currentKey).isJsonPrimitive()) {
					// LOGGER.debug("Inside the elseif block");
					String value = getUserInfoData.get(currentKey).getAsString();
					// LOGGER.debug("Value getUserInfoData.get().getAsString" + value);
					if (StringUtils.equals(currentKey, key)) {
						// LOGGER.debug("Key: " + key + " Value: " + value);
						return value;
					} else {
						continue;
					}

				}
			}
		} catch (JsonParseException e) {
			LOGGER.error(e.getMessage());
		}
		return StringUtils.EMPTY;
	}

	/**
	 * This function is used to find all validate emails from json data and return a
	 * set of email id. This function takes input has a hash map object of
	 * type<String, String>
	 */
	public Set<String> findEmails(HashMap<String, String> userInfoMap) {

		Set<String> emailSet = new HashSet<String>();
		HashMap<String, String> map = new HashMap<String, String>(userInfoMap);
		Iterator iterator = map.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry pair = (Map.Entry) iterator.next();
			String value = (String) pair.getValue();

			if (value != null && isEmailId(value)) {
				emailSet.add(value);
			}
			iterator.remove();
		}
		return emailSet;
	}

	public boolean isEmailId(String emailStr) {
		Matcher matcher = VALID_EMAIL_ADDRESS_REGEX.matcher(emailStr);
		return matcher.find();
	}

	/*
	 * Change made to original function : Extra parameters passed - set of OpenID
	 * apps and current appname. Now the OpenId flow is initiated if the scope
	 * contains string "openid" and also if the current appname is present in the
	 * list of apps supporting OIDC
	 */

	public static boolean isOpenIdProtocol(String scope, String appName, ArrayList<String> listOfOpenIdApps) {
		return listOfOpenIdApps.contains(appName);
	}

	/**
	 * This function is used to separate different json object and turn their keys
	 * in to a map of <String, String> which is stored with the key in final hash
	 * map Ex:-- {a: {b:1, c:2}} with be turned into hashmap a ={b=1, c=2}
	 */

	public HashMap<String, Object> toMapObjects(JsonObject object, HashMap<String, Object> map)
			throws JsonParseException {
		Set<Map.Entry<String, JsonElement>> keysItr = object.entrySet();
		for(Map.Entry<String, JsonElement> entry : keysItr) {
			String key = entry.getKey();
			JsonElement value = object.get(key);
			Object valueToPutInMap;
			if(!value.isJsonNull()) {
				if (value.isJsonArray()) {
					valueToPutInMap = toMapList(value.getAsJsonArray(), key);
					map.putAll((HashMap) valueToPutInMap);
				} else if (value.isJsonObject()) {
					JsonObject obj = value.getAsJsonObject();
					if (obj.isJsonNull()) {
						map.put(key, null);
					} else {
						HashMap<String, Object> newMap = new HashMap<String, Object>();
						valueToPutInMap = mapObjectsWithUpdatedKey(obj, newMap, key);
						map.putAll((HashMap) valueToPutInMap);
					}
				} else {
					try {
						map.put(key, value.getAsString());
					} catch (UnsupportedOperationException e) {
						map.put(key, value.toString());
					}
				}
			}else{
				map.put(key, StringUtils.EMPTY);
			}
		}
		return map;
	}

	public HashMap<String, Object> mapObjectsWithUpdatedKey(JsonObject object, HashMap<String, Object> map,
			String keyName) throws JsonParseException {
		Set<Map.Entry<String, JsonElement>> keysItr = object.entrySet();
		for(Map.Entry<String, JsonElement> entry : keysItr) {
			String key = entry.getKey();
			JsonElement value = object.get(key);
			key = keyName + '.' + key;
			Object valueToPutInMap;
			if(value.isJsonArray()) {
				valueToPutInMap = toMapList(value.getAsJsonArray(), key);
				map.putAll((HashMap) valueToPutInMap);
			}
			else if(value.isJsonObject()) {
				JsonObject obj = value.getAsJsonObject();
				if (obj.isJsonNull()) {
					map.put(key, null);
				} else {
					HashMap<String, Object> newMap = new HashMap<String, Object>();
					valueToPutInMap = mapObjectsWithUpdatedKey(obj, newMap, key);
					map.putAll((HashMap) valueToPutInMap);
				}
			} else{
				map.put(key, value.toString());
			}
		}
		return map;
	}

	public HashMap<String, Object> toMapList(JsonArray array, String key) throws JsonParseException {
		HashMap<String, Object> completeMap = new HashMap<String, Object>();
		List<String> list = new ArrayList<String>();
		if (array.size() == 0) {
			completeMap.put(key, null);
		} else {
			for (int i = 0; i < array.size(); i++) {
				JsonElement value = array.get(i);
				Object valueToPutInMap;
				if (value.isJsonArray()) {
					valueToPutInMap = toMapList(value.getAsJsonArray(), key + '[' + i + ']');
					completeMap.putAll((HashMap) valueToPutInMap);
				} else if (value.isJsonObject()) {
					JsonObject obj = value.getAsJsonObject();
					if (obj.isJsonNull()) {
						completeMap.put(key + '[' + i + ']', null);
					} else {
						HashMap<String, Object> newMap = new HashMap<String, Object>();
						valueToPutInMap = mapObjectsWithUpdatedKey(obj, newMap, key + '[' + i + ']');
						completeMap.putAll((HashMap) valueToPutInMap);
					}
				}  else {
					list.add(value.toString());
				}
			}
			if (list.size() > 0) {
				String temp = list.get(0);
				for (int i = 1; i < list.size(); i++)
					temp = temp + ";" + list.get(i);

				completeMap.put(key, temp);
			}
		}
		return completeMap;
	}

	public static String deserializePublicKey(String publicKey) {
		LOGGER.debug("Deserializing Public Key");
		String BEGIN_PUBLIC_KEY = "BEGIN PUBLIC KEY";
		String END_PUBLIC_KEY = "END PUBLIC KEY";
		if (StringUtils.isNotBlank(publicKey)) {
			publicKey = StringUtils.remove(publicKey, "\r");
			publicKey = StringUtils.remove(publicKey, "\n");
			publicKey = StringUtils.remove(publicKey, "-");
			publicKey = StringUtils.remove(publicKey, BEGIN_PUBLIC_KEY);
			publicKey = StringUtils.remove(publicKey, END_PUBLIC_KEY);
			publicKey = StringUtils.remove(publicKey, " ");
		}
		return publicKey;
	}

	public static String serializePublicCertificate(String publicKey) {
		LOGGER.debug("Serializing Public Certificate");
		String BEGIN_PUBLIC_KEY = "BEGIN PUBLIC KEY";
		String END_PUBLIC_KEY = "END PUBLIC KEY";
		if (StringUtils.isNotBlank(publicKey)) {
			publicKey = deserializePublicKey(publicKey);
			org.apache.commons.codec.binary.Base64 encoder = new org.apache.commons.codec.binary.Base64(64);
			publicKey = encoder.encodeToString(org.apache.commons.codec.binary.Base64.decodeBase64(publicKey));
			StringBuffer cert = new StringBuffer("-----" + BEGIN_PUBLIC_KEY + "-----\r\n");
			cert.append(publicKey);
			cert.append("-----" + END_PUBLIC_KEY + "-----");
			return cert.toString();
		}
		return publicKey;
	}

	public boolean checkIfKeyExist(HashMap<String, String> userInfoMap, String keyName) {
		LOGGER.debug("keyName : "+ keyName);
		LOGGER.debug("userInfoMap : "+ userInfoMap);
		String value = userInfoMap.get(keyName);
		if (value != null)
			return true;
		else
			return false;
	}

	public String getValue(HashMap<String, String> userInfoMap, String keyName) {
		LOGGER.debug("Taking Value from userInfoMap");
		return userInfoMap.get(keyName);
	}

	public String checkGroupPath(HashMap<String, String> userInfoMap) {
		HashMap<String, String> map = new HashMap<String, String>(userInfoMap);
		Iterator iterator = map.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry pair = (Map.Entry) iterator.next();
			String value = (String) pair.getValue();
			if (value != null && value.length() > 0 && value.charAt(0) == '/') {
				return (String) pair.getKey();
			}
			iterator.remove();
		}
		return null;
	}

}
