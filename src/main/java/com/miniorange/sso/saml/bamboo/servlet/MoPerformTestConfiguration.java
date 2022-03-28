package com.miniorange.sso.saml.bamboo.servlet;

import com.miniorange.sso.saml.bamboo.MoSAMLSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class MoPerformTestConfiguration  extends HttpServlet {
    private MoSAMLSettings settings;
    private static final Logger LOGGER = LoggerFactory.getLogger(MoPerformTestConfiguration.class);

    public MoPerformTestConfiguration(MoSAMLSettings settings) {
        this.settings = settings;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        LOGGER.debug("Calling doGet From MoPerformTestConfiguration.");
        String idpId = request.getParameter("idp");
        if (StringUtils.isNotBlank(idpId) && settings.getIdPList().contains(idpId)) {
            LOGGER.debug("Valid IDP ID found");
            response.sendRedirect(settings.getBaseUrl() +
                    "/plugins/servlet/saml/auth?return_to=testidpconfiguration&idp="+ idpId +"&setup=quicktestconfig");
            return;
        } else {
            LOGGER.error("Invalid IDP ID found : "+idpId);
            StringBuffer message = new StringBuffer("<div style=\"font-family:Calibri;padding:0 3%;\">");
            message = message.append("<div style=\"color: #a94442;background-color: #f2dede;padding: 15px;"
                    + "margin-bottom: 20px;text-align:center;border:1px solid #E6B3B2;font-size:18pt;\">TEST "
                    + "FAILED</div><div style=\"color: #a94442;font-size:14pt; margin-bottom:20px;\">WARNING: "
                    + "Invalid IDP ID Found. Make sure IDP ID id valid.</div>");

            response.setCharacterEncoding("iso-8859-1");
            response.setContentType("text/html");
            response.getOutputStream().write(message.toString().getBytes(StandardCharsets.UTF_8));
            return;
        }
    }

    public MoSAMLSettings getSettings() {
        return settings;
    }

    public void setSettings(MoSAMLSettings settings) {
        this.settings = settings;
    }

}
