package com.miniorange.oauth.confluence.servlet;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URLEncoder;
import java.security.Principal;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Cookie;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.atlassian.confluence.security.websudo.DefaultWebSudoManager;
import com.atlassian.confluence.user.AuthenticatedUserThreadLocal;
import com.atlassian.seraph.auth.DefaultAuthenticator;
import com.atlassian.seraph.config.SecurityConfigFactory;
import com.miniorange.oauth.confluence.MoOAuthSettings;
import com.miniorange.oauth.utils.MoOAuthHttpUtils;

import com.atlassian.seraph.auth.DefaultAuthenticator;
import com.atlassian.seraph.config.SecurityConfigFactory;
import com.miniorange.oauth.confluence.MoOAuthAuthenticator;

public class MoOAuthSessionTimeoutServlet extends HttpServlet {
	
	private static Log LOGGER = LogFactory.getLog(MoOAuthCallbackServlet.class);
	private DefaultWebSudoManager defaultWebSudoSessionManager;
	private MoOAuthSettings settings;
	private final DefaultAuthenticator authenticator;

	public MoOAuthSessionTimeoutServlet(DefaultWebSudoManager defaultWebSudoSessionManager, MoOAuthSettings settings) {
		super();
		this.defaultWebSudoSessionManager = defaultWebSudoSessionManager;
		this.settings = settings;
		this.authenticator = (DefaultAuthenticator) SecurityConfigFactory.getInstance().getAuthenticator();
	}
	
	protected DefaultAuthenticator getAuthenticator() {
		return this.authenticator;
	}
	
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		LOGGER.debug("inside MoOAuthSessionTimeoutServlet().. ");
		
		try  {
			if (MoOAuthHttpUtils.getCookie("SESSIONCOOKIE", request) == null) {
				LOGGER.debug("SESSIONCOOKIE expired");
				
				defaultWebSudoSessionManager.invalidateSession(request, response);
				AuthenticatedUserThreadLocal.set(null);
				
				String redirectUrl = settings.getLoginPageUrl();				
				
				String redirectTo = StringUtils.EMPTY;
				
				if (request.getParameter("os_destination") != null){
					redirectTo = URLEncoder.encode(request.getParameter("os_destination"),"UTF-8");
				} 
				
			    LOGGER.debug("redirectTo = " + redirectTo);
				if (StringUtils.isNotBlank(redirectTo)) {
					if(!StringUtils.startsWith(redirectTo,"%2"))
						redirectTo = "%2"+redirectTo;
					redirectUrl += "?os_destination=" + redirectTo;
				}
				
				MoOAuthLogoutFilter.clearCookie("SESSIONCOOKIE", request, (HttpServletResponse) response);
				MoOAuthLogoutFilter.clearCookie("VALIDATESESSIONCOOKIE", request, (HttpServletResponse) response);
				
				LOGGER.debug("Login Cookie : "+ settings.getLoginCookie());
				Cookie JSESSIONIDCOOKIE = MoOAuthHttpUtils.getCookie(settings.getLoginCookie(), request);
				LOGGER.debug("before JSESSIONIDCOOKIE : "+ JSESSIONIDCOOKIE);
				JSESSIONIDCOOKIE.setMaxAge(0);
				JSESSIONIDCOOKIE.setValue("");
				response.addCookie(JSESSIONIDCOOKIE);
				LOGGER.debug(" after JSESSIONIDCOOKIE : "+ JSESSIONIDCOOKIE);
				
				LOGGER.debug("redirecting to "+ redirectUrl);
				
				response.sendRedirect(redirectUrl);				
					
				return;	
			}
			
		} catch (Exception e) {
			LOGGER.debug("an error occurred : "+ e.getMessage());
			return;
		}
		
	}
	
	/*public boolean destroyEstablishedSession(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse ) {
        LOGGER.debug("Invoking destroyEstablishedSession for");
        try {
        	Class<?> authenticatorClass = DefaultAuthenticator.class;
			String methodName = "logout";
			Class<?>[] parameterTypes = new Class[]{HttpServletRequest.class, HttpServletResponse.class, Principal.class};
			Object[] parameters = new Object[]{httpServletRequest, httpServletResponse};
			Method methodToInvoke = authenticatorClass.getDeclaredMethod(methodName, parameterTypes);
			methodToInvoke.setAccessible(true);
			Object returnValue = methodToInvoke.invoke(this.authenticator, parameters);
			if(returnValue instanceof Boolean) {
				    return ((Boolean)returnValue).booleanValue();
			}
	    } catch (NoSuchMethodException var10) {
			LOGGER.error("Calling authoriseUserAndEstablishSession() via reflection failed."+ var10);
		} catch (SecurityException var11) {
			LOGGER.error("Calling authoriseUserAndEstablishSession() via reflection failed."+ var11);
		} catch (IllegalAccessException var12) {
			LOGGER.error("Calling authoriseUserAndEstablishSession() via reflection failed."+ var12);
		} catch (InvocationTargetException var13) {
			LOGGER.error("authoriseUserAndEstablishSession() threw exception"+ var13.getCause());
		}
		return Boolean.FALSE;
	}
	
	public DefaultWebSudoManager getDefaultWebSudoSessionManager() {
		return defaultWebSudoSessionManager;
	}

	public void setDefaultWebSudoSessionManager(DefaultWebSudoManager defaultWebSudoSessionManager) {
		this.defaultWebSudoSessionManager = defaultWebSudoSessionManager;
	}

	public void setSettings(MoOAuthSettings settings) {
		this.settings = settings;
	}*/
	
}
