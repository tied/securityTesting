package com.miniorange.sso.saml.bamboo.servlet;

import com.atlassian.bamboo.user.BambooAuthenticationContext;
import com.atlassian.user.User;
import com.miniorange.sso.saml.bamboo.MoSAMLSettings;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.miniorange.sso.saml.bamboo.action.MoIdpListAction;
import com.miniorange.sso.saml.utils.MoSAMLUtils;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import java.io.IOException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class MoSAMLLoginFilter implements Filter {
	private static Log LOGGER = LogFactory.getLog(MoSAMLLoginFilter.class);

	private MoSAMLSettings settings;
	private BambooAuthenticationContext bambooAuthenticationContext;

	public MoSAMLLoginFilter(MoSAMLSettings settings, BambooAuthenticationContext bambooAuthenticationContext){
		this.settings = settings;
		this.bambooAuthenticationContext = bambooAuthenticationContext;
	}
	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		// Do Nothing
	}
	
	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		
		try {
			HttpServletRequest req = (HttpServletRequest) request;
			HttpServletResponse res = (HttpServletResponse) response;
			LOGGER.debug("Filter based auth called");
			User bambooUser = bambooAuthenticationContext.getUser();
			if (bambooUser != null) {
				chain.doFilter(request, response);
				return;
			} 
//			Boolean isConfigured = StringUtils.isNotBlank(settings.getSsoServiceUrl());
//			if (isConfigured) {
//				HttpServletRequest req = (HttpServletRequest) request;
//				HttpServletResponse res = (HttpServletResponse) response;
//				Boolean defaultLoginDisabled = settings.getDefaultLoginDisabled();
//				Boolean backdoorEnabled = settings.getBackdoorEnabled();
//
//				if (defaultLoginDisabled && !(backdoorEnabled && StringUtils.equalsIgnoreCase(req.getParameter
//						("saml_sso"), "false"))) {
//					StringBuffer buffer = new StringBuffer(settings.getLoginServletUrl());
//					String redirectTo = request.getParameter("os_destination");
//					if(StringUtils.isNotBlank(redirectTo)) {
//						buffer.append("?redirect_to=").append(redirectTo);
//					}
//					res.sendRedirect(buffer.toString());
//				} else {
//					chain.doFilter(request, response);
//
//			}
			if(settings.getIdPList().size()!=1 && settings.getBambooRedirectionRulesMap().size()>0)
			{
				LOGGER.debug("Multiple IDPs configured");
				chain.doFilter(request,response);
				return;
			}
			Boolean redirect = false;
			Boolean backdoor = false;
			String idp = StringUtils.EMPTY;
			Boolean backdoorEnabled = settings.getBackdoorEnabled();
			String backdoorKey = settings.getBackdoorKey();
			String backdoorValue = settings.getBackdoorValue();
			Boolean enableAutoRedirectDelay = settings.getEnableAutoRedirectDelay();

			if(req.getParameter(backdoorKey)!=null){
				backdoor=req.getParameter(backdoorKey).equals(backdoorValue);
			}

			if(settings.getBambooRedirectionRulesMap().size()==0 && (!settings.getDefaultBambooIDP().equals("loginPage")&&!settings.getDefaultBambooIDP().equals("redirectUrl")))
			{
				LOGGER.debug("Redirect changed from False to true");
				redirect = true;
				idp = settings.getDefaultBambooIDP();
			}
			if (req.getParameter("moskipsso") != null && StringUtils.equals(req.getParameter("moskipsso"),"true") ){
				HttpSession session = req.getSession();
				Boolean noSSO = false;
				noSSO = (Boolean) session.getAttribute("noSSO");
				LOGGER.debug("noSSO = " + noSSO);
				if (BooleanUtils.isTrue(noSSO)){
					LOGGER.debug("Switching off redirection.");
					redirect = false;
				}else{
					redirect = true;
				}

			}

			if(!(redirect && !enableAutoRedirectDelay && !(backdoorEnabled &&backdoor))){
				LOGGER.debug("No redirection through filter");
				chain.doFilter(request,response);
				return;
			}

			StringBuffer buffer = new StringBuffer(settings.getLoginServletUrl());
			String redirectTo = StringUtils.EMPTY;
			if(req.getParameter("os_destination")!=null)
			{
				LOGGER.debug("Value of os_destination = "+ req.getParameter("os_destination"));
				redirectTo = req.getParameter("os_destination");
			}
			if(StringUtils.isNotBlank(redirectTo)){
				LOGGER.debug("Sanitizing the redirectTo value");
				redirectTo = MoSAMLUtils.sanitizeText(redirectTo);
				LOGGER.debug("RedirectTo after sanitization and URL encoding " + redirectTo);
				buffer.append("?return_to=").append(redirectTo);
				if (StringUtils.isNotEmpty(idp)){
					buffer.append("&idp=").append(idp);
				}
			}
			if (request.getParameter("moskipsso") != null && StringUtils.equals(request.getParameter("moskipsso"),"true") ){
				redirect = false;
			}
			res.sendRedirect(buffer.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void destroy() {
		// Do Nothing
	}

	public void setSettings(MoSAMLSettings settings) {
		this.settings = settings;
	}

	public BambooAuthenticationContext getBambooAuthenticationContext() {
		return bambooAuthenticationContext;
	}

	public void setBambooAuthenticationContext(BambooAuthenticationContext bambooAuthenticationContext) {
		this.bambooAuthenticationContext = bambooAuthenticationContext;
	}

	public MoSAMLSettings getSettings() {
		return settings;
	}
}
