package com.miniorange.oauth.confluence.servlet;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.Map;

import java.io.IOException;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.atlassian.confluence.user.AuthenticatedUserThreadLocal;
import com.atlassian.confluence.user.ConfluenceUser;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.atlassian.confluence.renderer.radeox.macros.MacroUtils;
import com.atlassian.confluence.util.velocity.VelocityUtils;
import com.miniorange.oauth.confluence.MoOAuthManager;
import com.miniorange.oauth.confluence.MoOAuthSettings;

public class MoOAuthRedirectFilter implements Filter {
    private MoOAuthSettings settings;
    private MoOAuthManager oauthManager;
    private static Log LOGGER = LogFactory.getLog(MoOAuthRedirectFilter.class);

    public MoOAuthRedirectFilter(MoOAuthSettings settings, MoOAuthManager oauthManager) {
        super();
        this.settings = settings;
        this.oauthManager = oauthManager;

    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        LOGGER.info("Initializing Filter!");

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        String path = ((HttpServletRequest) request).getRequestURI();
        if (path.contains("/autocallback") || path.contains("/autoAuth")) {
            chain.doFilter(request, response);
            return;
        }
        LOGGER.info("OAuth SSO Redirect Filter Called");
        try {
            if (settings.isLicenseValid() && StringUtils.isNotBlank(settings.getClientID())) {
                ConfluenceUser confluenceUser = AuthenticatedUserThreadLocal.get();
                if (confluenceUser != null || !settings.getEnableOAuthSSO()) {
                    LOGGER.debug("Already Logged in or SSO is off");
                    chain.doFilter(request, response);
                    return;
                } else if (settings.getDefaultLoginDisabled() && !settings.getEnableAutoRedirectDelay()) {
                    Boolean backdoor = false;
                    String backdoorKey = settings.getBackdoorKey();
                    String backdoorValue = settings.getBackdoorValue();
                    if (req.getParameter(backdoorKey) != null) {
                        backdoor = req.getParameter(backdoorKey).equals(backdoorValue);

                    }
                    if (settings.getBackdoorEnabled() && backdoor) {
                        LOGGER.debug("Entering through backdoor");
                        chain.doFilter(request, response);
                        return;
                    } else {
                        String oauthUrl = settings.getBaseUrl() + "/plugins/servlet/oauth/auth";
                        String osDestination = req.getParameter("os_destination");
                        String destination = req.getParameter("destination");
                        if (StringUtils.isNotBlank(StringUtils.trim(osDestination)) && !StringUtils.equals(osDestination, "/")) {
                            if (!(osDestination.indexOf("/") == 0)) {
                                osDestination = "/" + osDestination;
                            }
                            oauthUrl += "?return_to=" + URLEncoder.encode(osDestination, "UTF-8");

                        } else if (StringUtils.isNotBlank(StringUtils.trim(destination)) && !StringUtils.equals(destination, "/")) {
                            if (!(destination.indexOf("/") == 0)) {
                                destination = "/" + destination;
                            }
                            oauthUrl += "?return_to=" + URLEncoder.encode(destination, "UTF-8");
                        }
                        LOGGER.debug("Redirecting to Idp via filter to url "+ oauthUrl);
                        //oauthManager.httpRedirect((HttpServletResponse) response, oauthUrl);
                        res.sendRedirect(oauthUrl);
                        chain.doFilter(request,response);
                        return;

                    }
                }else{
                    LOGGER.debug("No redirection through filter");
                    chain.doFilter(request,response);
                    return;
                }

            } else {
                LOGGER.debug("Invalid license or App not configured");
                chain.doFilter(request, response);
                return;
            }
        } catch(Exception e){
            LOGGER.debug("Error occured which redirecting to Idp" + e);
        }
    }

    @Override
    public void destroy() {

    }
}