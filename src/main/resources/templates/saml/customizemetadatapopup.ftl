<section role="dialog" id="sp_customize_metadata_dialog" class="aui-layer aui-dialog2 aui-dialog2-medium" aria-hidden="true"
         style="height: auto;width: 50%">
    <header class="aui-dialog2-header">
        <h3 class="aui-dialog2-header-main" style="max-width: none">Customize Metadata</h3>
        <a id="sp_customize_metadata_dialog_close_button" class="aui-dialog2-header-close">
            <span class="aui-icon aui-icon-small aui-iconfont-close-dialog">Close</span>
        </a>
    </header>
    <div class="aui-dialog2-content" style="padding: 10px 40px; padding-bottom: 20px; height: auto;">
        <form id="customize_metadata_form" class="aui" action="" method="POST">
            <input type="hidden" name="certificatesSubmitted" value="true"/>
            <input type="hidden" name="atl_token" value="${xsrfToken}" />
            <p><b>Organization Details</b></p>
            <hr class="header">
            <div class="field-group">
                <label for="customOrganizationName">Organization name:</label>
                <input type="text" id="customOrganizationName" name="customOrganizationName"
                       value="${customOrganizationName}" class="text long-field">
            </div>
            <div class="field-group">
                <label for="customOrganizationDisplayName">Organization display name:</label>
                <input type="text" id="customOrganizationDisplayName" name="customOrganizationDisplayName"
                       value="${customOrganizationDisplayName}" class="text long-field">
            </div>
            <div class="field-group">
                <label for="customOrganizationUrl">Organization URL:</label>
                <input type="url" id="customOrganizationUrl" name="customOrganizationUrl"
                       value="${customOrganizationUrl}" class="text long-field">
            </div>
            <p><b>Technical contact Details</b></p>
            <hr class="header">
            <div class="field-group">
                <label for="technicalContactName">Technical Contact name:</label>
                <input type="text" id="technicalContactName" name="technicalContactName"
                       value="${technicalContactName}" class="text long-field">
            </div>
            <div class="field-group">
                <label for="technicalContactEmail">Technical Contact email:</label>
                <input type="text" id="technicalContactEmail" name="technicalContactEmail"
                       value="${technicalContactEmail}" class="text long-field">
            </div>
            <p><b>Support contact Details</b></p>
            <hr class="header">
            <div class="field-group">
                <label for="supportContactName">Support Contact name:</label>
                <input type="text" id="supportContactName" name="supportContactName" value="${supportContactName}"
                       class="text long-field">
            </div>
            <div class="field-group">
                <label for="supportContactEmail">Support Contact email:</label>
                <input type="text" id="supportContactEmail" name="supportContactEmail" value="${supportContactEmail}"
                       class="text long-field">
            </div>
            <hr/>
            <br>
            <div class="">
                <#if signing == true>
                    <input type="checkbox" checked="true" id="signing" value="true" name="signing">
                    <span>Include Signing Certificate in Metadata</span>
                <#else>
                    <input type="checkbox" id="signing" value="true" name="signing">
                    <span>Include Signing Certificate in Metadata</span>
                </#if>
                <div class="description">
                <span> Include signing certificate in the SAML Metadata to inform IdP that SP has signed request. To disable signing request, please uncheck this value and also uncheck <b>Send Signed Requests</b> in <a
                        href="listidp.action">Configure IDP</a> section.</span>
                </div>
                <#if settings.getSignedRequest() == false && settings.getSigning() == true>
                    <div class="description">
                        <div class="aui-message aui-message-warning">
                            <span> Warning: Signed Request is disabled in SP configuration. Please uncheck this value.</span>
                        </div>
                    </div>
                </#if>
            </div>
            <br>
            <div class="">
                <#if encryption == true>
                    <input type="checkbox" checked="true" id="encryption" value="true" name="encryption">
                    <span>Include Encryption Certificate in Metadata</span>
                <#else>
                    <input type="checkbox" id="encryption" value="true" name="encryption">
                    <span>Include Encryption Certificate in Metadata</span>
                </#if>
                <div class="description">
                    <span> Include encryption certificate in the SAML Metadata. This allows the IdP to encrypt Assertions.</span>
                </div>

                <#if isJCEInstalled == false && encryption == true>
                    <div class="description">
                        <div class="aui-message aui-message-warning">
                        <span>Warning: The Java Cryptography Extension (JCE) Unlimited Strength Jurisdiction Policy Files
                            are not installed on this system. It is likely that decrypting Assertions will fail.
                        <br>Download JCE Unlimited Strength Jurisdiction Policy Files for your java version.
                            Extract the jar files from the zip and save them in <b>${java.home}/jre/lib/security/</b></span>
                        </div>
                    </div>
                </#if>
            </div>
            <br>
            <div style="text-align:center">
                <input type="button" value="Save" id="submit_metadata" class="aui-button aui-button-primary" style="width:100px;"/>
            </div>
        </form>
    </div>
    <br>
</section>
