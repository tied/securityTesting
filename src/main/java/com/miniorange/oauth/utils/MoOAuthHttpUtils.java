package com.miniorange.oauth.utils;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.BasicHttpClientConnectionManager;
import org.apache.http.impl.conn.SystemDefaultRoutePlanner;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.TrustStrategy;

import javax.net.ssl.SSLContext;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.ProxySelector;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class MoOAuthHttpUtils {

	private static Log LOGGER = LogFactory.getLog(MoOAuthHttpUtils.class);

	public static final String CONTENT_TYPE_JSON = "application/json";

	public static String sendPostRequest(String url, List<NameValuePair> data, String contentType, HashMap headers) {
		try {
			LOGGER.debug("HttpUtils sendPostRequest Sending POST request to " + url + " with payload " + data);
			CloseableHttpClient httpClient = getHttpClient();
			LOGGER.debug("httpClient : "+ httpClient);
			LOGGER.debug("Hitting URL : "+url);
			
			HttpPost postRequest = new HttpPost(url);
			
			LOGGER.debug("Post Request : "+postRequest.toString());

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
			
			LOGGER.debug("Executing post request : "+postRequest.toString());
			
            return executePostRequest(httpClient, postRequest);
          
		} catch (Exception e) {
			LOGGER.debug("Error in sending post request, "+e.toString());
			e.printStackTrace();
			return "Failed";
		}
	}
	
	public static String sendPostRequest(String url, String data, String contentType, HashMap headers) {
		try {
			LOGGER.debug("MoHttpUtils sendPostRequest Sending POST request to " + url + " with payload " + data);
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
	
	private static CloseableHttpClient getHttpClient() throws KeyStoreException, NoSuchAlgorithmException,
			KeyManagementException {
		//LOGGER.debug("Inside getHttpClient");
		HttpClientBuilder builder = HttpClientBuilder.create();
		SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(null, new TrustStrategy() {
			@Override
			public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {
				LOGGER.debug("sending SSL context as true");
				return true;
			}
		}).build();
		
		SSLConnectionSocketFactory sslConnectionFactory = new SSLConnectionSocketFactory(sslContext, NoopHostnameVerifier
				.INSTANCE);
		builder.setSSLSocketFactory(sslConnectionFactory);

		Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory>create()
				.register("https", sslConnectionFactory)
				.register("http", PlainConnectionSocketFactory.INSTANCE)
				.build();

		HttpClientConnectionManager ccm = new BasicHttpClientConnectionManager(registry);

		builder.setConnectionManager(ccm);

		SystemDefaultRoutePlanner routePlanner = new SystemDefaultRoutePlanner(ProxySelector.getDefault());
		CloseableHttpClient httpclient = HttpClients.custom().setRoutePlanner(routePlanner).setConnectionManager(ccm)
				.build();
		LOGGER.debug("httpclient : "+ httpclient);
		return httpclient;
	}
	
	public static String sendGetRequestWithHeaders(String url, HashMap headers) {

		try {
			HttpClient httpClient = getHttpClient();
			HttpGet getRequest = new HttpGet(url);
			LOGGER.debug("Sending HTTP Request to URL " + url);
			if (headers != null) {
				Iterator iterator = headers.entrySet().iterator();
				if (!headers.isEmpty()) {
					while (iterator.hasNext()) {
						Map.Entry pairs = (Map.Entry) iterator.next();
						getRequest.setHeader(pairs.getKey().toString(), pairs.getValue().toString());
					}
				}
			}	
			HttpResponse response = httpClient.execute(getRequest);
			LOGGER.debug("Response for HTTP Request to URL " + url + " response: " + response.toString()
					+ " \n status code: " + response.getStatusLine().getStatusCode());
			if (response.getStatusLine().getStatusCode() < 200 || response.getStatusLine().getStatusCode() >300) {
				LOGGER.debug("Response status code out of range 200-300");
				return "Failed";
			}

			BufferedReader br = new BufferedReader(new InputStreamReader((response.getEntity().getContent())));

			String output, responseString = "";
			while ((output = br.readLine()) != null) {
				responseString += output;
			}

			LOGGER.debug("Response string HTTP Request to URL " + url + "\n response string: \n " + responseString);
			httpClient.getConnectionManager().shutdown();

			return responseString;

		} catch (Exception e) {
            return "Failed";
		}
	}

	public static void setCookie(String cookieName, String cookieValue, HttpServletResponse response){
		Cookie newCookie = new Cookie(cookieName,cookieValue);
		newCookie.setPath("/");
		response.addCookie(newCookie);
	}

	public static Cookie getCookie(String cookieName, HttpServletRequest request){
		Cookie[] cookies = request.getCookies();
		if(cookies!=null){
			for(Cookie cookie:cookies){
				if(cookie.getName().equalsIgnoreCase(cookieName)){
					return cookie;
				}
			}
		}
		return null;
	}

	public static void removeCookie(String cookieName,HttpServletRequest request,HttpServletResponse response){
		Cookie cookie = getCookie(cookieName,request);
		if(cookie!=null){
			cookie.setPath("/");
			cookie.setMaxAge(0);
			response.addCookie(cookie);
		}
	}
	
}
