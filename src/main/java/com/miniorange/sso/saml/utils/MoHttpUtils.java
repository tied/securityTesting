package com.miniorange.sso.saml.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.ProxySelector;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.net.ssl.SSLContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.BasicHttpClientConnectionManager;
import org.apache.http.impl.conn.SystemDefaultRoutePlanner;

import com.miniorange.sso.saml.MoPluginException;
import com.miniorange.sso.saml.MoPluginException.PluginErrorCode;

public class MoHttpUtils {

	private static Log LOGGER = LogFactory.getLog(MoHttpUtils.class);

	public static final String CONTENT_TYPE_JSON = "application/json";

	public static String sendPostRequest(String url, String data, String contentType, HashMap headers) {
		try {
			LOGGER.debug("HttpUtils sendPostRequest Sending POST request to " + url + " with payload " + data);
			CloseableHttpClient httpClient = getHttpClient();
			
			HttpPost postRequest = new HttpPost(url);

			if (headers != null) {
				Iterator iterator = headers.entrySet().iterator();
				if (!headers.isEmpty()) {
					while (iterator.hasNext()) {
						Map.Entry pairs = (Map.Entry) iterator.next();
						postRequest.setHeader(pairs.getKey().toString(), pairs.getValue().toString());
					}
				}
			}
			StringEntity input = new StringEntity(data);

			input.setContentType(contentType);
			postRequest.setEntity(input);

			
			HttpResponse response = httpClient.execute(postRequest);
			LOGGER.debug("Response for HTTP Request: " + response.toString() + " and Status Code: " + response
					.getStatusLine().getStatusCode());

			if (response.getEntity() != null) {
				LOGGER.debug("Response Entity found. Reading Response payload.");
				String status = IOUtils.toString(new InputStreamReader((response.getEntity().getContent())));
				
				httpClient.close();
				return status;
			} else {
				LOGGER.debug("Response Entity NOT found. Returning EMPTY string.");
				httpClient.close();
				return StringUtils.EMPTY;
			}


		} catch (Exception e) {
			throw new MoPluginException(PluginErrorCode.UNKNOWN, e.getMessage(), e);
		}
	}
	public static Cookie getCookie(HttpServletRequest request, String cookieName) {
		Cookie[] cookies = request.getCookies();
		if (cookies != null) {
			for (Cookie cookie : cookies) {
				if (cookie.getName().equals(cookieName)) {
					return cookie;
				}
			}
		}
		return null;
	}

	public static String sendPostRequest(String url, List<NameValuePair> data, String contentType, HashMap headers) {
		try {
			LOGGER.debug("HttpUtils sendPostRequest Sending POST request to " + url + " with payload " + data);
			CloseableHttpClient httpClient = getHttpClient();
			LOGGER.debug("httpClient : "+ httpClient);
			LOGGER.debug("Hitting URL");
			HttpPost postRequest = new HttpPost(url);

			if (headers != null) {
				Iterator iterator = headers.entrySet().iterator();
				if (!headers.isEmpty()) {
					while (iterator.hasNext()) {
						Map.Entry pairs = (Map.Entry) iterator.next();
						postRequest.setHeader(pairs.getKey().toString(), pairs.getValue().toString());
					}
				}
			}

			UrlEncodedFormEntity entity = new UrlEncodedFormEntity(data);
			entity.setContentType(contentType);
			postRequest.setEntity(entity);
			return executePostRequest(httpClient, postRequest);

		} catch (Exception e) {
			LOGGER.debug("Error in sending post request, "+e.toString());
			e.printStackTrace();
			return "Failed";
		}
	}

	public static String executePostRequest(CloseableHttpClient httpClient, HttpPost postRequest){
		try{

			HttpResponse response = httpClient.execute(postRequest);
			LOGGER.debug("Response for HTTP Request: " + response.toString() + " and Status Code: " + response
					.getStatusLine().getStatusCode());

			if (response.getEntity() != null) {
				LOGGER.debug("Response Entity found. Reading Response payload.");
				BufferedReader br = new BufferedReader(new InputStreamReader((response.getEntity().getContent())));

				String output, status = "";
				while ((output = br.readLine()) != null) {
					status += output;
				}
				LOGGER.debug("Response payload: " + status);
				httpClient.close();
				LOGGER.debug("Status" + status);
				return status;
			} else {
				LOGGER.debug("Response Entity NOT found. Returning EMPTY string.");
				httpClient.close();
				return StringUtils.EMPTY;
			}
		}catch (Exception e) {
			LOGGER.debug("Error in executing post request, "+e.toString());
			e.printStackTrace();
			return "Failed";
		}
	}

	public static String sendGetRequest(String url) {
		try {
			LOGGER.debug("MoHttpUtils sendGetRequest Sending GET request to " + url);
			CloseableHttpClient httpClient = getHttpClient();
			HttpGet getRequest = new HttpGet(url);
			HttpResponse response = httpClient.execute(getRequest);
			LOGGER.debug("Response for HTTP Request: " + response.toString() + " and Status Code: " + response
					.getStatusLine().getStatusCode());

			if (response.getStatusLine().getStatusCode() == 200 && response.getEntity() != null) {
				LOGGER.debug("Response Entity found. Reading Response payload.");
				String data = IOUtils.toString(new InputStreamReader((response.getEntity().getContent())));
				LOGGER.debug("Response payload: " + data);
				httpClient.close();
				return data;
			} else {
				LOGGER.debug("Response Entity NOT found. Returning EMPTY string.");
				httpClient.close();
				return StringUtils.EMPTY;
			}
		} catch (Exception e) {
			LOGGER.error(e);
			throw new MoPluginException(PluginErrorCode.UNKNOWN, e.getMessage(), e);
		}
	}
	private static CloseableHttpClient getHttpClient() throws KeyStoreException, NoSuchAlgorithmException,
	KeyManagementException {
		HttpClientBuilder builder = HttpClientBuilder.create();
		SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(null, (arg0, arg1) -> true).build();
		SSLConnectionSocketFactory sslConnectionFactory = new SSLConnectionSocketFactory(sslContext,
				SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
		builder.setSSLSocketFactory(sslConnectionFactory);
		
		Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory>create()
				.register("https", sslConnectionFactory)
				.register("http", PlainConnectionSocketFactory.INSTANCE)
				.build();
		
		HttpClientConnectionManager ccm = new BasicHttpClientConnectionManager(registry);
		
		builder.setConnectionManager(ccm);
		
		//return builder.build();
		SystemDefaultRoutePlanner routePlanner = new SystemDefaultRoutePlanner(ProxySelector.getDefault());
		CloseableHttpClient httpclient = HttpClients.custom().setRoutePlanner(routePlanner).setConnectionManager(ccm)
				.build();
		return httpclient;
	}

	public static Cookie createCookie(String url,String cookieName, String cookieValue, Boolean isHttpOnly){
		Cookie cookie = new Cookie(cookieName, cookieValue);
		cookie.setPath("/");
		cookie.setSecure(StringUtils.isNotBlank(url) && url.length()>7 && url.substring(0,8).equalsIgnoreCase("https://"));
		cookie.setHttpOnly(isHttpOnly);
		return cookie;
	}

	public static void addCookie(HttpServletResponse response, Cookie idpIdCookie) {
		String cookieHeader = createCookieHeader(idpIdCookie);
		LOGGER.debug("Cookie Header "+cookieHeader);
		response.setHeader("Set-Cookie",cookieHeader);
	}

	private static String createCookieHeader(Cookie cookie) {
		// create the special cookie header instead of creating a Java cookie
		// Set-Cookie:<name>=<value>[; <name>=<value>][; expires=<date>][;
		// domain=<domain_name>][; path=<some_path>][; secure][;HttpOnly
		String header = cookie.getName() + "=" + cookie.getValue();

		if (cookie.getDomain() != null) {
			header += "; Domain=" + cookie.getDomain();
		}
		if (cookie.getPath() != null) {
			header += "; Path=" + cookie.getPath();
		}
		if(cookie.getSecure()) {
			header += "; Secure";
			header += "; SameSite=None";
		}

		if ( cookie.isHttpOnly() ) {
			header += "; HttpOnly";
		}
		return header;
	}

	public static void clearCookie(HttpServletRequest request, HttpServletResponse response, String cookieName) {
		Cookie cookie = getCookie(request, cookieName);
		if (cookie != null) {
			cookie.setPath("/");
			cookie.setMaxAge(0);
			response.addCookie(cookie);
		}
		return;
	}

}
