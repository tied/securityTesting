<html>
<head>
    <meta name="decorator" content="atl.general"/>
    <script>
        AJS.$(function() {
            AJS.$(".aui-nav li").removeClass("aui-nav-selected");
            AJS.$("#mo-custom-certs").addClass("aui-nav-selected");
            AJS.$("#mo-saml").addClass("aui-nav-selected");
        });
    </script>
</head>
<body>
<#include "*/saml/headers/samlheader.ftl" parse=true>
<#include "*/saml/newcertdialog.ftl" parse=true>
<div class="tabs-pane active-pane" id="certificates"  role="tabpanel">
    <h1>SP Certificates</h1>
    <hr class="header"/>
    <#if settings.getSPCertExpireOn() < 0>
        <div class="aui-message aui-message-warning">
            <p class="title">
                <strong>Warning!</strong>
            </p>
            <p>The plugin own certificate has expired. Some IDPs might start denying your SSO request. Please update your certificate.</p>
            <br>
        </div>
    </#if>

    <#if settings.getSPCertExpireOn() < 60 && settings.getSPCertExpireOn() gt 0>
        <div class="aui-message aui-message-warning">
            <p><b>Attention!</b>
            <p>The plugin's current certificates will expire in ${settings.getSPCertExpireOn()} days.</p>
            <p>In case, if you are using one of the given configurations. It is recommended to update the certificate in the plugin as well as in your IDP.</p>
            <ol>
                <li>IDP is expecting the siged SAML Authentication/Logout Request</li>
                <li>IDP is encrypting the SAML Response or Assertions</li>
            </ol>
            </p>
            <br>
        </div>
    </#if>

    <div>
        <p style="font-size:12pt;"><b>1. Generate New Certificate.</b></p>
        <hr class="header"/>
        <p style="font-size:12pt;"> Provide your details to generate new Certificates.&emsp;&emsp;
            <input type="button" class="aui-button aui-button-primary" value="Generate New Certificates" id="dialog-show-button-cert">
        </p>
        <p><a href="downloadcertificate.action" style="cursor:pointer"><b>Click Here</b></a> to download the public certificate and update it on your IDP.</p>
    </div>
    <br>
    <p style="margin-left: 20%;"><b>OR</b></p>
    <br>
    <div>
        <p style="font-size:12pt;"><b>2. Configure Own Certificates.</b></p>
        <hr class="header"/>
        <form id="configure-certificates" class="aui long-label" action="" method="POST">
            <input type="hidden" name="certificateSubmitted" value="true"/>
            <input type="hidden" name="atl_token" value="${xsrfToken}" />

            <div class="field-group">
                <label for="publicSPCertificate">Public X.509 Certificate:
                </label>
                <textarea id="publicSPCertificate" name="publicSPCertificate" class="textarea long-field"
                          style="font-family:Courier New;" cols="64" rows="4">${publicSPCertificate}</textarea>
                <div class="description">
                    Enter the RSA X.509 Public Certificate (.cer, .crt). Format:<br />
                    <code>-----BEGIN CERTIFICATE-----<br/>
                        MII...<br/>
                        -----END CERTIFICATE-----
                    </code>
                </div>
            </div>
            <div class="field-group">
                <label for="privateSPCertificate">Private X.509 Certificate:
                </label>
                <textarea id="privateSPCertificate" name="privateSPCertificate"class="textarea long-field"
                          style="font-family:Courier New;" cols="64" rows="4">${privateSPCertificate}</textarea>
                <div class="description">
                    Enter the unencrypted RSA X.509 Private Certificate. Format:<br />
                    <code>-----BEGIN PRIVATE KEY-----<br/>
                        MII...<br/>
                        -----END PRIVATE KEY-----
                    </code>
                </div>
            </div>
            <br/>
            <div class="field-group">
                <p>In case if you have accidentally updated the certificate, <br>
                <ol>
                    <li>
                        <a id="revert-new-cert" style="cursor:pointer"><b>Click Here</b></a> to restore the default certificate.</p>
                </li>
                <li>
                    <a id="revert-old-cert" style="cursor:pointer"><b>Click Here</b></a> to restore the default certificate(older certificate) [deprecated].</p>
                </li>
                <li>
                    <a id="revert-old-configured-cert" style="cursor:pointer"><b>Click Here</b></a> to restore the certificate which was previously configured.</p>
                </li>
                </ol>
            </div>
            <div class="field-group">
                <input type="submit" value="Save" class="aui-button aui-button-primary" style="width:100px;"/>
            </div>
        </form>
    </div>
</div>
<#include "*/footer.ftl" parse=true>
</body>
</html>