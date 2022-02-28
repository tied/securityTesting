package com.miniorange.oauth.confluence;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.Principal;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.atlassian.seraph.auth.DefaultAuthenticator;
import com.atlassian.seraph.config.SecurityConfigFactory;
import com.miniorange.oauth.confluence.MoOAuthAuthenticator;

public class MoOAuthAuthenticator {
	
	private static Log LOGGER = LogFactory.getLog(MoOAuthAuthenticator.class);

	private final DefaultAuthenticator authenticator;
	
	public MoOAuthAuthenticator() {
		this.authenticator = (DefaultAuthenticator) SecurityConfigFactory.getInstance().getAuthenticator();
	}
	
	protected DefaultAuthenticator getAuthenticator() {
		return this.authenticator;
	}

	public boolean createUserSession(final HttpServletRequest request, final HttpServletResponse response, final
            Principal principal)  {
		boolean authorised = this.doAuthoriseUserAndEstablishSession(request, response,principal);
		return authorised;
	}
	
	public boolean doAuthoriseUserAndEstablishSession(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Principal principal ) {
        LOGGER.debug("Invoking authoriseUserAndEstablishSession for - "+ principal.getName());
        try {
			Class<?> authenticatorClass = DefaultAuthenticator.class;
			String methodName = "authoriseUserAndEstablishSession";
			Class<?>[] parameterTypes = new Class[]{HttpServletRequest.class, HttpServletResponse.class, Principal.class};
			Object[] parameters = new Object[]{httpServletRequest, httpServletResponse, principal};
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

}
