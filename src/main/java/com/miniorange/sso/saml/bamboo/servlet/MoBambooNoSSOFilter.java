package com.miniorange.sso.saml.bamboo.servlet;

import com.miniorange.sso.saml.bamboo.MoSAMLSettings;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ListIterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MoBambooNoSSOFilter implements Filter {

    private static Log LOGGER = LogFactory.getLog(MoBambooNoSSOFilter.class);

    private MoSAMLSettings settings;

    public MoBambooNoSSOFilter(MoSAMLSettings settings){
        super();
        this.settings=settings;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        LOGGER.debug("SAML NO SSO Filter initiated");
        HttpServletRequest req=(HttpServletRequest) request;
        String moSkipSso=req.getParameter("moskipsso");
        LOGGER.debug("moskipsso = " + moSkipSso);
        HttpSession session = req.getSession();
        Boolean noSSO = false;
        Boolean skipSSO = false;
        noSSO = (Boolean) session.getAttribute("noSSO");
        if(BooleanUtils.isTrue(noSSO)){
            LOGGER.debug("Switching off redirection");
            skipSSO = true;
        }

        if (settings.isEvaluationOrSubscriptionLicense()||(moSkipSso != null && moSkipSso.contains("true"))&& skipSSO){
            LOGGER.debug("No Valid license found or No SSO URL Entered redirecting to Login page");
            chain.doFilter(request,response);
            return;
        } else {
            filterNoSSOUrl(request,response,chain);
        }



    }

    @Override
    public void destroy() {

    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    private void filterNoSSOUrl(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException{
        LOGGER.debug("Validating requested url for bypassing SSO for Bamboo application user");
        HttpServletRequest req=(HttpServletRequest) request;
        HttpServletResponse res=(HttpServletResponse) response;
        HttpSession session = req.getSession();
        String BambooNoSSOUrl ="";
        if (req.getParameter("os_destination") != null) {
            BambooNoSSOUrl = URLDecoder.decode(req.getParameter("os_destination"),"UTF-8");
        }
        ListIterator<String> iterator = settings.getNoSSOUrls().listIterator();
        LOGGER.debug("Checking for No SSO URLs");
        while (iterator.hasNext()){
            Pattern pattern = Pattern.compile(".*" + iterator.next() + ".*");
            Matcher matcher = pattern.matcher(BambooNoSSOUrl);
            if (matcher.find()) {
                LOGGER.debug("No SSO user-agent is configured and detected! Skipping SSO");
                session.setAttribute("noSSO", true);
                res.sendRedirect(settings.getLoginPageUrl().concat("?moskipsso=true&os_destination=").concat(URLEncoder.encode(BambooNoSSOUrl, "UTF-8")));
                return;
            }

        }
        chain.doFilter(request,response);
        return;

    }

    public MoSAMLSettings getSettings() {
        return settings;
    }

    public void setSettings(MoSAMLSettings settings) {
        this.settings = settings;
    }
}