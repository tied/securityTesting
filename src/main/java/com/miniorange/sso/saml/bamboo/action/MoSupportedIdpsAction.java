package com.miniorange.sso.saml.bamboo.action;

import com.atlassian.bamboo.security.xsrf.XsrfTokenUtils;
import com.atlassian.bamboo.ww2.BambooActionSupport;
import com.atlassian.plugin.PluginException;
import com.atlassian.sal.api.auth.LoginUriProvider;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.sal.api.user.UserProfile;
import com.miniorange.sso.saml.bamboo.MoPluginHandler;
import com.miniorange.sso.saml.bamboo.MoSAMLSettings;
import org.apache.struts2.ServletActionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;

public class MoSupportedIdpsAction extends BambooActionSupport {
    private static final Logger LOGGER = LoggerFactory.getLogger(MoSupportedIdpsAction.class);

    private MoPluginHandler moPluginHandler;
    private MoSAMLSettings settings;
    private UserManager userManager;
    private LoginUriProvider loginUriProvider;
    private ArrayList idpGuides;
    private String xsrfToken;

    public MoSupportedIdpsAction(MoPluginHandler moPluginHandler, MoSAMLSettings settings, UserManager userManager,
                                 LoginUriProvider loginUriProvider) {
        this.moPluginHandler = moPluginHandler;
        this.settings = settings;
        this.userManager = userManager;
        this.loginUriProvider = loginUriProvider;
    }

    public void validate() {
        LOGGER.info("Supported IDPs Action doValidate called");
    }

    public String execute() throws Exception {
        LOGGER.debug("Supported Idps Action execute called");
        HttpServletRequest request = ServletActionContext.getRequest();
        HttpServletResponse response = ServletActionContext.getResponse();
        //xsrfToken = XsrfTokenUtils.getXsrfToken(request);

        final UserProfile user = userManager.getRemoteUser();

        if (user != null && userManager.isAdmin(user.getUserKey())) {
            try {
                LOGGER.debug("Supported IDPs doExecute called");
                if (!settings.isValidLicense()) {
                    LOGGER.error("No valid license found.");
                    return "upm";
                }
                //settings.setPageNumber("1");
                return "success";
            } catch (PluginException e) {
                LOGGER.error("An plugin exception occurred while initializing service provider configuration", e);
                addActionError(e.getMessage());
                return "input";
            } catch (Exception e) {
                LOGGER.error("An error occurred while initializing service provider configuration", e);
                addActionError("An error occured while saving your details. Please check logs for more info.");
                return "input";
            }
        }else {
            response.sendRedirect(loginUriProvider.getLoginUri(getUri(request)).toASCIIString());
            return null;
        }
    }

    private URI getUri(HttpServletRequest request) {
        StringBuffer builder = request.getRequestURL();
        if (request.getQueryString() != null) {
            builder.append("?");
            builder.append(request.getQueryString());
        }
        return URI.create(builder.toString());
    }

    public ArrayList<String> getIdpGuides() {
        ArrayList<String> idp = new ArrayList<>();
        idp.add("ADFS");
        idp.add("AWS");
        idp.add("G_Suite");
        idp.add("Centrify");
        idp.add("Okta");
        idp.add("OneLogin");
        idp.add("Salesforce");
        idp.add("JBoss_Keycloak");
        idp.add("Oracle");
        idp.add("Bitium");
        idp.add("PingFederate");
        idp.add("Ping_One");
        idp.add("WSO2");
        idp.add("OpenAM");
        idp.add("SimpleSAML_PHP");
        idp.add("Azure_AD");
        idp.add("Shibboleth_2");
        idp.add("Shibboleth_3");
        idp.add("RSA_SecurID");
        idp.add("AuthAnvil");
        idp.add("Auth0");
        idp.add("CA_Identity");
        Collections.sort(idp);
        this.idpGuides = idp;
        return this.idpGuides;
    }

    public MoPluginHandler getMoPluginHandler() {
        return moPluginHandler;
    }

    public void setMoPluginHandler(MoPluginHandler moPluginHandler) {
        this.moPluginHandler = moPluginHandler;
    }

    public MoSAMLSettings getSettings() {
        return settings;
    }

    public void setSettings(MoSAMLSettings settings) {
        this.settings = settings;
    }

    public void setIdpGuides(ArrayList idpGuides) {
        this.idpGuides = idpGuides;
    }
}
