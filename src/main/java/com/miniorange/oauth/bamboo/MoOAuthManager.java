package com.miniorange.oauth.bamboo;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class MoOAuthManager {

	private static Log LOGGER = LogFactory.getLog(MoOAuthManager.class);

	public static void httpRedirect(HttpServletResponse response, String redirectUrl) throws IOException {
		LOGGER.debug("Redirecting user to " + redirectUrl);
		StringBuffer htmlStart = new StringBuffer(
				"<html><head><script>window.onload = " + "function() {window.location.href=\"" + redirectUrl
						+ "\"};</script></head><body>Please " + "wait...</body></html>");
		response.setContentType("text/html");
		response.getOutputStream().write(htmlStart.toString().getBytes());
	}
	public static void httpRedirectWithText(HttpServletResponse response, String redirectUrl, String content) throws IOException {

		LOGGER.debug("Redirecting user to " + redirectUrl);
		StringBuffer htmlStart = new StringBuffer(
				"<html><head><script>window.location.href=\"" + redirectUrl
						+ "\"</script></head><body>" + content+"</body></html>");
		response.setContentType("text/html");
		response.getOutputStream().write(htmlStart.toString().getBytes());
	}
}