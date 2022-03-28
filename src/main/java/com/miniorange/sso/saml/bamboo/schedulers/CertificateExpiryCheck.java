package com.miniorange.sso.saml.bamboo.schedulers;

public interface CertificateExpiryCheck {
    public void reschedule(Long interval);
}
