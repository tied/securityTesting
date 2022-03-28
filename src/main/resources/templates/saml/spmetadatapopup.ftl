<section role="dialog" id="sp_metadata_dialog" class="aui-layer aui-dialog2 aui-dialog2-medium sp_metadata_dialog"
         style="display:none; height: auto;width: 60%">
    <header class="aui-dialog2-header">
        <h3 class="aui-dialog2-header-main" style="max-width: none">SP Information</h3>
        <a id="sp_metadata_dialog_close_button" class="aui-dialog2-header-close">
            <span class="aui-icon aui-icon-small aui-iconfont-close-dialog">Close</span>
        </a>
    </header>
    <div class="aui-dialog2-content" style="padding: 10px 40px; padding-bottom: 20px; height: auto;">
        <p><i>Provide this metadata to your Identity Provider to enable Bamboo as a service provider/relying party:</i><br>

        <p>&nbsp;&nbsp;&nbsp;&nbsp; <b>Metadata URL:</b> &nbsp;&nbsp;
            <#assign metadata = settings.getSpBaseUrl() + "/plugins/servlet/saml/metadata">
            <a href="${metadata}" target="_blank" class="aui-button aui-button-link"><b>${metadata}</b></a>
        </p>
        <p style="padding-left: 60px">OR</p>
        <p>&nbsp;&nbsp;&nbsp;&nbsp; <b>Metadata XML:</b> &nbsp;&nbsp;
            <#assign downloadMetadataEndpoint = settings.getSpBaseUrl() + "/plugins/servlet/saml/downloadmetadata">
            <a href="${downloadMetadataEndpoint}">
                <input type="button" class="aui-button" value="Download Metadata" id="download_metadata">
            </a>
        </p>
        <p style="padding-left: 60px">OR</p>
        </p>
        <p><i>If you wish to add the metadata manually, you will find the URLs below:</i><br>

            <table class="aui aui-table-interactive">
                <tbody>
                <tr>
                    <td headers="name" width="30%">
                        <b>SP Entity ID / Issuer</b>
                        <br>
        <p style="font-size: 13px"><i>Note: Also known as <b>Audience URL</b></i></p>
        </td>
        <td headers="type" width="50%">
            <p id="p1" class="copyable" title="Click to Copy">${settings.getSpEntityId()}</p>
        </td>
        <td width="15%" style="text-align: center">
            <span id="p1-copied-message" title="Copied" class="show-title" style="display:none"></span>
        </td>
        </tr>
        <tr>
            <td headers="name">
                <b>ACS URL</b>
                <br>
                <p style="font-size: 13px"><i>Note: Also known as <b>Destination</b> or <b>Recipient URL</b></i>
                </p>
            </td>
            <td headers="type">
                <p id="p2" class="copyable" title="Click to Copy">${settings.getLoginServletUrl()}</p>
            </td>
            <td width="15%" style="text-align: center">
                <span id="p2-copied-message" title="Copied" class="show-title" style="display:none"></span>
            </td>
        </tr>
        <tr>
            <td headers="name">
                <b>Single Logout URL</b>
            </td>
            <td headers="type">
                <p id="p3" class="copyable" title="Click to Copy">${settings.getLogoutServletUrl()}</p>
            </td>
            <td width="15%" style="text-align: center">
                <span id="p3-copied-message" title="Copied" class="show-title" style="display:none"></span>
            </td>
        </tr>
        <tr>
            <td headers="name">
                <b>Certificate</b>
            </td>
            <td headers="type">
                <a href="downloadcertificate.action">
                    <input type="button" class="aui-button aui-button-link" value="Download"/>
                </a>

                &nbsp;&nbsp;&nbsp;
                <!-- Link to show or hide certificate details -->
                <a class="aui-button aui-button-link" title="Copy to clipboard"
                   onclick="copyCertificate('#p4','#c4')"><i
                        class="fa fa-copy"></i> Copy Certificate
                </a>
                <!-- Certificate details. certificateInfo map is used. Hidden by default. -->

                <div class="description" style="font-size:12px;">Note: If the IdP requires signed requests, the
                    IdP will
                    need this certificate to validate requests. It is also used to decrypt encrypted SAML
                    Assertions
                    from the IdP. After download, open in notepad to copy certificate. You can configure your
                    own
                    certificates from <a href="customcerts.action">here</a>.
                </div>
            </td>
            <td width="15%" style="text-align: center">
                <p id="p4" style="font-family:Courier New;display: none" cols="64"
                   rows="4">${settings.getPublicSPCertificate()}
                </p>
                <span id="c4" title="Copied" class="show-title" style="display:none"></span>
            </td>
        </tr>
        </tbody>
        </table>
        <div class="aui-message aui-message-info">
            <p>If you want to customize the Service Provider Metadata,
                <a href="spmetadata.action" style="cursor:pointer;">Click Here</a>
            </p>
        </div>
    </div>
    <br>
</section>