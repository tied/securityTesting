package com.miniorange.sso.saml.bamboo.schedulers;

import com.atlassian.sal.api.scheduling.PluginJob;
import com.miniorange.sso.saml.bamboo.MoSAMLSettings;
import com.miniorange.sso.saml.utils.MoSendAlert;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.Map;

public class CertificateValidatorTask implements PluginJob {

    private static Log LOGGER = LogFactory.getLog(CertificateValidatorTask.class);
    public void execute(Map<String, Object> jobDataMap) {

        try {
            LOGGER.info("Starting Certificate Validator Scheduler.");

            /** Get the MoSAMLSettings Class */
            final CertificateExpiryCheckImpl impl
                    = (CertificateExpiryCheckImpl) jobDataMap.get(CertificateExpiryCheckImpl.KEY);
            MoSAMLSettings moSAMLSettings =(MoSAMLSettings) jobDataMap.get("moSAMLSettings") ;
            MoSendAlert moSendAlert=(MoSendAlert) jobDataMap.get("moSendAlert");
            /** Convert the formatted public certificate string into a X509 certificate */
            CertificateFactory cf = CertificateFactory.getInstance("x509");
            X509Certificate x509Certificate = (X509Certificate)
                    cf.generateCertificate(new ByteArrayInputStream(impl.getPublicSPCertificate().getBytes(StandardCharsets.UTF_8)));
            Date expiryDate = x509Certificate.getNotAfter(); /** get the expiry date */
            LOGGER.debug("Expiry Date of the certificate provided: " + expiryDate);

            /** Set certificate expiry date in days & send mail to admin user*/
            moSAMLSettings.setSPCertExpireOn(expiryDate.toString());
            Long dueIN=moSAMLSettings.getSPCertExpireOn();
            if (dueIN==60 || dueIN==30|| dueIN==15 || dueIN==1){
                boolean sent=moSendAlert.sendMail();
                LOGGER.debug("Send Email status "+sent);
            }
        }catch (CertificateException e) {
            LOGGER.error("An error occurred while running certificate validator", e);
        }catch (Exception e) {
            LOGGER.error("An error occurred while running certificate validator", e);
        }
    }
}
