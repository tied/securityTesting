package com.miniorange.oauth.bamboo.servlet;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;

import com.miniorange.oauth.bamboo.MoOAuthManager;
import com.miniorange.oauth.bamboo.MoOAuthSettings;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
public class MoOAuthRedirectFilter implements Filter{

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

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        LOGGER.info("OAuth SSO Redirect Filter Called");

        //Auto redirect is on and delay is off
        if(settings.getDefaultLoginDisabled() && !settings.getEnableAutoRedirectDelay()){
            //backdoor url case
            if(req.getParameter("oauth_sso")!=null){
                if(req.getParameter("oauth_sso").equals("false")){
                    chain.doFilter(request, response);
                    return;
                }
            }
            String oauthUrl = req.getContextPath() + "/plugins/servlet/oauth/auth";
            String osDestination = req.getParameter("os_destination");
            if(StringUtils.isNotBlank(StringUtils.trim(osDestination)) && !StringUtils.equals(osDestination,"/") ){
                if (!(osDestination.indexOf("/") == 0)) {
                    osDestination = "/" + osDestination;
                }
                oauthUrl += "?return_to=" + URLEncoder.encode(osDestination,"UTF-8");

            }


            oauthManager.httpRedirect((HttpServletResponse) response,oauthUrl);
            return;
        }
        chain.doFilter(request, response);
        return;

    }

    @Override
    public void destroy() {

    }
}
