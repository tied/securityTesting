package com.miniorange.sso.saml.bamboo.servlet;

import com.atlassian.json.jsonorg.JSONObject;
import com.miniorange.sso.saml.bamboo.MoSAMLSettings;
import com.miniorange.sso.saml.utils.MoSAMLUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import sun.security.x509.*;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.cert.CertificateFactory;
import java.util.Base64;
import java.util.Date;

public class MoGenerateCert extends HttpServlet {
    private static Log LOGGER = LogFactory.getLog(MoGenerateCert.class);
    private MoSAMLSettings settings;

    public MoGenerateCert(MoSAMLSettings settings) {
        this.settings = settings;
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String action = req.getParameter("action");
        switch (action) {
            case "gennewcert":
                generateNewCertificates(req, resp);
                break;
            case "revertoldcert":
                revertOldCertificate(req, resp);
                break;
            case "revertnewcert":
                revertNewCertificate(req, resp);
                break;
            case "revertoldconfiguredcert":
                revertOldConfiguredCertificate(req, resp);
                break;
            case "getdays":
                certExpireIN(req, resp);
                break;
            case "setCert":
                setCertificates(req, resp);
                break;
            case "setExpirayDate":
                setSPCertExpirayDate(req, resp);
            default:
                resp.setContentType(MediaType.APPLICATION_JSON);
                resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "The requested action is not allowed. Choose valid Action.");
        }
    }

    private void generateNewCertificates(HttpServletRequest request, HttpServletResponse response) throws IOException {
        LOGGER.debug("Generating new SP Key pairs");
        String companyName = request.getParameter("companyName");
        String locationName = request.getParameter("locationName");
        String countryCode = request.getParameter("countryCode");
        String orgUnit = request.getParameter("orgUnit");
        String emailAdsress = request.getParameter("emailAdsress");
        int validityDays = Integer.parseInt(request.getParameter("validityDays"));
        String distinguishedName;
        JSONObject jsonObject = new JSONObject();
        boolean resultStatus = false;
        if (StringUtils.isNotBlank(emailAdsress) && StringUtils.isNotBlank(companyName) && StringUtils.isNotBlank(locationName) && StringUtils.isNotBlank(countryCode) && StringUtils.isNotBlank(orgUnit) && validityDays > 0) {
            distinguishedName = "email=" + emailAdsress + ", CN=" + companyName + ", OU=" + orgUnit + ", L=" + locationName + " , C=" + countryCode;
        } else {
            sendErrorMessage(response, "Error");
            return;
        }
        try {
            KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("RSA");
            keyPairGen.initialize(2048);
            KeyPair keyPair = keyPairGen.generateKeyPair();
            String algorithm = "SHA256withRSA";
            PrivateKey privateKey = keyPair.getPrivate();
            X509CertInfo info = new X509CertInfo();
            Date from = new Date();
            Date to = new Date(from.getTime() + validityDays * 86400000l);
            CertificateValidity internal = new CertificateValidity(from, to);
            BigInteger sn = new BigInteger(64, new SecureRandom());
            X500Name owner = new X500Name(distinguishedName);
            info.set(X509CertInfo.VALIDITY, internal);
            info.set(X509CertInfo.SERIAL_NUMBER, new CertificateSerialNumber(sn));
            info.set(X509CertInfo.SUBJECT, owner);
            info.set(X509CertInfo.ISSUER, owner);
            info.set(X509CertInfo.KEY, new CertificateX509Key(keyPair.getPublic()));
            info.set(X509CertInfo.VERSION, new CertificateVersion(CertificateVersion.V3));
            AlgorithmId algorithmId = new AlgorithmId(AlgorithmId.md5WithRSAEncryption_oid);
            info.set(X509CertInfo.ALGORITHM_ID, new CertificateAlgorithmId(algorithmId));
            X509CertImpl cert = new X509CertImpl(info);
            cert.sign(privateKey, algorithm);
            algorithmId = (AlgorithmId) cert.get(X509CertImpl.SIG_ALG);
            info.set(CertificateAlgorithmId.NAME + "." + CertificateAlgorithmId.ALGORITHM, algorithmId);
            cert = new X509CertImpl(info);
            cert.sign(privateKey, algorithm);
            byte[][] keyPairBytes = new byte[2][];
            keyPairBytes[0] = keyPair.getPrivate().getEncoded();
            keyPairBytes[1] = cert.getEncoded();
            Base64.Encoder encoder = Base64.getEncoder();
            /**Taking backup of last configured key and certificate*/
            settings.setOldConfiguredPrivateSPCertificate(settings.getPrivateSPCertificate());
            settings.setOldConfiguredPublicSPCertificate(settings.getPublicSPCertificate());
            /** Updating newly generated certificate  */
            settings.setPrivateSPCertificate(MoSAMLUtils.serializePrivateCertificate(encoder.encodeToString(keyPairBytes[0])));
            settings.setPublicSPCertificate(MoSAMLUtils.serializePublicCertificate(encoder.encodeToString(keyPairBytes[1])));
            setExpiryDate(settings.getPublicSPCertificate());
            jsonObject.put("resultStatus", true);
            resultStatus = true;
        } catch (Exception e) {
            LOGGER.debug("An error occurred while generating new cert", e);
        }
        if (resultStatus) {
            sendSuccessResponse(jsonObject.toString(), response);
            return;
        }
        sendErrorMessage(response, "Error");
        return;
    }

    private void revertOldCertificate(HttpServletRequest request, HttpServletResponse response) {
        try {
            /**Taking backup of last configured key and certificate*/
            settings.setOldConfiguredPrivateSPCertificate(settings.getPrivateSPCertificate());
            settings.setOldConfiguredPublicSPCertificate(settings.getPublicSPCertificate());
            /**Restoring old default certificate */
            settings.setPrivateSPCertificate(StringUtils.EMPTY);
            settings.setPublicSPCertificate(StringUtils.EMPTY);
            setExpiryDate(settings.getPublicSPCertificate());
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("status", true);
            sendSuccessResponse(jsonObject.toString(), response);
        } catch (Exception e) {
            LOGGER.error("An error occurred while reverting old Certificates", e);
        }
    }

    private void revertNewCertificate(HttpServletRequest request, HttpServletResponse response) {
        try {
            /**Taking backup of last configured key and certificate*/
            settings.setOldConfiguredPrivateSPCertificate(settings.getPrivateSPCertificate());
            settings.setOldConfiguredPublicSPCertificate(settings.getPublicSPCertificate());
            /**Assigning new default certificate & Passing empty string to get key & Certificate from files*/
            settings.setPrivateSPCertificate(settings.getNewPrivateSPCertificate());
            settings.setPublicSPCertificate(settings.getNewPublicSPCertificate());
            setExpiryDate(settings.getPublicSPCertificate());
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("status", true);
            sendSuccessResponse(jsonObject.toString(), response);
        } catch (Exception e) {
            LOGGER.error("An error occurred while reverting old Certificates", e);
        }
    }

    private void revertOldConfiguredCertificate(HttpServletRequest request, HttpServletResponse response) {
        try {
            settings.setPrivateSPCertificate(settings.getOldConfiguredPrivateSPCertificate());
            settings.setPublicSPCertificate(settings.getOldConfiguredPublicSPCertificate());
            setExpiryDate(settings.getPublicSPCertificate());
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("status", true);
            sendSuccessResponse(jsonObject.toString(), response);
        } catch (Exception e) {
            LOGGER.error("An error occurred while reverting old Certificates", e);
        }
    }

    private void certExpireIN(HttpServletRequest request, HttpServletResponse response) {
        LOGGER.debug("Getting days remaining to expire Plugin Certificate");
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("days", settings.getSPCertExpireOn());
            response.setContentType(MediaType.APPLICATION_JSON);
            response.setStatus(HttpServletResponse.SC_OK);
            response.getOutputStream().write(jsonObject.toString().getBytes(StandardCharsets.UTF_8));
            response.getOutputStream().close();
        } catch (Exception e) {
            LOGGER.debug("An error occurred while getting days due to expire certificate", e);
        }
    }

    private void sendErrorMessage(HttpServletResponse resp, String errorMessages) throws IOException {
        resp.setContentType(MediaType.APPLICATION_JSON);
        resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        if (errorMessages != null) {
            resp.getOutputStream().write(errorMessages.getBytes());
            resp.getOutputStream().close();
        }
        return;
    }

    private void sendSuccessResponse(String result, HttpServletResponse resp) throws IOException {
        resp.setContentType(MediaType.APPLICATION_JSON);
        resp.setStatus(HttpServletResponse.SC_OK);
        if (result != null) {
            resp.getOutputStream().write(result.getBytes());
            resp.getOutputStream().close();
        }
        return;
    }

    private void setExpiryDate(String publicCertificate) {
        try {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            java.security.cert.X509Certificate cert = (java.security.cert.X509Certificate) cf
                    .generateCertificate(new ByteArrayInputStream(publicCertificate.getBytes(StandardCharsets.UTF_8)));
            settings.setSPCertExpireOn(cert.getNotAfter().toString());
        } catch (Exception e) {
            LOGGER.error("An error occurred while setting cert last date", e);
        }
    }

    private void setCertificates(HttpServletRequest request, HttpServletResponse response) {
        try {
            String privateCertificate = request.getParameter("privateCertificate");
            String publicCertificate = request.getParameter("publicCertificate");
            settings.setPrivateSPCertificate(privateCertificate);
            settings.setPublicSPCertificate(publicCertificate);
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("status", true);
            sendSuccessResponse(jsonObject.toString(), response);
        } catch (Exception e) {
            LOGGER.error("An error occurred while setting Certificates", e);
        }
    }

    private void setSPCertExpirayDate(HttpServletRequest request, HttpServletResponse response) {
        try {
            String validTo = request.getParameter("validTo");
            settings.setSPCertExpireOn(validTo);
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("status", true);
            sendSuccessResponse(jsonObject.toString(), response);
        } catch (Exception e) {
            LOGGER.error("An error occurred while setting Certificates", e);
        }
    }
}
