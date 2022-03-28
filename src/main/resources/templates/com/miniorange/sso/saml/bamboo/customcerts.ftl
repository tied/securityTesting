<html>
<head>
    <meta name="decorator" content="atl.general">
    <script src="https://code.jquery.com/jquery-3.2.1.min.js"></script>
    <script>
           jQuery(document).ready(function () {
                jQuery(".aui-nav li").removeClass("aui-nav-selected");
                jQuery("#CONFIGURECERTIFICATE").addClass("aui-nav-selected");

           });
    </script>
    <style>
        .aui-page-panel-content{
            padding: 0px !important;
        }

        .aui-page-panel{
            margin: 0px !important;
        }
    </style>
</head>
<body>
    <#include "*/header.ftl" parse=true>
	<#include "*/newcertificatedialog.ftl"  parse=true>
    <div class="tabs-pane active-pane" id="certificates"  role="tabpanel" >
         <p style="font-size:13pt;">SP Certificates</p>
         <hr class="header"/>
             <#if (settings.getSPCertExpireOn()<60 && settings.getSPCertExpireOn()>0)>
                <div class="aui-message aui-message-warning">
                   <p><b>Attention!</b>
                   <p>The plugin's current certificates will expire in ${settings.getSPCertExpireOn()} days.</p>
                   <p>In case if you are using one of the given configurations. It is recommended to update the certificate in the plugin as well as in your IDP.</p>
                   <ol>
                      <li>
                         Signed SAML Authentication Request.
                      </li>
                      <li>
                         If your IDP is sending the encrypted Response.
                      </li>
                      <li>
                         If you are using SAML Single logout with OKTA.
                      </li>
                      <li>
                         You can configure your own certificate or genrate new Certificate in <a href="customcerts.action">Certificates</a> tab.
                      </li>
                   </ol>
                   </p>
                </div>
                </#if>
               <#if (settings.getSPCertExpireOn()<0)>
               <div class="aui-message aui-message-warning">
                 <p class="title">
                     <strong>Warning!</strong>
                 </p>
                 <p>The plugin own certificate has expired. Some IDPs might start denying your SSO request. Please update your certificate.</p>
             </div>
            </#if>

         <br>
			<div>
            <p style="font-size:12pt;"><b>1. Generate New Certificate.</b></p>
            <hr class="header"/>
			<p style="font-size:12pt;"> Provide your details to generate new Certificates.&emsp;&emsp;
               <input type="button" class="aui-button aui-button-primary" value="Generate New Certificates" id="dialog-show-button-cert">
            </p>
			<p><a href="downloadcertificate.action" style="cursor:pointer"><b>Click here</b></a> to download the public certificate and update it on your IDP.</p>
            </div>
            <br>
			<p style="margin-left: 20%;"><b>OR</b></p>
			<br>
		<div>
		<p style="font-size:12pt;"><b>2. Configure Own Certificates.</b></p>
        <hr class="header"/>
        <form id="configure-certificates" class="aui long-label" action="" method="POST">
            <input type="hidden" name="atl_token" value="${xsrfToken}" />
            <input type="hidden" name="certificateSubmitted" value="true"/>
            <div class="field-group">
                <label for="publicSPCertificate">Public X.509 Certificate:
                </label>
                <textarea id="publicSPCertificate" name="publicSPCertificate"
                          class="textarea long-field"
                          style="font-family:Courier New;" cols="64" rows="5">${publicSPCertificate}
                </textarea>
                <div class="description">
                    Enter the RSA X.509 Public Certificate (.cer, .crt). Format:<br />
                    <p><i>-----BEGIN CERTIFICATE-----<br/>
                          MII...<br/>
                          -----END CERTIFICATE-----</i></p>
                </div>
            </div>
            <div class="field-group">
                <label for="privateSPCertificate">Private X.509 Certificate:
                </label>
                <textarea id="privateSPCertificate" name="privateSPCertificate"
                          class="textarea long-field"
                          style="font-family:Courier New;" cols="64" rows="5">${privateSPCertificate}
                </textarea>
                <div class="description">
                    Enter the unencrypted RSA X.509 Private Certificate (.key, .pem). Format:<br />
                    <p><i>-----BEGIN PRIVATE KEY-----<br/>
                          MII...<br/>
                          -----END PRIVATE KEY-----</i></p>

                </div>
            </div>
            <br/>
			<div class="field-group">
					<p>In case if you have accidentally updated the certificate, <br>
					<ol>
					<li>
					<a id="revert-new-cert" style="cursor:pointer"><b>click here</b></a> to restore the default certificate.</p>
					</li>
					<li>
					<a id="revert-old-cert" style="cursor:pointer"><b>click here</b></a> to restore the default certificate(older certificate) [deprecated].</p>
					</li>
					<li>
					<a id="revert-old-configured-cert" style="cursor:pointer"><b>click here</b></a> to restore the certificate which was previously configured.</p>
					</li>
					</ol>
				  </div>
            <div class="field-group">
                <input type="submit" value="Save" class="aui-button aui-button-primary" style="width:100px;"/>
            </div>
        </form>
		</div>
    </div>
    </div>
    </section>
    </div>
    </div>
</body>
</html>