package com.miniorange.oauth.confluence;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.miniorange.oauth.confluence.MoOAuthManager;

public class MoOAuthManager {
	
private static Log LOGGER = LogFactory.getLog(MoOAuthManager.class);
	
	public static void httpRedirect(HttpServletResponse response, String redirectUrl) throws IOException {
		LOGGER.debug("Redirecting user to " + redirectUrl);
		StringBuffer htmlStart = new StringBuffer(
				"<html><head><script>window.location.href=\"" + redirectUrl
						+ "\"</script></head><body>Please " + "wait...</body></html>");
				
		response.setContentType("text/html");
		response.getOutputStream().write(htmlStart.toString().getBytes(StandardCharsets.UTF_8));
		response.sendRedirect(redirectUrl);
	}

}