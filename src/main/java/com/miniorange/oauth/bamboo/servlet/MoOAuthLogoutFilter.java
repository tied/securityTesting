package com.miniorange.oauth.bamboo.servlet;

import com.atlassian.templaterenderer.TemplateRenderer;
import com.miniorange.oauth.bamboo.MoOAuthManager;
import com.miniorange.oauth.bamboo.MoOAuthSettings;
import com.miniorange.oauth.utils.MoOAuthHttpUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.*;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static com.miniorange.oauth.bamboo.MoOAuthPluginConstants.LOGOUTCOOKIE;

public class MoOAuthLogoutFilter implements Filter{
    private MoOAuthSettings settings;
    private MoOAuthManager oauthManager;
    private TemplateRenderer renderer;
    private static Log LOGGER = LogFactory.getLog(MoOAuthLogoutFilter.class);

    public  MoOAuthLogoutFilter(MoOAuthSettings settings, MoOAuthManager oAuthManager, TemplateRenderer renderer){
        this.settings = settings;
        this.oauthManager = oAuthManager;
        this.renderer = renderer;
    }
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) servletRequest;
        HttpServletResponse res = (HttpServletResponse) servletResponse;
        HttpSession session = req.getSession();
        Cookie logoutCookie = MoOAuthHttpUtils.getCookie(LOGOUTCOOKIE, req);
//        LOGGER.debug("URL caught in filter : " + req.getRequestURL());
        if(logoutCookie!=null){
            MoOAuthHttpUtils.removeCookie(LOGOUTCOOKIE, req, res);
            if (StringUtils.isNotEmpty(settings.getSingleLogoutURL())) {
                LOGGER.debug("Redirect to single logout url: " + settings.getSingleLogoutURL());
                session.invalidate();
                oauthManager.httpRedirect(res, settings.getSingleLogoutURL());
                return;
            }
            else if(StringUtils.isNotEmpty(settings.getCustomLogoutURL())){
                session.invalidate();
                oauthManager.httpRedirect(res, settings.getCustomLogoutURL());
                return;
            }
            else if(settings.getEnableLogoutTemplate()){
                session.invalidate();
                redirectToLogoutTemplate(res);
                return;
            }
            else{
                MoOAuthHttpUtils.removeCookie(LOGOUTCOOKIE, req, res);
                session.invalidate();
                oauthManager.httpRedirect(res, settings.getBaseUrl());
                return;
            }
        }
        else{
            MoOAuthHttpUtils.removeCookie(LOGOUTCOOKIE, req, res);
            session.invalidate();
            oauthManager.httpRedirect(res, settings.getBaseUrl());
        }
        filterChain.doFilter(servletRequest, servletResponse);
    }

    @Override
    public void destroy() {

    }

    private void redirectToLogoutTemplate(HttpServletResponse response) throws IOException {
        try {
            Map<String, Object> context = new HashMap();
            context.put("baseURL", settings.getBaseUrl());
            StringBuffer result = new StringBuffer(renderer.renderFragment(settings.getLogoutTemplate(), context));
            response.setContentType("text/html;charset=utf-8");
            response.getWriter().write(result.toString());
        } catch (Exception e) {
            e.printStackTrace();
            oauthManager.httpRedirect(response, settings.getLoginPageUrl());
        }
    }
}
