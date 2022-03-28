package com.miniorange.sso.saml.bamboo.schedulers;

import com.atlassian.sal.api.lifecycle.LifecycleAware;
import com.atlassian.sal.api.scheduling.PluginScheduler;
import com.miniorange.sso.saml.bamboo.MoSAMLSettings;
import com.miniorange.sso.saml.utils.MoSendAlert;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Date;
import java.util.HashMap;

public class CertificateExpiryCheckImpl implements CertificateExpiryCheck, LifecycleAware {

    public static final String KEY = CertificateExpiryCheckImpl.class.getName() + ":instance";
    private static final String JOB_NAME = CertificateExpiryCheckImpl.class.getName() + ":job";
    private final Log LOGGER = LogFactory.getLog(CertificateExpiryCheckImpl.class);

    private MoSAMLSettings moSAMLSettings;
    private PluginScheduler pluginScheduler;
    private Long interval = 86400000L; //60000L;
    private MoSendAlert moSendAlert;

    public CertificateExpiryCheckImpl(MoSAMLSettings moSAMLSettings, PluginScheduler pluginScheduler, MoSendAlert moSendAlert){
        this.moSAMLSettings = moSAMLSettings;
        this.pluginScheduler = pluginScheduler;
        this.moSendAlert = moSendAlert;
    }


    @Override
    public void onStart() {
        reschedule(interval);
    }


    @Override
    public void reschedule(Long interval) {
        LOGGER.info("Rescheduling the Certificate Check Expiry");
        /** schedule a event to check the expiry of the certificate */
        pluginScheduler.scheduleJob(
                JOB_NAME,    									// unique name of the job
                CertificateValidatorTask.class,     			// class of the job
                new HashMap<String,Object>(){{
                    put(KEY,CertificateExpiryCheckImpl.this);
                    put("moSAMLSettings",moSAMLSettings);
                    put("moSendAlert",moSendAlert);
                }},  											// data that needs to be passed to the job
                new Date(),                 					// the time the job is to start
                interval    									// interval between repeats, 24 hours in milliseconds
        );

    }

    /** Returns the Public Certificates of the Service Provider saved in the settings */
    public String getPublicSPCertificate() {
        return moSAMLSettings.getPublicSPCertificate();
    }

    /** This is the setter method for PluginScheduler class */
    public void setPluginScheduler(PluginScheduler pluginScheduler){
        this.pluginScheduler = pluginScheduler;
    }

    /** This is the setter method for MoSAMLSettings class */
    public void setSettings(MoSAMLSettings settings) {
        this.moSAMLSettings = settings;
    }
}
