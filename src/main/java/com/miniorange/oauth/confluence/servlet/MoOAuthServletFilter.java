package com.miniorange.oauth.confluence.servlet;

import com.atlassian.confluence.user.AuthenticatedUserThreadLocal;
import com.atlassian.confluence.user.ConfluenceUser;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.*;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import java.net.URLEncoder;

import com.miniorange.oauth.confluence.MoOAuthSettings;
import com.miniorange.oauth.confluence.MoOAuthManager;
import com.miniorange.oauth.utils.MoOAuthHttpUtils;
import com.miniorange.oauth.confluence.MoOAuthPluginConstants;

public class MoOAuthServletFilter implements Filter {
    private MoOAuthSettings settings;
    private MoOAuthManager oauthManager;
    private static Log LOGGER = LogFactory.getLog(MoOAuthServletFilter.class);

    public MoOAuthServletFilter(MoOAuthSettings settings) {
        super();
        this.settings = settings;
        this.oauthManager = oauthManager;
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // TODO Auto-generated method stub

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletResponse res = (HttpServletResponse) response;
        HttpServletRequest req = (HttpServletRequest) request;

        String path = ((HttpServletRequest) request).getRequestURI();
        if (path.contains("/autocallback") || path.contains("/autoAuth")) {
            chain.doFilter(request, response);
            return;
        }
        if (StringUtils.isBlank(getCurrentLoggedInUser()) && !skipUrl(req)) {
            String url=req.getRequestURL().toString();
            if (!((url.matches("(.*)/login.action(.*)")) || (url.matches("(.*)/index.action(.*)")))){
                if(!isGuestCookieExpired(req)){
                    chain.doFilter(request, response);
                    return;
                }
                if (StringUtils.isNotBlank(settings.getClientID()) && settings.getDisableAnonymousAccess()) {
                    String contextPath = req.getContextPath();
                    String requestUri = null;
                    if(settings.getBaseUrl().indexOf(contextPath) < 0){
                        requestUri = req.getRequestURI().toString();
                    }else{
                        requestUri = req.getRequestURI().substring(contextPath.length());
                    }
                    LOGGER.debug("Request URI: " + requestUri);
                    url=settings.getBaseUrl()+"/login.action?os_destination="+URLEncoder.encode(requestUri + (req.getQueryString() != null ? "?" + req.getQueryString() : ""), "UTF-8");
                    oauthManager.httpRedirect((HttpServletResponse) res, url);
                    return;
                }
                chain.doFilter(request, response);
                return;
            }
            chain.doFilter(request, response);
            return;
        }
        chain.doFilter(request, response);
        return;
    }

    private Cookie getCookie(String cookieName, HttpServletRequest request) {
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

    private void clearCookie(String cookieName, HttpServletRequest request, HttpServletResponse response) {
        LOGGER.debug("Removing Cookie : " + cookieName);
        Cookie cookie = getCookie(cookieName, request);
        if (cookie != null) {
            cookie.setPath("/");
            cookie.setMaxAge(0);
            response.addCookie(cookie);
        }
    }

    @Override
    public void destroy() {

    }

    private String getCurrentLoggedInUser() {
        ConfluenceUser confluenceUser = AuthenticatedUserThreadLocal.get();
        if (confluenceUser == null) {
            return StringUtils.EMPTY;
        }
        return confluenceUser.getName();
    }

    private Boolean isGuestCookieExpired(HttpServletRequest request) {
		Cookie guestCookie = MoOAuthHttpUtils.getCookie(MoOAuthPluginConstants.GUEST_COOKIE,request);
		return guestCookie==null;
    }
    
    private Boolean skipUrl(HttpServletRequest req){
        String url=req.getRequestURL().toString();
        if ((url.matches("(.*)/plugins/servlet/oauth/auth(.*)"))||url.matches("(.*)/plugins/servlet/oauth/callback(.*)")||url.matches("(.*)plugins/servlet/oauth/logout(.*)")
            || url.matches("(.*)batch.css(.*)") || url.matches("(.*)batch.js(.*)") || url.matches("(.*)login.js(.*)") ||  url.matches("(.*)adgs-icons.ttf(.*)") ||
             url.matches("(.*)plugins/servlet/oauth/getconfig(.*)") || url.matches("(.*)/rest(.*)") || url.matches("(.*)/plugins/servlet/oauth/moapi(.*)") || url.matches("(.*)/oauth/consumer-info(.*)")
        || url.matches("(.*)/plugins/servlet/capabilities(.*)"))
            return true;
        return false;
    }
}
